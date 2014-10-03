var iris = angular.module("irisApp", [ "ngRoute", "ngResource", "ngTable", 
                                       "ui.bootstrap", "cfp.hotkeys", "treeControl", 
                                       "ngDraggable", "openlayers-directive", "ngSanitize" ]);

// include application wide route-specific cheat sheets
iris.config(function(hotkeysProvider) {
    hotkeysProvider.includeCheatSheet = true;
  });

iris.run(['$route', '$rootScope', '$location', function ($route, $rootScope, $location) {
    var original = $location.path;
    $location.path = function (path, reload) {
        if (reload === false) {
            var lastRoute = $route.current;
            var un = $rootScope.$on('$locationChangeSuccess', function () {
                $route.current = lastRoute;
                un();
            });
        }
        return original.apply($location, [path]);
    };
}])

iris.config(function($routeProvider, $locationProvider) {
	$routeProvider.when("/", {
		templateUrl : "views/welcome.html",
	});
	$routeProvider.when("/map", {
		templateUrl : "views/map.html",
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
	$routeProvider.when("/project/:projectID/image/:imageID/label/", {
		templateUrl : "views/labeling.html"
	});
//	$routeProvider.when("/project/:projectID/image/:imageID/label/:annID", {
//		templateUrl : "views/labeling.html"
//	});
	$routeProvider.when("/project/:projectID/gallery", {
		templateUrl : "views/annotationGallery.html"
	});
	// default route
//	$routeProvider.otherwise({
//		templateUrl : "views/welcome.html"
//	});
});