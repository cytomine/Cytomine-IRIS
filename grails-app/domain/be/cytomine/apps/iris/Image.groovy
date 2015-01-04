package be.cytomine.apps.iris

import java.util.Map;

class Image implements Comparable<Image>, Updateable{
	// GRAILS auto variables
	Date dateCreated = new Date()
	Date lastUpdated = new Date()

    static constraints = {
		olTileServerURL size:0..1000
		width min:0L
		height min:0L 
    }

	// class members	
	Long lastActivity = new Date().getTime()
	Long cmID = 0L
	String originalFilename = "defaultImage"
	String goToURL = null
	String olTileServerURL = null
	String macroURL = null
	
	Long width = 0
	Long height = 0
	
	Long numberOfAnnotations = 0L
	Long labeledAnnotations = 0L
	Long userProgress = 0L
		
	// many images can belong to one project
	Project project = null
	
	// DELETE CASCADES
	// in order to get deleted, when the parent project is deleted,
	// we need to declare, that the image belongs to its project
	static belongsTo = [project:Project]
	
	// filter flag for the labeling view
	Boolean hideCompletedAnnotations = false

	/**
	 * The cytomine ID of the currently active annotation
	 */
	Long currentCmAnnotationID = 0L
	
	@Override
	public int compareTo(Image img) {
		// sort the images according to the last activity,
		// such that the project first gets the current image (index 0)
		return this.lastActivity.compareTo(img.getLastActivity());
	}
	
	@Override
	public void updateLastActivity() {
		this.lastActivity = new Date().getTime()		
	}
}
