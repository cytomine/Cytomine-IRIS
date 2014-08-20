var iris = angular.module("irisApp");

iris.controller("helpCtrl", function($scope, $modal, $log) {

	$scope.help = {
		errors : {},
	};

	// open the help dialog
	$scope.showHelp = function(size, templateUrl) {

		// $log.info(templateUrl);

		var modalInstance = $modal.open({
			templateUrl : templateUrl,
			controller : function($scope, $modalInstance) {

				$scope.ok = function() {
					$modalInstance.close('here goes the result');
				};

				$scope.cancel = function() {
					$modalInstance.dismiss('cancel');
				};
			},
			size : size,
			resolve : {
				items : function() {
					return $scope.items;
				}
			}
		});

		modalInstance.result.then(function(result) {
			// callbackSuccess
			$log.info(result);
		}, function() {
			// callbackError
			$log.warn('Modal dismissed.');
		});
	};
});
