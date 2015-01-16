package be.cytomine.apps.iris

/**
 * An IRISUserSession reflects the current progress of a user in the application.
 *
 * @author Philipp Kainz
 * @since 0.3
 */
class IRISUserSession{
	// GRAILS auto variables
	Date dateCreated = new Date()
	Date lastUpdated = new Date()

	static constraints = {
		user nullable: false
	}

	/**
	 * The user of this session.
	 */
	IRISUser user
	/**
 	 * The most recently opened project instance.
	 */
	Long currentCmProjectID = null
}
