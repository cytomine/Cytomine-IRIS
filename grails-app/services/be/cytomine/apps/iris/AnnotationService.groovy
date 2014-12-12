package be.cytomine.apps.iris

import org.json.simple.JSONObject;

import grails.converters.JSON;
import grails.transaction.Transactional
import be.cytomine.client.collections.AnnotationCollection;
import be.cytomine.client.collections.TermCollection
import be.cytomine.client.models.Annotation

@Transactional
class AnnotationService {

	def grailsApplication

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

	be.cytomine.apps.iris.Annotation resolveTermsByUser(long ontologyID,
			TermCollection terms, User user, Annotation annotation,
			be.cytomine.apps.iris.Annotation irisAnn, boolean searchForNoTerm,
			def queryTerms, JSONObject annotationMap){
		// grab all terms from all users for the current annotation
		List userByTermList = annotation.getList("userByTerm");

//		println userByTermList

		// if no assignment at all is done, add the
		if (userByTermList.isEmpty()){
			log.debug("This annotation does not have any terms assigned at all [" + annotation.id + "]")
			if (searchForNoTerm){
				annotationMap["-99"].add(irisAnn)
			}
		} else {
		// TODO FIX LOGICAL BUG!
//			boolean hasAssignment = false
//
//			// search the assignments for a match "user" and "term"
//			for (assignment in userByTermList){
////				println ("user is in assignment: " + (user.cmID in assignment.get("user")))
//
//				// if the user has assessed this annotation
//				if (user.cmID in assignment.get("user")){
//
//					// check, if the assessed term ID is in the query list
//					long termID = assignment.get("term")
//
//					// if the user has assigned one of the queryTerms (USER CAN ONLY SET UNIQUE LABELS)
//					if (queryTerms.contains(""+termID)){
//						String cmTermName = terms.list.find{it.id == assignment.get("term")}.get("name")
//						println user.cmUserName + " assigned a query term " + cmTermName + " (" + termID + ") to [" + annotation.id + "]"
//
//						// store the properties in the irisAnnotation
//						irisAnn.setCmUserID(user.cmID)
//						irisAnn.setCmTermID(assignment.get("term"))
//						irisAnn.setCmTermName(cmTermName)
//						irisAnn.setCmOntology(ontologyID)
//						irisAnn.setAlreadyLabeled(true)
//
//						// add to map
//						annotationMap[termID+""].add(irisAnn)
//						hasAssignment = true
//						break
//					} else {
//						if (!searchForNoTerm){
//							println user.cmUserName + " assigned NO term to [" + annotation.id + "]"
//							hasAssignment = false
//						} else {
//							println user.cmUserName + " did not assign any query term to [" + annotation.id + "]"
//							// if querying annotations 'without terms' and the user has none of the terms in the list
//							// count this as hit
//							hasAssignment = true
//						}
//					}
//				}
//
//				// if the user is not in this assignment, continue searching in the other ones
//				else {
//					hasAssignment = false
//				}
//			}
//
//			// if the user does not have an assignment and we are searching for 'noTerms'
//			// add it to the no term list
//			if (searchForNoTerm && !hasAssignment){
//				annotationMap["-99"].add(irisAnn)
//			}
		}

		return irisAnn
	}

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
}
