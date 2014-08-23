var iris = angular.module("irisApp");

iris.constant("welcomeUrl", "/api/welcome.json");

iris.controller("welcomeCtrl", function($rootScope, $scope, $log, $http, $location,
		helpService, cytomineService, welcomeUrl, hotkeys) {
	console.log("welcomeCtrl");

	// set the help variable for this page
	helpService.setContentUrl("content/help/welcomeHelp.html");

	$scope.welcome = {
		errors : {}
	}

	// put all valid shortcuts for this page here
	hotkeys.bindTo($scope)
	.add({
		combo : 'h',
		description : 'Show help for this page',
		callback : function() {
			helpService.showHelp();
		}
	})
//	.add({
//		combo : '*',
//		description : 'Navigate to the available projects',
//		callback : function() {
//			$location.url("/projects");
//		}
//	});

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