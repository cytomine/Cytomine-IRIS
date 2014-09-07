var iris = angular.module("irisApp");

iris.constant("sessionURL", "/api/session.json");
iris.constant("touchProjectURL", "/api/session/{sessionID}/project/{projectID}/touch");
iris.constant("updateProjectURL", "/api/session/{sessionID}/project/{projectID}");

// A generic service for handling client sessions.
iris.factory("sessionService", function($http, $log, $location, sessionURL,
		touchProjectURL, updateProjectURL,
		sharedService, cytomineService) {

	return {
		// ###############################################################
		// SESSION MANAGEMENT
		
		// retrieve the session
		getSession : function() {
			return JSON.parse(localStorage.getItem("session"));
		},
		
		// set the session to the local storage
		setSession : function(session) {
			if (session){
				localStorage.setItem("session", JSON.stringify(session));
			} else {
				localStorage.removeItem("session");
			}
		},
		
		// retrieve the current session for a user identified by public key
		// this will overwrite the existing local session in order to reflect
		// any updates performed by this user on another client
		fetchSession : function(callbackSuccess, callbackError) {
			var sessionService = this;
			var url = cytomineService.addKeys(sessionURL);
			$log.debug(url)
			
			$http.get(url).success(function(data) {
				$log.debug("Successfully retrieved session. ID=" + data.id);
				
				// put the session to the local storage
				sessionService.setSession(data);

				if (callbackSuccess) {
					callbackSuccess(data);
				}
			}).error(function(data, status, header, config) {
				$log.error("Error retrieving session: " + data);
				sharedService.addAlert("The session could not be retrieved!", "danger")
				
				if (callbackError) {
					callbackError(data, status);
				}
			})
		},
		
		// retrieve the currently active project
		getCurrentProject : function() {
			var sess = this.getSession();
			if (sess.currentProject != null){
				$log.debug("returning local IRIS project")
				return sess.currentProject;
			} else {
				$log.debug("returning null")
				return null;
			}
		},

		// set the currently active project
		setCurrentProject : function(project) {
			if (project == null){
				var session = this.getSession();
				session.currentProject = null;
				this.setSession(session);
			} else {
				this.updateProject(project);
			}
		},

		// touches a project in the current session
		touchProject : function(cmProjectID, callbackSuccess, callbackError){
			var sessionService = this;
			var session = sessionService.getSession();
			var url = cytomineService.addKeys(touchProjectURL)
							.replace("{sessionID}", session.id)
							.replace("{projectID}", cmProjectID);
			
			$http.post(url, null).success(function(data){
				// on success, update the project in the local storage
				session.currentProject = data;
				sessionService.setSession(session);
				if (callbackSuccess){
					callbackSuccess(data)
				}
			}).error(function(data,status,header,config){
				// on error, show the error message
				sharedService.addAlert(status + ": " + data, "danger");
				$log.error(data);

				if (callbackError){
					callbackError(data, status)
				}
			});
		},			
		
		// updates a project in the current session, payload is the IRIS project instance
		updateProject : function(irisProject, callbackSuccess, callbackError){
			var sessionService = this;
			var session = sessionService.getSession();
			
			var projectClass = irisProject['class']
			
			var url = cytomineService.addKeys(updateProjectURL + "?class=" + projectClass)
							.replace("{sessionID}", session.id)
							.replace("{projectID}", irisProject.cmID);
			
			$http.put(url, irisProject).success(function(data){
				// on success, update the project in the local storage
				session.currentProject = data;
				sessionService.setSession(session);

				$log.debug("successfully updated current project")
				
				if (callbackSuccess){
					callbackSuccess(data)
				}
			}).error(function(data,status,header,config){
				// on error, show the error message
				sharedService.addAlert(status + ": " + data, "danger");
				$log.error(status);

				if (callbackError){
					callbackError(data, status)
				}
			});
		},
	// END SESSION MANAGEMENT
	// ###############################################################
	}
});