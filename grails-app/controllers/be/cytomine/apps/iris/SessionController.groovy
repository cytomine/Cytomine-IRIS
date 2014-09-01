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
		JSON.use('deep')
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
		Project projectForUpdate = sess.getProjects().find {
			it.cmID == cm_projectID
		}
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
		
		// prepare the client session object as response
		ClientSession cs = new ClientSession()
		
		// get the current user from the DB and read the associated session
		User u = User.findByCmID(userID)
		Session s = u.getSession()
		
		// assemble the response
		long uID = 93518990L
		long pID = 93519082L
		long iID = 100117637L
		long aID = 107885186L
		
		JSONObject usr = cytomine.getUser(uID).getAttr()
		JSONObject pj = cytomine.getProject(pID).getAttr()
		JSONObject img = cytomine.getImageInstance(iID).getAttr()
		JSONObject ann = cytomine.getAnnotation(aID).getAttr()
		
		cs.user = usr
		cs.currentProject = pj
		cs.currentImage = img
//		cs.currentAnnotation = ann
		
		render cs as JSON
	}
}
