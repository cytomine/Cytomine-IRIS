package be.cytomine.apps.iris

import java.util.Map;

class Annotation {
	// GRAILS auto variables
	Date dateCreated = new Date()
	Date lastUpdated = new Date()

    static constraints = {
    }
	
	Map<String, String> prefs = [:]
	
	Image image
	static belongsTo = [image:Image]
}
