package be.cytomine.apps.iris

import org.aspectj.weaver.patterns.HasThisTypePatternTriedToSneakInSomeGenericOrParameterizedTypePatternMatchingStuffAnywhereVisitor;
import org.codehaus.groovy.runtime.typehandling.GroovyCastException;
import org.json.simple.JSONObject;

import grails.converters.JSON;
import grails.transaction.Transactional
import be.cytomine.client.Cytomine;
import be.cytomine.client.CytomineException;
import be.cytomine.client.collections.AnnotationCollection;
import be.cytomine.client.collections.TermCollection
import be.cytomine.client.models.Annotation
import be.cytomine.client.models.AnnotationTerm;
import be.cytomine.clientx.CytomineX;

@Transactional
class AnnotationService {

	def grailsApplication
	def activityService

	/**
	 * Resolves the terms assigned by a single user. This method stops searching, 
	 * as soon as the user has a single assignment. 
	 * 
	 * @param ontologyID the ontologyID
	 * @param terms the term list
	 * @param user the querying user
	 * @param annotation the annotation to search in
	 * @param irisAnn the IRIS annotation (result)
	 * @return the IRIS annotation
	 */
	be.cytomine.apps.iris.Annotation resolveTerms(long ontologyID, TermCollection terms, User user, Annotation annotation, be.cytomine.apps.iris.Annotation irisAnn){
		// grab all terms from all users for the current annotation
		List userByTermList = annotation.getList("userByTerm");

		for (assignment in userByTermList){
			//println currentUser.get("id") + ", " + assignment.get("user")
			if (user.cmID in assignment.get("user")){
				String cmTermName = terms.list.find{it.id == assignment.get("term")}.get("name")
				log.debug(user.cmUserName + " assigned " + cmTermName)

				// store the properties in the irisAnnotation
				irisAnn.setCmUserID(user.cmID)
				irisAnn.setCmTermID(assignment.get("term"))
				irisAnn.setCmTermName(cmTermName)
				irisAnn.setCmOntology(ontologyID)
				irisAnn.setAlreadyLabeled(true)

				break
			}
		}

		return irisAnn
	}

	/**
	 * Resolves the terms a user assigned to an annotation and injects full term name and other
	 * properties into the annotation
	 * 
	 * @param ontologyID the ontology ID
	 * @param terms the list of terms
	 * @param user the calling user
	 * @param annotation the query annotation
	 * @param irisAnn the IRIS annotation
	 * @param searchForNoTerm a flag, whether the user is also searching for annotations, 
	 * 						where he/she did not yet assign a term to 
	 * @param queryTerms the list of terms to query for
	 * @param annotationMap the map of filtered annotations
	 * @return the annotation where the query terms match
	 */
	be.cytomine.apps.iris.Annotation resolveTermsByUser(long ontologyID,
			TermCollection terms, User user, Annotation annotation,
			be.cytomine.apps.iris.Annotation irisAnn, boolean searchForNoTerm,
			def queryTerms, JSONObject annotationMap){
		// grab all terms from all users for the current annotation
		List userByTermList = annotation.getList("userByTerm");

		log.debug(userByTermList)
		log.debug("Searching for 'no Term': " + searchForNoTerm + ". Query terms: " + queryTerms)

		// if no assignment at all is done
		// add the annotation to the map at -99 ('no term assigned'), if the query contains termID=IRISConstants.ANNOTATION_NO_TERM_ASSIGNED
		if (userByTermList.isEmpty()){
			log.debug("This annotation does not have any terms assigned at all [" + annotation.id + "]")
			if (searchForNoTerm){
				annotationMap[IRISConstants.ANNOTATION_NO_TERM_ASSIGNED.toString()].get("annotations").add(irisAnn)
			}
		} else {
			boolean userAssignedOntologyTerm = false

			// search the assignments for a match both "user" and "term"
			for (assignment in userByTermList){

				//log.debug("Querying user is " + user.cmUserName + " (" + user.cmID + ")")

				// if the user has assessed this annotation
				if (user.cmID in assignment.get("user")){
					log.debug("user " + user.cmUserName + " has assignment: " + (user.cmID in assignment.get("user")))

					// check, if the assessed term ID is in the query list
					long ontologyTermID = assignment.get("term")

					// if the user has assigned one of the queryTerms
					// ASSUMPTION: USER CAN ONLY SET UNIQUE LABELS
					if (queryTerms.contains(""+ontologyTermID)){
						String cmTermName = terms.list.find{it.id == ontologyTermID}.get("name")
						log.debug(user.cmUserName + " assigned an ontology term "
								+ cmTermName + " (" + ontologyTermID + ") to [" + annotation.id + "]")

						// store the properties in the irisAnnotation
						irisAnn.setCmUserID(user.cmID)
						irisAnn.setCmTermID(assignment.get("term"))
						irisAnn.setCmTermName(cmTermName)
						irisAnn.setCmOntology(ontologyID)
						irisAnn.setAlreadyLabeled(true)

						// add the annotation to the corresponding list in the map
						annotationMap[ontologyTermID+""].get("annotations").add(irisAnn)
						userAssignedOntologyTerm = true
						break
					} else {
						log.debug(user.cmUserName + " did not assign any ontology term to [" + annotation.id
								+ "], checking for 'no term' query")
						if (!searchForNoTerm){
							log.debug(user.cmUserName + " did not assign any term to [" + annotation.id + "]")
							userAssignedOntologyTerm = false
						} else {
							log.debug(user.cmUserName + " did not assign any query term to [" + annotation.id + "], skipping annotation")
							// if querying annotations 'without terms' and the user has none of the terms in the list
							// count this as hit such that the annotation does not get added to the response
							userAssignedOntologyTerm = true
						}
					}
				}

				// if the user is not in this assignment, continue searching in the other ones
				else {
					log.debug("User " + user.cmUserName + " is not in this assignment, continuing search...")
				}
			}

			// if the user does not have an assignment and we are searching for 'noTerms'
			// add it to the no term list
			if (searchForNoTerm && !userAssignedOntologyTerm){
				annotationMap[IRISConstants.ANNOTATION_NO_TERM_ASSIGNED.toString()].get("annotations").add(irisAnn)
			}
		}

		return irisAnn
	}

