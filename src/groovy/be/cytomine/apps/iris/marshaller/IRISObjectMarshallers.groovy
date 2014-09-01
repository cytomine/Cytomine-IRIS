package be.cytomine.apps.iris.marshaller

import be.cytomine.apps.iris.IRISMarshaller;

/**
 * This class automatically registers all custom object marshallers 
 * for the IRIS application. The declaration of the <code>irisMarshallers</code>
 * is given in <code>conf/spring/resources.groovy</code>
 * @author Philipp Kainz
 *
 */
class IRISObjectMarshallers {

	List<IRISMarshaller> irisMarshallers = []
	
	// register all custom iris marshallers
	def register(){
		irisMarshallers.each {
			it.register()
		}
	}
	
}
