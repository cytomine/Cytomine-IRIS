/**
 * Created by lrollus on 7/14/14.
 */
angular.module("irisApp")
		.constant("cytomineHostUrl", "/api/cytomineHost.json")
		.constant("cytomineWebUrl", "/api/cytomineWeb.json")
		.factory("cytomineService", function($http, cytomineHostUrl, cytomineWebUrl) {

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
			console.log("set key=" + publicK + " " + privateK);
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
		getAppName : function(url, callback) {
			$http.get(this.addKeys(url)).success(function(data) {
				callback(data);
			}).error(function(data, status, headers, config) {
				callback({
					status : status,
					message : data.errors
				});
			})
		},

		// get the application version number and execute the callback
		getAppVersion : function(url, callback) {
			$http.get(this.addKeys(url)).success(function(data) {
				callback(data);
			}).error(function(data, status, headers, config) {
				callback({
					status : status,
					message : data.errors
				});
			})
		},

		// get the cytomine host address
		getCytomineHost : function(callback) {
			$http.get(this.addKeys(cytomineHostUrl)).success(function(data) {
				callback(data);
			}).error(function(data, status, headers, config) {
				callback({
					status : status,
					message : data.errors
				});
			})
		},

		// get the cytomine web address and execute the callback
		getCytomineWeb : function(callback) {
			$http.get(this.addKeys(cytomineWebUrl)).success(function(data) {
				callback(data);
			}).error(function(data, status, headers, config) {
				callback({
					status : status,
					message : data.errors
				});
			})
		}
	};
});
