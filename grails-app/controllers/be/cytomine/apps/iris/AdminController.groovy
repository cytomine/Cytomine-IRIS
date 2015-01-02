package be.cytomine.apps.iris

import grails.converters.JSON

import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.json.simple.JSONObject

import be.cytomine.client.CytomineException

class AdminController {

	def beforeInterceptor = {
		log.debug("Executing action $actionName with params $params")
	}
	
	def grailsApplication
	def imageService
	def sessionService
	def adminService
	def activityService
	def mailService	
	
	/**
	 * Manually trigger the synchronization of the user progress.
	 * @return
	 */
	def synchronizeUserProgress() {
		def resp = new SynchronizeUserProgressJob(grailsApplication,sessionService,imageService,activityService,mailService).execute()
		if (resp == true) {
			render "Sync OK."
		} else if (resp == "deactivated"){
			render "Sync deactivated."
		} else {
			response.setStatus(500)
			render "Sync Failed."
		}
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
