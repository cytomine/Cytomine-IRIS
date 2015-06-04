var iris = angular.module("irisApp");

iris.controller("settingsSyncCtrl", [
    "$rootScope", "$routeParams", "$scope", "$http", "$filter", "$location", "$document",
    "$modal", "$log", "hotkeys", "settingsService", "helpService", "sharedService", "sessionService", "ngTableParams", "navService", "$cookieStore",
    "$timeout",
    function ($rootScope, $routeParams, $scope, $http, $filter, $location, $document,
              $modal, $log, hotkeys, settingsService, helpService, sharedService, sessionService, ngTableParams, navService, $cookieStore,
              $timeout) {
        $log.debug("settingsSyncCtrl");

        $scope.settingsSync = {
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

                delete $scope.settingsSync.error.retrieve;

                $scope.settingsSync.users = data;

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
                    total: $scope.settingsSync.users.length, // number of users
                    counts: [10, 25, 50],
                    getData: function ($defer, params) {
                        // use build-in angular filter
                        var newData = $scope.settingsSync.users;
                        // use build-in angular filter
                        newData = params.filter() ? $filter('filter')(newData,
                            params.filter()) : newData;
                        newData = params.sorting() ? $filter('orderBy')(newData,
                            params.orderBy()) : newData;

                        $scope.settingsSync.pageItemMin = (params.page() - 1) * params.count();
                        var pageItemMax = params.page() * params.count();
                        $scope.settingsSync.pageItemMax = pageItemMax > newData.length ? newData.length : pageItemMax;

                        $scope.data = newData.slice($scope.settingsSync.pageItemMin, pageItemMax);
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
                $scope.settingsSync.error.retrieve = {
                    status: status,
                    message: data.error.message
                };
                $scope.loading = false;
            });
        };

        // compute the number of disabled projects
        $scope.computeNUsersSyncDisabled = function(){
            $scope.settingsSync.nDisabled = 0;
            for (var i = 0; i < $scope.settingsSync.users.length; i++){
                if (!$scope.settingsSync.users[i].synchronize){
                    $scope.settingsSync.nDisabled = $scope.settingsSync.nDisabled+1;
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

        $scope.synchronizeAllProjectUsers = function(){
            $log.debug("All users will be synchronized for project " + $scope.projectID);

            $scope.disableSync = true;

            // call service method
            settingsService.synchronizeAllProjectUsers($scope.projectID, function(data){
                sharedService.addAlert("Synchronization of all project users was successful!", "success");
                $log.debug("Success!");
                $scope.disableSync = false;
            }, function(data, status){
                $log.error(data);
                sharedService.addAlert("Could not synchronize all project users! Status: " + status, "danger");
                $scope.disableSync = false;
            });
        };

        // set the enabled/disabled status for the project
        $scope.setUserAutoSync = function(user, flag){

            var chbx = document.getElementById(user.cmID + ":checkBox:userAutoSync");

            $log.debug("Attempting to change auto-sync settings for user " +
            user.cmUserName + " on project " + $scope.projectID + " from " + !flag + " to " + flag);

            // make post request to the server via the settings service
            settingsService.setUserAutoSync($scope.projectID, user.cmID, !flag, flag, user.projectSettings.id,
                function(data){
                    sharedService.addAlert("Changed auto-sync settings for " + user.cmFirstName + " "
                    + user.cmLastName + " on project " + $scope.projectID + " from " + !flag + " to " + flag + ".", "success");
                    $log.debug("Successful!");

                    $scope.computeNUsersSyncDisabled();
                }, function(data, status, header, config){
                    sharedService.addAlert("Cannot alter auto-sync settings for " + user.cmFirstName + " "
                    + user.cmLastName + " on project " + $scope.projectID + " from " + !flag + " to " + flag + "!", "danger");
                    chbx.checked = !flag;
                    $log.error("Failed!");
                    // let the model not change its value
                    user.synchronize = !flag;

                    $scope.computeNUsersSyncDisabled();
                });
        };

        // synchronize a single user manually
        $scope.syncUser = function(user){

            // change the model binding
            $scope.disableSync = true;

            $log.debug("Attempting to manually synchronize user " +
                user.cmUserName + " on project " + $scope.projectID);

            // make post request to the server via the settings service (all images will be synced)
            settingsService.triggerProjectSync($scope.projectID, user.cmID, null, function(data){
                    sharedService.addAlert("Synchronized labeling progress for " + user.cmFirstName + " "
                        + user.cmLastName + ".", "success");
                    $log.debug("Successful!");

                    $scope.disableSync = false;
                }, function(data, status, header, config){
                    sharedService.addAlert("Cannot synchronize labeling progress for " + user.cmFirstName + " "
                    + user.cmLastName + "!", "danger");
                    $log.error("Failed!");

                    $scope.disableSync = false;
                });
        };
    }]);