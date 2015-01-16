package be.cytomine.apps.iris

/**
 * This domain model represents an IRIS
 * user and has a reference to the current IRIS session.
 *
 * @author Philipp Kainz
 * @since 0.3
 */
class IRISUser {
    // GRAILS auto variables
    Date dateCreated = new Date()
    Date lastUpdated = new Date()

    static constraints = {
        cmID nullable: false, unique: true
        cmUserName nullable: false, unique: true
        cmFirstName nullable: false
        cmLastName nullable: false
        cmEmail nullable: false, email: true
    }

    /*
     * Each user has one session.
     */
    IRISUserSession session = null
    static hasOne = [session:IRISUserSession] // this puts a foreign key in the session table

    // domain class properties (mapped from the Cytomine core)
    /**
     * The Cytomine domain identifier of this user.
     */
    Long cmID
    /**
     * The Cytomine user name.
     */
    String cmUserName
    /**
     * The last name of the user.
     */
    String cmLastName
    /**
     * The first name of the user.
     */
    String cmFirstName
    /**
     * The API public key of this user.
     */
    String cmPublicKey
    /**
     * The API private key of this user.
     */
    String cmPrivateKey
    /**
     * The email address of this user.
     */
    String cmEmail
    /**
     * A flag, whether the password on the Cytomine account has expired.
     */
    Boolean cmPasswordExpired
    /**
     * The date, the user was created on Cytomine.
     */
    Long cmCreated
    /**
     * The date the user was updated on Cytomine.
     */
    Long cmUpdated
    /**
     * The date the user has been deleted on Cytomine.
     */
    Long cmDeleted = null
    /**
     * A flag, whether this IRISUser should be synchronized with the Cytomine user instance.
     * This flag also specifies, whether this user's progress should be synced by the background sync job.
     */
    Boolean synchronize = false
}
