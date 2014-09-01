var iris = angular.module("irisApp");

iris.constant("userSessionURL", "/api/session/{userID}.json");

// A generic service for handling client sessions.
iris.service("sessionService", function($http, $log, $location, userSessionURL,
		sharedService, cytomineService) {

	return {
		// ###############################################################
		// SESSION MANAGEMENT

		// retrieve the current session for a user
		fetchSession : function(userID, callbackSuccess, callbackError) {
			var url = cytomineService.addKeys(userSessionURL).replace(
					"{userID}", userID);
			$http.get(url).success(function(data) {
				$log.debug("Successfully retrieved session for userID " + userID);
				if (callbackSuccess) {
					callbackSuccess(data);
				}
			}).error(function(data, status, header, config) {
				$log.error("Error retrieving session for userID " + userID);
				if (callbackError) {
					callbackError(data, status);
				}
			})
		},

	// END SESSION MANAGEMENT
	// ###############################################################
	}
});