var iris = angular.module("irisApp");

iris.controller("helpCtrl", ["$rootScope", "$scope", "$modal", "$log", "helpService", 
                             function($rootScope, $scope, $modal, $log, helpService) {

	// shared object of the help controller and the modal dialog
	// which will display the help page
	$scope.help = {
		  errors : {},
	};
	
	// catch the help event and show the modal
	$scope.$on("showPageHelp", function(event, mass) {
		$scope.showPageHelp();
	});
	
	$scope.isVisible = false;
	
	// shows the help function, if the modal is not visible
	$scope.showPageHelp = function(){
		if (!$scope.isVisible){
			$scope.display('lg','helpModal.html');
		}
	};
	
	// open the help dialog
	$scope.display = function(size, templateID) {

		// refresh the current content url for inclusion
		$scope.help.contentUrl = helpService.getContentUrl();

		var modalInstance = $modal.open({
			templateUrl : templateID,
			controller : modalHelpCtrl,
			size : size,
			resolve : {
				help : function() {
					return $scope.help;
				}
			}
		});
		$scope.isVisible = true;

		modalInstance.result.then(function(result) {
			// callbackSuccess
			$log.info(result);
			$scope.isVisible = false;
		}, function(result) {
			// callbackError
			$log.info(result);
			$scope.isVisible = false;
		});
	};
	
	// controller for the modal instance
	var modalHelpCtrl = function($scope, $modalInstance, help) {
		
		$scope.help = help;

		$scope.ok = function() {
			$modalInstance.close('ok');
		};

		$scope.cancel = function() {
			$modalInstance.dismiss('cancel');
		};
		
		$scope.showShortcuts = function(){
			// TODO implement trigger of '?'
		};
	}
}]);