package be.cytomine.apps.iris

import org.json.simple.JSONObject;

import be.cytomine.client.Cytomine;
import grails.transaction.Transactional

@Transactional
class AdminService {

	def grailsApplication

	/**
	 * Gets the application name
	 * @return the name of the application
	 */
	String getAppName() {
		return grailsApplication.metadata['app.name']
	}

	/**
	 * Gets the version of the IRIS module.
	 * @return the version as string
	 */
	String getAppVersion(){
		return grailsApplication.metadata['app.version']
	}

	/**
	 * Gets the context of the IRIS module.
	 * @return the context as string
	 */
	String getAppContext(){
		return grailsApplication.metadata['app.context']
	}

	/**
	 * Gets the Grails version of the IRIS module.
	 * @return the version as string
	 */
	String getGrailsVersion(){
		return grailsApplication.metadata['app.grails.version']
	}

	/**
	 * Gets the servlet version of the IRIS module.
	 * @return the version as string
	 */
	String getServletVersion(){
		return grailsApplication.metadata['app.servlet.version']
	}

	/**
	 * Gets the Cytomine host URL from the configuration.
	 * @return the URL of the host in Config.groovy and the connection status
	 */
	JSONObject getCytomineHostAddress(Cytomine cytomine) {
		JSONObject hostInfo = new JSONObject()
		hostInfo.put("host", grailsApplication.config.grails.cytomine.host)
		if (!cytomine.testConnexion()){
			hostInfo.put("available", false)
		} else {
			hostInfo.put("available", true)
		}
		return hostInfo
	}

	/**
	 * Gets the IRIS host URL from the configuration.
	 * @return the URL of the host in Config.groovy
	 */
	String getIrisHostAddress(Cytomine cytomine) {
		return grailsApplication.config.grails.serverURL
	}

	/**
	 * Gets the Cytomine web URL from the configuration.
	 * @return the URL of the web page in Config.groovy
	 */
	String getCytomineWebAddress(){
		return grailsApplication.config.grails.cytomine.web
	}


	JSONObject getDatabaseStatistics(){
		// TODO number of users

		// last activity of any user

		// last activity of each user

	}

	def getActivityLog() {
		// TODO get all activities (in form of a log)
	}
}
