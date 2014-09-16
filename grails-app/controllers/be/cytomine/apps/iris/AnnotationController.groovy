package be.cytomine.apps.iris

import grails.converters.JSON

import org.json.simple.JSONArray
import org.json.simple.JSONObject

import be.cytomine.client.Cytomine
import be.cytomine.client.collections.AnnotationCollection;
import be.cytomine.client.collections.TermCollection;
import be.cytomine.client.models.Annotation;
import be.cytomine.client.models.AnnotationTerm;
import be.cytomine.client.models.Ontology
import be.cytomine.clientx.CytomineX;


/**
 * 
 * @author Philipp Kainz
 *
 */
class AnnotationController {

	def grailsApplication
	def sessionService

	/**
	 * Get all annotations for a specific project and image.
	 * @return the annotations as JSON array
	 */
	def getAnnotations(){
		Cytomine cytomine = request['cytomine']
		String publicKey = params['publicKey']

		long sessionID = params.long('sessionID')
		long projectID = params.long('cmProjectID')
		long imageID = params.long('cmImageID')
		int max = (params['max']==null?0:params.int('max'))
		// TODO implement offset for pagination
		int offset = (params['offset']==null?0:params.int('offset'))

		// get the session and the user
		Session sess = Session.get(sessionID)
		User user = sess.getUser()

		// find the project
		Project p = sess.getProjects().find { it.cmID == projectID }
		long ontologyID = p.getCmOntology();

		// fetch the terms from the ontology
		TermCollection terms = cytomine.getTermsByOntology(ontologyID)

		// get a maximum of (max) instances
		cytomine.setMax(max)

		Map<String,String> filters = new HashMap<String,String>()
		filters.put("project", String.valueOf(projectID))
		filters.put("image", String.valueOf(imageID))

		// get all annotations according to the filter
		AnnotationCollection annotations = cytomine.getAnnotations(filters)
		def irisAnnList = new JSONArray()

		DomainMapper dm = new DomainMapper(grailsApplication)

		for(int i=0;i<annotations.size();i++) {
			Annotation annotation = annotations.get(i)

			// map the annotation to the IRIS model
			be.cytomine.apps.iris.Annotation irisAnn = dm.mapAnnotation(annotation, null)

			// grab all terms from all users for the current annotation
			List userByTermList = annotation.getList("userByTerm");

			for (assignment in userByTermList){
				//println currentUser.get("id") + ", " + assignment.get("user")
				if (user.cmID in assignment.get("user")){
					log.debug(user.cmUserName + " assigned " + assignment.get("term"))

					// store the properties in the irisAnnotation
					irisAnn.setCmUserID(user.cmID)
					irisAnn.setCmTermID(assignment.get("term"))
					irisAnn.setCmTermName(terms.list.find{it.id == assignment.get("term")}.get("name"))
					irisAnn.setCmOntology(ontologyID)

					// look for the next annotation
					break
				}
			}

			// ######################################################################################
			// IMPORTANT: do NOT inject the cytomine annotation, because they contain information on
			// mappings done by other users
			// ######################################################################################
			// add the annotation to the result and use the AnnotationMarshaller in order to
			// serialize the domain objects
			irisAnnList.add(irisAnn)
		}

		render irisAnnList as JSON
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


		// TODO retrieve one specific annotation for a project
		def annotation = null;

		// resolve the assigned label by the calling user
		// TODO speed up the query by getting the user from the local database
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
	def assignUniqueTerm(){
		Cytomine cytomine = request['cytomine']
		CytomineX cX = new CytomineX(cytomine.host, cytomine.publicKey, cytomine.privateKey, cytomine.basePath)

		long sessionID = params.long('sessionID')
		long cmProjectID = params.long('cmProjectID')
		long cmImageID = params.long('cmImageID')
		long cmAnnID = params.long('cmAnnID')
		long cmTermID = params.long('cmTermID')

		def payload = (request.JSON)
		// something like {annotation: 12345, term: 1239458}
		long pldTermID = Long.valueOf(payload.get('term'))
		long pldAnnID = Long.valueOf(payload.get('annotation'))
		
		if (pldAnnID != cmAnnID || pldTermID != cmTermID){
			throw new IllegalArgumentException("The identifiers in URL and payload do not match!")
		}

		// TODO store the currentAnnotation for this user
		//get the user from the session
//		Session sess = Session.get(sessionID)
//		User u = sess.getUser()
//		Project p = sess.getProjects().find { it.cmID == cmProjectID }
//		Image i = p.getImages().find { it.cmID == cmImageID }

		// set the term to the annotation
		AnnotationTerm annTerm = cX.setAnnotationTerm(cmAnnID, pldTermID)

		render annTerm.getAttr() as JSON;
	}

	def touchAnnotation(){
		// TODO insert the annotation into the database and send back the object as "current" annotation
		Cytomine cytomine = request['cytomine']
		long sessionID = params.long('sessionID')
		long cmProjectID = params.long('cmProjectID')
		long cmImageID = params.long('cmImageID')
		long cmAnnID = params.long('cmAnnID')

		def irisAnn = sessionService.touchAnnotation(cytomine, sessionID, cmProjectID, cmImageID, cmAnnID);

		render irisAnn as JSON
	}

	def removeLabels(){
		// TODO get the user by public/private key in the params

		// TODO if the user exists, retrieve the annotation by ID in the params

		// TODO clear the labels of the user

	}
}
