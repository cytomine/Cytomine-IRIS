/**
 * Created by lrollus on 7/14/14.
 */
var iris = angular.module("irisApp");

iris.constant("imageURL", "/api/project/{id}/images.json");
iris.constant(
				"imageServerURLs",
				"/api/abstractimage/{abstractImageID}/imageinstance/{imageInstanceID}/imageservers.json");

iris.factory("imageService", function($http, $log, imageURL, imageServerURLs,
		cytomineService) {

	return {
		
		// get the images for a project
		fetchImages : function(projectID, callbackSuccess, callbackError) {
			var url = cytomineService.addKeys(imageURL).replace("{id}",
					projectID);

			// execute the get request to the server
			$http.get(url).success(function(data) {
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