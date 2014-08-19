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

    def welcome() {
        render "Welcome to Cytomine ${grailsApplication.metadata.'app.name'}"
    }
	
	def appName() {
		render "${grailsApplication.metadata.'app.name'}"
	}
	
	def appVersion(){
		render "${grailsApplication.metadata.'app.version'}"
	}
}
