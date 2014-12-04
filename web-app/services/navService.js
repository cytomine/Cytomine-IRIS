var iris = angular.module("irisApp");

/**
 * This service provides navigation functionality for the client.
 */
iris.factory("navService", function($http, $rootScope, $location, $window,
		$log, cytomineService, sessionService) {

	return {

		navToLabelingPage : function(projectID, imageID) {
			var url = "/project/" + projectID + "/image/" + imageID + "/label";

			$location.path(url, false);
			console.log($location.absUrl());
			$window.location.href = $location.absUrl();
			$window.location.reload();
		},

		navToProjects : function() {
			$location.url("/projects");
		},

		navToImages : function() {
			var projectID = sessionService.getCurrentProject().cmID;
			if (projectID == null || projectID == undefined) {
				$location.url("/projects");
			} else {
				$location.url("/project/" + projectID + "/images");
			}
		},

		navToAnnotationGallery : function() {
			var projectID = sessionService.getCurrentProject().cmID;
			if (projectID == null || projectID == undefined) {
				$location.url("/projects");
			} else {
				$location.url("/project/" + projectID + "/gallery");
			}
		},
	};

});