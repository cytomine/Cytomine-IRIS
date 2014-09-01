package be.cytomine.apps.iris

import java.util.Map;

class Annotation {
	// GRAILS auto variables
	Date dateCreated
	Date lastUpdated

    static constraints = {
    }
	
	Map<String, String> prefs = [:]
	
	Image image
	static belongsTo = [image:Image]
}
