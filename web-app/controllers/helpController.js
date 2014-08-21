var iris = angular.module("irisApp");

iris.controller("helpCtrl", function($scope, $modal, $log, helpService) {

	// shared object of the help controller and the modal dialog
	// which will display the help page
	$scope.help = {
      contentUrl : helpService.getContentUrl(),
		  errors : {},
	};
	
	// open the help dialog
	$scope.showHelp = function(size, templateID) {

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

		modalInstance.result.then(function(result) {
			// callbackSuccess
			$log.info(result);
		}, function(result) {
			// callbackError
			$log.info(result);
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
	}
});
		

		
	
