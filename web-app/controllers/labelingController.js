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
		annotationTuple : {},
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
			$scope.moveToNextAnnotation();
		}
	}).add({
		combo : 'p',
		description : 'Go back to the previous annotation',
		callback : function() {
			$scope.moveToPreviousAnnotation();
		}
	});

	// get a new 3-tuple (around annID) from the server
	$scope.fetchNewTuple = function(annID, displayCurrentAnnotation) {
		annotationService.fetchUserAnnotations3Tuple($scope.projectID, $scope.imageID, annID,
		function(data) {
//			$log.debug(data)
			$scope.labeling.annotationTuple = data;
			$log.debug("retrieved " + data.size + " annotations");
			
			if (displayCurrentAnnotation === true){
				$scope.annotation = data.currentAnnotation;
				$log.debug("Setting the current annotation for display.");
			}
			
		}, function(data, status) {
			sharedService.addAlert("Cannot get user annotations! Status "
					+ status + ".", "danger");
		});
	};
	
	// fetch the new tuple (from start = null), or from current annotation
	// on loading set the flag to true, if the current annotation should be displayed
	$scope.fetchNewTuple($scope.annotationID, true);
	
	$scope.moveToNextAnnotation = function(){
		var hasNext = $scope.labeling.annotationTuple.hasNext;
		if (!hasNext){
			sharedService.addAlert("This is the last annotation.", "info");
			return;
		}
		
		$log.debug("Fetching next annotation")
		// get the next annotation ID from the array
		var nextAnnID = $scope.labeling.annotationTuple.nextAnnotation.cmID
		
		// blend in next annotation
		$scope.annotation = $scope.labeling.annotationTuple.nextAnnotation

		// fetch the next 3-tuple from the server, currentAnnotation = nextAnnotation.cmID
		if ($scope.labeling.annotationTuple.hasNext){
			$scope.fetchNewTuple(nextAnnID, false)
		}
	};
	
	$scope.moveToPreviousAnnotation = function(){
		var hasPrevious = $scope.labeling.annotationTuple.hasPrevious;
		if (!hasPrevious){
			sharedService.addAlert("This is the first annotation.", "info");
			return;
		}
		
		$log.debug("Fetching previous annotation")
		// get the previous annotation ID
		var prevAnnID = $scope.labeling.annotationTuple.previousAnnotation.cmID
		
		// blend in previous annotation
		$scope.annotation = $scope.labeling.annotationTuple.previousAnnotation

		// fetch the 3-tuple from the server, currentAnnotation = previousAnnotation.cmID
		if ($scope.labeling.annotationTuple.hasPrevious){
			$scope.fetchNewTuple(prevAnnID, false)
		}
	};
	
		
	
	///////////////////////
	// ONTOLOGY functions
	///////////////////////
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
			// map the results to the local element
			$scope.annotation.cmTermName = term.name;
			$scope.annotation.cmTermID = term.id;

			sharedService.addAlert("Term '" + term.name + "' has been assigned.");

			$scope.saving.status = "success";
			$timeout(function(){ $scope.saving.status = ""; }, 2000);
		}, function(data, status){
			sharedService.addAlert("Cannot assign term '" + term.name + "'. Status " + status + ".", "danger");
			$scope.saving.status = "error";
			$timeout(function(){ $scope.saving.status = ""; }, 2000);
		});
	};
	
	// removes a term
	$scope.removeTerm = function() {
		var termID = $scope.annotation.cmTermID;
		var termName = $scope.annotation.cmTermName;
		
		$scope.saving.status = "saving";
		annotationService.deleteAnnotationTerm($scope.projectID, 
				$scope.imageID, $scope.annotation.cmID, termID,
				function(data){
			// remove the elements from the local object
			$scope.annotation.cmTermName = null;
			$scope.annotation.cmTermID = 0;
			$scope.annotation.cmOntologyID = 0;
			
			$log.debug("Removing term '" + termName + "' from annotation.");
			sharedService.addAlert("Term '" + termName + "' has been removed.");
			$scope.saving.status = "success";
			$timeout(function(){ $scope.saving.status = ""; }, 2000);
		}, function(data, status){
			sharedService.addAlert("Cannot remove term '" + termName + "'. Status " + status + ".", "danger");
			$scope.saving.status = "error";
			$timeout(function(){ $scope.saving.status = ""; }, 2000);
		});
	};
});