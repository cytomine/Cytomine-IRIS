angular.module("irisApp", ["ngRoute","ngResource","ngTable"])
    .config(function($routeProvider, $locationProvider) {
        $routeProvider.when("/", {
            templateUrl: "views/welcome.html"
        });
        $routeProvider.when("/keys", {
            templateUrl: "views/keys.html"
        });
        $routeProvider.when("/project/:idProject/image", {
            templateUrl: "views/images.html"
        });
        $routeProvider.when("/projects", {
            templateUrl: "views/projects.html"
        });
        $routeProvider.when("/labeling", {
            templateUrl: "views/labeling.html"
        });
        $routeProvider.when("/annotation", {
            templateUrl: "views/annotationGallery.html"
        });
        // default route
        $routeProvider.otherwise({
            templateUrl: "views/welcome.html"
        });
    });
