var iris = angular.module("irisApp");

iris.constant("userAnnURL", "api/session/{sessionID}/project/"
		+ "{projectID}/annotations.json?");
iris.constant("userAnn3TupleURL", "api/session/{sessionID}/project/"
		+ "{projectID}/image/{imageID}/annotations/tuple.json");
iris.constant("setAnnTermURL", "api/session/{sessionID}/project/"
		+ "{projectID}/image/{imageID}/annotation/{annID}/term/{termID}/clearBefore.json");
iris.constant("addAnnTermURL", "api/session/{sessionID}/project/"
		+ "{projectID}/image/{imageID}/annotation/{annID}/term/{termID}.json");
iris.constant("delAnnTermURL", "api/session/{sessionID}/project/"
		+ "{projectID}/image/{imageID}/annotation/{annID}/term/{termID}.json");
iris.constant("delAllAnnURL", "api/session/{sessionID}/project/" 
		+ "{cmProjectID}/image/{cmImageID}/annotation/{cmAnnID}/terms.json");

iris.factory("annotationService", function($http, $log, cytomineService,
		sessionService, userAnnURL, sharedService, userAnn3TupleURL, setAnnTermURL, addAnnTermURL, delAnnTermURL, delAllAnnURL) {

	return {
		// get the annotations for a given project and image
		fetchUserAnnotations : function(projectID, imageIDs, callbackSuccess,
				callbackError) {
			var sessionID = sessionService.getSession().id
			$log.debug("Getting user annotations: " + sessionID + " - "
					+ projectID + " - " + imageIDs)
					
			// modify the parameters
			var url = cytomineService.addKeys(userAnnURL).replace(
					"{sessionID}", sessionID).replace("{projectID}", projectID);
			
			if (imageIDs !== null){
				url += "&imageID=" + imageIDs.toString().replace("[","").replace("]","");
			}

			// execute the http get request to the IRIS server
			$http.get(url).success(function(data) {
				// console.log("success on $http.get(" + url + ")");
				$log.debug(data)
				if (callbackSuccess) {
					callbackSuccess(data);
				}
			}).error(function(data, status, headers, config) {
				// on error log the error
				console.log(callbackError)
				if (callbackError) {
					callbackError(data, status, headers, config);
				}
			})
		},
		
		// get the annotations for a given project, images and a single term
		fetchUserAnnotationsByTerm : function(projectID, imageIDs, termIDs, callbackSuccess,
				callbackError, offset, max) {
			var sessionID = sessionService.getSession().id
			$log.debug("Getting user annotations by terms: " + sessionID + " - "
					+ projectID + " - " + imageIDs + " - " + termIDs)
					
			// modify the parameters
			var url = cytomineService.addKeys(userAnnURL).replace(
					"{sessionID}", sessionID).replace("{projectID}", projectID);
			
			if (imageIDs !== null){
				url += ("&images=" + imageIDs.toString().replace("[","").replace("]",""));
			}
			
			// add the terms of interest to the query
			url += ("&terms=" + termIDs.toString().replace("[","").replace("]",""));
			
			// add pagination parameters
			if (offset) {
				// 0 offset is counted as missing parameter and causes loading of first page
				url += "&offset=" + offset;
			}
			if (max) {
				url += "&max=" + max;
			}

			// execute the http get request to the IRIS server
			$http.get(url).success(function(data) {
				// console.log("success on $http.get(" + url + ")");
				//$log.debug(data)
				if (callbackSuccess) {
					callbackSuccess(data);
				}
			}).error(function(data, status, headers, config) {
				// on error log the error
//				console.log(callbackError)
				if (callbackError) {
					callbackError(data, status, headers, config);
				}
			})
		},
		
		// get the annotations for a given project, images and WITHOUT any term for the querying user
		fetchUserAnnotationsWithoutTerm : function(projectID, imageIDs, callbackSuccess,
				callbackError) {
			var sessionID = sessionService.getSession().id
			$log.debug("Getting user annotations without terms: " + sessionID + " - "
					+ projectID + " - " + imageIDs)
					
			// modify the parameters
			var url = cytomineService.addKeys(userAnnURL).replace(
					"{sessionID}", sessionID).replace("{projectID}", projectID);
			
			if (imageIDs !== null){
				url += ("&image=" + imageIDs.toString().replace("[","").replace("]",""));
			}
			
			url += ("&term=" + sharedService.constants.noTermAssigned);

			// execute the http get request to the IRIS server
			$http.get(url).success(function(data) {
				// console.log("success on $http.get(" + url + ")");
				//$log.debug(data)
				if (callbackSuccess) {
					callbackSuccess(data);
				}
			}).error(function(data, status, headers, config) {
				// on error log the error
//				console.log(callbackError)
				if (callbackError) {
					callbackError(data, status, headers, config);
				}
			})
		},
		
		// get the 3-tuple annotations for a given project and image
		fetchUserAnnotations3Tuple : function(projectID, imageID, hideCompleted, annID, callbackSuccess,
				callbackError) {
			var sessionID = sessionService.getSession().id
			$log.debug("Getting user annotations (3-tuple): " + sessionID + " - "
					+ projectID + " - " + imageID + ", starting at " + annID)

			// modify the parameters
			var url = cytomineService.addKeys(userAnn3TupleURL).replace(
					"{sessionID}", sessionID).replace("{projectID}", projectID)
					.replace("{imageID}", imageID);

			// add optional offset and max parameters
			url += ("&currentAnnotation=" + annID);
			url += ("&hideCompleted=" + hideCompleted);

			// execute the http get request to the IRIS server
			$http.get(url).success(function(data) {
				// console.log("success on $http.get(" + url + ")");
				$log.debug(data)
				if (callbackSuccess) {
					callbackSuccess(data);
				}
			}).error(function(data, status, headers, config) {
				// on error log the error
				// console.log(callbackError)
				if (callbackError) {
					callbackError(data, status, headers, config);
				}
			})
		},
		
		// set the unique term for a specific user for a specific annotation
		setAnnotationTerm : function(projectID, imageID, annID, termID, callbackSuccess,
				callbackError, item) {
			var sessionID = sessionService.getSession().id
			$log.debug("Posting term assignment to IRIS: " + sessionID + " - "
					+ projectID + " - " + imageID + " - " + annID + " - " + termID);

			// modify the parameters
			var url = cytomineService.addKeys(setAnnTermURL).replace(
					"{sessionID}", sessionID).replace("{projectID}", projectID)
					.replace("{imageID}", imageID).replace("{annID}", annID)
					.replace("{termID}", termID);
			
			// construct the payload
			var payload = "{ annotation: " + annID + ", term: " + termID + " }";

//			HINT: content-type "application/json" is default!
			// execute the http post request to the IRIS server
			$http.post(url, payload).success(function(data) {
				// console.log("success on $http.get(" + url + ")");
//				$log.debug(data)
				if (callbackSuccess) {
					if (item){
						callbackSuccess(data,item);
					} else {
						callbackSuccess(data);
					}
				}
			}).error(function(data, status, headers, config) {
				// on error log the error
				$log.error(status)
				if (callbackError) {
					callbackError(data, status, headers, config);
				}
			})
		}, 
		
		// remove term for a specific user for a specific annotation
		deleteAnnotationTerm : function(projectID, imageID, annID, termID, callbackSuccess,
				callbackError, item) {
			var sessionID = sessionService.getSession().id
			$log.debug("Removing term assignment: " + sessionID + " - "
					+ projectID + " - " + imageID + " - " + annID + " - " + termID);

			// modify the parameters
			var url = cytomineService.addKeys(delAnnTermURL).replace(
					"{sessionID}", sessionID).replace("{projectID}", projectID)
					.replace("{imageID}", imageID).replace("{annID}", annID)
					.replace("{termID}", termID);
			
			// execute the http delete request to the IRIS server
			$http.delete(url).success(function(data) {
				// console.log("success on $http.get(" + url + ")");
//				$log.debug(data)
				if (callbackSuccess) {
					if (item){
						callbackSuccess(data,item);
					} else {
						callbackSuccess(data);
					}
				}
			}).error(function(data, status, headers, config) {
				// on error log the error
				$log.error(status)
				if (callbackError) {
					callbackError(data, status, headers, config);
				}
			})
		},
		
		// remove all terms for a specific user for a specific annotation
		deleteAllAnnotationTerms : function(projectID, imageID, annID, callbackSuccess,
				callbackError, item) {
			var sessionID = sessionService.getSession().id
			$log.debug("Removing all term assignment: " + sessionID + " - "
					+ projectID + " - " + imageID + " - " + annID);

			// modify the parameters
			var url = cytomineService.addKeys(delAnnTermURL).replace(
					"{sessionID}", sessionID).replace("{projectID}", projectID)
					.replace("{imageID}", imageID).replace("{annID}", annID);
			
			// execute the http delete request to the IRIS server
			$http.delete(url).success(function(data) {
				// console.log("success on $http.get(" + url + ")");
//				$log.debug(data)
				if (callbackSuccess) {
					if (item){
						callbackSuccess(data,item);
					} else {
						callbackSuccess(data);
					}
				}
			}).error(function(data, status, headers, config) {
				// on error log the error
				$log.error(status)
				if (callbackError) {
					callbackError(data, status, headers, config);
				}
			})
		},
	}

});
