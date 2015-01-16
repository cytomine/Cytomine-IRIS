var iris = angular.module("irisApp");

iris.factory("helpService", [
                             "$rootScope", "$log",
                             function($rootScope, $log) {
	var defaultUrl = "content/help/defaultHelp.html";
	var contentUrl = defaultUrl;
	
	return {
		getContentUrl : function() {
			return contentUrl;
		},
		
		setContentUrl : function(url) {
			if (url === undefined || url === null){
				url = defaultUrl;
			}
			contentUrl = url;
		},
		
		// broadcasts the "show help" property to all children
		showHelp : function() {
			$rootScope.$broadcast("showPageHelp");
		}
	}
}]);