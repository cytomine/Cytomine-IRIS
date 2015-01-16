var iris = angular.module("irisApp");

iris.controller("aboutCtrl", 
		["$scope", "$log", "helpService", "hotkeys", "cytomineService", 
		 function($scope, $log, helpService, hotkeys, cytomineService) {
	$log.debug("aboutCtrl");
	
	// help variable for this page
	helpService.setContentUrl(null);
	
	$scope.about = {};
	
	// put all valid shortcuts for this page here
//	hotkeys.bindTo($scope)
//	.add({
//		combo : 'h',
//		description : 'Show help for this page',
//		callback : function() {
//			helpService.showHelp();
//		}
//	});

	$scope.supportLink = function(){
		var coded = "oa2u2oo.Ww2PT@sJEpP2MlwT.wR"
			var key = "shxv2YSJKy1bjToGBO3XkIuHcnRLafrdDgNm7FViq0eAUZ6WzpQ4P89w5lECMt"
				var shift=coded.length
				var link=""
					for (i=0; i<coded.length; i++) {
						if (key.indexOf(coded.charAt(i))==-1) {
							ltr = coded.charAt(i)
							link += (ltr)
						}
						else {     
							ltr = (key.indexOf(coded.charAt(i))-shift+key.length) % key.length
							link += (key.charAt(ltr))
						}
					}
		return link
	}

	// retrieve the Cytomine IRIS application information
	$scope.getAppInfo = function() {
		cytomineService.getAppInfo(function(appInfo) {
			$scope.about.appInfo = appInfo;
			delete $scope.about.error;
		}, function(data, status){
			$scope.about.error = {
				status: status,
				message: data.error.message
			}
		});
	};
	$scope.getAppInfo();
}]);