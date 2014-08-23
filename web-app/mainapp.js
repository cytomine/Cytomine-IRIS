var iris = angular.module("irisApp", [ "ngRoute", "ngResource", "ngTable",
		"ui.bootstrap" ]);

iris.config(function($routeProvider, $locationProvider) {
	$routeProvider.when("/", {
		templateUrl : "views/welcome.html"
	});
	$routeProvider.when("/keys", {
		templateUrl : "views/keys.html"
	});
	$routeProvider.when("/projects", {
		templateUrl : "views/projects.html"
	});
	$routeProvider.when("/project/:projectID/images", {
		templateUrl : "views/images.html"
	});
	$routeProvider.when("/project/:projectID/image/:imageID/label", {
		templateUrl : "views/labeling.html"
	});
	$routeProvider.when("/annotation", {
		templateUrl : "views/annotationGallery.html"
	});
	// default route
//	$routeProvider.otherwise({
//		templateUrl : "views/welcome.html"
//	});
});