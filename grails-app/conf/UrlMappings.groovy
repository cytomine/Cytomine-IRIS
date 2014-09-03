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
		"/"(view:"/index")
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
		"/api/session.$format"(controller:"session"){
			action = [
				GET: "getSession",
				PUT: "updateSession", // create or overwrite a resource
			]
		}
		
		"/api/session/$sessionID/project/$projectID.$format"(controller:"session"){
			action = [
				PUT: "updateProject" // create or overwrite a resource
				]
		}

		"/api/sessions.$format"(controller:"session"){
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
		 */
		"/api/project/$projectID/availability(.$format)"(controller:"project"){
			action = [GET: "checkAvailability"]
		}
		
		/*
		 *  optional parameters:
		 *  	resolveOntology={true|false}
		 */
		"/api/projects.$format"(controller:"project"){
			action = [GET: "getProjects"]
		}
		
		"/api/project/$projectID/description.$format"(controller:"project"){
			action = [GET: "getProjectDescription"]
		}

	}
}
