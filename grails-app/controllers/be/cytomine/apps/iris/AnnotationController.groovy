package be.cytomine.apps.iris

import grails.converters.JSON

import org.json.simple.JSONObject

import be.cytomine.client.Cytomine
import be.cytomine.client.models.Annotation;
import be.cytomine.client.models.Ontology
import be.cytomine.client.models.User;


/**
 * 
 * @author Philipp Kainz
 *
 */
class AnnotationController {

	/**
	 * Get all annotations for a specific project and image.
	 * @return
	 */
	def getAnnotations(){
		Cytomine cytomine = request['cytomine']

		// TODO get a maximum of n instances
		cytomine.setMax(5)

		long projectID = params.long('projectID')
		long imageID = params.long('imageID')


	}

	/**
	 * Get a specific annotation by ID.
	 * @return
	 */
	def getAnnotation() {
		Cytomine cytomine = request['cytomine']
		String publicKey = params['publicKey']
		//long projectID = params.long('projectID')
		//long imageID = params.long('imageID')
		long annID = params.long('annID')

		
		// TODO retrieve the specific annotation for a project
		def annotation = null;
				
		// resolve the assigned label by the calling user
		User user = cytomine.getUser(publicKey);
		
		List userByTermList = annotation.getList("userByTerm");
		println userByTermList.getClass()
		return;
		for (int i=0; i < userByTermList.size(); i++){
			def assignment = userByTermList.get(i);
			List userList = assignment.get("user").toList()
			
			// if the user has assigned a label to this annotation
			if (user.get("id") in userList){
				// inject the term ID into the annotation as "userLabels"
				annotation.putAt("userLabels", [assignment.get("term")])
			}
		}

		render (annotation as JSON)
	}

	/**
	 * 
	 * @return
	 */
	def storeUniqueLabel(){
		// TODO get the user by public/private key in the params

		// TODO if the user exists, retrieve the annotation by ID in the params

		// TODO if there is already a label assigned, clear the label and set the new one

	}

	def removeLabels(){
		// TODO get the user by public/private key in the params

		// TODO if the user exists, retrieve the annotation by ID in the params

		// TODO clear the label and set the new one

	}
}
