var iris = angular.module("irisApp");

iris.constant("sessionURL", "api/session.json");
iris.constant("openProjectURL",
    "api/project/{projectID}.json");
iris.constant("updateProjectSettingsURL",
    "api/project/{projectID}/settings.json");
iris.constant("openImageURL",
    "api/project/{projectID}/image/{imageID}.json");
iris.constant("labelingProgressURL",
    "api/project/{projectID}/image/{imageID}/progress.json");

// A generic service for handling client sessions.
iris.factory("sessionService", [
    "$http", "$log", "$location", "sessionURL",
    "openProjectURL", "updateProjectSettingsURL", "openImageURL", "labelingProgressURL", "sharedService",
    "cytomineService", "$rootScope",
    function ($http, $log, $location, sessionURL,
              openProjectURL, updateProjectSettingsURL, openImageURL, labelingProgressURL, sharedService,
              cytomineService, $rootScope) {

        return {
            // ###############################################################
            // SESSION MANAGEMENT

            // retrieve the session
            getSession: function () {
                return JSON.parse(localStorage.getItem("session"));
            },

            // set the session to the local storage and broadcast an update event
            setSession: function (sess) {
                if (sess) {
                    localStorage.setItem("session", JSON.stringify(sess));
                } else {
                    localStorage.removeItem("session");
                }

                $rootScope.$broadcast('sessionUpdate', { session: sess });
            },

            // retrieve the current session for a user identified by public key
            // this will overwrite the existing local session in order to reflect
            // any updates performed by this user on another client
            fetchSession: function (callbackSuccess, callbackError) {
                var sessionService = this;
                var url = cytomineService.addKeys(sessionURL);
                // $log.debug(url)

                $http.get(url).success(function (data) {
                    $log.debug("Successfully retrieved session. ID=" + data.id);

                    // put the session to the local storage
                    sessionService.setSession(data);

                    if (callbackSuccess) {
                        callbackSuccess(data);
                    }
                }).error(
                    function (data, status, header, config) {
                        $log.error("Error retrieving session.");

                        sessionService.setSession(undefined);

                        if (callbackError) {
                            callbackError(data, status);
                        }
                    })
            },

            // retrieve the currently active project
            getCurrentProject: function () {
                return this.getSession().currentProject;
            },

            // set the currently active project
            setCurrentProject: function (project) {
                var sess = this.getSession();
                if (project) {
                    sess.currentProject = project;
                } else {
                    sess.currentProject = null;
                }
                this.setSession(sess);
            },

            // opens a project
            openProject: function (cmProjectID, callbackSuccess, callbackError) {
                var sessionService = this;

                var url = cytomineService.addKeys(openProjectURL)
                    .replace("{projectID}", cmProjectID);

                $http.get(url).success(function (data) {
                    // on success, update the project in the local storage
                    // including the current image and annotation in that project
                    sessionService.setCurrentProject(data);

                    $log.debug(data);
                    if (callbackSuccess) {
                        callbackSuccess(data)
                    }
                }).error(function (data, status, header, config) {
                    // on error, log the error message
                    $log.error(data);

                    sessionService.setCurrentProject(undefined);

                    if (callbackError) {
                        callbackError(data, status)
                    }
                });
            },

            // updates a project in the current session, payload is the IRIS project
            // instance
            updateProjectSettings: function (irisProjectSettings, callbackSuccess, callbackError) {
                var sessionService = this;

                var url = cytomineService.addKeys(updateProjectSettingsURL)
                    .replace("{projectID}", irisProjectSettings.cmProjectID);

                $http.put(url, irisProjectSettings).success(function (data) {
                    // on success, update the project in the local storage
                    // including the current image and annotation in that project
                    sessionService.setCurrentProject(data);

                    $log.debug("Successfully updated current project settings.");

                    if (callbackSuccess) {
                        callbackSuccess(data)
                    }
                }).error(
                    function (data, status, header, config) {

                        sessionService.setCurrentProject(undefined);

                        if (callbackError) {
                            callbackError(data, status)
                        }
                    });
            },

            // retrieve the currently active image
            getCurrentImage: function () {
                return this.getCurrentProject().currentImage;
            },

            // set the currently active image
            setCurrentImage: function (image) {
                var project = this.getCurrentProject();
                if (image) {
                    project.currentImage = image;
                } else {
                    project.currentImage = null;
                }
                this.setCurrentProject(project);
            },

            // opens an image in the current session
            openImage: function (cmProjectID, cmImageID, callbackSuccess,
                                 callbackError) {
                var sessionService = this;

                var url = cytomineService.addKeys(openImageURL).replace("{projectID}",
                    cmProjectID).replace("{imageID}", cmImageID);

                $http.get(url).success(function (data) {
                    // on success, update the image in the local storage
                    sessionService.setCurrentImage(data);

                    if (callbackSuccess) {
                        callbackSuccess(data)
                    }
                }).error(function (data, status, header, config) {
                    sessionService.setCurrentImage(undefined);
                    if (callbackError) {
                        callbackError(data, status)
                    }
                });

            },

            // retrieve the current annotation id in the currently active image
            getCurrentAnnotationID: function () {
                var img = this.getCurrentImage();
                return img.settings.currentCmAnnotationID;
            },

            // set the current annotation id in the currently active image
            setCurrentAnnotationID: function (annID) {
                var img = this.getCurrentImage();
                if (annID) {
                    img.settings.currentCmAnnotationID = annID;
                } else {
                    img.settings.currentCmAnnotationID = null;
                }
                this.setCurrentImage(img);
            },

            // gets the labeling progress (freshly computed) for an image in a project
            getLabelingProgress: function (cmProjectID, cmImageID, callbackSuccess,
                                           callbackError) {
                var sessionService = this;

                var url = cytomineService.addKeys(labelingProgressURL).replace("{projectID}",
                    cmProjectID).replace("{imageID}", cmImageID);

                $http.get(url).success(function (data) {
                    if (callbackSuccess) {
                        callbackSuccess(data)
                    }
                }).error(function (data, status, header, config) {
                    $log.error(data);
                    if (callbackError) {
                        callbackError(data, status)
                    }
                });
            }

            // END SESSION MANAGEMENT
            // ###############################################################
        }
    }]);