package be.cytomine.apps.iris

import java.util.Date;

class Preference {
	// GRAILS auto variables
	Date dateCreated
	Date lastUpdated
	
    static constraints = {
    }
	
	Object key
	Object value
	
	// we want to delete the preference if the image or project gets deleted
	//static belongsTo = [project:Project,image:Image]
}
