package be.cytomine.apps.iris

import org.json.simple.JSONObject;

import com.sun.org.apache.bcel.internal.generic.RETURN;

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
	Collection prefs
	static hasMany = [projects:Project,prefs:Preference]
	
	// TODO transient objects which can be injected by controllers
//	JSONObject currentProject
//	JSONObject currentImage
//	JSONObject currentAnnotation
//	static transients = ['currentProject','currentImage','currentAnnotation']

	// ###################################################
	// CLASS METHODS
	/**
	 * Gets the most recent project.
	 * @return the most recent active project.
	 */
	Project getCurrentIRISProject(){
		return this.projects.last();
	}

	/**
	 * Gets the most recent image of the most recent project. Delegate for the image.
	 * @return the most recent active image from the last active project.
	 */
	Image getCurrentIRISImage(){
		return this.getCurrentIRISProject().getCurrentIRISImage();
	}
}
