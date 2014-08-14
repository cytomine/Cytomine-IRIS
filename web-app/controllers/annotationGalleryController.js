angular.module("irisApp")
    .config(function ($logProvider) {
        $logProvider.debugEnabled(true);
    })
    .controller("annotationGalleryCtrl", function ($scope, $http,$filter, $location,annotationService,$routeParams) {
        console.log("annotationGalleryCtrl");

        $scope.annotation = {error:{}};
        $scope.idProject = $routeParams["idProject"];

        // TODO
    });
