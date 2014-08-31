package be.cytomine.apps.iris

import java.util.Date;


class Activity {
	// GRAILS auto variables
	Date dateCreated
	Date lastUpdated

    static constraints = {
		lastActivity nullable:false, blank:false
		description blank:false
		user nullable:false
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
}
