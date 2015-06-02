
/* Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
					log.warn("Someone tried to access the API without valid API keys!")
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


