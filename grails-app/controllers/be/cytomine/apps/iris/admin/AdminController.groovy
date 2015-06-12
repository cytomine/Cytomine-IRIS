
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
package be.cytomine.apps.iris.admin

import be.cytomine.apps.iris.IRISUser
import be.cytomine.apps.iris.IRISUserProjectSettings
import be.cytomine.apps.iris.SynchronizeUserProgressJob
import be.cytomine.apps.iris.Utils
import be.cytomine.client.Cytomine
import be.cytomine.client.CytomineException
import be.cytomine.client.collections.UserCollection
import be.cytomine.client.models.Project
import be.cytomine.client.models.User
import grails.converters.JSON
import grails.util.Environment
import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.json.simple.JSONObject
import org.springframework.security.access.annotation.Secured

class AdminController {

    def beforeInterceptor = {
        log.debug("Executing action $actionName with params $params")
    }

    def grailsApplication
    def sessionService
    def imageService
    def activityService
    def mailService
    def syncService
    def executorService
    def annotationService


    def index() {}

    /**
     * Manually trigger the synchronization of the user progress.
     * The calling user must have an admin login, otherwise he receives a 503 message.
     *
     * @return
     */
    //@Secured(['ROLE_IRIS_PROJECT_ADMIN', 'ROLE_IRIS_PROJECT_COORDINATOR', 'ROLE_IRIS_ADMIN'])
    def synchronizeAllUserProgress() {
        def resp = new SynchronizeUserProgressJob(grailsApplication,
                sessionService, imageService,
                activityService, mailService,
                syncService, executorService,
                annotationService, "override_config")
                .execute()

        if (resp == true || resp == []) {
            render (['success':true, 'msg': 'Synchronization succeeded without errors.'] as JSON)
        } else if (resp == "deactivated") {
            render (['success':true, 'msg': 'Synchronization is deactivated.'] as JSON)
        } else {
            String message = 'Synchronization failed with errors\n'
            if (resp != []){
                String exStr = message + "\nEXCEPTIONS ARE ORDERED REVERSE CHRONOLOGICALLY (MOST RECENT FIRST)"
                resp.reverse()
                resp.each { item ->
                    exStr += (item['msg'] + "\n" + item['exception'] + "\n " +
                            "------------------------------------------------------\n")
                }
            }
            response.setStatus(500)
            render (['success':false, 'msg': message] as JSON)
        }
    }


    /**
     * Manually trigger the synchronization of the user progress for a specific project and all or specific images.
     * The calling user must have an admin login, otherwise he receives a 503 message.
     *
     * @return
     */
    //@Secured(['ROLE_IRIS_PROJECT_ADMIN', 'ROLE_IRIS_PROJECT_COORDINATOR', 'ROLE_IRIS_ADMIN'])
    def synchronizeUserProjectProgress() {

        Cytomine cytomine = request['cytomine']
        IRISUser irisUser = request['user']
        Long cmProjectID = params.long('cmProjectID')
        Long cmUserID = params.long('cmUserID')
        String queryImageIDs = params['images']

        def resp = syncService.synchronizeUserLabelingProgress(cytomine,
                irisUser, cmProjectID, cmUserID, queryImageIDs)

        if (resp == true || resp == []) {
            render (['success':true, 'msg': 'Synchronization succeeded without errors.'] as JSON)
        } else {
            String message = 'Synchronization failed with errors\n'
            if (resp != []){
                String exStr = message + "\nEXCEPTIONS ARE ORDERED REVERSE CHRONOLOGICALLY (MOST RECENT FIRST)"
                resp.reverse()
                resp.each { item ->
                    exStr += (item['msg'] + "\n" + item['exception'] + "\n " +
                            "------------------------------------------------------\n")
                }
            }
            response.setStatus(500)
            render (['success':false, 'msg': message] as JSON)
        }
    }

