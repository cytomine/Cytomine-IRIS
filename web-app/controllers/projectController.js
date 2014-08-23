var iris = angular.module("irisApp");

iris.config(function($logProvider) {
	$logProvider.debugEnabled(true);
});

iris.controller("projectCtrl", function($scope, $http, $filter, $location, $document,
		$modal, $log, projectService, helpService, sharedService, ngTableParams) {
	console.log("projectCtrl");

	$scope.project = {
		stillNew : ((365 / 6) * 24 * 60 * 60 * 1000), // last 2 months
		error : {}
	};
	
	// get the current day as long
	$scope.today = sharedService.today;
	
	// set the help variable for this page 
	//$log.debug("Setting help content url for project ctrl");
	helpService.setContentUrl("content/help/projectHelp.html");

	// gets all projects
	$scope.getAllProjects = function(callbackSuccess) {
		projectService.fetchProjects(function(data) {
			callbackSuccess(data);
		}, function(data, status) {
			$scope.project.error.retrieve = {
				status : status,
				message : data.errors
			};
			$scope.loading = false;
		});
	};

	// refresh the page
	$scope.refreshPage = function() {
		$scope.loading = true;
		console.log("loading: " + $scope.loading)
		
		$scope.getAllProjects(function(data) {
			$scope.project.error.retrieve = null;
			$scope.project.projects = data;
			
			// build the data table
			$scope.tableParams = new ngTableParams({
				page : 1, // show first page
				count : 10, // count per page
				// leave this blank for no sorting at all
				sorting : {
					created : 'desc' // initial sorting
				},
				filter : {
					// applies filter to the "data" object before sorting
				}
			}, {
				total : $scope.project.projects.length, // number of projects
				getData : function($defer, params) {
					// use build-in angular filter
					var newData = $scope.project.projects;
					// use build-in angular filter
					newData = params.filter() ? $filter('filter')(newData,
							params.filter()) : newData;
					newData = params.sorting() ? $filter('orderBy')(newData,
							params.orderBy()) : newData;
					$scope.data = newData.slice((params.page() - 1)
							* params.count(), params.page() * params.count());
					params.total(newData.length); // set total for recalc pagination
					
					$defer.resolve($scope.data);
				},
			});
			$scope.loading = false;
		});
	};
	
	// execute project loading
	$scope.refreshPage();
	
	// set the current project ID
	$scope.setProjectID = function(projectID) {
		projectService.setProjectID(projectID);
		$scope.$emit("currentProjectID", projectID);
		$scope.$broadcast("currentProjectID", projectID);
	}

	// retrieve the current project ID
	$scope.getProjectID = function() {
		return projectService.getProjectID();
	}

	// clear the project ID
	$scope.removeProjectID = function() {
		$scope.setProjectID(null);
		projectService.removeProjectID();
	}
	// each time, the project controller gets loaded, clear the ID
	// $scope.clearProjectID();

	$scope.printProjectID = function() {
		alert("The current project ID is: " + projectService.getProjectID());
	}

	
	// ###############################################################
	// PROJECT DESCRIPTION MODAL DIALOG
	// retrieve the information for a given project
	$scope.retrieveInfo = function(project) {
		projectService.getDescription(project.id, 
		
		// successCallback
		function(jsonDescription) {
			// open the dialog
			$scope.showInfoDialog(project, jsonDescription.attr);
		}, 
		
		// error callback
		function(data, status) {

			// react to specific status codes
			if (status === 404){
				dlgData = { data : "This project does not have any description.",
							error : {
							message : "No object found.",
							status : status,
							show : false
						}
				};
			} else {
				dlgData = { data : {},
						error : {
							message : "The project information cannot be retrieved.",
							status : status,
							show : true
						}
					}
			}
			
			// open the dialog
			$scope.showInfoDialog(project, dlgData);
		});
	}

	// open the modal project information dialog
	$scope.showInfoDialog = function(project, dlgData) {
		var modalInstance = $modal.open({
			templateUrl : 'projectDescription.html',
			controller : projectDescriptorCtrl,
			//size : 'lg',
			resolve : {
				data : function() {
					return dlgData.data;
				},
				project : function() {
					return project;
				},
				error : function() {
					return dlgData.error;
				}
			}
		});

		modalInstance.result.then(function(result) {
			// callbackSuccess branch
			$log.debug('Project Description Modal: ' + result);
		}, function(result) {
			// callbackError branch
			$log.debug('Project Description Modal: ' + result);
		});
	};

	// controller for the project descriptor modal dialog
	var projectDescriptorCtrl = function($scope, $modalInstance, data,
			project, error) {

		$scope.description = data;
		$scope.project = project;
		$scope.error = error;

		$scope.ok = function() {
			$modalInstance.close('OK');
		};

		$scope.cancel = function() {
			$modalInstance.dismiss('cancel');
		};
	};
	// END PROJECT DESCRIPTION MODAL DIALOG
	// ###############################################################

	
});
