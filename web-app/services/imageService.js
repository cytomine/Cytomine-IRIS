/**
 * Created by lrollus on 7/14/14.
 */
angular.module("irisApp")
    .constant("imageUrl", "/api/project/{id}/image.json")
    .factory("imageService",function($http,imageUrl,cytomineService) {

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
            getImagesFromProject : function(idProject,callbackSuccess, callbackError) {
                $http.get(cytomineService.addKeys(imageUrl).replace("{id}",idProject))
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