var iris = angular.module("irisApp");

iris.controller("navCtrl", [
"$scope", "$window", "$route", "$location", "$log", "sharedService", "navService",
"projectService", "helpService", "sessionService", "imageService", "breadcrumbs",
        function($scope, $window, $route, $location, $log, sharedService, navService,
		projectService, helpService, sessionService, imageService, breadcrumbs) {
	$log.debug("navCtrl");
	
	$scope.breadcrumbs = breadcrumbs;
	
	// enable session refresh button
	$scope.blockSessionRefresh = false;

	// navigation active tab controller
	$scope.isActive = function(viewLocation) {
		// $log.debug($location.path())

		// full match
		if ($location.path() === viewLocation) {
			return true;
		}
		// partial (suffix) match
		else if (sharedService.strEndsWith($location.path(), viewLocation)) {
			return true;
		}
		// partial (suffix) match
		else if (sharedService.strContains($location.path(), viewLocation)) {
			return true;
		}
		// no match
		else {
			return false;
		}
	};

	// navigate to the current annotation for labeling
	$scope.labeling = function() {
		navService.navToLabelingPage();
	};
	
	// navigate to the available projects
	$scope.projects = function() {
		navService.navToProjects();
	};
	
	// navigate to the current project's images
	$scope.images = function() {
		navService.navToImages();
	};
	
	// navigate to the current project's annotations
	$scope.annotations = function() {
		navService.navToAnnotationGallery();
	};
	
	// show the help page
	$scope.showHelp = function() {
		helpService.showHelp();
	};
	
	// computes the breadcrumbs from the current application state
	$scope.getProjectName = function(){
		try {
			// get the project name if a project is opened
			var session = sessionService.getSession();
			var cProject = session.currentProject;
			$scope.bcProjectName = cProject.cmName;
		} catch (e){
			delete $scope.bcProjectName;
			delete $scope.bcImageName;
			delete $scope.bcAnnotationID;
			
//			$log.debug("cannot read project name");
		}
	};
	
	// computes the breadcrumbs from the current application state
	$scope.getImageName = function(){
		try {
			// get the project name
			$scope.getProjectName();
			
			var cProject = sessionService.getCurrentProject();
			var cImage = cProject.currentImage;
			var imageName = cImage.originalFilename;
			$scope.bcImageName = imageName;
		} catch (e){
			delete $scope.bcImageName;
			delete $scope.bcAnnotationID;
			
//			$log.debug("cannot read image name");
		}
	};
	
	// computes the breadcrumbs from the current application state
	$scope.resolveBreadcrumbs = function(){
		try {
			$scope.getImageName();
			// get the project name if a project is opened
			var cProject = sessionService.getCurrentProject();
			var cImage = cProject.currentImage;
			$scope.bcAnnotationID = cImage.currentCmAnnotationID;
			return $scope.bcAnnotationID;
		} catch (e){
			delete $scope.bcAnnotationID;
			return undefined;
		}
	};
	
	$scope.refreshSession = function(){
		if ($scope.blockSessionRefresh){
			return;
		}
		
		$scope.blockSessionRefresh = true;
		sessionService.fetchSession(function(data){
			$log.debug("Successfully refreshed session.");
			$scope.blockSessionRefresh = false;
		},function(data,status){
			$log.error("Session refresh failed!");
			$scope.blockSessionRefresh = false;
		});
	};
	
	$scope.hasSession = function(){
		return (sessionService.getSession() != null);
	};
}]);
