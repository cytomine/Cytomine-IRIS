package be.cytomine.apps.iris

/**
 * An IRISUserAnnotationTermSettings object is used to synchronize the annotations with Cytomine.
 *
 * @author Philipp Kainz
 * @since 2.0
 */
class IRISUserAnnotationTermSettings {
    // GRAILS auto variables
    Date dateCreated = new Date()
    Date lastUpdated = new Date()

    static constraints = {
        user nullable: false
        cmAnnotationID nullable: false
    }

    /**
     * The user working with this annotation.
     */
    IRISUser user
    /**
     * The Cytomine project ID of this annotation.
     */
    Long cmProjectID
    /**
     * The Cytomine image instance ID this annotation belongs to.
     */
    Long cmImageInstanceID
    /**
     * The Cytomine domain identifier.
     */
    Long cmAnnotationID
    /**
     * The Cytomine ontology domain identifier.
     */
    Long cmOntologyID
    /**
     * The Cytomine term domain identifier.
     */
    Long cmTermID
}
