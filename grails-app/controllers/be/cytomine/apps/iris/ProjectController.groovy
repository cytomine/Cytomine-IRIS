package be.cytomine.apps.iris

import grails.converters.JSON
import be.cytomine.client.Cytomine
import be.cytomine.client.CytomineException

/**
 * This class provides methods for handling custom requests 
 * regarding Project instances.
 * 
 * @author Philipp Kainz
 *
 */
class ProjectController {

	def beforeInterceptor = {
		log.debug("Executing action $actionName with params $params")
	}
	
	/**
	 * The injected ProjectService instance for this controller.
	 */
	def projectService

	/**
	 * Checks the availability of a given project for the calling user.
	 * 
	 * @return HTTP 200: <code>true</code>, if it exists and is available, 
	 * HTTP 404: <code>false</code>, if the project exists, but is not 
	 * available for that user,
	 * HTTP 400: if the received project ID is malformed
	 */
	def checkAvailability(){
		try {
			long projectID = params.long('projectID')
			def available = projectService.isAvailable(request['cytomine'], projectID)
			response.setContentType("text/plain")
			render available
		} catch(CytomineException e){
			response.setStatus(404)
			response.setContentType("text/plain")
			render e.toString()
		} catch(Exception ex) {
			// send back 400 if the project ID is other than long format
			response.setStatus(400)
			response.setContentType("text/plain")
			render ex.getMessage()
		}
	}
	
	/**
	 * Get all projects from Cytomine host, which are associated with the 
	 * user executing this query.
	 *
	 * @return a ProjectCollection as JSON object
	 */
	def getProjects() {
		// get the Cytomine instance from the request (injected by the security filter!)
		Cytomine cytomine = request['cytomine']
		boolean resolveOntology = params['resolveOntology'].equals("true")
		def projectList = projectService.getProjects(cytomine, resolveOntology)
		render projectList as JSON
	}
	
	/**
	 * Gets the project description for a given ID.
	 * 
	 * @return the description of a project as JSON object, if there is one, or HTTP 404,
	 * if there is no description available
	 */
	def getProjectDescription(){
		try {
			def description = projectService.getProjectDescription(request['cytomine'], params.long('projectID'))
			render description as JSON
		} catch (CytomineException e){
			response.setStatus(404)
			response.setContentType("text/plain")
			render e.toString()
		}
	}	
}
