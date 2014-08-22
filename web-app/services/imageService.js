/**
 * Created by lrollus on 7/14/14.
 */
var iris = angular.module("irisApp");

iris.constant("imageUrl", "/api/project/{id}/images.json");

iris.factory("imageService",function($http, $log, imageUrl, cytomineService) {

        var images=[];

        return {
        	
        	// set the current image ID
        	setImageID : function(imageID) {
				localStorage.setItem("imageID", imageID);
			},
			
			// get the current image ID
			getImageID : function() {
				return localStorage.getItem("imageID");
			},
			
			// remove the current image ID
			removeImageID : function() {
				localStorage.removeItem("imageID");
			},
			
			// get all cached images
            allImages : function() {
                return images;
            },

            // get the images
            getImagesFromProject : function(idProject, callbackSuccess, callbackError) {
            	var url = cytomineService.addKeys(imageUrl).replace("{id}", idProject);
            	
                $http.get(url)
                    .success(function (data) {
                        images = data;
                        if(callbackSuccess) {
                            callbackSuccess(data);
                        }
                    })
                    .error(function (data, status, headers, config) {
                        if(callbackError) {
                            callbackError(data,status);
                        }
                    })
            }
        };
    });