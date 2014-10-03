var iris = angular.module("irisApp");

iris.controller("imageTreeCtrl", function($scope, $timeout, $log, sessionService, imageService, projectService, sharedService){
	
	console.log("imageTreeCtrl");
	
	var demoTree = [{"class":"be.cytomine.image.AbstractImage","id":94255014,"created":"1389786341459","updated":"1389786344467","deleted":null,"filename":"93518990/1389785459805/HE_32911_12_converted.tif","originalFilename":"HE_32911_12.svs","scanner":null,"sample":94255013,"path":"93518990/1389785459805/HE_32911_12_converted.tif","mime":"tiff","width":56640,"height":39163,"depth":8,"resolution":0.65,"magnification":40,"thumb":"http://beta.cytomine.be/api/abstractimage/94255014/thumb.png?maxSize=512","preview":"http://beta.cytomine.be/api/abstractimage/94255014/thumb.png?maxSize=1024","fullPath":"/data/beta.cytomine.be/93518990/93518990/1389785459805/HE_32911_12_converted.tif","macroURL":"http://beta.cytomine.be/api/abstractimage/94255014/associated/macro.png?maxWidth=512"},{"class":"be.cytomine.image.AbstractImage","id":98878438,"created":"1391527195500","updated":"1391527199160","deleted":null,"filename":"93518990/1391527097255/HE_397_2013.svs","originalFilename":"HE_397_2013.svs","scanner":null,"sample":98878437,"path":"93518990/1391527097255/HE_397_2013.svs","mime":"svs","width":39983,"height":30114,"depth":8,"resolution":0.24539999663829803,"magnification":40,"thumb":"http://beta.cytomine.be/api/abstractimage/98878438/thumb.png?maxSize=512","preview":"http://beta.cytomine.be/api/abstractimage/98878438/thumb.png?maxSize=1024","fullPath":"/data/beta.cytomine.be/93518990/93518990/1391527097255/HE_397_2013.svs","macroURL":"http://beta.cytomine.be/api/abstractimage/98878438/associated/macro.png?maxWidth=512"}];

	var checkedImages = [];
	
	$scope.tree = {
		loading : true,
	};
	
	var initTree = function(){
		$scope.expandAll();
		$scope.showTree = true;
	}
	
	// get the images and initialize the tree
	imageService.fetchImages(sessionService.getCurrentProject().cmID, function(images){
		$scope.treeData = demoTree;
		//$scope.treeData = images;
		$scope.tree.loading = false;
		
		// initialize the tree (show and expand all)
		initTree();
	}, function(data, status){
		sharedService.addAlert("Cannot retrieve images. Status " + status + ".", "danger");
	})
	
	
	$scope.selectedNode = {};
	$scope.expandedNodes = [];
	
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
    
    var searchForExpandableNode = function(root, nodeArray){
    	for (var i = 0; i< root.children.length; i++){
    		var child = root.children[i];
//    		console.log(child.name);
	    	if (child.isFolder){
//	    		console.log("search in folder: " + child.name)
	    		nodeArray.push(child);
	    		// recurse
	    		searchForExpandableNode(child, nodeArray);
	    	} 
	    }
    	return nodeArray;
    };
    
    var searchForSelectableNode = function(root, nodeArray){
    	for (var i = 0; i< root.children.length; i++){
    		var child = root.children[i];
//    		console.log(child.name);
	    	if (child.isFolder){
//	    		console.log("found selectable child:  " + child.name)
	    		// recurse
	    		searchForSelectableNode(child, nodeArray);
	    	} else {
	    		nodeArray.push(child.id);
	    	}
	    }
    	return nodeArray;
    };
    
    $scope.showToggle = function(node, expanded) {
        //$log.debug(node.id+ (expanded?" expanded":" collapsed"));
        
        if (expanded){
        	// reselect previously selected children
        	$timeout(function(){ selectCheckboxes(checkedImages, true);}, 50);  
        }
        // find selected children of the node id
        //$log.debug("Active Images: {" + checkedImages.toString() + "}.");
    };
    
    // expand all nodes by default and reselect the nodes
	$scope.expandAll = function() {
	    $scope.expandedNodes = [];
	    searchForExpandableNode($scope.treeData, $scope.expandedNodes);
	    $timeout(function(){ selectCheckboxes(checkedImages, true);}, 50);  
	};
	
	// collapse all nodes, but do not deselect the check boxes
	$scope.collapseAll = function(){
		 $scope.expandedNodes = [];
	}
	
    $scope.checkAllImages = function(){
    	checkedImages = [];
    	searchForSelectableNode($scope.treeData, checkedImages);
    	selectCheckboxes(checkedImages, true);    
    	//$log.debug("Active Images: {" + checkedImages.toString() + "}.");
    	//$log.debug("checked all images: " + checkedImages.length);
    };
    
    $scope.uncheckAllImages = function(){
    	checkedImages = [];
    	searchForSelectableNode($scope.treeData, checkedImages);
    	selectCheckboxes(checkedImages, false);
    	checkedImages = [];
    	//$log.debug("Active Images: {" + checkedImages.toString() + "}.");
    	//$log.debug("UNchecked all images.")
    	
    	// TODO hide all panels
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
    var applyFilter = function(checkedImages, checkedImages){
    	// TODO continue implementation
    }
	
});
