var iris = angular.module("irisApp");

iris.factory("helpService", function($modal, $log) {
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
		}
	}
});