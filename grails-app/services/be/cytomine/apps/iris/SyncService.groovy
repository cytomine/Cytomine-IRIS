
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
import be.cytomine.apps.iris.sync.RemoteConfiguration
import be.cytomine.client.Cytomine
import be.cytomine.client.CytomineException
import be.cytomine.client.collections.AnnotationCollection
import be.cytomine.client.collections.PropertyCollection
import be.cytomine.client.models.Annotation
import be.cytomine.client.models.ImageInstance
import be.cytomine.client.models.Project
import be.cytomine.client.models.Property
import be.cytomine.client.models.User
import grails.converters.JSON
import grails.transaction.Transactional
import org.apache.commons.beanutils.PropertyUtils
import org.json.simple.JSONObject

import java.text.SimpleDateFormat

/**
 * Synchronization methods for keeping the IRIS DB updated with changes in Cytomine.
 *
 * @author Philipp Kainz
 * @since 2.0
 */
@Transactional
class SyncService {

    def grailsApplication
    def imageService
    def activityService
    def executorService
    def annotationService
    def mailService

    /**
     * Synchronizes a user with the Cytomine instance.
     * Essentially, this creates the user in the IRIS database.
     *
     * @param cmUser the Cytomine user to be synced
     * @return the IRIS user (either a new instance or an existing one which got updated if anything changed)
     *
     * @throws CytomineException
     * @throws Exception
     */
    IRISUser synchronizeUser(User cmUser) throws CytomineException, Exception {
        // try to locate the object in the IRIS db
        IRISUser irisUser = IRISUser.findByCmID(cmUser.getId())
        boolean userExists = (irisUser != null)

        // create a temporary object
        IRISUser tmp = new IRISUser()
        if (userExists) {
            PropertyUtils.copyProperties(tmp, irisUser)
        }
        // generate a new user, if the user does not yet exist, otherwise update the
        // user information from Cytomine
        irisUser = new DomainMapper(grailsApplication).mapUser(cmUser, irisUser)
        if (!userExists) {
            log.debug("Inserting new user.")
            irisUser.setSynchronize(true)
            activityService.logUserCreate(cmUser)
            irisUser.save(flush: true)
        } else {
            if (irisUser.synchronize && irisUser.cmUpdated > tmp.cmUpdated) {
                log.debug("Mapped user requires synchronization.")
                activityService.logUserUpdate(cmUser)
                irisUser.merge(flush: true)
            } else {
                log.debug("Mapped user requires NO synchronization.")
            }
        }
        return irisUser
    }

    /**
     * Synchronizes a user with the Cytomine instance without the user's API keys.
     * Essentially, any user can call this method to add any other user to the IRIS database using the
     * publicly available personal information.
     *
     * @param cmUser the Cytomine user to be synced
     * @return the IRIS user (either a new instance or an existing one which got updated)
     *
     * @throws CytomineException if the user cannot be found
     * @throws Exception
     */
    IRISUser synchronizeUserNoAPIKeys(User cmUser) throws CytomineException, Exception {
        // try to locate the object in the IRIS db
        IRISUser irisUser = IRISUser.findByCmID(cmUser.getId())
        boolean userExists = (irisUser != null)

        // create a temporary object
        IRISUser tmp = new IRISUser()
        if (userExists) {
            PropertyUtils.copyProperties(tmp, irisUser)
        }
        // generate a new user, if the user does not yet exist, otherwise update the
        // user information from Cytomine without the user's API keys
        irisUser = new DomainMapper(grailsApplication).mapUser(cmUser, irisUser, ['noAPIKeys':true])
        if (!userExists) {
            log.debug("Inserting new user.")
            irisUser.setSynchronize(true)
            activityService.logUserCreate(cmUser)
            irisUser.save(flush: true)
        } else {
            log.debug("Mapped user has been synchronized.")
            activityService.logUserUpdate(cmUser)
            irisUser.merge(flush: true)
        }
        return irisUser
    }

