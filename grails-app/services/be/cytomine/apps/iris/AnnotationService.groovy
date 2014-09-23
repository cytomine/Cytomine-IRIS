package be.cytomine.apps.iris

import grails.converters.JSON;
import grails.transaction.Transactional
import be.cytomine.client.collections.TermCollection
import be.cytomine.client.models.Annotation

@Transactional
class AnnotationService {

    be.cytomine.apps.iris.Annotation resolveTerms(long ontologyID, TermCollection terms, User user, Annotation annotation, be.cytomine.apps.iris.Annotation irisAnn){
		// grab all terms from all users for the current annotation
		List userByTermList = annotation.getList("userByTerm");
		
		for (assignment in userByTermList){
			//println currentUser.get("id") + ", " + assignment.get("user")
			if (user.cmID in assignment.get("user")){
				log.debug(user.cmUserName + " assigned " + terms.list.find{it.id == assignment.get("term")}.get("name"))

				// store the properties in the irisAnnotation
				irisAnn.setCmUserID(user.cmID)
				irisAnn.setCmTermID(assignment.get("term"))
				irisAnn.setCmTermName(terms.list.find{it.id == assignment.get("term")}.get("name"))
				irisAnn.setCmOntology(ontologyID)
				
				break
			}
		}
		
		return irisAnn
	}
}
