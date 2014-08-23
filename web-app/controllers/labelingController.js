var iris = angular.module("irisApp");

iris.config(function($logProvider) {
	$logProvider.debugEnabled(true);
});

iris.controller("labelingCtrl", function($scope, $http, $filter, $location,
		$routeParams, $log, annotationService, sharedService) {
	console.log("labelingCtrl");

	$scope.labeling = {
		error : {}
	};
	$scope.projectID = $routeParams["projectID"];

	// TODO

	$scope.maxSize = 10;
	$scope.totalItems = 64;
	$scope.currentPage = 4;
	$scope.numPages = 2;

});
