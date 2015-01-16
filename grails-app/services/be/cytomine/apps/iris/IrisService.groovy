package be.cytomine.apps.iris

import grails.transaction.Transactional

/**
 * Common IRIS services.
 *
 * @author Philipp Kainz
 * @since 1.9
 */
@Transactional
class IrisService {

    def activityService

    /**
     * Schedules a task for execution.
     * The closure will be executed, as soon as there is a thread available.
     *
     * @param c the Closure object
     * @return the UUID of the task
     */
    UUID appendToDBQueue(Closure c) {

        ScheduledTask st = new ScheduledTask(c)
        activityService.log("Queueing task [" + st.getId() + "] for scheduled execution")
        runAsync {
            activityService.log("Executing scheduled task [" + st.getId() + "]")
            st.closure()
        }
        return  st.getId()
    }

}
