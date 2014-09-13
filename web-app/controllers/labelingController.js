var iris = angular.module("irisApp");

iris.config(function($logProvider) {
	$logProvider.debugEnabled(true);
});

iris.controller("labelingCtrl", function($scope, $http, $filter, $location,
		$routeParams, $log, hotkeys, helpService, annotationService,
		projectService, sessionService, sharedService, imageService,
		cytomineService) {
	console.log("labelingCtrl");

	// preallocate the objects
	$scope.labeling = {
		annotation : {},
		ontology : {},
		error : {}
	};

	$scope.labeling.projectID = $routeParams["projectID"];
	$scope.labeling.imageID = $routeParams["imageID"];
	$scope.labeling.annotation.id = $routeParams["annID"];

	// set content url for the help page
	helpService.setContentUrl("content/help/labelingHelp.html");

	$scope.saving = {
		status : "",
	};

	// TODO DEBUG
	$scope.item = {
		userProgress : 45,
		labeledAnnotations : 45,
		numberOfAnnotations : 100
	};

	// put all valid shortcuts for this page here
	hotkeys.bindTo($scope).add({
		combo : 'h',
		description : 'Show help for this page',
		callback : function() {
			helpService.showHelp();
		}
	}).add({
		combo : 'n',
		description : 'Proceed to next annotation',
		callback : function() {
			// TODO
			console.log("move to next annotation")
		}
	}).add({
		combo : 'p',
		description : 'Go back to the previous annotation',
		callback : function() {
			// TODO
			console.log("go back to the previous annotation")
		}
	}).add({
		// TODO remove
		combo : 'd',
		description : 'print debug info on the project',
		callback : function() {
			// TODO

		}
	});

	// get the image URL for the crop view
	cytomineService
			.getCytomineHost(function(host) {
				$scope.labeling.annotation.cropURL = host + "/api/annotation/"
						+ $scope.labeling.annotation.id
						+ "/crop.png?&increaseArea=8&maxSize=256&draw=true";

				$scope.labeling.annotation.goToURL = sessionService
						.getCurrentImage().goToURL
						+ $scope.labeling.annotation.id
			});

	$scope.removeTerm = function() {
		console.log("remove term");
		// TODO
		$scope.saving.status = "saving"
	};

	// TODO

	$scope.maxSize = 4;
	$scope.totalItems = 64;
	$scope.currentPage = 1;
	$scope.numPages = 64;

});

iris.controller("termCtrl", function($scope, $log, $filter, $routeParams,
		sharedService, sessionService, projectService, ngTableParams) {
	console.log("termCtrl")

	// this corresponds to resolvedOntology.attr
	// 136435754 (2x hierarchical)
	// 95190197 (BM-01) 1x hierarchical
	// 585609 (ALCIAN BLUE) // flat

	var cp = sessionService.getCurrentProject();
	// $log.debug(cp)

	// TODO get the ontologyID from the current project
	projectService.fetchOntology(cp.cytomine.ontology, { flat : true }, function(data) {
		$scope.ontology = data;

		// build the ontology table
		$scope.tableParams = new ngTableParams({
			page : 1, // show first page
			count : 25, // count per page
			// leave this blank for no sorting at all
			sorting : {
				name : 'asc' // initial sorting
			},
			filter : {
				// applies filter to the "data" object before sorting
			}
		}, {
			total : $scope.ontology.length, // number of terms
			getData : function($defer, params) {
				// use build-in angular filter
				var newData = $scope.ontology;
				// use build-in angular filter
				newData = params.filter() ? $filter('filter')(newData,
						params.filter()) : newData;
				newData = params.sorting() ? $filter('orderBy')(newData,
						params.orderBy()) : newData;
				$scope.data = newData.slice((params.page() - 1)
						* params.count(), params.page() * params.count());
				params.total(newData.length); // set total for recalc
				// pagination
				$defer.resolve($scope.data);
			},
			filterDelay : 0,
		});
	});

	$scope.assign = function(term) {
		$log.debug("assigning term '" + term.name + "' to annotation");
		sharedService.addAlert("Term " + term.name + " has been assigned.");
	}

	$scope.postAssignment = function(child, annotation) {

	}
});
