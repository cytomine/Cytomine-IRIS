var iris = angular.module("irisApp")

iris.constant("userAnnUrl","/api/project/{pID}/image/{iID}/user/{uID}/annotations.json");

iris.service("annotationService", function($http, $log, cytomineService, userAnnUrl) {
	
	return {
		// get the annotations for a given project and image
		getUserAnnotations : function(projectID, imageID, userID, callbackSuccess, callbackError){
			$log.debug(projectID + " - " + imageID + " - " + userID)
			
			// modify the parameters
			userAnnUrl = userAnnUrl.replace('{pID}', projectID).replace('{iID}', imageID).replace("{uID}", userID);
			
			$log.debug(userAnnUrl)
			
			// execute the http get request to the IRIS server
			$http.get(tmpUrl = cytomineService.addKeys(userAnnUrl))
            .success(function (data) {
            	//console.log("success on $http.get(" + tmpUrl + ")");
            	$log.debug(data)
            	// on success, assign the data to the projects array
//              TODO  if(callbackSuccess) {
//                    callbackSuccess(data);
//                }
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
