var iris = angular.module("irisApp");

iris.config(function($logProvider) {
	$logProvider.debugEnabled(true);
});

iris.controller("projectCtrl", function($scope, $http, $filter, $location,
		$modal, $log, projectService, ngTableParams) {
	console.log("projectCtrl");

	$scope.project = {
		stillNew : ((365 / 6) * 24 * 60 * 60 * 1000), // last 2 months
		error : {}
	};

	// create the today variable
	$scope.today = new Date().getTime();

	// gets all projects
	$scope.getAllProjects = function(callbackSuccess) {
		projectService.getAllProjects(function(data) {
			callbackSuccess(data);
		}, function(data, status) {
			$scope.project.error.retrieve = {
				status : status,
				message : data.errors
			};
			$scope.loading = false;
		});
	};

	// execute project loading
	$scope.loading = true;
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
				name : '' // initial filter
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
				params.total(newData.length); // set total for recalc
				// pagination
				$defer.resolve($scope.data);
				$scope.loading = false;
			}
		});

	});
	// debug
	// console.debug($scope);

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

	// retrieve the information for a given project
	$scope.retrieveInfo = function(projectID) {
		projectService.getDescription(projectID, 
		function(jsonDescription) {
			$scope.showInfoDialog('lg', projectID, jsonDescription.attr);
		}, function() {
			$scope.showInfoDialog('lg', projectID, {data : "This project does not have any information."});
		});
	}

	// open the modal project information dialog
	$scope.showInfoDialog = function(size, projectID, dsc) {
		var modalInstance = $modal.open({
			templateUrl : 'projectDescription.html',
			controller : projectDescriptorCtrl,
			//size : size,
			resolve : {
				description : function() {
					return dsc.data;
				},
				id : function() {
					return projectID;
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
	var projectDescriptorCtrl = function($scope, $modalInstance, description,
			id) {

		$scope.description = description;
		$scope.id = id;

		$scope.ok = function() {
			$modalInstance.close('OK');
		};

		$scope.cancel = function() {
			$modalInstance.dismiss('cancel');
		};
	};

});
