package be.cytomine.apps.iris

import org.json.simple.JSONObject

/**
 * This class represents a session on the client side.
 * Necessary properties are injected by the controller before
 * rendering the object to the client.
 * 
 * @author Philipp Kainz
 */
class ClientSession extends JSONObject {
	
	User user = null
	JSONObject currentProject = null
	JSONObject currentImage = null
	JSONObject currentAnnotation = null
	
}
