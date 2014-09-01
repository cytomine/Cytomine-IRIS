package be.cytomine.apps.iris.marshaller

import be.cytomine.apps.iris.IRISMarshaller;
import be.cytomine.apps.iris.Preference;
import grails.converters.JSON

class PreferenceMarshaller implements IRISMarshaller{

	@Override
	void register(){
		JSON.registerObjectMarshaller(Preference) {
			def pref = [:]
			pref['class'] = it.getClass()
			pref['id'] = it.id
			pref['key'] = it.key
			pref['value'] = it.value
			return pref
		}
	}
}
