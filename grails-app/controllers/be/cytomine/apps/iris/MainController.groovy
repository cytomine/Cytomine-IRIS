package be.cytomine.apps.iris

import be.cytomine.client.Cytomine;

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
		render grailsApplication.config.grails.cytomine.host
	}
	
	/**
	 * Gets the Cytomine web URL from the configuration.
	 * @return the URL of the web page in Config.groovy
	 */
	def webAddress(){
		render grailsApplication.config.grails.cytomine.web
	}
}
