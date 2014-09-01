package be.cytomine.apps.iris

class Project implements Comparable<Project>, Updateable{
	// GRAILS auto variables
	Date dateCreated
	Date lastUpdated
	
	static constraints = {
		cmID nullable:false, blank:false
	}

	// class members
	Long lastActivity = new Date().getTime()
	Long cmID = 0L
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
	Map<String, String> prefs = [:]
	static hasMany = [images:Image]

	// ###################################################
	// CLASS METHODS
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

	/**
	 * Gets the most recent image.
	 * @return the most recent image
	 */
	Image getCurrentImage(){
		return this.images.last();
	}
}
