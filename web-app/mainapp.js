var iris = angular.module("irisApp", [ "ngRoute", "ngResource", "ngTable",
		"ui.bootstrap", "cfp.hotkeys", "ui.tree" ]);

// include application wide route-specific cheat sheets
iris.config(function(hotkeysProvider) {
    hotkeysProvider.includeCheatSheet = true;
  });

iris.config(function($routeProvider, $locationProvider) {
	$routeProvider.when("/", {
		templateUrl : "views/welcome.html",
	});
	$routeProvider.when("/login", {
		templateUrl : "views/login.html",
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
	$routeProvider.when("/project/:projectID/gallery", {
		templateUrl : "views/annotationGallery.html"
	});
	// default route
//	$routeProvider.otherwise({
//		templateUrl : "views/welcome.html"
//	});
});