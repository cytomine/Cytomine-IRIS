var iris = angular.module("irisApp");

iris.controller("alertCtrl", ["$rootScope","$scope","$timeout", function($rootScope, $scope, $timeout) {
	
	// limit the number of concurrent alerts to n
	var maxAlerts = 4;
	$scope.alerts = [];
	
	// possible types of alert are: info, success, warning, danger, default
	$scope.$on("addAlert", function(event, mass) {
		var newAlert = {
			type : mass.type?mass.type:'success',
			msg : mass.msg
		};

		var customDelay = mass.delay;

		var delay = customDelay?customDelay:2500;
		
		// remove the oldest entry from the array
		if ($scope.alerts.length >= maxAlerts){
			$scope.closeAlert(0);
		}
		$scope.alerts.push(newAlert);
		
		// let the alert vanish after x seconds
		$timeout(function(){
	        $scope.closeAlert($scope.alerts.indexOf(newAlert))
		}, delay)
	});

	// close the alert manually using the 'x' in the UI element
	$scope.closeAlert = function(index) {
		$scope.alerts.splice(index, 1);
	};
	
}]);