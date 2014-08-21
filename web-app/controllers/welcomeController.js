var iris = angular.module("irisApp");

iris.constant("welcomeUrl", "/api/welcome.json");

iris.controller("welcomeCtrl", function($scope, $log, $http, helpService, cytomineService, welcomeUrl){
	console.log("welcomeCtrl");
	
	// set the help variable for this page 
	helpService.setContentUrl("content/help/welcomeHelp.html");
	
	$scope.welcome = {
			errors : {}
	}

	// retrieve the welcome text
	$scope.getWelcome = function() {
		$http.get(welcomeUrl).success(function(data, status, headers, config) {
			// console.log(data);
			$scope.welcome.welcome = data;
		}).error(function(data, status, headers, config) {
			$scope.welcome.error.retrieve = {
				status : status,
				message : data.errors
			};
		})
	};
	$scope.getWelcome();
	
	// retrieve the Cytomine host address
	$scope.getCytomineHost = function() {
		cytomineService.getCytomineHost(function(cytomineHost) {
			$scope.welcome.cytomineHost = cytomineHost;
			// console.log("Cytomine Host: " + $scope.main.cytomineHost)
		})
	};
	$scope.getCytomineHost();

	// retrieve the Cytomine web address
	$scope.getCytomineWeb = function() {
		cytomineService.getCytomineWeb(function(cytomineWeb) {
			$scope.welcome.cytomineWeb = cytomineWeb;
			// console.log("Cytomine Web: " + $scope.main.cytomineWeb)
		})
	};
	$scope.getCytomineWeb();
	
});