	/**
	 * Gets the current annotation a list and updates the image where the calling user is navigating.
	 * 
	 * @param annotations the list
	 * @param startIdx the start index
	 * @param hideCompleted flag, whether terms with a label should be skipped
	 * @param ontologyID the ontology ID
	 * @param terms the list of terms
	 * @param user the user to search for
	 * @param image the image where the user is searching in
	 * @return an IRIS annotation, if a predecessor exists, or <code>null</code> otherwise
	 */
	be.cytomine.apps.iris.Annotation getCurrentAnnotation(AnnotationCollection annotations,
			int startIdx, boolean hideCompleted, long ontologyID, TermCollection terms, User user, Image image){

		def curr = null
		def currIrisAnn = null

		DomainMapper dm = new DomainMapper(grailsApplication)

		// DETERMINE THE CURRENT ANNOTATION
		curr = annotations.get(startIdx)
		assert curr != null
		currIrisAnn = dm.mapAnnotation(curr, null)
		currIrisAnn = resolveTerms(ontologyID, terms, user, curr, currIrisAnn)

		def origIrisAnn = currIrisAnn

		if (hideCompleted){
			if (currIrisAnn.isAlreadyLabeled()){
				// if already labeled move to first unlabeled annotation in this list
				// start at -1, such that the first element is 0
				currIrisAnn = getNextAnnotation(annotations, -1, hideCompleted, ontologyID, terms, user)
				if (currIrisAnn == null){
					currIrisAnn = origIrisAnn
				}
			}
		}
		// save the current annotation for that image
		image.setCurrentCmAnnotationID(currIrisAnn.cmID)
		image.getPrefs().putAt("annotations.hideCompleted", String.valueOf(hideCompleted))
		image.save(failOnError:true, flush:true)

		assert currIrisAnn != null

		log.debug("############ CURRENT ANNOTATION RESOLVED")

		return currIrisAnn
	}

	/**
	 * Gets the successor of an annotation in a list.
	 * 
	 * @param annotations the list
	 * @param startIdx the start index
	 * @param hideCompleted flag, whether terms with a label should be skipped
	 * @param ontologyID the ontology ID
	 * @param terms the list of terms
	 * @param user the user to search for
	 * @return an IRIS annotation, if a predecessor exists, or <code>null</code> otherwise
	 */
	be.cytomine.apps.iris.Annotation getNextAnnotation(AnnotationCollection annotations,
			int startIdx, boolean hideCompleted, long ontologyID, TermCollection terms, User user){

		def succ = null
		def succIrisAnn = null
		boolean foundSuccessor = false
		int succIdx = startIdx+1

		DomainMapper dm = new DomainMapper(grailsApplication)

		while(!foundSuccessor){
			try {
				succ = annotations.get(succIdx)
				succIrisAnn = dm.mapAnnotation(succ, null)
				succIrisAnn = resolveTerms(ontologyID, terms, user, succ, succIrisAnn)

				if (hideCompleted){
					if (!succIrisAnn.isAlreadyLabeled()){
						foundSuccessor = true
					} else {
						succIrisAnn = null
						// move on in the index list
						succIdx++
					}
				}else {
					foundSuccessor = true
				}
			} catch (IndexOutOfBoundsException e){
				log.debug("Index " + startIdx + " has no successor.")
				break
			}

		}

		log.debug("############ SUCCESSOR ANNOTATION RESOLVED: has successor " + foundSuccessor)

		return succIrisAnn
	}

