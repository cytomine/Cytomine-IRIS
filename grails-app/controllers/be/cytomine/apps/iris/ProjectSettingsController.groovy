
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

import be.cytomine.apps.iris.model.IRISImage
import be.cytomine.client.Cytomine
import be.cytomine.client.CytomineException
import be.cytomine.client.models.Project
import grails.converters.JSON
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.json.simple.JSONObject
import org.springframework.security.access.annotation.Secured

/**
 * This controller serves project settings.
 *
 * @author Philipp Kainz
 */
//@Secured(['ROLE_PROJECT_ADMIN'])
class ProjectSettingsController {

    def settingsService
    def imageService

    def beforeInterceptor = {
        log.debug("Executing action $actionName with params $params")
    }

    /**
     * Get the list of all users for a particular project.
     * @return
     */
    def userList(){
        try {

            // TODO check whether the user is allowed to view this list

            Cytomine cytomine = request['cytomine']
            IRISUser irisUser = request['user']
            Long cmProjectID = params.long('cmProjectID')
            int offset = (params['offset'] == null ? 0 : params.int('offset'))
            int max = (params['max'] == null ? 0 : params.int('max'))

//            def userList = settingsService.getUserList(cytomine, irisUser,
//                    cmProjectID, [:], offset, max)

            // check the ACL on cytomine
            Project cmProject = cytomine.getProject(cmProjectID)

            def settingsList = IRISUserProjectSettings.findAllByCmProjectID(cmProjectID)
            def userList = IRISUser.findAll { cmID in settingsList.collect{ it.user.cmID } }

            Utils utils = new Utils()

            // inject the user project settings
            def allUsersWithProjectSettings = []
            userList.each { user ->
                def json = utils.toJSONObject(user)
                json['cmPublicKey'] = null
                json['cmPrivateKey'] = null
                json.putAt("projectSettings", settingsList.find { settings -> settings.user.cmID == user.cmID})
                allUsersWithProjectSettings.add(json)
            }

            render allUsersWithProjectSettings as JSON

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
     * Alter the access to a particular image in a project for a given user
     * @return
     */
    def imageAccess(){
        try {

            // TODO check whether the user is allowed to make this request

            Cytomine cytomine = request['cytomine']
            IRISUser irisUser = request['user']
            Long cmProjectID = params.long('cmProjectID')
            Long cmImageID = params.long('cmImageID')
            Long cmUserID = params.long('cmUserID')

            def payload = (request.JSON)
            // example {settingsID: 18, oldValue: false, newValue: true}
            Long settingsID = Long.valueOf(payload.get('settingsID'))
            Boolean oldValue = Boolean.valueOf(payload.get('oldValue'))
            Boolean newValue = Boolean.valueOf(payload.get('newValue'))

            if (settingsID == null || oldValue == null || newValue == null){
                throw new CytomineException(400, "SettingsID, old and new value must be set in the payload of the request!")
            }

            // TODO
            // get the user image settings
            IRISUserImageSettings settings = IRISUserImageSettings.findByCmProjectIDAndCmImageInstanceIDAndId(cmProjectID, cmImageID, settingsID)
            settings.setEnabled(newValue)
            settings.save(flush:true, failOnError: true)

            render "OK"

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
     * Get all images for the calling user in a specific Cytomine project.
     *
     * @return the list of images for a given project ID as JSON object including the current progress
     */
    def userImageList() {
        try {
            Cytomine cytomine = request['cytomine']
            IRISUser irisUser = request['user']

            long projectID = params.long('cmProjectID')
            long userID = params.long('cmUserID')

            int offset = (params['offset'] == null ? 0 : params.int('offset'))
            int max = (params['max'] == null ? 0 : params.int('max'))

            List<IRISImage> images = imageService
                    .getAllImagesWithSettings(cytomine, irisUser, projectID, userID, offset, max)

            render images as JSON

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
     * Alter the access to a particular project for a given user
     * @return
     */
    def projectAccess(){
        try {

            // TODO check whether the user is allowed to make this request

            Cytomine cytomine = request['cytomine']
            IRISUser irisUser = request['user']
            Long cmProjectID = params.long('cmProjectID')
            Long cmUserID = params.long('cmUserID')

            if (irisUser.cmID == cmUserID){
                throw new CytomineException(400, "The calling user cannot remove itself from the project!")
            }

            def payload = (request.JSON)
            // example {settingsID: 21, oldValue: false, newValue: true}
            Long settingsID = Long.valueOf(payload.get('settingsID'))
            Boolean oldValue = Boolean.valueOf(payload.get('oldValue'))
            Boolean newValue = Boolean.valueOf(payload.get('newValue'))

            if (settingsID == null || oldValue == null || newValue == null){
                throw new CytomineException(400, "SettingsID, old and new value must be set in the payload of the request!")
            }

            // TODO
            // get the user project settings
            IRISUserProjectSettings settings = IRISUserProjectSettings.findByCmProjectIDAndId(cmProjectID, settingsID)
            settings.setEnabled(newValue)
            settings.save(flush:true, failOnError: true)

            render "OK"

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