var iris = angular.module("irisApp");

iris.controller("navCtrl", function($scope, $window, $route, $location, $log, sharedService, navService,
		projectService, helpService, sessionService, imageService) {
	console.log("navCtrl");

	// navigation active tab controller
	$scope.isActive = function(viewLocation) {
		// console.log($location.path())

		// full match
		if ($location.path() === viewLocation) {
			return true;
		}
		// partial (suffix) match
		else if (sharedService.strEndsWith($location.path(), viewLocation)) {
			return true;
		}
		// partial (suffix) match
		else if (sharedService.strContains($location.path(), viewLocation)) {
			return true;
		}
		// no match
		else {
			return false;
		}
	};

	// navigate to the current annotation for labeling
	$scope.labeling = function() {
		// then get the current project ID and image ID
		var pID = sessionService.getCurrentProject().cmID;
		var iID = sessionService.getCurrentImage().cmID;
	
		navService.navToLabelingPage(pID, iID);
	};
	
	// navigate to the available projects
	$scope.projects = function() {
		navService.navToProjects();
	};
	
	// navigate to the current project's images
	$scope.images = function() {
		navService.navToImages();
	};
	
	// navigate to the current project's annotations
	$scope.annotations = function() {
		navService.navToAnnotationGallery();
	};
	
	// show the help page
	$scope.showHelp = function() {
		helpService.showHelp();
	}
});
