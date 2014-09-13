package be.cytomine.apps.iris

import grails.converters.JSON;

import org.codehaus.groovy.grails.web.json.JSONElement;
import org.codehaus.groovy.grails.web.json.JSONObject;

class Project implements Comparable<Project>, Updateable{
	// GRAILS auto variables
	Date dateCreated = new Date()
	Date lastUpdated = new Date()

	static constraints = {
		cmID nullable:false, blank:false
	}

	// class members
	Long lastActivity = new Date().getTime()
	Long cmID = 0L
	String cmName = "defaultProject"
	Boolean cmBlindMode = false
	Long cmOntology = 0L

	// many projects in one session
	Session session = null;

	// DELETE CASCADES
	// in order to get deleted, when the parent session is deleted,
	// we need to declare, that the project belongs to its session
	static belongsTo = [session:Session]

	// a project has many images
	SortedSet<Image> images
	static hasMany = [images:Image]

	// a project has a map of unique preferences
	Map<String, String> prefs = [
		"images.hideCompleted":String.valueOf(false), // hide the completed images in the image overview
	]

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

	/**
	 * Gets the most recent image.
	 * @return the most recent image
	 */
	Image getCurrentImage(){
		try {
			return this.images.last()
		} catch (NoSuchElementException e) {
			return null
		} catch (NullPointerException e) {
			return null
		}
	}

	/**
	 * Updates the project using a JSON object.  
	 * 
	 * @param json a JSONElement object (e.g. parsed from a PUT request payload)
	 * @return the updated project
	 */
	Project updateByJSON(def json){
		// assign all properties from the json to the object
		this.updateLastActivity()

		// setting the preferences does not work directly,
		// resolve each property from the set
		json.prefs.each {
			String[] entry = it.toString().split("=")
			this.getPrefs().put(entry[0],entry[1])
		}
		return this
	}
}
