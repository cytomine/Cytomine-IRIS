package be.cytomine.apps.iris

import java.util.Map;

/**
 * This domain model represents a single Cytomine 
 * user and has a reference to the current IRIS session.
 * 
 * @author Philipp Kainz
 */
class User {
	// GRAILS auto variables
	Date dateCreated
	Date lastUpdated

	static constraints = {
		session nullable:true, unique:true
		cmID nullable:false, unique:true
		cmUserName nullable:false, unique:true
		cmFirstName nullable:false
		cmLastName nullable:false
		cmPublicKey nullable:true
		cmPrivateKey nullable:true
		cmEmail nullable:true, email:true, blank:true
		cmClass nullable:true, blank:true
		cmPasswordExpired nullable:true
		cmDeleted nullable:true
		cmAdminByNow nullable:true
		cmAlgo nullable:true
		cmAdmin nullable:true
		cmGuestByNow nullable:true
		cmIsSwitched nullable:true
		cmGuest nullable:true
		cmUser nullable:true
		cmUserByNow nullable:true
	}

	/*
	 * Each User has one session.
	 */
	Session session = null
	static hasOne = [session:Session] // this puts a foreign key in the session table
	
	// domain class properties (mapped from the Cytomine core)
	Long cmID
	String cmUserName
	String cmLastName
	String cmFirstName
	String cmPublicKey
	String cmPrivateKey
	String cmEmail
	String cmClass
	Boolean cmPasswordExpired = false
	Boolean cmDeleted = false
	Boolean cmAdminByNow = false
	Boolean cmAlgo = false
	Boolean cmAdmin
	Boolean cmGuestByNow
	Boolean cmIsSwitched
	Boolean cmGuest
	Boolean cmUser = true
	Boolean cmUserByNow = true
	
	Map<String, String> prefs = [:]
}
