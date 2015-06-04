
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

import be.cytomine.apps.iris.SynchronizeUserProgressJob
import grails.converters.JSON
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
    @Secured(['ROLE_IRIS_PROJECT_ADMIN', 'ROLE_IRIS_PROJECT_COORDINATOR', 'ROLE_IRIS_ADMIN'])
    def synchronizeUserProgress() {
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
}
