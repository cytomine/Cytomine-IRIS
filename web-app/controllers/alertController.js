var iris = angular.module("irisApp");

iris.controller("alertCtrl", function($rootScope, $scope, $timeout) {
	
	// limit the number of alerts to 8
	var maxAlerts = 8;
	$scope.alerts = [];
	
	// possible types of alert are: info, success, warning, danger, default
	$scope.$on("addAlert", function(event, mass) {
		var newAlert = {
			type : mass.type?mass.type:'success',
			msg : mass.msg,
		}
		
		// remove the oldest entry from the array
		if ($scope.alerts.length >= maxAlerts){
			$scope.closeAlert(0);
		}
		$scope.alerts.push(newAlert);
		
		// let the alert vanish after 7 seconds
		$timeout(function(){
	        $scope.closeAlert($scope.alerts.indexOf(newAlert))
		}, 4000)
	});

	// close the alert manually using the 'x' in the UI element
	$scope.closeAlert = function(index) {
		$scope.alerts.splice(index, 1);
	};
	
});