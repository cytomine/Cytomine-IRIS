var iris = angular.module("irisApp");

iris.config(function($logProvider) {
	$logProvider.debugEnabled(true);
});

iris.controller(
		"imageCtrl",
		function($rootScope, $scope, $http, $filter, 
				$document, $timeout, $location, 
				$routeParams, $log, hotkeys,
				projectService, imageService, sessionService, 
				helpService, sharedService, navService, annotationService, 
				ngTableParams) {
			console.log("imageCtrl");
			
			// retrieve the project parameter from the URL
			$scope.projectID = $routeParams["projectID"];
			
			// look up in IRIS, if the requested project is available
			// for this user.
			projectService.checkAvailability($scope.projectID, function(data){
				// the success
				$log.debug("project is available!");
			}, function(data, status){
				// the error
				$log.error("project is not available!");
			})
			
			// set content url for the help page
			helpService.setContentUrl("content/help/imageHelp.html");
			
			// get the current date as long
			$scope.today = sharedService.today;
			
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
			
			// proceed to the labeling page
			$scope.startLabeling = function(image) {
				sessionService.touchImage($scope.projectID, image.id, function(data){
					$log.debug("Successfully touched image " + image.id);
					navService.navToLabelingPage($scope.projectID, image.id);
				}, function(data,status){
					sharedService.addAlert("Cannot update image. Error " + status + ".", "danger");
				});
			};
			
			$scope.annotationGallery = function(image){
				sessionService.touchImage($scope.projectID, image.id, function(data){
					// handle success promise
					// forward to the annotation gallery of this image
					$log.debug("successfully touched image " + image.id);
					navService.navToAnnotationGallery();
				}, function(data,status){
					sharedService.addAlert("Cannot update image. Error " + status + ".", "danger");
				})
			};
			
			$scope.navToProjects = function() {
				navService.navToProjects();
			};
			
			// get all the images for the current project 
			$scope.getAllImages = function(callbackSuccess) {
				imageService.fetchImages($scope.projectID, true, function(data) {
					callbackSuccess(data);
				}, function(data, status) {
					$scope.image.error.retrieve = {
						status : status,
						message : data.error.message
					};
					$scope.loading = false;
				});
			};
			
			// refresh the page
			$scope.refreshPage = function(){
				$scope.loading = true;
				console.time('loading all images');
				$scope.getAllImages(function(data) {
					console.timeEnd('loading all images');
					
					$scope.image.images = data; // this should be an IRIS image list
					$scope.image.total = data.length;
					
					if (data.length < 1){
						$scope.image.error.empty= {
								message : "This project does not have any images.",
							};
						$scope.loading = false;
						return;
					} else {
						delete $scope.image.error;
					}
					
//					$log.debug(data)
		    		// #######################
		    		// TODO PAGINATION
		    		// #######################
//					console.log("hideCompleted (session): " + sessionService.getCurrentProject().prefs['images.hideCompleted']);
					
					// get user preferences from session
					$scope.hide = (sessionService.getCurrentProject().prefs['images.hideCompleted'] === 'true');
					
					// build the data table
					$scope.tableParams = new ngTableParams(
					{
						// define the parameters
						page : 1, // show first page
						count : 10, // count per page
						sorting : {
							 // initial sorting
							originalFilename : 'asc',
//							userProgress : 'asc'
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
						},
						filterDelay : 0,
					});
					$log.info("image refresh successful");
					
					// hide or show the completed images
					$scope.hideCompleted($scope.hide)
					$scope.loading = false;
				});
			};

			// execute actual image loading at startup
			$scope.refreshPage();
			
			// //////////////////////////////////////////
			// declare additional methods
			// //////////////////////////////////////////
			$scope.touchImage = function(image){
				// update the image in the session
				sessionService.touchImage($scope.projectID, image.id, function(data){
					// handle success promise
					$log.debug("Successfully touched image " + image.id);
				}, function(data,status){
					sharedService.addAlert("Cannot touch image. Error " + status + ".", "danger");
				});
			}
			
			// Determine the row's background color class according 
			// to the current labeling progress.
			$scope.rowClass = function(progress) {
				if (progress == 100){
					return "success";
				}
			};
			
			getBtnAllProgresses = function() {
				return document.getElementById("allProgresses");
			};
			
			getBtnHideFinishedProgresses = function() {
				return document.getElementById("hideCompleted");
			};
			
			// post the prefs update to the server
			$scope.hideCompleted = function(hideCompleted){
				
//				console.log(typeof hideCompleted)
				
				// POSTING UPDATE TO SERVER
				var cPrj = sessionService.getCurrentProject();
				cPrj.prefs['images.hideCompleted'] = hideCompleted;
				sessionService.updateProject(cPrj)
				
				if (hideCompleted == false){
					$timeout(function() {
						console.log("showing all progresses")
						getBtnAllProgresses().click();
						$scope.hide = false;
						},
						1);
				}else {
					$timeout(function() {
						console.log("filtered progresses")
						getBtnHideFinishedProgresses().click();
						$scope.hide = true;
						},
						1);
				}
				
//				console.log("hideCompleted (scope after update): " + $scope.hide);
				
			};
		});
