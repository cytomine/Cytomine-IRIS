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
				helpService, sharedService, annotationService, 
				ngTableParams) {
			console.log("imageCtrl");
			
			// retrieve the project parameter from the URL
			$scope.projectID = $routeParams["projectID"];
			
			// TODO look up in IRIS, if the requested project is available
			// for this user.
			projectService.checkAvailability($scope.projectID, function(data){
				// the success
				$log.debug("project is available!");
			}, function(data, status){
				// the error
				$log.error("project is not available!");
			})
			
			// TODO fetch a session for the user
//			sessionService.fetchSession();
			
			
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
			
//			// proceed to the labeling page and resume labeling
//			$scope.resumeLabeling = function(image) {
//				sessionService.setCurrentImage(image);
//				// TODO load the "next" annotation from the session and 
//				// append it to the URL
//				$location.url("/project/"+$scope.projectID+"/image/"+image.id+"/label");
//			};
			
			// proceed to the labeling page
			$scope.startLabeling = function(image) {
				sessionService.touchImage($scope.projectID, image.id, function(data){
					$log.debug("Successfully touched image " + image.id);
					$location.url("/project/"+$scope.projectID+"/image/"+image.id+"/label");
				}, function(data,status){
					sharedService.addAlert("Cannot update image. Error " + status + ".", "danger");
				})
			};
			
			$scope.annotationGallery = function(image){
				sessionService.touchImage($scope.projectID, image.id, function(data){
					// handle success promise
					// TODO forward to the annotation gallery of this image
					$log.debug("successfully touched image " + image.id);
				}, function(data,status){
					sharedService.addAlert("Cannot update image. Error " + status + ".", "danger");
				})
			};
			
			// get all the images for the current project 
			$scope.getAllImages = function(callbackSuccess) {
				imageService.fetchImages($scope.projectID, function(data) {
					callbackSuccess(data);
				}, function(data, status) {
					$scope.image.error.retrieve = {
						status : status,
						message : data.errors
					};
					$scope.loading = false;
				});
			};
			
			// refresh the page
			$scope.refreshPage = function(){
				$scope.loading = true;
				
				$scope.getAllImages(function(data) {
					$scope.image.error.retrieve = null;
					$scope.image.images = data; // this should be an IRIS image list
					
//					$log.debug(data)
					
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