    /**
     * Synchronizes a project's settings for a given user with the Cytomine instance.
     *
     * @param cytomine a Cytomine instance
     * @param user the IRIS user, this project is synced for
     * @param cmProjectID the Cytomine project ID to be synced
     * @param cmProject the be.cytomine.client.Project, per default null, but if given
     *                  no REST API call to Cytomine will be done
     * @return the IRIS project
     * @throws CytomineException
     * @throws Exception
     */
    IRISProject synchronizeProject(Cytomine cytomine, IRISUser user, Long cmProjectID, Project cmProject=null)
            throws CytomineException, Exception {
        DomainMapper domainMapper = new DomainMapper(grailsApplication)

        try {
            // perform lookup and see if the project is still available to this user
            if (cmProject == null){
                cmProject = cytomine.getProject(cmProjectID)
            }

            // if the project cannot be found for that user
            if (cmProject.get('success') == false){
                throw new CytomineException(403, cmProject.get("errors"))
            }

            // map the project with the Cytomine instance
            IRISProject irisProject = domainMapper.mapProject(cmProject, null)

            // flag the project as interesting to this IRIS instance
            RemoteConfiguration rc = writeCytomineIRISConfig(cytomine, cmProject.getId(),
                    IRISConstants.CM_PROJECT_DOMAINNAME)

            // check for the local settings
            IRISUserProjectSettings settings = IRISUserProjectSettings
                    .findByUserAndCmProjectID(user, cmProjectID)

            // if no local settings exist, the user has not accessed this project so far
            // write access of the settings is just required at first access
            if (!settings) {
                IRISUserProjectSettings.withTransaction {
                    settings = new IRISUserProjectSettings(
                            user: user,
                            cmProjectID: cmProject.getId(),
                    )
                    settings.save(flush: true)
                }
            }

            // 'un-delete' the project and let the user resume at the last image
            // this covers the situation, where the user has been temporarily removed from a project
            if (settings.deleted != null) {
                settings.deleted = null
            }

            // set the ontology ID to the project settings
            settings.setCmOntologyID(irisProject.getCmOntologyID())

            // inject the settings to the transient model
            irisProject.setSettings(settings)

            // return the transient model
            return irisProject

        } catch (CytomineException ex) {
            String message = "The requested project [" + cmProjectID +
                    "] is currently not available for user '" + user.cmID + "'."
            log.warn(message)
            activityService.logRead(user, ex.message ? ex.message : message)

            // if the project is not available by this ID (404, non-existent, deleted,
            // user (temporarily) removed)
            // mark the user project entry 'deleted'
            IRISUserProjectSettings.withTransaction {
                IRISUserProjectSettings settings = IRISUserProjectSettings
                        .findByUserAndCmProjectID(user, cmProjectID)
                settings?.deleted = new Date().getTime()
                settings?.save()
            }

            // remove the very same project ID from the session of this user
            IRISUserSession.withTransaction {
                IRISUserSession session = IRISUserSession.findByUser(user)
                session?.setCurrentCmProjectID(null)
                session?.save()
            }

            // finally return null (or re-throw cytomine exception)
//            return null
            throw ex
        }
    }

