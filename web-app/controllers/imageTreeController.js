var iris = angular.module("irisApp");

iris.controller("imageTreeCtrl", function($scope, $timeout, $log, sessionService, imageService, projectService, sharedService){
	
	console.log("imageTreeCtrl");
	
	var demoTree = [{"goToURL":"http://beta.cytomine.be/#tabs-image-93519082-100117637-","userProgress":42,"cytomine":{"reviewUser":107758880,"resolution":0.24539999663829803,"reviewed":false,"id":100117637,"numberOfReviewedAnnotations":0,"numberOfJobAnnotations":0,"height":30114,"updated":"1408348746333","created":"1391852925210","path":null,"inReview":true,"originalFilename":"[BLIND]100117637","macroURL":"http://beta.cytomine.be/api/abstractimage/98878438/associated/macro.png?maxWidth=512","fullPath":"/data/beta.cytomine.be/93518990/93518990/1391527097255/HE_397_2013.svs","reviewStart":"1408348746298","baseImage":98878438,"sample":98878437,"reviewStop":null,"width":39983,"class":"be.cytomine.image.ImageInstance","originalMimeType":null,"deleted":null,"depth":8,"extension":null,"magnification":40,"project":93519082,"preview":"http://beta.cytomine.be/api/abstractimage/98878438/thumb.png?maxSize=1024","numberOfAnnotations":7,"filename":null,"mime":null,"user":93518990,"thumb":"http://beta.cytomine.be/api/abstractimage/98878438/thumb.png?maxSize=512"},"nextAnnotation":null,"labeledAnnotations":3,"class":"be.cytomine.apps.iris.Image","previousAnnotation":null,"id":null,"cmID":100117637,"project":null,"lastActivity":1412357502898,"prefs":{},"olTileServerURL":"http://localhost:8080/image/tile?zoomify=/data/beta.cytomine.be/93518990/93518990/1391527097255/HE_397_2013.svs/","currentAnnotation":null,"numberOfAnnotations":7,"originalFilename":"[BLIND]100117637"},{"goToURL":"http://beta.cytomine.be/#tabs-image-93519082-94255021-","userProgress":42,"cytomine":{"reviewUser":93518990,"resolution":0.65,"reviewed":false,"id":94255021,"numberOfReviewedAnnotations":2,"numberOfJobAnnotations":0,"height":39163,"updated":"1395240656230","created":"1389786341627","path":null,"inReview":true,"originalFilename":"[BLIND]94255021","macroURL":"http://beta.cytomine.be/api/abstractimage/94255014/associated/macro.png?maxWidth=512","fullPath":"/data/beta.cytomine.be/93518990/93518990/1389785459805/HE_32911_12_converted.tif","reviewStart":"1393863218998","baseImage":94255014,"sample":94255013,"reviewStop":null,"width":56640,"class":"be.cytomine.image.ImageInstance","originalMimeType":null,"deleted":null,"depth":8,"extension":null,"magnification":40,"project":93519082,"preview":"http://beta.cytomine.be/api/abstractimage/94255014/thumb.png?maxSize=1024","numberOfAnnotations":133,"filename":null,"mime":null,"user":93518990,"thumb":"http://beta.cytomine.be/api/abstractimage/94255014/thumb.png?maxSize=512"},"nextAnnotation":null,"labeledAnnotations":56,"class":"be.cytomine.apps.iris.Image","previousAnnotation":null,"id":null,"cmID":94255021,"project":null,"lastActivity":1412357503229,"prefs":{},"olTileServerURL":"http://localhost:8080/image/tile?zoomify=/data/beta.cytomine.be/93518990/93518990/1389785459805/HE_32911_12_converted.tif/","currentAnnotation":null,"numberOfAnnotations":133,"originalFilename":"[BLIND]94255021"}];

	var checkedImages = [];
	
	$scope.tree = {
		loading: true,
	};
	
	var initTree = function(){
		$scope.showTree = false;
		$scope.checkAllImages();
	}
	
	// get the images and initialize the tree
	imageService.fetchImages(sessionService.getCurrentProject().cmID, false, function(images){
		//$scope.treeData = demoTree;
		$scope.treeData = images;
		$scope.tree.loading = false;
		
		// initialize the tree (show and expand all)
		initTree();
	}, function(data, status){
		sharedService.addAlert("Cannot retrieve images. Status " + status + ".", "danger");
	})
	
	
	$scope.selectedNode = {};
	
	$scope.treeOptions = {
		    nodeChildren: "children",
		    dirSelectable: false
		};
	
    $scope.selectOrUnselectImage = function(evt) {
        var targ;
        if (!evt) {
            var evt = window.event;
        }
        if (evt.target) {
            targ=evt.target;
        } else if (evt.srcElement) {
            targ=evt.srcElement;
        }
        
        // get the ID of the clicked image
        var id = Number(targ.id.split("-")[1]);
        var chbxID = "chbxImage-"+id;
               
        // if the image is checked, it is in the checked list
        var idx = checkedImages.indexOf(id);
        var chbx = document.getElementById(chbxID);
        if (idx === -1){
        	// add the image
        	checkedImages.push(id);
        	// select the checkbox
        	chbx.checked = true;
        }else {
        	// remove the image from the array
        	checkedImages.splice(idx,1);
        	// unselect the checkbox
        	chbx.checked = false;
        }
        
        //$log.debug("Active Images: {" + checkedImages.toString() + "}.");
    };
    
    $scope.clearSelected = function() {
        $scope.selectedNode = undefined;
    };
    
    $scope.checkAllImages = function(){
    	checkedImages = [];
    	
    	for (var i=0; i<$scope.treeData.length; i++){
    		checkedImages.push($scope.treeData[i].cmID)
    	}
    	
    	selectCheckboxes(checkedImages, true);    
    	//$log.debug("Active Images: {" + checkedImages.toString() + "}.");
    	//$log.debug("checked all images: " + checkedImages.length);
    };
    
    $scope.uncheckAllImages = function(){
    	checkedImages = [];
    	for (var i=0; i<$scope.treeData.length; i++){
    		checkedImages.push($scope.treeData[i].cmID)
    	}
    	selectCheckboxes(checkedImages, false);
    	checkedImages = [];
    	//$log.debug("Active Images: {" + checkedImages.toString() + "}.");
    	//$log.debug("UNchecked all images.")
    };
    
    // select the images in the tree
    var selectCheckboxes = function(checkedImages, select){
    	for (var i = 0; i < checkedImages.length; i++){
    		var imageID = checkedImages[i];
    		var chbxID = "chbxImage-"+imageID;
    		var chbx = document.getElementById(chbxID);
    		try {
    			chbx.checked = select;
    		}catch(e){
    			$log.warn(e.message);
    		}
    	}
    };
    
    // applies a filter using the checked terms and the checked images
    var applyFilter = function(checkedTerms, checkedImages){
    	// TODO continue implementation
    }
});
