var iris = angular.module("irisApp");

iris.controller(
		"annStatsCtrl", [
"$rootScope", "$scope", "$http", "$filter", 
"$document", "$timeout", "$location", "$route",
"$routeParams", "$log", "hotkeys",
"cytomineService", "projectService", "imageService", "sessionService", 
"helpService", "sharedService", "navService", "annotationService", "statisticsService",
"ngTableParams",
function($rootScope, $scope, $http, $filter, 
		$document, $timeout, $location, $route,
		$routeParams, $log, hotkeys,
		cytomineService, projectService, imageService, sessionService, 
		helpService, sharedService, navService, annotationService, statisticsService,
		ngTableParams) {
	$log.debug("annStatsCtrl");

	$scope.myData = [10,20,30];

	// retrieve the project parameter from the URL
	$scope.projectID = $routeParams["projectID"];

	// get the current date as long
	$scope.today = sharedService.today;

	$scope.annstats = {
			stillNew : (21 * (24 * 60 * 60 * 1000)), // last 21 days
			error : {},
			opening : {}
	};

	// refresh the page
	$scope.refreshPage = function(){
		// show the loading button
		$scope.loading = true;

		statisticsService.fetchAnnotationAgreementList($scope.projectID, null, null, null, function(data) {
			// success
			$scope.annstats.annotations = data.annotationStats; // this should be a list of annotations
			$scope.annstats.total = data.length;

			if (data.length < 1){
				$scope.annstats.error.empty= {
						message : "This project does not have any images."
				};
				$scope.loading = false;
				return;
			} else {
				delete $scope.annstats.error;
			}

			$scope.usercolumns = [];

			// construct the dynamic columns
			for (var k = 0; k < data.users.length; k++){
				var userid = data.users[k].id;
				var username = data.users[k].username;
				$scope.usercolumns.push(
					{ title: String(username), userStats: 'userStats', visible: true, userID: userid, user: data.users[k]}
				)
			}

			// build the data table
			$scope.tableParams = new ngTableParams(
					{
						// define the parameters
						page : 1, // show first page
						count : 10, // count per page
						sorting : {
							// initial sorting
						},
						filter : {
							// applies filter to the "data" object before sorting
						}
					}, {
						// compute the pagination view
						total : $scope.annstats.annotations.length, // number of images
						getData : function($defer, params) {
							// use build-in angular filter
							var newData = $scope.annstats.annotations;

							// use build-in angular filter
							newData = params.filter() ? $filter('filter')(newData,
									params.filter()) : newData;
							newData = params.sorting() ? $filter('orderBy')(
									newData, params.orderBy()) : newData;

							$scope.annstats.pageItemMin = (params.page() - 1) * params.count();
							
							var pageItemMax = params.page() * params.count();
							$scope.annstats.pageItemMax = pageItemMax > newData.length ? newData.length : pageItemMax;
							
							$scope.data = newData.slice($scope.annstats.pageItemMin, pageItemMax);
							params.total(newData.length); // set total for recalc pagination

							$defer.resolve($scope.data);
						},
						filterDelay : 0
					});

			$log.info("image refresh successful");
			// hide or show the completed images
			$scope.loading = false;
		}, function(data, status) {
			// image fetching failed
			$scope.annstats.error.retrieve = {
					status : status,
					message : data.error.message
			};
			$scope.loading = false;
		});
	};

//	fetch the annotations
//	$scope.refreshPage();

//	//////////////////////////////////////////
//	declare additional methods
//	//////////////////////////////////////////
//	Determine the row's background color class according 
//	to the current labeling progress.
	$scope.addKeys = function(url){
		try {
			return cytomineService.addKeys(url);
		} catch (e){
			return '';
		}
	};
}]);