    /**
     * Manually trigger the synchronization of the user progress for a specific project and all its images.
     * The calling user must have an admin login, otherwise he receives a 503 message.
     *
     * @return
     */
    //@Secured(['ROLE_IRIS_PROJECT_ADMIN', 'ROLE_IRIS_PROJECT_COORDINATOR', 'ROLE_IRIS_ADMIN'])
    def synchronizeAllUserProjectProgress() {

        Cytomine cytomine = request['cytomine']
        IRISUser irisUser = request['user']
        Long cmProjectID = params.long('cmProjectID')

        def resp = syncService.synchronizeUserLabelingProgress(cytomine, irisUser, cmProjectID, null, null)

        if (resp == true || resp == []) {
            render (['success':true, 'msg': 'Synchronization succeeded without errors.'] as JSON)
        } else {
            String message = 'Synchronization failed with errors\n'
            if (resp != []){
                String exStr = message + "\nEXCEPTIONS ARE ORDERED REVERSE CHRONOLOGICALLY (MOST RECENT FIRST)"
                resp.reverse()
                resp.each { item ->
                    exStr += (item['msg'] + "\n" + item['exception'] + "\n " +
                            "------------------------------------------------------\n")
                }
            }
            response.setStatus(500)
            render (['success':false, 'msg': message] as JSON)
        }
    }

