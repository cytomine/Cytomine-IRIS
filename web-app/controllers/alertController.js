var iris = angular.module("irisApp");

iris.controller("alertCtrl", function($rootScope, $scope, $timeout) {
	
	// limit the number of alerts to 5
	var maxAlerts = 5;
	$scope.alerts = [];
	
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
		
		
		$timeout(function(){
	        $scope.closeAlert($scope.alerts.indexOf(newAlert))
		}, 4000)
	});

	$scope.closeAlert = function(index) {
		$scope.alerts.splice(index, 1);
	};
	
});