var iris = angular.module("irisApp");

iris.controller(
		"annStatsCtrl", [
"$rootScope", "$scope", "$http", "$filter", "$modal", "$window",
"$document", "$timeout", "$location", "$route", "$q",
"$routeParams", "$log", "hotkeys",
"cytomineService", "projectService", "imageService", "sessionService",
"helpService", "sharedService", "navService", "annotationService", "statisticsService",
"ngTableParams", "downloadService",
function($rootScope, $scope, $http, $filter, $modal, $window,
		$document, $timeout, $location, $route, $q,
		$routeParams, $log, hotkeys,
		cytomineService, projectService, imageService, sessionService,
		helpService, sharedService, navService, annotationService, statisticsService,
		ngTableParams, downloadService) {
	$log.debug("annStatsCtrl");

	// retrieve the project parameter from the URL
	$scope.projectID = $routeParams["projectID"];

	// get the current date as long
	$scope.today = sharedService.today;

	$scope.annstats = {
			stillNew : (21 * (24 * 60 * 60 * 1000)), // last 21 days
			error : {},
			sbcollapsed : false,
			opening : {},
			startup: true,
			slider : {
				options : {
					start: function (event, ui) {
						$log.debug('Slider start');
					},
					stop: function (event, ui) {
						$log.debug('Slider stop');
						if ($scope.annstats.slider.value === $scope.annstats.slider.max){
							$log.debug("reached maximum agreement")
						} else if ($scope.annstats.slider.value === $scope.annstats.slider.min){
							$log.debug("reached minimum agreement, showing all annotations")
						}

						// filter the table
						$scope.tableParams.reload();
						$scope.tableParams.page(1);
					}
				},
				// initial value of the slider
				min : 0,
				max : 0,
				// current slider value
				value : 0
			}
	};

	$scope.setCollapsed = function(flag){
		$scope.annstats.sbcollapsed = flag;
	};

	// selected users for filtering
	var selectedUsers = [];
	// selected terms for filtering
	var selectedTerms = [];
	// selected images for filtering
	var selectedImages = [];

	// react to term filter change events
	$scope.$on("termFilterChange", function(event, object) {
		$log.debug("termFilterChange detected in annStatsCtrl");

		var action = object.action;
		//$log.debug(event.name + "::" + action + ": "  + object.name + " [" + object.id + "].");

		if (action === 'add'){
			// incremental fetching
			// fetch the selected term for the selected images
			selectedTerms.push(object.id);
		} else if (action === 'remove'){
			// remove the selected term
			selectedTerms.splice(selectedTerms.indexOf(object.id), 1);
		} else if (action === 'addAll'){
			// here the id is the entire array
			selectedTerms = object.id;
		} else if (action === 'removeAll'){
			// remove all selected terms
			selectedTerms = [];
		}
		$scope.showOrHideNoLabelWarning();
	});

	$scope.$on("userFilterChange", function(event, object) {
		$log.debug("userFilterChange detected in annStatsCtrl");

		selectedUsers = object.id;

		$scope.showOrHideNoUsersWarning();
	});

	$scope.$on("imageFilterChange", function(event, object) {
		$log.debug("imageFilterChange detected in annStatsCtrl");

		selectedImages = object.id;

		$scope.showOrHideNoImageWarning();
	});

	//$scope.queryStatus = function(){
	//	$log.debug("users : " + selectedUsers);
	//	$log.debug("terms : " + selectedTerms);
	//	$log.debug("images: " + selectedImages);
	//};

	$scope.performQuery = function(){
		$scope.showOrHideNoImageWarning();
		$scope.showOrHideNoLabelWarning();
		$scope.showOrHideNoUsersWarning();

		if (selectedImages.length === 0){
			$log.debug("No images to retrieve statistics for.");
			return;
		}

		if (selectedTerms.length === 0){
			$log.debug("No terms to retrieve statistics for.");
			return;
		}

		if (selectedUsers.length === 0){
			$log.debug("No users to retrieve statistics for.");
			return;
		}


		$log.debug("Fetching user statistics " + selectedUsers +
		" for terms " + selectedTerms
		+ " for " + selectedImages.length + " images.");

		// show the loading button
		$scope.loading = true;
		$scope.annstats.startup = false;

		try {
			// -> evil hack to get back the pagination after refreshing table! <-
			$scope.tableParams.page(-1);
		} catch (ignored){
			//$log.debug("Expected (nullpointer) error on table parameters.");
		}

		statisticsService.fetchAnnotationAgreementList($scope.projectID,
			selectedImages, selectedTerms, selectedUsers, function(data) {

			if (data.annotationStats.length < 1){
				$scope.annstats.error = {
					empty : {
						message : "This query does not result in any annotations."
					}
				};
				$scope.loading = false;
				return;
			} else {
				delete $scope.annstats.error;
			}


			// success
			$scope.annstats.annotations = data.annotationStats; // this should be a list of annotations
			$scope.terms = data.terms; // the term map
			$scope.users = data.users; // the sorted users list
			$scope.annstats.total = data.annotationStats.length;

			// set the slider max value
			$scope.annstats.slider.max = data.nUniqueUsersOverall;
			// set the current value (must not exceed the maximum value)
			if ($scope.annstats.slider.value > $scope.annstats.slider.max){
				$scope.annstats.slider.value = $scope.annstats.slider.max;
			}

			$scope.usercolumns = [];

			// the lookup map of user names
			$scope.usermap = {};

			// loop over the map and construct the dynamic columns
			for (var k = 0; k < data.users.length; k++) {
				var userid = data.users[k].id;
				var username = data.users[k].username;
				$scope.usercolumns.push(
					{
						title: String(username),
						userStats: 'userStats',
						visible: true,
						userID: userid
					}
				);
				$scope.usermap[userid] = {
					'username' : username,
					'firstname': data.users[k].firstname,
					'lastname': data.users[k].lastname
				};
			}

			// build the data table
			$scope.tableParams = new ngTableParams(
					{
						// define the parameters
						page : 1, // show first page
						count : 10, // count per page
						sorting : {
							// initial sorting
						},
						filter : {
							// applies filter to the "data" object before sorting
						}
					}, {
						// compute the pagination view
						total : $scope.annstats.annotations.length, // number of annotations
						getData : function($defer, params) {
							// use build-in angular filter
							var newData = $scope.annstats.annotations;

							var theFinalData = [];

							var nEmpty = 0;
							// search for minimum agreement in the data
							for (var i = 0; i < newData.length; i++){
								var elmnt = newData[i]; // get the annotation data

								// if the assignment has no ranking, i.e. the 'no terms' filter is active
								if (elmnt.assignmentRanking.length === 0){
									// collect the entry only if there is NO agreement filter set as well
									if ($scope.annstats.slider.value === 0) {
										theFinalData.push(elmnt);
										//$log.debug(elmnt);
									}
									nEmpty = nEmpty+1;
								} else {
									// if the highest agreement is in the filter range
									if (elmnt.assignmentRanking[0].nUsers >= $scope.annstats.slider.value) {
										// collect the agreement entry
										theFinalData.push(elmnt);
										//$log.debug(elmnt);
									}
								}
							}

							$log.debug(nEmpty + " annotations without terms.");

                            // if the final data is of the same size of the original, set filtered false
							if (theFinalData.length === $scope.annstats.annotations.length){
								$scope.annstats.filtered = false;
							} else {
								$scope.annstats.filtered = true;
								$scope.annstats.totalFiltered = theFinalData.length;
							}

							// ALL ANNOTATIONS TO BE DISPLAYED
							$scope.annstats.annotationsFiltered = theFinalData;

							$log.debug("Filtered " + theFinalData.length + " annotations for display.");

							// just show elements above a certain agreement
							newData = theFinalData;

							// use build-in angular filter
							newData = params.filter() ? $filter('filter')(newData,
									params.filter()) : newData;
							newData = params.sorting() ? $filter('orderBy')(
									newData, params.orderBy()) : newData;

							$scope.annstats.pageItemMin = (params.page() - 1) * params.count();

							var pageItemMax = params.page() * params.count();
							$scope.annstats.pageItemMax = pageItemMax > newData.length ? newData.length : pageItemMax;

							$scope.data = newData.slice($scope.annstats.pageItemMin, pageItemMax);
							params.total(newData.length); // set total for recalc pagination

							// set the page data for export purpose
							$scope.annstats.annotationsFilteredPage = $scope.data;

							$defer.resolve($scope.data);
						},
						filterDelay : 0
					});

			$log.info("annotations refresh successful");
			// hide or show the completed images
			$scope.loading = false;
		}, function(data, status) {
			// image fetching failed
			$scope.annstats.error = {
				retrieve : {
					status : status,
					message : data.error.message
				}
				};
			$scope.loading = false;
		});
	};

	// perform the query on the refresh button
	$scope.refreshPage = $scope.performQuery;

	$scope.warn = {};

	// warnings/infos
	$scope.showOrHideNoLabelWarning = function(){
		$scope.warn.noLabel = {};

		if (selectedTerms.length === 0){
			$scope.warn.noLabel = {
				message : "There is no label/term selected! " +
				"Please choose at least one from the ontology on the left side, then press REFRESH."
			};
			$log.info("No terms selected, won't fetch any statistics, but show warning.");
		} else {
			$log.info("deleted noLabel warning!");
			delete $scope.warn.noLabel;
		}
	};

	$scope.showOrHideNoImageWarning = function(){
		$scope.warn.noImage = {};
		if (selectedImages.length === 0){
			$scope.warn.noImage = {
				message : "There is no image selected! " +
				"Please choose at least one from the image list on the left side, then press REFRESH."
			};
			$log.info("No image(s) selected, won't fetch any statistics, but show warning.");
		} else {
			delete $scope.warn.noImage;
		}
	};

	$scope.showOrHideNoUsersWarning = function(){
		$scope.warn.noUsers = {};
		if (selectedUsers.length === 0){
			$scope.warn.noUsers = {
				message : "There are no users selected! " +
				"Please choose at least one from the project user list on the left side, then press REFRESH."
			};
			$log.info("No user(s) selected, won't fetch any statistics, but show warning.");
		} else {
			delete $scope.warn.noUsers;
		}
	};


	//////////////////////////////////////////
	// START IMAGE EXPORT MODAL DIALOG
	//////////////////////////////////////////
	$scope.showExportImageDatasetModal = function(){
		$log.debug("showing image dataset export dialog");

		var modalInstance = $modal.open({
			templateUrl: 'exportImageDatasetModal.html',
			controller: modalCtrl,
			size : 'lg',
			resolve: { // put all variables here that should be available in the modal
				project: function () {
					return $scope.projectID;//dlgData.data;
				},
				annstats: function(){
					return $scope.annstats;
				},
                filterActive: function() {
                    return $scope.annstats.filtered;
                },
                tableParams: function() {
                    return $scope.tableParams;
                },
				error: function () {
					return undefined;//dlgData.error;
				}
			}
		});

		modalInstance.result.then(function (result) {
			// callbackSuccess branch
			$log.debug('Export Image Dataset Modal: ' + result);
		}, function (result) {
			// callbackError branch
			$log.debug('Export Image Dataset Modal: ' + result);
		});
	};

	// controller for the image export modal
	var modalCtrl = function ($scope, $window, $modalInstance, $sce, $q, $timeout, project, annstats,
							  filterActive, tableParams, error) {

		$scope.projectID = project;
		$scope.error = error;
        $scope.filterActive = filterActive;

		$scope.imageExportSettings = {
			scope: 'all', // can be one of {all | filtered | currentPage | pages }
			pages: "e.g. 1-3; 5; 10;", // is read, if scope == 'pages'
			minBB: true, // export the minimum bounding box, i.e. just the annotation
			fixedWindow: false, // export a fixed window around the annotations centroid
			fixedWindowDimensions: { width: 100, height: 100 }, // fixed window dimensions
			dynamicWindow: false, // export a dynamic window around the annotation's minimum bounding box
			dynamicWindowBorder: 128, // the border around the annotation's minimum bounding box, is read when dynamicWindow == true
			levels: "0", // level 0 is the original maximum resolution of the image (bottom pyramid level)
			format: "jpg" // format 'jpg' or 'png'
		};

		$scope.exportData = function(){

			var nTotalAnno = annstats.total;
			var nFilteredAnno = annstats.totalFiltered;

			// an array where we collect the annotation IDs to be exported
			var annotationIDs = [];

			if ($scope.imageExportSettings.scope === 'all'){
				// export all annotations, ignore the filter
				for (var i = 0; i < nTotalAnno; i++){
					annotationIDs.push(annstats.annotations[i].cmID);
				}
			} else if ($scope.imageExportSettings.scope === 'filtered'){
				// export all filtered across all pages
				for (var i = 0; i < nFilteredAnno; i++){
					annotationIDs.push(annstats.annotationsFiltered[i].cmID);
				}
			} else if ($scope.imageExportSettings.scope === 'currentPage'){
				// export all annotations from the current page (filtered or not)
				for (var i = 0; i < annstats.annotationsFilteredPage.length; i++){
					annotationIDs.push(annstats.annotationsFilteredPage[i].cmID);
				}
			} else if ($scope.imageExportSettings.scope === 'pages'){
				// export all annotations from the specified pages (filtered or not)
				// resolve the page numbers
				// sanity checks
				var ranges = $scope.imageExportSettings.pages;
				if (ranges === undefined || ranges === null || ranges === ""){
					$scope.pageRangeError = true;
					$scope.pageRangeErrorText = "The range must not be empty";
					return;
				} else {
					delete $scope.pageRangeError;
				}

				// split the string at the ';' character
				//"1-3; 5; 10;"
				var pageRangeArray = String(ranges).split(';');
				$log.debug(pageRangeArray);
				// trim all whitespaces
				pageRangeArray = pageRangeArray.map(Function.prototype.call, String.prototype.trim);
                $log.debug(pageRangeArray);
				// final pages (split and cleaned)
				var pageObject = {};

				// split all ranges
				for (var i = 0; i < pageRangeArray.length; i++){
					var entry = pageRangeArray[i];
                    if (entry === ""){
                        continue;
                    }
					var dash_idx = entry.indexOf('-');
					if (dash_idx !== -1){
						// get the first and last entry
						var first_and_second = entry.split('-');
						first_and_second = first_and_second.map(Function.prototype.call, String.prototype.trim);
						first_and_second = first_and_second.sort();
						if (first_and_second.length !== 2){
							$scope.pageRangeError = true;
							$scope.pageRangeErrorText = "Enter a valid range in the form of e.g. '1-2;3' or '1;3;7'.";
							return;
						} else {
							delete $scope.pageRangeError;
						}
						// both border ranges inclusive
						for (var j = Number(first_and_second[0]); j <= Number(first_and_second[1]); j++){
							pageObject[j] = 1;
						}
					} else {
						// field contains just one entry (number)
						pageObject[entry] = 1;
					}
				}

				// create array from key set
				var pageArray = Object.keys(pageObject);

				// collect all pages
				for (var i = 0; i < pageArray.length; i++){
					// get the min and max index of the page
					var pageItemMin = (pageArray[i] - 1) * tableParams.count();
					var pageItemMax = pageArray[i] * tableParams.count();

					// check, whether the page contains enough items to be added as full page
					pageItemMax = pageItemMax > annstats.annotationsFiltered.length ? annstats.annotationsFiltered.length : pageItemMax;

					var pageData = annstats.annotationsFiltered.slice(pageItemMin, pageItemMax);
                    // add the IDs
					for (var j = 0; j < pageData.length; j++){
						annotationIDs.push(pageData[j].cmID);
					}
				}
			}

            if (annotationIDs.length === 0){
                $scope.warnAnnoEmpty = true;
                $log.warn("Your query does not contain any annotations!");
                return;
            }else {
                delete $scope.warnAnnoEmpty;
            }


			// validate the window'ed export settings
			if ($scope.imageExportSettings.fixedWindow === true){
				try {
					var width = parseInt($scope.imageExportSettings.fixedWindowDimensions.width);
					$scope.imageExportSettings.fixedWindowDimensions.width = width; // changes the text field value
					var height = parseInt($scope.imageExportSettings.fixedWindowDimensions.height);
					$scope.imageExportSettings.fixedWindowDimensions.height = height; // changes the text field value

					if (width <= 0 || height <=0){
						$scope.fixedWidthHeightError = true;
						$scope.fixedWidthHeightErrorText = "Width and height must be >0!";
						return;
					}
					delete $scope.fixedWidthHeightError;
				} catch (e) {
					$scope.fixedWidthHeightError = true;
					$scope.fixedWidthHeightErrorText = "Width and height must be a number >0!";
					return;
				}
			}

			// TODO possible future implementation: allow dynamic border to be negative
			if ($scope.imageExportSettings.dynamicWindow === true){
				try {
					var border = parseInt($scope.imageExportSettings.dynamicWindowBorder);
					$scope.imageExportSettings.dynamicWindowBorder = border;
					if (border < 0){
						$scope.dynamicBorderError = true;
						$scope.dynamicBorderErrorText = "The border must be >=0!";
						return;
					}
					delete $scope.dynamicBorderError;
				} catch (e) {
					$scope.dynamicBorderError = true;
					$scope.dynamicBorderErrorText = "The border must be >=0!";
					return;
				}
			}

			// parse the level settings from the string
			// resolve the page numbers
			// sanity checks
			var levelRanges = $scope.imageExportSettings.levels;
			if (levelRanges === undefined || levelRanges === null || levelRanges === ""){
				$scope.levelRangeError = true;
				$scope.levelRangeErrorText = "The range must not be empty!";
				return;
			} else {
				delete $scope.levelRangeError;
			}

			// split the string at the ';' character
			// e.g. "0;1"
			var levelRangeArray = String(levelRanges).split(';');
			$log.debug(levelRangeArray);
			// trim all whitespaces
			levelRangeArray = levelRangeArray.map(Function.prototype.call, String.prototype.trim);
			$log.debug(levelRangeArray);
			// final levels (split and cleaned)
			var levelObject = {};

			// split all ranges
			for (var i = 0; i < levelRangeArray.length; i++){
				var entry = levelRangeArray[i];
				if (entry === ""){
					continue;
				}
				var dash_idx = entry.indexOf('-');
				if (dash_idx !== -1){
					// get the first and last entry
					var first_and_second = entry.split('-');
					first_and_second = first_and_second.map(Function.prototype.call, String.prototype.trim);
					first_and_second = first_and_second.sort();
					if (first_and_second.length !== 2){
						$scope.levelRangeError = true;
						$scope.levelRangeErrorText = "Enter a valid range in the form of e.g. '0-1' or '0;3'.";
						return;
					} else {
						delete $scope.levelRangeError;
					}
					// both border ranges inclusive
					for (var j = Number(first_and_second[0]); j <= Number(first_and_second[1]); j++){
						levelObject[j] = 1;
					}
				} else {
					// field contains just one entry (number)
					levelObject[entry] = 1;
				}
			}

			// create array from key set
			var levelArray = Object.keys(levelObject);
			// set the levels to the config object
			var exp_config = {};
			angular.copy($scope.imageExportSettings, exp_config);
			exp_config['levels'] = levelArray;

			var params = {
				projectID: project,
				annotationIDs: annotationIDs,
				imageExportSettings: exp_config,
                userIDs: selectedUsers,
                termIDs: selectedTerms,
                imageIDs: selectedImages
			};

			$log.debug("Requesting image dataset: ", params);

			$scope.downloadPrepInProgress = true;
			var promise = downloadService.requestDownloadURL(params).then(null, function (reason) {
				sharedService.addAlert(reason, 'danger', 7500);
				// you can also throw the error
				// throw reason;
				return $q.reject(reason);
			}).then(downloadService.downloadFile).then(function(){
				$scope.downloadFileText = "Your dataset is ready. If your download did not start automatically, " +
				"use the following link to download it directly:";
				$scope.downloadFullURL = params.resp.fullURL;
				$scope.downloadPrepInProgress = false;
				$scope.downloadPrepStatus = 'success';
				sharedService.addAlert("Creating image dataset succeeded.", 'success', 7500);
			}, function(){
				$scope.downloadFileText = "We could not prepare your dataset, sorry. Please try again or contact" +
				" the system administrator!";
				$scope.downloadPrepStatus = 'failed';
				$scope.downloadPrepInProgress = false;
				sharedService.addAlert("Creating image dataset failed!", 'danger', 7500);
			}, function(i){
				// NOTIFY CALLBACK
			});

            // cleanup duties
			promise.finally(function(){
			});
		};

		$scope.close = function () {
			$modalInstance.close('close');
		};
	};
	// END IMAGE DATASET EXPORT MODAL DIALOG
	// ###############################################################


//	//////////////////////////////////////////
//	declare additional methods
//	//////////////////////////////////////////
//	Determine the row's background color class according 
//	to the current labeling progress.
	$scope.addKeys = function(url){
		try {
			return cytomineService.addKeys(url);
		} catch (e){
			return '';
		}
	};
}]);
