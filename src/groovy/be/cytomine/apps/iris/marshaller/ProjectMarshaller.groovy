package be.cytomine.apps.iris.marshaller

import be.cytomine.apps.iris.IRISMarshaller;
import be.cytomine.apps.iris.Project;
import grails.converters.JSON

class ProjectMarshaller implements IRISMarshaller{
	
	@Override
	void register(){
		JSON.registerObjectMarshaller(Project) {
			def prj = [:]
			prj['class'] = it.getClass()
			prj['id'] = it.id
			prj['lastActivity'] = it.lastActivity
			prj['cmID'] = it.cmID
			prj['cmName'] = it.cmName
			prj['cmBlindMode'] = it.cmBlindMode
			prj['cmOntology'] = it.cmOntology
			prj['cmNumberOfImages'] = it.cmNumberOfImages
			prj['currentImage'] = it.currentImage
			prj['prefs'] = it.prefs
			return prj
		}
	}
}
