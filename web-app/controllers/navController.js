var iris = angular.module("irisApp");

iris.constant("labelUrl", "/project/{projectID}/image/{imageID}/label/");

iris.controller("navCtrl", function($scope, $location, $log, sharedService,
		projectService, helpService, sessionService, imageService, labelingService, labelUrl) {
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
		//var annID = 136334701;// labelingService.getNextAnnotation().id;
		var url = labelUrl.replace("{projectID}", pID)
				.replace("{imageID}", iID);

		$location.url(url)
	};
	
	// navigate to the current project's images
	$scope.images = function() {
		var projectID = sessionService.getCurrentProject().cmID;
		// TODO check for null in the project
		$location.url("/project/" + projectID + "/images");
	};
	
	// show the help page
	$scope.showHelp = function() {
		helpService.showHelp();
	}
});
