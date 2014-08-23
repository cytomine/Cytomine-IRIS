package be.cytomine.apps.iris

import org.json.simple.JSONObject;

import be.cytomine.client.Cytomine;
import be.cytomine.client.CytomineException;
import be.cytomine.client.models.Description;
import grails.converters.JSON

/**
 * 
 * 
 * @author Loic Rollus, Philipp Kainz
 *
 */
class CytomineController {

	/**
	 * Get all projects from Cytomine host, which are associated with the user executing this query.
	 * @return a JSON collection
	 */
	def getProjects() {
		// get the Cytomine instance from the request (injected by the security filter!)
		Cytomine cytomine = request['cytomine']
		def projectList = cytomine.getProjects().list
		
		// optionally resolve the ontology and inject it in the 
		if (params['resolveOntology'].equals("true")){
			// get the ontology object for each project
			projectList.each {
				println it.get("ontology")
				long oID = it.get("ontology")
				def ontology = cytomine.getOntology(oID);
				it.resolvedOntology = ontology;
			}
		}
		render (projectList as JSON)
	}

	/**
	 * Build the URLs for the images by adding the proper suffix to the Cytomine host URL.
	 * The project ID is retrieved via the injected <code>params</code> property.
	 * @return the list of images for a given project ID
	 */
	def getImages() {
		Cytomine cytomine = request['cytomine']
		
		long userID = cytomine.getUser(params.get("publicKey")).getId();
		long projectID = params.long("projectID");
		
		//cytomine.setMax(5); //max 5 images
		def imageList = cytomine.getImageInstances(params.long('projectID')).list
		imageList.each {
			//for each image, add a goToURL property containing the full URL to open the image in the core Cytomine instance
			it.goToURL = grailsApplication.config.grails.cytomine.host + "/#tabs-image-" + projectID + "-" + it.id + "-"

			// retrieve the user's progress on each image and return it in the object
			JSONObject annInfo = new Utils().getUserProgress(cytomine, projectID, it.id, userID);
			// resolving the values from the JSONObject to each image as property
			it.labeledAnnotations = annInfo.get("labeledAnnotations")
			it.userProgress = annInfo.get("userProgress")
		}
		render imageList as JSON
	}

	/**
	 * Gets the project description for a given ID.
	 * @return the description of a project as JSON object
	 */
	def getProjectDescription(){
		def d;
		try {
			//println "request arrived: " + params
			d = request['cytomine'].getDescription(params.long('projectID'), 'be.cytomine.project.Project')

			//println d as JSON
			render (d as JSON)
		} catch (CytomineException e) {
			// 404 {"message":"Domain not found with id : 98851569 and className=be.cytomine.project.Project"}
			// println e
			// if there is no description associated, return a JSON object
			//			if (e.httpCode == 404){
			//				d = new JSONObject()
			//				d.put("data", "This project does not have any description.")
			//				d.put("status", 404);
			//			}
			//			println d as JSON
			//			render d as JSON
			log.error(e);
			// do not send any response, since the Grails server automatically sends a 404 code
			// and triggers the callbackError method
		}
	}
	
	/**
	 * Gets an ontology by ID.
	 * @return the ontology as JSON object
	 */
	def getOntology(){
		Cytomine cytomine = request['cytomine']
		long oID = params.long('ontologyID')
		
		def ontology = cytomine.getOntology(oID)
		
		render (ontology as JSON)
	}
}