    /**
     * Gets the remote configuration of a Cytomine domain model. If no remote configuration for this model exists,
     * a new default will be written as property.
     *
     * @param cytomine a Cytomine instance
     * @param domainModelID the Cytomine client domain model ID
     * @param domainModelName the fully qualified name of the domain model
     * @return a RemoteConfiguration for the Cytomine domain model
     * @throws CytomineException
     * @throws Exception
     */
    RemoteConfiguration writeCytomineIRISConfig(Cytomine cytomine, Long domainModelID, String domainModelName) throws CytomineException, Exception {
        // get the properties from the Cytomine instance
        PropertyCollection properties = cytomine.getDomainProperties(domainModelName, domainModelID)
        String propertyKey = grailsApplication.config.grails.cytomine.apps.iris.sync.clientIdentifier
        JSONObject property = properties.list.find { property ->
            property['key'] == propertyKey
        }
        if (property != null) {
            String configStr = property.get("value")
            // build remote config object from the string
            RemoteConfiguration rc = new DomainMapper(grailsApplication).mapRemoteConfiguration(configStr, null)
            return rc
        } else {
            // set the initial configuration on the remote object
            // thus, flag it as interesting to this IRIS instance
            RemoteConfiguration rc = RemoteConfiguration.createDefault(grailsApplication)
            def rcJSON = rc as JSON

            // add default property to the remote instance
            // CAUTION: this also overwrites the existing property!
            Property prop = cytomine.addDomainProperties(domainModelName, domainModelID,
                    propertyKey, rcJSON.toString(true));

            if (prop == null) {
                throw new CytomineException(503, "Cannot set IRIS property on remote domain model [" + domainModelName + "].")
            }
            return rc
        }
    }

    /**
     * Removes the remote configuration of a Cytomine domain object.
     *
     * @param cytomine a Cytomine instance
     * @param domainModelID the Cytomine client domain model ID
     * @param domainModelName the fully qualified name of the domain model
     * @return a RemoteConfiguration for the Cytomine domain model
     * @throws CytomineException
     * @throws Exception
     */
    void removeCytomineIRISConfig(Cytomine cytomine, Long domainModelID, String domainModelName)
            throws CytomineException, Exception {
        // get the properties from the Cytomine instance
        PropertyCollection properties = cytomine.getDomainProperties(domainModelName, domainModelID)
        String propertyKey = grailsApplication.config.grails.cytomine.apps.iris.sync.clientIdentifier

        // find the property
        JSONObject property = properties.list.find { property ->
            property['key'] == propertyKey
        }

        if (property != null) {
            // remove property from the remote instance
            cytomine.deleteDomainProperty(domainModelName, property['id'] as Long, domainModelID)
        }
    }

    /**
     * Synchronizes an ImageInstance's settings for a given user with the Cytomine instance.
     *
     * @param cytomine a Cytomine instance
     * @param user the IRISUser
     * @param irisProject the IRISProject
     * @param cmImageInstID the Cytomine project ID to be synced
     * @return the IRISImage
     */
    IRISImage synchronizeImage(Cytomine cytomine, IRISUser user, IRISProject irisProject, Long cmImageInstID)
            throws CytomineException, Exception {
        DomainMapper domainMapper = new DomainMapper(grailsApplication)

        // perform lookup and see if the image is available to this user
        try {
            // throws 404 if the image is not found, user has no permissions, or has been deleted on Cytomine
            ImageInstance cmImage = cytomine.getImageInstance(cmImageInstID)

            // map the image with the Cytomine instance
            IRISImage irisImage = domainMapper.mapImage(cmImage, null, irisProject.cmBlindMode)

            // flag this particular image as interesting to this IRIS instance
            RemoteConfiguration rc = writeCytomineIRISConfig(cytomine,
                    cmImageInstID, IRISConstants.CM_IMAGEINSTANCE_DOMAINNAME)

            // check for the local settings
            IRISUserImageSettings settings = IRISUserImageSettings
                    .findByUserAndCmImageInstanceID(user, cmImageInstID)

            // if no local settings exist, write them
            if (!settings) {
                settings = new IRISUserImageSettings(
                        user: user,
                        cmImageInstanceID: cmImage.getId(),
                        cmProjectID: irisProject.cmID
                )
                settings.save()

                // ##################################################################
                // COMPUTE THE PROGRESS USING ANOTHER SERVICE METHOD
                // ##################################################################
                //settings = computeUserProgress(cytomine,user,irisImage,cmImage.getId())
            }

            // if the settings have been deleted, 'un-delete' them on next access to have the
            // current annotations ready
            if (settings.deleted != null) {
                settings.deleted = null
            }

            // put the settings to the image
            irisImage.setSettings(settings)

            return irisImage

        } catch (CytomineException ex) {
            String message = "The requested image [" + cmImageInstID +
                    "] is not available for user '" + user.cmID + "'."
            log.warn(message)

            activityService.logRead(user, message + ex)

            // INVALIDATE THE IMAGE SETTINGS
            // if the image is not available by this ID (404, non-existent, deleted)
            // do DB cleanup operations
            // update ALL users' current status (remove the current image)
            removeCurrentImageFromProjects(cmImageInstID)

            // invalidate ALL user progress records on that image
            invalidateAllImageSettings(cmImageInstID)

            // finally return null (or re-throw cytomine exception)
//          return null
            throw ex
        }
    }

