/**
 * Created by lrollus on 7/14/14.
 */
var iris = angular.module("irisApp");

iris.constant("projectUrl", "/api/projects.json?resolveOntology=true");
iris.constant("projectDescrUrl", "/api/project/{id}/description.json");
iris.constant("ontologyUrl", "/api/ontology/{ontologyID}.json");
iris.factory("projectService", function($http, projectUrl, projectDescrUrl, ontologyUrl,
		cytomineService) {
	/*
	 * static String publickey = "29f51819-3dc6-468c-8aa7-9c81b9bc236b"; static
	 * String privatekey = "db214699-0384-498c-823f-801654238a67";
	 * 
	 */
	// cached variables
	var projects = [];
	var currentProject;
	var currentOntology;

	return {

		// retrieve one specific project
		getDescription : function(projectID, callbackSuccess, callbackError) {
			var tmpUrl = projectDescrUrl.replace("{id}", projectID);
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

		// retrieve the currently active project ID
		getProjectID : function() {
			return localStorage.getItem("currentProjectID");
		},

		// set the currently active project ID
		setProjectID : function(projectID) {
			localStorage.setItem("currentProjectID", projectID);
		},

		// remove the current project ID
		removeProjectID : function() {
			localStorage.removeItem("currentProjectID");
		},

		// list the retrieved project as an array
		getAllProjects : function() {
			return projects;
		},

		// refresh all projects (fetch the entire collection freshly from the
		// Cytomine core server
		fetchProjects : function(callbackSuccess, callbackError) {
			var url = cytomineService.addKeys(projectUrl);

			$http.get(url).success(function(data) {
				// console.log("success on $http.get(" + url + ")");
				// on success, assign the data to the projects array
				projects = data;
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
			var url = cytomineService.addKeys(ontologyUrl).replace("{ontologyID}", ontologyID);
			if (params.flat == true) {
				console.log("appending flat")
				url += "&flat=true";
			}			

			$http.get(url).success(function(data) {
				// console.log("success on $http.get(" + url + ")");
				// on success, assign the data to the projects array
				projects = data;
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
		}
	};
});