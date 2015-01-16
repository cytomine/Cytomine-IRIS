package be.cytomine.apps.iris.marshaller

import be.cytomine.apps.iris.IRISMarshaller;
import be.cytomine.apps.iris.IRISUser;
import grails.converters.JSON

class IRISUserMarshaller implements IRISMarshaller {

	@Override
	void register() {
		JSON.registerObjectMarshaller(IRISUser) {
			def user = [:]
			user['class'] = it.getClass()
			user['id'] = it.id
			user['cmID'] = it.cmID
			user['cmUserName'] = it.cmUsername
			user['cmLastName'] = it.cmLastName
			user['cmFirstName'] = it.cmFirstName
			user['cmPublicKey'] = it.cmPublicKey
			user['cmPrivateKey'] = it.cmPrivateKey
			user['cmEmail'] = it.cmEmail

			return user
		}
	}
}
