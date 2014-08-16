angular.module("irisApp").config(function($logProvider) {
	$logProvider.debugEnabled(true);
}).controller(
		"projectCtrl",
		function($scope, $http, $filter, $location, projectService,
				ngTableParams) {
			console.log("projectCtrl");

			$scope.project = {
				stillNew : ((365/6)*24*60*60*1000), // last 2 months
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
						newData = params.sorting() ? $filter('orderBy')(
								newData, params.orderBy()) : newData;
						$scope.data = newData.slice((params.page() - 1)
								* params.count(), params.page()
								* params.count());
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
			$scope.setProjectID = function(idProject) {
				projectService.setProjectID(idProject);
				$scope.$emit("currentProjectID", idProject);
				$scope.$broadcast("currentProjectID", idProject);
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
				alert("The current project ID is: "
						+ projectService.getProjectID());
			}
		});
