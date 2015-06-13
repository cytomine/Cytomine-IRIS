
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
package be.cytomine.apps.iris

import be.cytomine.client.Cytomine
import be.cytomine.client.CytomineException;
import grails.converters.JSON
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.json.simple.JSONObject

import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * This is the main controller of the IRIS application.
 * 
 * @author Philipp Kainz
 * @since 0.1
 */
class MainController {

    def grailsApplication
	def adminService
	
	def beforeInterceptor = {
		log.debug("Executing action $actionName with params $params")
	}

	def index(){}

	/**
	 * Renders a welcome text for the start page.
	 * @return
	 */
    def welcome() {
        render "Welcome to Cytomine IRIS"
    }
	
	/**
	 * Gets the application name
	 * @return the name of the application
	 */
	def appName() {
		render adminService.getAppName()
	}
	
	/**
	 * Gets the version of the IRIS module.
	 * @return the version as string
	 */
	def appVersion(){
		render adminService.getAppVersion()
	}
	
	/**
	 * Gets the Cytomine host URL from the configuration.
	 * @return the URL of the host in Config.groovy
	 */
	def hostAddress(){
		try {
			Cytomine cytomine = request["cytomine"]

			if (params['ping'] != null && Boolean.valueOf(params['ping']) == true){
				if (!cytomine.testConnexion()){
					throw new UnknownHostException("The cytomine host is currently not available!")
				}
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
		render adminService.getCytomineWebAddress()
	}

	/**
	 * Get the application information of this IRIS instance.
	 * @return
	 */
	def applicationInfo() {
		try {
			JSONObject appInfo = new JSONObject()
			appInfo.put("appName", adminService.getAppName())
			appInfo.put("appVersion", adminService.getAppVersion())

			appInfo.put("grailsVersion", adminService.getGrailsVersion())
			appInfo.put("servletVersion", adminService.getServletVersion())
			appInfo.put("appContext", adminService.getAppContext())
			appInfo.put("irisHost", adminService.getIrisHostAddress())

			appInfo.put("cytomineHost", adminService.getCytomineHostAddress(request['cytomine']))
			appInfo.put("cytomineWeb", adminService.getCytomineWebAddress())

			render appInfo as JSON
		} catch(CytomineException e1){
			log.error(e1)
			// exceptions from the cytomine java client
			response.setStatus(e1.httpCode)
			JSONObject errorMsg = new Utils().resolveCytomineException(e1)
			render errorMsg as JSON
		} catch(GroovyCastException e2) {
			log.error(e2)
			// send back 400 if the project ID is other than long format
			response.setStatus(400)
			JSONObject errorMsg = new Utils().resolveException(e2, 400)
			render errorMsg as JSON
		} catch(Exception e3){
			log.error(e3)
			// on any other exception render 500
			response.setStatus(500)
			JSONObject errorMsg = new Utils().resolveException(e3, 500)
			render errorMsg as JSON
		}
	}
}
