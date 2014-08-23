var iris = angular.module("irisApp");

// controller for the progress bar functionality
iris.controller("progressCtrl", function($scope){
	
	// determine the type of the progress bar (color)
	$scope.type = function(progress) {
		if (progress < 50){
			return "danger";
		}
		else if (progress >= 50 && progress < 75){
			return "warning";
		}
		else if (progress >= 75 && progress < 95){
			return "info";
		}
		else if (progress >= 95){
			return "success";
		}
	}
});