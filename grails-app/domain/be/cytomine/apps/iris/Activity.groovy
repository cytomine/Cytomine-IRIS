package be.cytomine.apps.iris

import java.util.Date;

/**
 * Activity represents an action on the server which will be persisted to the DB.
 * @author Philipp Kainz
 * @since 1.5
 */
class Activity {
	// GRAILS auto variables
	Date dateCreated = new Date()
	Date lastUpdated = new Date()

    static constraints = {
		lastActivity nullable:false
		description size:0..5000, blank:true
		user nullable:true
	}

	// class members	
	Long lastActivity = new Date().getTime()
	String description
	
	// many activities can have the same user
	User user
	
	// many activities can have the same project
	Project project
	
	// many activities can have the same image
	Image image
	
	// many activities can have the same annotation
	Annotation annotation = null
}
