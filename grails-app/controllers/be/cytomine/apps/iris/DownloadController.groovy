
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
import grails.converters.JSON
import org.json.simple.JSONObject

/**
 * This controller is responsible for handling download and export requests.
 */
class DownloadController {

    def exportService
    def grailsApplication

    def beforeInterceptor = {
        log.debug("Executing action $actionName with params $params")
    }

    /**
     *
     */
    def requestAgreementAsCSV(){

    }

    /**
     * Request the download link for an image dataset.
     * The query submitted by the request object defines the scope of the export.
     * @return
     */
    def requestImageDataset() {
        // ##################################################
        Cytomine cytomine = request['cytomine']
        IRISUser irisUser = request['user']

        long projectID = params.long('cmProjectID')

        def payload = (request.JSON)

        def annotationIDs = payload['annotationIDs']
        def userIDs = payload['userIDs']
        def termIDs = payload['termIDs']
        def imageIDs = payload['imageIDs']
        def settings = payload['imageExportSettings']

        log.info("Getting data and creating ZIP file for " + irisUser.getCmFirstName() + " " + irisUser.getCmLastName())

        // make the ZIP file, store it and send back the link to retrieve it
        def fileURI = exportService.exportDataset(cytomine,irisUser,projectID,annotationIDs,userIDs,termIDs,imageIDs,settings)

        log.info("Done creating ZIP file...")

        def irisHost = grailsApplication.config.grails.cytomine.apps.iris.host

        JSONObject resp = new JSONObject();
        resp.putAt("source", fileURI)
        resp.putAt("fullURL", irisHost + "/" + fileURI + "?publicKey="+params['publicKey']+"&privateKey="+params['privateKey'])
        render resp as JSON
    }
}
