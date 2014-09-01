package be.cytomine.apps.iris

import grails.converters.JSON

import org.json.simple.JSONObject

import be.cytomine.client.Cytomine

/**
 * A SessionController handles the communication with the client and 
 * dispatches existing sessions for users or creates/updates them.
 * 
 * @author Philipp Kainz
 *
 */
class SessionController {

	/**
	 * Gets all sessions on this IRIS server instance.
	 * @return a JSON object
	 */
	def getAll(){
		// fetch all sessions from the database where the user is owner
		List<Session> allSessions = Session.getAll()

		//print allSessions as JSON
		render allSessions as JSON
	}

	/**
	 * Gets a session object for a user. If the querying user does not have a session, 
	 * a new one will be created. 
	 * @return the Session as deeply resolved JSON object
	 */
	def getSession(){
		long userID = params.long('userID')
		String pubKey = params['publicKey']

		// TODO how to handle changed publicKeys??
		User u;

		// try to fetch a user session from the database
		List<Session> userSession = Session.getAll()

		render userSession as JSON
	}

	/**
	 * Create a new session for a given user identified by publicKey
	 * @return
	 */
	def createSession(){
		Cytomine cytomine = request['cytomine']
		String publicKey = params['publicKey']
		long cm_userID = params.long('userID')

		// TODO

		render null
	}

	def deleteSession(){
		long userID = params.long('userID')
		User user = User.findById(userID)
		// TODO
	}

	def updateSession(){
		// TODO
	}

	def updateProject(){
		Cytomine cytomine = request['cytomine']
		String publicKey = params['publicKey']
		long cm_userID = params.long('userID')
		long cm_projectID = params.long('projectID')

		User usr = User.findByCmID(cm_userID)
		Session sess = usr.getSession()
		// find the project by cm_projectID
		Project projectForUpdate = sess.getProjects().find { it.cmID == cm_projectID }
		projectForUpdate.updateLastActivity()
		projectForUpdate.save(flush:true)
		render sess as JSON
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
		def sessJSON = Utils.modelToJSON(sess);

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
