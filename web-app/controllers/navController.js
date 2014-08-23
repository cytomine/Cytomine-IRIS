var iris = angular.module("irisApp");

iris.constant("labelUrl", "/project/{projectID}/image/{imageID}/label");

iris.controller("navCtrl", function($scope, $location, $log, sharedService,
		projectService, helpService, imageService, labelUrl) {
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
		// no match
		else {
			return false;
		}
	};

	// navigate to the current image for labeling
	$scope.labeling = function() {
		var pID = projectService.getProjectID();
		var iID = imageService.getImageID();
		var url = labelUrl.replace("{projectID}", pID)
				.replace("{imageID}", iID);

		$location.url(url)
	};
	
	// show the help page
	$scope.showHelp = function() {
		helpService.showHelp();
	}
});
