
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

import be.cytomine.apps.iris.model.IRISImage
import be.cytomine.apps.iris.model.IRISProject
import be.cytomine.client.Cytomine
import be.cytomine.client.CytomineException
import be.cytomine.client.models.ImageInstance
import be.cytomine.client.models.Project
import grails.converters.JSON
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.json.simple.JSONArray
import org.json.simple.JSONObject

/**
 * A SessionController handles the communication with the client and
 * dispatches existing sessions for users or creates/updates them.
 * <p>
 * It uses the underlying SessionService to communicate with the
 * Cytomine host.
 *
 * @author Philipp Kainz
 *
 */
class SessionController {

    def beforeInterceptor = {
        log.debug("Executing action $actionName with params $params")
    }

    /**
     *  Injected Services for this controller.
     */
    def sessionService
    def imageService
    def projectService
    def grailsApplication
    def syncService
    def activityService

    /**
     * Gets a session object for a user. If the querying user does not have a session,
     * a new one will be created. For each session, the user gets updated from Cytomine and synchronized with the
     * IRIS database.
     *
     * @return the IRISUserSession as JSON object
     */
    def getSession() {
        Cytomine cytomine = request["cytomine"]
        String publicKey = params["publicKey"]
        try {
            IRISUserSession session = sessionService.getSession(cytomine, publicKey)

            Utils utils = new Utils()

            // get and inject the project/image/annotations, if any
            def sessJSON = utils.toJSONObject(session)

            Long cmProjectID = session.getCurrentCmProjectID()

            IRISProject prj
            IRISImage img

            // compute the session state with the recent information
            if (cmProjectID != null) {
                try {
                   prj = syncService.synchronizeProject(cytomine, session.getUser(), cmProjectID)
                } catch (Exception prjEx){
                    log.warn("Project [" + cmProjectID + "] cannot be accessed by '" + session.getUser().cmUserName + "'.")
                }
                if (prj != null) {
                    Long imageInstanceID = prj.settings.getCurrentCmImageInstanceID()
                    if (imageInstanceID != null) {
                        try {
                            img = syncService.synchronizeImage(cytomine, session.getUser(), prj, imageInstanceID)
                        } catch (Exception imgEx){
                            log.warn("Image [" + imageInstanceID + "] cannot be accessed by '" + session.getUser().cmUserName + "'.")
                        }
                    }
                }
            }

            def prjJSON
            if (prj != null){
                prjJSON = utils.toJSONObject(prj)
                prjJSON['currentImage'] = img
            }
            sessJSON['currentProject'] = prjJSON

            render sessJSON as JSON
        } catch (CytomineException e1) {
            log.error(e1)
            // exceptions from the cytomine java client
            response.setStatus(e1.httpCode)
            JSONObject errorMsg = new Utils().resolveCytomineException(e1)
            render errorMsg as JSON
        } catch (Exception e3) {
            log.error(e3)
            // on any other exception render 500
            response.setStatus(500)
            JSONObject errorMsg = new Utils().resolveException(e3, 500)
            render errorMsg as JSON
        }
    }

    /**
     * Resume the IRISUserSession at viewing the image list of the current project.
     */
    def resumeSessionAtImages() {
        forward('action': 'getSession')
    }

    /**
     * Resume the IRISUserSession at viewing the gallery of the current project.
     */
    def resumeSessionAtGallery() {
        forward('action': 'getSession')
    }

    /**
     * Resume the IRISUserSession at the current annotation in the current image of the current project.
     */
    def resumeSessionAtLabeling() {
        forward('action': 'getSession')
    }

    /**
     * Gets an image and sets it as current image in the session.
     *
     * @return the updated image as JSON object
     */
    def getImage() {
        try {
            Cytomine cytomine = request['cytomine']
            IRISUser irisUser = request['user']
            long cmProjectID = params.long('cmProjectID')
            long cmImageID = params.long('cmImageID')

            IRISImage irisImage = sessionService.getImage(cytomine, irisUser, cmProjectID, cmImageID)
            def imageJSON = new Utils().modelToJSON(irisImage)

            render imageJSON
        } catch (CytomineException e1) {
            log.error(e1)
            // exceptions from the cytomine java client
            response.setStatus(e1.httpCode)
            JSONObject errorMsg = new Utils().resolveCytomineException(e1)
            render errorMsg as JSON
        } catch (GroovyCastException e2) {
            log.error(e2)
            // send back 400 if the project ID is other than long format
            response.setStatus(400)
            JSONObject errorMsg = new Utils().resolveException(e2, 400)
            render errorMsg as JSON
        } catch (Exception e3) {
            log.error(e3)
            // on any other exception render 500
            response.setStatus(500)
            JSONObject errorMsg = new Utils().resolveException(e3, 500)
            render errorMsg as JSON
        }
    }

