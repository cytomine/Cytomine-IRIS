package be.cytomine.apps.iris

import grails.converters.JSON;

import org.codehaus.groovy.grails.web.json.JSONElement;
import org.codehaus.groovy.grails.web.json.JSONObject;

class Project implements Comparable<Project>, Updateable{
	// GRAILS auto variables
	Date dateCreated = new Date()
	Date lastUpdated = new Date()

	static constraints = {
		cmID nullable:false
	}

	// class members
	Long lastActivity = new Date().getTime()
	Long cmID = 0L
	String cmName = "defaultProject"
	Boolean cmBlindMode = false
	Long cmOntology = 0L
	Integer cmNumberOfImages = 0
	Boolean hideCompletedImages = false

	// many projects can be in one session
	Session session = null

	// DELETE CASCADES
	// in order to get deleted, when the parent session is deleted,
	// we need to declare, that the project belongs to its session
	static belongsTo = [session:Session]

	// a project has many images
	SortedSet<Image> images
	static hasMany = [images:Image]
	
	// each project has a current image
	Image currentImage

	// ###################################################
	// CLASS METHODS
	@Override
	public int compareTo(Project p) {
		// sort the projects according to the last activity,
		// such that the session's first gets the current project (index 0)
		return this.lastActivity.compareTo(p.getLastActivity())
	}

	@Override
	public void updateLastActivity() {
		this.lastActivity = new Date().getTime()
	}
}
