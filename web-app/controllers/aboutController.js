var iris = angular.module("irisApp");

iris.controller("aboutCtrl", 
		["$scope", "$log", "helpService", "hotkeys", "cytomineService", 
		 function($scope, $log, helpService, hotkeys, cytomineService) {
	$log.debug("aboutCtrl");
	
	// help variable for this page
	helpService.setContentUrl(null);
	
	$scope.about = {};
	
	// put all valid shortcuts for this page here
//	hotkeys.bindTo($scope)
//	.add({
//		combo : 'h',
//		description : 'Show help for this page',
//		callback : function() {
//			helpService.showHelp();
//		}
//	});

	// retrieve the Cytomine IRIS application information
	$scope.getAppInfo = function() {
		cytomineService.getAppInfo(function(appInfo) {
			$scope.about.appInfo = appInfo;
			delete $scope.about.error;
		}, function(data, status){
			$scope.about.error = {
				status: status,
				message: data.error.message
			}
		});
	};
	$scope.getAppInfo();
}]);