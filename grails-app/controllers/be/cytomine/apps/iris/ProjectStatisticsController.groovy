package be.cytomine.apps.iris

import grails.converters.JSON
import org.springframework.security.access.annotation.Secured

/**
 * This controller serves project statistics on term agreements.
 *
 * @author Philipp Kainz
 */
class ProjectStatisticsController {

    def beforeInterceptor = {
        log.debug("Executing action $actionName with params $params")
        // TODO check whether the user is allowed to view the statistics
    }

    /**
     * Compute the statistics on agreements according to a specific filter.
     */
    //@Secured(['ROLE_PROJECT_ADMIN'])
    def agreements(){

        // TODO
        // Required: List of users
        def users = params.get("users")


        render 'SERVICE UNINPLEMENTED' as JSON
    }



}
