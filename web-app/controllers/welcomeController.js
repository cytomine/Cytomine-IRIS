var iris = angular.module("irisApp");

iris.constant("welcomeUrl", "/api/welcome.json");

iris.controller("welcomeCtrl", function($rootScope, $scope, $log, $http, $location,
		helpService, cytomineService, welcomeUrl, hotkeys, sessionService, $routeParams, $route, navService) {
	$log.debug("welcomeCtrl");
	
	// set the help variable for this page
	helpService.setContentUrl("content/help/welcomeHelp.html");
	
	$scope.welcome = {};

	// put all valid shortcuts for this page here
	hotkeys.bindTo($scope)
	.add({
		combo : 'h',
		description : 'Show help for this page',
		callback : function() {
			helpService.showHelp();
		}
	});

	// retrieve the Cytomine host address
	$scope.getCytomineHost = function() {
		cytomineService.getCytomineHost(function(cytomineHost) {
			$scope.welcome.cytomineHost = cytomineHost;
			delete $scope.welcome.error;
		}, function(data, status){
			$scope.welcome.error = {
				status: status,
				message: data.error.message
			}
		});
	};
	$scope.getCytomineHost();
});