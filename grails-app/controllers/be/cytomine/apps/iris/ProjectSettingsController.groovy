
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
import be.cytomine.apps.iris.model.IRISProject
import be.cytomine.client.Cytomine
import be.cytomine.client.CytomineException
import be.cytomine.client.models.Project
import grails.converters.JSON
import grails.util.Environment
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

    def projectService
    def imageService
    def syncService
    def mailService
    def activityService
    def grailsApplication

    def beforeInterceptor = {
        log.debug("Executing action $actionName with params $params")
    }

    /**
     * Get the list of all users for a particular project.
     * @return
     */
    def userProjectSettingsList() {
        try {
            Cytomine cytomine = request['cytomine']
            IRISUser irisUser = request['user']
            Long cmProjectID = params.long('cmProjectID')
            String userIDs = params['users']
            int offset = (params['offset'] == null ? 0 : params.int('offset'))
            int max = (params['max'] == null ? 0 : params.int('max'))

            // get the IRIS project
            IRISProject project = projectService.getProject(cytomine, irisUser,
                    cmProjectID, ['updateSession':false])

            // get the settings for the user(s)
            def settingsList = IRISUserProjectSettings.findAllByCmProjectID(cmProjectID)

            def userList
            if (userIDs == null) {
                userList = IRISUser.findAll {
                    cmID in settingsList.collect {
                        it.user.cmID
                    }
                }
            } else {
                def usersArray = String.valueOf(userIDs).split(",").collect { Long.valueOf(it) }

                userList = IRISUser.findAll {
                    cmID in settingsList.findAll {
                        it.user.cmID in usersArray
                    }.collect{
                        it.user.cmID
                    }
                }
            }

            Utils utils = new Utils()

            // inject the user project settings
            def allUsersWithProjectSettings = []
            userList.each { user ->
                def json = utils.toJSONObject(user)
                // remove the public and private key
                json['cmPublicKey'] = null
                json['cmPrivateKey'] = null
                json.putAt("projectSettings", settingsList.find { settings -> settings.user.cmID == user.cmID })
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
    def imageAccess() {
        try {

            Cytomine cytomine = request['cytomine']
            IRISUser irisUser = request['user']
            Long cmProjectID = params.long('cmProjectID')
            Long cmImageID = params.long('cmImageID')
            Long cmUserID = params.long('cmUserID')

            // TODO check whether the user is allowed to alter access settings
            def checkSettings = IRISUserProjectSettings.findByCmProjectIDAndUser(cmProjectID,irisUser)

            if (checkSettings == null || checkSettings.irisCoordinator == false){
                throw new CytomineException(503, "You don't have the permission to alter image access rules!")
                return
            }

            def payload = (request.JSON)
            // example {settingsID: 18, oldValue: false, newValue: true}
            Long settingsID = Long.valueOf(payload.get('settingsID'))
            Boolean oldValue = Boolean.valueOf(payload.get('oldValue'))
            Boolean newValue = Boolean.valueOf(payload.get('newValue'))

            if (settingsID == null || oldValue == null || newValue == null) {
                throw new CytomineException(400, "SettingsID, old and new value must be set in the payload of the request!")
            }

            // TODO move to service
            // get the user image settings
            IRISUserImageSettings settings = IRISUserImageSettings
                    .findByCmProjectIDAndCmImageInstanceIDAndId(cmProjectID, cmImageID, settingsID)
            settings.setEnabled(newValue)
            settings.save(flush: true, failOnError: true)

            render(['success': true, 'msg': 'The settings have been successfully updated!', 'settings': settings] as JSON)

            // now trigger the synchronization of that image
            if (oldValue == false && newValue == true) {
                syncService.synchronizeUserLabelingProgress(cytomine,
                        irisUser, cmProjectID, cmUserID, String.valueOf(cmImageID))
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

            // get the list of images without updating the user's session
            List<IRISImage> images = imageService
                    .getAllImagesWithSettings(cytomine, irisUser,
                    projectID, userID, offset, max)

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
    def projectAccess() {
        try {
            Cytomine cytomine = request['cytomine']
            IRISUser irisUser = request['user']
            Long cmProjectID = params.long('cmProjectID')
            Long cmUserID = params.long('cmUserID')

            if (irisUser.cmID == cmUserID) {
                throw new CytomineException(400, "The calling user cannot alter its own project access settings!")
            }

            // the calling user must be a coordinator
            def checkSettings = IRISUserProjectSettings
                    .findByCmProjectIDAndUser(cmProjectID,irisUser)

            if (checkSettings == null || checkSettings.irisCoordinator == false){
                throw new CytomineException(503, "You don't have the permission to alter project access rules!")
            }

            def payload = (request.JSON)
            // example {settingsID: 21 (may also be null), oldValue: false, newValue: true}
            Long settingsID = Long.valueOf(payload.get('settingsID'))
            Boolean oldValue = Boolean.valueOf(payload.get('oldValue'))
            Boolean newValue = Boolean.valueOf(payload.get('newValue'))

            if (oldValue == null || newValue == null) {
                throw new CytomineException(400, "Old and new value must be set in the payload of the request!")
            }

            // TODO move to service
            // get the user
            IRISUser user = IRISUser.findByCmID(cmUserID)

            // get the user's project settings
            IRISUserProjectSettings settings = IRISUserProjectSettings.
                    findByCmProjectIDAndUser(cmProjectID, user)
            settings.enabled = newValue
            settings.merge(flush: true, failOnError: true)

            render(['success': true, 'msg': 'The settings have been successfully updated!', 'settings': settings] as JSON)

            // now trigger the synchronization of that project for all images
            if (oldValue == false && newValue == true) {
                syncService.synchronizeUserLabelingProgress(cytomine,
                        irisUser, cmProjectID, cmUserID, null)
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
     * Alter the access to a particular project for a given user
     * @return
     */
    def userAutoSync() {
        try {

            Cytomine cytomine = request['cytomine']
            IRISUser irisUser = request['user']
            Long cmProjectID = params.long('cmProjectID')
            Long cmUserID = params.long('cmUserID')

            // check if the user is allowed to do that
            def checkSettings = IRISUserProjectSettings.findByCmProjectIDAndUser(cmProjectID,irisUser)

            if (checkSettings == null || checkSettings.irisCoordinator == false){
                throw new CytomineException(503, "You don't have the permission to alter synchronization settings!")
                return
            }

            def payload = (request.JSON)
            // example {oldValue: false, newValue: true}
            Boolean oldValue = Boolean.valueOf(payload.get('oldValue'))
            Boolean newValue = Boolean.valueOf(payload.get('newValue'))

            if (oldValue == null || newValue == null) {
                throw new CytomineException(400, "Old and new value must be set in the payload of the request!")
            }

            // TODO move to service
            // update the user
            IRISUser user = IRISUser.findByCmID(cmUserID)
            user.setSynchronize(newValue)
            user.save(flush: true, failOnError: true)

            render(['success': true, 'msg': 'The settings have been successfully updated!', 'user': user] as JSON)

            // now trigger the synchronization of that project for all images
            if (oldValue == false && newValue == true) {
                syncService.synchronizeUserLabelingProgress(cytomine,
                        irisUser, cmProjectID, cmUserID, null)
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
     * Request to become a project coordinator from the IRIS administrator.
     * @return
     */
    def requestProjectCoordinator() {
        try {
            Cytomine cytomine = request['cytomine']
            IRISUser irisUser = request['user']
            Long cmProjectID = params.long('cmProjectID')
            Long cmUserID = params.long('cmUserID')

            def payload = (request.JSON)

            // example {message: "this is a message to the admin"}
            String userMessage = String.valueOf(payload.get('message'))

            IRISUser user = IRISUser.findByCmID(cmUserID)
            if (user == null) {
                throw new CytomineException(404, "The user with ID [" + cmUserID + "] cannot be found!")
            }

            Project p = cytomine.getProject(cmProjectID)
            // check whether the user is already a coordinator
            IRISUserProjectSettings settings = IRISUserProjectSettings
                    .findByCmProjectIDAndUser(cmProjectID, user)
            if (settings?.irisCoordinator) {
                // send back a notification
                render(['success': true, 'msg': 'You are already coordinator for project [' + p.get("name") + '].'] as JSON)
                return
            }

            // get all coordinator names for that project and send it to the admin in addition
            List allSettings = IRISUserProjectSettings.findAllByCmProjectIDAndIrisCoordinator(cmProjectID,new Boolean(true))

            // get all coordinators
            // make an array of all coordinator names
            def allCoordsNames = allSettings.collect { setting ->
                setting.user.cmLastName + " " + setting.user.cmFirstName
            }
            // join them to a list
            def allCoordsStr = allCoordsNames.join("\n")

            // make a token for the admin to directly assign the rights to the user
            UserToken usrTkn = new UserToken()
            usrTkn.description = ("Project coordinator authorization for user '" + cmUserID
                    + "', project '" + cmProjectID + "'")
            usrTkn.user = user
            usrTkn.save(flush: true, failOnError: true)

            String recipient = grailsApplication.config.grails.cytomine.apps.iris.server.admin.email
            String hostname = grailsApplication.config.grails.host
            String urlprefix = grailsApplication.config.grails.cytomine.apps.iris.host
            String restURL = (urlprefix + "/api/admin/project/" + cmProjectID
                    + "/user/" + cmUserID + "/authorize/coordinator.json?irisCoordinator=true" +
                    "&token=" + usrTkn.token)
            String adminLoginURL = (urlprefix + "/admin/")

            log.info("Sending email to admin [" + recipient + "]...")

            String subj = ("[Cytomine-IRIS: " + hostname + "] Project Coordinator Request")

            String bdy = ("Dear Cytomine-IRIS administrator, \n\n" +
                    user.cmFirstName + " " + user.cmLastName + " (" + user.cmEmail + ") requests" +
                    " to be assigned as PROJECT COORDINATOR for the project [" + p.get("name") + "] " +
                    "on the IRIS host [" + hostname + "]. \n\n" +
                    "You can directly grant the user the rights by clicking the following link: \n"
                    + restURL + "\n\n" +
                    "Alternatively, log into the admin interface of IRIS and do it manually: \n" +
                    adminLoginURL +
                    "\n\n\n#### BEGIN CUSTOM USER MESSAGE ####\n"
                    + userMessage.toString().replace("\n", "\n") +
                    "\n#### END CUSTOM USER MESSAGE ####\n" +
                    "\n\n\n" +
                    "Other coordinators for this project (" +
                    allCoordsNames.size() + " users): " +
                    allCoordsStr +
                    "\n\n\n" +
                    "Disclaimer: You receive this message, because your email address is registered on " +
                    "Cytomine-IRIS host [" + hostname + "] as administrator address.")

            log.info("Sending coordinator request email to admin [" + recipient + "]...")
            activityService.log(user, "Requesting project coordinator rights for [" + cmProjectID + "]")

            // send an email to the user that he/she now has access
            if (Environment.current == Environment.PRODUCTION){
                mailService.sendMail {
                    async false
                    to recipient
                    subject subj
                    body String.valueOf(bdy)
                }
            } else {
                mailService.sendMail {
                    async false
                    to "cytomine-iris@pkainz.com"//recipient
                    subject ("DEVELOPMENT MESSAGE: " + subj) // subj
                    body String.valueOf(bdy)
                }
            }

            render(['success': true, 'msg': 'The request has been sent to the administrator!'] as JSON)

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
            errorMsg['error']['extramessage'] = "We apologize for this inconvenience and try to solve the problem as soon as possible. " +
                    "Meanwhile, please contact the IRIS administrator directly via email (" +
                    grailsApplication.config.grails.cytomine.apps.iris.server.admin.email +
                    ") to complete your request!";
            render errorMsg as JSON
        }
    }


    /**
     * Request access to a particular project from the coordinators.
     * @return
     */
    def requestProjectAccess() {
        try {
            Cytomine cytomine = request['cytomine']
            IRISUser irisUser = request['user']
            Long cmProjectID = params.long('cmProjectID')
            Long cmUserID = params.long('cmUserID')

            def payload = (request.JSON)

            // example {message: "this is a message to the coordinator"}
            String userMessage = String.valueOf(payload.get('message'))

            IRISUser user = IRISUser.findByCmID(cmUserID)
            if (user == null) {
                throw new CytomineException(404, "The user with ID [" + cmUserID + "] cannot be found!")
            }

            Project p = cytomine.getProject(cmProjectID)
            // check whether the user is already enabled
            IRISUserProjectSettings settings = IRISUserProjectSettings
                    .findByCmProjectIDAndUser(cmProjectID, user)
            if (settings?.enabled) {
                // send back a notification
                render(['success': true, 'msg': 'You already have access to project [' + p.get("name") + '].'] as JSON)
                return
            }

            // get all coordinator names for that project
            List allSettings = IRISUserProjectSettings.
                    findAllByCmProjectIDAndIrisCoordinator(cmProjectID, new Boolean(true))

            if (allSettings == null || allSettings.isEmpty()){
                // send back a notification
                render(['success': false, 'msg': 'There is no coordinator for project [' + p.get("name") + '] yet! ' +
                        'Please check with the people responsible for that project or request to become a ' +
                        'coordinator yourself.'] as JSON)
                return
            }

            activityService.log(user, "Requesting project access for [" + cmProjectID + "]")

            // send an email to all coordinators
            allSettings.each { sett ->
                // make a token for the project coordinators to directly enable the project for the user
                UserToken usrTkn = new UserToken()
                usrTkn.description = ("Project coordinator authorization for user '" + cmUserID
                        + "', project '" + cmProjectID + "'")
                usrTkn.user = user
                usrTkn.save(flush: true, failOnError: true)

                String recipient = sett.user.cmEmail
                String hostname = grailsApplication.config.grails.host
                String urlprefix = grailsApplication.config.grails.cytomine.apps.iris.host

                String restURL = (urlprefix + "/api/admin/project/" + cmProjectID +
                        "/user/" + cmUserID + "/authorize/access.json?projectAccess=true" +
                        "&token=" + usrTkn.token)

                String subj = ("[Cytomine-IRIS: " + hostname + "] Project Access Request")

                String bdy = ("Dear project coordinator, \n\n" +
                        user.cmFirstName + " " + user.cmLastName + " (" + user.cmEmail + ") requests" +
                        " access to the project [" + p.get("name") + "] " +
                        "on the IRIS host [" + hostname + "]. \n\n" +
                        "You can directly grant the user the rights by clicking the following link: \n"
                        + restURL + "\n\n" +
                        "\n\n\n#### BEGIN CUSTOM USER MESSAGE ####\n"
                        + userMessage.toString().replace("\n", "\n") +
                        "\n#### END CUSTOM USER MESSAGE ####\n" +
                        "\n\n\n" +
                        "Disclaimer: You receive this message, because your email address is registered on " +
                        "Cytomine-IRIS host [" + hostname + "] as project coordinator.")

                log.info("Sending project access request email to coordinator [" + recipient + "]...")

                // send an email to the user that he/she now has access
                if (Environment.current == Environment.PRODUCTION){
                    mailService.sendMail {
                        async true
                        to recipient
                        subject subj
                        body String.valueOf(bdy)
                    }
                } else {
                    mailService.sendMail {
                        async true
                        to "cytomine-iris@pkainz.com"//recipient
                        subject ("DEVELOPMENT MESSAGE: " + subj) // subj
                        body String.valueOf(bdy)
                    }
                }
            }

            render(['success': true, 'msg': 'The request has been sent to the coordinators!'] as JSON)

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
            errorMsg['error']['extramessage'] = "We apologize for this inconvenience and try to solve the problem as soon as possible. " +
                    "Meanwhile, please contact the IRIS administrator directly via email (" +
                    grailsApplication.config.grails.cytomine.apps.iris.server.admin.email +
                    ") to complete your request!";
            render errorMsg as JSON
        }
    }
}