var iris = angular.module("irisApp");

iris.config(function($logProvider) {
	$logProvider.debugEnabled(true);
});

iris.controller("annotationGalleryCtrl", function($rootScope, $scope, $http, $filter, $log,
		$location, hotkeys, helpService, annotationService, sessionService, sharedService, $routeParams) {
	console.log("annotationGalleryCtrl");

	// set content url for the help page
	helpService.setContentUrl("content/help/annGalleryHelp.html");
	
	$scope.annotation = {
		groups : [] // this variable holds all terms
	};

	var selectedTerms = [];
	var selectedImages = [];
	// create empty set
	$scope.selectedAnnotations = {};

	$scope.projectName = sessionService.getCurrentProject().cmName;
	
//	$scope.annotations=[ 
//	  {"class":"be.cytomine.apps.iris.Annotation","id":null,"cmID":140736277,"cmProjectID":93519082,"cmImageID":94255021,"cmCreatorUserID":16,"cmImageURL":"http://beta.cytomine.be/#tabs-image-93519082-94255021-140736277","cmCropURL":"http://beta.cytomine.be/api/userannotation/140736277/crop.jpg","drawIncreasedAreaURL":"http://beta.cytomine.be/api/annotation/140736277/crop.png?increaseArea=8&maxSize=256&draw=true","cmSmallCropURL":"http://beta.cytomine.be/api/userannotation/140736277/crop.png?maxSize=256","cmTermID":0,"cmTermName":null,"cmOntology":0,"cmUserID":0,"prefs":{},"image":null,"location":null,"x":21712.0,"y":26280.5},{"class":"be.cytomine.apps.iris.Annotation","id":null,"cmID":140736270,"cmProjectID":93519082,"cmImageID":94255021,"cmCreatorUserID":16,"cmImageURL":"http://beta.cytomine.be/#tabs-image-93519082-94255021-140736270","cmCropURL":"http://beta.cytomine.be/api/userannotation/140736270/crop.jpg","drawIncreasedAreaURL":"http://beta.cytomine.be/api/annotation/140736270/crop.png?increaseArea=8&maxSize=256&draw=true","cmSmallCropURL":"http://beta.cytomine.be/api/userannotation/140736270/crop.png?maxSize=256","cmTermID":0,"cmTermName":null,"cmOntology":0,"cmUserID":0,"prefs":{},"image":null,"location":null,"x":39680.0,"y":33000.5},{"class":"be.cytomine.apps.iris.Annotation","id":null,"cmID":140736262,"cmProjectID":93519082,"cmImageID":94255021,"cmCreatorUserID":16,"cmImageURL":"http://beta.cytomine.be/#tabs-image-93519082-94255021-140736262","cmCropURL":"http://beta.cytomine.be/api/userannotation/140736262/crop.jpg","drawIncreasedAreaURL":"http://beta.cytomine.be/api/annotation/140736262/crop.png?increaseArea=8&maxSize=256&draw=true","cmSmallCropURL":"http://beta.cytomine.be/api/userannotation/140736262/crop.png?maxSize=256","cmTermID":0,"cmTermName":null,"cmOntology":0,"cmUserID":0,"prefs":{},"image":null,"location":null,"x":22608.0,"y":26984.5},{"class":"be.cytomine.apps.iris.Annotation","id":null,"cmID":140688172,"cmProjectID":93519082,"cmImageID":100117637,"cmCreatorUserID":107758862,"cmImageURL":"http://beta.cytomine.be/#tabs-image-93519082-100117637-140688172","cmCropURL":"http://beta.cytomine.be/api/userannotation/140688172/crop.jpg","drawIncreasedAreaURL":"http://beta.cytomine.be/api/annotation/140688172/crop.png?increaseArea=8&maxSize=256&draw=true","cmSmallCropURL":"http://beta.cytomine.be/api/userannotation/140688172/crop.png?maxSize=256","cmTermID":0,"cmTermName":null,"cmOntology":0,"cmUserID":0,"prefs":{},"image":null,"location":null,"x":22212.5,"y":11048.000000000002},{"class":"be.cytomine.apps.iris.Annotation","id":null,"cmID":140688165,"cmProjectID":93519082,"cmImageID":100117637,"cmCreatorUserID":107758862,"cmImageURL":"http://beta.cytomine.be/#tabs-image-93519082-100117637-140688165","cmCropURL":"http://beta.cytomine.be/api/userannotation/140688165/crop.jpg","drawIncreasedAreaURL":"http://beta.cytomine.be/api/annotation/140688165/crop.png?increaseArea=8&maxSize=256&draw=true","cmSmallCropURL":"http://beta.cytomine.be/api/userannotation/140688165/crop.png?maxSize=256","cmTermID":0,"cmTermName":null,"cmOntology":0,"cmUserID":0,"prefs":{},"image":null,"location":null,"x":22184.499999999996,"y":11105.999999999996},{"class":"be.cytomine.apps.iris.Annotation","id":null,"cmID":140688158,"cmProjectID":93519082,"cmImageID":100117637,"cmCreatorUserID":107758862,"cmImageURL":"http://beta.cytomine.be/#tabs-image-93519082-100117637-140688158","cmCropURL":"http://beta.cytomine.be/api/userannotation/140688158/crop.jpg","drawIncreasedAreaURL":"http://beta.cytomine.be/api/annotation/140688158/crop.png?increaseArea=8&maxSize=256&draw=true","cmSmallCropURL":"http://beta.cytomine.be/api/userannotation/140688158/crop.png?maxSize=256","cmTermID":0,"cmTermName":null,"cmOntology":0,"cmUserID":0,"prefs":{},"image":null,"location":null,"x":22139.482211350005,"y":11155.816230617},{"class":"be.cytomine.apps.iris.Annotation","id":null,"cmID":140688137,"cmProjectID":93519082,"cmImageID":100117637,"cmCreatorUserID":107758862,"cmImageURL":"http://beta.cytomine.be/#tabs-image-93519082-100117637-140688137","cmCropURL":"http://beta.cytomine.be/api/userannotation/140688137/crop.jpg","drawIncreasedAreaURL":"http://beta.cytomine.be/api/annotation/140688137/crop.png?increaseArea=8&maxSize=256&draw=true","cmSmallCropURL":"http://beta.cytomine.be/api/userannotation/140688137/crop.png?maxSize=256","cmTermID":0,"cmTermName":null,"cmOntology":0,"cmUserID":0,"prefs":{},"image":null,"location":null,"x":22120.575855042494,"y":11182.838905629778}
//	  ];
	
	// put all valid shortcuts for this page here
	hotkeys.bindTo($scope)
	.add({
		combo : 'h',
		description : 'Show help for this page',
		callback : function() {
			helpService.showHelp();
		}
	});
	
	$scope.projectID = $routeParams["projectID"];

	// TODO implement drag and drop feature
    $scope.droppedObjects = [];
    $scope.onDropComplete=function(data,evt,termID){
    	var index = $scope.droppedObjects.indexOf(data);
        if (index == -1){
        	// add the item
        	$scope.droppedObjects.push(data);
        }

        // TODO handle the 0 term (remove term)
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
    var inArray = function(array, obj) {
        var index = array.indexOf(obj);
    };
    
    $scope.fetchAllAnnotations = function(){
    	annotationService.fetchUserAnnotations(sessionService.getCurrentProject().cmID, null, 
    		function(data){
    			$scope.annotations = data;
		    }, function(data,status){
		    	sharedService.addAlert("Retrieving annotations failed. Status " + status + ".", "danger");
		    });
    };
    	
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
    	
    	// perform the query
    	annotationService.fetchUserAnnotationsByTerm(sessionService.getCurrentProject().cmID, 
    			imageIDs, termIDs, function(data){
    		
    		$log.debug(data);
    		// TODO resolve the map data to the groups
    		
    		for (var termID in data) {
    		    if (data.hasOwnProperty(termID)) {
//    		    	$log.debug("Resolving group for term '" + $rootScope.termList[termID] + "'")
		    		var obj = {termName: $rootScope.termList[termID], termID: termID, annotations : data[termID]};
		    		$scope.annotation.groups.push(obj);
    		    }
    		}
    		
    		// sort the groups according to the newly added elements
    		$scope.annotation.groups.sort(sortGroups);
  
    		// finally delete the error message
    		delete $scope.error
    	}, function(data,status,header,config){
    		$scope.error = {
    			message : data.error.message,
    			status : status
    		};
    		$log.error(status);
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
});

function sortGroups(a, b) {
	if (a.termName < b.termName)
		return -1;
	if (a.termName > b.termName)
		return 1;
	return 0;
};