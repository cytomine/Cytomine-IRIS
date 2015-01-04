package be.cytomine.apps.iris

import java.util.Map;

class Annotation {
	// GRAILS auto variables
	Date dateCreated = new Date()
	Date lastUpdated = new Date()

    static constraints = {
		cmLocation size:0..5000 
    }
	
	// the Cytomine ID of this annotation
	Long cmID = 0L
	// the parent Cytomine project and image ID for this annotation
	Long cmProjectID = 0L
	Long cmImageID = 0L
	
	// the Cytomine user ID of the person who created this annotation
	Long cmCreatorUserID = 0L
	
	// the URL of the image explorer
	String cmImageURL = null
	String cmCropURL = null
	String drawIncreasedAreaURL = null
	String cmSmallCropURL = null
	String cmLocation = null
	Double cmCentroidX = null
	Double cmCentroidY = null
	
	String smallCropURL = null
	
	// the assigned Cytomine term ID by the user // TODO implement multiple terms
	/**
	 * Default term ID is -99 (no term assigned).
	 */
	Long cmTermID = IRISConstants.ANNOTATION_NO_TERM_ASSIGNED
	String cmTermName = null
	Long cmOntology = 0L
	// the Cytomine user ID of the person who assigned this term
	Long cmUserID = 0L
	// a flag determining whether the annotation is already labeled
	boolean alreadyLabeled = false
	
	// the parent image 
	Image image = null
	static belongsTo = [image:Image]
}