    /**
     * Removes the settings for all users in an image in a batch.
     *
     * @param cmPjID the Cytomine project ID
     */
    void invalidateAllProjectSettings(Long cmPjID) throws Exception {
        def query = IRISUserProjectSettings.where { cmProjectID == cmPjID }
        query.updateAll(deleted: new Date().getTime())
    }

    /**
     * Invalidates the settings for all users in an image in a batch.
     *
     * @param cmImageID the Cytomine image ID
     */
    void invalidateAllImageSettings(Long cmImageID) throws Exception {
        def query = IRISUserImageSettings.where { cmImageInstanceID == cmImageID }
        query.updateAll(deleted: new Date().getTime())
    }

    /**
     * Sets the current image ID in the projects to null. This is a batch operation.
     *
     * @param cmImageID the Cytomine image ID
     */
    void removeCurrentImageFromProjects(Long cmImageID) throws Exception {
        def query = IRISUserProjectSettings.where { currentCmImageInstanceID == cmImageID }
        query.updateAll(currentCmImageInstanceID: null)
    }

    /**
     * Computes the annotation progress of a user in an image. Retrieves the annotations for that user. Each
     * annotation has to have at least one term assigned by the user,
     * otherwise this does not attribute to the progress.
     *
     * @param cytomine a Cytomine instance
     * @param projectID the Cytomine project id
     * @param cmImageID the Cytomine image id
     * @param user the Cytomine user id
     *
     * @return a IRISUserImageSettings object which contains progress information for the user
     */
    IRISUserImageSettings computeUserProgress(Cytomine cytomine, Long projectID, Long cmImageID, IRISUser user)
            throws CytomineException, Exception {
        // clone the cytomine object and retrieve annotations without pagination
        Cytomine cm = new Cytomine(cytomine.getHost(), cytomine.getPublicKey(), cytomine.getPrivateKey())

        // define the filter for the query
        Map<String, String> filters = new HashMap<String, String>()
        filters.put("project", String.valueOf(projectID))
        filters.put("image", String.valueOf(cmImageID))

        // get all annotations of this image
        AnnotationCollection annotations = cm.getAnnotations(filters)

        def progressInfo = computeUserProgress(annotations, user)

        IRISUserImageSettings settings
        IRISUserImageSettings.withTransaction {
            settings = IRISUserImageSettings
                    .findByUserAndCmImageInstanceID(user, cmImageID)

            settings?.setLabeledAnnotations(progressInfo['labeledAnnotations'] as Long)
            settings?.setNumberOfAnnotations(progressInfo['totalAnnotations'] as Long)
            settings?.computeProgress()

            settings?.merge(flush: true)
        }

        // return the settings
        return settings
    }

