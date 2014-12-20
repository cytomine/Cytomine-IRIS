var iris = angular.module("irisApp");

/**
 * This service provides navigation functionality for the client.
 */
iris.factory("navService", function($http, $rootScope, $location, $window,
		$log, cytomineService, sessionService, sharedService) {

	return {

		navToProjects : function() {
			$location.url("/projects");
		},

		navToImages : function() {
			try {
				var projectID = sessionService.getCurrentProject().cmID;
				$location.url("/project/" + projectID + "/images");
			} catch (e) {
				this.fallbackToProjects();
			}
		},
		
		navToLabelingPage : function() {
			try {
				//get the current project ID and image ID
				var pID = sessionService.getCurrentProject().cmID;
			} catch (e) {
				this.fallbackToProjects();
				return;
			}
			try {
				var iID = sessionService.getCurrentImage().cmID;
				var url = "/project/" + pID + "/image/" + iID + "/label";
	
				$location.path(url, false);
				console.log($location.absUrl());
				$window.location.href = $location.absUrl();
				$window.location.reload();
			} catch (e) {
				this.fallbackToImages();
			}
		},

		navToAnnotationGallery : function(imageID) {
			try {
				var projectID = sessionService.getCurrentProject().cmID;
				
				if (imageID){
					$location.url("/project/" + projectID + "/image/" + imageID + "/gallery");
				} else {
					$location.url("/project/" + projectID + "/gallery");
				}
			} catch (e) {
				this.fallbackToProjects()
			}
		},
		
		fallbackToProjects : function() {
			sharedService.addAlert("There is no project selected yet. " +
					"Open a project from the list!", "info");
			this.navToProjects();
		}, 
		
		fallbackToImages : function() {
			sharedService.addAlert("There is no image selected yet. " +
					"Choose an image from the image list!", "info");
			this.navToImages();
		}
	};

});