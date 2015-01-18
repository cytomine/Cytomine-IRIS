var iris = angular.module("irisApp");

/**
 * This service provides navigation functionality to the client.
 */
iris.factory("navService", [
"$http", "$rootScope", "$location", "$window",
"$log", "cytomineService", "sessionService", "sharedService",
	//"resumeAtImagesURL", "resumeAtLabelingURL", "resumeAtGalleryURL",
	"$routeParams",
	    function($http, $rootScope, $location, $window,
		$log, cytomineService, sessionService, sharedService,
		$routeParams) {

	return {
		// 1. reconstruct the URL from the request
		// 2. query the session on the server
		// 3. handle response and redirect to correct fallback page
		navToProjects : function() {
			// get the session
			sessionService.fetchSession();
			$location.url("/projects");
		},

		navToImages : function() {
			var navService = this;

			// get the session
			sessionService.fetchSession(
				function(session){
					// status 200, response will be an IRISUserSession JSON object
					var projectID = session.currentCmProjectID;
					if (projectID == null || projectID == undefined){
						navService.fallbackToProjects();
					} else {
						$location.url("/project/" + projectID + "/images");
					}
				}, function(data, status){
					// status !200
					$log.error(data);
					navService.analyzeErrorStatus(status);
			});
		},

		resumeAtLabelingPage : function(){
			var navService = this;

			sessionService.fetchSession(function(session){
				// status 200, response will be an IRISUserSession JSON object with additional information on
				// the image and current annotation
				var projectID = session.currentCmProjectID;

				if (projectID == null || projectID == undefined){
					navService.fallbackToProjects();
				} else {
					if (session.currentProject.currentImage == null || session.currentProject.currentImage == undefined){
						navService.fallbackToImages();
					} else {
						var imageID = session.currentProject.currentImage.cmID;
						var annotationID = session.currentProject.currentImage.settings.currentCmAnnotationID;

						// store the current image in local storage and then forward
						sessionService.openImage(projectID, imageID, function (image) {
							// url computation complete
							$log.debug("URL computation complete.");
							navService.navToLabelingPage(projectID, imageID, annotationID);
						}, function (data, status) {
							navService.analyzeErrorStatus(status);
						});
					}
				}
			}, function(data, status){
				// status !200
				$log.error(data);
				navService.analyzeErrorStatus(status);
			});
		},

		resumeImageAtLabelingPage : function(projectID, imageID){
			var navService = this;

			// store the current image in local storage and then forward
			sessionService.openImage(projectID, imageID, function (image) {
				// url computation complete
				$log.debug("URL computation complete.");

				navService.navToLabelingPage(projectID, imageID, image.settings.currentCmAnnotationID);
			}, function (data, status) {
				navService.analyzeErrorStatus(status);
			});
		},
		
		navToLabelingPage : function(projectID, imageID, annotationID) {
			var navService = this;

			// user has no project opened
			if (projectID == null || projectID === undefined){
				navService.fallbackToProjects();
			} else {
				if (imageID == null || imageID === undefined) {
					navService.fallbackToImages();
				} else {
					if (annotationID == null || annotationID === undefined){
						// start at the beginning
						var lblUrl = "/project/" + projectID + "/image/" + imageID + "/label";
					} else {
						var lblUrl = "/project/" + projectID + "/image/" + imageID + "/label/" + annotationID;
					}

					// ###### IMPORTANT NOTE ######
					// DUE TO A CURRENT BUG IN THE OL3 SYSTEM
					// WE HAVE TO ENTIRELY RELOAD THE PATH
					//$location.url(lblUrl);
					$location.path(lblUrl, false);
					$window.location.href = $location.absUrl();
					$window.location.reload();
				}
			}
		},

		navToAnnotationGallery : function(projectID, imageID) {
			var navService = this;

			if (projectID == null || projectID === undefined){
				navService.fallbackToProjects();
			} else {
				if (imageID){
					$location.url("/project/" + projectID + "/image/" + imageID + "/gallery");
				} else {
					$location.url("/project/" + projectID + "/gallery");
				}
			}
		},

		resumeAtAnnotationGallery : function(){
			var navService = this;

			// get the session
			sessionService.fetchSession(function(session){
				// status 200, response will be an IRISUserSession JSON object
				var projectID = session.currentCmProjectID;
				if (projectID == null){
					navService.fallbackToProjects();
				} else {
					$location.url("/project/" + projectID + "/gallery");
				}
			}, function(data, status){
				// status !200
				$log.error(data);
				navService.analyzeErrorStatus(status);
			});
		},
		
		fallbackToProjects : function() {
			sharedService.addAlert("There is no project selected yet. " +
					"Open a project from the list!", "info", 7000);
			this.navToProjects();
		}, 
		
		fallbackToImages : function() {
			sharedService.addAlert("There is no image selected yet. " +
					"Choose an image from the image list!", "info", 7000);
			this.navToImages();
		},

		showNotFound : function() {
			$location.url("/404");
		},

		showClientError : function() {
			$location.url("/400");
		},

		showServerError : function() {
			$location.url("/500");
		},

		analyzeErrorStatus : function(status) {
			if (status == 400){
				this.showClientError();
			}else if (status == 404){
				this.showNotFound();
			} else {
				this.showServerError();
			}
		}
	};
}]);