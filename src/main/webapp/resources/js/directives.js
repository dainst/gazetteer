'use strict';

/* Directives */


var directives = angular.module('gazetteer.directives', ['gazetteer.messages']);

directives.directive('gazTranslate', function(messages) {
	return {
		link: function(scope, element, attrs) {
			scope.$watch(attrs.gazTranslate, function(code) {
				element.text(messages[code.toLowerCase()]);
			});
		}
	};
});

directives.directive('gazLocationPicker', function() {	
	return {
		restrict: 'E',
		replace: true,
		scope: { coordinates: '=' },
		template: '<div class="input-append"><input type="text" ng-model="coordinates" ng-list class="lnglat"></input>'
			+ '<button class="picker-search-button btn" type="button">'
			+ '<i class="icon-map-marker"></i></button></div>',
		link: function(scope, element, attrs) {
			$(element).find('input.lnglat').locationPicker();
		}
	};	
});

directives.directive('gazPlacePicker', function() {
	return {
		restrict: 'E',
		replace: true,
		scope: { place: '=', id: '=' },
		templateUrl: 'partials/placePicker.html',
		controller: function($scope, Place) {
			
			$scope.search = {
				offset: 0,
				limit: 30,
				fuzzy: true,
				q: "" 
			};
			
			$scope.showOverlay = false;
			
			$scope.openOverlay = function() {
				$scope.showOverlay = true;
			};
			
			$scope.selectPlace = function(place) {
				$scope.place = place;
				$scope.id = place["@id"];
				$scope.showOverlay = false;
			};
			
			$scope.$watch("search.q", function() {
				Place.query($scope.search, function(result) {
					$scope.places = result.result;
				});
			});
			
		},
		link: function(scope, element, attrs) {
			
		}
	};
});
