var iris = angular.module("irisApp");

iris.config(function($logProvider) {
	$logProvider.debugEnabled(true);
});

iris.controller(
		"imageCtrl",
		function($scope, $http, $filter, $location, imageService, $routeParams, $log, hotkeys,
				projectService, helpService, sharedService, annotationService, ngTableParams) {
			console.log("imageCtrl");
			
			// set content url for the help page
			helpService.setContentUrl("content/help/imageHelp.html");

			$scope.image = {
				stillNew : (21 * (24 * 60 * 60 * 1000)), // last 21 days
				error : {}
			};
			
			// put all valid shortcuts for this page here
			hotkeys.bindTo($scope)
			.add({
				combo : 'h',
				description : 'Show help for this page',
				callback : function() {
					helpService.showHelp();
				}
			});
			
			// get the current date as long
			$scope.today = sharedService.today;
			
			// retrieve the parameter from the URL
			$scope.projectID = $routeParams["projectID"];
			
			// get all the images for a project ID
			$scope.getAllImages = function(projectID, callbackSuccess) {
				imageService.fetchImages(projectID, function(data) {
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
			
			// blind the image name
			$scope.blindNames = function(projectID, data){
				// get the project list
				projectService.getAllProjects(function(pList){
					// modify the original file name
					pList.forEach(function(p){
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
					$log.error("Damn, error blinding names :(")
				});
			};
			
			// refresh the page
			$scope.refreshPage = function(){
				$scope.loading = true;
				
				$scope.getAllImages($scope.projectID, function(data) {
					$scope.image.error.retrieve = null;
					$scope.image.images = data;
					$log.info("image refresh successful");

					// build the data table
					$scope.tableParams = new ngTableParams(
					{
						// define the parameters
						page : 1, // show first page
						count : 10, // count per page
						sorting : {
							userProgress : 'asc' // initial sorting
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
							params.total(newData.length); // set total for recalc pagination
							
							$defer.resolve($scope.data);
						}
					});
					$scope.loading = false;
				});
			};

			// execute actual image loading at startup
			$scope.refreshPage();
			
			// //////////////////////////////////////////
			// declare additional methods
			// //////////////////////////////////////////
			// set the current image ID
			$scope.setImageID = function(imageID) {
				imageService.setImageID(imageID);
				$scope.$emit("currentImageID", imageID);
				$scope.$broadcast("currentImageID", imageID);
			};

			// retrieve the current image ID
			$scope.getImageID = function() {
				return imageService.getImageD();
			};
			
			// removes the current image ID
			$scope.removeImageID = function() {
				$scope.setImageID(null);
				imageService.removeImageID();
			};

			// Determine the row's background color class according 
			// to the current labeling progress.
			$scope.rowClass = function(progress) {
				if (progress == 100){
					return "success";
				}
			};
		});
