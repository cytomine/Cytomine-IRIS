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
				opening : {}
			};

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

			// refresh the page
			$scope.refreshPage = function(){
				// show the loading button
				$scope.loading = true;

				statisticsService.fetchUserStatistics($scope.projectID, null, null, null, function(data){

					$scope.globalStats = data.annotations;
					$scope.terms = data.terms;

					// compute a lookup map for terms vs labels
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
					$scope.userstats.error.retrieve = {
						status : status,
						message : data.error.message
					};
					$scope.loading = false;
				});
			};

//	fetch the annotation
	//$scope.refreshPage();

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
