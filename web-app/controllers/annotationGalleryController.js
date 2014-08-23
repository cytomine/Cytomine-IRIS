var iris = angular.module("irisApp");

iris.config(function($logProvider) {
	$logProvider.debugEnabled(true);
});

iris.controller("annotationGalleryCtrl", function($scope, $http, $filter,
		$location, annotationService, $routeParams) {
	console.log("annotationGalleryCtrl");

	$scope.annotation = {
		error : {}
	};
	$scope.projectID = $routeParams["projectID"];

	// TODO
});
