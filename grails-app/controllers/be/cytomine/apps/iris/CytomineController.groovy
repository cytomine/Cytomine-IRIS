package be.cytomine.apps.iris

import org.json.simple.JSONObject;

import be.cytomine.client.CytomineException;
import be.cytomine.client.models.Description;
import grails.converters.JSON

class CytomineController {



	/**
	 * Get all projects from Cytomine host, which are associated with the user executing this query.
	 * @return a JSON collection
	 */
	def getProjects() {
		// get the Cytomine instance from the request (injected by the security filter!)
		render request['cytomine'].getProjects().list as JSON
	}

	/**
	 * Build the URLs for the images by adding the proper suffix to the Cytomine host URL.
	 * The project ID is retrieved via the injected <code>params</code> property.
	 * @return the list of images for a given project ID
	 */
	def getImages() {
		//		request['cytomine'].setMax(5); //max 5 images
		def imageList = request['cytomine'].getImageInstances(params.long('idProject')).list
		imageList.each {
			//for each image, add a goToURL property containing the full URL to open the image in the core Cytomine instance
			it.goToURL = grailsApplication.config.grails.cytomine.host + "/#tabs-image-" + params.long('idProject') + "-" + it.id + "-"
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
			d = request['cytomine'].getDescription(params.long('idProject'), 'be.cytomine.project.Project')

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
}
