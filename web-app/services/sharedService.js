var iris = angular.module("irisApp");

/**
 * This service provides common (shared) functionality for the client.
 */
iris.factory("sharedService",function() {
		
		return {
			// string function for start with
			strStartsWith : function(string, prefix) {
				  return string.indexOf(prefix) == 0;
			},
				
			// string function for ends with
			strEndsWith : function(string, suffix) {
				  return string.indexOf(suffix, string.length - suffix.length) != -1;
			},

			// string function testing if string contains a substring
			strContains : function(string, substr) {
				  return string.indexOf(substr) != -1;
			},
			
		};
		
	});