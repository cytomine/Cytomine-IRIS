package be.cytomine.apps.iris

import java.util.Map;

class Image implements Comparable<Image>, Updateable{
	// GRAILS auto variables
	Date dateCreated = new Date()
	Date lastUpdated = new Date()

    static constraints = {
		olTileServerURL size:0..1000
    }

	// class members	
	Long lastActivity = new Date().getTime()
	Long cmID = 0L
	String originalFilename = "defaultImage"
	String goToURL = null
	String olTileServerURL = null
	
	Long numberOfAnnotations = 0L
	Long labeledAnnotations = 0L
	Long userProgress = 0L
		
	// many images can belong to one project
	Project project = null
	
	// DELETE CASCADES
	// in order to get deleted, when the parent project is deleted,
	// we need to declare, that the image belongs to its project
	static belongsTo = [project:Project]
	
	// an image can have preferences
	Map<String, String> prefs = [:]
	
	Long currentCmAnnotationID = 0L
	
	// each image may have up to 3 annotations (current, previous, next)
	// which will be updated accordingly 
	Map annotations = [:]
	static hasMany = [annotations:Annotation]
	
	public void setCurrentAnnotation(Annotation currAnn){
		this.annotations.put("currentAnnotation", currAnn)
	}
	
	public void setPreviousAnnotation(Annotation prevAnn){
		this.annotations.put("previousAnnotation", prevAnn)
	}
	
	public void setNextAnnotation(Annotation nextAnn){
		this.annotations.put("nextAnnotation", nextAnn)
	}
	
	public Annotation getCurrentAnnotation(){
		return this.annotations.get("currentAnnotation")
	}
	
	public Annotation getPreviousAnnotation(){
		return this.annotations.get("previousAnnotation")
	}
	
	public Annotation getNextAnnotation(){
		return this.annotations.get("nextAnnotation")
	}
	
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
	
	/**
	 * Updates the image using a JSON object.
	 *
	 * @param json a JSONElement object (e.g. parsed from a PUT request payload)
	 * @return the updated project
	 */
	Image updateByJSON(def json){
		this.updateLastActivity()
		
		json.prefs.each {
			String[] entry = it.toString().split("=")
			this.getPrefs().put(entry[0],entry[1])
		}
		return this
	}
}
