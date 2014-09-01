package be.cytomine.apps.iris

class Preference {
	// GRAILS auto variables
	Date dateCreated
	Date lastUpdated

	static constraints = {
		key size:0..500
		value size:0..5000
	}

	String key
	String value

	// we want to delete the preference if the annotation, image, project or session gets deleted
	static belongsTo = [session:Session,project:Project,image:Image,annotation:Annotation]
}
