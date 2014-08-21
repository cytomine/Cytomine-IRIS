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
		userName : "DOE John (jdoe)",
		error : {}
	};

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

	$scope.images = function() {
		$scope.main.projectID = projectService.getProjectID();
		$location.url("/project/" + $scope.main.projectID + "/images");
	};

	$scope.$on('currentProjectID', function(event, data) {
		$scope.main.projectID = data;
	});
	$scope.$on('currentImageID', function(event, data) {
		$scope.main.imageID = data;
	});

	// TODO just for DEBUG
	$scope.main.imageID = imageService.getImageID();
	$scope.main.projectID = projectService.getProjectID();
	
	$scope.throwEx = function() {
		throw {
			message : 'error occurred!'
		}
	}
});