var iris = angular.module("irisApp");

iris.controller("annotationGalleryCtrl", [
"$rootScope", "$scope", "$http", "$filter", "$log",
"$location", "hotkeys", "helpService", "cytomineService", 
"navService", "annotationService", "sessionService", "sharedService", "$routeParams",
                                          function($rootScope, $scope, $http, $filter, $log,
		$location, hotkeys, helpService, cytomineService, navService, annotationService, sessionService, sharedService, $routeParams) {
	$log.debug("annotationGalleryCtrl");

	// set content url for the help page
	helpService.setContentUrl("content/help/annGalleryHelp.html");
	
	$scope.annotation = {
		groups : [] // this variable holds all terms and their annotations
	};
	
	// selected terms for filtering
	var selectedTerms = [];
	// selected images for filtering
	var selectedImages = [];
	// selected annotations (via check box)
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
	
	// assigns a new term to an annotation
	$scope.assignNewTerm = function(item,targetTermID){
		// if the term ID does not change, skip the element(s)
        if (item.cmTermID == targetTermID){
        	return false;
        }
        
        // handle the -99 term (remove term)
        if (targetTermID == -99){
        	var termName = $rootScope.termList[item.cmTermID];
        	
        	$log.debug("Removing term " + termName +
        			" from annotation " + item.cmID);
        	
        	// perform server call
        	annotationService.deleteAnnotationTerm(item.cmProjectID, item.cmImageID, item.cmID, targetTermID,
    				function(data){
    			// if call was successful, remove the term from the source group and add it to the target group
        		var _srcGroup = getGroup($scope.annotation.groups, item.cmTermID);
    			var _srcGroupAnnList = _srcGroup.annotations;
    			var _srcAnnIdx = getAnnotationIndex($scope.annotation.groups, item.cmTermID, item.cmID);
    			
    			_srcGroupAnnList.splice(_srcAnnIdx, 1);
    			_srcGroup.totalItems = _srcGroup.totalItems - 1;
    			try {
    				var _targetGroup = getGroup($scope.annotation.groups, targetTermID);
    				var _targetGroupAnnList = _targetGroup.annotations;
    				// change the information in the local object
    				item.cmTermName = null;
    				item.cmTermID = -99;
    				item.cmOntologyID = 0;

    				_targetGroupAnnList.push(item)
    				_targetGroup.totalItems = _targetGroup.totalItems + 1;
    			} catch (e) {
    				$log.debug("Term has been removed, but group is not filtered.");
    			}
    			
    			sharedService.addAlert("Term '" + termName + "' has been removed.");
    			
    		}, function(data, status, config, header){
    			// don't do anything to the local stuff if an error occurs
    			sharedService.addAlert("Cannot remove term '" + termName + "'. Status " + status + ".", "danger");
    		});
        } else {
        	var termName = $rootScope.termList[targetTermID];
        	
        	$log.debug("Assigning term " + termName +
        			" to annotation " + item.cmID);
        	
        	// perform the server call
        	annotationService.setAnnotationTerm(item.cmProjectID, item.cmImageID, item.cmID, targetTermID, 
        			function(data){
        		
        		// if call was successful, remove the term from the source group and add it to the target group
        		var _srcGroup = getGroup($scope.annotation.groups, item.cmTermID);
        		var _srcGroupAnnList = _srcGroup.annotations;
    			var _srcAnnIdx = getAnnotationIndex($scope.annotation.groups, item.cmTermID, item.cmID);
    			
    			_srcGroupAnnList.splice(_srcAnnIdx, 1);
    			_srcGroup.totalItems = _srcGroup.totalItems - 1;
    			try {
    				var _targetGroup = getGroup($scope.annotation.groups, targetTermID);
	    			var _targetGroupAnnList = _targetGroup.annotations;
	    			// remove the information from the local object
	    			item.cmTermName = termName;
	    			item.cmTermID = targetTermID;
	    			// don't change the ontology ID within a project!
	    			
	    			_targetGroupAnnList.push(item)
	    			_targetGroup.totalItems = _targetGroup.totalItems + 1;
    			} catch (e) {
					$log.debug("Term has been assigned, but group is not filtered.");
				}
        		        		
        		sharedService.addAlert("Term '" + termName + "' has been assigned.");
        	}, function(data, status, config, header){
        		sharedService.addAlert("Cannot assign term '" + termName + "'. Status " + status + ".", "danger");
        	});
        }
        return true;
	};
	
	// drag and drop feature for single annotations
    $scope.droppedObjects = [];
    $scope.onDropComplete = function(item,evt,targetTermID){
    	// assign the new term 
    	var success = $scope.assignNewTerm(item, targetTermID);
    	
    	if (success === true){
    		var index = $scope.droppedObjects.indexOf(item);
    		if (index == -1){
    			// add the item
    			$scope.droppedObjects.push(item);
    		}
    	}
    };
    $scope.onDragSuccess = function(data,evt){
        var index = $scope.droppedObjects.indexOf(data);
        if (index > -1) {
        	// remove the item
            $scope.droppedObjects.splice(index, 1);
        }
    };
    
    // load annotations from the server
    $scope.fetchAnnotations = function(termIDs, imageIDs, offset, loadPage){
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
    	
    	if (loadPage === true){
    		$scope['loading-'+termIDs[0]] = true;
    	}else {
    		// set the loading spinner
    		$scope.loading = true;
    	}
    	
    	// perform the query
    	annotationService.fetchUserAnnotationsByTerm(sessionService.getCurrentProject().cmID, 
    			imageIDs, termIDs, function(data){
    		
    		//$log.debug(data);
    		
    		// resolve the map data to the groups
    		for (var termID in data) {
    		    if (data.hasOwnProperty(termID)) {
//    		    	$log.debug("Resolving group for term '" + $rootScope.termList[termID] + "'")
		    		var group = data[termID];
		    		// resolve the term name
    		    	group.termName = $rootScope.termList[termID];
    		    	
    		    	var selectedAnns = Object.keys($scope.selectedAnnotations);
    		    	
    		    	// check if the group contains any selected annotation
    		    	// and set the corresponding check box selected again
    		    	for (var i = 0; i < group.pageItems; i++){
    		    		var annotation = group.annotations[i];
    		    		if (selectedAnns.indexOf(annotation.cmID + "") != -1){
    		    			annotation.checked = true;
    		    		} else {
    		    			annotation.checked = false;
    		    		}
    		    	}
		    		
		    		// search for the object in the array and remove it
		    		for (var idx = 0; idx < $scope.annotation.groups.length; idx++){
		    			var existingObj = $scope.annotation.groups[idx];
		    			if (existingObj.termID == termID){
		    				$scope.annotation.groups.splice(idx,1);
		    				break;
		    			}
		    		}
		    		// then add the new objects
		    		$scope.annotation.groups.push(group);
    		    }
    		}
    		
    		// sort the groups according to the newly added elements
    		$scope.annotation.groups.sort(sortGroups);

    		// finally delete the error message
    		delete $scope.error;
    		
    		// disable the loading spinner
    		if ($scope['loading-'+termIDs[0]]){
    			delete $scope['loading-'+termIDs[0]];
    		} else {
    			delete $scope.loading;
    		}
    		
    		$log.debug($scope.selectedAnnotations);
    		
    		}, function(data,status,header,config){
    		$scope.error = {
    			message : data.error.message,
    			status : status
    		};
    		$log.error(status);
    		
    		// disable the loading spinner
    		if ($scope['loading-'+termIDs[0]]){
    			delete $scope['loading-'+termIDs[0]];
    		} else {
    			delete $scope.loading;
    		}
    	}, 
    	offset, // variable offset
    	$scope.pagination.global.itemsPerPage); // max stays constant
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
    		$scope.fetchAnnotations([object.id], selectedImages, 0);
    	} else if (action === 'remove'){
    		// remove the selected term
    		selectedTerms.splice(selectedTerms.indexOf(object.id), 1);
    		
    		// check if any annotations of this term were selected and remove them from the object
    		$scope.unSelectAllAnnotations(object.id);
    		
    		// remove the group from the 'groups' array
    		for (var idx = 0; idx < $scope.annotation.groups.length; idx++){
    			if ($scope.annotation.groups[idx].termID == object.id){
    				$scope.annotation.groups.splice(idx,1);
    				break;
    			}
    		}
    		// INFO: no need to reorder array, since we just remove from a sorted
    		
    	} else if (action === 'addAll'){
    		// here the id is the entire array
    		selectedTerms = object.id; 
    		// clear the groups
    		$scope.annotation.groups = [];
    		$scope.fetchAnnotations(selectedTerms, selectedImages, 0);
    	} else if (action === 'removeAll'){
    		// reset all stuff
    		selectedTerms = [];
    		$scope.annotation.groups = [];
    		$scope.selectedAnnotations = {};
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
    		$scope.fetchAnnotations(selectedTerms, selectedImages, 0);
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
    		$scope.selectedAnnotations[ann.cmID] = ann;
    	}
    	
    	$log.debug($scope.selectedAnnotations);
    };    
    
    // opens the labeling interface and jumps to the current annotation
    $scope.labelAnnotation = function(ann){
    	$log.debug("Computing URL for annotation " +  ann.cmID);
    	
    	sessionService.touchImage(ann.cmProjectID, ann.cmImageID, function(data){
    		sessionService.setCurrentAnnotationID(ann.cmID);
			$log.debug("Successfully touched image " + ann.cmImageID);
			delete $scope.error;
			navService.navToLabelingPage(ann.cmProjectID, ann.cmImageID);
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
    		var chbx = document.getElementById("checkbox:" + annIDs[idx].cmID);
    		// overwrite the selection state
    		$scope.selectedAnnotations[annIDs[idx].cmID] = termID;
    		chbx.checked = true;
    	}
    	
    	$log.debug($scope.selectedAnnotations);
    }; 
    
    $scope.unSelectAllAnnotations = function(termID){
    	$log.debug("UNChecking all annotations of group " +  $rootScope.termList[termID]);
    	var annIDs = getGroup($scope.annotation.groups, termID).annotations;
    	for (var idx = 0; idx < annIDs.length; idx++){
    		var chbx = document.getElementById("checkbox:" + annIDs[idx].cmID);
        	delete $scope.selectedAnnotations[annIDs[idx].cmID];
    		chbx.checked = false;
    	}
    	$log.debug($scope.selectedAnnotations);
    }; 
    
    // reload all annotations
    $scope.refreshPage = function() {
    	$log.debug("Refreshing page...");
    	$scope.clearAllSelectedItems();
    	$scope.fetchAnnotations(selectedTerms, selectedImages, 0);
    };
    
    // assign a selected term to all selected annotations
    $scope.assignTermToSelectedItems = function(){
    	var targetTermID = getKey($rootScope.termList, $scope.chosenTerm);
    	var nSelections = getSize($scope.selectedAnnotations);
    	if (targetTermID === undefined || nSelections === 0) {
    		$log.debug("No term chosen or no item selected, returning.");
    		return;
    	}
    	
    	$log.debug("Assigning term " + $scope.chosenTerm + "(" + targetTermID + ") to " + 
    			nSelections + " annotation" + ((nSelections===1)?".":"s."));
    	
    	// for each selected annotation, do the server call
    	var keys = Object.keys($scope.selectedAnnotations);
    	for (var idx = 0; idx < nSelections; idx++){
    		// assign a term to the annotation
    		var id = keys[idx];
    		var item = $scope.selectedAnnotations[id];
    		var oldTermID = item.cmTermID;
    		
    		// if the term ID does not change, skip the element(s)
            if (oldTermID == targetTermID){
            	$log.debug("No change in assignment, skipping element.");
            	continue;
            }
            
            // handle the -99 term (remove term)
            if (targetTermID == -99){
            	var termName = $rootScope.termList[item.cmTermID];
            	
            	$log.debug("Removing term " + termName +
            			" from annotation " + item.cmID);
            	
            	// perform server call
            	annotationService.deleteAnnotationTerm(item.cmProjectID, item.cmImageID, item.cmID, targetTermID,
        				function(data, item){
            		
            		// if call was successful, remove the term from the source group and add it to the target group
            		var _srcGroup = getGroup($scope.annotation.groups, item.cmTermID);
        			var _srcGroupAnnList = _srcGroup.annotations;
        			var _srcAnnIdx = getAnnotationIndex($scope.annotation.groups, item.cmTermID, item.cmID);
        			
        			$log.debug("length src group annlist: " + _srcGroupAnnList.length + ", try to remove index --> " + _srcAnnIdx);
        			_srcGroupAnnList.splice(_srcAnnIdx, 1);
        			_srcGroup.totalItems = _srcGroup.totalItems - 1;
        			try {
        				var _targetGroup = getGroup($scope.annotation.groups, targetTermID);
        				var _targetGroupAnnList = _targetGroup.annotations;
        				// remove the information from the local object
        				item.cmTermName = null;
        				item.cmTermID = -99;
        				item.cmOntologyID = 0;
        				
        				_targetGroupAnnList.push(item);
        				_targetGroup.totalItems = _targetGroup.totalItems + 1;
        			} catch (e) {
        				$log.debug("Term has been removed, but group is not filtered.");
					}
        			
        			sharedService.addAlert("Term '" + termName + "' has been removed.");
        			
        		}, function(data, status, config, header){
        			// don't do anything to the local stuff if an error occurs
        			sharedService.addAlert("Cannot remove term '" + termName + "'. Status " + status + ".", "danger");
        		}, item);
            } else {
            	var termName = $rootScope.termList[targetTermID];
            	
            	$log.debug("Assigning term " + termName +
            			" to annotation " + item.cmID);
            	
            	// perform the server call
            	annotationService.setAnnotationTerm(item.cmProjectID, item.cmImageID, item.cmID, targetTermID, 
            			function(data, item){
            		
            		// if call was successful, remove the term from the source group and add it to the target group
            		var _srcGroup = getGroup($scope.annotation.groups, item.cmTermID);
        			var _srcGroupAnnList = _srcGroup.annotations;
        			var _srcAnnIdx = getAnnotationIndex($scope.annotation.groups, item.cmTermID, item.cmID);
        			
        			$log.debug("length src group annlist: " + _srcGroupAnnList.length + ", try to remove index --> " + _srcAnnIdx);
        			_srcGroupAnnList.splice(_srcAnnIdx, 1);
        			_srcGroup.totalItems = _srcGroup.totalItems - 1;
        			try {
        				var _targetGroup = getGroup($scope.annotation.groups, targetTermID);
        				var _targetGroupAnnList = _targetGroup.annotations;
        				// change the information in the local object
        				item.cmTermName = termName;
        				item.cmTermID = targetTermID;
        				// don't change the ontology ID within a project!
        				
        				_targetGroupAnnList.push(item);
        				_targetGroup.totalItems = _targetGroup.totalItems + 1;
					} catch (e) {
						$log.debug("Term has been assigned, but group is not filtered.");
					}
            		
            		sharedService.addAlert("Term '" + termName + "' has been assigned.");
            	}, function(data, status, config, header){
            		sharedService.addAlert("Cannot assign term '" + termName + "'. Status " + status + ".", "danger");
            	}, item);
            }
   		}
    	
    	// clear selected annotations and check boxes
    	$scope.clearAllSelectedItems();
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
    		$scope.selectedAnnotations[id].checked = false;
    	}
    	
    	// clear selections
    	$scope.selectedAnnotations = {};
    };
    
    // #########################################
    // pagination settings
    // #########################################
	$scope.pagination = {
		global : {
			itemsPerPage : 30,
		}
	};
	
	$scope.pageChanged = function(event, termID, index) {
		var pageToFetch = getGroup($scope.annotation.groups, termID).currentPage;
		$log.debug('Page of term ' +  termID + ' changed to: ' + pageToFetch);
		
		// compute the offset from page
		var max = $scope.pagination.global.itemsPerPage
		var offset = (pageToFetch-1)*max;
		
		// perform call for next/previous annotation page for that term
		$scope.fetchAnnotations([termID], selectedImages, offset, true);
	};
	
	$scope.addKeys = function(url){
		return cytomineService.addKeys(url);
	}
}]);

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

function getAnnotationIndex(groups, termID, annID) {
	// search for the object in the array
	for (var idx = 0; idx < groups.length; idx++){
		var existingObj = groups[idx];
		if (existingObj.termID == termID){
			var annList = existingObj.annotations;
			for (var i = 0; i < annList.length; i++){
				var ann = annList[i];
				if (ann.cmID == annID){
					return i;
				}
			}
		}
	}
	return -1;
};

function getAnnotation(groups, termID, annID) {
	// search for the object in the array
	for (var idx = 0; idx < groups.length; idx++){
		var existingObj = groups[idx];
		if (existingObj.termID == termID){
			var annList = existingObj.annotations;
			for (var i = 0; i < annList.length; i++){
				var ann = annList[i];
				if (ann.cmID == annID){
					return ann;
				}
			}
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
