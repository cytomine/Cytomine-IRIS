package be.cytomine.apps.iris

class Annotation {
	// GRAILS auto variables
	Date dateCreated
	Date lastUpdated

    static constraints = {
    }
	
	Set<Preference> prefs
	static hasMany = [prefs:Preference]
	
	Image image
	static belongsTo = [image:Image]
}
