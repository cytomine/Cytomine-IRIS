var iris = angular.module("irisApp");

iris.constant("appNameUrl", "/api/appName.json");
iris.constant("appVersionUrl", "/api/appVersion.json");

iris.config(function($logProvider) {
	$logProvider.debugEnabled(true);
});

iris.controller("mainCtrl", function($scope, $http, $route, $location, $modal,
		$log, welcomeUrl, appNameUrl, appVersionUrl, cytomineService,
		projectService, imageService, sharedService) {
	console.log("mainCtrl");
	
	// declare variables for expression
	$scope.main = {
		user : {},
		error : {}
	};
	
	// TODO remove debug keys
//	function setKeys() {
//		localStorage.setItem("publicKey", "0880e4b4-fe26-4967-8169-f15ed2f9be5c");
//		localStorage.setItem("privateKey", "a511a35c-5941-4932-9b40-4c8c4c76c7e7");
//	}
	//setKeys();

	// get the key from the local HTML5 storage
	$scope.publicKey = (localStorage.getItem("publicKey") ? localStorage
			.getItem("publicKey") : "");
	$scope.privateKey = (localStorage.getItem("privateKey") ? localStorage
			.getItem("privateKey") : "");
	$scope.publicKeyCurrent = $scope.publicKey;
	$scope.privateKeyCurrent = $scope.privateKey;
	
	// set the keys each time the main controller loads
	cytomineService.setKeys($scope.publicKey, $scope.privateKey);

	// retrieve the application name
	$scope.getAppName = function() {
		cytomineService.getAppName(appNameUrl, function(appName) {
			$scope.main.appName = appName;
		});
	}
	$scope.getAppName();

	// retrieve the application version
	$scope.getAppVersion = function() {
		cytomineService.getAppVersion(appVersionUrl, function(appVersion) {
			$scope.main.appVersion = appVersion;
		});
	}
	$scope.getAppVersion();

	

	// save the keys in the local storage
	$scope.saveKeys = function() {
		localStorage.setItem("publicKey", $scope.publicKeyCurrent);
		localStorage.setItem("privateKey", $scope.privateKeyCurrent);
		$scope.publicKey = $scope.publicKeyCurrent;
		$scope.privateKey = $scope.privateKeyCurrent;
		cytomineService.setKeys($scope.publicKey, $scope.privateKey);
		window.location.reload();
	}

	$scope.changeKeys = function() {
		$scope.$broadcast('clearKeys', []);
	}

	$scope.$on('clearKeys', function(event, mass) {
		$scope.publicKey = "";
		$scope.privateKey = "";
		$scope.publicKeyCurrent = "";
		$scope.privateKeyCurrent = "";
		localStorage.removeItem("publicKey");
		localStorage.removeItem("privateKey");
	});

	$scope.throwEx = function() {
		throw {
			message : 'error occurred!'
		}
	};
	
	// add an alert to the 
	$scope.addAlert = function(message) {
		sharedService.addAlert(message);		
	};
	
	// update the current user information according to the public key
	$scope.getUser = function(publicKey){
		cytomineService.getUserByPublicKey(publicKey,function(data){
			// user callback was successful
			$scope.main.user = data;
		},function(data, status){
			$log.error(status + ": " + data);
		});
	}, 
	// TODO 
	$scope.getUser(cytomineService.getPublicKey());
	
	// TODO debug fetching the URLs for the openlayers
	//imageService.fetchImageServerURLs(94255014,94255021);
	
});