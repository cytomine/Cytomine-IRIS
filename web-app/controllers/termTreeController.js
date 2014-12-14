var iris = angular.module("irisApp");

iris.controller("termTreeCtrl", function($rootScope, $scope, $timeout, $log, sessionService, projectService, sharedService){
	
	console.log("termTreeCtrl");
	
	var demoTree = {"hideCheckbox":true,"state":"open","data":"BM-01","children":[{"hideCheckbox":false,"data":"Z99_Background","class":"be.cytomine.ontology.Term","parent":null,"children":[],"attr":{"id":95795881,"type":"be.cytomine.ontology.Term"},"id":95795881,"title":"Z99_Background","isFolder":false,"color":"#677dff","name":"Z99_Background","checked":false,"key":95795881},{"hideCheckbox":true,"data":"Erythrocytopoesis","class":"be.cytomine.ontology.Term","parent":null,"children":[{"hideCheckbox":false,"data":"E01_Pronormoblast","class":"be.cytomine.ontology.Term","parent":136483400,"children":[],"attr":{"id":95202366,"type":"be.cytomine.ontology.Term"},"id":95202366,"title":"E01_Pronormoblast","isFolder":false,"color":"#2bfdcf","name":"E01_Pronormoblast","checked":false,"key":95202366},{"hideCheckbox":false,"data":"E02_Basophilic Normoblast","class":"be.cytomine.ontology.Term","parent":136483400,"children":[],"attr":{"id":95202360,"type":"be.cytomine.ontology.Term"},"id":95202360,"title":"E02_Basophilic Normoblast","isFolder":false,"color":"#2bfdcf","name":"E02_Basophilic Normoblast","checked":false,"key":95202360},{"hideCheckbox":false,"data":"E03_Polychromatophilic Normoblast","class":"be.cytomine.ontology.Term","parent":136483400,"children":[],"attr":{"id":95202354,"type":"be.cytomine.ontology.Term"},"id":95202354,"title":"E03_Polychromatophilic Normoblast","isFolder":false,"color":"#2bfdcf","name":"E03_Polychromatophilic Normoblast","checked":false,"key":95202354},{"hideCheckbox":false,"data":"E04_Orthochromatic Normoblast","class":"be.cytomine.ontology.Term","parent":136483400,"children":[],"attr":{"id":95202348,"type":"be.cytomine.ontology.Term"},"id":95202348,"title":"E04_Orthochromatic Normoblast","isFolder":false,"color":"#2bfdcf","name":"E04_Orthochromatic Normoblast","checked":false,"key":95202348},{"hideCheckbox":false,"data":"E05_Reticulocyte","class":"be.cytomine.ontology.Term","parent":136483400,"children":[],"attr":{"id":95202334,"type":"be.cytomine.ontology.Term"},"id":95202334,"title":"E05_Reticulocyte","isFolder":false,"color":"#2bfdcf","name":"E05_Reticulocyte","checked":false,"key":95202334},{"hideCheckbox":false,"data":"E06_Erythrocyte","class":"be.cytomine.ontology.Term","parent":136483400,"children":[],"attr":{"id":95202328,"type":"be.cytomine.ontology.Term"},"id":95202328,"title":"E06_Erythrocyte","isFolder":false,"color":"#2bfdcf","name":"E06_Erythrocyte","checked":false,"key":95202328}],"attr":{"id":136483400,"type":"be.cytomine.ontology.Term"},"id":136483400,"title":"Erythrocytopoesis","isFolder":true,"color":"#2bfdcf","name":"Erythrocytopoesis","checked":false,"key":136483400},{"hideCheckbox":true,"data":"Granulocytopoesis","class":"be.cytomine.ontology.Term","parent":null,"children":[{"hideCheckbox":false,"data":"G01_Myeloblast","class":"be.cytomine.ontology.Term","parent":136483532,"children":[],"attr":{"id":95202322,"type":"be.cytomine.ontology.Term"},"id":95202322,"title":"G01_Myeloblast","isFolder":false,"color":"#2bfdcf","name":"G01_Myeloblast","checked":false,"key":95202322},{"hideCheckbox":false,"data":"G02_Promyelocyte","class":"be.cytomine.ontology.Term","parent":136483532,"children":[],"attr":{"id":95202311,"type":"be.cytomine.ontology.Term"},"id":95202311,"title":"G02_Promyelocyte","isFolder":false,"color":"#2bfdcf","name":"G02_Promyelocyte","checked":false,"key":95202311},{"hideCheckbox":false,"data":"G03_Myelocyte","class":"be.cytomine.ontology.Term","parent":136483532,"children":[],"attr":{"id":95202287,"type":"be.cytomine.ontology.Term"},"id":95202287,"title":"G03_Myelocyte","isFolder":false,"color":"#2bfdcf","name":"G03_Myelocyte","checked":false,"key":95202287},{"hideCheckbox":false,"data":"G04_Metamyelocyte","class":"be.cytomine.ontology.Term","parent":136483532,"children":[],"attr":{"id":95202281,"type":"be.cytomine.ontology.Term"},"id":95202281,"title":"G04_Metamyelocyte","isFolder":false,"color":"#2bfdcf","name":"G04_Metamyelocyte","checked":false,"key":95202281},{"hideCheckbox":false,"data":"G05_BandCell","class":"be.cytomine.ontology.Term","parent":136483532,"children":[],"attr":{"id":95202274,"type":"be.cytomine.ontology.Term"},"id":95202274,"title":"G05_BandCell","isFolder":false,"color":"#2bfdcf","name":"G05_BandCell","checked":false,"key":95202274},{"hideCheckbox":false,"data":"G06_Granulocyte","class":"be.cytomine.ontology.Term","parent":136483532,"children":[],"attr":{"id":95202266,"type":"be.cytomine.ontology.Term"},"id":95202266,"title":"G06_Granulocyte","isFolder":false,"color":"#2bfdcf","name":"G06_Granulocyte","checked":false,"key":95202266}],"attr":{"id":136483532,"type":"be.cytomine.ontology.Term"},"id":136483532,"title":"Granulocytopoesis","isFolder":true,"color":"#2bfdcf","name":"Granulocytopoesis","checked":false,"key":136483532},{"hideCheckbox":true,"data":"Megakariocytopoesis","class":"be.cytomine.ontology.Term","parent":null,"children":[{"hideCheckbox":false,"data":"M01_Megakaryocyte","class":"be.cytomine.ontology.Term","parent":136483515,"children":[],"attr":{"id":95202372,"type":"be.cytomine.ontology.Term"},"id":95202372,"title":"M01_Megakaryocyte","isFolder":false,"color":"#2bfdcf","name":"M01_Megakaryocyte","checked":false,"key":95202372}],"attr":{"id":136483515,"type":"be.cytomine.ontology.Term"},"id":136483515,"title":"Megakariocytopoesis","isFolder":true,"color":"#2bfdcf","name":"Megakariocytopoesis","checked":false,"key":136483515}],"class":"be.cytomine.ontology.Ontology","attr":{"id":95190197,"type":"be.cytomine.ontology.Ontology"},"deleted":null,"id":95190197,"projects":[{"ontologyName":"BM-01","hideAdminsLayers":false,"class":"be.cytomine.project.Project","numberOfSlides":0,"ontology":95190197,"deleted":null,"blindMode":true,"retrievalProjects":[],"id":93519082,"numberOfReviewedAnnotations":2,"numberOfJobAnnotations":0,"created":"1389717327194","updated":"1408524340543","disciplineName":"HISTOLOGY","name":"MEDUNIGRAZ-BONE-MARROW","isReadOnly":false,"numberOfAnnotations":130,"retrievalAllOntology":false,"hideUsersLayers":true,"discipline":26481,"isClosed":false,"numberOfImages":2,"retrievalDisable":true}],"title":"BM-01","isFolder":true,"created":"1390234826742","updated":null,"name":"BM-01","user":93518990,"key":95190197};

	var checkedTerms = [];
	
	$rootScope.termList = {"-99" : "no term assigned" };
	
	$scope.tree = {
		loading : true,
	};
	
	var initTree = function(){
		$scope.expandAll();
		$scope.showTree = true;
	}
	
	// get the ontology and initialize the tree
	projectService.fetchOntology(sessionService.getCurrentProject().cmOntology, null, function(ontology){
		//	$scope.treeData = demoTree;
		$scope.treeData = ontology;
		$scope.tree.loading = false;
		
		// initialize the tree (show and expand all)
		initTree();
	}, function(data, status){
		sharedService.addAlert("Cannot retrieve ontology. Status " + status + ".", "danger");
	})
	
	$scope.selectedNode = {};
	$scope.expandedNodes = [];
	
	$scope.treeOptions = {
		    nodeChildren: "children",
		    dirSelectable: false
		};
	
    $scope.selectOrUnselectTerm = function(evt) {
        var targ;
        if (!evt) {
            var evt = window.event;
        }
        if (evt.target) { // TODO BUG IN FIREFOX!!!
            targ=evt.target;
        } else if (evt.srcElement) {
            targ=evt.srcElement;
        }
        
        // get the ID of the clicked term
        var id = Number(targ.id.split(":")[1]);
        var chbxID = "chbxTerm:"+id;
               
        // if the term is checked, it is in the checked list
        var idx = checkedTerms.indexOf(id);
        var chbx = document.getElementById(chbxID);
        
        if (isNaN(id)){
        	$log.debug("Nothing has been selected.");
        }
        else if (idx === -1){
        	// add the term
        	checkedTerms.push(id);
        	// select the checkbox
        	chbx.checked = true;
        	
        	// notify other instances about the change
        	$rootScope.$broadcast("termFilterChange", { id : id, name : $scope.termList[id], action : 'add' });
        }else {
        	// remove the term from the array
        	checkedTerms.splice(idx,1);
        	// unselect the checkbox
        	chbx.checked = false;
        	
        	// notify other instances about the change
        	$rootScope.$broadcast("termFilterChange", { id : id, name : $scope.termList[id], action : 'remove' });
        }
        
        $log.debug("Active Terms: {" + checkedTerms.toString() + "}.");
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
	    	} else {
	    		// add the child to the local object of term ids and names
	    		$rootScope.termList[child.id] = child.name
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
        	$timeout(function(){ selectCheckboxes(checkedTerms, true);}, 50);  
        }
        // find selected children of the node id
        //$log.debug("Active Terms: {" + checkedTerms.toString() + "}.");
    };
    
    // expand all nodes by default and reselect the nodes
	$scope.expandAll = function() {
	    $scope.expandedNodes = [];
	    // the following statement also constructs the list of assignable terms
	    searchForExpandableNode($scope.treeData, $scope.expandedNodes);
	    $timeout(function(){ selectCheckboxes(checkedTerms, true);}, 50);  
	};
	
	// collapse all nodes, but do not deselect the check boxes
	$scope.collapseAll = function(){
		 $scope.expandedNodes = [];
	}
	
    $scope.checkAllTerms = function(){
    	checkedTerms = [];
    	searchForSelectableNode($scope.treeData, checkedTerms);
    	selectCheckboxes(checkedTerms, true);    
    	
    	$rootScope.$broadcast("termFilterChange", { id : checkedTerms, action : 'addAll' });
    	
    	$log.debug("Active Terms: {" + checkedTerms.toString() + "}.");
    	//$log.debug("checked all terms: " + checkedTerms.length);
    };

    $scope.uncheckAllTerms = function(){
    	checkedTerms = [];
    	searchForSelectableNode($scope.treeData, checkedTerms);
    	selectCheckboxes(checkedTerms, false);
    	checkedTerms = [];
    	
    	$rootScope.$broadcast("termFilterChange", { id : checkedTerms, action : 'removeAll' });

    	$log.debug("Active Terms: {" + checkedTerms.toString() + "}.");
    	//$log.debug("UNchecked all terms.")
    };
    
    // select the terms in the tree
    var selectCheckboxes = function(checkedTerms, select){
    	for (var i = 0; i < checkedTerms.length; i++){
    		var termID = checkedTerms[i];
    		var chbxID = "chbxTerm:"+termID;
    		var chbx = document.getElementById(chbxID);
    		try {
    			chbx.checked = select;
    		}catch(e){
    			$log.warn(e.message);
    		}
    	}
    };
    
    // if an element has been dropped on the list element
    $scope.onDropComplete=function(annotation,evt,termID){
    	// TODO handle the 0 term
    	
        $log.debug("TODO assigning unique term " + $rootScope.termList[termID] +
        		" to annotation " + annotation.cmID);
    };
	
});
