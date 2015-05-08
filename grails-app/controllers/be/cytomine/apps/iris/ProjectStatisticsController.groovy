package be.cytomine.apps.iris

import be.cytomine.client.Cytomine
import be.cytomine.client.CytomineException
import grails.converters.JSON
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.json.simple.JSONObject
import org.springframework.security.access.annotation.Secured

/**
 * This controller serves project statistics on term agreements.
 *
 * @author Philipp Kainz
 */
//@Secured(['ROLE_PROJECT_ADMIN'])
class ProjectStatisticsController {

    def statisticsService

    def beforeInterceptor = {
        log.debug("Executing action $actionName with params $params")
        // TODO check whether the user is allowed to view the statistics
    }

    /**
     * Compute the statistics on agreements according to a specific filter.
     */
    def majorityAgreements() {
        try {
            Cytomine cytomine = request['cytomine']
            IRISUser irisUser = request['user']
            Long cmProjectID = params.long('cmProjectID')
            String imageIDs = params['images']
            String termIDs = params['terms']
            int offset = (params['offset'] == null ? 0 : params.int('offset'))
            int max = (params['max'] == null ? 0 : params.int('max'))

            // compute and get the statistics
            def stats = statisticsService.getMajorityAgreement(cytomine, irisUser,
                    cmProjectID, imageIDs, termIDs, ['ignoreTies': true], offset, max)

            render stats as JSON

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
     * Get the list of all annotations
     * @return
     */
    def agreementsList(){
        try {
            Cytomine cytomine = request['cytomine']
            IRISUser irisUser = request['user']
            Long cmProjectID = params.long('cmProjectID')
            String imageIDs = params['images']
            String termIDs = params['terms']
            int offset = (params['offset'] == null ? 0 : params.int('offset'))
            int max = (params['max'] == null ? 0 : params.int('max'))

            def stats = statisticsService.getAgreementList(cytomine, irisUser,
                    cmProjectID, imageIDs, termIDs, [:], offset, max)

            render stats as JSON

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
     * Get labeling statistics of particluar users
     * @return
     */
    def userStatistics(){
        try {
            Cytomine cytomine = request['cytomine']
            IRISUser irisUser = request['user']
            Long cmProjectID = params.long('cmProjectID')
            String imageIDs = params['images']
            String termIDs = params['terms']
            String userIDs = params['users']
            int offset = (params['offset'] == null ? 0 : params.int('offset'))
            int max = (params['max'] == null ? 0 : params.int('max'))

            def stats = statisticsService.getUserStatistics(cytomine, irisUser,
                    cmProjectID, imageIDs, termIDs, userIDs, [:], offset, max)

            render stats as JSON

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