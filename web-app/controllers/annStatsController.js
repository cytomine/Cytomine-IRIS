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

	// retrieve the project parameter from the URL
	$scope.projectID = $routeParams["projectID"];

	// get the current date as long
	$scope.today = sharedService.today;

	$scope.annstats = {
			stillNew : (21 * (24 * 60 * 60 * 1000)), // last 21 days
			error : {},
			sbcollapsed : false,
			opening : {},
			startup: true,
			slider : {
				options : {
					start: function (event, ui) {
						$log.debug('Slider start');
					},
					stop: function (event, ui) {
						$log.debug('Slider stop');
						if ($scope.annstats.slider.value === $scope.annstats.slider.max){
							$log.debug("reached maximum agreement")
						} else if ($scope.annstats.slider.value === $scope.annstats.slider.min){
							$log.debug("reached minimum agreement, showing all annotations")
						}

						// filter the table
						$scope.tableParams.reload();
						$scope.tableParams.page(1);
					}
				},
				// initial value of the slider
				min : 0,
				max : 0,
				// current slider value
				value : 0
			}
	};

	$scope.setCollapsed = function(flag){
		$scope.annstats.sbcollapsed = flag;
	};

	// selected users for filtering
	var selectedUsers = [];
	// selected terms for filtering
	var selectedTerms = [];
	// selected images for filtering
	var selectedImages = [];

	// react to term filter change events
	$scope.$on("termFilterChange", function(event, object) {
		$log.debug("termFilterChange detected in annStatsCtrl");

		var action = object.action;
		//$log.debug(event.name + "::" + action + ": "  + object.name + " [" + object.id + "].");

		if (action === 'add'){
			// incremental fetching
			// fetch the selected term for the selected images
			selectedTerms.push(object.id);
		} else if (action === 'remove'){
			// remove the selected term
			selectedTerms.splice(selectedTerms.indexOf(object.id), 1);
		} else if (action === 'addAll'){
			// here the id is the entire array
			selectedTerms = object.id;
		} else if (action === 'removeAll'){
			// remove all selected terms
			selectedTerms = [];
		}
		$scope.showOrHideNoLabelWarning();
	});

	$scope.$on("userFilterChange", function(event, object) {
		$log.debug("userFilterChange detected in annStatsCtrl");

		selectedUsers = object.id;

		$scope.showOrHideNoUsersWarning();
	});

	$scope.$on("imageFilterChange", function(event, object) {
		$log.debug("imageFilterChange detected in annStatsCtrl");

		selectedImages = object.id;

		$scope.showOrHideNoImageWarning();
	});

	//$scope.queryStatus = function(){
	//	$log.debug("users : " + selectedUsers);
	//	$log.debug("terms : " + selectedTerms);
	//	$log.debug("images: " + selectedImages);
	//};

	$scope.performQuery = function(){
		$scope.showOrHideNoImageWarning();
		$scope.showOrHideNoLabelWarning();
		$scope.showOrHideNoUsersWarning();

		if (selectedImages.length === 0){
			$log.debug("No images to retrieve statistics for.");
			return;
		}

		if (selectedTerms.length === 0){
			$log.debug("No terms to retrieve statistics for.");
			return;
		}

		if (selectedUsers.length === 0){
			$log.debug("No users to retrieve statistics for.");
			return;
		}


		$log.debug("Fetching user statistics " + selectedUsers +
		" for terms " + selectedTerms
		+ " for " + selectedImages.length + " images.");

		// show the loading button
		$scope.loading = true;
		$scope.annstats.startup = false;

		try {
			// -> evil hack to get back the pagination after refreshing table! <-
			$scope.tableParams.page(-1);
		} catch (ignored){
			//$log.debug("Expected (nullpointer) error on table parameters.");
		}

		statisticsService.fetchAnnotationAgreementList($scope.projectID,
			selectedImages, selectedTerms, selectedUsers, function(data) {
			// success
			$scope.annstats.annotations = data.annotationStats; // this should be a list of annotations
			$scope.terms = data.terms; // the term map
			$scope.users = data.users; // the sorted users list
			$scope.annstats.total = data.annotationStats.length;

			// set the slider max value
			$scope.annstats.slider.max = data.nUniqueUsersOverall;
			// set the current value (must not exceed the maximum value)
			if ($scope.annstats.slider.value > $scope.annstats.slider.max){
				$scope.annstats.slider.value = $scope.annstats.slider.max;
			}

			if (data.length < 1){
				$scope.annstats.error.empty= {
						message : "This query does not result in any annotations."
				};
				$scope.loading = false;
				return;
			} else {
				delete $scope.annstats.error;
			}

			$scope.usercolumns = [];

			// the lookup map of user names
			$scope.usermap = {};

			// loop over the map and construct the dynamic columns
			for (var k = 0; k < data.users.length; k++) {
				var userid = data.users[k].id;
				var username = data.users[k].username;
				$scope.usercolumns.push(
					{
						title: String(username),
						userStats: 'userStats',
						visible: true,
						userID: userid
					}
				);
				$scope.usermap[userid] = {
					'username' : username,
					'firstname': data.users[k].firstname,
					'lastname': data.users[k].lastname
				};
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

							var theFinalData = [];

							// search for minimum agreement in the data
							for (var i = 0; i < newData.length; i++){
								var elmnt = newData[i]; // one annotation

								// if the highest agreement is in the filter range
								if (elmnt.assignmentRanking[0].nUsers >= $scope.annstats.slider.value){
									// collect the agreement entry
									theFinalData.push(elmnt);
								}
							}

							$log.debug("Filtered " + theFinalData.length + " annotations for display.");

							// just show elements above a certain agreement
							newData = theFinalData;

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

			$log.info("annotations refresh successful");
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

	// perform the query on the refresh button
	$scope.refreshPage = $scope.performQuery;

	$scope.warn = {};

	// warnings/infos
	$scope.showOrHideNoLabelWarning = function(){
		$scope.warn.noLabel = {};

		if (selectedTerms.length === 0){
			$scope.warn.noLabel = {
				message : "There is no label/term selected! " +
				"Please choose at least one from the ontology on the left side, then press REFRESH."
			};
			$log.info("No terms selected, won't fetch any statistics, but show warning.");
		} else {
			$log.info("deleted noLabel warning!");
			delete $scope.warn.noLabel;
		}
	};

	$scope.showOrHideNoImageWarning = function(){
		$scope.warn.noImage = {};
		if (selectedImages.length === 0){
			$scope.warn.noImage = {
				message : "There is no image selected! " +
				"Please choose at least one from the image list on the left side, then press REFRESH."
			};
			$log.info("No image(s) selected, won't fetch any statistics, but show warning.");
		} else {
			delete $scope.warn.noImage;
		}
	};

	$scope.showOrHideNoUsersWarning = function(){
		$scope.warn.noUsers = {};
		if (selectedUsers.length === 0){
			$scope.warn.noUsers = {
				message : "There are no users selected! " +
				"Please choose at least one from the project user list on the left side, then press REFRESH."
			};
			$log.info("No user(s) selected, won't fetch any statistics, but show warning.");
		} else {
			delete $scope.warn.noUsers;
		}
	};

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
