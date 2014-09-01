package be.cytomine.apps.iris

class Preference {
	// GRAILS auto variables
	Date dateCreated
	Date lastUpdated

	static constraints = {
		key nullable:false, blank:false
		value blank:true
	}

	String key
	String value

	// we want to delete the preference if the annotation, image, project or session gets deleted
	static belongsTo = [session:Session,project:Project,image:Image,annotation:Annotation]
}
