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

	// look up in IRIS, if the requested project is available
	// for this user.
	projectService.checkAvailability($scope.projectID, function(data){
		// the success
		$log.debug("project is available!");
	}, function(data, status){
		// the error
		$log.error("project is not available!");
	})

	// set content url for the help page
	helpService.setContentUrl("content/help/imageHelp.html");

	// get the current date as long
	$scope.today = sharedService.today;

	$scope.image = {
			stillNew : (21 * (24 * 60 * 60 * 1000)), // last 21 days
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
	}).add({
		combo : 'r',
		description : 'Resume labeling where you left last time',
		callback : function() {
			navService.navToLabelingPage();
		}
	});

	// proceed to the labeling page
	$scope.startLabeling = function(image) {
		sessionService.touchImage($scope.projectID, image.id, function(data){
			$log.debug("Successfully touched image " + image.id);
			navService.navToLabelingPage($scope.projectID, image.id);
		}, function(data,status){
			sharedService.addAlert("Cannot update image. Error " + status + ".", "danger");
		});
	};

	$scope.annotationGallery = function(image){
		if (image.numberOfAnnotations=='0') {
			return;
		}

		sessionService.touchImage($scope.projectID, image.id, function(data){
			// handle success promise
			// forward to the annotation gallery of this image
			$log.debug("successfully touched image " + image.id);
			// navigate to annotation gallery of selected image (predefine filter)
			navService.navToAnnotationGallery(image.id);
		}, function(data,status){
			sharedService.addAlert("Cannot update image. Error " + status + ".", "danger");
		})
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
						message : "This project does not have any images.",
				};
				$scope.loading = false;
				return;
			} else {
				delete $scope.image.error;
			}

			$log.debug("hideCompleted (local): " + sessionService.getCurrentProject()['hideCompletedImages']);

			// get user preferences from session
			$scope.hide = (sessionService.getCurrentProject()['hideCompletedImages'] === true);

			// build the data table
			$scope.tableParams = new ngTableParams(
					{
						// define the parameters
						page : 1, // show first page
						count : 10, // count per page
						sorting : {
							// initial sorting
							userProgress : 'desc'
						},
						filter : {
							// applies filter to the "data" object before sorting
						}						
					}, {
						// compute the pagination view
						total : $scope.image.images.length, // number of images
						getData : function($defer, params) {
							// use build-in angular filter
							var newData = $scope.image.images;
							
							// fixed sorting for images with no annoations
							params.sorting()['numberOfAnnotations'] = params.sorting()['userProgress'];
							
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
						filterDelay : 0,
					});

			$log.info("image refresh successful");
			// hide or show the completed images
			$scope.hideCompleted($scope.hide)
			$scope.loading = false;
		}, function(data, status) {
			// image fetching failed
			$scope.image.error.retrieve = {
					status : status,
					message : data.error.message
			};
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
	$scope.rowClass = function(progress) {
		if (progress == 100){
			return "success";
		}
	};

	$scope.addKeys = function(url){
		return cytomineService.addKeys(url);
	}

	getBtnAllProgresses = function() {
		return document.getElementById("allProgresses");
	};

	getBtnHideFinishedProgresses = function() {
		return document.getElementById("hideCompleted");
	};

//	post the update to the server
	$scope.hideCompleted = function(hideCompleted){
		// POSTING UPDATE TO SERVER
		var cPrj = sessionService.getCurrentProject();
		cPrj['hideCompletedImages'] = hideCompleted;

		// possible BUG in updating the project! sometimes the argument is the input DOM element!!
		sessionService.updateProject(cPrj);

		if (hideCompleted == false){
			$timeout(function() {
				$log.info("showing all progresses")
				getBtnAllProgresses().click();
				$scope.hide = false;
			},
			1);
		}else {
			$timeout(function() {
				$log.info("showing only incomplete progresses")
				getBtnHideFinishedProgresses().click();
				$scope.hide = true;
			},
			1);
		}

//		$log.debug("hideCompleted (scope after update): " + $scope.hide);

	};
}]);
