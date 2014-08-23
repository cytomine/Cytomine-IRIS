var iris = angular.module("irisApp");

iris.config(function($logProvider) {
	$logProvider.debugEnabled(true);
});

iris.controller("labelingCtrl", function($scope, $http, $filter, $location,
		$routeParams, $log, hotkeys, helpService, annotationService, sharedService) {
	console.log("labelingCtrl");

	// set content url for the help page
	helpService.setContentUrl("content/help/labelingHelp.html");

	$scope.labeling = {
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
	
	$scope.projectID = $routeParams["projectID"];

	// TODO

	$scope.maxSize = 10;
	$scope.totalItems = 64;
	$scope.currentPage = 4;
	$scope.numPages = 2;
	
});
