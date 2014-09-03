package be.cytomine.apps.iris

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest;
import org.codehaus.groovy.grails.web.util.WebUtils;

import grails.transaction.Transactional
import be.cytomine.client.Cytomine

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
	
	@Transactional(readOnly = true)
	List<Session> getAll(){
		// fetch all sessions from the database where the user is owner
		List<Session> allSessions = Session.getAll()
		return allSessions
	}
	
	def get(Cytomine cytomine, long sessID){
		// TODO
	}
	
	/**
	 * Gets the Session for a user identified by public key. 
	 * If the user has no Session yet, a new one will be created.
	 * The Session's state gets resolved and all <code>current</code> 
	 * instances are fetched freshly from Cytomine and injected into the JSON object.
	 * 
	 * @param cytomine a Cytomine instance
	 * @param publicKey the user's public key
	 * @return a Session as JSON object ready to render it to the client
	 */
	def get(Cytomine cytomine, String publicKey){
		be.cytomine.client.models.User cmUser = cytomine.getUser(publicKey)
		User user = User.findByCmID(cmUser.getId())
		
		// generate a new user, if the user does not yet exist, otherwise update the
		// user information from cytomine
		user = new DomainMapper().mapUser(cmUser, user)

		// try to fetch the session for this user
		Session userSession = user.getSession()
		if (userSession == null){
			userSession = new Session()
			user.setSession(userSession)
		}
		
		// save the user and the session
		user.save(flush:true,failOnError:true)
		
		
		// ############################################
		// inject non-IRIS objects into the session
		def sessJSON = new Utils().modelToJSON(userSession);
		
		// inject the user
		sessJSON.user.cytomine = cmUser.getAttr()
		
		// inject current project
		Project cP = userSession.getCurrentProject()

		if (cP != null){
			def pj = cytomine.getProject(cP.getCmID()).getAttr()
			sessJSON.currentProject.cytomine = pj
		}
		Image cI = userSession.getCurrentImage()
		if (cI != null){
			def img = cytomine.getImageInstance(cI.getCmID()).getAttr()
			sessJSON.currentImage.cytomine = img
		}
		// TODO inject current annotation
		//		if (aID != null){
		//			def ann = cytomine.getAnnotation(aID).getAttr()
		//			sessJSON.currentAnnotation.cytomine = ann
		//		}
		
		// return the session as json object
		return sessJSON
	}

	def update(Cytomine cytomine, long sessID){
		// set the new timestamp and save the session
		
	}
	
	def delete(Cytomine cytomine, long sessID){
		
	}
		
}
