package be.cytomine.apps.iris

import java.util.Date;

import be.cytomine.client.models.Annotation

class Project implements Comparable<Project>, Updateable{
	// GRAILS auto variables
	Date dateCreated
	Date lastUpdated

	static constraints = {
		cmID nullable:false, blank:false
		cmName nullable:false, blank:true
	}
	
	// class members
	Long lastActivity = new Date().getTime()
	String cmID = 0L
	String cmName = "defaultProject"
	Boolean cmBlindMode = false
	
	// many projects in one session
	Session session;
	
	// DELETE CASCADES
	// in order to get deleted, when the parent session is deleted,  
	// we need to declare, that the project belongs to its session
	static belongsTo = [session:Session]
	
	// a project has many images and a list of preferences
	SortedSet<Image> images
	Set<Preference> prefs
	static hasMany = [images:Image,prefs:Preference]	

	@Override
	public int compareTo(Project p) {
		// sort the projects according to the last activity,
		// such that the session's first gets the current project (index 0)
		return this.lastActivity.compareTo(p.getLastActivity());
	}	
	
	@Override
	public void updateLastActivity() {
		this.lastActivity = new Date().getTime()
	}
}
