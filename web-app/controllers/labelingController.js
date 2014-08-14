angular.module("irisApp")
    .config(function ($logProvider) {
        $logProvider.debugEnabled(true);
    })
    .controller("labelingCtrl", function ($scope, $http,$filter, $location,annotationService,$routeParams) {
        console.log("labelingCtrl");

        $scope.annotation = {error:{}};
        $scope.idProject = $routeParams["idProject"];

        // TODO
    });
