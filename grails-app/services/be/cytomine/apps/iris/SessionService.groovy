
/* Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.cytomine.apps.iris

import be.cytomine.apps.iris.model.IRISAnnotation
import be.cytomine.apps.iris.model.IRISImage
import be.cytomine.apps.iris.model.IRISProject
import grails.transaction.Transactional

import org.codehaus.groovy.grails.web.json.JSONElement
import be.cytomine.client.Cytomine
import be.cytomine.client.CytomineException
import be.cytomine.client.models.ImageInstance

/**
 * This service handles all CRUD operations of a Session object.
 * It also communicates with the Cytomine host instance, if 
 * required. Thus passing the <code>request['cytomine']</code> instance 
 * is mandatory for some class methods.
 *
 * @author Philipp Kainz
 *
 */
@Transactional
class SessionService {

    def projectService
    def imageService
    def grailsApplication
    def activityService
    def syncService
    def irisService

    /**
     * Gets the Session for a user identified by public key.
     * If the user has no Session yet, a new one will be created.
     *
     * @param cytomine a Cytomine instance
     * @param publicKey the user's public key
     * @return an IRISUserSession
     *
     * @throws Exception
     * @throws CytomineException
     */
    IRISUserSession getSession(Cytomine cytomine, String publicKey) throws CytomineException, Exception {
        // get the calling user from Cytomine
        be.cytomine.client.models.User cmUser = cytomine.getUser(publicKey);

        if (cmUser == null) {
            throw new CytomineException(404, "This Cytomine user does not exist.")
        }

        // make or synchronize the user object
        IRISUser irisUser = syncService.synchronizeUser(cmUser)

        // try to fetch the session for this user
        IRISUserSession userSession = irisUser.getSession()
        if (userSession == null) {
            // create new empty session
            userSession = new IRISUserSession(user: irisUser)
            irisUser.setSession(userSession)
            userSession.save(flush: true)
            log.debug("Created new user session.")
            activityService.logCreate(irisUser, "Created a new user session [" + userSession.getId() + "]")
        } else {
            log.debug("Accessed user session.")
            activityService.logRead(irisUser, "Accessed session [" + userSession.getId() + "]")
        }

        // return the session
        return userSession
    }

    /**
     * Injects a Cytomine project into the IRIS project (as JSON).
     *
     * @param cytomine a Cytomine instance
     * @param irisProject the IRIS Project
     * @param cmProject the cytomineProject, or <code>null</code>, if the project should be fetched from Cytomine
     * @return a JSON object with the injected Cytomine project
     *
     * @throws CytomineException if the project is is not available for the
     * querying user
     * @throws Exception
     */
    JSONElement injectCytomineProject(Cytomine cytomine, IRISProject irisProject,
                                      def cmProject) throws CytomineException, Exception {

        JSONElement projectJSON = new Utils().modelToJSON(irisProject)

        // fetch the Cytomine project instance
        if (cmProject == null) {
            cmProject = cytomine.getProject(irisProject.cmID)
            if (cmProject == null) {
                throw new CytomineException(404, "The requested project is not available.");
            }
            projectJSON.cytomine = cmProject.getAttr()
        } else {
            if (cmProject['attr'] != null) {
                projectJSON.cytomine = cmProject.getAttr()
            } else {
                projectJSON.cytomine = cmProject
            }
        }
        return projectJSON
    }

    /**
     * Injects a Cytomine Annotation into the IRIS annotation.
     *
     * @param cytomine a Cytomine instance
     * @param irisAnnotation the IRIS Annotation
     * @param cmAnnotation the cytomine Annotation, or <code>null</code>, if the Annotation should be fetched from Cytomine
     * @return a JSON object with the injected Cytomine annotation
     *
     * @throws CytomineException if the annotation is is not available for the
     * querying user
     * @throws Exception
     */
    JSONElement injectCytomineAnnotation(Cytomine cytomine, IRISAnnotation irisAnnotation,
                                         def cmAnnotation) throws CytomineException, Exception {

        JSONElement annJSON = new Utils().modelToJSON(irisAnnotation)

        // fetch the Cytomine project instance
        if (cmAnnotation == null) {
            cmAnnotation = cytomine.getAnnotation(irisAnnotation.cmID)
            annJSON.cytomine = cmAnnotation.getAttr()
        } else {
            if (cmAnnotation['attr'] != null) {
                annJSON.cytomine = cmAnnotation.getAttr()
            } else {
                annJSON.cytomine = cmAnnotation
            }
        }
        return annJSON
    }

