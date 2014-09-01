package be.cytomine.apps.iris.marshaller

import be.cytomine.apps.iris.IRISMarshaller;
import be.cytomine.apps.iris.User;
import grails.converters.JSON

class UserMarshaller implements IRISMarshaller {

	@Override
	void register() {
		JSON.registerObjectMarshaller(User) {
			def user = [:]
			user['class'] = it.getClass()
			user['id'] = it.id
			user['cmID'] = it.cmID
			user['prefs'] = it.prefs
			return user
		}
	}
}
