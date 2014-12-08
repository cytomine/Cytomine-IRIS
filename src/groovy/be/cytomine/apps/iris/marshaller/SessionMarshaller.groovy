package be.cytomine.apps.iris.marshaller

import be.cytomine.apps.iris.IRISMarshaller;
import be.cytomine.apps.iris.Session;
import grails.converters.JSON;

class SessionMarshaller implements IRISMarshaller{

	@Override
	void register() {
		JSON.registerObjectMarshaller(Session) {
			def sess = [:]
			sess['class'] = it.getClass()
			sess['id'] = it.id
			sess['lastActivity'] = it.lastActivity
			sess['user'] = it.user
			sess['currentProject'] = it.getCurrentProject()
			sess['currentImage'] = it.getCurrentImage()
			//sess['currentCmAnnotationID'] = it.getCurrentImage()==null?null:it.getCurrentImage().getCurrentCmAnnotationID()
			sess['prefs'] = it.prefs
			return sess
		}
	}
}
