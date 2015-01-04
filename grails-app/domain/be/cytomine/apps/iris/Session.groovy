package be.cytomine.apps.iris

import java.util.Map;

import org.json.simple.JSONObject;

import com.sun.org.apache.bcel.internal.generic.RETURN;

class Session implements Updateable{
	// GRAILS auto variables
	Date dateCreated = new Date()
	Date lastUpdated = new Date()

	static constraints = {
		user nullable:true
	}
	/**
	 * Date of last activity.
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
	
	// the last opened project instance 
	Project currentProject

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
	
	@Override
	public void updateLastActivity() {
		this.lastActivity = new Date().getTime()
	}
}
