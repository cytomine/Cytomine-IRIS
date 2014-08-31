package be.cytomine.apps.iris

import java.util.Date;

class Annotation {
	// GRAILS auto variables
	Date dateCreated
	Date lastUpdated

    static constraints = {
    }
	
	Image image
	static belongsTo = [image:Image]
	
}