    /**
     * Computes the annotation progress of a user in an image. Retrieves the annotations for the user. Each
     * annotation has to have at least one term assigned by the user,
     * otherwise this does not attribute to the progress.
     *
     * @param cmImageID the Cytomine image id
     * @param annotations existing Cytomine AnnotationCollection
     * @param user the Cytomine user to search for
     *
     * @return a map ['labeledAnnotations':labeledAnnotations, 'totalAnnotations': totalAnnotations]
     */
    def computeUserProgress(AnnotationCollection annotations, IRISUser user)
            throws Exception {

        int labeledAnnotations = 0

        // total annotations in a given image
        int totalAnnotations = annotations.size();

        // count the annotations per user
        for (int i = 0; i < totalAnnotations; i++) {
            Annotation annotation = annotations.get(i)
            // grab all terms from all users for the current annotation
            List userByTermList = annotation.getList("userByTerm");
            for (assignment in userByTermList) {
                List userList = assignment.get("user").toList()

                // if the user has assigned a label to this annotation, increase the counter
                if (user.cmID in userList) {
                    labeledAnnotations++
                }
            }
        }

        // return the settings
        return ['labeledAnnotations': labeledAnnotations as Long, 'totalAnnotations': totalAnnotations as Long]
    }

    /**
     * Increment the number of labeled annotations in an image for a given user.
     *
     * @param user the IRISUser
     * @param cmImageID the Cytomine image ID
     *
     * @return true if successful
     * @throws Exception on any error
     */
    boolean incrementLabeledAnnotations(IRISUser user, Long cmImageID) throws Exception {

        IRISUserImageSettings settings = IRISUserImageSettings.
                findByUserAndCmImageInstanceID(user, cmImageID)

        if (settings.labeledAnnotations < settings.numberOfAnnotations) {
            settings.labeledAnnotations++
        } else {
            settings.labeledAnnotations = settings.numberOfAnnotations
        }
        // compute new progress
        settings.computeProgress()
        settings.merge(flush: true)

        return true
    }

    /**
     * Decrement the number of labeled annotations in an image for a given user.
     *
     * @param user the IRISUser
     * @param cmImageID the Cytomine image ID
     *
     * @return true , if successful
     * @throws Exception on any error
     */
    boolean decrementLabeledAnnotations(IRISUser user, Long cmImageID) throws Exception {

        IRISUserImageSettings settings = IRISUserImageSettings.
                findByUserAndCmImageInstanceID(user, cmImageID)

        if (settings.labeledAnnotations > 0) {
            settings.labeledAnnotations--
        } else {
            settings.labeledAnnotations = 0
        }

        // compute new progress
        settings.computeProgress()
        settings.merge(flush: true)

        return true
    }

