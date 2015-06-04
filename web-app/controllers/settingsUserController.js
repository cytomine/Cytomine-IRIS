var iris = angular.module("irisApp");

iris.controller("settingsUserCtrl", [
    "$rootScope", "$routeParams", "$scope", "$http", "$filter", "$location", "$document",
    "$modal", "$log", "hotkeys", "settingsService", "helpService", "sharedService", "sessionService", "ngTableParams", "navService", "$cookieStore",
    "$timeout",
    function ($rootScope, $routeParams, $scope, $http, $filter, $location, $document,
              $modal, $log, hotkeys, settingsService, helpService, sharedService, sessionService, ngTableParams, navService, $cookieStore,
              $timeout) {
        $log.debug("settingsUserCtrl");

        $scope.settingsUser = {
            stillNew: ((365 / 6) * 24 * 60 * 60 * 1000), // last 2 months
            error: {}
        };

        $scope.projectID = $routeParams['projectID'];

        $scope.tmp = {
            tableSorting: {
                cmLastName: 'asc'
            }
        };
        // get the current day as long
        $scope.today = sharedService.today;

        // refresh the page (get all users)
        $scope.loadUsers = function () {
            $scope.loading = true;

            // gets all project users
            settingsService.fetchProjectUserList($scope.projectID, function (data) {

                delete $scope.settingsUser.error.retrieve;

                $scope.settingsUser.users = data;

                $scope.computeNUsersSyncDisabled();

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
                    total: $scope.settingsUser.users.length, // number of users
                    counts: [10, 25, 50],
                    getData: function ($defer, params) {
                        // use build-in angular filter
                        var newData = $scope.settingsUser.users;
                        // use build-in angular filter
                        newData = params.filter() ? $filter('filter')(newData,
                            params.filter()) : newData;
                        newData = params.sorting() ? $filter('orderBy')(newData,
                            params.orderBy()) : newData;

                        $scope.settingsUser.pageItemMin = (params.page() - 1) * params.count();
                        var pageItemMax = params.page() * params.count();
                        $scope.settingsUser.pageItemMax = pageItemMax > newData.length ? newData.length : pageItemMax;

                        $scope.data = newData.slice($scope.settingsUser.pageItemMin, pageItemMax);
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
                $scope.settingsUser.error.retrieve = {
                    status: status,
                    message: data.error.message
                };
                $scope.loading = false;
            });
        };

        // compute the number of disabled projects
        $scope.computeNUsersSyncDisabled = function(){
            $scope.settingsUser.nDisabled = 0;
            for (var i = 0; i < $scope.settingsUser.users.length; i++){
                if (!$scope.settingsUser.users[i].projectSettings.enabled){
                    $scope.settingsUser.nDisabled = $scope.settingsUser.nDisabled+1;
                }
            }
        };

        // re-fetch the users for that project
        $scope.refreshPage = function () {
            $scope.tmp.tableSorting = $scope.tableParams ? $scope.tableParams.sorting() : $scope.tmp.tableSorting;
            $scope.loadUsers();
        };

        // execute user loading
        $scope.loadUsers();

        // set the enabled/disabled status for the project
        $scope.setProjectEnabled = function(user, flag){

            var chbx = document.getElementById(user.cmID + ":checkBox:projectEnabled");

            // check if the executing user is trying to alter its own project access
            if ($scope.main.user.id === user.cmID){
                chbx.checked = !flag;
                sharedService.addAlert("You cannot remove yourself from this project!", "warning");
                return;
            }

            $log.debug("Attempting to change image access settings for user " +
            user.cmUserName + " on project " + $scope.projectID + " from " + !flag + " to " + flag);

            // make post request to the server via the settings service
            settingsService.setProjectAccess($scope.projectID, user.cmID, !flag, flag, user.projectSettings.id,
                function(data){
                    sharedService.addAlert("Changed access settings for " + user.cmFirstName + " "
                    + user.cmLastName + " on project " + $scope.projectID + " from " + !flag + " to " + flag + ".", "success");
                    $log.debug("Successful!");

                    $scope.computeNUsersSyncDisabled();
                }, function(data, status, header, config){
                    sharedService.addAlert("Cannot alter access settings for " + user.cmFirstName + " "
                    + user.cmLastName + " on project " + $scope.projectID + " from " + !flag + " to " + flag + "!", "danger");
                    chbx.checked = !flag;
                    $log.error("Failed!");

                    $scope.computeNUsersSyncDisabled();
                });
        };


        // START IMAGE ACCESS MODAL DIALOG

        // open the modal for image access settings
        $scope.openImageAccessModal = function (user, dlgData) {
            $log.debug(user);
            var modalInstance = $modal.open({
                templateUrl: 'settingsUserImagesModal.html',
                controller: modalCtrl,
                size : 'lg',
                resolve: { // put all variables here that should be available in the modal
                    project: function () {
                        return $scope.projectID;//dlgData.data;
                    },
                    user: function () {
                        return user;
                    },
                    error: function () {
                        return undefined;//dlgData.error;
                    }
                }
            });

            modalInstance.result.then(function (result) {
                // callbackSuccess branch
                $log.debug('User Images Access Modal: ' + result);
            }, function (result) {
                // callbackError branch
                $log.debug('User Images Access Modal: ' + result);
            });
        };

        // controller for the image access modal
        var modalCtrl = function ($scope, $modalInstance, $sce, project,
                                  user, error) {

            $scope.projectID = project;
            $scope.user = user;
            $scope.error = error;

            $scope.setImageEnabled = function(image, flag){

                var chbx = document.getElementById(image.cmID + ":checkBox:imageEnabled");

                $log.debug("Attempting to change image access settings for user " +
                user.cmUserName + " on project " + $scope.projectID +
                " and image " + image.cmID + " from " + !flag + " to " + flag);

                // make post request to the server via the settings service
                settingsService.setImageAccess($scope.projectID, image.cmID, user.cmID, !flag, flag, image.settings.id,
                    function(data){
                        sharedService.addAlert("Changed access settings for " + user.cmFirstName + " "
                        + user.cmLastName + " on project " + $scope.projectID +
                        ", image " + image.cmID + " from " + !flag + " to " + flag + ".", "success");
                        $log.debug("Successful!");

                        $scope.$broadcast("computeNImagesDisabled");
                    }, function(data, status, header, config){
                        sharedService.addAlert("Cannot alter access settings for " + user.cmFirstName + " "
                        + user.cmLastName + " on project " + $scope.projectID +
                        ", image " + image.cmID + " from " + !flag + " to " + flag + "!", "danger");
                        chbx.checked = !flag;
                        $log.error("Failed!");
                        $scope.$broadcast("computeNImagesDisabled");
                    });
            };

            $scope.ok = function () {
                $modalInstance.close('OK');
            };

            $scope.cancel = function () {
                $modalInstance.dismiss('cancel');
            };
        };
        // END IMAGE ACCESS MODAL DIALOG
        // ###############################################################
    }]);


iris.controller(
    "imageAccessTableCtrl", [
        "$rootScope", "$scope", "$http", "$filter",
        "$document", "$timeout", "$location", "$route",
        "$routeParams", "$log", "hotkeys",
        "cytomineService", "projectService", "imageService", "sessionService",
        "helpService", "sharedService", "settingsService", "annotationService",
        "ngTableParams",
        function($rootScope, $scope, $http, $filter,
                 $document, $timeout, $location, $route,
                 $routeParams, $log, hotkeys,
                 cytomineService, projectService, imageService, sessionService,
                 helpService, sharedService, settingsService, annotationService,
                 ngTableParams) {
            $log.debug("imageAccessTableCtrl");

            // retrieve the project parameter from the URL
            $scope.projectID = $routeParams["projectID"];

            // get the current date as long
            $scope.today = sharedService.today;

            $scope.image = {
                stillNew : (21 * (24 * 60 * 60 * 1000)), // last 21 days
                error : {},
                opening : {}
            };

            $scope.$on("computeNImagesDisabled", function(){
                $scope.computeNImagesDisabled();
            });

            $scope.computeNImagesDisabled = function(){
                $scope.image.nDisabled = 0;
                for (var i = 0; i < $scope.image.total; i++){
                    if (!$scope.image.images[i].settings.enabled){
                        $scope.image.nDisabled = $scope.image.nDisabled+1;
                    }
                }
            };

            // refresh the page
            $scope.refreshPage = function(){
                // show the loading button
                $scope.loading = true;

                settingsService.fetchAllImages($scope.projectID, $scope.user.cmID, function(data) {
                    // success
                    $scope.image.images = data; // this should be an IRIS image list
                    $scope.image.total = data.length;

                    $scope.computeNImagesDisabled();

                    if (data.length < 1){
                        $scope.image.error.empty= {
                            message : "This project does not have any images."
                        };
                        $scope.loading = false;
                        return;
                    } else {
                        delete $scope.image.error;
                    }

                    // set the user preference
                    $scope.hide = (data[0].projectSettings.hideCompletedImages === true);

                    // copy the values since THIS FREAKING NG-TABLE CAN ONLY FILTER FOR
                    // DIRECT ATTRIBUTES!!! THAT SHIT COST ME 2 HOURS OF MY LIFE!
                    // many thanks, missing ng-table doc :)
                    for (var i = 0; i < data.length; i++){
                        $scope.image.images[i].userProgress = $scope.image.images[i].settings.progress;
                    }

                    // build the data table
                    $scope.tableParams = new ngTableParams(
                        {
                            // define the parameters
                            page : 1, // show first page
                            count : 10, // count per page
                            sorting : {
                                // initial sorting
                                'settings.progress' : 'desc'
                            },
                            filter : {
                                // applies filter to the "data" object before sorting
                                'userProgress' : $scope.hide?"!100":""
                            }
                        }, {
                            // compute the pagination view
                            total : $scope.image.images.length, // number of images
                            getData : function($defer, params) {
                                // use build-in angular filter
                                var newData = $scope.image.images;

                                // fixed sorting for images with no annotations
                                params.sorting()['settings.numberOfAnnotations'] = params.sorting()['settings.progress'];

                                $log.debug("FILTERS:", params.filter());
                                $log.debug("SORTING:", params.sorting());

                                // use build-in angular filter
                                newData = params.filter() ? $filter('filter')(newData,
                                    params.filter()) : newData;
                                newData = params.sorting() ? $filter('orderBy')(
                                    newData, params.orderBy()) : newData;

                                $scope.image.pageItemMin = (params.page() - 1) * params.count();

                                var pageItemMax = params.page() * params.count();
                                $scope.image.pageItemMax = pageItemMax > newData.length ? newData.length : pageItemMax;

                                $scope.data = newData.slice($scope.image.pageItemMin, pageItemMax);
                                params.total(newData.length); // set total for recalc pagination

                                $defer.resolve($scope.data);
                            },
                            filterDelay : 0
                        });

                    $log.info("image refresh successful");
                    // hide or show the completed images
                    $scope.loading = false;
                }, function(data, status) {
                    // image fetching failed
                    $scope.image.error = {
                        retrieve: {
                            status: status,
                            message: data.error.message
                        }
                    };
                    $scope.loading = false;
                });
            };

            //	fetch the images
            $scope.refreshPage();

            //	//////////////////////////////////////////
            //	declare additional methods
            //	//////////////////////////////////////////
            //	Determine the row's background color class according
            //	to the current labeling progress.
            $scope.addKeys = function(url){
                try {
                    return cytomineService.addKeys(url);
                } catch (e){
                    return '';
                }
            };
        }]);