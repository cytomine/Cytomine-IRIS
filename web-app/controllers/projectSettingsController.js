/**
 * Created by phil on 08/05/15.
 */
var iris = angular.module("irisApp");

iris.controller("prjSettingsCtrl",
    ["$scope", "$log", "helpService", "hotkeys", "cytomineService", "sessionService", "navService",
        function ($scope, $log, helpService, hotkeys, cytomineService, sessionService, navService) {

            $log.debug("prjSettingsCtrl");

            // set content url for the help page
            helpService.setContentUrl("content/help/prjSettingsHelp.html");

            // put all valid shortcuts for this page here
            hotkeys.bindTo($scope)
                .add({
                    combo : 'h',
                    description : 'Show help for this page',
                    callback : function() {
                        helpService.showHelp();
                    }
                }).add({
                    combo : 'r',
                    description : 'Resume labeling where you left last time',
                    callback : function() {
                        navService.resumeAtLabelingPage();
                    }
                });

            $scope.settings = {
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
