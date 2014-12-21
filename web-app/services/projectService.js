/**
 * Created by lrollus on 7/14/14.
 */
var iris = angular.module("irisApp");

iris.constant("projectURL", "/api/projects.json");
iris.constant("projectDescrURL", "/api/project/{id}/description.json");
iris.constant("ontologyURL", "/api/ontology/{ontologyID}.json");
iris.constant("projectAvailURL", "/api/project/{projectID}/availability");

iris.factory("projectService", function($http, $log,
		projectURL, 
		projectDescrURL, 
		ontologyURL, 
		projectAvailURL,
		sessionService,
		cytomineService) {
	/*
	 * static String publickey = "29f51819-3dc6-468c-8aa7-9c81b9bc236b"; static
	 * String privatekey = "db214699-0384-498c-823f-801654238a67";
	 * 
	 */
	// cached variables

	return {

		// retrieve one specific project
		getDescription : function(projectID, callbackSuccess, callbackError) {
			var tmpUrl = projectDescrURL.replace("{id}", projectID);
			$http.get(cytomineService.addKeys(tmpUrl)).success(function(data) {
				// console.log("success on $http.get(" + tmpUrl + ")");
				if (callbackSuccess) {
					callbackSuccess(data);
				}
			}).error(function(data, status, headers, config) {
				// console.log(callbackError)
				if (callbackError) {
					callbackError(data, status, headers, config);
				}
			})
		},

		// refresh all projects (fetch the entire collection freshly from the
		// Cytomine core server
		fetchProjects : function(callbackSuccess, callbackError) {
			var url = cytomineService.addKeys(projectURL);

			$http.get(url).success(function(data) {
				// console.log("success on $http.get(" + url + ")");
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
		
		// get the associated ontology for a project
		fetchOntology : function(ontologyID, params, callbackSuccess, callbackError) {
			var url = cytomineService.addKeys(ontologyURL).replace("{ontologyID}", ontologyID);
			if (params !== null){
				if (params.flat == true) {
					url += "&flat=true";
				}	
			}
				
			$http.get(url).success(function(data) {
				// console.log("success on $http.get(" + url + ")");
				// on success, assign the data to the projects array
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
		
		// check availability of a specific project for a user
		checkAvailability : function(cmProjectID,callbackSuccess,callbackError){
			var url = cytomineService.addKeys(projectAvailURL)
							.replace("{projectID}", cmProjectID);
			
			$http.get(url).success(function(data) {
				$log.debug("success on $http.get(" + url + ")");
				if (callbackSuccess) {
					callbackSuccess(data);
				}
			}).error(function(data, status, headers, config) {
				// on error log the error
				if (callbackError) {
					callbackError(data, status, headers, config);
				}
			})
		},
	};
});