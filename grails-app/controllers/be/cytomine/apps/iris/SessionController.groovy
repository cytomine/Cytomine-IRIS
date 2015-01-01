package be.cytomine.apps.iris

import grails.converters.JSON

import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.json.simple.JSONObject

import be.cytomine.client.Cytomine
import be.cytomine.client.CytomineException
import be.cytomine.client.collections.ImageInstanceCollection
import be.cytomine.client.collections.ProjectCollection
import be.cytomine.client.models.ImageInstance

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
	def grailsApplication

	/**
	 * Gets all sessions from the IRIS server.
	 * 
	 * @return a JSON array of Session objects
	 */
	def getAll(){
		// call the session service
		def allSessions = sessionService.getAll() as JSON
		render allSessions
	}

	/**
	 * Gets a session object for a user. If the querying user does not have a session, 
	 * a new one will be created. 
	 * 
	 * @return the Session as JSON object
	 */
	def getSession(){
		try {
			def sessJSON = sessionService.getSession(request["cytomine"], params["publicKey"]) as JSON
			render sessJSON
		} catch(Exception e3){
			// if retrieving session fails, delete the session from the DB by removing the user
			try {
				User user = User.findByCmPublicKey(params["publicKey"])
				user.delete()
			} catch (Exception e) {
				log.error(e)
			}
		
			log.error(e3)
			// on any other exception render 500
			response.setStatus(500)
			JSONObject errorMsg = new Utils().resolveException(e3, 500)
			render errorMsg as JSON
		}
	}

	/**
	 * Gets an IRIS project instance.
	 * @return the IRIS project and the injected Cytomine instance
	 */
	def getProject(){
		try {
			long sID = params.long('sessionID')
			long pID = params.long('projectID')

			def projJSON = sessionService.getProject(request["cytomine"], sID, pID) as JSON
			render projJSON
		} catch(CytomineException e1){
			log.error(e1)
			// exceptions from the cytomine java client
			response.setStatus(e1.httpCode)
			JSONObject errorMsg = new Utils().resolveCytomineException(e1)
			render errorMsg as JSON
		} catch(GroovyCastException e2) {
			log.error(e2)
			// send back 400 if the project ID is other than long format
			response.setStatus(400)
			JSONObject errorMsg = new Utils().resolveException(e2, 400)
			render errorMsg as JSON
		} catch(Exception e3){
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
	def deleteSession(){
		try {
			long sessID = params.long('sessionID')
			sessionService.delete(sessID)
		} catch(GroovyCastException e2) {
			log.error(e2)
			// send back 400 if the project ID is other than long format
			response.setStatus(400)
			JSONObject errorMsg = new Utils().resolveException(e2, 400)
			render errorMsg as JSON
		} catch(Exception e3){
			log.error(e3)
			// on any other exception render 500
			response.setStatus(500)
			JSONObject errorMsg = new Utils().resolveException(e3, 500)
			render errorMsg as JSON
		}
	}

	/**
	 * Update a session.
	 * 
	 * @return the updated session
	 */
	def updateSession(){
		try {
			// parse the payload of the POST request
			def sess = request.JSON

			// fetch the session from the DB
			Session s = Session.get(sess.id)

			s = s.updateByJSON(sess)

			render s as JSON

		} catch(CytomineException e1){
			log.error(e1)
			// exceptions from the cytomine java client
			response.setStatus(e1.httpCode)
			JSONObject errorMsg = new Utils().resolveCytomineException(e1)
			render errorMsg as JSON
		} catch(GroovyCastException e2) {
			log.error(e2)
			// send back 400 if the project ID is other than long format
			response.setStatus(400)
			JSONObject errorMsg = new Utils().resolveException(e2, 400)
			render errorMsg as JSON
		} catch(Exception e3){
			log.error(e3)
			// on any other exception render 500
			response.setStatus(500)
			JSONObject errorMsg = new Utils().resolveException(e3, 500)
			render errorMsg as JSON
		}
	}

	/**
	 * Update a specific project in a session. The payload of the 
	 * PUT request defines the ID to be used for querying the IRIS 
	 * database.
	 * 
	 * @return the updated IRIS project instance
	 */
	def updateProject(){
		try{
			// get the JSON object from the payload
			def payload = (request.JSON)

			if (payload == null){
				throw new IllegalArgumentException("The payload is empty!")
			}

			// extract the class names
			String pldClazz = payload["class"]
			String urlClazz = params["class"]

			if (!pldClazz.equalsIgnoreCase(urlClazz)){
				throw new IllegalArgumentException("Class of payload and URL do not match!")
			}

			Cytomine cytomine = request['cytomine']
			long sessionID = params.long('sessionID')
			long projectID = Long.valueOf(payload['id'])

			// declare the project
			def irisProject = null

			if (pldClazz.equals(Project.class.name)){
				// update using the IRIS project
				irisProject = sessionService.updateByIRISProject(cytomine, sessionID, projectID, payload)
			}

			render (irisProject as JSON)
			
		} catch(CytomineException e1){
			log.error(e1)
			// exceptions from the cytomine java client
			response.setStatus(e1.httpCode)
			JSONObject errorMsg = new Utils().resolveCytomineException(e1)
			render errorMsg as JSON
		} catch(GroovyCastException e2) {
			log.error(e2)
			// send back 400 if the project ID is other than long format
			response.setStatus(400)
			JSONObject errorMsg = new Utils().resolveException(e2, 400)
			render errorMsg as JSON
		} catch(Exception e3){
			log.error(e3)
			// on any other exception render 500
			response.setStatus(500)
			JSONObject errorMsg = new Utils().resolveException(e3, 500)
			render errorMsg as JSON
		}
	}

	/**
	 * Touches a project in a session in order to be the most recent one.
	 * 
	 * @return the updated project as JSON object
	 */
	def touchProject(){
		try {
			Cytomine cytomine = request['cytomine']
			long sessionID = params.long('sessionID')
			long cmProjectID = params.long('cmProjectID')

			def irisProject = sessionService.touchProject(cytomine, sessionID, cmProjectID)
			
			render irisProject as JSON
		} catch(CytomineException e1){
			log.error(e1)
			// exceptions from the cytomine java client
			response.setStatus(e1.httpCode)
			JSONObject errorMsg = new Utils().resolveCytomineException(e1)
			render errorMsg as JSON
		} catch(GroovyCastException e2) {
			log.error(e2)
			// send back 400 if the project ID is other than long format
			response.setStatus(400)
			JSONObject errorMsg = new Utils().resolveException(e2, 400)
			render errorMsg as JSON
		} catch(Exception e3){
			log.error(e3)
			// on any other exception render 500
			response.setStatus(500)
			JSONObject errorMsg = new Utils().resolveException(e3, 500)
			render errorMsg as JSON
		}
	}

	/**
	 * Touches an image in order to be the most recent in a project. 
	 *
	 * @return the updated image as JSON object
	 */
	def touchImage(){
		try {
			Cytomine cytomine = request['cytomine']
			long sessionID = params.long('sessionID')
			long cmProjectID = params.long('cmProjectID')
			long cmImageID = params.long('cmImageID')

			def irisImage = sessionService.touchImage(cytomine, sessionID, cmProjectID, cmImageID)

			render irisImage as JSON
		} catch(CytomineException e1){
			log.error(e1)
			// exceptions from the cytomine java client
			response.setStatus(e1.httpCode)
			JSONObject errorMsg = new Utils().resolveCytomineException(e1)
			render errorMsg as JSON
		} catch(GroovyCastException e2) {
			log.error(e2)
			// send back 400 if the project ID is other than long format
			response.setStatus(400)
			JSONObject errorMsg = new Utils().resolveException(e2, 400)
			render errorMsg as JSON
		} catch(Exception e3){
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
	def labelingProgress(){
		try {
			Cytomine cytomine = request['cytomine']
			long sessionID = params.long('sessionID')
			long cmProjectID = params.long('cmProjectID')
			long cmImageID = params.long('cmImageID')

			Session sess = Session.get(sessionID)
			User u = sess.getUser()

			Utils utils = new Utils()
			// retrieve the user's progress on each image and return it in the object
			JSONObject annInfo = utils.getUserProgress(cytomine, cmProjectID, cmImageID, u.getCmID())
			render annInfo as JSON
		} catch(CytomineException e1){
			log.error(e1)
			// exceptions from the cytomine java client
			response.setStatus(e1.httpCode)
			JSONObject errorMsg = new Utils().resolveCytomineException(e1)
			render errorMsg as JSON
		} catch(GroovyCastException e2) {
			log.error(e2)
			// send back 400 if the project ID is other than long format
			response.setStatus(400)
			JSONObject errorMsg = new Utils().resolveException(e2, 400)
			render errorMsg as JSON
		} catch(Exception e3){
			log.error(e3)
			// on any other exception render 500
			response.setStatus(500)
			JSONObject errorMsg = new Utils().resolveException(e3, 500)
			render errorMsg as JSON
		}
	}

	/**
	 * Build the URLs for the images by adding the proper suffix to the Cytomine host URL.
	 * The project ID is retrieved via the injected <code>params</code> property.
	 *
	 * @return the list of images for a given project ID as JSON object including the current progress
	 */
	def getImages() {
		try {
			Cytomine cytomine = request['cytomine']
			long projectID = params.long("projectID")
			String publicKey = params.get("publicKey")
			Boolean withProgress = Boolean.parseBoolean(params['computeProgress'])
			Boolean withTiles = Boolean.parseBoolean(params['withTileURL'])

			int offset = (params['offset']==null?0:params.int('offset'))
			int max = (params['max']==null?0:params.int('max'))
			
			def imagesObject
			if (withProgress){
				imagesObject = imageService.getImagesWithProgress(cytomine, projectID, publicKey, withTiles)
			} else {
				imagesObject = imageService.getImages(cytomine, projectID, withTiles, offset, max)
			}
			
			render imagesObject as JSON
		} catch(CytomineException e1){
			log.error(e1)
			// exceptions from the cytomine java client
			response.setStatus(e1.httpCode)
			JSONObject errorMsg = new Utils().resolveCytomineException(e1)
			render errorMsg as JSON
		} catch(GroovyCastException e2) {
			log.error(e2)
			// send back 400 if the project ID is other than long format
			response.setStatus(400)
			JSONObject errorMsg = new Utils().resolveException(e2, 400)
			render errorMsg as JSON
		} catch(Exception e3){
			log.error(e3)
			// on any other exception render 500
			response.setStatus(500)
			JSONObject errorMsg = new Utils().resolveException(e3, 500)
			render errorMsg as JSON
		}
	}

	/**
	 * Get an image in a project.
	 *
	 * @return the image as JSON object
	 */
	def getImage() {
		try {
			Cytomine cytomine = request['cytomine']
			long projectID = params.long("cmProjectID")
			long imageInstanceID = params.long("cmImageID")

			def image = imageService.getImage(cytomine, projectID, imageInstanceID, params.get("publicKey"))

			render image as JSON
		} catch(CytomineException e1){
			log.error(e1)
			// exceptions from the cytomine java client
			response.setStatus(e1.httpCode)
			JSONObject errorMsg = new Utils().resolveCytomineException(e1)
			render errorMsg as JSON
		} catch(GroovyCastException e2) {
			log.error(e2)
			// send back 400 if the project ID is other than long format
			response.setStatus(400)
			JSONObject errorMsg = new Utils().resolveException(e2, 400)
			render errorMsg as JSON
		} catch(Exception e3){
			log.error(e3)
			// on any other exception render 500
			response.setStatus(500)
			JSONObject errorMsg = new Utils().resolveException(e3, 500)
			render errorMsg as JSON
		}
	}

	def dev(){
		Cytomine cytomine = request['cytomine']
		long userID = params.long('userID')

		// TODO get the current user from the DB and read the associated session
		User u = User.findByCmID(userID)
		//		User u = User.get(1)
		Session sess = u.getSession()
		def sessJSON = new Utils().modelToJSON(sess);

		// assemble the response
		long uID = 93518990L
		long pID = 93519082L
		long iID = 100117637L
		//long aID // = 107885186L

		def usr = cytomine.getUser(uID).getAttr()
		sessJSON.user.cytomine = usr

		if (pID != null){
			def pj = cytomine.getProject(pID).getAttr()
			sessJSON.currentProject.cytomine = pj
		}
		if (iID != null){
			def img = cytomine.getImageInstance(iID).getAttr()
			sessJSON.currentImage.cytomine = img
		}
		//		if (aID != null){
		//			def ann = cytomine.getAnnotation(aID).getAttr()
		//			sessJSON.currentAnnotation.cytomine = ann
		//		}

		render sessJSON as JSON
	}
}
