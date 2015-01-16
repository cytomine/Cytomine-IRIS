package be.cytomine.apps.iris

/**
 * An IRISUserProjectSettings object is used to synchronize a project with Cytomine.
 *
 * @author Philipp Kainz
 * @since 2.0
 */
class IRISUserProjectSettings {
    // GRAILS auto variables
    Date dateCreated = new Date()
    Date lastUpdated = new Date()

    static constraints = {
        user nullable: false
        cmProjectID nullable: false
    }

    /**
     * The user working with this project.
     */
    IRISUser user
    /**
     * The Cytomine domain identifier.
     */
    Long cmProjectID
    /**
     * The Cytomine ontology ID of the project.
     */
    Long cmOntologyID
    /**
     * The currently opened image ID in this project.
     */
    Long currentCmImageInstanceID = null
    /**
     * Per default, disable the project in IRIS.
     * Configuration has to be checked by the admin, which then enables the project.
     */
    Boolean enabled = false
    /**
     * Per default, show all images in the image list view.
     */
    Boolean hideCompletedImages = false
    /**
     * A time stamp, when the settings got deleted.
     * <br>
     * <b>HINT: This property overrides the 'enabled' property!</b>
     */
    Long deleted = null
}
