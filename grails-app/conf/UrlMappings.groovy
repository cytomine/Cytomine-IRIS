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
		"/"(view:"/iris") // TODO REDIRECT TO /iris IN PRODUCTION
		"/index"(view:"/index")
		"/iris"(view: "/iris")
		"500"(view:"/error")

		/* Administrator Interface */
		"/admin"(view:"admin/index")

		/*
		 * ###########################
		 * Action mapping (to controllers)
		 * 
		 * mainController
		 * - handles information about the IRIS application
		 */
		"/api/welcome(.$format)"(controller:"main"){
			action = [GET: "welcome"]
		}

		"/api/appName(.$format)"(controller:"main"){
			action = [GET: "appName"]
		}

		"/api/appVersion(.$format)"(controller:"main"){
			action = [GET: "appVersion"]
		}

		"/api/cytomineHost(.$format)"(controller:"main"){
			action = [GET: "hostAddress"]
		}

		"/api/cytomineWeb(.$format)"(controller:"main"){
			action = [GET: "webAddress"]
		}

		"/api/admin/appInfo(.$format)"(controller:"main"){
			action = [GET: "applicationInfo"]
		}


		/*
		 * cytomineController
		 * - handles the general communication to the core via the Java client
		 */
		/*
		 *  optional parameters: 
		 *  	flat={true|false}
		 */
		"/api/ontology/$cmOntologyID(.$format)"(controller:"cytomine"){
			action = [GET: "getOntology"]
		}

		"/api/user/publicKey/$pubKey(.$format)"(controller:"cytomine"){
			action = [GET: "getUserByPublicKey"]
		}

		"/api/user/current(.$format)"(controller:"cytomine"){
			action = [GET: "getCurrentIRISUser"]
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
		 * Optional Parameters:
		 * 		maxWidth
		 */
		//http://beta.cytomine.be/api/abstractimage/140850906/associated/macro.png?maxWidth=512
		"/api/abstractimage/$abstractImageID/associated/macro(.$format)?"(controller:"cytomine"){
			action = [GET: "getMacroImage"]
		}
		
		/*
		 * Optional Parameters:
		 * 		maxSize
		 */
		//http://beta.cytomine.be/api/userannotation/151650613/crop.png?maxSize=256
		"/api/userannotation/$annotationID/crop(.$format)?"(controller:"cytomine"){
			action = [GET: "getCropImage"]
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
		"/api/project/$cmProjectID/annotations(.$format)"(controller:"annotation"){
			action = [GET: "getAnnotationsByUser"]
		}
		
		/*
		 * Get a 3-tuple of annotations for an image in a project
		 * Optional parameters
		 * 		currentAnnotation = { $cmAnnID || null }
		 * 			if null, return first and subsequent item, if any, previousAnnotation = null
		 * 		hideCompleted = { true || false }
		 */
		"/api/project/$cmProjectID/image/$cmImageID/annotations/tuple(.$format)"(controller:"annotation"){
			action = [GET: "getAnnotation3Tuple"]
		}

		/*
		 * Assign a unique term		
		 */
		"/api/project/$cmProjectID/image/$cmImageID/annotation/$cmAnnID/term/$cmTermID/clearBefore(.$format)"(controller:"annotation"){
			action = [POST: "setUniqueTerm"]
		}
		
		/*
		 * Adds another term or deletes it
		 */
		"/api/project/$cmProjectID/image/$cmImageID/annotation/$cmAnnID/term/$cmTermID(.$format)"(controller:"annotation"){
			action = [DELETE: "deleteTerm"]
		}
		
		/*
		 * Removes all terms of a user from the annotation
		 */
		"/api/project/$cmProjectID/image/$cmImageID/annotation/$cmAnnID/terms(.$format)"(controller:"annotation"){
			action = [DELETE: "deleteAllTerms"]
		}
		
		/*
		 * SessionController
		 */
		// user identification is done by public key
		"/api/session(.$format)"(controller:"session"){
			action = [
				GET: "getSession",
				//DELETE: "deleteSession"
			]
		}

		/*
		 * Resumes a session at the images of the last opened project.
		 */
		"/api/session/resume/images(.$format)"(controller:"session"){
			action = [GET: "resumeSessionAtImages"]
		}

		/*
		 * Resumes a session at the gallery of the last opened project.
		 */
		"/api/session/resume/gallery(.$format)"(controller:"session"){
			action = [GET: "resumeSessionAtGallery"]
		}

		/*
		 * Resumes a session at the last known annotation in the last opened
		 * image of the last opened project.
		 */
		"/api/session/resume/labeling(.$format)"(controller:"session"){
			action = [GET: "resumeSessionAtLabeling"]
		}


		/*
		 * Gets a single image.
		 */
		"/api/project/$cmProjectID/image/$cmImageID(.$format)"(controller:"session"){
			action = [GET: "getImage"]
		}

		/**
		 * List the images from a specific project
		 */
		"/api/project/$cmProjectID/images(.$format)"(controller:"session"){
			action = [GET: "getImages"]
		}


		"/api/project/$cmProjectID/image/$cmImageID/progress(.$format)"(controller:"session"){
			action = [
				GET: "labelingProgress" // get the current labeling progress of an image
				]
		}
				
		"/api/session/dev"(controller:"session"){
			action = [GET: "dev"]
		}

		"/api/session/dev2"(controller:"session"){
			action = [GET: "dev2"]
		}
		
		/*
		 * ProjectController
		 * Retrieves information on and instances of Cytomine projects.
		 */
		/*
		 * Gets the projects for a user. 
		 *  optional parameters:
		 *  	resolveOntology={true|false}
		 */
		"/api/projects(.$format)"(controller:"project"){
			action = [GET: "getAll"]
		}

		/**
		 * Open a project.
		 */
		"/api/project/$cmProjectID(.$format)"(controller:"project"){
			action = [
					GET: "openProject" // create or overwrite the "current" project in the session
			]
		}

		/**
		 * Update the project settings.
		 */
		"/api/project/$cmProjectID/settings(.$format)"(controller:"project"){
			action = [
					PUT: "updateProjectSettings" // updates specific project settings
			]
		}

		/*
		 * Gets the description of a project.
		 * 		$projectID = Cytomine project ID 
		 */
		"/api/project/$cmProjectID/description(.$format)"(controller:"project"){
			action = [GET: "getDescription"]
		}

		/*
		 * Gets the description of a project.
		 * 		$projectID = Cytomine project ID
		 */
		"/api/project/$cmProjectID/ontology(.$format)"(controller:"project"){
			action = [GET: "getOntologyByProject"]
		}

		/*
		 * Gets the users of a project.
		 * 		$projectID = Cytomine project ID
		 */
		"/api/project/$cmProjectID/users(.$format)"(controller:"project"){
			action = [GET: "getUsersByProject"]
		}

		/**
		 * ADMIN
		 *
		 * Synchronize all users' progresses at once. This explicit service call equals
		 * the nightly auto-synchronization.
		 */
		"/api/admin/synchronize(.$format)"(controller:"admin"){
			action = [POST: "synchronizeAllUserProgress"]
		}

		/**
		 * Synchronize all users' progresses for a specific project at once.
		 */
		"/api/admin/project/$cmProjectID/synchronize(.$format)"(controller:"admin"){
			action = [POST: "synchronizeAllUserProjectProgress"]
		}

		/**
		 * User per image synchronization.
		 * If optional parameter 'images' is empty/null, all images of the project will be synced
		 */
		"/api/admin/project/$cmProjectID/user/$cmUserID/synchronize(.$format)"(controller:"admin"){
			action = [POST: "synchronizeUserProjectProgress"]
		}

		/**
		 * Authorize a user for specific roles/tasks.
		 * Additional parameter must be set, e.g. irisCoordinator={true|false}
		 */
		"/api/admin/project/$cmProjectID/user/$cmUserID/authorize(.$format)"(controller:"admin"){
			action = [GET: "authorizeCoordinator"]
		}

		"/api/dev"(controller:"admin"){
			action = [GET: "dev"]
		}

		/**
		 * STATISTICS
		 *
		 * A compressed form of agreement visualization.
		 */
//		"/api/stats/$cmProjectID/agreements(.$format)"(controller:"projectStatistics"){
//			action = [GET: "majorityAgreements"]
//		}

		/**
		 * Annotation statistics: agreement list
		 */
		"/api/stats/$cmProjectID/agreements/list(.$format)"(controller:"projectStatistics"){
			action = [GET: "agreementsList"]
		}

		/**
		 * User statistics
		 */
		"/api/stats/$cmProjectID/userstatistics(.$format)"(controller:"projectStatistics"){
			action = [GET: "userStatistics"]
		}

		/**
		 * One-vs-all statistics
		 */
//		"/api/stats/$cmProjectID/ovastatistics(.$format)"(controller:"projectStatistics"){
//			action = [GET: "userVsAll"]
//		}


		/**
		 * SETTINGS
		 *
		 * User settings list for a specific project.
		 */
		"/api/settings/$cmProjectID/users(.$format)"(controller:"projectSettings"){
			action = [GET: "userProjectSettingsList"]
		}

		/**
		 * Enable/Disable specific images for a user.
		 * 		payload contains the old and new value and the settings id of the IRISUserImageSettings
		 */
		"/api/settings/user/$cmUserID/project/$cmProjectID/image/$cmImageID/access(.$format)"(controller:"projectSettings"){
			action = [POST: "imageAccess"]
		}

		/**
		 * Get the full image list without filtering the disabled images for an user.
		 */
		"/api/settings/user/$cmUserID/project/$cmProjectID/images(.$format)"(controller:"projectSettings"){
			action = [GET: "userImageList"]
		}

		/**
		 * Enable/Disable specific projects for a user.
		 * 		payload contains the old and new value and the settings id of the IRISUserProjectSettings
		 */
		"/api/settings/user/$cmUserID/project/$cmProjectID/access(.$format)"(controller:"projectSettings"){
			action = [POST: "projectAccess"]
		}

		/**
		 * Enable/Disable auto-synchronization for a user.
		 * 		payload contains the old and new value and the settings id of the IRISUserProjectSettings
		 */
		"/api/settings/user/$cmUserID/project/$cmProjectID/autosync(.$format)"(controller:"projectSettings"){
			action = [POST: "userAutoSync"]
		}

		/**
		 * Request to become a coordinator for the project.
		 * 		payload contains the custom message to the administrator
		 */
		"/api/settings/user/$cmUserID/project/$cmProjectID/coordinator/request(.$format)"(controller:"projectSettings"){
			action = [POST: "requestProjectCoordinator"]
		}
	}
}