    /**
     * Authorize the user as a coordinator for a specific project.
     * @return
     */
    //@Secured(['ROLE_IRIS_PROJECT_ADMIN', 'ROLE_IRIS_PROJECT_COORDINATOR', 'ROLE_IRIS_ADMIN'])
    def authorizeCoordinator() {

        String tmpEmail = null
        try {
            // TODO check whether the user is allowed to make this request

            Cytomine cytomine = request['cytomine']
            IRISUser irisUser = request['user']
            Long cmProjectID = params.long('cmProjectID')
            Long cmUserID = params.long('cmUserID')

            if (params['irisCoordinator'] == null){
                throw new CytomineException(400, "The request requires additional parameters!")
            }

            Boolean targetValueCoord = Boolean.valueOf(params['irisCoordinator'])

            // get the user from the IRIS db
            IRISUser user = IRISUser.findByCmID(cmUserID)

            if (user == null){
                throw new CytomineException(404, "The user cannot be updated!")
            }

            // a user cannot revoke its own coordinator rights!
            if (irisUser.cmID == cmUserID && targetValueCoord == false) {
                throw new CytomineException(503, "The user cannot revoke its own project coordinator rights!")
            }

            // assign the email
            tmpEmail = user.cmEmail

            Project cmProject = cytomine.getProject(cmProjectID)

            // get the project settings
            IRISUserProjectSettings settings = IRISUserProjectSettings
                    .findByCmProjectIDAndUser(cmProjectID,user)

            if (settings == null){
                throw new CytomineException(404, "The project settings cannot be found.")
            }

            // update the value
            settings.irisCoordinator = targetValueCoord
            // enable the project for that user, if he/she became coordinator
            if (targetValueCoord == true){
                settings.enabled = true
            } else {
                // do not disable the project once the rights as coordinator are revoked
            }
            settings.merge(flush: true)

            if (targetValueCoord == true){
                // if the user is now a coordinator, send a mail
                // send a confirmation mail to the user
                String recipient = user.cmEmail

                String hostname = grailsApplication.config.grails.host
                String urlprefix = grailsApplication.config.grails.cytomine.apps.iris.host
                String restURL = (urlprefix + "/index.html#/projects")

                String subj = ("[Cytomine-IRIS: " + hostname + "] Your Project Coordinator Request")
                String bdy = ("Dear " + user.cmFirstName + " " + user.cmLastName + ",\n\n" +
                        "you are now coordinator of the project [" + cmProject.get("name") + "].\n" +
                        "Start right away configuring the project for other participants," +
                        " or viewing statistics using the following link: \n" +
                        restURL +
                        "\n\nBest regards,\n" +
                        "Cytomine-IRIS")

                // sync all project users without their API keys and add new IRISProjectSettings
                // for that project, if they are not already present
                UserCollection projectUsers = cytomine.getProjectUsers(cmProjectID)
                for (User u in projectUsers.getList()){
                    log.info("Synchronizing user " + u.get("username") + " without API keys.")
                    // create/update the users in the IRIS database
                    IRISUser usr = syncService.synchronizeUserNoAPIKeys(u) // no calls to Cytomine REST API
                    // create/update the IRISUserProjectSettings for that user
                    syncService.synchronizeProject(cytomine, usr, cmProjectID, cmProject) // no calls to Cytomine REST API
                }

                // send an email to the user that he/she is now coordinator
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

                render(['success': true, 'msg': 'User \'' + user.cmUserName + '\' ' +
                        'has been successfully authorized as project coordinator for ' +
                        'project [' + cmProject.get("name") + ']!',
                        'settings': settings] as JSON)

            } else {
                // the rights have been revoked
                render(['success': true, 'msg': 'User \'' + user.cmUserName + '\' ' +
                        'has been successfully removed as project coordinator for ' +
                        'project [' + cmProject.get("name") + ']!',
                        'settings': settings] as JSON)
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
            errorMsg['error']['extramessage'] = "We apologize for this inconvenience and try to solve the problem as soon as possible. " +
                    "Meanwhile, please contact the user directly via email (" + tmpEmail + ")!";
            render errorMsg as JSON
        }
    }

    /**
     * Enable a project for a user.
     * @return
     */
    def authorizeProjectAccess() {

        String tmpEmail = null
        try {
            // TODO check whether the user is allowed to make this request

            Cytomine cytomine = request['cytomine']
            IRISUser irisUser = request['user']
            Long cmProjectID = params.long('cmProjectID')
            Long cmUserID = params.long('cmUserID')

            if (params['projectAccess'] == null){
                throw new CytomineException(400, "The request requires additional parameters!")
            }

            Boolean targetValueEnabled = Boolean.valueOf(params['projectAccess'])

            // get the user from the IRIS db
            IRISUser user = IRISUser.findByCmID(cmUserID)

            if (user == null){
                throw new CytomineException(404, "The user cannot be updated!")
            }

            // assign the email
            tmpEmail = user.cmEmail

            Project cmProject = cytomine.getProject(cmProjectID)

            // get the project settings
            IRISUserProjectSettings settings = IRISUserProjectSettings
                    .findByCmProjectIDAndUser(cmProjectID,user)

            if (settings == null){
                throw new CytomineException(404, "The project settings cannot be found.")
            }

            Boolean before = settings.enabled

            // enable the project for that user
            settings.enabled = targetValueEnabled
            settings.merge(flush: true)

            // only send a mail once
            if (before == false && targetValueEnabled == true){
                // if the user now has access
                // send a confirmation mail to the user
                String recipient = user.cmEmail

                String hostname = grailsApplication.config.grails.host
                String urlprefix = grailsApplication.config.grails.cytomine.apps.iris.host
                String restURL = (urlprefix + "/index.html#/project/" + cmProjectID + "/images")

                String subj = ("[Cytomine-IRIS: " + hostname + "] Your Project Access Request")
                String bdy = ("Dear " + user.cmFirstName + " " + user.cmLastName + ",\n\n" +
                        "you have been granted the rights to project [" + cmProject.get("name") + "].\n" +
                        "Start right away viewing the images of the project using the following link: \n" +
                        restURL +
                        "\n\nBest regards,\n" +
                        "the project coordinator")

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

                render(['success': true, 'msg': 'User \'' + user.cmUserName + '\' ' +
                        'has been successfully authorized access for ' +
                        'project [' + cmProject.get("name") + ']!'] as JSON)

            } else {
                // the rights have been revoked
                render(['success': true, 'msg': 'User \'' + user.cmUserName + '\' ' +
                        'has been successfully revoked access for ' +
                        'project [' + cmProject.get("name") + ']!'] as JSON)
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
            errorMsg['error']['extramessage'] = "We apologize for this inconvenience and try to solve the problem as soon as possible. " +
                    "Meanwhile, please contact the user directly via email (" + tmpEmail + ")!";
            render errorMsg as JSON
        }
    }

    def dev(){

    }
}
