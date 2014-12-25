/**
 * Created by lrollus on 7/14/14.
 */
var iris = angular.module("irisApp");

iris.constant("imageURL", "api/project/{projectID}/images.json");
iris.constant("imageServerURLs",
				"api/abstractimage/{abstractImageID}/imageinstance/{imageInstanceID}/imageservers.json");

iris.factory("imageService", [
	"$http", "$log", "imageURL", "imageServerURLs", "cytomineService", 
	function($http, $log, imageURL, imageServerURLs, cytomineService) {

	return {
		
		// get the images with their progress for a project
		fetchImages : function(projectID, computeProgress, callbackSuccess, callbackError, offset, max) {
			var url = cytomineService.addKeys(imageURL).replace("{projectID}",
					projectID);
			
			url += "&computeProgress=" + computeProgress;
			
			// add pagination parameters
			if (offset) {
				// 0 offset is counted as missing parameter and causes loading of first page
				url += "&offset=" + offset;
			}
			if (max) {
				url += "&max=" + max;
			}

			// execute the get request to the server
			$http.get(url).success(function(data) {
				if (callbackSuccess) {
					callbackSuccess(data);
				}
			}).error(function(data, status, headers, config) {
				if (callbackError) {
					callbackError(data, status);
				}
			});
		},

		// get the image server URLs for the OpenLayers map viewer
		fetchImageServerURLs : function(abstractImageID, imageInstanceID,
				callbackSuccess, callbackError) {
			var url = cytomineService.addKeys(imageServerURLs.replace(
					"{abstractImageID}", abstractImageID).replace(
					"{imageInstanceID}", imageInstanceID));

			// execute the get request to the server
			$http.get(url).success(function(data) {
				//////////////////////////////////////
				var urls = data.imageServersURLs;
				//console.log(urls)
				var randomServer = urls[Math.round(Math.random()*urls.length)];
				//////////////////////////////////////
				
				if (callbackSuccess) {
					callbackSuccess(data);
				}
			}).error(function(data, status, headers, config) {
				if (callbackError) {
					callbackError(data, status);
				}
			});
		},
	};
}]);