/**
 * Created by lrollus on 7/14/14.
 */
var iris = angular.module("irisApp");

iris.constant("cytomineHostUrl", "api/cytomineHost.json");
iris.constant("cytomineWebUrl", "api/cytomineWeb.json");
iris.constant("userPublicKeyUrl", "api/user/publicKey/{pubKey}.json");

/**
 * This service is responsible for communicating HTTP requests to the IRIS server. 
 */
iris.factory("cytomineService", [
	"$http", "$log", "cytomineHostUrl", "cytomineWebUrl", "userPublicKeyUrl",
	function($http, $log, cytomineHostUrl, cytomineWebUrl, userPublicKeyUrl) {

	var publicKey; 
	var privateKey; 
	
	return {

		// returns the public key
		getPublicKey : function() {
			return publicKey;
		},

		// returns the private key
		getPrivateKey : function() {
			return privateKey;
		},

		// sets the keys
		setKeys : function(publicK, privateK) {
			$log.debug("set key=" + publicK + " " + privateK);
			publicKey = publicK;
			privateKey = privateK;
		},

		// add the keys to the end of an existing URL and return
		// the concatenated URL
		addKeys : function(url) {
			url = url + ((url.indexOf("?") == -1) ? "?" : "&");
			url = url + "publicKey={publicKey}&privateKey={privateKey}";
			url = url.replace("{publicKey}", this.getPublicKey());
			url = url.replace("{privateKey}", this.getPrivateKey());
			return url;
		},

		// get the application name and execute the callback
		getAppName : function(url, callbackSuccess, callbackError) {
			$http.get(this.addKeys(url)).success(function(data) {
				if (callbackSuccess){
					callbackSuccess(data);
				}
			}).error(function(data, status, headers, config) {
				if (callbackError){
					callbackError(data, status);
				}
			})
		},

		// get the application version number and execute the callback
		getAppVersion : function(url, callbackSuccess, callbackError) {
			$http.get(this.addKeys(url)).success(function(data) {
				if (callbackSuccess){
					callbackSuccess(data);
				}
			}).error(function(data, status, headers, config) {
				if (callbackError){
					callbackError(data, status);
				}
			})
		},

		// get the cytomine host address
		getCytomineHost : function(callbackSuccess, callbackError) {
			$http.get(this.addKeys(cytomineHostUrl)).success(function(data) {
				if (callbackSuccess){
					callbackSuccess(data);
				}
			}).error(function(data, status, headers, config) {
				if (callbackError){
					callbackError(data, status);
				}
			})
		},

		// get the cytomine web address and execute the callback
		getCytomineWeb : function(callbackSuccess, callbackError) {
			$http.get(this.addKeys(cytomineWebUrl)).success(function(data) {
				if (callbackSuccess){
					callbackSuccess(data);
				}
			}).error(function(data, status, headers, config) {
				if (callbackError){
					callbackError(data, status);
				}
			})
		}, 
		
		// get the user by the public key
		getUserByPublicKey : function(publicKey, callbackSuccess, callbackError){
			var url = this.addKeys(userPublicKeyUrl.replace("{pubKey}", publicKey));
			
			$http.get(url).success(function(data) {
				//console.log(data)
				if (callbackSuccess){
					callbackSuccess(data);
				}
			}).error(function(data, status, headers, config){
				if (callbackError){
					callbackError(data, status);
				}
			});
		},
	};
}]);
