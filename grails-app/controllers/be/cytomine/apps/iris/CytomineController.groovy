package be.cytomine.apps.iris

import grails.converters.JSON

import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.json.simple.JSONObject

import be.cytomine.client.Cytomine
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
	
	def beforeInterceptor = {
		log.debug("Executing action $actionName with params $params")
	}
	
	/**
	 * Gets an ontology by ID and optionally 'deflates' the hierarchy, if the 
	 * request <code>params</code> contain <code>flat=true</code>.
	 * @return the ontology as JSON object
	 */
	def getOntology(){
		Cytomine cytomine = request['cytomine']
		long oID = params.long('ontologyID')

		Ontology ontology = cytomine.getOntology(oID)

		if (params["flat"].equals("true")){
			// flattens the ontology but preserves the parent element in the hierarchy
			List<JSONObject> flatOntology = new Utils().flattenOntology(ontology)
			render flatOntology as JSON
		} else {
			render ontology.getAttr() as JSON
		}
	}

	/**
	 * Gets a user which is identified by public key.
	 * @return the user as JSON object
	 */
	def getUserByPublicKey(){
		Cytomine cytomine = request['cytomine']
		String publicKey = params['pubKey']

		User user = cytomine.getUser(publicKey)

		render user.getAt("attr") as JSON
	}
	
	/**
	 * Gets the image server URLs for a given image.
	 * @return the URLs for a given abstractImage for the OpenLayers instance
	 */
	def getImageServerURLs(){
		Cytomine cytomine = request['cytomine']
		long abstrImgID = params.long('abstractImageID')
		long imgInstID = params.long('imageInstanceID')

		// perform a synchronous get request to the Cytomine host server
		def urls = imageService.getImageServerURLs(cytomine, abstrImgID, imgInstID)
		// render URLs to client
		render urls;
	}
	
	/**
	 * Gets the tile for a zoomify image
	 * @return
	 */
	def getTile(){
		Cytomine cytomine = request['cytomine']
		String zoomify_string = params['zoomify']
		
		if (zoomify_string == null){
			throw new IllegalArgumentException("Zoomify URL is not specified in the request URL.")
		}
		
		//  "/data/beta.cytomine.be/93518990/93518990/1389785459805/HE_32911_12_converted.tif/TileGroup0/z-x-y.jpg"
		String[] zoomify_params = zoomify_string.split("/")
		
		if (zoomify_params.length < 9){
			throw new IllegalArgumentException("Zoomify URL is not specified in the request URL.")
		}
		
		int tileGroup = Integer.valueOf(zoomify_params[7].substring("TileGroup".length()))
		String extension = zoomify_params[8].substring(zoomify_params[8].indexOf(".")+1)
		String[] positions = zoomify_params[8].substring(0, zoomify_params[8].indexOf(".")).split("-")
		
		int tileZ = Integer.valueOf(positions[0])
		int tileX = Integer.valueOf(positions[1])
		int tileY = Integer.valueOf(positions[2])
		
		println tileGroup + " --> " + positions

		String path = "&tileGroup={tileGroup}&z={z}&x={x}&y={y}&channels=0&layer=0&timeframe=0&mimeType=image/tiff"
		path = path.replace("{tileGroup}", tileGroup+"")
		path = path.replace("{z}", tileZ+"")
		path = path.replace("{x}", tileX+"")
		path = path.replace("{y}", tileY+"")
		
		String cmHost = grailsApplication.config.grails.cytomine.image.host;
		
		// choose a random server (1-10, and "")
		int serverID = new Random().nextInt(10)
		cmHost = cmHost.replace("{serverID}", String.valueOf(serverID==0?"":serverID))
		String dataString = zoomify_string.replace(zoomify_params[7]+"/"+zoomify_params[8], "")
		
		String imageURL = cmHost + "/image/tile?zoomify=" + dataString + path
		
		// get the image as byte[]
		HttpClient client = new DefaultHttpClient()
		HttpGet req = new HttpGet(imageURL)
		HttpResponse resp = client.execute(req)
		HttpEntity entity = resp.getEntity()
		InputStream is = entity.getContent()
		
		int statusCode = resp.getStatusLine().getStatusCode()
		response.setStatus(statusCode)
		response.setContentType(entity.getContentType().getValue())
		
		int contentLength = entity.getContentLength().toInteger()
		response.setContentLength(contentLength)
		
		// render image back to client
		response.outputStream.leftShift(is)
	}
	
	def dev(){
		
	}
}
