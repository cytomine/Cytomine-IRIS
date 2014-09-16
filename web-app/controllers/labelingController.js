var iris = angular.module("irisApp");

iris.config(function($logProvider) {
	$logProvider.debugEnabled(true);
});

iris.controller("labelingCtrl", function($scope, $http, $filter, $location, $timeout,
		$routeParams, $log, hotkeys, ngTableParams, helpService, annotationService,
		projectService, sessionService, sharedService, imageService,
		cytomineService) {
	console.log("labelingCtrl");

	// preallocate the objects
	$scope.labeling = {
		annotations : {},
		error : {}
	};
	
	$scope.ontology = {};

	$scope.projectID = $routeParams["projectID"];
	$scope.imageID = $routeParams["imageID"];
	$scope.annotationID = $routeParams["annID"];

	// set content url for the help page
	helpService.setContentUrl("content/help/labelingHelp.html");

	// an object for saving progress status
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

	annotationService.fetchUserAnnotations($scope.projectID, $scope.imageID,
			function(data) {
				$log.debug("retrieved " + data.length + " annotations");
				$scope.labeling.annotations = data;
				$scope.totalItems = data.length;
				$scope.itemsPerPage = 1;
				// show the first page
				$scope.currentPage = 1;
				
			}, function(data, status) {
				sharedService.addAlert("Cannot get user annotations! Status "
						+ status + ".", "danger");
			});

	// TODO pagination settings
	//$scope.maxSize = 4;
	//$scope.numPages = 64;
	var nextRequestBlocked = false;
	
	$scope.$watch('currentPage', function(){
		if (nextRequestBlocked){
			// TODO workaround for not crashing server by DoS
			$log.debug("blocked sending request");
			return;
		}
		
		$log.debug("Setting current page to " + $scope.currentPage)
		// switch through the array of annotations
		$scope.annotation = $scope.labeling.annotations[$scope.currentPage-1];
		
		try {
			$log.debug($scope.annotation);
			$timeout(function(){ 
				nextRequestBlocked = false;
			}, 1000);
			sessionService.touchAnnotation($scope.projectID, $scope.imageID, $scope.annotation.cmID, function(data){
				$log.debug("successfully touched annotation " + data.id)
			});
			nextRequestBlocked = true;
		} catch(e){
			$log.error(e.message);
		}
	});
	
	$scope.loadOntology = function(){
		$scope.loadingTerms = true;
		
		var cp = sessionService.getCurrentProject();
		
		// get the ontologyID from the current project
		projectService.fetchOntology(cp.cytomine.ontology, {
			flat : true
		}, function(data) {
			$scope.ontology = data;

			// build the ontology table
			$scope.tableParams = new ngTableParams({
				page : 1, // show first page
				count : 25, // count per page
				// leave this blank for no sorting at all
				sorting : {
					name : 'desc' // initial sorting
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
			$scope.loadingTerms = false;
		});
	};
	
	// fetch the ontology on page reload
	$scope.loadOntology();
	
	// assign a term to the current annotation
	$scope.assign = function(term) {
		$scope.saving.status = "saving";
		annotationService.setAnnotationTerm($scope.projectID, 
				$scope.imageID, $scope.annotation.cmID, term.id,
				function(data){
			$log.debug("assigning term '" + term.name + "' to annotation.");
			sharedService.addAlert("Term '" + term.name + "' has been assigned.");
			$scope.currentTerm = term;
			$scope.saving.status = "success";
			$timeout(function(){ $scope.saving.status = ""; }, 2000);
		}, function(data, status){
			sharedService.addAlert("Cannot assign term '" + term.name + "'. Status " + status + ".", "danger");
			$scope.saving.status = "error";
			$scope.currentTerm = null;
			$timeout(function(){ $scope.saving.status = ""; }, 2000);
		});
	}
	
	// removes a term
	$scope.removeTerm = function() {
		var term;
		var termID;
		try {
			term = $scope.currentTerm;
			termID = $scope.currentTerm.id;
		} catch(e){
			$log.error(e);
			return;
		}
		$scope.saving.status = "saving";
		annotationService.deleteAnnotationTerm($scope.projectID, 
				$scope.imageID, $scope.annotation.cmID, termID,
				function(data){
			$log.debug("Removing term '" + term.name + "' from annotation.");
			sharedService.addAlert("Term '" + term.name + "' has been removed.");
			$scope.saving.status = "success";
			$timeout(function(){ $scope.saving.status = ""; }, 2000);
		}, function(data, status){
			sharedService.addAlert("Cannot remove term '" + term.name + "'. Status " + status + ".", "danger");
			$scope.saving.status = "error";
			$timeout(function(){ $scope.saving.status = ""; }, 2000);
		});
	};
	
	$scope.setButtonClass = function(term) {
		// TODO set the corresponding button selected
		var button = document.getElementById(term.id);
		
	}
	
	$scope.reset = function() {
		// TODO unselect all terms (e.g. if term is successfully removed)
		
	}
});