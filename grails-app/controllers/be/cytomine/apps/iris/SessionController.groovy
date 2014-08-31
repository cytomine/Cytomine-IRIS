package be.cytomine.apps.iris

import grails.converters.JSON
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

		// try to fetch a user session from the database
		Session userSession = new Session(
				//			lastActivity: new Date().getTime(),
				//			key: "fooooo::" + new Random().nextInt()
				);

		// pull the user details from the client
		be.cytomine.client.models.User cm_user = cytomine.getUser(cm_userID)
		userSession.user = new User(
				firstName: cm_user.get("firstname"),
				lastName: cm_user.get("lastname"),
				userName: cm_user.get("username"),
				publicKey: cm_user.get("publicKey"),
				privateKey: cm_user.get("privateKey"));

		userSession.save(failOnError:true)
		render userSession as JSON
	}

	def deleteSession(){
		long userID = params.long('userID')
		User user = User.findById(userID)
		// TODO
	}
	
	def updateSession(){
		// TODO
	}
}
