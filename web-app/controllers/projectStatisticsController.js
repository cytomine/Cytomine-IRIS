/**
 * Created by phil on 08/05/15.
 */
var iris = angular.module("irisApp");

iris.controller("prjStatsCtrl",
    ["$scope", "$log", "hotkeys", "$routeParams",
        "helpService",
        "cytomineService",
        "sessionService",
        "navService",
        "settingsService",
        function ($scope, $log, hotkeys, $routeParams,
                  helpService,
                  cytomineService,
                  sessionService,
                  navService,
                  settingsService) {

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

            $scope.statistics = {
                checkingAccess : true
                // error : {}
            };

            $scope.projectID = $routeParams['projectID'];

            $scope.$watch("main.irisUser", function(irisUser){
                //$log.debug(irisUser);

                $scope.checkAccess($scope.projectID, irisUser.cmID);
            });

            $scope.checkAccess = function (projectID, userID) {
                $log.debug("Checking access to project statistics...");

                settingsService.fetchProjectUsersSettings(projectID, userID,
                    function (data) {
                        if (data[0].projectSettings.irisCoordinator === false){
                            navService.analyzeErrorStatus(403);
                        } else {
                            $scope.statistics.checkingAccess = false;
                        }
                    }
                    , function (error, status) {
                        navService.analyzeErrorStatus(status);
                    });
            };

        }]);
