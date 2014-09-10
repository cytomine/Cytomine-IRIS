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
	 * The injected ProjectService for this controller.
	 */
	def projectService

	/**
	 * Build the URLs for the images by adding the proper suffix to the Cytomine host URL.
	 * The project ID is retrieved via the injected <code>params</code> property.
	 * 
	 * @return the list of images for a given project ID as JSON object
	 */
	def getImages() {
		Cytomine cytomine = request['cytomine']

		long userID = cytomine.getUser(params.get("publicKey")).getId()
		long projectID = params.long("projectID");
		
		// important for blinding image names
		boolean blindMode = false
		
		be.cytomine.apps.iris.User u = be.cytomine.apps.iris.User.find { cmID == userID }
		
		// get the session
		Session sess = u.getSession();
		
		// try to search in local database
		Project irisProject = sess.getProjects().find { it.cmID == projectID }
		
		if (irisProject == null){
			def cmProject = cytomine.getProject(projectID)
			// update the project in the local database
			irisProject = new DomainMapper().mapProject(cmProject, irisProject)
			irisProject.save(failOnError:true)
			
			// get the blind mode
			blindMode = cmProject.get("blindMode")
		} else {
			blindMode = irisProject.cmBlindMode
		}
		
		// TODO implement paging using max and offset parameters from the request params
		//int offset = params.long("offset")
		//int max = params.long("max")
		//cytomine.setMax(5); //max 5 images
		
		def imageList = cytomine.getImageInstances(params.long('projectID')).list
		imageList.each {
			//for each image, add a goToURL property containing the full URL to open the image in the core Cytomine instance
			it.goToURL = grailsApplication.config.grails.cytomine.host + "/#tabs-image-" + projectID + "-" + it.id + "-"
			
			// inject the blinded file name in each image, if required
			if (blindMode){
				it.originalFilename = "[BLIND]" + it.id
			} 
			
			// retrieve the user's progress on each image and return it in the object
			JSONObject annInfo = new Utils().getUserProgress(cytomine, projectID, it.id, userID)
			// resolving the values from the JSONObject to each image as property
			it.labeledAnnotations = annInfo.get("labeledAnnotations")
			it.userProgress = annInfo.get("userProgress")
		}
		render imageList as JSON
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
