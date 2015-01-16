package be.cytomine.apps.iris

/**
 * Domain class holding per user information
 * on the synchronization status of a project.
 *
 * @author Philipp Kainz
 * @since 2.0
 */
class ImageSyncStatus {
    // GRAILS auto variables
    Date dateCreated = new Date()
    Date lastUpdated = new Date()

    static constraints = {
    }

    /**
     * The user this status belongs to.
     */
    IRISUser user

    /**
     * The project ID of the image being synchronized.
     */
    Long cmProjectID

    /**
     * The image ID being synchronized.
     */
    Long cmImageInstanceID

    /**
     * The progress in Integer percent.
     */
    Integer progress = null
}
