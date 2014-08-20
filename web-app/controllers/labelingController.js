var iris = angular.module("irisApp");

iris.config(function ($logProvider) {
        $logProvider.debugEnabled(true);
    });

iris.controller("labelingCtrl", function ($scope, $http, $filter, $location, $routeParams, 
		annotationService, sharedService) {
        console.log("labelingCtrl");

        $scope.annotation = {error:{}};
        $scope.idProject = $routeParams["idProject"];

        // TODO
    });
