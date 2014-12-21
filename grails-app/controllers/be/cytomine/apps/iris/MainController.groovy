package be.cytomine.apps.iris

import be.cytomine.client.Cytomine;
import grails.converters.JSON
import org.json.simple.JSONObject

/**
 * This is the main controller of the IRIS application.
 * 
 * @author Philipp Kainz, 2014-08-13
 *
 */
class MainController {

    def grailsApplication
	
	def beforeInterceptor = {
		log.debug("Executing action $actionName with params $params")
	}

	/**
	 * Renders a welcome text for the start page.
	 * @return
	 */
    def welcome() {
        render "Welcome to Cytomine ${grailsApplication.metadata.'app.name'}"
    }
	
	/**
	 * Gets the application name
	 * @return the name of the application
	 */
	def appName() {
		render "${grailsApplication.metadata.'app.name'}"
	}
	
	/**
	 * Gets the version of the IRIS module.
	 * @return the version as string
	 */
	def appVersion(){
		render "${grailsApplication.metadata.'app.version'}"
	}
	
	/**
	 * Gets the Cytomine host URL from the configuration.
	 * @return the URL of the host in Config.groovy
	 */
	def hostAddress(){
		try {
			Cytomine cytomine = request["cytomine"]
			if (!cytomine.testConnexion()){
				throw new UnknownHostException("The cytomine host is currently not available!")
			}
			render grailsApplication.config.grails.cytomine.host
		} catch(UnknownHostException e1){
			log.error(e1)
			// throw host unavailable exception
			response.setStatus(503)
			JSONObject errorMsg = new Utils().resolveException(e1, 503)
			render errorMsg as JSON
		} catch(Exception e3){
			log.error(e3)
			// on any other exception render 500
			response.setStatus(500)
			JSONObject errorMsg = new Utils().resolveException(e3, 500)
			render errorMsg as JSON
		}
	}
	
	/**
	 * Gets the Cytomine web URL from the configuration.
	 * @return the URL of the web page in Config.groovy
	 */
	def webAddress(){
		render grailsApplication.config.grails.cytomine.web
	}
}
