/**
 * Created by phil on 09/05/15.
 */
var iris = angular.module("irisApp");

iris.constant("annAgreementStatsListURL", "api/stats/{projectID}/agreements/list.json");

iris.factory("statisticsService", [
    "$http", "$log",
    "annAgreementStatsListURL",
    "sessionService",
    "cytomineService",
    function($http, $log,
             annAgreementStatsListURL,
             sessionService,
             cytomineService) {

        return {
            // get the annotations for a given project, images, terms and users
            fetchAnnotationAgreementList : function(projectID, imageIDs, termIDs, userIDs, callbackSuccess,
                                                  callbackError, offset, max) {
                $log.debug("Getting annotation term list: " + projectID + " - " + imageIDs + " - " + termIDs);

                // modify the parameters
                var url = cytomineService.addKeys(annAgreementStatsListURL).replace("{projectID}", projectID);

                if (imageIDs !== null){
                    url += ("&images=" + imageIDs.toString().replace("[","").replace("]",""));
                }

                if (userIDs !== null){
                    url += ("&users=" + userIDs.toString().replace("[","").replace("]",""));
                }

                // add the terms of interest to the query
                if (termIDs !== null) {
                    url += ("&terms=" + termIDs.toString().replace("[", "").replace("]", ""));
                }

                // add pagination parameters
                if (offset) {
                    // 0 offset is counted as missing parameter and causes loading of first page
                    url += "&offset=" + offset;
                }
                if (max) {
                    url += "&max=" + max;
                }

                // execute the http get request to the IRIS server
                $http.get(url).success(function(data) {
                    // $log.debug("success on $http.get(" + url + ")");
                    //$log.debug(data);
                    if (callbackSuccess) {
                        callbackSuccess(data);
                    }
                }).error(function(data, status, headers, config) {
                    // on error log the error
//				$log.debug(callbackError)
                    if (callbackError) {
                        callbackError(data, status, headers, config);
                    }
                })
            }
        }
    }]);