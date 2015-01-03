package be.cytomine.apps.iris

import org.codehaus.groovy.grails.web.json.JSONElement;
import org.springframework.aop.ThrowsAdvice;

import grails.converters.JSON;
import grails.transaction.Transactional
import be.cytomine.client.Cytomine
import be.cytomine.client.CytomineException;
import be.cytomine.client.models.ImageInstance;

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

	def projectService
	def imageService
	def grailsApplication

	/**
	 * Get all sessions.
	 * 
	 * @return
	 */
	@Transactional(readOnly = true)
	List<Session> getAll(){
		// fetch all sessions from the database where the user is owner
		List<Session> allSessions = Session.getAll()
		return allSessions
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
	 * 
	 * @throws Exception
	 */
	JSONElement getSession(Cytomine cytomine, String publicKey) throws Exception{
		// get the calling user from cytomine
		be.cytomine.client.models.User cmUser = cytomine.getUser(publicKey)
		
		// try to find it in the data base
		User user = User.findByCmID(cmUser.getId())

		// generate a new user, if the user does not yet exist, otherwise update the
		// user information from cytomine
		user = new DomainMapper(grailsApplication).mapUser(cmUser, user)

		// try to fetch the session for this user
		Session userSession = user.getSession()
		if (userSession == null){
			userSession = new Session()
			user.setSession(userSession)
		}

		// save the user and the session
		user.save(flush:true, failOnError:true)

		// ############################################
		// inject non-IRIS objects into the session
		JSONElement sessJSON = new Utils().modelToJSON(userSession)

		// inject the current user
		sessJSON.user.cytomine = cmUser.getAttr()

		// inject current Cytomine project
		Project irisProject = userSession.getCurrentProject()
		if (irisProject != null){
			def pj = cytomine.getProject(irisProject.getCmID()).getAttr()
			sessJSON.currentProject.cytomine = pj
			
			// also try to inject the current image
			Image irisImage = irisProject.getCurrentImage()
			if (irisImage != null){
				def img = cytomine.getImageInstance(irisImage.getCmID()).getAttr()
				sessJSON.currentProject.currentImage.cytomine = img
			}
		}
		
		// return the session as json object
		return sessJSON
	}

	/**
	 * Updates a project associated with an IRIS Session. 
	 * Also fetches the current project settings from the Cytomine host 
	 * and returns the updated project as JSON object.
	 * 
	 * @param cytomine a Cytomine instance
	 * @param sessionID the IRIS session ID where the project belongs to
	 * @param cmProjectID the Cytomine project ID for updating
	 * @return the updated IRIS project instance as JSON object
	 * 
	 * @throws CytomineException if the project is is not available for the 
	 * querying user
	 * @throws Exception
	 */
	JSONElement touchProject(Cytomine cytomine, long sessionID, long cmProjectID) throws CytomineException, Exception{
		
		// find the project in the session
		Session sess = Session.get(sessionID)

		// find the nested project by projectID
		Project projectForUpdate = sess.getProjects().find { it.cmID == cmProjectID }

		// fetch the Cytomine project instance
		be.cytomine.client.models.Project cmProject = cytomine.getProject(cmProjectID)

		// map to the IRIS domain model
		projectForUpdate = new DomainMapper(grailsApplication).mapProject(cmProject, projectForUpdate)
		projectForUpdate.updateLastActivity()

		// save the project in order to have the ID returned in the response
		projectForUpdate.save(flush:true,failOnError:true)

		// trigger reordering of the projects in the session
		sess.setCurrentProject(projectForUpdate)
		sess.addToProjects(projectForUpdate)
		sess.updateLastActivity()

		// inject the project here directly in order to avoid fetching it again
		// from Cytomine
		JSONElement projectJSON = injectCytomineProject(cytomine, projectForUpdate, cmProject)

		return projectJSON
	}

	/**
	 * Fetches the current project settings from the Cytomine host
	 * and returns the project. Does not save the project on the IRIS server.
	 *
	 * @param cytomine a Cytomine instance
	 * @param sessionID the IRIS session ID where the project belongs to
	 * @param cmProjectID the Cytomine project ID for updating
	 * @return the updated IRIS project instance
	 *
	 * @throws CytomineException if the project is is not available for the
	 * querying user
	 * @throws Exception
	 */
	JSONElement getProject(Cytomine cytomine, long sessionID, long cmProjectID) throws CytomineException, Exception{
		// find the project in the session
		Session sess = Session.get(sessionID)

		// find the nested project by projectID
		Project projectForUpdate = sess.getProjects().find { it.cmID == cmProjectID }

		JSONElement projectJSON = injectCytomineProject(cytomine, projectForUpdate, null)

		return projectJSON
	}

	/**
	 * Injects a Cytomine project into the IRIS project (as JSON).
	 * 
	 * @param cytomine a Cytomine instance
	 * @param irisProject the IRIS Project
	 * @param cmProject the cytomineProject, or <code>null</code>, if the project should be fetched from Cytomine
	 * @return a JSON object with the injected Cytomine project
	 * 
	 * @throws CytomineException if the project is is not available for the
	 * querying user
	 * @throws Exception
	 */
	JSONElement injectCytomineProject(Cytomine cytomine, Project irisProject, def cmProject) throws CytomineException, Exception{

		JSONElement projectJSON = new Utils().modelToJSON(irisProject)

		// fetch the Cytomine project instance
		if (cmProject == null){
			cmProject = cytomine.getProject(irisProject.cmID)
			if (cmProject == null){
				throw new CytomineException(404, "The requested project is not available.");
			}
			projectJSON.cytomine = cmProject.getAttr()
		} else {
			if (cmProject['attr'] != null){
				projectJSON.cytomine = cmProject.getAttr()
			} else {
				projectJSON.cytomine = cmProject
			}
		}
		return projectJSON
	}

	/**
	 * Injects a Cytomine Annotation into the IRIS annotation.
	 *
	 * @param cytomine a Cytomine instance
	 * @param irisAnnotation the IRIS Annotation
	 * @param cmAnnotation the cytomine Annotation, or <code>null</code>, if the Annotation should be fetched from Cytomine
	 * @return a JSON object with the injected Cytomine annotation
	 *
	 * @throws CytomineException if the annotation is is not available for the
	 * querying user
	 * @throws Exception
	 */
	JSONElement injectCytomineAnnotation(Cytomine cytomine, Annotation irisAnnotation, def cmAnnotation) throws CytomineException, Exception{

		JSONElement annJSON = new Utils().modelToJSON(irisAnnotation)

		// fetch the Cytomine project instance
		if (cmAnnotation == null){
			cmAnnotation = cytomine.getAnnotation(irisAnnotation.cmID)
			annJSON.cytomine = cmAnnotation.getAttr()
		} else {
			if (cmAnnotation['attr'] != null){
				annJSON.cytomine = cmAnnotation.getAttr()
			} else {
				annJSON.cytomine = cmAnnotation
			}
		}
		return annJSON
	}

	/**
	 * Injects a Cytomine Image into the IRIS Image.
	 *
	 * @param cytomine a Cytomine instance
	 * @param irisImage the IRIS Image
	 * @param cmImage the Cytomine image, or <code>null</code>, if the image should be fetched from Cytomine
	 * @param blindMode flag determining the 'Blind Mode' of the project
	 * @return a JSON object with the injected Cytomine project
	 *
	 * @throws CytomineException if the image is is not available for the
	 * querying user
	 * @throws Exception
	 */
	JSONElement injectCytomineImageInstance(Cytomine cytomine, Image irisImage, def cmImage, boolean blindMode) throws CytomineException, Exception{
		JSONElement imageJSON = new Utils().modelToJSON(irisImage)

		// fetch the Cytomine image instance
		if (cmImage == null){
			cmImage = cytomine.getImageInstance(irisImage.cmID)
			imageJSON.cytomine = cmImage.getAttr()
		} else {
			if (cmImage['attr'] != null){
				imageJSON.cytomine = cmImage.getAttr()
			} else {
				imageJSON.cytomine = cmImage
			}
		}
		// inject/overwrite the file name in each image, if required
		if (blindMode){
			imageJSON.cytomine.originalFilename = "[BLIND]" + cmImage.getId()
			imageJSON.cytomine.path = null
			imageJSON.cytomine.fullPath = null // this contains the image path for the tiles
			imageJSON.cytomine.extension = null
			imageJSON.cytomine.filename = null
			imageJSON.cytomine.mime = null
			imageJSON.cytomine.originalMimeType = null
		}

		return imageJSON
	}

	/**
	 * Update an existing IRIS Project in a session with a JSON object delivered in the payload. 
	 * 
	 * @param cytomine a Cytomine instance
	 * @param sessionID the session ID
	 * @param irisProjectID the Cytomine project ID
	 * @param payload the IRIS project as JSON (incl. injected cytomine project at "payload.cytomine")
	 * @return
	 * @throws CytomineException
	 * @throws Exception
	 */
	JSONElement updateByIRISProject(Cytomine cytomine, long sessionID, long irisProjectID, def payload) throws CytomineException, Exception{
		// check if the user may access this project
		def cmProject = cytomine.getProject(payload['cytomine'].id);

		// get the cytomine project from the payload
		if (cmProject == null){
			log.error("This user is not allowed to access the project.")
			throw new CytomineException(404, "The project is not available for this user.")
		}

		// get the session
		Session sess = Session.get(sessionID)
		if (sess == null){
			log.error("Cannot find session " + sessionID)
			throw new CytomineException(404, "Cannot find session for this user.")
		}

		// find the specific project
		def sessProjects = sess.getProjects();
		log.debug("Session "+ sess.id +  " has " + sessProjects.size() + " projects: " + sessProjects)

		// try to find the project
		Project project = sessProjects.find { it.id == irisProjectID }

		// if no project is available in this session, return null and cause error
		if (project == null){
			log.error("Cannot find project " + irisProjectID + " for session " + sessionID)
			throw new CytomineException(404, "Cannot find requested project in the session.")	
		}

		// map all properties from json to the project
		project = project.updateByJSON(payload)

		// add the project to the session and cause reordering
		sess.setCurrentProject(project)
		sess.addToProjects(project)
		sess.updateLastActivity()

		JSONElement pjJSON = injectCytomineProject(cytomine, project, cmProject)
		return pjJSON
	}

	/**
	 * Updates an image associated with an IRIS Session and Project.
	 *
	 * @param cytomine a Cytomine instance
	 * @param sessionID the IRIS session ID where the project belongs to
	 * @param cmProjectID the Cytomine project ID for updating
	 * @param cmImageID the Cytomine imageID for updating
	 * @return the updated IRIS image instance
	 *
	 * @throws CytomineException if the image is is not available for the
	 * querying user
	 * @throws Exception
	 */
	JSONElement touchImage(Cytomine cytomine, long sessionID, long cmProjectID, long cmImageID) throws CytomineException, Exception {
		// find the project in the session
		Session sess = Session.get(sessionID)

		// find the nested project by projectID
		Project irisProject = sess.getProjects().find { it.cmID == cmProjectID }

		// find the nested image by imageID
		Image imageForUpdate = irisProject.getImages().find { it.cmID == cmImageID }

		// fetch the image from Cytomine
		ImageInstance cmImage = cytomine.getImageInstance(cmImageID)

		// Map the client model to the IRIS model
		imageForUpdate = new DomainMapper(grailsApplication).mapImage(cmImage, imageForUpdate, irisProject.getCmBlindMode())
		imageForUpdate.updateLastActivity()
		// set the "goToURL"
		imageForUpdate.setGoToURL(grailsApplication.config.grails.cytomine.host + "/#tabs-image-" + cmProjectID + "-" + cmImageID + "-")
		imageForUpdate.setOlTileServerURL(imageService.getImageServerURL(cytomine, cmImage.get("baseImage"), cmImage.getId()));

		// save the image in order to immediately reflect the ID
		imageForUpdate.save(failOnError:true, flush:true)

		// update the time stamps and re-order the most recent images
		irisProject.setCurrentImage(imageForUpdate)
		irisProject.addToImages(imageForUpdate)
		irisProject.updateLastActivity()

		sess.setCurrentProject(irisProject)
		sess.addToProjects(irisProject)
		sess.updateLastActivity()

		// inject the project here directly in order to avoid fetching it again
		// from Cytomine
		JSONElement imageJSON = injectCytomineImageInstance(cytomine, imageForUpdate, cmImage, irisProject.getCmBlindMode())

		return imageJSON
	}

	/**
	 * Updates an annotation associated with an IRIS Session, Project and Image.
	 *
	 * @param cytomine a Cytomine instance
	 * @param sessionID the IRIS session ID where the project belongs to
	 * @param cmProjectID the Cytomine project ID for updating
	 * @param cmImageID the Cytomine imageID for updating
	 * @param cmAnnID the Cytomine annotationID for updating
	 * @return the updated IRIS image instance
	 *
	 * @throws CytomineException if the annotation is is not available for the
	 * querying user
	 * @throws Exception
	 */
	JSONElement touchAnnotation(Cytomine cytomine, long sessionID, long cmProjectID, long cmImageID, long cmAnnID) throws CytomineException, Exception{
		// find the project in the session
		Session sess = Session.get(sessionID)

		// find the nested project by projectID
		Project irisProject = sess.getProjects().find { it.cmID == cmProjectID }

		// find the nested image by imageID
		Image irisImage = irisProject.getImages().find { it.cmID == cmImageID }

		// get the annotation
		be.cytomine.apps.iris.Annotation irisAnn = irisImage.getAnnotations().find { it.cmID == cmAnnID }

		// fetch the annotation from Cytomine
		def cmAnn = cytomine.getAnnotation(cmAnnID)

		// Map the client model to the IRIS model
		irisAnn = new DomainMapper(grailsApplication).mapAnnotation(cmAnn, irisAnn)

		// save the image in order to immediately reflect the ID
		// TODO GORM LOCKING ERROR when sending multiple requests in rather short time!!
		irisAnn.save(failOnError:true, flush:true)

		// TODO add a Set<Annotation> annotations to the Image domain model!
		irisImage.addToAnnotations(irisAnn);
		irisImage.updateLastActivity();

		// update the time stamps and re-order the most recent images
		irisProject.addToImages(irisImage)
		irisProject.updateLastActivity()

		sess.addToProjects(irisProject)
		sess.updateLastActivity()

		// inject the project here directly in order to avoid fetching it again
		// from Cytomine
		JSONElement annJSON = new Utils().modelToJSON(irisAnn)

		return annJSON
	}

	/**
	 * Removes a session from the database.
	 * 
	 * @param sessID the IRIS session ID
	 * 
	 * @return <code>null</code>, if the session does not exist
	 * @throws Exception if any error occurs during deletion
	 */
	void delete(long sessID) throws Exception{
		Session sess = Session.get(sessID)
		sess.delete(flush:true, failOnError:true)
	}
}
