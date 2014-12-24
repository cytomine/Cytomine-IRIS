var iris = angular.module("irisApp");

iris.constant("appNameUrl", "/api/appName.json");
iris.constant("appVersionUrl", "/api/appVersion.json");

iris.controller("mainCtrl", function($scope, $http, $route, $location, $modal,
		$log, welcomeUrl, appNameUrl, appVersionUrl, cytomineService,
		projectService, imageService, sharedService) {
	$log.debug("mainCtrl");
	
	// declare variables for expression
	$scope.main = {};
	
	// get the key from the local HTML5 storage
	$scope.publicKey = (localStorage.getItem("publicKey") ? localStorage
			.getItem("publicKey") : "");
	$scope.privateKey = (localStorage.getItem("privateKey") ? localStorage
			.getItem("privateKey") : "");
	$scope.publicKeyCurrent = $scope.publicKey;
	$scope.privateKeyCurrent = $scope.privateKey;
	
	// set the keys each time the main controller loads
	cytomineService.setKeys($scope.publicKey, $scope.privateKey);

	// save the keys in the local storage
	$scope.saveKeys = function() {
		if ($scope.publicKeyCurrent != "" && $scope.privatKeyCurrent != ""){
			localStorage.setItem("publicKey", $scope.publicKeyCurrent);
			localStorage.setItem("privateKey", $scope.privateKeyCurrent);
			$scope.publicKey = $scope.publicKeyCurrent;
			$scope.privateKey = $scope.privateKeyCurrent;
			cytomineService.setKeys($scope.publicKey, $scope.privateKey);
			
			cytomineService.getUserByPublicKey($scope.publicKeyCurrent,function(data){
				// user callback was successful
				$scope.main.user = data;
				
				delete $scope.error;
				window.location.reload();
			},function(data, status){
				$scope.error = {
						message : "Public or private keys are incorrect!",
					}
				$scope.changeKeys();
			});
		}else {
			$scope.error = {
				message : "The keys must not be empty!",
			}
			$scope.changeKeys();
		}
	}

	$scope.changeKeys = function() {
		$scope.$broadcast('clearKeys', []);
	}

	$scope.$on('clearKeys', function(event, mass) {
		$scope.publicKey = "";
		$scope.privateKey = "";
		$scope.publicKeyCurrent = "";
		$scope.privateKeyCurrent = "";
		delete $scope.main.user;
		localStorage.removeItem("publicKey");
		localStorage.removeItem("privateKey");
		localStorage.removeItem("session");
	});
	
	// add an alert 
	$scope.addAlert = function(message) {
		sharedService.addAlert(message);		
	};
	
	cytomineService.getUserByPublicKey($scope.publicKey,function(data){
		// user callback was successful
		$scope.main.user = data;
	},function(data, status){
		// do nothing
	});
	
	$scope.isIE = function(){
		$log.info($.browser.msie);
		return $.browser.msie;
	}
});