angular
		.module("irisApp")
		.constant("welcomeUrl", "/api/welcome.json")
		.constant("appNameUrl", "/api/appName.json")
		.constant("appVersionUrl", "/api/appVersion.json")
		.config(function($logProvider) {
			$logProvider.debugEnabled(true);
		})
		.controller(
				"mainCtrl",
				function($scope, $http, $route, $location, welcomeUrl,
						appNameUrl, appVersionUrl, cytomineService, projectService) {
					console.log("mainCtrl");

					// declare variables for expression 
					$scope.main = {
						error : {}
					};

					// get the key from the local HTML5 storage
					$scope.publicKey = (localStorage.getItem("publicKey") ? localStorage
							.getItem("publicKey")
							: "");
					$scope.privateKey = (localStorage.getItem("privateKey") ? localStorage
							.getItem("privateKey")
							: "");
					$scope.publicKeyCurrent = $scope.publicKey;
					$scope.privateKeyCurrent = $scope.privateKey;
					cytomineService
							.setKeys($scope.publicKey, $scope.privateKey);

					// retrieve the welcome text
					$scope.getWelcome = function() {
						$http.get(welcomeUrl).success(function(data) {
							//console.log(data);
							$scope.main.welcome = data;
						}).error(function(data, status, headers, config) {
							$scope.main.error.retrieve = {
								status : status,
								message : data.errors
							};
						})
					};
					$scope.getWelcome();

					// retrieve the application name
					$scope.getAppName = function() {
						cytomineService.getAppName(appNameUrl,
								function(appName) {
									$scope.main.appName = appName;
								});
					}
					$scope.getAppName();

					// retrieve the application version
					$scope.getAppVersion = function() {
						cytomineService.getAppVersion(appVersionUrl, function(
								appVersion) {
							$scope.main.appVersion = appVersion;
						});
					}
					$scope.getAppVersion();

					// retrieve the Cytomine host address
					$scope.getCytomineHost = function() {
						cytomineService.getCytomineHost(function(cytomineHost) {
							$scope.main.cytomineHost = cytomineHost;
							console.log("Cytomine Host: " + $scope.main.cytomineHost)
						})
					};
					$scope.getCytomineHost();
					
					// retrieve the Cytomine web address
					$scope.getCytomineWeb = function() {
						cytomineService.getCytomineWeb(function(cytomineWeb) {
							$scope.main.cytomineWeb = cytomineWeb;
							console.log("Cytomine Web: " + $scope.main.cytomineWeb)
						})
					};
					$scope.getCytomineWeb();
				
					$scope.throwEx = function() {
						throw {
							message : 'error occurred!'
						}
					}

					$scope.saveKeys = function() {
						localStorage.setItem("publicKey",
								$scope.publicKeyCurrent);
						localStorage.setItem("privateKey",
								$scope.privateKeyCurrent);
						$scope.publicKey = $scope.publicKeyCurrent;
						$scope.privateKey = $scope.privateKeyCurrent;
						cytomineService.setKeys($scope.publicKey,
								$scope.privateKey);
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
						$location.url("/project/" + $scope.main.projectID + 
								"/image");
					};
					
					$scope.$on('currentProjectID', function(event, data){
						$scope.main.projectID = data;
					});
					$scope.$on('currentImageID', function(event, data){
						$scope.main.imageID = data;
					});
				});
