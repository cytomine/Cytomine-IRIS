var iris = angular.module("irisApp");

iris.config(function($logProvider) {
	$logProvider.debugEnabled(true);
});

iris.controller("annotationGalleryCtrl", function($scope, $http, $filter,
		$location, hotkeys, helpService, annotationService, sessionService, sharedService, $routeParams) {
	console.log("annotationGalleryCtrl");

	// set content url for the help page
	helpService.setContentUrl("content/help/annGalleryHelp.html");
	
	$scope.annotation = {
		groups : [{termName: "Group 01"},{termName: "Group 02"}], // this variable 
		error : {}
	};
	
	$scope.projectName = sessionService.getCurrentProject().cmName;
	
	$scope.annotations=[ 
	  {"class":"be.cytomine.apps.iris.Annotation","id":null,"cmID":140736277,"cmProjectID":93519082,"cmImageID":94255021,"cmCreatorUserID":16,"cmImageURL":"http://beta.cytomine.be/#tabs-image-93519082-94255021-140736277","cmCropURL":"http://beta.cytomine.be/api/userannotation/140736277/crop.jpg","drawIncreasedAreaURL":"http://beta.cytomine.be/api/annotation/140736277/crop.png?increaseArea=8&maxSize=256&draw=true","cmSmallCropURL":"http://beta.cytomine.be/api/userannotation/140736277/crop.png?maxSize=256","cmTermID":0,"cmTermName":null,"cmOntology":0,"cmUserID":0,"prefs":{},"image":null,"location":null,"x":21712.0,"y":26280.5},{"class":"be.cytomine.apps.iris.Annotation","id":null,"cmID":140736270,"cmProjectID":93519082,"cmImageID":94255021,"cmCreatorUserID":16,"cmImageURL":"http://beta.cytomine.be/#tabs-image-93519082-94255021-140736270","cmCropURL":"http://beta.cytomine.be/api/userannotation/140736270/crop.jpg","drawIncreasedAreaURL":"http://beta.cytomine.be/api/annotation/140736270/crop.png?increaseArea=8&maxSize=256&draw=true","cmSmallCropURL":"http://beta.cytomine.be/api/userannotation/140736270/crop.png?maxSize=256","cmTermID":0,"cmTermName":null,"cmOntology":0,"cmUserID":0,"prefs":{},"image":null,"location":null,"x":39680.0,"y":33000.5},{"class":"be.cytomine.apps.iris.Annotation","id":null,"cmID":140736262,"cmProjectID":93519082,"cmImageID":94255021,"cmCreatorUserID":16,"cmImageURL":"http://beta.cytomine.be/#tabs-image-93519082-94255021-140736262","cmCropURL":"http://beta.cytomine.be/api/userannotation/140736262/crop.jpg","drawIncreasedAreaURL":"http://beta.cytomine.be/api/annotation/140736262/crop.png?increaseArea=8&maxSize=256&draw=true","cmSmallCropURL":"http://beta.cytomine.be/api/userannotation/140736262/crop.png?maxSize=256","cmTermID":0,"cmTermName":null,"cmOntology":0,"cmUserID":0,"prefs":{},"image":null,"location":null,"x":22608.0,"y":26984.5},{"class":"be.cytomine.apps.iris.Annotation","id":null,"cmID":140688172,"cmProjectID":93519082,"cmImageID":100117637,"cmCreatorUserID":107758862,"cmImageURL":"http://beta.cytomine.be/#tabs-image-93519082-100117637-140688172","cmCropURL":"http://beta.cytomine.be/api/userannotation/140688172/crop.jpg","drawIncreasedAreaURL":"http://beta.cytomine.be/api/annotation/140688172/crop.png?increaseArea=8&maxSize=256&draw=true","cmSmallCropURL":"http://beta.cytomine.be/api/userannotation/140688172/crop.png?maxSize=256","cmTermID":0,"cmTermName":null,"cmOntology":0,"cmUserID":0,"prefs":{},"image":null,"location":null,"x":22212.5,"y":11048.000000000002},{"class":"be.cytomine.apps.iris.Annotation","id":null,"cmID":140688165,"cmProjectID":93519082,"cmImageID":100117637,"cmCreatorUserID":107758862,"cmImageURL":"http://beta.cytomine.be/#tabs-image-93519082-100117637-140688165","cmCropURL":"http://beta.cytomine.be/api/userannotation/140688165/crop.jpg","drawIncreasedAreaURL":"http://beta.cytomine.be/api/annotation/140688165/crop.png?increaseArea=8&maxSize=256&draw=true","cmSmallCropURL":"http://beta.cytomine.be/api/userannotation/140688165/crop.png?maxSize=256","cmTermID":0,"cmTermName":null,"cmOntology":0,"cmUserID":0,"prefs":{},"image":null,"location":null,"x":22184.499999999996,"y":11105.999999999996},{"class":"be.cytomine.apps.iris.Annotation","id":null,"cmID":140688158,"cmProjectID":93519082,"cmImageID":100117637,"cmCreatorUserID":107758862,"cmImageURL":"http://beta.cytomine.be/#tabs-image-93519082-100117637-140688158","cmCropURL":"http://beta.cytomine.be/api/userannotation/140688158/crop.jpg","drawIncreasedAreaURL":"http://beta.cytomine.be/api/annotation/140688158/crop.png?increaseArea=8&maxSize=256&draw=true","cmSmallCropURL":"http://beta.cytomine.be/api/userannotation/140688158/crop.png?maxSize=256","cmTermID":0,"cmTermName":null,"cmOntology":0,"cmUserID":0,"prefs":{},"image":null,"location":null,"x":22139.482211350005,"y":11155.816230617},{"class":"be.cytomine.apps.iris.Annotation","id":null,"cmID":140688137,"cmProjectID":93519082,"cmImageID":100117637,"cmCreatorUserID":107758862,"cmImageURL":"http://beta.cytomine.be/#tabs-image-93519082-100117637-140688137","cmCropURL":"http://beta.cytomine.be/api/userannotation/140688137/crop.jpg","drawIncreasedAreaURL":"http://beta.cytomine.be/api/annotation/140688137/crop.png?increaseArea=8&maxSize=256&draw=true","cmSmallCropURL":"http://beta.cytomine.be/api/userannotation/140688137/crop.png?maxSize=256","cmTermID":0,"cmTermName":null,"cmOntology":0,"cmUserID":0,"prefs":{},"image":null,"location":null,"x":22120.575855042494,"y":11182.838905629778}
	  ];
	
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
    $scope.onDropComplete=function(data,evt){
        var index = $scope.droppedObjects.indexOf(data);
        if (index == -1){
        	$scope.droppedObjects.push(data);
        }
    };
    $scope.onDragSuccess=function(data,evt){
        var index = $scope.droppedObjects.indexOf(data);
        if (index > -1) {
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
    
    //$scope.fetchAllAnnotations();
	
});
