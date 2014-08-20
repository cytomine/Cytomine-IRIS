var iris = angular.module("irisApp");

iris.config(function($logProvider) {
	$logProvider.debugEnabled(true);
});

iris.controller(
		"imageCtrl",
		function($scope, $http, $filter, $location, imageService, $routeParams,
				projectService, ngTableParams) {
			console.log("imageCtrl");

			$scope.image = {
				error : {}
			};
			
			// retrieve the parameter from the URL
			$scope.idProject = $routeParams["idProject"];
			
			// get all the images for a project ID
			$scope.getAllImages = function(projectID, callbackSuccess) {
				imageService.getImagesFromProject(projectID, function(data) {
					// change the image names if the project is in blind mode
					$scope.blindNames(projectID, data);
					callbackSuccess(data);
				}, function(data, status) {
					$scope.image.error.retrieve = {
						status : status,
						message : data.errors
					};
					$scope.loading = false;
				});
			};

			// execute actual image loading
			$scope.loading = true;
			$scope.getAllImages($scope.idProject, function(data) {
				$scope.image.error.retrieve = null;
				$scope.image.images = data;

				// build the data table
				$scope.tableParams = new ngTableParams(
				{
					// define the parameters
					page : 1, // show first page
					count : 10, // count per page
					sorting : {
						numberOfAnnotations : 'desc' // initial sorting
					},
					filter : {
						// applies filter to the "data" object before sorting
					}
				}, {
					// compute the pagination view
					total : $scope.image.images.length, // number of images
					getData : function($defer, params) {
						// use build-in angular filter
						var newData = $scope.image.images;
						// use build-in angular filter
						newData = params.filter() ? $filter('filter')(newData,
								params.filter()) : newData;
						newData = params.sorting() ? $filter('orderBy')(
								newData, params.orderBy()) : newData;
						
						$scope.data = newData.slice((params.page() - 1)
								* params.count(), params.page()
								* params.count());
						params.total(newData.length); // set total for recalc
						// pagination
						
						$defer.resolve($scope.data);
						$scope.loading = false;
					}
				});
			});
			
			// blind the image name
			$scope.blindNames = function(projectID, data){
				// get the project list
				projectService.getAllProjects(function(pList){
					// modify the original file name
					pList.forEach(function(p){
						console.log(projectID);
						if (p.id == projectID){
							data.forEach(function(item){
								if (p.blindMode === true){
									item.fileName = "[BLIND]" + item.id;
								} else {
									item.fileName = item.originalFilename;
								}
							});
						}
					});
				},function(header, error){
					console.log("damn, error blinding names :(")
				});
				
			}

			// //////////////////////////////////////////
			// declare additional methods
			// //////////////////////////////////////////

			// set the current image ID
			$scope.setImageID = function(imageID) {
				imageService.setImageID(imageID);
				$scope.$emit("currentImageID", imageID);
				$scope.$broadcast("currentImageID", imageID);
			}

			// retrieve the current image ID
			$scope.getImageID = function() {
				return imageService.getImageD();
			}
			
			// removes the current image ID
			$scope.removeImageID = function() {
				$scope.setImageID(null);
				imageService.removeImageID();
			}

		});
