package be.cytomine.apps.iris

import grails.converters.JSON

class CytomineController {

	def grailsApplication

	// create a JSON object and send it back to the client
	def projects() {
		// get the cytomine instance from the request (injected by the security filter!)
		render request['cytomine'].getProjects().list as JSON
	}

	/**
	 * Build the URLs for the images by adding the proper suffix to the Cytomine host URL.
	 * The project ID is retrieved via the injected <code>params</code> property.
	 * @return the list of images for a given project id
	 */
	def images() {
//		request['cytomine'].setMax(5); //max 5 images
		def list = request['cytomine'].getImageInstances(params.long('idProject')).list
		list.each {
			//for each image, add a goToURL property containing the full URL to open the image in the core Cytomine instance
			it.goToURL = grailsApplication.config.grails.cytomine.host + "/#tabs-image-" + params.long('idProject') + "-" + it.id + "-"
			}
		render list as JSON
	}

	/**
	 * Gets the Cytomine host URL from the configuration.
	 * @return the URL of the host in Config.groovy
	 */
	def getHostAddress(){
		render grailsApplication.config.grails.cytomine.host
	}
	
	/**
	 * Gets the Cytomine web URL from the configuration.
	 * @return the URL of the web page in Config.groovy
	 */
	def getWebAddress(){
		render grailsApplication.config.grails.cytomine.web
	}
}
