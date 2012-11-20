'use strict';

/* Directives */


var directives = angular.module('gazetteer.directives', ['gazetteer.messages']);

directives.directive('gazTranslate', function(messages) {
	return {
		link: function(scope, element, attrs) {
			scope.$watch(attrs.gazTranslate, function(code) {
				element.text(messages[code]);
			});
		}
	};
});

directives.directive('gazLocationPicker', function() {
	
	return {
		restrict: 'E',
		replace: true,
		scope: { coordinates: '=' },
		template: '<div><input type="text" ng-model="coordinates" ng-list class="lnglat"></input></div>',
		link: function(scope, element, attrs) {
			$('input.lnglat').locationPicker();
		}
	};
});
