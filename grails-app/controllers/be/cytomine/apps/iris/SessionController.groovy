package be.cytomine.apps.iris

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONElement
import be.cytomine.client.Cytomine
import be.cytomine.client.CytomineException

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

	/**
	 *  Injected SessionService instance for this controller. 
	 */
	def sessionService

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
		def sessJSON = sessionService.get(request["cytomine"], params["publicKey"]) as JSON
		render sessJSON
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
		} catch(CytomineException e){
			// TODO 404
			log.error("",e)
		} catch(Exception ex){
			// TODO 400
			log.error("",ex)
		}
	}

	/**
	 * Deletes a session.
	 */
	def deleteSession(){
		long sessID = params.long('sessionID')
		sessionService.delete(sessID)
	}

	/**
	 * Update a session.
	 * 
	 * @return the updated session
	 */
	def updateSession(){
		// parse the payload of the PUT request
		def sess = request.JSON
		
		// fetch the session from the DB
		Session s = Session.get(sess.id)
		
		// TODO update all fields from the request body
		s.setPrefs(sess.prefs)
		
		// update the record
		s.save(failOnError:true,flush:true)
		
		render s as JSON
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
			long projectID = payload["id"]
			
			// declare the project
			def irisProject = null
			
			if (pldClazz.equals(Project.class.name)){
				// update using the IRIS project
				irisProject = sessionService.updateByIRISProject(cytomine, sessionID, projectID, payload)
			} else if(pldClazz.equals(IRISConstants.CM_PROJECT)){
				// update using the Cytomine project
				irisProject = sessionService.updateByCytomineProject(cytomine, sessionID, projectID, payload)
			}
						
			render (irisProject as JSON)
		}catch(CytomineException e){
			// TODO redirect to an error page or send back 404 and message
			log.error("",e)
		}catch(Exception ex){
			// TODO redirect to an error page or send back 400
			log.error("",ex)
		}
	}

	/**
	 * Updates a project in a session.
	 * 
	 * @return the updated project as JSON object
	 */
	def touchProject(){
		try {
			Cytomine cytomine = request['cytomine']
			long sessionID = params.long('sessionID')
			long cmProjectID = params.long('cmProjectID')
			
			println sessionID + " - " + cmProjectID
			
			def irisProject = sessionService.touchProject(cytomine, sessionID, cmProjectID)

			render irisProject as JSON
		}catch(CytomineException e){
			log.error("",e)	
		// TODO redirect to an error page or send back 404 and message
		}catch(Exception ex){
			log.error("",ex)
		// TODO redirect to an error page or send back 400
		}
	}
	
	def delProj(){
		//		Project toDelete = Project.get(2)
		//		println toDelete
		//		toDelete.delete()

		//		Preference pr = Preference.get(307)
		//		pr.delete()
		render "ok"
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
