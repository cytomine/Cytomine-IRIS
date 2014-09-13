package be.cytomine.apps.iris.marshaller

import grails.converters.JSON
import be.cytomine.apps.iris.IRISMarshaller
import be.cytomine.apps.iris.Image

class ImageMarshaller implements IRISMarshaller{

	@Override
	void register() {
		JSON.registerObjectMarshaller(Image) {
			def img = [:]
			img['class'] = it.getClass()
			img['id'] = it.id
			img['lastActivity'] = it.lastActivity
			img['cmID'] = it.cmID
			img['annotations'] = it.annotations
			img['prefs'] = it.prefs
			img['project'] = (it.project==null?null:it.project.id)
			img['goToURL'] = it.goToURL
			img['labeledAnnotations'] = it.labeledAnnotations
			img['numberOfAnnotations'] = it.numberOfAnnotations
			img['userProgress'] = it.userProgress
			img['originalFilename'] = it.originalFilename
			return img
		}
	}
	
}
