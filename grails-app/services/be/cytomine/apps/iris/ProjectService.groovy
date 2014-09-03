package be.cytomine.apps.iris

import org.json.simple.JSONArray;
import org.springframework.aop.ThrowsAdvice;

import be.cytomine.client.Cytomine;
import be.cytomine.client.CytomineException
import be.cytomine.client.collections.ProjectCollection;
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
	 * @return a ProjectCollection as JSON array with an optionally 
	 * injected ontology 
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

	/**
	 * Checks, whether or not the calling user has access to a 
	 * requested project.
	 * 
	 * @param cytomine a Cytomine instance
	 * @param projectID the requested project ID
	 * @return <code>true</code> if the project is available for this user, <code>false</code> 
	 * otherwise
	 * 
	 * @throws CytomineException if the project does not exist
	 */
	boolean isAvailable(Cytomine cytomine, long projectID) throws CytomineException{

		def p = cytomine.getProject(projectID)

		if (p != null){
			// project exists and user is authorized
			return true
		} else {
			// project exists and user is NOT authorized
			return false
		}
	}
}
