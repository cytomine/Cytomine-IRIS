package be.cytomine.apps.iris

import java.util.Map;

class Annotation {
	// GRAILS auto variables
	Date dateCreated = new Date()
	Date lastUpdated = new Date()

    static constraints = {
    }
	
	// the Cytomine ID of this annotation
	long cmID = 0L
	// the parent Cytomine project and image ID for this annotation
	long cmProjectID = 0L
	long cmImageID = 0L
	
	// the Cytomine user ID of the person who created this annotation
	long cmCreatorUserID = 0L
	
	// the URL of the image explorer
	String cmImageURL = null
	String cmSmallCropURL = null
	
	// the assigned Cytomine term ID by the user // TODO implement multiple terms
	long cmTermID = 0L
	String cmTermName = null
	long cmOntology = 0L
	// the Cytomine user ID of the person who assigned this term
	long cmUserID = 0L
	
	// a map of preferences
	Map<String, String> prefs = [:]
	
	// the parent image 
	Image image = null
	static belongsTo = [image:Image]
}
