/**
 * Created by lrollus on 7/14/14.
 */
var iris = angular.module("irisApp");

iris.constant("imageUrl", "/api/project/{id}/images.json");
iris
		.constant(
				"imageServerURLs",
				"/api/abstractimage/{abstractImageID}/imageservers.json?imageinstance={imageInstanceID}");

iris.factory("imageService", function($http, $log, imageUrl, imageServerURLs,
		cytomineService) {

	// cached object for the images
	var images = [];

	return {

		// set the current image
		setCurrentImage : function(image) {
			localStorage.setItem("currentImage", JSON.stringify(image));
		},

		// get the current image
		getCurrentImage : function() {
			return JSON.parse(localStorage.getItem("currentImage"));
		},

		// remove the current image
		removeImageID : function() {
			localStorage.removeItem("currentImage");
		},

		// get all cached images
		getAllImages : function() {
			return images;
		},

		// get the images
		fetchImages : function(projectID, callbackSuccess, callbackError) {
			var url = cytomineService.addKeys(imageUrl).replace("{id}",
					projectID);

			// execute the get request to the server
			$http.get(url).success(function(data) {
				images = data;
				if (callbackSuccess) {
					callbackSuccess(data);
				}
			}).error(function(data, status, headers, config) {
				if (callbackError) {
					callbackError(data, status);
				}
			})
		},

		// get the image server URLs for the OpenLayers map viewer
		fetchImageServerURLs : function(abstractImageID, imageInstanceID,
				callbackSuccess, callbackError) {
			var url = cytomineService.addKeys(imageServerURLs.replace(
					"{abstractImageID}", abstractImageID).replace(
					"{imageInstanceID}", imageInstanceID));

			// execute the get request to the server
			$http.get(url).success(function(data) {
				// TODO pass the URL array to the callback and 
				// execute the random selection in the custom getURL function of openlayers
				//////////////////////////////////////
				var urls = data.imageServersURLs;
				//console.log(urls)
				var randomServer = urls[Math.round(Math.random()*urls.length)];
				console.log(randomServer)
				//////////////////////////////////////
				
				if (callbackSuccess) {
					callbackSuccess(data);
				}
			}).error(function(data, status, headers, config) {
				if (callbackError) {
					callbackError(data, status);
				}
			})
		}
	};
});