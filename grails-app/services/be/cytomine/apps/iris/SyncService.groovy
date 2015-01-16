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

    /**
     * Synchronizes a user with the Cytomine instance.
     *
     * @param cmUser the Cytomine user to be synced
     * @return the IRIS user (either a new instance or an existing one which got updated if anything changed)
     *
     * @throws Exception
     */
    IRISUser synchronizeUser(User cmUser) throws Exception {
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
                irisUser.save(flush: true)
            } else {
                log.debug("Mapped user requires NO synchronization.")
            }
        }
        return irisUser
    }

    /**
     * Synchronizes a project's settings for a given user with the Cytomine instance.
     *
     * @param cytomine a Cytomine instance
     * @param cmProjectID the Cytomine project ID to be synced
     * @return the IRIS project
     * @throws CytomineException
     * @throws Exception
     */
    IRISProject synchronizeProject(Cytomine cytomine, IRISUser user, Long cmProjectID)
            throws CytomineException, Exception {
        DomainMapper domainMapper = new DomainMapper(grailsApplication)

        try {
            // perform lookup and see if the project is still available to this user
            Project cmProject = cytomine.getProject(cmProjectID)

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
                settings = new IRISUserProjectSettings(
                        user: user,
                        cmProjectID: cmProject.getId(),
                )
                settings.save()
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
                // DO NOT COMPUTE THE PROGRESS HERE, USE ANOTHER SERVICE METHOD!
                // ##################################################################
//                settings = computeUserProgress(cytomine,user,irisImage,cmImage.getId())
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
        Cytomine cm = new Cytomine(cytomine.host, cytomine.publicKey, cytomine.privateKey, cytomine.basePath)

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
        return ['labeledAnnotations':labeledAnnotations as Long, 'totalAnnotations': totalAnnotations as Long]
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
}
