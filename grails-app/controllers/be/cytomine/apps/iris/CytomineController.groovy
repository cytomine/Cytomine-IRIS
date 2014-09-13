package be.cytomine.apps.iris

import grails.converters.JSON

import org.json.simple.JSONArray
import org.json.simple.JSONObject

import be.cytomine.client.Cytomine
import be.cytomine.client.CytomineException
import be.cytomine.client.models.Ontology
import be.cytomine.client.models.User

/**
 * 
 * 
 * @author Loic Rollus, Philipp Kainz
 *
 */
class CytomineController {
	
	/**
	 * The injected Services for this controller.
	 */
	def projectService
	def imageService

	
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
			List<JSONObject> flatOntology = new Utils().flattenOntology(ontology)
			render flatOntology as JSON
		} else {
			render ontology as JSON
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
		long imgInstID = params.long('imageinstance')

		// perform a synchronous get request to the Cytomine host server
		def urls = cytomine.doGet("/api/abstractimage/" + abstrImgID + "/imageservers.json?imageinstance=" + imgInstID)
		urls.replace("\"", "\\\"")
		response.setContentType("application/json")
		response.setCharacterEncoding("UTF-8")
		render urls;
	}
}
