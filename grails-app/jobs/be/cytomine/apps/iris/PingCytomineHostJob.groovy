package be.cytomine.apps.iris

import java.util.logging.Logger;

import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.params.BasicHttpParams
import org.apache.http.params.HttpConnectionParams
import org.apache.http.params.HttpParams
import org.codehaus.groovy.grails.io.support.GrailsResourceUtils;


/**
 * Maintenance job, which regularly checks the availability of the Cytomine host.
 * 
 * @author Philipp Kainz
 * @since 1.5
 */
@SuppressWarnings("deprecation")
class PingCytomineHostJob {
	static triggers = { 
		simple repeatInterval: 30000l // execute job every 30 seconds
	}

	def grailsApplication
	def mailService
	def activityService
	
	int notifyThreshold = 100

	def execute() {
		// check, if the job is enabled
		String className = getClass().simpleName
		if (grailsApplication.config."$className".disabled) {
		  return
		}
		
		ServerPing sp = ServerPing.getInstance()

		String cmHost = grailsApplication.config.grails.cytomine.host
		String irisHost = grailsApplication.config.grails.cytomine.apps.iris.host
		
		try {
			String targetURL = cmHost + "/server/ping.json"

			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
			HttpConnectionParams.setSoTimeout(httpParams, 2000);
			HttpClient client = new DefaultHttpClient(httpParams);
			HttpGet request = new HttpGet(targetURL)
			HttpResponse response = client.execute(request)

			HttpEntity entity = response.getEntity()
			int code = response.getStatusLine().getStatusCode()

			if (!(code == 200 || code == 201 || code == 304)) {
				log.error("Cytomine host is not reachable!")
				activityService.log("Cytomine host is not reachable!")

				// store a tmp property
				int failCount = sp.incrementFailCount()

				// ignore the first n fails
				if (failCount <= notifyThreshold) {
					log.warn("Ignoring server ping fail count #" + failCount + ", threshold is set to " + notifyThreshold)
					return
				} else {
					notifyAdmin("Cytomine Host NOT Available", "Cytomine IRIS host at [" + irisHost +
						"] is unable to ping Cytomine host at [" + cmHost + "]!\n"
						+ "Server ping failed " + sp.getFailCount() + " times, so please check the connection!")
					sp.resetFailCount()
				}
			} else {
				log.debug("Cytomine host ping successful.")
			}
		} catch(UnknownHostException uhe){
			log.error("Cannot find host. ", uhe)
			activityService.log("Cytomine host is not reachable!")
			
			int failCount = sp.incrementFailCount()
			
			// ignore the first n fails
			if (failCount >= notifyThreshold) {
				notifyAdmin("Cytomine Host NOT Available", "Cytomine IRIS host at [" + irisHost +
					"] is unable to ping Cytomine host at [" + cmHost + "]!\n"
					+ "Server ping failed, so please check the connection!\n\n" + uhe.getStackTrace().toString())
				sp.resetFailCount()
			}
		} catch(Exception ex){
			log.error("Exception occurred during host ping: ")
			activityService.log("Cytomine host is not reachable!")
			
			int failCount = sp.incrementFailCount()

			// ignore the first n fails
			if (failCount >= notifyThreshold) {
				notifyAdmin("Error during Cytomine host ping", "Cytomine IRIS host at [" + irisHost +
					"] is unable to ping Cytomine host at [" + cmHost + "]!\n"
					+ "Server ping failed " + sp.getFailCount() + " times, so please check the connection!\n\n" 
					+ ex.getStackTrace().toString())
				sp.resetFailCount()
			}
		}
	}
	
	def notifyAdmin(String subj, String bdy) throws Exception{
		String recipient = grailsApplication.config.grails.cytomine.apps.iris.server.admin.email
		
		log.info("Sending email to admin [" + recipient + "]...")
	
		// notify the admin
		mailService.sendMail {
			async true
			from "cytomine-iris@pkainz.com"
			to recipient
			subject String.valueOf(subj)
			body String.valueOf(bdy)
		}
	}
}
