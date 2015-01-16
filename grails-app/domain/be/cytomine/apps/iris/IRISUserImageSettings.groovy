package be.cytomine.apps.iris

/**
 * An IRISUserImageSettings object is used to synchronize the user progress with Cytomine.
 *
 * @author Philipp Kainz
 * @since 2.0
 */
class IRISUserImageSettings {
    // GRAILS auto variables
    Date dateCreated = new Date()
    Date lastUpdated = new Date()

    static constraints = {
        user nullable: false
        cmImageInstanceID nullable: false
    }

    /**
     * The user working with this image instance.
     */
    IRISUser user
    /**
     * The Cytomine domain model identifier.
     */
    Long cmImageInstanceID
    /**
     * The Cytomine domain model identifier of the project the image belongs to.
     */
    Long cmProjectID
    /**
     * The current annotation in this image.
     */
    Long currentCmAnnotationID = null
    /**
     * Whether the image is enabled or not, default is true.
     */
    Boolean enabled = true
    /**
     * The absolute number of annotations labeled by the user.
     */
    Long labeledAnnotations = 0L
    /**
     * The total number of annotations in this image.
     */
    Long numberOfAnnotations = 0L
    /**
     * The user progress
     */
    Integer progress = 0
    /**
     * A flag whether the user hides already labeled annotations in the navigation.
     */
    Boolean hideCompletedAnnotations = false

    /**
     * The timestamp when the image settings have been deleted, or NULL, if it is still active.
     */
    Long deleted = null

    /**
     * Convenient wrapper method to compute the current progress.
     *
     * @return progress in Integer percent
     */
    Integer computeProgress(){
        this.progress = (this.numberOfAnnotations==0L?0:(int) (this.labeledAnnotations*100/this.numberOfAnnotations))
    }
}
