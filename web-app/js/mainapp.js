var iris;
iris = angular.module("irisApp", ["ngRoute", "ngResource", "ngTable",
	"ui.bootstrap", "cfp.hotkeys", "treeControl",
	"ngDraggable", "openlayers-directive", "ngSanitize",
	"ng-breadcrumbs", "ngCookies",
	"com.2fdevs.videogular",
	"com.2fdevs.videogular.plugins.controls",
	"com.2fdevs.videogular.plugins.overlayplay",
	"com.2fdevs.videogular.plugins.poster"]);

// include application wide route-specific cheat sheets
iris.config(["hotkeysProvider", function(hotkeysProvider) {
    hotkeysProvider.includeCheatSheet = true;
}]);

// log configuration
iris.config(["$logProvider", function($logProvider){
	$logProvider.debugEnabled(true);
}]);


iris.run(["$route", "$rootScope", "$location", function ($route, $rootScope, $location) {
	var original = $location.path;
	$location.path = function (path, reload) {
		if (reload === false) {
			var lastRoute = $route.current;
			var un = $rootScope.$on("$locationChangeSuccess", function () {
				$route.current = lastRoute;
				un();
			});
		}
		return original.apply($location, [path]);
	};
}]);

iris.config(["$routeProvider", function($routeProvider) {
	$routeProvider.when("/", {
		templateUrl : "views/welcome.html",
		label : "Home"
	});
//	$routeProvider.when("/map", {
//		templateUrl : "views/map.html",
//	});
	$routeProvider.when("/keys", {
		templateUrl : "views/keys.html",
		label : "Keys"
	});
	$routeProvider.when("/projects", {
		templateUrl : "views/projects.html",
		label : "Projects"
	});
	$routeProvider.when("/project/:projectID/images", {
		templateUrl : "views/images.html",
		label : "Images"
	});
	$routeProvider.when("/project/:projectID/stats", {
		templateUrl : "views/projectstatistics.html",
		label : "Project Statistics"
	});
	$routeProvider.when("/project/:projectID/settings", {
		templateUrl : "views/projectsettings.html",
		label : "Project Settings"
	});
	$routeProvider.when("/project/:projectID/image/:imageID/label", {
		templateUrl : "views/labeling.html",
		label : "Labeling"
	});
	$routeProvider.when("/project/:projectID/image/:imageID/label/:annID", {
		templateUrl : "views/labeling.html",
		label : "Labeling"
	});
	$routeProvider.when("/project/:projectID/image/:imageID/gallery", {
		templateUrl : "views/annotationGallery.html",
		label : "Gallery"
	});
	$routeProvider.when("/project/:projectID/gallery", {
		templateUrl : "views/annotationGallery.html",
		label : "Gallery"
	});
	$routeProvider.when("/about", {
		templateUrl : "views/about.html",
		label : "About"
	});
	//$routeProvider.when("/login", {
	//	templateUrl : "views/login.html",
	//	label : "Login"
	//});
	$routeProvider.when("/400", {
		templateUrl : "views/400.html"
	});
	$routeProvider.when("/500", {
		templateUrl : "views/500.html"
	});
	$routeProvider.when("/404", {
		templateUrl : "views/404.html"
	});
	// default route (unknown)
	$routeProvider.otherwise({
		templateUrl : "views/404.html"
	});
}]);

// OTHER SYSTEM WIDE CONFIGURATION
$(".fancybox").fancybox({
	openEffect	: "elastic",
	closeEffect	: "elastic"
});

