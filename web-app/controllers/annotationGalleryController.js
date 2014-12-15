var iris = angular.module("irisApp");

iris.config(function($logProvider) {
	$logProvider.debugEnabled(true);
});

iris.controller("annotationGalleryCtrl", function($rootScope, $scope, $http, $filter, $log,
		$location, hotkeys, helpService,navService, annotationService, sessionService, sharedService, $routeParams) {
	console.log("annotationGalleryCtrl");

	// set content url for the help page
	helpService.setContentUrl("content/help/annGalleryHelp.html");
	
	$scope.annotation = {
		groups : [] // this variable holds all terms and their annotations
	};

	// selected terms for filtering
	var selectedTerms = [];
	// selected images for filtering
	var selectedImages = [];
	// selected annotations (e.g. for drag-and-drop)
	$scope.selectedAnnotations = {};

	$scope.projectID = $routeParams["projectID"];
	$scope.projectName = sessionService.getCurrentProject().cmName;
	
	// put all valid shortcuts for this page here
	hotkeys.bindTo($scope)
	.add({
		combo : 'h',
		description : 'Show help for this page',
		callback : function() {
			helpService.showHelp();
		}
	});

	// TODO implement drag and drop feature for single annotations
    $scope.droppedObjects = [];
    $scope.onDropComplete=function(data,evt,termID){
    	// if the term ID does not change, skip the element(s)
        if (data.cmTermID == termID){
        	return;
        }
    	
    	var index = $scope.droppedObjects.indexOf(data);
        if (index == -1){
        	// add the item
        	$scope.droppedObjects.push(data);
        }

        // TODO handle the -99 term (remove term)
        $log.debug("TODO assigning unique term " + $rootScope.termList[termID] +
        		" to annotation " + data.cmID);
    };
    $scope.onDragSuccess=function(data,evt){
        var index = $scope.droppedObjects.indexOf(data);
        if (index > -1) {
        	// remove the item
            $scope.droppedObjects.splice(index, 1);
        }
    };
    
    // load annotations from the server
    $scope.fetchAnnotations = function(termIDs, imageIDs){
    	$scope.showOrHideNoImageWarning();
    	if (imageIDs.length === 0){
    		$log.debug("No images to retrieve annotations for.");
    		return;
    	}
    	
    	if (termIDs.length === 0){
    		$log.debug("No terms to retrieve annotations for.");
    		return;
    	}

    	$log.debug("Fetching annotations for terms " + termIDs + " for " + imageIDs.length + " images.");
    	
    	// set the loading spinner
    	$scope.loading = true;
    	
    	// perform the query
    	annotationService.fetchUserAnnotationsByTerm(sessionService.getCurrentProject().cmID, 
    			imageIDs, termIDs, function(data){
    		
    		//$log.debug(data);
    		
    		// resolve the map data to the groups
    		for (var termID in data) {
    		    if (data.hasOwnProperty(termID)) {
//    		    	$log.debug("Resolving group for term '" + $rootScope.termList[termID] + "'")
		    		var obj = {termName: $rootScope.termList[termID], termID: termID, annotations : data[termID]};
		    		
		    		// search for the object in the array and remove it
		    		for (var idx = 0; idx < $scope.annotation.groups.length; idx++){
		    			var existingObj = $scope.annotation.groups[idx];
		    			if (existingObj.termID == termID){
		    				$scope.annotation.groups.splice(idx,1);
		    				break;
		    			}
		    		}
		    		// then add the new objects
		    		$scope.annotation.groups.push(obj);
    		    }
    		}
    		
    		// sort the groups according to the newly added elements
    		$scope.annotation.groups.sort(sortGroups);
  
    		// finally delete the error message
    		delete $scope.error
    		
    		// disable the loading spinner
    		delete $scope.loading;
    	}, function(data,status,header,config){
    		$scope.error = {
    			message : data.error.message,
    			status : status
    		};
    		$log.error(status);
    		
    		// disable the loading spinner
    		delete $scope.loading;
    	});
    };
    
    $scope.showOrHideNoLabelWarning = function(){
    	$scope.warn = {
    			noLabel : {}
    	};
    	if (selectedTerms.length === 0){
    		$scope.warn.noLabel = {
    				message : "There is no label term selected, please choose at least one from the ontology on the left side."
    		}
    		$log.info("No terms selected, won't fetch any annotations, but show warning.");
    	} else {
    		delete $scope.warn.noLabel;
    	}
    }
    
    $scope.showOrHideNoImageWarning = function(){
    	$scope.warn = {
    			noImage : {}
    	};
    	if (selectedImages.length === 0){
    		// reset the groups
    		$scope.annotation.groups = [];
    		$scope.warn.noImage = {
    				message : "There is no image selected, please choose at least one from the image list on the left side."
    		}
    		$log.info("No image(s) selected, won't fetch any annotations, but show warning.");
    	} else {
    		delete $scope.warn.noImage;
    	}
    }
    
    $scope.$on("termFilterChange", function(event, object){
    	var action = object.action;
//    	$log.debug(event.name + "::" + action + ": "  + object.name + " [" + object.id + "].");
    	
    	if (action === 'add'){
    		// incremental fetching
    		// fetch the selected term for the selected images
    		selectedTerms.push(object.id);
    		$scope.fetchAnnotations([object.id], selectedImages);
    	} else if (action === 'remove'){
    		// remove the selected term
    		selectedTerms.splice(selectedTerms.indexOf(object.id), 1)
    		// remove the group from the 'groups' array
    		for (var idx = 0; idx < $scope.annotation.groups.length; idx++){
    			if ($scope.annotation.groups[idx].termID == object.id){
    				$scope.annotation.groups.splice(idx,1);
    				break;
    			}
    		}
    		// no need to reorder array, since we just remove from a sorted list
    	} else if (action === 'addAll'){
    		// here the id is the entire array
    		selectedTerms = object.id; 
    		// clear the groups
    		$scope.annotation.groups = [];
    		$scope.fetchAnnotations(selectedTerms, selectedImages);
    	} else if (action === 'removeAll'){
    		// reset the stuff
    		selectedTerms = [];
    		$scope.annotation.groups = [];
    	}
    });
    
    // react to changes in the image filter panel
    $scope.$on("imageFilterChange", function(event, imageList){
    	var action = imageList.action;
//    	$log.debug(event.name + "::" + action + ": "  + imageList.ids + ".");
    	
    	if (action === 'selectedImages'){
    		// get the selected images list
    		selectedImages = imageList.id;

    		// perform the query using the currently selected terms
    		$scope.fetchAnnotations(selectedTerms, selectedImages);
    	} else {
    		$log.error("Unrecognized image filter action: '" + action + "'");
    	}
    });
    
    // select or unselect an annotation (for multi-drag and drop)
    $scope.selectAnnotation = function(ann){
    	if ($scope.selectedAnnotations[ann.cmID]){
    		// remove it
    		delete $scope.selectedAnnotations[ann.cmID];
    	} else {
    		$scope.selectedAnnotations[ann.cmID] = true;
    	}
    	
    	$log.debug($scope.selectedAnnotations);
    };    
    
    // opens the labeling interface and jumps to the current annotation
    $scope.labelAnnotation = function(ann){
    	$log.debug("Computing URL for annotation " +  ann.cmID);
    	
    	sessionService.touchImage(ann.cmProjectID, ann.cmImageID, function(data){
    		sessionService.setCurrentAnnotationID(ann.cmID);
			$log.debug("Successfully touched image " + ann.cmImageID);
			navService.navToLabelingPage(ann.cmProjectID, ann.cmImageID);
			delete $scope.error;
		}, function(data,status){
			$scope.error = {
    			message : data.error.message,
    			status : status
    		};
    		$log.error(status);
			sharedService.addAlert("Cannot update image. Error " + status + ".", "danger");
		});
    };
    
    $scope.selectAllAnnotations = function(termID){
    	$log.debug("Checking all annotations of group " +  $rootScope.termList[termID]);
    	var annIDs = getGroup($scope.annotation.groups, termID).annotations;
    	for (var idx = 0; idx < annIDs.length; idx++){
    		var chbx = document.getElementById("checkbox-" + annIDs[idx].cmID);
    		// overwrite the selection state
    		$scope.selectedAnnotations[annIDs[idx].cmID] = true;
    		chbx.checked = true;
    	}
    	
    	$log.debug($scope.selectedAnnotations);
    }; 
    
    $scope.unSelectAllAnnotations = function(termID){
    	$log.debug("UNChecking all annotations of group " +  $rootScope.termList[termID]);
    	var annIDs = getGroup($scope.annotation.groups, termID).annotations;
    	for (var idx = 0; idx < annIDs.length; idx++){
    		var chbx = document.getElementById("checkbox-" + annIDs[idx].cmID);
        	delete $scope.selectedAnnotations[annIDs[idx].cmID];
    		chbx.checked = false;
    	}
    	$log.debug($scope.selectedAnnotations);
    }; 
    
    // reload all annotations
    $scope.refreshPage = function() {
    	$log.debug("Refreshing page...");
    	$scope.clearAllSelectedItems();
    	$scope.fetchAnnotations(selectedTerms, selectedImages);
    };
    
    // assign a selected term to all selected annotations
    $scope.assignTermToSelectedItems = function(){
    	var termID = getKey($rootScope.termList, $scope.chosenTerm);
    	var nSelections = getSize($scope.selectedAnnotations);
    	if (termID === undefined || nSelections === 0) {
    		$log.debug("No term chosen or no item selected, returning.");
    		return;
    	}
    	$log.debug("Assigning term " + $scope.chosenTerm + "(" + termID + ") to " + nSelections + " annotations.");
    	
    	// TODO implement server calls
    };
    
    // checks for selected items
    $scope.hasSelections = function(){
    	return !(getSize($scope.selectedAnnotations) === 0);
    };
    
    // clears all selected items
    $scope.clearAllSelectedItems = function(){
    	var keys = Object.keys($scope.selectedAnnotations);
    	
    	for (var idx = 0; idx < keys.length; idx++){
    		var id = keys[idx];
    		var chbx = document.getElementById("checkbox-" + id);
    		chbx.checked = false;
    	}
    	
    	// clear selections
    	$scope.selectedAnnotations = {};
    };
});

function sortGroups(a, b) {
	if (a.termName < b.termName)
		return -1;
	if (a.termName > b.termName)
		return 1;
	return 0;
};

function getGroup(groups, termID) {
	// search for the object in the array
	for (var idx = 0; idx < groups.length; idx++){
		var existingObj = groups[idx];
		if (existingObj.termID == termID){
			return groups[idx];
		}
	}
	return null;
};

function getKey(object, value){
	// search for the object in the array
	for( var prop in object ) {
        if( object.hasOwnProperty( prop ) ) {
             if( object[ prop ] === value )
                 return prop;
        }
    }
};

function getSize(obj) {
    var size = 0, key;
    for (key in obj) {
        if (obj.hasOwnProperty(key)) size++;
    }
    return size;
};