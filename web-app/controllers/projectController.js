var iris = angular.module("irisApp");

iris.controller("projectCtrl", [
"$rootScope", "$routeParams", "$scope", "$http", "$filter", "$location", "$document",
"$modal", "$log", "hotkeys", "projectService", "helpService", "sharedService", "sessionService", "ngTableParams",
	    function($rootScope, $routeParams, $scope, $http, $filter, $location, $document,
		$modal, $log, hotkeys, projectService, helpService, sharedService, sessionService, ngTableParams) {
	$log.debug("projectCtrl");
	
	// set content url for the help page
	helpService.setContentUrl("content/help/projectHelp.html");

	$scope.project = {
		stillNew : ((365 / 6) * 24 * 60 * 60 * 1000), // last 2 months
		error : {}
	};
	
	// put all valid shortcuts for this page here
	hotkeys.bindTo($scope)
	.add({
		combo : 'h',
		description : 'Show help for this page',
		callback : function() {
			helpService.showHelp();
		}
	});
	
	// get the current day as long
	$scope.today = sharedService.today;
	
	// refresh the page (get all projects and the session)
	$scope.refreshPage = function() {
		// fetch the session for the user
		sessionService.fetchSession(function(data){}, function(data,status,config,header){});
		
		$scope.loading = true;
		
		// gets all projects
		projectService.fetchProjects(function(data) {
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
				counts : [10,25,50],
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
					
					// provide data to the table
					$defer.resolve($scope.data);
				},
				filterDelay : 0,
			});
			$scope.loading = false;
		}, function(data, status) {
			$scope.project.error.retrieve = {
				status : status,
				message : data.error.message
			};
			$scope.loading = false;
		});
	};
	
	// execute project loading
	$scope.refreshPage();
	
	// open a project
	$scope.openProject = function(project){
		// update the project in the session
		sessionService.touchProject(project.id, function(data){
			// forward to the image overview table of this project
			$location.url("/project/" + project.id + "/images")
		}, function(data,status){
			sharedService.addAlert("Cannot open project. Error " + status + ".", "danger");
		})
	};
	
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
	};

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
	
	$scope.canBeOpened = function(item){
		return item.numberOfImages>0 && !item.isClosed;
	}

	// controller for the project descriptor modal dialog
	var projectDescriptorCtrl = function($scope, $modalInstance, $sce, data,
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
}]);
