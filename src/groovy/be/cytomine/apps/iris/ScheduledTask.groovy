package be.cytomine.apps.iris

import org.codehaus.groovy.grails.orm.hibernate.support.ClosureEventListener

/**
 * Created by phil on 08/01/15.
 */
class ScheduledTask {

    UUID id = UUID.randomUUID()
    Closure closure

    public ScheduledTask(Closure closure){
        this.closure = closure
    }

}
