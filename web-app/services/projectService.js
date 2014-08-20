/**
 * Created by lrollus on 7/14/14.
 */
var iris = angular.module("irisApp");

iris.constant("projectUrl", "/api/projects.json");
iris.constant("projectDescrUrl", "/api/project/{id}/description.json");
iris.factory("projectService",function($http, projectUrl, projectDescrUrl, cytomineService) {
/*
 static String publickey = "29f51819-3dc6-468c-8aa7-9c81b9bc236b";
 static String privatekey = "db214699-0384-498c-823f-801654238a67";

 */
    	// actionName : function(functionParam1, functionParam2, ...) 
        var projects=[];

        return {
        	
//        	// retrieve one specific project
//        	getProject : function(projectID){
//        		var tmpUrl = projectUrl + "&projectID=" + projectID;
//        		$http.get(cytomineService.addKeys(tmpUrl))
//	                .success(function (data) {
//	                	//console.log("success on $http.get(" + tmpUrl + ")");
//	                	// on success, assign the data to the projects array
//	                    project = data;
//	                    if(callbackSuccess) {
//	                        callbackSuccess(data);
//	                    }
//	                })
//	                .error(function (data, status, headers, config) {
//	                	// on error log the error
//	                	// console.log(callbackError)
//	                    if(callbackError) {
//	                        callbackError(data,status);
//	                    }
//	                })
//        	},
        	
        	// retrieve one specific project
        	getDescription : function(projectID, callbackSuccess, callbackError){
        		var tmpUrl = projectDescrUrl.replace("{id}", projectID);
        		$http.get(cytomineService.addKeys(tmpUrl))
	                .success(function (data) {
	                	//console.log("success on $http.get(" + tmpUrl + ")");
	                    if(callbackSuccess) {
	                        callbackSuccess(data);
	                    }
	                })
	                .error(function (data, status, headers, config) {
	                	// console.log(callbackError)
	                    if(callbackError) {
	                        callbackError(data, status, headers, config);
	                    }
	                })
        	},
        	
        	// retrieve the currently active project ID
        	getProjectID : function() {
				return localStorage.getItem("currentProjectID");
			},
			
			// set the currently active project ID
			setProjectID : function(idProject) {
				localStorage.setItem("currentProjectID", idProject);
			},
			
			// remove the current project ID
			removeProjectID : function() {
				localStorage.removeItem("currentProjectID");
			},

        	// list the retrieved project as an array
            allProjects : function() {
            	return projects;
            },

            // gets all projects from the Cytomine core instance
            getAllProjects : function(callbackSuccess, callbackError) {
                if(projects.length == 0) {
                    this.refreshAllProjects(callbackSuccess,callbackError);
                } else {
                    callbackSuccess(projects);
                }
            },

            // refresh all projects (fetch the entire collection freshly from the 
            // Cytomine core server
            refreshAllProjects : function(callbackSuccess, callbackError) {
                $http.get(tmpUrl = cytomineService.addKeys(projectUrl))
                    .success(function (data) {
                    	//console.log("success on $http.get(" + tmpUrl + ")");
                    	// on success, assign the data to the projects array
                        projects = data;
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
        };
    });