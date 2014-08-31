var iris = angular.module("irisApp");

iris.constant("labelUrl", "/project/{projectID}/image/{imageID}/label/{annID}");

iris.controller("navCtrl", function($scope, $location, $log, sharedService,
		projectService, helpService, imageService, labelingService, labelUrl) {
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

	// navigate to the current image for labeling
	$scope.labeling = function() {
		// TODO update the current status of the user and retrieve the 
		// UserSession object
		
		// then get the current project ID and the status
		
		var pID = projectService.getCurrentProject().id;
		var iID = imageService.getCurrentImage().id;
		var annID = 136334701;// labelingService.getNextAnnotation().id;
		var url = labelUrl.replace("{projectID}", pID)
				.replace("{imageID}", iID).replace("{annID}",annID);

		$location.url(url)
	};
	
	// navigate to the current project's images
	$scope.images = function() {
		var projectID = projectService.getCurrentProject().id;
		$location.url("/project/" + projectID + "/images");
	};
	
	// show the help page
	$scope.showHelp = function() {
		helpService.showHelp();
	}
});
