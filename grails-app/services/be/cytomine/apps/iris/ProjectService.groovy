package be.cytomine.apps.iris

import org.springframework.aop.ThrowsAdvice;

import be.cytomine.client.Cytomine;
import be.cytomine.client.CytomineException
import grails.converters.JSON;
import grails.transaction.Transactional

/**
 * This service handles all CRUD operations of a Project object.
 * It also communicates with the Cytomine host instance, if
 * required. Thus passing the <code>request['cytomine']</code> instance
 * is mandatory for some class methods.
 *
 * @author Philipp Kainz
 *
 */
@Transactional
class ProjectService {

	/**
	 * Fetches the project list of the user executing this query.
	 * Optionally, there will be an injection of the associated ontology with 
	 * the resolved hierarchy names.
	 * 
	 * @param cytomine a Cytomine instance
	 * @param resolveOntology <code>true</code>: ontology names will be
	 * fully resolved in the property <code>resolvedOntology</code>, 
	 * <code>false</code>: ontology will not be resolved
	 * 
	 * @return a ProjectCollection with optional additionally 
	 * injected properties as JSON array
	 */
	def getProjects(Cytomine cytomine, boolean resolveOntology){
		def projectList = cytomine.getProjects().list

		// optionally resolve the ontology and inject it in the project
		if (resolveOntology){
			// get the ontology object for each project
			projectList.each {
				long oID = it.get("ontology")
				def ontology = cytomine.getOntology(oID)
				it.resolvedOntology = ontology;
			}
		}
		
		return projectList
	}
	
	/**
	 * Gets the description of a project from Cytomine.
	 * 
	 * @param cytomine a Cytomine instance
	 * @param projectID the id of the project
	 * @return a JSON object
	 * @throws CytomineException if the project does not have a description
	 */
	def getProjectDescription(Cytomine cytomine, long projectID) throws CytomineException{
		def description = cytomine.getDescription(projectID, 'be.cytomine.project.Project')
		return description
	}
}
