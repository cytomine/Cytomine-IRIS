package be.cytomine.apps.iris

import be.cytomine.apps.iris.model.IRISImage
import be.cytomine.apps.iris.model.IRISProject
import be.cytomine.client.Cytomine
import be.cytomine.client.CytomineException
import be.cytomine.client.models.Ontology
import grails.converters.JSON
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.json.simple.JSONObject

/**
 * This class provides methods for handling custom requests 
 * regarding Project instances.
 *
 * @author Philipp Kainz
 *
 */
class ProjectController {

    def beforeInterceptor = {
        log.debug("Executing action $actionName with params $params")
    }

    /**
     * The injected ProjectService instance for this controller.
     */
    def projectService
    def sessionService

    /**
     * Get all projects from Cytomine host, which are associated with the
     * user executing this query.
     *
     * @return all projects of the user as JSON array
     */
    def getAll() {
        try {
            // get the Cytomine instance from the request (injected by the security filter!)
            Cytomine cytomine = request['cytomine']
            IRISUser irisUser = request['user']

            def allProjects = projectService.getAllProjects(cytomine, irisUser)

            render allProjects as JSON
        } catch (CytomineException e1) {
            log.error(e1)
            // exceptions from the cytomine java client
            response.setStatus(e1.httpCode)
            JSONObject errorMsg = new Utils().resolveCytomineException(e1)
            render errorMsg as JSON
        } catch (GroovyCastException e2) {
            log.error(e2)
            // send back 400 if the project ID is other than long format
            response.setStatus(400)
            JSONObject errorMsg = new Utils().resolveException(e2, 400)
            render errorMsg as JSON
        } catch (Exception e3) {
            log.error(e3)
            // on any other exception render 500
            response.setStatus(500)
            JSONObject errorMsg = new Utils().resolveException(e3, 500)
            render errorMsg as JSON
        }
    }

    /**
     * Gets an IRIS project instance.
     *
     * @return the IRIS project and optionally (if not null) the currentImage as JSON
     * ["project":project, "currentImage":image]
     */
    def openProject(){
        try {
            Cytomine cytomine = request['cytomine']
            IRISUser user = request['user']

            long cmProjectID = params.long('cmProjectID')

            IRISProject prj = projectService.getProject(cytomine, user, cmProjectID)

            Long currentImageID = prj.settings.currentCmImageInstanceID
            def prjJSON = new Utils().toJSONObject(prj)

            IRISImage img
            if (currentImageID != null){
                img = sessionService.getImage(cytomine, user, cmProjectID, currentImageID)
            }

            prjJSON['currentImage'] = img

            render prjJSON as JSON
        } catch(CytomineException e1){
            log.error(e1)
            // exceptions from the cytomine java client
            response.setStatus(e1.httpCode)
            JSONObject errorMsg = new Utils().resolveCytomineException(e1)
            render errorMsg as JSON
        } catch(GroovyCastException e2) {
            log.error(e2)
            // send back 400 if the project ID is other than long format
            response.setStatus(400)
            JSONObject errorMsg = new Utils().resolveException(e2, 400)
            render errorMsg as JSON
        } catch(Exception e3){
            log.error(e3)
            // on any other exception render 500
            response.setStatus(500)
            JSONObject errorMsg = new Utils().resolveException(e3, 500)
            render errorMsg as JSON
        }
    }

    /**
     * Gets an ontology by project ID and optionally 'deflates' the hierarchy, if the
     * request <code>params</code> contain <code>flat=true</code>.
     *
     * @return the ontology as JSON object
     */
    def getOntologyByProject(){
        try {
            Cytomine cytomine = request['cytomine']
            IRISUser irisUser = request['user']
            long pID = params.long('cmProjectID')

            IRISProject project = projectService.getProject(cytomine, irisUser, pID)
            Ontology ontology = cytomine.getOntology(project.cmOntologyID)

            if (params["flat"].equals("true")){
                // flattens the ontology but preserves the parent element in the hierarchy
                List<JSONObject> flatOntology = new Utils().flattenOntology(ontology)
                render flatOntology as JSON
            } else {
                render ontology.getAttr() as JSON
            }
        } catch(CytomineException e1){
            log.error(e1)
            // exceptions from the cytomine java client
            response.setStatus(e1.httpCode)
            JSONObject errorMsg = new Utils().resolveCytomineException(e1)
            render errorMsg as JSON
        } catch(GroovyCastException e2) {
            log.error(e2)
            // send back 400 if the project ID is other than long format
            response.setStatus(400)
            JSONObject errorMsg = new Utils().resolveException(e2, 400)
            render errorMsg as JSON
        } catch(Exception e3){
            log.error(e3)
            // on any other exception render 500
            response.setStatus(500)
            JSONObject errorMsg = new Utils().resolveException(e3, 500)
            render errorMsg as JSON
        }
    }

    /**
     * Gets the project description for a given project ID.
     *
     * @return the description of a project as JSON object, if there is one, or HTTP 404,
     * if there is no description available
     */
    def getDescription() {
        try {
            def description = projectService.getProjectDescription(request['cytomine'], params.long('cmProjectID'))

            render description as JSON
        } catch (CytomineException e1) {
            log.error(e1)
            // exceptions from the cytomine java client
            response.setStatus(e1.httpCode)
            JSONObject errorMsg = new Utils().resolveCytomineException(e1)
            render errorMsg as JSON
        } catch (GroovyCastException e2) {
            log.error(e2)
            // send back 400 if the project ID is other than long format
            response.setStatus(400)
            JSONObject errorMsg = new Utils().resolveException(e2, 400)
            render errorMsg as JSON
        } catch (Exception e3) {
            log.error(e3)
            // on any other exception render 500
            response.setStatus(500)
            JSONObject errorMsg = new Utils().resolveException(e3, 500)
            render errorMsg as JSON
        }
    }

    /**
     * Update a specific project in a session. The payload of the
     * PUT request defines the ID to be used for querying the IRIS
     * database.
     *
     * @return the updated IRIS project instance
     */
    def updateProjectSettings(){
        try{
            // get the JSON object from the payload
            def payload = (request.JSON)

            if (payload == null){
                throw new CytomineException(400, "Failed to update project settings.")
            }

            Cytomine cytomine = request['cytomine']
            IRISUser user = request['user']

            long cmProjectID = params.long('cmProjectID')

            IRISProject prj = projectService.updateProjectSettings(cytomine, user, cmProjectID, payload)
            def prjJSON = new Utils().toJSONObject(prj)

            Long currentImageID = prj.settings.currentCmImageInstanceID
            IRISImage img
            if (currentImageID != null){
                img = sessionService.getImage(cytomine, user, cmProjectID, currentImageID)
            }
            prjJSON['currentImage'] = img

            render prjJSON as JSON
        } catch(CytomineException e1){
            log.error(e1)
            // exceptions from the cytomine java client
            response.setStatus(e1.httpCode)
            JSONObject errorMsg = new Utils().resolveCytomineException(e1)
            render errorMsg as JSON
        } catch(GroovyCastException e2) {
            log.error(e2)
            // send back 400 if the project ID is other than long format
            response.setStatus(400)
            JSONObject errorMsg = new Utils().resolveException(e2, 400)
            render errorMsg as JSON
        } catch(Exception e3){
            log.error(e3)
            // on any other exception render 500
            response.setStatus(500)
            JSONObject errorMsg = new Utils().resolveException(e3, 500)
            render errorMsg as JSON
        }
    }
}
