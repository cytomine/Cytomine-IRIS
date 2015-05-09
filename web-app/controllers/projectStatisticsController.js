/**
 * Created by phil on 08/05/15.
 */
var iris = angular.module("irisApp");

iris.controller("prjStatsCtrl",
    ["$routeParams", "$scope", "$log", "helpService", "hotkeys", "cytomineService", "sessionService", "navService",
        function ($routeParams, $scope, $log, helpService, hotkeys, cytomineService, sessionService, navService) {

            $log.debug("prjStatsCtrl");

            // set content url for the help page
            helpService.setContentUrl("content/help/prjStatisticsHelp.html");

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

            $scope.projectID = $routeParams['projectID'];

            $scope.stats = {
                sbcollapsed: false
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

            $scope.setCollapsed = function(flag){
                $scope.stats.sbcollapsed = flag;
                $log.debug(flag)
            };
        }]);