    /**
     * Injects a Cytomine Image into the IRIS Image.
     *
     * @param cytomine a Cytomine instance
     * @param irisImage the IRIS Image
     * @param cmImage the Cytomine image, or <code>null</code>, if the image should be fetched from Cytomine
     * @param blindMode flag determining the 'Blind Mode' of the project
     * @return a JSON object with the injected Cytomine project
     *
     * @throws CytomineException if the image is is not available for the
     * querying user
     * @throws Exception
     */
    JSONElement injectCytomineImageInstance(Cytomine cytomine, IRISImage irisImage,
                                            def cmImage, boolean blindMode) throws CytomineException, Exception {
        JSONElement imageJSON = new Utils().modelToJSON(irisImage)

        // fetch the Cytomine image instance
        if (cmImage == null) {
            cmImage = cytomine.getImageInstance(irisImage.cmID)
            imageJSON.cytomine = cmImage.getAttr()
        } else {
            if (cmImage['attr'] != null) {
                imageJSON.cytomine = cmImage.getAttr()
            } else {
                imageJSON.cytomine = cmImage
            }
        }
        // inject/overwrite the file name in each image, if required
        if (blindMode) {
            imageJSON.cytomine.originalFilename = "[BLIND]" + cmImage.getId()
            imageJSON.cytomine.path = null
            imageJSON.cytomine.fullPath = null // this contains the image path for the tiles
            imageJSON.cytomine.extension = null
            imageJSON.cytomine.filename = null
//			imageJSON.cytomine.mime = null
//			imageJSON.cytomine.originalMimeType = null
        }

        return imageJSON
    }

    /**
     * Gets an image for the current user in the current project.
     *
     * @param cytomine a Cytomine instance
     * @param irisUser the IRIS user
     * @param cmProjectID the Cytomine project ID for updating
     * @param cmImageID the Cytomine imageID for updating
     * @return the IRISImage
     *
     * @throws CytomineException if the image or project is is not available for the
     * querying user
     * @throws Exception
     */
    IRISImage getImage(Cytomine cytomine, IRISUser irisUser,
                       Long cmProjectID, Long cmImageID)
            throws CytomineException, Exception {

        IRISProject irisProject = projectService.getProject(cytomine, irisUser, cmProjectID)
        IRISImage irisImage = syncService.synchronizeImage(cytomine, irisUser, irisProject, cmImageID)

        irisProject.settings.setCurrentCmImageInstanceID(cmImageID)
        irisProject.settings.save()

        irisImage.setProjectSettings(irisProject.settings)

        return irisImage
    }

    /**
     * Removes a IRISUserSession from the database.
     *
     * @param user the IRIS user to delete the session from
     *
     * @return <code>null</code>, if the session does not exist
     * @throws Exception if any error occurs during deletion
     */
    void deleteUserSession(IRISUser user) throws Exception {
        IRISUserSession sess = user.getSession()
        sess.delete(flush: true)
    }


    def devTransactional(IRISUser u) {
    }

    def foo(int i) {
        Activity activity = Activity.findByDescription(String.valueOf(i))
        activity = activity.merge(flush: true)
        activity.cmProjectID = new Long(new Random().nextInt(1000000))
//		activity.save()
        println "OK, done (" + activity + ") by Thread " + Thread.currentThread().getName()

        //for (int j = 1; j <= 3; j++) {
        //try {

        //Thread.sleep(new Long(100 + new Random().nextInt(1000)))
        //break
//			} catch (org.hibernate.StaleObjectStateException se) {
//				println "StaleObjectStateException, trying again..."
//				Thread.currentThread().sleep(new Long(1000 + new Random().nextInt(1000)))
//			}
        //}
    }

}
