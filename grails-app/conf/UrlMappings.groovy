import java.awt.Desktop.Action;

class UrlMappings {

	static mappings = {
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(view:"/iris")
        "500"(view:'/error')

        "/api/welcome.$fomat"(controller:"main"){
            action = [GET: "welcome"]
        }

        "/api/projects.$fomat"(controller:"cytomine"){
            action = [GET: "projects"]
        }
		
        "/api/project/$idProject/images.$fomat"(controller:"cytomine"){
            action = [GET: "images"]
        }
		
		"/api/appName.$format"(controller:"main"){
			action = [GET: "appName"]
		}
		
		"/api/appVersion.$format"(controller:"main"){
			action = [GET: "appVersion"]
		}
		
		"/api/cytomineHost.$format"(controller:"cytomine"){
			action = [GET: "getHostAddress"]
		}
		
		"/api/cytomineWeb.$format"(controller:"cytomine"){
			action = [GET: "getWebAddress"]
		}
	}
}
