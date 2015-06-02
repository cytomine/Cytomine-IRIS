if (typeof jQuery !== 'undefined') {
	(function($) {
		$('#spinner').ajaxStart(function() {
			$(this).fadeIn();
		}).ajaxStop(function() {
			$(this).fadeOut();
		});
	})(jQuery);
}

// ANGULAR DIRECTIVES
var iris = angular.module("irisApp");

iris.directive("userTree", ["$compile", function($compile){
	return {
		restrict: 'E',
		scope: false, // just pass through the scope from the parent controller
		templateUrl: 'templates/html/userTree.html'
	}
}]);

iris.directive("termTree", ["$compile", function($compile){
	return {
		restrict: 'E',
		scope: false, // just pass through the scope from the parent controller
		templateUrl: 'templates/html/termTree.html',
		link: function($scope, $elem, $attr){
			// link to the attribute
			$scope.parentid = $attr.parentid;
		}
	}
}]);

iris.directive("dndTermTree", ["$compile", function($compile){
	return {
		restrict: 'E',
		scope: false, // just pass through the scope from the parent controller
		templateUrl: 'templates/html/dndTermTree.html',
		link: function($scope, $elem, $attr){
			// link to the attribute
			$scope.parentid = $attr.parentid;
		}
	}
}]);

iris.directive("imageTree", ["$compile", function($compile){
	return {
		restrict: 'E',
		scope: false, // just pass through the scope from the parent controller
		templateUrl: 'templates/html/imageTree.html'
	}
}]);
