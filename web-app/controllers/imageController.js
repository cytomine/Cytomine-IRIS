var iris = angular.module("irisApp");

iris.controller(
		"imageCtrl", [
"$rootScope", "$scope", "$http", "$filter", 
"$document", "$timeout", "$location", "$route",
"$routeParams", "$log", "hotkeys",
"cytomineService", "projectService", "imageService", "sessionService", 
"helpService", "sharedService", "navService", "annotationService", 
"ngTableParams",
function($rootScope, $scope, $http, $filter, 
		$document, $timeout, $location, $route,
		$routeParams, $log, hotkeys,
		cytomineService, projectService, imageService, sessionService, 
		helpService, sharedService, navService, annotationService, 
		ngTableParams) {
	$log.debug("imageCtrl");

	// retrieve the project parameter from the URL
	$scope.projectID = $routeParams["projectID"];

	// set content url for the help page
	helpService.setContentUrl("content/help/imageHelp.html");

	// get the current date as long
	$scope.today = sharedService.today;

	$scope.image = {
			stillNew : (21 * (24 * 60 * 60 * 1000)), // last 21 days
			error : {},
			opening : {}
	};

	// put all valid shortcuts for this page here
	hotkeys.bindTo($scope)
	.add({
		combo : 'h',
		description : 'Show help for this page',
		callback : function() {
			helpService.showHelp();
		}
	}).add({
		combo : 'r',
		description : 'Resume labeling where you left last time',
		callback : function() {
			navService.resumeAtLabelingPage();
		}
	});

	// proceed to the labeling page
	$scope.startLabeling = function(image) {
		$scope.image.opening = image;
		sessionService.openImage($scope.projectID, image.cmID, function(data){
				$log.debug("Successfully opened image " + image.cmID);
				navService.navToLabelingPage($scope.projectID, image.cmID, data.settings.currentCmAnnotationID);
			//$scope.image.opening = {};
		}, function(data,status){
			sharedService.addAlert("Cannot open image. " +
				data.error.message, "danger");
			$scope.image.opening = {};
			// load the images freshly from the server
			$scope.refreshPage();
		});
	};

	$scope.annotationGallery = function(image){
		if (image.numberOfAnnotations=='0') {
			return;
		}

		$scope.image.opening = image;
		sessionService.openImage($scope.projectID, image.cmID, function(data){
			// forward to the annotation gallery of this image
			$log.debug("Successfully opened image " + image.cmID);
			// navigate to annotation gallery of selected image (predefine filter)
			navService.navToAnnotationGallery($scope.projectID, image.cmID);
		}, function(data,status){
			sharedService.addAlert("Cannot open image. Error " + status + ".", "danger");
			$scope.image.opening = {};
		});
	};

	$scope.navToProjects = function() {
		navService.navToProjects();
	};

	// refresh the page
	$scope.refreshPage = function(){
		// show the loading button
		$scope.loading = true;

		imageService.fetchImages($scope.projectID, true, function(data) {
			// success
			$scope.image.images = data; // this should be an IRIS image list
			$scope.image.total = data.length;

			if (data.length < 1){
				$scope.image.error.empty= {
						message : "This project does not have any images."
				};
				$scope.loading = false;
				return;
			} else {
				delete $scope.image.error;
			}

			// set the user preference
			$scope.hide = (data[0].projectSettings.hideCompletedImages === true);

			// copy the values since THIS FREAKING NG-TABLE CAN ONLY FILTER FOR
			// DIRECT ATTRIBUTES!!! THAT SHIT COST ME 2 HOURS OF MY LIFE!
			// many thanks, missing ng-table doc :)
			for (var i = 0; i < data.length; i++){
				$scope.image.images[i].userProgress = $scope.image.images[i].settings.progress;
			}

			// build the data table
			$scope.tableParams = new ngTableParams(
					{
						// define the parameters
						page : 1, // show first page
						count : 10, // count per page
						sorting : {
							// initial sorting
							'settings.progress' : 'desc'
						},
						filter : {
							// applies filter to the "data" object before sorting
							'userProgress' : $scope.hide?"!100":""
						}
					}, {
						// compute the pagination view
						total : $scope.image.images.length, // number of images
						getData : function($defer, params) {
							// use build-in angular filter
							var newData = $scope.image.images;

							// fixed sorting for images with no annotations
							params.sorting()['settings.numberOfAnnotations'] = params.sorting()['settings.progress'];

							$log.debug("FILTERS:", params.filter());
							$log.debug("SORTING:", params.sorting());

							// use build-in angular filter
							newData = params.filter() ? $filter('filter')(newData,
									params.filter()) : newData;
							newData = params.sorting() ? $filter('orderBy')(
									newData, params.orderBy()) : newData;

							$scope.image.pageItemMin = (params.page() - 1) * params.count();
							
							var pageItemMax = params.page() * params.count();
							$scope.image.pageItemMax = pageItemMax > newData.length ? newData.length : pageItemMax;
							
							$scope.data = newData.slice($scope.image.pageItemMin, pageItemMax);
							params.total(newData.length); // set total for recalc pagination

							$defer.resolve($scope.data);
						},
						filterDelay : 0
					});

			$log.info("image refresh successful");
			// hide or show the completed images
			$scope.loading = false;
		}, function(data, status) {

			if (status === 403){
				// no access to the images
				$scope.image.error.forbidden = {
					status : status,
					message : data.error.message
				};
			} else {
				// image fetching failed
				$scope.image.error.retrieve = {
					status : status,
					message : data.error.message
				};
			}

			$scope.loading = false;
		});
	};

//	fetch the images
	$scope.refreshPage();

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

	var getBtnAllProgresses = function() {
		return document.getElementById("allProgresses");
	};

	var getBtnHideFinishedProgresses = function() {
		return document.getElementById("hideCompleted");
	};

//	put the settings update to the server
	$scope.hideCompleted = function(hideCompleted){
		// get the settings from the first image
		var settings = $scope.image.images[0].projectSettings;

		// PUT UPDATE TO SERVER
		settings.hideCompletedImages = hideCompleted;

		$scope.filterLoading = true;

		sessionService.updateProjectSettings(settings, function(data){
			$scope.filterLoading = false;
		}, function(data, status){
			$scope.filterLoading = false;
		});

		$scope.hide = hideCompleted;
		if (!hideCompleted){
			$timeout(function(){
				getBtnAllProgresses().click();
				$log.info("Showing all progresses.");
			}, 1);
		}else {
			$timeout(function(){
				getBtnHideFinishedProgresses().click();
				$log.info("Showing only incomplete progresses.");
			},1)
		}
	};
}]);
