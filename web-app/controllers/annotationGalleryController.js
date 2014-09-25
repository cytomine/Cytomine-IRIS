var iris = angular.module("irisApp");

iris.config(function($logProvider) {
	$logProvider.debugEnabled(true);
});

iris.controller("annotationGalleryCtrl", function($scope, $http, $filter,
		$location, hotkeys, helpService, annotationService, sessionService, $routeParams) {
	console.log("annotationGalleryCtrl");

	// set content url for the help page
	helpService.setContentUrl("content/help/annGalleryHelp.html");
	
	$scope.annotation = {
		error : {}
	};
	
	$scope.projectName = sessionService.getCurrentProject().cmName;
	
	$scope.annotations=[ 
	  {name:'John'},
	  {name:'Jessie'},
	  {name:'Johanna'},
	  {name:'Joy'},
	  {name:'Mary'},
	  {name:'Peter'},
	  {name:'Sebastian'},
	  {name:'Erika'},
	  {name:'Patrick'},
	  {name:'John'},
	  {name:'Jessie'},
	  {name:'Johanna'},
	  {name:'Joy'},
	  {name:'Mary'},
	  {name:'Peter'},
	  {name:'Sebastian'},
	  {name:'Erika'},
	  {name:'Patrick'},
	  {name:'Samantha'} 
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
	
});
