import grails.converters.JSON;

import java.net.Authenticator.RequestorType;

import org.json.simple.JSONObject;
import org.junit.After;

import be.cytomine.apps.iris.Utils
import be.cytomine.client.Cytomine
import be.cytomine.client.CytomineException

class SecurityFilters {
	def springSecurityService

	def filters = {

		api(uri:'/api/**') {
			before = {
				String publicKey = params.get("publicKey")
				String privateKey = params.get("privateKey")
				
				if(publicKey && privateKey) {
					// store a new cytomine instance (from the client-JAR)
					// to the request object (will be available in the controllers etc.)
					request['cytomine'] = new Cytomine(grailsApplication.config.grails.cytomine.host, publicKey, privateKey, "./")
					request['cytomine'].setMax(0) //no max
				}
				else {
					response.setStatus(403)
					JSONObject exObj = new Utils().resolveCytomineException(new CytomineException(403, "You do not have " +
						" the permissions to access this resource. Invalid credentials."))
					render exObj as JSON
					return false
				}
			}
			after = {

			}
			afterView = {

			}
		}
	}

}