	/**
	 * Gets the predecessor of an annotation in a list.
	 * 
	 * @param annotations the list
	 * @param startIdx the start index
	 * @param hideCompleted flag, whether terms with a label should be skipped
	 * @param ontologyID the ontology ID
	 * @param terms the list of terms
	 * @param user the user to search for
	 * @return an IRIS annotation, if a predecessor exists, or <code>null</code> otherwise
	 */
	be.cytomine.apps.iris.Annotation getPreviousAnnotation(AnnotationCollection annotations,
			int startIdx, boolean hideCompleted, long ontologyID, TermCollection terms, User user){

		def pred = null
		def predIrisAnn = null
		int predIdx = startIdx-1

		DomainMapper dm = new DomainMapper(grailsApplication)

		boolean foundPredecessor = false

		while (!foundPredecessor){
			try {
				pred = annotations.get(predIdx)
				predIrisAnn = dm.mapAnnotation(pred, null)
				predIrisAnn = resolveTerms(ontologyID, terms, user, pred, predIrisAnn)

				if (hideCompleted){
					// check if the predecessor is already labeled
					if (!predIrisAnn.isAlreadyLabeled()){
						foundPredecessor = true
					} else {
						predIrisAnn = null
						// move backwards in the index list
						predIdx--
					}
				} else {
					foundPredecessor = true
				}
			} catch (IndexOutOfBoundsException e){
				log.debug("Index " + startIdx + " has no predecessor.")
				break
			}
		}

		log.debug("############ PREDECESSOR ANNOTATION RESOLVED: has predecessor " + foundPredecessor)

		return predIrisAnn
	}
	
	/**
	 * Removes a single term, or all terms, a user has assigned to an annotation. 
	 * If the cmTermID is <code>null</code>, then all terms will be removed. 
	 *
	 * @param cytomine a Cytomine instance
	 * @param sessionID the IRIS session ID
	 * @param cmProjectID the Cytomine project ID
	 * @param cmImageID the Cytomine image ID
	 * @param cmAnnID the Cytomine annotation ID
	 * @param cmTermID the Cytomine term ID to be removed, or <code>null</code>, if <b>every</b> term should be removed
	 * @return <code>true</code>, if all terms of this user have been deleted, <code>false</code> otherwise
	 * @throws CytomineException if anything goes wrong with the Cytomine Java client
	 * @throws GroovyCastException if parameters cannot be cast
	 * @throws Exception in any case of error
	 */
	boolean deleteTermByUser(Cytomine cytomine, long sessionID, long cmProjectID, long cmImageID, long cmAnnID, Long cmTermID) throws CytomineException, GroovyCastException, Exception{
		// get session and user
		Session sess = Session.get(sessionID)
		User user = sess.getUser()
		Project irisProject = sess.getProjects().find { it.cmID == cmProjectID }
		Image irisImage = irisProject.getImages().find { it.cmID == cmImageID }

		Map<String,String> filters = new HashMap<String,String>()
		filters.put("project", String.valueOf(cmProjectID))
		filters.put("image", String.valueOf(cmImageID))
		filters.put("showTerm", "true")

		// fetch all annotations (necessary because this contains info on the assignments by this user)
		AnnotationCollection annotations = cytomine.getAnnotations(filters)
		def annList = annotations.list

		// provoke exception if the annotation is not in the list
		Annotation annotation = annotations.get(
				annList.indexOf(annList.find { ann -> ann.id == cmAnnID }))

		// remove all terms
		List userByTermList = annotation.getList("userByTerm");
		// just delete the specific term
		boolean hasTermsLeft = false
		
		// if term is NULL, delete all terms
		if (cmTermID == null){
			// search for user in all assignments
			for (assignment in userByTermList){
				if (user.cmID in assignment.get("user")){
					long cmTermID_ = Long.valueOf(assignment.get("term"))
					cytomine.deleteAnnotationTerm(cmAnnID, cmTermID_)
					log.debug("Removed term: " + cmTermID_ + " from annotation [" + annotation.getId() + "]")
				} 
			}
		} else {
			// search for user in all assignments
			for (assignment in userByTermList){
				if (user.cmID in assignment.get("user")){
					long cmTermID_ = Long.valueOf(assignment.get("term"))
					// if the term matches the query, remove it
					if (cmTermID == cmTermID_){
						cytomine.deleteAnnotationTerm(cmAnnID, cmTermID_)
						log.debug("Removed term: " + cmTermID_ + " from annotation [" + annotation.getId() + "]")
						hasTermsLeft = false
					} else {
						hasTermsLeft = true
					}
				}
			}
		}
		
		// once the user does not have any labels left, 
		// decrement the "labeledAnnotations" by 1 and re-calculate userProgress
		// then save the iris image back to the IRIS db
		if (!hasTermsLeft){
			log.debug("User " + user.cmUserName + " has no other terms assigned to annotation [" + annotation.getId() + "]")
			
			// decrement labeled annotations
			if (irisImage.labeledAnnotations>0){
				irisImage.labeledAnnotations--
			} else {
				irisImage.labeledAnnotations = 0
			}
			// compute new progress
			irisImage.userProgress = (irisImage.numberOfAnnotations==0?
					0:(int)((irisImage.labeledAnnotations/irisImage.numberOfAnnotations)*100))
			// save image
			irisImage.save(flush:true, failOnError:true)
		} else {
			log.debug("User " + user.cmUserName + " has some other terms assigned to annotation [" + annotation.getId() + "]")
		}
		
		activityService.logForAll(user, irisProject.getCmID(), irisImage.getCmID(), annotation.getId(), "Deleted term from annotation [" + annotation.getId() + "]")

		return true;
	}
	
