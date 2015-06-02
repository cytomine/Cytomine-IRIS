
/* Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.cytomine.apps.iris

import grails.converters.JSON
import org.codehaus.groovy.GroovyBugError
import org.codehaus.groovy.grails.web.json.JSONElement
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.json.simple.JSONArray
import org.json.simple.JSONObject

import be.cytomine.client.Cytomine
import be.cytomine.client.CytomineException
import be.cytomine.client.collections.AnnotationCollection
import be.cytomine.client.collections.TermCollection
import be.cytomine.client.models.Annotation
import be.cytomine.client.models.AnnotationTerm
import be.cytomine.client.models.ImageInstance


/**
 * Controller for managing annotations.
 *
 * @author Philipp Kainz
 * @since 0.4
 */
class AnnotationController {

    def grailsApplication
    def sessionService
    def annotationService
    def activityService

    def beforeInterceptor = {
        log.debug("Executing action $actionName with params $params")
      }

    /**
     * Get all annotations for a specific project and its images,
     * where a specific user has assigned one or more special terms.
     *
     * @return the annotations as JSON array
     */
    def getAnnotationsByUser() {
        try {
            // ##################################################
            Cytomine cytomine = request['cytomine']
            IRISUser irisUser = request['user']

            long projectID = params.long('cmProjectID')
            String imageIDs = params['images']
            String termIDs = params['terms']

            int offset = (params['offset'] == null ? 0 : params.int('offset'))
            int max = (params['max'] == null ? 0 : params.int('max'))

            JSONObject annotationMap = annotationService.filterAnnotationsByUser(cytomine, irisUser,
                    projectID, imageIDs, termIDs, offset, max)

            render annotationMap as JSON

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
     * Sets a unique term to an annotation.
     *
     * @return
     */
    def setUniqueTerm() {
        try {
            Cytomine cytomine = request['cytomine']
            IRISUser irisUser = request['user']

            long cmProjectID = params.long('cmProjectID')
            long cmImageID = params.long('cmImageID')
            long cmAnnID = params.long('cmAnnID')
            long cmTermID = params.long('cmTermID')

            def payload = (request.JSON)
            // example {annotation: 12345, term: 1239458}
            long pldTermID = Long.valueOf(payload.get('term'))
            long pldAnnID = Long.valueOf(payload.get('annotation'))

            if (!(pldAnnID == cmAnnID && pldTermID == cmTermID)) {
                throw new IllegalArgumentException("The identifiers in URL and payload do not match!")
            }

            // call the service
            def result = annotationService.setUniqueTermByUser(cytomine, irisUser,
                    cmProjectID, cmImageID, cmAnnID, pldTermID)

            render result as JSON

        } catch (CytomineException e1) {
            // exceptions from the cytomine java client
            response.setStatus(e1.httpCode)
            JSONObject errorMsg = new Utils().resolveCytomineException(e1)
            render errorMsg as JSON
        } catch (GroovyCastException e2) {
            // send back 400 if the project ID is other than long format
            response.setStatus(400)
            JSONObject errorMsg = new Utils().resolveException(e2, 400)
            render errorMsg as JSON
        } catch (Exception e3) {
            // on any other exception render 500
            response.setStatus(500)
            JSONObject errorMsg = new Utils().resolveException(e3, 500)
            render errorMsg as JSON
        }
    }

    /**
     * Deletes a specific term for a given user on a single annotation.
     *
     * @return
     */
    def deleteTerm() {
        try {
            Cytomine cytomine = request['cytomine']
            IRISUser irisUser = request['user']

            long cmProjectID = params.long('cmProjectID')
            long cmImageID = params.long('cmImageID')
            long cmAnnID = params.long('cmAnnID')
            long cmTermID = params.long('cmTermID')

            if (cmTermID == IRISConstants.ANNOTATION_NO_TERM_ASSIGNED) {
                // get the annotation and delete all terms in a loop
                // remove all terms by passing 'null' as last argument
                def result = annotationService.deleteTermByUser(cytomine, irisUser,
                        cmProjectID, cmImageID, cmAnnID, null)

                render result as JSON
            } else {
                def result = annotationService.deleteTermByUser(cytomine, irisUser,
                        cmProjectID, cmImageID, cmAnnID, cmTermID)

                render result as JSON
            }

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
     * Deletes all terms of the querying user from a specific annotation.
     * @return
     */
    def deleteAllTerms() {
        try {
            Cytomine cytomine = request['cytomine']
            IRISUser irisUser = request['user']

            long cmProjectID = params.long('cmProjectID')
            long cmImageID = params.long('cmImageID')
            long cmAnnID = params.long('cmAnnID')

            // last argument 'null' means delete all terms of this user
            def result = annotationService.deleteTermByUser(cytomine, irisUser,
                    cmProjectID, cmImageID, cmAnnID, null)

            render result as JSON
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
     * Gets predecessor (if any), query annotation, successor (if any).
     * @return
     */
    def getAnnotation3Tuple() {
        try {
            Cytomine cytomine = request['cytomine']
            IRISUser user = request['user']

            long cmProjectID = params.long('cmProjectID')
            long cmImageID = params.long('cmImageID')

            def hc = params['hideCompleted']


            Boolean hideCompleted = null
            if (!"null".equals(hc+"")){
                hideCompleted = Boolean.parseBoolean(hc+"")
            }

            Long cmAnnID = null
            try {
                cmAnnID = Long.valueOf(params['currentAnnotation'])
            } catch (NumberFormatException nfe) {
                log.warn("Parameter value of 'currentAnnotation' is 'null' or 'undefined', "
                        + "this query will return the first one or two annotations!")
            }

            JSONObject result = annotationService.get3Tuple(cytomine, user,
                    cmProjectID, cmImageID, cmAnnID, hideCompleted)

            render(result as JSON)
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
}
