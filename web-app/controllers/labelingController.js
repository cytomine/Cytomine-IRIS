var iris = angular.module("irisApp");

iris.controller("labelingCtrl", [
    "$scope", "$http", "$filter", "$location", "$timeout",
    "$routeParams", "$log", "hotkeys", "ngTableParams", "helpService", "annotationService",
    "projectService", "sessionService", "sharedService", "navService", "imageService",
    "cytomineService", "$route", function ($scope, $http, $filter, $location, $timeout,
                                 $routeParams, $log, hotkeys, ngTableParams, helpService, annotationService,
                                 projectService, sessionService, sharedService, navService, imageService,
                                 cytomineService, $route) {
        $log.debug("labelingCtrl");

        $scope.projectID = $routeParams["projectID"];
        $scope.imageID = $routeParams["imageID"];
        $scope.annotationID = $routeParams["annID"];

        // preallocate the objects
        $scope.labeling = {
            annotationTuple: {},
            annotations: {},
            // take default hiding preferences from server if not otherwise specified
            hideCompleted: "undefined" === typeof($routeParams["hideCompleted"]) ? null : Boolean($routeParams["hideCompleted"]),
            loading: true
        };

        $scope.ontology = {};

        // set content url for the help page
        helpService.setContentUrl("content/help/labelingHelp.html");

        // an object for saving progress status
        $scope.saving = {
            status: ""
        };

        // Initialize the variable for the progress
        $scope.image = {
            settings: {
                progress: 0,
                labeledAnnotations: 0,
                numberOfAnnotations: 0
            }
        };

        // put all valid shortcuts for this page here
        hotkeys.bindTo($scope).add({
            combo: 'h',
            description: 'Show help for this page',
            callback: function () {
                helpService.showHelp();
            }
        }).add({
            combo: 'n',
            description: 'Proceed to next annotation',
            callback: function () {
                $scope.moveToNextAnnotation();
            }
        }).add({
            combo: 'p',
            description: 'Go back to the previous annotation',
            callback: function () {
                $scope.moveToPreviousAnnotation();
            }
        });

        // get a new 3-tuple (around annID) from the server
        $scope.fetchNewTuple = function (annID, displayCurrentAnnotation, initCallback) {
            $scope.navDisabled = true;

            annotationService.fetchUserAnnotations3Tuple(
                $scope.projectID,
                $scope.imageID,
                $scope.labeling.hideCompleted,
                annID,
                function (data) {
                    // delete the errors
                    delete $scope.labeling.error;
        //			$log.debug(data)
                    $scope.labeling.annotationTuple = data;
                    $log.debug("retrieved " + data.size + " annotations");

                    if (displayCurrentAnnotation === true) {
                        $scope.annotation = data.currentAnnotation;
                        $log.debug("Setting the current annotation for display.");
                    }

                    // map the progress to the 'image' for the progress bar
                    $scope.image.settings = data.imageSettings;
                    $scope.labeling.hideCompleted = data.imageSettings.hideCompletedAnnotations;

                    // store all available annotation IDs into an array for the dropdown list
                    $scope.labeling.annotationIDList = data.annotationIDList;
                    $scope.gtAnnotationID = $scope.annotation.cmID;

                    var urlPath = "/project/" + $scope.projectID + "/image/" + $scope.imageID + "/label/"
                                + $scope.annotation.cmID;

                    // update the path in the URL (e.g. for bookmarking)
                    //$location.path(urlPath, false);
                    // TODO the prev. statement somehow breaks the ability to move to
                    // "/project/:idProject/images", however all other
                    // links work

                    // enable navigation
                    $scope.navDisabled = false;
                    if (initCallback) {
                        initCallback(true);
                    }
                }, function (data, status) {
                    var msg = "";
                    if (status === 404) {
                        msg = "This image does not have any annotations!";
                        // add message
                        $scope.labeling.error = {
                            empty: {
                                message: msg,
                                status: status
                            }
                        }
                    } else if (status === 412) {
                        $scope.labeling.error = {
                            retrieve: {
                                message: data.error.message,
                                status: status
                            }
                        }
                    } else {
                        $scope.labeling.error = {
                            retrieve: {
                                message: data.error.message,
                                status: status
                            }
                        }
                    }

                    $log.error(data);

                    $scope.navDisabled = false;

                    if (initCallback) {
                        initCallback(false);
                    }
                });
        };

        // initialize the controller
        $scope.init = function () {

            // fetch the new tuple (from start = null), or from current annotation
            // on loading set the flag to true, if the current annotation should be displayed
            $scope.fetchNewTuple($scope.annotationID, true, function(success) {
                if (success) {
                    $log.debug("Loading initial annotation was successful.");
                } else {
                    $log.error("Cannot load initial annotation!");
                }
                $scope.labeling.loading = false;
            });
        };
        $scope.init();

        $scope.moveToNextAnnotation = function () {
            // check for disabled next button (e.g. while loading)
            if ($scope.navDisabled) {
                return;
            }

            var hasNext = $scope.labeling.annotationTuple.hasNext;
            if (!hasNext) {
                sharedService.addAlert("This is the last annotation.", "info");
                return;
            }

            $log.debug("Fetching next annotation");
            // get the next annotation ID from the array
            var nextAnnID = $scope.labeling.annotationTuple.nextAnnotation.cmID;

            // blend in next annotation
            $scope.annotation = $scope.labeling.annotationTuple.nextAnnotation;

            // fetch the next 3-tuple from the server, currentAnnotation = nextAnnotation.cmID
            if ($scope.labeling.annotationTuple.hasNext) {
                $scope.fetchNewTuple(nextAnnID, false);
            }
        };

        $scope.moveToPreviousAnnotation = function () {
            // check for disabled navigation (e.g. while loading)
            if ($scope.navDisabled) {
                return;
            }

            var hasPrevious = $scope.labeling.annotationTuple.hasPrevious;
            if (!hasPrevious) {
                sharedService.addAlert("This is the first annotation.", "info");
                return;
            }

            $log.debug("Fetching previous annotation");
            // get the previous annotation ID
            var prevAnnID = $scope.labeling.annotationTuple.previousAnnotation.cmID;

            // blend in previous annotation
            $scope.annotation = $scope.labeling.annotationTuple.previousAnnotation;

            // fetch the 3-tuple from the server, currentAnnotation = previousAnnotation.cmID
            if ($scope.labeling.annotationTuple.hasPrevious) {
                $scope.fetchNewTuple(prevAnnID, false);
            }
        };

        // shows or hides completed annotations
        $scope.showOrHideCompleted = function () {
            $log.debug($scope.labeling.hideCompleted);

            // fetch new tuple
            $scope.fetchNewTuple($scope.labeling.annotationTuple.currentAnnotation.cmID, true);
        };


        ///////////////////////
        // ONTOLOGY functions
        ///////////////////////
        $scope.loadOntology = function () {
            $scope.ontologyLoading = true;

            projectService.fetchOntologyByProjectID($scope.projectID, {
                flat: true
            }, function (data) {
                $scope.ontology = data;

                // build the ontology table
                $scope.tableParams = new ngTableParams({
                    page: 1, // show first page
                    count: 25, // count per page
                    // leave this blank for no sorting at all
                    sorting: {
                        name: 'asc' // initial sorting
                    },
                    filter: {
                        // applies filter to the "data" object before sorting
                    }
                }, {
                    total: $scope.ontology.length, // number of terms
                    getData: function ($defer, params) {
                        // use build-in angular filter
                        var newData = $scope.ontology;
                        // use build-in angular filter
                        newData = params.filter() ? $filter('filter')(newData,
                            params.filter()) : newData;
                        newData = params.sorting() ? $filter('orderBy')(newData,
                            params.orderBy()) : newData;

                        $scope.ontologySettings = {
                            pageItemMin : (params.page() - 1) * params.count()
                        };

                        var pageItemMax = params.page() * params.count();
                        $scope.ontologySettings.pageItemMax = pageItemMax > newData.length ? newData.length : pageItemMax;

                        $scope.data = newData.slice($scope.ontologySettings.pageItemMin, pageItemMax);
                        params.total(newData.length); // set total for recalc pagination

                        $defer.resolve($scope.data);
                    },
                    filterDelay: 0
                });
                delete $scope.ontologyError;
                $scope.ontologyLoading = false;
            }, function(data, status){
                $scope.ontologyError = {
                    message: data.error.message,
                    status: status
                };
                $scope.ontologyLoading = false;
            });
        };

        // fetch the ontology on page reload
        $scope.loadOntology();

        // assign a term to the current annotation
        $scope.assign = function (term) {
            if (!$scope.saving.status === "saving")
                return;

            $scope.saving.status = "saving";
            annotationService.setAnnotationTerm($scope.projectID,
                $scope.imageID, $scope.annotation.cmID, term.id,
                function (data) {

                    // data is a map:
                    //["annotationTerm": annTerm.getAttr(), "hadTermsAssigned": (boolean)hadTermsAssigned]

                    // map the results to the local element
                    $scope.annotation.cmTermName = term.name;
                    $scope.annotation.cmTermID = term.id;

                    sharedService.addAlert("Term '" + term.name + "' has been assigned.");

                    $scope.saving.status = "success";

                    if (data['hadTermsAssigned'] === false){
                        // TODO optimistically increment by 1 on the client!
                        $scope.image.settings.labeledAnnotations += 1;
                        $scope.image.settings.progress = parseInt($scope.image.settings.labeledAnnotations * 100 / $scope.image.settings.numberOfAnnotations);
                    }

                    // REFLECT UPDATED LABELING PROGRESS
                    //$scope.updateLabelingProgress();
                    $timeout(function () {
                        $scope.saving.status = "";
                    }, 2000);
                }, function (data, status) {
                    sharedService.addAlert("Cannot assign term '" + term.name + "'. Status " + status + ".", "danger");
                    $scope.saving.status = "error";

                    // TODO do nothing with the progress

                    // REFLECT UPDATED LABELING PROGRESS
                    //$scope.updateLabelingProgress();
                    $timeout(function () {
                        $scope.saving.status = "";
                    }, 2000);
                });
        };

        // removes a term
        $scope.removeTerm = function () {
            if (!$scope.saving.status === "saving")
            return;

            var termID = $scope.annotation.cmTermID;
            var termName = $scope.annotation.cmTermName;

            $scope.saving.status = "saving";
            annotationService.deleteAnnotationTerm($scope.projectID,
                $scope.imageID, $scope.annotation.cmID, termID,
                function (data) {

                    // data response is a map
                    //["hasTermsLeft": (boolean)hasTermsLeft]

                    // remove the elements from the local object
                    $scope.annotation.cmTermName = null;
                    $scope.annotation.cmTermID = 0;
                    $scope.annotation.cmOntologyID = 0;

                    $log.debug("Removing term '" + termName + "' from annotation.");
                    sharedService.addAlert("Term '" + termName + "' has been removed.");
                    $scope.saving.status = "success";

                    // TODO optimistically update the progress on the client.
                    if (data['hasTermsLeft'] === false){
                        $scope.image.settings.labeledAnnotations -= 1;
                        $scope.image.settings.progress = parseInt($scope.image.settings.labeledAnnotations * 100 / $scope.image.settings.numberOfAnnotations);
                    }

                    // REFLECT UPDATED LABELING PROGRESS
                    //$scope.updateLabelingProgress();
                    $timeout(function () {
                        $scope.saving.status = "";
                    }, 2000);
                }, function (data, status) {
                    sharedService.addAlert("Cannot remove term '" + termName + "'. Status " + status + ".", "danger");
                    $scope.saving.status = "error";

                    // TODO do nothing with the progress

                    // REFLECT UPDATED LABELING PROGRESS
                    // $scope.updateLabelingProgress();
                    $timeout(function () {
                        $scope.saving.status = "";
                    }, 2000);
                });
        };

        // TODO THIS IS TOO SLOW FOR FREQUENT USE
        // fetch the labeling status of the current image
        $scope.updateLabelingProgress = function () {
            sessionService.getLabelingProgress($scope.projectID, $scope.imageID, function (data) {
                $log.debug("updated labeling progress");
                $scope.image.settings = data;
                $log.debug($scope.image.settings);
            }, function (data, status, header, config) {
                sharedService.addAlert("Cannot update your progress! Status " + status + ".", "danger");
            })
        };

        // navigates to the image list
        $scope.navToImages = function () {
            navService.navToImages();
        };

        $scope.goToAnnotation = function(){
            try {
                var raw = $scope.gtAnnotationID;
                if (raw === undefined){
                    // return
                    return;
                }
                var gotoID = Number(raw);
                $log.debug("User navigates to annotation [" + gotoID + "].");
                $scope.fetchNewTuple(gotoID, true);
            } catch (e) {
                sharedService.addAlert("Cannot go to annotation [" + $scope.gtAnnotationID + "].", "danger");
            }
        };

        $scope.selectLabel = function(value){
            return "#" + ($scope.labeling.annotationIDList.indexOf(value)+1) + ": " + value;
        };
    }]);
