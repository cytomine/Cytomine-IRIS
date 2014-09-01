package be.cytomine.apps.iris.marshaller

import be.cytomine.apps.iris.Activity;
import be.cytomine.apps.iris.IRISMarshaller;
import grails.converters.JSON;

class ActivityMarshaller implements IRISMarshaller{

	@Override
	void register() {
		JSON.registerObjectMarshaller(Activity) {
			def act = [:]
			act['class'] = it.getClass()
			act['id'] = it.id
			return act
		}
	}
	
}
