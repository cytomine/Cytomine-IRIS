/**
 * Created by phil on 08/05/15.
 */
var iris = angular.module("irisApp");

iris.controller("statsCtrl",
    ["$scope", "$log", "helpService", "hotkeys", "cytomineService", "sessionService", "navService",
        function ($scope, $log, helpService, hotkeys, cytomineService, sessionService, navService) {

            $log.debug("statsCtrl");

            // set content url for the help page
            helpService.setContentUrl(undefined);

            $scope.stats = {
                //error: {}
            };

            $scope.loading = false;

            $scope.tabs = [
                { title:'Dynamic Title 1', content:'Dynamic content 1' },
                { title:'Dynamic Title 2', content:'Dynamic content 2', disabled: true }
            ];

            $scope.alertMe = function() {
                window.alert('You\'ve selected the alert tab!');
            };

}]);
