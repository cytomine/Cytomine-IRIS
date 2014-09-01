package be.cytomine.apps.iris.marshaller

import be.cytomine.apps.iris.Annotation;
import be.cytomine.apps.iris.IRISMarshaller;
import grails.converters.JSON

class AnnotationMarshaller implements IRISMarshaller {

	@Override
	void register() {
		JSON.registerObjectMarshaller(Annotation) {
			def ann = [:]
			ann['class'] = it.getClass()
			ann['id'] = it.id
			// TODO marshal the image server URLs
			return ann
		}
	}
}
