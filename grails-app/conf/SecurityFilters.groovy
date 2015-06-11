
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
import be.cytomine.apps.iris.UserToken
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

				// if the user can provide a one time token, everything's fine
				String oneTimeToken = params.get("token")

				if (oneTimeToken != null){
					// check if the token is still available
					UserToken ut = UserToken.findByToken(oneTimeToken)
					if (ut==null || !ut.valid) {
						log.warn("Someone tried to access the API without a valid token!")
						response.setStatus(HttpServletResponse.SC_FORBIDDEN)
						JSONObject exObj = new Utils().resolveException(new InvalidCredentialsException("You do not have " +
								"the permissions to access this API resource. Invalid token."), HttpServletResponse.SC_FORBIDDEN)
						render exObj as JSON
						return false
					}

					// invalidate the token and proceed
					ut.invalidate()

					// if the token contains a valid IRISUser, inject it into the request object
					if (ut.user != null) {
						// find the user in the DB
						IRISUser currentUser = IRISUser.get(ut.user.id)
						request['user'] = currentUser

						request['cytomine'] = new Cytomine(grailsApplication.config.grails.cytomine.host as String
								,  currentUser.cmPublicKey, currentUser.cmPrivateKey, "./")
						request['cytomine'].setMax(0) //no max
					}
				} else {
					// otherwise check for the public and private key
					if (publicKey != null && privateKey != null) {
						// store a new cytomine instance (from the client-JAR)
						// to the request object (will be available in the controllers etc.)
						request['cytomine'] = new Cytomine(grailsApplication.config.grails.cytomine.host as String
								, publicKey, privateKey, "./")
						request['cytomine'].setMax(0) //no max

						// find the user in the DB
						// TODO get authenticated user from spring security service
						IRISUser currentUser = IRISUser.findByCmPublicKey(publicKey)
						request['user'] = currentUser
					} else {
						log.warn("Someone tried to access the API without valid API keys!")
						response.setStatus(HttpServletResponse.SC_FORBIDDEN)
						JSONObject exObj = new Utils().resolveException(new InvalidCredentialsException("You do not have " +
								"the permissions to access this API resource. Invalid API keys."), HttpServletResponse.SC_FORBIDDEN)
						render exObj as JSON
						return false
					}
				}
			}
			after = { Model m ->

			}
			afterView = { Exception e ->

			}
		}


		dev(uri:'/dev/**') {

		}
	}

}


