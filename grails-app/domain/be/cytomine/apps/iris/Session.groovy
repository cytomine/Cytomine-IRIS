package be.cytomine.apps.iris

import java.util.Map;

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
	Map<String, String> prefs = [:]
	static hasMany = [projects:Project]

	// ###################################################
	// CLASS METHODS
	/**
	 * Gets the most recent project.
	 * @return the most recent active project.
	 */
	Project getCurrentProject(){
		try {
			return this.projects.last()
		} catch (NoSuchElementException e) {
			return null
		} catch (NullPointerException e) {
			return null
		}
	}

	/**
	 * Gets the most recent image of the most recent project. Delegate for the image.
	 * @return the most recent active image from the last active project.
	 */
	Image getCurrentImage(){
		try {
			return this.getCurrentProject().getCurrentImage()
		} catch (NoSuchElementException e) {
			return null
		} catch(NullPointerException e){
			return null
		}
	}
}
