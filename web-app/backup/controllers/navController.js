var iris = angular.module("irisApp");

iris.controller("navCtrl", [
    "$scope", "$window", "$route", "$location", "$log", "sharedService", "navService",
    "projectService", "helpService", "sessionService", "imageService", "breadcrumbs",
    function ($scope, $window, $route, $location, $log, sharedService, navService,
              projectService, helpService, sessionService, imageService, breadcrumbs) {
        $log.debug("navCtrl");

        $scope.breadcrumbs = breadcrumbs;

        // enable session refresh button
        $scope.blockSessionRefresh = false;

        // navigation active tab controller
        $scope.isActive = function (viewLocation) {
            // $log.debug($location.path())

            // full match
            if ($location.path() === viewLocation) {
                return true;
            }
            // partial (suffix) match
            else if (sharedService.strEndsWith($location.path(), viewLocation)) {
                return true;
            }
            // partial (suffix) match
            else if (sharedService.strContains($location.path(), viewLocation)) {
                return true;
            }
            // no match
            else {
                return false;
            }
        };

        // navigate to the current annotation for labeling
        $scope.labeling = function () {
            navService.resumeAtLabelingPage();
        };

        // navigate to the available projects
        $scope.projects = function () {
            navService.navToProjects();
        };

        // navigate to the current project's images
        $scope.images = function () {
            navService.navToImages();
        };

        // navigate to the current project's annotations
        $scope.annotations = function () {
            navService.resumeAtAnnotationGallery();
        };

        // show the help page
        $scope.showHelp = function () {
            helpService.showHelp();
        };

        $scope.refreshSession = function () {
            if ($scope.blockSessionRefresh) {
                return;
            }

            $scope.blockSessionRefresh = true;
            sessionService.fetchSession(function (data) {
                $log.debug("Successfully refreshed session.");
                $scope.blockSessionRefresh = false;
            }, function (data, status) {
                $log.error("Session refresh failed!");
                $scope.blockSessionRefresh = false;
            });
        };

        $scope.hasSession = function () {
            return (sessionService.getSession() != null);
        };

        // react to the session update
        $scope.$on('sessionUpdate', function (event, mass) {

            $log.debug("session update!");

            var currSess = mass['session'];

            if (currSess == null || currSess === undefined){
                delete $scope.bcProjectName;
                delete $scope.bcImageName;
                delete $scope.bcAnnotationID;
                return;
            }

            var prj = currSess.currentProject;

            if (prj == null || prj === undefined){
                delete $scope.bcProjectName;
                delete $scope.bcImageName;
                delete $scope.bcAnnotationID;
            } else {
                $scope.bcProjectName = prj.cmName;

                var img = currSess.currentProject.currentImage;
                if (img == null || img === undefined) {
                    delete $scope.bcImageName;
                    delete $scope.bcAnnotationID;
                } else {
                    $scope.bcImageName = img.originalFilename;
                    var annID = img.settings.currentCmAnnotationID;
                    if (annID == null || annID === undefined){
                        delete $scope.bcAnnotationID;
                    } else {
                        $scope.bcAnnotationID = annID;
                    }
                }
            }
        });

        // UPDATE THE SESSION AND REFRESH BREADCRUMBS
        sessionService.fetchSession();
    }]);
