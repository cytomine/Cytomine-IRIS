var iris = angular.module("irisApp");

iris.constant("sessionURL", "/api/session.json");

// A generic service for handling client sessions.
iris.factory("sessionService", function($http, $log, $location, sessionURL,
		sharedService, cytomineService) {

	return {
		// ###############################################################
		// SESSION MANAGEMENT

		// retrieve the current session for a user identified by public key
		fetchSession : function(callbackSuccess, callbackError) {
			var url = cytomineService.addKeys(sessionURL);
			$log.debug(url)
			
			$http.get(url).success(function(data) {
				$log.debug("Successfully retrieved session. ID=" + data.id);
				
				// put the session to the local storage
				localStorage.setItem("session", JSON.stringify(data));
				
				if (callbackSuccess) {
					callbackSuccess(data);
				}
			}).error(function(data, status, header, config) {
				$log.error("Error retrieving session: " + data);
				if (callbackError) {
					callbackError(data, status);
				}
			})
		},
		
		// retrieve the session
		getSession : function() {
			return JSON.parse(localStorage.getItem("session"));
		},
		
		// 
		setSession : function(session) {
			if (session){
				localStorage.setItem("session", JSON.stringify(session));
			} else {
				localStorage.removeItem("session");
			}
		}

	// END SESSION MANAGEMENT
	// ###############################################################
	}
});