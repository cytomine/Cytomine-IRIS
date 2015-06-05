var iris = angular.module("irisApp");

iris.controller("projectCtrl", [
    "$rootScope", "$routeParams", "$scope", "$http", "$filter", "$location", "$document",
    "$modal", "$log", "hotkeys", "projectService", "helpService", "sharedService", "sessionService", "ngTableParams", "navService", "$cookieStore",
    "$timeout",
    function ($rootScope, $routeParams, $scope, $http, $filter, $location, $document,
              $modal, $log, hotkeys, projectService, helpService, sharedService, sessionService, ngTableParams, navService, $cookieStore,
              $timeout) {
        $log.debug("projectCtrl");

        // set content url for the help page
        helpService.setContentUrl("content/help/projectHelp.html");

        $scope.project = {
            stillNew: ((365 / 6) * 24 * 60 * 60 * 1000), // last 2 months
            error: {}
        };

        $scope.tmp = {
            tableSorting: {
                cmCreated: 'desc'
            }
        };

        // put all valid shortcuts for this page here
        hotkeys.bindTo($scope)
            .add({
                combo: 'h',
                description: 'Show help for this page',
                callback: function () {
                    helpService.showHelp();
                }
            }).add({
                combo: 'r',
                description: 'Resume labeling where you left last time',
                callback: function () {
                    navService.resumeAtLabelingPage();
                }
            });

        // get the current day as long
        $scope.today = sharedService.today;

        // refresh the page (get all projects and the session)
        $scope.loadProjects = function () {
            $scope.loading = true;

            // gets all projects
            projectService.fetchProjects(function (data) {
                $scope.project.error.retrieve = null;
                $scope.project.projects = data.projects;

                $scope.accessibleProjects = data.accessibleProjects;

                // build the data table
                $scope.tableParams = new ngTableParams({
                    page: 1, // show first page
                    count: 10, // count per page
                    // leave this blank for no sorting at all
                    sorting: $scope.tmp.tableSorting,
                    filter: {
                        // applies filter to the "data" object before sorting
                    }
                }, {
                    total: $scope.project.projects.length, // number of projects
                    counts: [10, 25, 50],
                    getData: function ($defer, params) {
                        // use build-in angular filter
                        var newData = $scope.project.projects;
                        // use build-in angular filter
                        newData = params.filter() ? $filter('filter')(newData,
                            params.filter()) : newData;
                        newData = params.sorting() ? $filter('orderBy')(newData,
                            params.orderBy()) : newData;

                        $scope.project.pageItemMin = (params.page() - 1) * params.count();
                        var pageItemMax = params.page() * params.count();
                        $scope.project.pageItemMax = pageItemMax > newData.length ? newData.length : pageItemMax;

                        $scope.data = newData.slice($scope.project.pageItemMin, pageItemMax);
                        params.total(newData.length); // set total for recalc pagination

                        // provide data to the table
                        $defer.resolve($scope.data);
                    },
                    filterDelay: 0
                });

                // WORKAROUND FOR RE-RENDERING NG-TABLE WITH UPDATED DATA (DYNAMIC CSS)!!
                $scope.tableParams.sorting({});
                $timeout(function () {
                        $scope.tableParams.sorting($scope.tmp.tableSorting);
                        $scope.loading = false;
                    },
                    1);

            }, function (data, status) {
                $scope.project.error.retrieve = {
                    status: status,
                    message: data.error.message
                };
                $scope.loading = false;
            });
        };

        // re-fetch the projects for the user
        $scope.refreshPage = function () {
            $scope.tmp.tableSorting = $scope.tableParams ? $scope.tableParams.sorting() : $scope.tmp.tableSorting;
            $scope.loadProjects();
        };

        // execute project loading
        $scope.loadProjects();

        // open a project
        $scope.openProject = function (project) {

            if (project['settings'].enabled !== true) {
                $scope.showNoOpenDialog(project);
                return;
            }

            // update the project in the session
            sessionService.openProject(project.cmID, function (data) {
                // forward to the image overview table of this project
                $location.url("/project/" + project.cmID + "/images");
            }, function (data, status) {
                sharedService.addAlert("Cannot open project. "
                + data.error.message + " (" + status + ")", "danger");
            })
        };

        // show the project statistics page
        $scope.showStatistics = function (project) {

            if (project['settings'].enabled !== true) {
                $scope.showNoOpenDialog(project);
                return;
            }

            // update the project in the session
            sessionService.openProject(project.cmID, function (data) {
                // forward to the image overview table of this project
                $location.url("/project/" + project.cmID + "/stats");
            }, function (data, status) {
                sharedService.addAlert("Cannot open project statistics. "
                + data.error.message + " (" + status + ")", "danger");
            })
        };

        // show the project settings
        $scope.showSettings = function (project) {

            if (project['settings'].enabled !== true) {
                $scope.showNoOpenDialog(project);
                return;
            }

            // update the project in the session
            sessionService.openProject(project.cmID, function (data) {
                // forward to the project settings
                $location.url("/project/" + project.cmID + "/settings");
            }, function (data, status) {
                sharedService.addAlert("Cannot open project settings. "
                + data.error.message + " (" + status + ")", "danger");
            })
        };

        $scope.canBeOpened = function (item) {
            return item.cmNumberOfImages > 0 && !item.isClosed;
        };

        // ###############################################################
        // PROJECT DESCRIPTION MODAL DIALOG
        // retrieve the information for a given project
        $scope.retrieveInfo = function (project) {
            projectService.getDescription(project.cmID,

                // successCallback
                function (jsonDescription) {
                    // open the dialog
                    $scope.showInfoDialog(project, jsonDescription.attr);
                },

                // error callback
                function (data, status) {

                    // react to specific status codes
                    if (status === 404) {
                        dlgData = {
                            data: "This project does not have any description.",
                            error: {
                                message: "No object found.",
                                status: status,
                                show: false
                            }
                        };
                    } else {
                        dlgData = {
                            data: {},
                            error: {
                                message: "The project information cannot be retrieved.",
                                status: status,
                                show: true
                            }
                        }
                    }

                    // open the dialog
                    $scope.showInfoDialog(project, dlgData);
                });
        };

        // open the modal project information dialog
        $scope.showInfoDialog = function (project, dlgData) {
            var modalInstance = $modal.open({
                templateUrl: 'projectDescription.html',
                controller: modalCtrl,
                //size : 'lg',
                resolve: {
                    data: function () {
                        return dlgData.data;
                    },
                    project: function () {
                        return project;
                    },
                    error: function () {
                        return dlgData.error;
                    }
                }
            });

            modalInstance.result.then(function (result) {
                // callbackSuccess branch
                $log.debug('Project Description Modal: ' + result);
            }, function (result) {
                // callbackError branch
                $log.debug('Project Description Modal: ' + result);
            });
        };

        // open a modal information dialog
        $scope.showNoOpenDialog = function (project) {
            var modalInstance = $modal.open({
                templateUrl: 'projectDisabled.html',
                controller: modalCtrl,
                size: 'md',
                resolve: {
                    data: function () {
                        return '<div class="alert alert-info">This project is not enabled for this IRIS instance ('
                            + $location.host() + '). ' +
                            '<br/>Please contact the administrator of your study or of this project.</div>';
                    },
                    project: function () {
                        return project;
                    },
                    error: function () {
                        return undefined;
                    }
                }
            });

            modalInstance.result.then(function (result) {
                // callbackSuccess branch
                $log.debug('Project no open modal: ' + result);
            }, function (result) {
                // callbackError branch
                $log.debug('Project no open modal: ' + result);
            });
        };

        // controller for the project descriptor modal dialog
        var modalCtrl = function ($scope, $modalInstance, $sce, data,
                                  project, error) {

            $scope.description = data;
            $scope.project = project;
            $scope.error = error;

            $scope.ok = function () {
                $modalInstance.close('OK');
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        };
        // END PROJECT DESCRIPTION MODAL DIALOG
        // ###############################################################

        // open a modal information dialog
        $scope.showCoordRequestDialog = function (project) {
            var modalInstance = $modal.open({
                templateUrl: 'coordinatorRequestForm.html',
                controller: coordRequCtrl,
                size: 'md',
                resolve: {
                    project: function () {
                        return project;
                    },
                    user: function () {
                        return $scope.main.user;
                    }
                }
            });

            modalInstance.result.then(function (result) {
                // callbackSuccess branch
                $log.debug('Coordinator Request Form modal: ' + result);
            }, function (result) {
                // callbackError branch
                $log.debug('Coordinator Request Form modal: ' + result);
            });
        };

        // controller for the coordinator request modal dialog
        var coordRequCtrl = function ($scope, $modalInstance, $sce,
                                      project, user) {

            $scope.coord = {
                project: project,
                user: user,
                textAreaContent: "Please authorize me as a project coordinator of ["
                + project.cmName + "].\n\nBest regards, \n" + user.firstname + " "
                + user.lastname + "\n(ID:" + user.id + ", username: " + user.username + ")",
                error: {}
            };

            var finalMessage = "";

            $scope.$watch('coord.textAreaContent', function (textAreaContent) {
                $log.debug(textAreaContent);
                finalMessage = textAreaContent;
            });

            $scope.ok = function () {
                // TODO send an email

                //$scope.coord.error = {
                //    show: true
                //};

                $log.debug(finalMessage);

                $scope.coord.requestsent = true;
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        };


    }]);
