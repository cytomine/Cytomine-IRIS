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

		"/api/abstractimage/$abstractImageID/imageinstance/$imageInstanceID/imageservers.json"(controller:"cytomine"){
			action = [GET: "getImageServerURLs"]
		}
		
		/*
		 * Optional Parameters: 
		 * 		zoomify
		 */
		"/image/tile"(controller:"cytomine"){
			action = [GET: "getTile"]
		}
		
		/*
		 * annotationController
		 * - communicates annotation CRUD operations to the core via the Java client
		 */
		/*
		 * Get all annotations for a project.
		 * Optional parameters
		 * 		image = [imageID#1,imageID#2,...]
		 */
		"/api/session/$sessionID/project/$cmProjectID/annotations.$format"(controller:"annotation"){
			action = [GET: "getAnnotationsByUser"]
		}
		
		/*
		 * Get a 3-tuple of annotations for an image in a project
		 * Optional parameters
		 * 		currentAnnotation = { $cmAnnID || null }
		 * 			if null, return first and subsequent item, if any, previousAnnotation = null
		 * 		hideCompleted = { true || false }
		 */
		"/api/session/$sessionID/project/$cmProjectID/image/$cmImageID/annotations/tuple.$format"(controller:"annotation"){
			action = [GET: "getAnnotation3Tuple"]
		}

		/*
		 * Assign a unique term		
		 */
		"/api/session/$sessionID/project/$cmProjectID/image/$cmImageID/annotation/$cmAnnID/term/$cmTermID/clearBefore.$format"(controller:"annotation"){
			action = [POST: "setUniqueTerm"]
		}
		
		/*
		 * Adds another term or deletes it
		 */
		"/api/session/$sessionID/project/$cmProjectID/image/$cmImageID/annotation/$cmAnnID/term/$cmTermID.$format"(controller:"annotation"){
			action = [POST: "addTerm",
					DELETE: "deleteTerm"]
		}
		
		/*
		 * Removes all terms of a user from the annotation
		 */
		"/api/session/$sessionID/project/$cmProjectID/image/$cmImageID/annotation/$cmAnnID/terms.$format"(controller:"annotation"){
			action = [DELETE: "deleteAllTerms"]
		}
		
		/*
		 * Touches the annotation (UNUSED)
		 */
//		"/api/session/$sessionID/project/$cmProjectID/image/$cmImageID/annotation/$cmAnnID/touch"(controller:"annotation"){
//			action = [POST: "touchAnnotation"]
//		}

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
				POST: "touchProject" // create or overwrite the "current" project in the session
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
		
		"/api/project/$cmProjectID/image/$cmImageID.$format"(controller:"session"){
			action = [GET: "getImage"]
		}
		
		// optional parameters (progress = true | false)
		"/api/project/$projectID/images.$format"(controller:"session"){
			action = [GET: "getImages"]
		}
		
		"/api/session/$sessionID/project/$cmProjectID/image/$cmImageID/touch"(controller:"session"){
			action = [
				POST: "touchImage" // create or overwrite the "current" image in the session
				]
		}
		
		"/api/session/$sessionID/project/$cmProjectID/image/$cmImageID/progress"(controller:"session"){
			action = [
				GET: "labelingProgress" // get the labeling progress of an image
				]
		}
		
		"/api/sessions(.$format)"(controller:"session"){
			action = [GET: "getAll"]
		}
		
		"/api/deleteproject.json"(controller:"session"){
			action = [DELETE: "delProj"]
		}
		
		"/api/session/dev.json"(controller:"session"){
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
