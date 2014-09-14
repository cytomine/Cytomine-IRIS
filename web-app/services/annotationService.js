var iris = angular.module("irisApp")

iris.constant("userAnnURL", "/api/session/{sessionID}/project/{projectID}/image/{imageID}/annotations.json")

iris.factory("annotationService", function($http, $log, cytomineService, sessionService, userAnnURL) {
	
	return {
		// get the annotations for a given project and image
		fetchUserAnnotations : function(projectID, imageID, callbackSuccess, callbackError){
			var sessionID = sessionService.getSession().id
			$log.debug("Getting user annotations: " + sessionID + " - " + projectID + " - " + imageID)
			
			// modify the parameters
			var url = cytomineService.addKeys(userAnnURL)
				.replace("{sessionID}", sessionID)
				.replace('{projectID}', projectID)
				.replace('{imageID}', imageID);
			
			// TODO add optional offset and max parameters 
			
			// execute the http get request to the IRIS server
			$http.get(url).success(function (data) {
            	//console.log("success on $http.get(" + url + ")");
            	$log.debug(data)
				if(callbackSuccess) {
					callbackSuccess(data);
				}
            })
            .error(function (data, status, headers, config) {
            	// on error log the error
            	// console.log(callbackError)
                if(callbackError) {
                    callbackError(data,status,headers,config);
                }
            })
		}
	}
	
});
