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
     *
     * @return
     */
    @Secured(['ROLE_ADMIN', 'ROLE_SUPERADMIN'])
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
