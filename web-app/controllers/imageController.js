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
				if (image.numberOfAnnotations=='0') {
					return;
				}
				
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
			
			// get all images (limited by offset and max) for the current project 
			$scope.getImages = function(offset, max, callbackSuccess) {
				imageService.fetchImages($scope.projectID, true, function(data) {
					callbackSuccess(data);
				}, function(data, status) {
					$scope.image.error.retrieve = {
						status : status,
						message : data.error.message
					};
					$scope.loading = false;
				}, 
				offset, // offset 
				max); // max
			};
			
			// pagination settings
			$scope.pagination = {
				global : {
					itemsPerPage : 6,
				}	
			};
			
			// resolves the JSON object into a table with pagination
			$scope.resolveImagePages = function(){
//				console.log("hideCompleted (session): " + sessionService.getCurrentProject().prefs['images.hideCompleted']);
				
				// get user preferences from session
				$scope.hide = (sessionService.getCurrentProject().prefs['images.hideCompleted'] === 'true');
				
				// build the data table
				$scope.tableParams = new ngTableParams(
				{
					// define the parameters
					page : 1, // show first page
					count : $scope.pagination.global.itemsPerPage, // count per page 
					sorting : {
						 // initial sorting
						originalFilename : 'asc',
//						userProgress : 'asc'
					},
					filter : {
						// applies filter to the "data" object before sorting
					}						
				}, {
					counts : [], // deactivate the 'counts' on each page in the table
					
					// function to get the data
					getData : function($defer, params) {
						var pageToFetch = params.page();

						$log.debug("Fetching page #" + pageToFetch);
						
						// show the loading button
						$scope.loading = true;
						
						// compute offset and max
						var max = $scope.pagination.global.itemsPerPage
						var offset = (pageToFetch-1)*max;
						
						console.time('loading images');
						
						$scope.getImages(
								offset, 
								max, 
								function(data) {
							console.timeEnd('loading images');
							
							// SUCCESS PROMISE
							// get the meta information
							$scope.image.images = data.images;
							$scope.image.currentPage = data.currentPage;
							$scope.image.pageItems = data.pageItems;
							$scope.image.total = data.totalItems;
							$scope.image.pageItemMin = offset + 1;
							$scope.image.pageItemMax = offset + data.pageItems;
							
							if (data.totalItems < 1){
								$scope.image.error.empty= {
										message : "This project does not have any images.",
									};
								$scope.loading = false;
								return;
							} else {
								delete $scope.image.error;
							}
							
							var newData = $scope.image.images;
							
							// filter and order the data according to the defined config objects
							newData = params.filter() ? $filter('filter')(newData,
									params.filter()) : newData;
							newData = params.sorting() ? $filter('orderBy')(
									newData, params.orderBy()) : newData;
								
							$scope.data = newData;
							params.total($scope.image.total); // set total for pagination
							
							// provide the data to the view
							$defer.resolve($scope.data);
							
							//hide or show the completed images
//							$scope.hideCompleted($scope.hide) 
							$scope.loading = false;
							$log.info("Image page fetching successful.");
						});
					},
					filterDelay : 0,
				});
				
			}
			
			// refresh the page
			$scope.refreshPage = function(){
				$scope.resolveImagePages();
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
