package be.cytomine.apps.iris

class Session {
	// GRAILS auto variables
	Date dateCreated
	Date lastUpdated

	static constraints = {
		lastActivity nullable:true, blank:true
		user nullable:true
	}
	/**
	 * The time of the last activity.
	 */
	Long lastActivity = new Date().getTime()
	/**
	 * The user of this session.
	 */
	User user
	/**
	 * 	The list of all projects for this session.
	 */
	// saving a session cascades saving the projects, but not deleting them, when the session is deleted!
	// in order to delete a project, we need to add 'belongsTo' to the project!
	
	// proper method to add a project is Session.addToProjects(Project p)
	SortedSet<Project> projects
	static hasMany = [projects:Project]
	
	/**
	 * Gets the most recent project.
	 * @return
	 */
	Project getCurrentProject(){
		return projects.last();
	}
}
