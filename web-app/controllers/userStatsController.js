var iris = angular.module("irisApp");

iris.controller(
	"userStatsCtrl", [
		"$rootScope", "$scope", "$http", "$filter",
		"$document", "$timeout", "$location", "$route",
		"$routeParams", "$log", "hotkeys",
		"cytomineService", "projectService", "imageService", "sessionService",
		"helpService", "sharedService", "navService", "annotationService", "statisticsService",
		"ngTableParams",
		function($rootScope, $scope, $http, $filter,
				 $document, $timeout, $location, $route,
				 $routeParams, $log, hotkeys,
				 cytomineService, projectService, imageService, sessionService,
				 helpService, sharedService, navService, annotationService, statisticsService,
				 ngTableParams) {
			$log.debug("userStatsCtrl");

			// retrieve the project parameter from the URL
			$scope.projectID = $routeParams["projectID"];

			// get the current date as long
			$scope.today = sharedService.today;

			$scope.userstats = {
				error : {},
				sbcollapsed: false,
				opening : {}
			};

			$scope.setCollapsed = function(flag){
				$scope.userstats.sbcollapsed = flag;
			};

			// settings for the user bar chart
			$scope.barChartOptions = {
				chart: {
					type: 'discreteBarChart',
					height: 300,
					margin : {
						top: 20,
						right: 20,
						bottom: 130,
						left: 80
					},
					x: function(series){
						return series.label;
					},
					y: function(series){
						return series.value;
					},
					showValues: false,
					valueFormat: function(value){
						return d3.format(',.0f')(value);
					},
					transitionDuration: 500,
					xAxis: {
						axisLabel: 'Ontology Terms',
						rotateLabels: -30,
						tickFormat: function(d) {
							return $scope.terms[d].name;
						}
					},
					yAxis: {
						axisLabel: 'Frequency'
					},
					color : function(series, index) {
						return $scope.terms[series.label].color;
					},
					tooltipContent : function(key, x, y, e, graph) {
						//$log.debug(key); // this is the name of the series
						//$log.debug(x); // this is the x value (term name)
						return '<div style="padding:3px; background: ' +
							$scope.termsByName[x].color + ';">'
							+ '<strong>' + String(x) + '</strong></div>' +
							'<p>' + Number(y) + '</p>';
					}
				}
			};

			// selected users for filtering
			var selectedUsers = [];
			// selected terms for filtering
			var selectedTerms = [];
			// selected images for filtering
			var selectedImages = [];

			// react to term filter change events
			$scope.$on("termFilterChange", function(event, object) {
				$log.debug("termFilterChange detected in userStatsCtrl");

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
				$log.debug("userFilterChange detected in userStatsCtrl");

				selectedUsers = object.id;

				$scope.showOrHideNoUsersWarning();
			});

			$scope.$on("imageFilterChange", function(event, object) {
				$log.debug("imageFilterChange detected in userStatsCtrl");

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

				statisticsService.fetchUserStatistics($scope.projectID,
					selectedImages, selectedTerms, selectedUsers, function(data){

					$scope.globalStats = data.annotations;
					$scope.terms = data.terms;


					if (data.annotations.length < 1){
						$scope.userstats.error.empty= {
							message : "This query does not result in any statistics."
						};
						$scope.loading = false;
						return;
					} else {
						delete $scope.userstats.error;
					}

					// compute a lookup map for term ID vs termName
					$scope.termsByName = {};
					for (var key in data.terms){
						if (data.terms.hasOwnProperty(key)){
							$scope.termsByName[data.terms[key].name] = {
								color: data.terms[key].color,
								id : key
							}
						}
					}

					$scope.dataset = {};

					for (var k = 0; k < data.users.length; k++){
						var values = data.users[k].userStats;
						$scope.dataset[k] = [ {
							"key" : (data.users[k].lastname + " " + data.users[k].firstname),
							"user" : data.users[k],
							"values" : values
						} ];
					}

					$scope.loading=false;
				}, function(data, status){
					// fetching failed
					$scope.userstats.error = {
						retrieve : {
							status : status,
							message : data.error.message
						}
						};
					$scope.loading = false;
				});
			};

	// refresh the page
	$scope.refreshPage = $scope.performQuery;

	// determine the type of the progress bar (color)
	$scope.type = function(progress) {
		if (progress < 50){
			return "danger";
		}
		else if (progress >= 50 && progress < 75){
			return "warning";
		}
		else if (progress >= 75 && progress < 95){
			return "info";
		}
		else if (progress >= 95){
			return "success";
		}
	};

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
