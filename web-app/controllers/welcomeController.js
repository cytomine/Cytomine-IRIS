var iris = angular.module("irisApp");

iris.controller("welcomeCtrl", 
		["$scope", "$log", "helpService", "hotkeys", "cytomineService", "sessionService",
		 function($scope, $log, helpService, hotkeys, cytomineService, sessionService) {
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
		}, function(data, status){
			$scope.welcome.error = {
				status: status,
				message: data.error.message
			}
		});
	};
	$scope.getCytomineHost();
}]);