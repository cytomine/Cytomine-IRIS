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

            long projectID = params.long('cmProjectID')

            int offset = (params['offset'] == null ? 0 : params.int('offset'))
            int max = (params['max'] == null ? 0 : params.int('max'))

            List<IRISImage> images = imageService.getImages(cytomine, irisUser, projectID, offset, max)

            // TODO on first call of the user in this project, also render a message that the
            // computation of the progress information initially may take a while

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

    def activityService
    def executorService
    def irisService

    def dev() {
        def nAnnotations = 5000;
        def nUsers = 10;
//        for (int i = 0; i<nAnnotations;i++){
//            for (int u = 0; u < nUsers; u++){
//                Thread.currentThread().sleep(new Long(new Random().nextInt(10)))
//            }
//        }
        IRISUser u = request['user']
        //sessionService.devTransactional(u)

//		(1..200).each { index ->
//			activityService.log(u, String.valueOf(index))
//		}

        //def list = (1..200)

//		(1..3).each {
//			irisService.appendToDBQueue {
//				println "updating something in the db"
//			}
////			ScheduledTask st = new ScheduledTask({
////				println "closure of task"
////			})
////
////			println "Queueing task [" + st.id + "] for scheduled execution"
////			runAsync {
////				println "Executing scheduled task [" + st.getId() + "]"
////				st.closure()
////			}
//		}

//			// creates new hibernate sessions for the async threads
//		try {
//			// call a transactional service method
//			sessionService.foo(1)
//		} catch (Exception e){
//			render new Utils().resolveException(e, 500) as JSON
//		}
//
//		runAsync {
//			// call a transactional service method
//			sessionService.foo(2)
//		}
//
        render "ok"
    }

    def dev2() {
        try {
            // call a transactional service method
            sessionService.foo(5)
            render "updated activity 5 " + new Date()
        } catch (Exception e) {
            response.setStatus(500)
            render new Utils().resolveException(e, 500) as JSON
        }

    }
}
