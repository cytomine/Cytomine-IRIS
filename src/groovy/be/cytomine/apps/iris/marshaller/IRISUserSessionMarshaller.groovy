package be.cytomine.apps.iris.marshaller

import be.cytomine.apps.iris.IRISMarshaller;
import be.cytomine.apps.iris.IRISUserSession;
import grails.converters.JSON;

class IRISUserSessionMarshaller implements IRISMarshaller{

	@Override
	void register() {
		JSON.registerObjectMarshaller(IRISUserSession) {
			def sess = [:]
			sess['class'] = it.getClass()
			sess['id'] = it.id

			sess['user'] = it.user
			// TODO marshall the current project
//			sess['currentProject'] = it.getCurrentProject()
			return sess
		}
	}
}
