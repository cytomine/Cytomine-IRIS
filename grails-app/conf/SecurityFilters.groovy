import grails.converters.JSON
import org.apache.http.auth.InvalidCredentialsException
import org.springframework.ui.Model;

import org.json.simple.JSONObject;

import be.cytomine.apps.iris.IRISUser;
import be.cytomine.apps.iris.Utils
import be.cytomine.client.Cytomine

import javax.servlet.http.HttpServletResponse

class SecurityFilters {
	def springSecurityService

	def filters = {
		// FILTER FOR THE REST API
		// all requests going to /api/** will require the public and private key of
		// the user to be present
		api(uri:'/api/**') {
			before = {
				String publicKey = params.get("publicKey")
				String privateKey = params.get("privateKey")

				if(publicKey != null && privateKey != null) {
					// store a new cytomine instance (from the client-JAR)
					// to the request object (will be available in the controllers etc.)
					request['cytomine'] = new Cytomine(grailsApplication.config.grails.cytomine.host as String
							, publicKey, privateKey, "./")
					request['cytomine'].setMax(0) //no max
					
					// find the user in the DB
					// TODO get authenticated user from spring security service
					IRISUser currentUser = IRISUser.findByCmPublicKey(publicKey)
					request['user'] = currentUser
				}
				else {
					log.warn("Some tried to access the API without valid API keys!")
					response.setStatus(HttpServletResponse.SC_FORBIDDEN)
					JSONObject exObj = new Utils().resolveException(new InvalidCredentialsException("You do not have " +
						"the permissions to access this API resource. Invalid API keys."), HttpServletResponse.SC_FORBIDDEN)
					render exObj as JSON
					return false
				}
			}
			after = { Model m ->

			}
			afterView = { Exception e ->

			}
		}
	}

}


