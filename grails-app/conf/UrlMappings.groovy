import java.awt.Desktop.Action;

class UrlMappings {

	static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

		/*
		 * ###########################
		 * Global redirects (to views)
		 */
        "/"(view:"/iris")
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
		 * - handles the communication to the core via the Java client
		 */
		"/api/projects.$format"(controller:"cytomine"){
			action = [GET: "getProjects"]
		}
		
		"/api/project/$idProject/images.$format"(controller:"cytomine"){
			action = [GET: "getImages"]
		}
		
		"/api/project/$idProject/description.$format"(controller:"cytomine"){
			action = [GET: "getProjectDescription"]
		}
		
	}
}