    /**
     * Gets the labeling progress for a specific image in a project.
     * The user is determined by the session.
     *
     * @return the updated image as JSON object
     */
    def labelingProgress() {
        try {
            Cytomine cytomine = request['cytomine']
            IRISUser user = request['user']
            long cmProjectID = params.long('cmProjectID')
            long cmImageID = params.long('cmImageID')

//            // return the currently cached progress
//			IRISUserImageSettings annInfo = IRISUserImageSettings
//					.findByUserAndCmProjectIDAndCmImageInstanceID(user, cmProjectID, cmImageID)
//            annInfo.refresh()

            // TODO this can get really slow but is most accurate
            IRISUserImageSettings annInfo = syncService.computeUserProgress(cytomine, cmProjectID, cmImageID, user)

            render annInfo as JSON
        } catch (CytomineException e1) {
            log.error(e1)
            // exceptions from the cytomine java client
            response.setStatus(e1.httpCode)
            JSONObject errorMsg = new Utils().resolveCytomineException(e1)
            render errorMsg as JSON
        } catch (GroovyCastException e2) {
            log.error(e2)
            // send back 400 if the project ID is other than long format
            response.setStatus(400)
            JSONObject errorMsg = new Utils().resolveException(e2, 400)
            render errorMsg as JSON
        } catch (Exception e3) {
            log.error(e3)
            // on any other exception render 500
            response.setStatus(500)
            JSONObject errorMsg = new Utils().resolveException(e3, 500)
            render errorMsg as JSON
        }
    }

    /**
     * Get all images for the calling user in a specific Cytomine project.
     *
     * @return the list of images for a given project ID as JSON object including the current progress
     */
    def getImages() {
        try {
            Cytomine cytomine = request['cytomine']
            IRISUser irisUser = request['user']

            long cmProjectID = params.long('cmProjectID')

            int offset = (params['offset'] == null ? 0 : params.int('offset'))
            int max = (params['max'] == null ? 0 : params.int('max'))

            // query if there are any settings present
            List<IRISUserImageSettings> settingsListBefore
            IRISUserImageSettings.withTransaction {
                def criteria = IRISUserImageSettings.createCriteria()
                settingsListBefore = criteria.list {
                    and {
                        eq('user', irisUser)
                        eq('cmProjectID', cmProjectID)
                    }
                }
            }

            List<IRISImage> images = imageService.getImages(cytomine, irisUser, cmProjectID, offset, max)

            // if the list is empty, the user has not visited this project yet
            if (settingsListBefore.isEmpty()) {
                // on first call of the user in this project, also compute progress
                syncService.synchronizeUserLabelingProgress(cytomine, irisUser, cmProjectID, irisUser.cmID, null)
            }

            render images as JSON
        } catch (CytomineException e1) {
            log.error(e1)
            // exceptions from the cytomine java client
            response.setStatus(e1.httpCode)
            JSONObject errorMsg = new Utils().resolveCytomineException(e1)
            render errorMsg as JSON
        } catch (GroovyCastException e2) {
            log.error(e2)
            // send back 400 if the project ID is other than long format
            response.setStatus(400)
            JSONObject errorMsg = new Utils().resolveException(e2, 400)
            render errorMsg as JSON
        } catch (Exception e3) {
            log.error(e3)
            // on any other exception render 500
            response.setStatus(500)
            JSONObject errorMsg = new Utils().resolveException(e3, 500)
            render errorMsg as JSON
        }
    }

    /**
     * Deletes a session.
     */
    def deleteSession() {
        try {
            long sessID = params.long('sessionID')
            sessionService.deleteUserSession(sessID)
        } catch (GroovyCastException e2) {
            log.error(e2)
            // send back 400 if the project ID is other than long format
            response.setStatus(400)
            JSONObject errorMsg = new Utils().resolveException(e2, 400)
            render errorMsg as JSON
        } catch (Exception e3) {
            log.error(e3)
            // on any other exception render 500
            response.setStatus(500)
            JSONObject errorMsg = new Utils().resolveException(e3, 500)
            render errorMsg as JSON
        }
    }
}
