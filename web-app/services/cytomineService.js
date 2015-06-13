/**
 * Created by lrollus on 7/14/14.
 */
var iris = angular.module("irisApp");

iris.constant("appNameURL", "api/appName.json");
iris.constant("appVersionURL", "api/appVersion.json");
iris.constant("appInfoURL", "api/admin/appInfo.json");
iris.constant("cytomineHostURL", "public/cytomineHost.json");
iris.constant("cytomineWebURL", "public/cytomineWeb.json");
iris.constant("userPublicKeyURL", "api/user/publicKey/{pubKey}.json");
iris.constant("currentIRISUserURL", "api/user/current.json");

/**
 * This service is responsible for communicating HTTP requests to the IRIS server. 
 */
iris.factory("cytomineService", [
	"$http", "$log",
	"cytomineHostURL",
	"cytomineWebURL",
	"userPublicKeyURL",
	"appNameURL",
	"appVersionURL",
	"appInfoURL",
	"currentIRISUserURL",
	function($http, $log,
			 cytomineHostURL,
			 cytomineWebURL,
			 userPublicKeyURL,
			 appNameURL,
			 appVersionURL,
			 appInfoURL,
			 currentIRISUserURL) {

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
		getAppName : function(callbackSuccess, callbackError) {
			$http.get(this.addKeys(appNameURL)).success(function(data) {
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
		getAppVersion : function(callbackSuccess, callbackError) {
			$http.get(this.addKeys(appVersionURL)).success(function(data) {
				if (callbackSuccess){
					callbackSuccess(data);
				}
			}).error(function(data, status, headers, config) {
				if (callbackError){
					callbackError(data, status);
				}
			})
		},
		
		// get the application information (condensed)
		getAppInfo : function(callbackSuccess, callbackError) {
			$http.get(this.addKeys(appInfoURL)).success(function(data) {
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
		getCytomineHost : function(ping, callbackSuccess, callbackError) {
			var url = this.addKeys(cytomineHostURL);
			if (ping === false){
				url += ("?ping="+ping);
			}
			$http.get(url).success(function(data) {
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
			$http.get(this.addKeys(cytomineWebURL)).success(function(data) {
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
			var url = this.addKeys(userPublicKeyURL.replace("{pubKey}", publicKey));
			
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

		// get the current IRIS user identified by the locally stored public and private key pair
		getCurrentIRISUser : function(callbackSuccess, callbackError){
			var url = this.addKeys(currentIRISUserURL);

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
		}
	};
}]);
