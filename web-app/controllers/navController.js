var iris = angular.module("irisApp");

iris.controller("navCtrl", function($scope, $location, $log, sharedService) {
	console.log("navCtrl");
	
	// navigation active tab controller
	$scope.isActive = function(viewLocation) {
		// console.log($location.path())

		// full match
		if ($location.path() === viewLocation) {
			return true;
		}
		// partial (suffix) match
		else if (sharedService.strEndsWith($location.path(), viewLocation)) {
			return true;
		}
		// no match
		else {
			return false;
		}
	};
	
	
});
