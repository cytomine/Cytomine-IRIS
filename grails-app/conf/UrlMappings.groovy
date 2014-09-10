import java.awt.Desktop.Action;

class UrlMappings {

	static mappings = {
		"/$controller/$action?/$id?(.$format)?"{ 
			constraints { // apply constraints here
			
			}
		 }

		/*
		 * ###########################
		 * Global redirects (to views)
		 */
		"/"(view:"/iris")
		"/iris"(view:"/iris")
		"500"(view:'/error')


		/*
		 * ###########################
		 * Action mapping (to controllers)
		 * 
		 * 
		 * 
		 * mainController
		 * - handles information about the IRIS application
		 */
		"/api/welcome.$format"(controller:"main"){
			action = [GET: "welcome"]
		}

		"/api/appName.$format"(controller:"main"){
			action = [GET: "appName"]
		}

		"/api/appVersion.$format"(controller:"main"){
			action = [GET: "appVersion"]
		}

		"/api/cytomineHost.$format"(controller:"main"){
			action = [GET: "hostAddress"]
		}

		"/api/cytomineWeb.$format"(controller:"main"){
			action = [GET: "webAddress"]
		}


		/*
		 * cytomineController
		 * - handles the general communication to the core via the Java client
		 */
		"/api/project/$projectID/images.$format"(controller:"cytomine"){
			action = [GET: "getImages"]
		}

		/*
		 *  optional parameters: 
		 *  	flat={true|false}
		 */
		"/api/ontology/$ontologyID.$format"(controller:"cytomine"){
			action = [GET: "getOntology"]
		}

		"/api/user/publicKey/$pubKey.$format"(controller:"cytomine"){
			action = [GET: "getUserByPublicKey"]
		}

		"/api/abstractimage/$abstractImageID/imageservers.json"(controller:"cytomine"){
			action = [GET: "getImageServerURLs"]
		}

		/*
		 * annotationController
		 * - communicates annotation CRUD operations to the core via the Java client
		 */
		"/api/annotation/$annID.$format"(controller:"annotation"){
			action = [GET: "getAnnotation"]
		}

		"/api/image/$imageID/annotations.$format"(controller:"annotation"){
			action = [GET: "getAnnotations"]
		}


		/*
		 * SessionController
		 */
		// user identification is done by public key
		"/api/session(.$format)"(controller:"session"){
			action = [
				GET: "getSession",
				PUT: "updateSession", // create or overwrite a resource
				DELETE: "deleteSession"
			]
		}
		
		"/api/session/$sessionID/project/$cmProjectID/touch"(controller:"session"){
			action = [
				POST: "touchProject" // create or overwrite the project in the session
				]
		}
		
		/*
		 * Optional parameters for PUT request is 
		 * 		class={cytomine|iris}
		 */
		"/api/session/$sessionID/project/$cmProjectID(.$format)"(controller:"session"){
			action = [
				GET: "getProject", // gets an IRIS project from a session
				PUT: "updateProject" // update the project in the session using the params from the payload
				]
		}
		
		"/api/sessions(.$format)"(controller:"session"){
			action = [GET: "getAll"]
		}
		
		"/api/deleteproject.json"(controller:"session"){
			action = [DELETE: "delProj"]
		}
		
		"/api/dev.json"(controller:"session"){
			action = [GET: "dev"]
		}
		
		
		/*
		 * ProjectController
		 * Retrieves information on and instances of Cytomine projects.
		 */
		
		/*
		 *  checks the availability of a project
		 *  	$projectID = Cytomine project ID
		 */ 
		"/api/project/$projectID/availability(.$format)"(controller:"project"){
			action = [GET: "checkAvailability"]
		}
		
		/*
		 * Gets the projects for a user. 
		 *  optional parameters:
		 *  	resolveOntology={true|false}
		 */
		"/api/projects(.$format)"(controller:"project"){
			action = [GET: "getProjects"]
		}
		
		/*
		 * Gets the description of a project.
		 * 		$projectID = Cytomine project ID 
		 */
		"/api/project/$projectID/description(.$format)"(controller:"project"){
			action = [GET: "getProjectDescription"]
		}

	}
}
