package be.cytomine.apps.iris

import java.util.Map;

class Image implements Comparable<Image>{
	// GRAILS auto variables
	Date dateCreated
	Date lastUpdated

    static constraints = {
    }

	// class members	
	Long cmID = 0L
	String cmName = "defaultImage"
	
	Long cmNumberOfAnnotations = 127
	Long labeledAnnotations = 0
	
	
	// many images can belong to one project
	Project project = null
	
	// DELETE CASCADES
	// in order to get deleted, when the parent project is deleted,
	// we need to declare, that the image belongs to its project
	static belongsTo = [project:Project]
	
	
	// an image can have annotations and preferences
	Set<Annotation> annotations
	Map<String, String> prefs = [:]
	static hasMany = [annotations:Annotation]

			
	@Override
	public int compareTo(Image img) {
		// sort the images according to the last activity,
		// such that the project first gets the current image (index 0)
		return this.lastUpdated.compareTo(img.getLastUpdated());
	}
	
	Annotation getCurrentAnnotation(){
		// TODO 
	}
}
