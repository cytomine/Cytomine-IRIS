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
	def annotationService

	/**
	 * Get all annotations for a specific project and image.
	 * 
	 * @return the annotations as JSON array
	 */
	def getAnnotations(){
		Cytomine cytomine = request['cytomine']
		String publicKey = params['publicKey']

		long sessionID = params.long('sessionID')
		long projectID = params.long('cmProjectID')

		// filter for a specific project/image/term
		Map<String,String> filters = new HashMap<String,String>()
		filters.put("project", String.valueOf(projectID))
		filters.put("showGIS", "true") // show the centroid information on the location
		filters.put("showMeta", "true") // show the meta informations
		filters.put("showTerm", "true") // show the term informations

		String imageIDs = params['image']
		if (imageIDs != null){
			filters.put("image", String.valueOf(imageIDs))
		}

		// filter for terms
		String termIDs = params['term']
		if (termIDs != null){
			filters.put("term", String.valueOf(termIDs))
		}
		// additionally check for termID 0, this will be annotations without terms
		if (params['noTerm'] != null && params['noTerm'].equals("true")){
			filters.put("noTerm", "true")
		}
		// check for multiple terms
		if (params['multipleTerm'] != null && params['multipleTerm'].equals("true")){
			filters.put("multipleTerm", "true")
		}

		// TODO implement pagination
		int max = (params['max']==null?0:params.int('max'))

		// get the session and the user
		Session sess = Session.get(sessionID)
		User user = sess.getUser()

		// find the project
		Project p = sess.getProjects().find { it.cmID == projectID }
		long ontologyID = p.getCmOntology();

		// fetch the terms from the ontology
		TermCollection terms = cytomine.getTermsByOntology(ontologyID)

		// get all annotations according to the filter
		AnnotationCollection annotations = cytomine.getAnnotations(filters)
		def irisAnnList = new JSONArray()

		// create a new domain mapper
		DomainMapper dm = new DomainMapper(grailsApplication)

		for(int i=0;i<annotations.size();i++) {
			Annotation annotation = annotations.get(i)

			// map the annotation to the IRIS model
			be.cytomine.apps.iris.Annotation irisAnn = dm.mapAnnotation(annotation, null)

			irisAnn = annotationService.resolveTerms(ontologyID, terms, user, annotation, irisAnn)

			// ######################################################################################
			// IMPORTANT: do NOT inject the cytomine annotation, because they contain information on
			// mappings done by other users
			// ######################################################################################
			// add the annotation to the result and use the AnnotationMarshaller in order to
			// serialize the domain objects
			irisAnnList.add(irisAnn)
		}

		render (irisAnnList as JSON)
	}

	/**
	 * 
	 * @return
	 */
	def setUniqueTerm(){
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
		//		TermCollection terms = cX.getTermsByOntology(p.getCmOntology())
		//Image i = p.getImages().find { it.cmID == cmImageID }

		// set the term to the annotation
		AnnotationTerm annTerm = cX.setAnnotationTerm(cmAnnID, pldTermID)

		render (annTerm.getAttr() as JSON)
	}

	/**
	 * Touches an annotation and adds it to the image.
	 * 
	 * @return
	 */
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

	def deleteTerm(){
		Cytomine cytomine = request['cytomine']

		long sessionID = params.long('sessionID')
		long cmProjectID = params.long('cmProjectID')
		long cmImageID = params.long('cmImageID')
		long cmAnnID = params.long('cmAnnID')
		long cmTermID = params.long('cmTermID')

		cytomine.deleteAnnotationTerm(cmAnnID, cmTermID)

		render new JSONObject().put("message", "The term has been deleted.");
	}


	def deleteAllTerms(){
		Cytomine cytomine = request['cytomine']

		long sessionID = params.long('sessionID')
		long cmProjectID = params.long('cmProjectID')
		long cmImageID = params.long('cmImageID')
		long cmAnnID = params.long('cmAnnID')

		// TODO implement deleting all terms by assigning a unique term and deleting it immediately


		render new JSONObject().put("message", "All terms have been deleted.");
	}

	def getAnnotation3Tuple(){
		Cytomine cytomine = request['cytomine']

		long sessionID = params.long('sessionID')
		long cmProjectID = params.long('cmProjectID')
		long cmImageID = params.long('cmImageID')
		boolean filterCompleted = params['filtercompleted']==null?false:Boolean.parseBoolean(params['filtercompleted']).booleanValue()

		// get the session and the user
		Session sess = Session.get(sessionID)
		User user = sess.getUser()

		// find the project
		Project p = sess.getCurrentProject()
		long ontologyID = p.getCmOntology()

		// get the currentImage
		Image i = p.getCurrentImage()

		// fetch the terms from the ontology
		TermCollection terms = cytomine.getTermsByOntology(ontologyID)

		Long cmAnnID = null
		try {
			cmAnnID = Long.valueOf(params['currentAnnotation'])
		} catch (NumberFormatException nfe){
			log.warn("Parameter value of 'currentAnnotation' is 'null', will return first one or two annotations!")
		}

		Map<String,String> filters = new HashMap<String,String>()
		filters.put("project", String.valueOf(cmProjectID))
		filters.put("image", String.valueOf(cmImageID))
		filters.put("showWKT", "true")
		filters.put("showGIS", "true")
		filters.put("showMeta", "true")
		filters.put("showTerm", "true")

		AnnotationCollection annotations = cytomine.getAnnotations(filters)
		// previous call equals cytomine.doGet("/api/annotation.json?project=93519082&image=94255021")

		if (annotations.isEmpty()){
			response.setStatus(404)
			render new JSONObject().put("message", "No annotations found for that query.")
			return
		}

		DomainMapper dm = new DomainMapper(grailsApplication)
		Utils utils = new Utils()

		// search in the annotations for "114156768" (has predecessor and successor!)
		// "114156782" has no predecessor
		// "94310122" has no successor
		int idx = 0
		int tupleSize = 0
		if (cmAnnID != null){
			idx = annotations.list.indexOf( annotations.list.find { ann -> ann.id == cmAnnID } )
		}
		// if the list does not contain the required annotation id
		// start from the beginning and get the first element
		if (idx == -1){
			idx = 0;
		}
		def curr = annotations.get(idx)
		def currIrisAnn = dm.mapAnnotation(curr, null)
		currIrisAnn = annotationService.resolveTerms(ontologyID, terms, user, curr, currIrisAnn)
		//		TODO save the annotation
		//i.setCurrentAnnotation(currIrisAnn)
		tupleSize++

		// remove all completed elements
		def incompleteAnnotations = annotations.getList()
		println incompleteAnnotations
		
		// TODO if the 'filtercompleted==true', search for the next unlabeled annotation
		// TODO filter all completed annotations
		
		// find the predecessor
		def pred = null
		def predIrisAnn = null
		try {
			pred = utils.getPredecessor(annotations, idx);
			predIrisAnn = dm.mapAnnotation(pred, null)
			predIrisAnn = annotationService.resolveTerms(ontologyID, terms, user, pred, predIrisAnn)
			//		TODO save the annotation
			//			i.setPreviousAnnotation(predIrisAnn)
			tupleSize++
		} catch (IndexOutOfBoundsException e){
			log.debug("Index " + idx + " has no predecessor.")
		}

		// find the successor
		def succ = null
		def succIrisAnn = null
		try {
			succ = utils.getSuccessor(annotations, idx);
			succIrisAnn = dm.mapAnnotation(succ, null)
			succIrisAnn = annotationService.resolveTerms(ontologyID, terms, user, succ, succIrisAnn)
			//		TODO save the annotation
			//			i.setNextAnnotation(succIrisAnn)
			tupleSize++
		} catch (IndexOutOfBoundsException e){
			log.debug("Index " + idx + " has no successor.")
		}

		// log the information message
		log.debug("Request: annotation " + curr.id + " at index [" + idx + "] has " +
				(pred==null?"no":"annotation "+pred.id+" as") + " predecessor, and " +
				(succ==null?"no":"annotation "+succ.id+" as") + " successor.")

		// add annotations to a new JSON object
		JSONObject result = new JSONObject()

		result.put("previousAnnotation", predIrisAnn) // may be null
		result.put("hasPrevious", predIrisAnn==null?false:true)
		result.put("currentAnnotation", currIrisAnn) // must not be null at this point
		result.put("nextAnnotation", succIrisAnn) // may be null
		result.put("hasNext", succIrisAnn==null?false:true)
		result.put("size", tupleSize) // number of annotations in the tuple

		result.put("currentIndex", idx)
		result.put("labeledAnnotations", 2) // TODO compute userProgress in this image
		result.put("numberOfAnnotations", annotations.size())

		render (result as JSON)
	}
}
