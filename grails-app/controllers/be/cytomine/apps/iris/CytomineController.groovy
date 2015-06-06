
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

import be.cytomine.client.models.Project

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

import javax.imageio.ImageIO;

import grails.converters.JSON

import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.codehaus.groovy.runtime.typehandling.GroovyCastException;
import org.json.simple.JSONObject
import org.json.simple.JSONArray

import be.cytomine.client.Cytomine
import be.cytomine.client.CytomineException
import be.cytomine.client.collections.ImageInstanceCollection
import be.cytomine.client.models.ImageInstance
import be.cytomine.client.models.Ontology
import be.cytomine.client.models.User

/**
 * 
 * 
 * @author Loic Rollus, Philipp Kainz
 *
 */
@SuppressWarnings("deprecation")
class CytomineController {

	/**
	 * The injected Services for this controller.
	 */
	def projectService
	def imageService
	def grailsApplication
	def sessionService

	def beforeInterceptor = {
		log.debug("Executing action $actionName with params $params")
	}

	/**
	 * Gets an ontology by ID and optionally 'deflates' the hierarchy, if the 
	 * request <code>params</code> contain <code>flat=true</code>.
	 * @return the ontology as JSON object
	 */
	def getOntology(){
		try {
			Cytomine cytomine = request['cytomine']
			long oID = params.long('cmOntologyID')

			Ontology ontology = cytomine.getOntology(oID)

			if (params["flat"].equals("true")){
				// flattens the ontology but preserves the parent element in the hierarchy
				List<JSONObject> flatOntology = new Utils().flattenOntology(ontology)
				render flatOntology as JSON
			} else {
				render ontology.getAttr() as JSON
			}
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

	/**
	 * Gets a user which is identified by public key.
	 * @return the user as JSON object
	 */
	def getUserByPublicKey(){
		try {
			Cytomine cytomine = request['cytomine']
			String publicKey = params['pubKey']
			
			User user = cytomine.getUser(publicKey)

			render user.getAt("attr") as JSON
		} catch(CytomineException e1){
			log.error(e1)
			// exceptions from the cytomine java client
			response.setStatus(404)
			JSONObject errorMsg = new Utils().resolveCytomineException(e1)
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
	 * Gets an IRISUser instance.
	 * @return the IRISUser as JSON object
	 */
	def getCurrentIRISUser(){
		try {
			Cytomine cytomine = request['cytomine']
			IRISUser irisUser = request['user']
			render irisUser as JSON
		} catch(CytomineException e1){
			log.error(e1)
			// exceptions from the cytomine java client
			response.setStatus(404)
			JSONObject errorMsg = new Utils().resolveCytomineException(e1)
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
	 * Gets the image server URLs for a given image.
	 * @return the URLs for a given abstractImage for the OpenLayers instance
	 */
	def getImageServerURLs(){
		try {
			Cytomine cytomine = request['cytomine']
			long abstrImgID = params.long('abstractImageID')
			long imgInstID = params.long('imageInstanceID')

			// perform a synchronous get request to the Cytomine host server
			def urls = imageService.getImageServerURLs(cytomine, abstrImgID, imgInstID)
			// render URLs to client
			render urls
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
	
	/**
	 * Retrieves the macro image for the image list (preview).
	 * @return
	 */
	def getMacroImage() {
		try {
			Cytomine cytomine = request['cytomine']
			long abstractImageID = params.long('abstractImageID')
			int maxWidth = params.int("maxWidth")?params.int("maxWidth"):512
			
			String cmHost = grailsApplication.config.grails.cytomine.host
			String macroURL = cmHost + "/api/abstractimage/" + 
					abstractImageID + "/associated/macro.png?maxWidth=" + maxWidth
					
		    long start = System.currentTimeMillis()

			log.debug(macroURL)

			be.cytomine.client.HttpClient client = new be.cytomine.client.HttpClient(cytomine.publicKey, cytomine.privateKey, cmHost)
			
			BufferedImage macroImage = client.readBufferedImageFromURL(macroURL)
			
			log.debug("Fetched macro image from server in " + (System.currentTimeMillis() - start)/1000 + " seconds")
			start = System.currentTimeMillis()
			
			// get the image as byte[]
			ByteArrayOutputStream baos = new ByteArrayOutputStream()
			
			ImageIO.write(macroImage, "png", baos)
			baos.flush()
			byte[] imageInByte = baos.toByteArray()
			baos.close()
			
			log.debug("Converted macro image to byte[] in " + (System.currentTimeMillis() - start)/1000 + " seconds") 
			
			response.setStatus(200)
			response.setContentType("image/png")

			int contentLength = imageInByte.length
			response.setContentLength(contentLength)

			response.setHeader("Access-Control-Allow-Methods", "GET")
			response.setHeader("Access-Control-Allow-Origin", "*")

			// render image back to client
			def outputStream = null
			try {
				outputStream = response.outputStream
				outputStream.leftShift(imageInByte)
		
				log.debug("Rendered macro image response in " + (System.currentTimeMillis() - start)/1000 + " seconds")
			} catch (IOException e){
				log.warn('Client canceled AJAX request. ' + e)
			} finally {
				if (outputStream != null){
					try {
						outputStream.close()
					} catch (IOException e) {
						log.error('Exception on close', e)
					}
				}
			}
			
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

	/**
	 * Gets the tile for a zoomify image
	 * @return
	 */
	def getTile(){
		try {
			String zoomify_string = params['zoomify']
			String mimeType = params['mimeType']

			if (zoomify_string == null){
				throw new IllegalArgumentException("Zoomify URL is not specified in the request URL.")
			}

			//  "/data/beta.cytomine.be/93518990/93518990/1389785459805/HE_32911_12_converted.tif/TileGroup0/z-x-y.jpg"
			String[] zoomify_params = zoomify_string.split("/")

			if (zoomify_params.length < 9){
				throw new IllegalArgumentException("Zoomify URL is malformed in the request URL.")
			}
			
			long start = System.currentTimeMillis()
			
			int tileGroup = Integer.valueOf(zoomify_params[7].substring("TileGroup".length()))
			String extension = zoomify_params[8].substring(zoomify_params[8].indexOf(".")+1)
			String[] positions = zoomify_params[8].substring(0, zoomify_params[8].indexOf(".")).split("-")

			int tileZ = Integer.valueOf(positions[0])
			int tileX = Integer.valueOf(positions[1])
			int tileY = Integer.valueOf(positions[2])

			String path = "&tileGroup={tileGroup}&z={z}&x={x}&y={y}&channels=0&layer=0&timeframe=0"
			path = path.replace("{tileGroup}", tileGroup+"")
			path = path.replace("{z}", tileZ+"")
			path = path.replace("{x}", tileX+"")
			path = path.replace("{y}", tileY+"")

			String cmHost = grailsApplication.config.grails.cytomine.image.host;

			// choose a random server (1-10, and "")
			int serverID = new Random().nextInt(10)
			cmHost = cmHost.replace("{serverID}", String.valueOf(serverID==0?"":serverID))
			String dataString = zoomify_string.replace(zoomify_params[7]+"/"+zoomify_params[8], "")

			String imageURL = cmHost + "/image/tile?zoomify=" + dataString + path + "&mimeType=" + mimeType

			log.debug(imageURL)

			// get the image as byte[]
			HttpClient client = new DefaultHttpClient()
			HttpGet req = new HttpGet(imageURL)
			HttpResponse resp = client.execute(req)
			HttpEntity entity = resp.getEntity()
			InputStream is = entity.getContent()

			log.trace("Fetched tile image from server in " + (System.currentTimeMillis() - start)/1000 + " seconds")
			
			int statusCode = resp.getStatusLine().getStatusCode()
			response.setStatus(statusCode)
			response.setContentType(entity.getContentType().getValue())

			int contentLength = entity.getContentLength().toInteger()
			response.setContentLength(contentLength)

			response.setHeader("Access-Control-Allow-Methods", "GET")
			response.setHeader("Access-Control-Allow-Origin", "*")

			// render tile image back to client
			def outputStream = null
			try {
				outputStream = response.outputStream
				outputStream.leftShift(is)
				
				log.debug("Rendered tile image response in " + (System.currentTimeMillis() - start)/1000 + " seconds")
			} catch (IOException e){
				log.warn('Client canceled AJAX request. ' + e)
			} finally {
				if (outputStream != null){
					try {
						outputStream.close()
					} catch (IOException e) {
						log.error('Exception on close', e)
					}
				}
			}

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
	
	/**
	 * Retrieves the cropped image for an annoation.
	 * @return
	 */
	def getCropImage() {
		try {
			Cytomine cytomine = request['cytomine']
			long annID = params.long('annotationID')
			int maxSize = params.int("maxSize")?params.int("maxSize"):256
			
			String cmHost = grailsApplication.config.grails.cytomine.host
			String cropURL = cmHost + "/api/userannotation/" +
					annID + "/crop.png?maxSize=" + maxSize
					
			long start = System.currentTimeMillis()

			log.debug(cropURL)

			be.cytomine.client.HttpClient client = new be.cytomine.client.HttpClient(cytomine.publicKey, cytomine.privateKey, cmHost)
			
			BufferedImage macroImage = client.readBufferedImageFromURL(cropURL)
			
			log.debug("Fetched crop image from server in " + (System.currentTimeMillis() - start)/1000 + " seconds")
			start = System.currentTimeMillis()
			
			// get the image as byte[]
			ByteArrayOutputStream baos = new ByteArrayOutputStream()
			
			ImageIO.write(macroImage, "jpg", baos)
			baos.flush()
			byte[] imageInByte = baos.toByteArray()
			baos.close()
			
			log.debug("Converted crop image to byte[] in " + (System.currentTimeMillis() - start)/1000 + " seconds")
			
			response.setStatus(200)
			response.setContentType("image/jpeg")

			int contentLength = imageInByte.length
			response.setContentLength(contentLength)

			response.setHeader("Access-Control-Allow-Methods", "GET")
			response.setHeader("Access-Control-Allow-Origin", "*")

			// render image back to client
			def outputStream = null
			try {
				outputStream = response.outputStream
				outputStream.leftShift(imageInByte)
		
				log.debug("Rendered crop image response in " + (System.currentTimeMillis() - start)/1000 + " seconds")
			} catch (IOException e){
				log.warn('Client canceled AJAX request. ' + e)
			} finally {
				if (outputStream != null){
					try {
						outputStream.close()
					} catch (IOException e) {
						log.error('Exception on close', e)
					}
				}
			}
			
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
