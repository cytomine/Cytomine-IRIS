/**
* Created by phil on 15/10/15.
*/
var iris = angular.module("irisApp");

iris.constant("downloadImageDatasetURL", "api/download/{projectID}/annotations/images.json");

iris.factory("downloadService",
    ["$http", "$log", "$interval", "$timeout", "$q", "$cookieStore",
    "downloadImageDatasetURL",
    "sessionService",
    "cytomineService",
    function($http, $log, $interval, $timeout, $q, $cookieStore,
             downloadImageDatasetURL,
             sessionService,
             cytomineService) {

    var generateIframeDownload = function(source){
        var iframe = document.createElement('iframe');
        $cookieStore.put('download_file', 'true');

        iframe.src = cytomineService.addKeys(source);
        iframe.style.display = "none";
        document.body.appendChild(iframe);
    };

    var manageIframeProgress = function(){
        var defer = $q.defer();

        // notify that the download is in progress every half a second / do this for a maximum of 50 intervals
        var promise = $interval(function () {
            if (!$cookieStore.get('download_file')){
                $interval.cancel(promise);
            }
        }, 500, 50);

        promise.then(defer.reject, defer.resolve, defer.notify);

        promise.finally(function () {
            $cookieStore.remove('download_file');
            document.body.removeChild(iframe);
        });
    };

    return {
        requestDownloadURL: function (params) {
            var defer = $q.defer();

            // notify that the download is in progress every half a second
            // the calling function provides a notifyCallback, that gets called every 500 ms.
            $interval(function (i) {
                defer.notify(i);
            }, 500);

            // modify the parameters
            var url = cytomineService.addKeys(downloadImageDatasetURL).replace("{projectID}", params.projectID);

            // construct the payload
            var payload = params;

            // get the URL of the file
            $http.post(url, payload).success(function(data){
                params.resp = data;
                defer.resolve(params);
            }).error(function (data, status){
                defer.reject("Sorry, but this file cannot be downloaded!");
            });

            return defer.promise;
        },
        downloadFile: function(params) {

            generateIframeDownload(params.resp.source);
            var promise = manageIframeProgress();

            // once the download is completed, this code gets executed
            $cookieStore.remove('download_file');

            return promise;
        }
    }
}]);