    /**
     * Synchronizes the labeling progress for particular users.
     * The query can be refined for particular images or for the entire project.
     *
     * @param cytomine a Cytomine instance
     * @param irisUser the executing IRISUser
     * @param cmProjectID the Cytomine project ID
     * @param cmUserID a single Cytomine user ID or null, if all project users should be synced
     * @param imageIDs a string of Cytomine image instance IDs, comma-separated
     * @return
     * @throws Exception
     * @throws CytomineException
     */
    def synchronizeUserLabelingProgress(Cytomine cytomine, IRISUser irisUser,
                                        Long cmProjectID, Long cmUserID, String queryImageIDs)
            throws Exception, CytomineException {

        // an array to record sync exceptions in
        def syncExceptions = []

        log.info("Starting user labeling progress synchronization...")

        try {
            List<IRISUser> irisUsers
            // if we want to sync all users at once
            if (cmUserID == null) {
                // get all users
                IRISUser.withTransaction {
                    def userCriteria = IRISUser.createCriteria()
                    irisUsers = userCriteria.list {
                        and {
                            isNull('cmDeleted')
                            not {
                                'in'('cmUserName', ['system', 'admin', 'superadmin'])
                            }
                        }
                    }
                }
            } else {
                // just sync one specific user
                IRISUser.withTransaction {
                    def userCriteria = IRISUser.createCriteria()
                    irisUsers = userCriteria.list {
                        and {
                            eq('cmID', cmUserID) // one specific user
                            not {
                                'in'('cmUserName', ['system', 'admin', 'superadmin'])
                            }
                        }
                    }
                }
            }

            String userNameStr = irisUsers.cmUserName.join(",")

//          if the query did not result in any user instances, return true
            if (checkSkip(irisUsers)) {
                String msg = "No eligible user(s) found, skipping sync."
                log.info(msg)
                executorService.execute({
                    activityService.logSync(msg)
                })
                return true;
            } else {
                String msg = "Synchronizing labeling progress for user(s) [" + userNameStr + "]"
                log.info(msg)
                executorService.execute({
                    activityService.logSync(msg)
                })
                assert irisUsers.size() != 0
            }

            // check the the project in the DB
            Long projectID
            IRISUserProjectSettings.withTransaction {
                def prjCriteria = IRISUserProjectSettings.createCriteria()
                projectID = prjCriteria.get {
                    and {
                        eq('cmProjectID', cmProjectID) // one specific project
                        isNull('deleted')
                    }
                    projections { // get a unique result (the ID as Long)
                        distinct('cmProjectID')
                    }
                }
            }

//          if the query did not result in any project, return true
            if (checkSkip(projectID)) {
                String msg = "No eligible project found for user(s) [" + userNameStr + "], skipping sync."
                log.info(msg)
                executorService.execute({
                    activityService.logSync(msg)
                })
                return true;
            } else {
                String msg = "Synchronizing progress for project [" + projectID + "]"
                log.info(msg)
                executorService.execute({
                    activityService.logSync(msg)
                })
                assert projectID != null
            }

            // for the particular project, get the image(s)
            List imageIDs
            // if the queryImageIDs are null, sync all images in the project
            if (queryImageIDs == null) {
                // get ALL images
                IRISUserImageSettings.withTransaction {
                    def imgCriteria = IRISUserImageSettings.createCriteria()
                    imageIDs = imgCriteria.list {
                        and {
                            eq('cmProjectID', projectID)
                            isNull('deleted')
                        }
                        projections { // just get the IDs as Long
                            distinct('cmImageInstanceID')
                        }
                    }
                }
            } else {

                // make query image ID array
                def queryImageIDArray = queryImageIDs.split(",").collect { Long.valueOf(it) }

                // otherwise get just the specified image(s)
                IRISUserImageSettings.withTransaction {
                    def imgCriteria = IRISUserImageSettings.createCriteria()
                    imageIDs = imgCriteria.list {
                        and {
                            eq('cmProjectID', projectID) // just the specific project
                            'in'('cmImageInstanceID', queryImageIDArray) // just the specific image indices
                            isNull('deleted')
                        }
                        projections { // just get the IDs as Long
                            distinct('cmImageInstanceID')
                        }
                    }
                }
            }

            String imageIDStr = imageIDs.join(",")

//          if the query did not result in any images in the project, return true
            if (checkSkip(imageIDs)) {
                String msg = "No eligible image(s) found for project [" + projectID + "], skipping sync."
                log.info(msg)
                executorService.execute({
                    activityService.logSync(msg)
                })
                return true;
            } else {
                String msg = "Synchronizing progress for image(s) [" + imageIDStr + "]"
                log.info(msg)
                executorService.execute({
                    activityService.logSync(msg)
                })
                assert imageIDs != []
            }

            int nImages = imageIDs.size()

            // for each image, get its annotations and compute the progress for every user
            for (int i = 0; i < nImages; i++) {
                Long imageID = imageIDs[i]

                try {
                    List<IRISUser> imageUsers
                    // get the users per image and compute their progress
                    IRISUserImageSettings.withTransaction {
                        def imgCriteria = IRISUserImageSettings.createCriteria()
                        imageUsers = imgCriteria.list {
                            and {
                                eq('cmImageInstanceID', imageID)
                                isNull('deleted')
                            }
                            projections {
                                distinct('user')
                            }
                        }
                    }

                    // get all annotations (including ACL check for that user)
                    AnnotationCollection allImageAnnotations

                    try {
                        // create the cytomine connection for that user
                        Cytomine cytomine2 = new Cytomine(cytomine.getHost(),
                                imageUsers[0].cmPublicKey, imageUsers[0].cmPrivateKey)

                        allImageAnnotations = annotationService.getImageAnnotationsLight(cytomine2,
                                imageUsers[0], null, imageID)

                    } catch (Exception ex) {
                        addSyncException(ex, "User '" + imageUsers[0].cmUserName + "' is not allowed to access " +
                                "image [" + imageID + "].", syncExceptions)
                    }

                    // skip the image if there are no annotations!
                    if (allImageAnnotations == null || allImageAnnotations.isEmpty()) {
                        log.info("Skipping computing user progress for this image, since no annotations can be found.")
                        continue
                    }

                    imageUsers.each { user ->
                        try {
                            def progressInfo = computeUserProgress(allImageAnnotations, user)
                            int nLabeled = progressInfo['labeledAnnotations']
                            int nTotal = progressInfo['totalAnnotations']

                            IRISUserImageSettings.withTransaction {
                                // store the record of this user
                                IRISUserImageSettings settings = IRISUserImageSettings
                                        .findByUserAndCmImageInstanceID(user, imageID)

                                settings?.lock()
                                settings?.setLabeledAnnotations(nLabeled)
                                settings?.setNumberOfAnnotations(nTotal)
                                settings?.computeProgress()

                                settings?.merge(flush: true)
                            }

                            log.trace("Done synchronizing image [" + imageID + "] for user '" + user.cmUserName + "'.")
                        } catch (Exception e) {
                            String msg = "Cannot synchronize image [" + imageID +
                                    "] for user '" + user.cmUserName + "'."
                            addSyncException(e, msg, syncExceptions)
                        }
                    }
                    String msg = "Done synchronizing image [" + imageID + "]."
                    log.info(msg)
                    activityService.logSync(msg)
                } catch (Exception e) {
                    addSyncException(e, "Cannot synchronize image [" + imageID + "]!", syncExceptions)
                }
            }

        } catch (Exception ex) {
            String msg = "The synchronization failed! This is a serious global error, you should act quickly!"
            log.fatal(msg, ex)
            // GLOBAL ERROR
            addSyncException(ex, msg, syncExceptions)
        }

        if (!syncExceptions.isEmpty()) {
            String recipient = grailsApplication.config.grails.cytomine.apps.iris.server.admin.email

            log.info("User synchronization succeeded, but had some errors! Sending email to admin...")

            // notify the admin
            mailService.sendMail {
                async true
                to recipient
                subject new SimpleDateFormat('E, yyyy-MM-dd').format(new Date()) + ": Progress synchronization had some errors"
                body('Errors occurred during scheduled user progress synchronization. See stack traces below. \n\n\n'
                        + exceptionsToString(syncExceptions)
                )
            }
        } else {
            log.info("Splendit! All synchronizations completed without errors :-)")
        }

        log.info("Done synchronizing user progress.")

        return syncExceptions
    }

    /**
     * Checks whether the sync of an item should be skipped
     * @param item
     * @return
     */
    boolean checkSkip(def item) {
        boolean skip = false

        if (item == null)
            skip = true
        else if (item instanceof Collection)
            if (item.isEmpty())
                skip = true

        return skip
    }

    /**
     * Adds an Exception to the list of errors which will be sent to the admin.
     *
     * @param e
     * @param syncExceptions
     * @param msg
     */
    void addSyncException(Exception e, String msg, def syncExceptions) {
        syncExceptions.add(['msg': msg, 'exception': e])
        log.error(msg, e)
        executorService.execute({
            activityService.logSync("ERROR\n" + msg)
        })
    }

    /**
     * Prints the exceptions in a reverse chronologically stack of messages/exceptions.
     *
     * @params syncExceptions
     * @return
     */
    String exceptionsToString(def syncExceptions) {
        String exStr = "\nEXCEPTIONS ARE ORDERED REVERSE CHRONOLOGICALLY (MOST RECENT FIRST)"
        syncExceptions.reverse()
        syncExceptions.each { item ->
            exStr += (item['msg'] + "\n" + item['exception'] + "\n " +
                    "------------------------------------------------------\n")
        }
    }

}