	/**
	 * Sets a <b>single</b> term to an annotation.
	 *
	 * @param cytomine a Cytomine instance
	 * @param sessionID the IRIS session ID
	 * @param cmProjectID the Cytomine project ID
	 * @param cmImageID the Cytomine image ID
	 * @param cmAnnID the Cytomine annotation ID
	 * @param cmTermID the Cytomine term ID to be set
	 * @return the AnnotationTerm instance 
	 * @throws CytomineException if anything goes wrong with the Cytomine Java client
	 * @throws GroovyCastException if parameters cannot be cast
	 * @throws Exception in any case of error
	 */
	AnnotationTerm setUniqueTermByUser(Cytomine cytomine, long sessionID, long cmProjectID, long cmImageID, long cmAnnID, long pldTermID) throws CytomineException, GroovyCastException, Exception{
		// get session and user
		Session sess = Session.get(sessionID)
		User user = sess.getUser()
		Project irisProject = sess.getProjects().find { it.cmID == cmProjectID }
		Image irisImage = irisProject.getImages().find { it.cmID == cmImageID }

		Map<String,String> filters = new HashMap<String,String>()
		filters.put("project", String.valueOf(cmProjectID))
		filters.put("image", String.valueOf(cmImageID))
		filters.put("showTerm", "true")

		// fetch all annotations (necessary because this contains info on the assignments by this user)
		AnnotationCollection annotations = cytomine.getAnnotations(filters)
		def annList = annotations.list

		// provoke exception if the annotation is not in the list
		Annotation annotation = annotations.get(
				annList.indexOf(annList.find { ann -> ann.id == cmAnnID }))

		// check, if there is at least one term set by this user
		List userByTermList = annotation.getList("userByTerm");

		boolean hadTermsAssigned = false
		for (assignment in userByTermList){
			if (user.cmID in assignment.get("user")){
				hadTermsAssigned = true
				break
			}
		}
		
		// set the term using the extended Java client 
		// NOTE: this behaviour can be some day included in the default Java client
		CytomineX cX = new CytomineX(cytomine.host, cytomine.publicKey, cytomine.privateKey, cytomine.basePath)
		// set the term to the annotation
		AnnotationTerm annTerm = cX.setAnnotationTerm(cmAnnID, pldTermID)
		
		// once the user does not have any labels left,
		// decrement the "labeledAnnotations" by 1 and re-calculate userProgress
		// then save the iris image back to the IRIS db
		if (!hadTermsAssigned){
			log.debug("User " + user.cmUserName + " had no terms assigned to annotation [" + annotation.getId() + "]")
			
			// increment labeled annotations
			if (irisImage.labeledAnnotations<irisImage.numberOfAnnotations){
				irisImage.labeledAnnotations++
			} else {
				irisImage.labeledAnnotations = irisImage.numberOfAnnotations
			}
			// compute new progress
			irisImage.userProgress = (irisImage.numberOfAnnotations==0?
					0:(int)((irisImage.labeledAnnotations/irisImage.numberOfAnnotations)*100))
			// save image
			irisImage.save(flush:true, failOnError:true)
		} else {
			log.debug("User " + user.cmUserName + " has some terms assigned to annotation [" + annotation.getId() + "]")
		}

		activityService.logForAll(user, irisProject.getCmID(), irisImage.getCmID(), annotation.getId(), "Set unique term to annotation [" + annotation.getId() + "]")
		
		return annTerm;
	}
}
