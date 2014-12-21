var iris = angular.module("irisApp");

iris.controller("labelingCtrl", function($scope, $http, $filter, $location, $timeout,
		$routeParams, $log, hotkeys, ngTableParams, helpService, annotationService,
		projectService, sessionService, sharedService, navService, imageService,
		cytomineService) {
	$log.debug("labelingCtrl");

	// preallocate the objects
	$scope.labeling = {
		annotationTuple : {},
		annotations : {},
		hideCompleted : (sessionService.getCurrentImage().prefs['annotations.hideCompleted'] === 'true'),
	};
	
	$scope.ontology = {};

	// TODO fetch the updated session
	//sessionService.fetchSession();
	
	$scope.projectID = $routeParams["projectID"];
	$scope.imageID = $routeParams["imageID"];
	
	// get the current annotation according to the current image
	$scope.annotationID = sessionService.getCurrentAnnotationID()==null?
			null:sessionService.getCurrentAnnotationID();

	// set content url for the help page
	helpService.setContentUrl("content/help/labelingHelp.html");

	// an object for saving progress status
	$scope.saving = {
		status : "",
	};

	// Initialize the variable for the progress
	$scope.item = {
		userProgress : 0,
		labeledAnnotations : 0,
		numberOfAnnotations : 0
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
		$scope.navDisabled = true;
		annotationService.fetchUserAnnotations3Tuple(
				$scope.projectID, 
				$scope.imageID, 
				$scope.labeling.hideCompleted, 
				annID,
		function(data) {
			// delete the errors
			delete $scope.labeling.error;
//			$log.debug(data)
			$scope.labeling.annotationTuple = data;
			$log.debug("retrieved " + data.size + " annotations");
			
			if (displayCurrentAnnotation === true){
				$scope.annotation = data.currentAnnotation;
				$log.debug("Setting the current annotation for display.");
			}
			
			// map the progress to the 'item' for the progress bar
			$scope.item = {
				userProgress : data.userProgress,
				labeledAnnotations : data.labeledAnnotations,
				numberOfAnnotations : data.numberOfAnnotations
			};

			// set the current annotation to the local session
			sessionService.setCurrentAnnotationID(data.currentAnnotation.cmID);
			
			// set the hidden state of the annotations to the local session
			var cImg = sessionService.getCurrentImage();
			cImg.prefs['annotations.hideCompleted'] = data.hideCompleted;
			sessionService.setCurrentImage(cImg);
			
			// enable navigation
			$scope.navDisabled = false;
		}, function(data, status) {
			var msg = "";
			if (status === 404){
				msg = "This image does not have any annotations!";
				// add message
				$scope.labeling.error = {
						empty : { 
							message : msg,
							status : status
							}
				}
			} else if (status === 412){
				$scope.labeling.error = {
						retrieve : { 
							message : data.error.message,
							status : status
						}
				}
			}
						
			$log.error(data);
			
			// set the current annotation to the local session
			sessionService.setCurrentAnnotationID(null);

			$scope.navDisabled = false;
//			sharedService.addAlert(msg + " Status "
//					+ status + ".", "danger");
		});
	};
	
	// fetch the new tuple (from start = null), or from current annotation
	// on loading set the flag to true, if the current annotation should be displayed
	$scope.fetchNewTuple($scope.annotationID, true);
	
	$scope.moveToNextAnnotation = function(){
		// check for disabled next button (e.g. while loading)
		if ($scope.navDisabled){
			return;
		}
		
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
		// check for disabled navigation (e.g. while loading)
		if ($scope.navDisabled){
			return;
		}
		
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
	
	// shows or hides completed annotations
	$scope.showOrHideCompleted = function() {
		$log.debug($scope.labeling.hideCompleted);
		
		// fetch new tuple
		$scope.fetchNewTuple($scope.labeling.annotationTuple.currentAnnotation.cmID, true);
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
	try {
		$scope.loadOntology();
	} catch (e) {
		$scope.labeling.error = {}
	}
	
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

			// REFLECT UPDATED LABELING PROGRESS
			$scope.updateLabelingProgress();
			$timeout(function(){ $scope.saving.status = ""; }, 2000);
		}, function(data, status){
			sharedService.addAlert("Cannot assign term '" + term.name + "'. Status " + status + ".", "danger");
			$scope.saving.status = "error";

			// REFLECT UPDATED LABELING PROGRESS
			$scope.updateLabelingProgress();
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
			
			// REFLECT UPDATED LABELING PROGRESS
			$scope.updateLabelingProgress();
			$timeout(function(){ $scope.saving.status = ""; }, 2000);
		}, function(data, status){
			sharedService.addAlert("Cannot remove term '" + termName + "'. Status " + status + ".", "danger");
			$scope.saving.status = "error";
			
			// REFLECT UPDATED LABELING PROGRESS
			$scope.updateLabelingProgress();
			$timeout(function(){ $scope.saving.status = ""; }, 2000);
		});
	};
	
	// fetch the labeling status of the current image 
	$scope.updateLabelingProgress = function(){
		sessionService.getLabelingProgress($scope.projectID, $scope.imageID, function(data){
			console.log("updated status")
			$scope.item = {
					userProgress : data.userProgress,
					labeledAnnotations : data.labeledAnnotations,
					numberOfAnnotations : data.totalAnnotations
				};
			$log.debug($scope.item)
		}, function(data, status, header, config){
			sharedService.addAlert("Cannot update progress! Status " + status + ".", "danger");
		})
	}
	
	// navigates to the image list
	$scope.navToImages = function(){
		navService.navToImages();
	};
});