
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
    }

    /**
     * Compute the statistics on agreements according to a specific filter.
     */
    // TODO currently unused
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
            String userIDs = params['users']
            String termIDs = params['terms']
            int offset = (params['offset'] == null ? 0 : params.int('offset'))
            int max = (params['max'] == null ? 0 : params.int('max'))

            def stats = statisticsService.getAgreementList(cytomine, irisUser,
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

    /**
     * Get labeling statistics of particular users
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

//    /**
//     * Get labeling statistics of a single user in comparison with all other users.
//     *
//     * @return
//     */
//    def userVsAll(){
//        try {
//            Cytomine cytomine = request['cytomine']
//            IRISUser irisUser = request['user']
//            Long cmProjectID = params.long('cmProjectID')
//            String imageIDs = params['images']
//            String termIDs = params['terms']
//            String userID = params['user']
//
//            def stats = statisticsService.oneVsAll(cytomine, irisUser,
//                    cmProjectID, imageIDs, termIDs, userID, [:])
//
//            render stats as JSON
//
//        } catch (CytomineException e1) {
//            log.error(e1)
//            // exceptions from the cytomine java client
//            response.setStatus(e1.httpCode)
//            JSONObject errorMsg = new Utils().resolveCytomineException(e1)
//            render errorMsg as JSON
//        } catch (GroovyCastException e2) {
//            log.error(e2)
//            // send back 400 if the project ID is other than long format
//            response.setStatus(400)
//            JSONObject errorMsg = new Utils().resolveException(e2, 400)
//            render errorMsg as JSON
//        } catch (Exception e3) {
//            log.error(e3)
//            // on any other exception render 500
//            response.setStatus(500)
//            JSONObject errorMsg = new Utils().resolveException(e3, 500)
//            render errorMsg as JSON
//        }
//    }
}