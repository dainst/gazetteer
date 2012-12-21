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

directives.directive('gazTooltip', function(messages) {
	return {
		link: function(scope, element, attrs) {
			scope.$watch(attrs.gazTooltip, function(code) {
				$(element).tooltip({title: messages[code.toLowerCase()]});
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

directives.directive('gazCopyUri', function() {
	return {
		restrict: 'E',
		replace: true,
		scope: { uri: '=' },
		templateUrl: 'partials/copyUri.html',
		link: function(scope, element, attrs) {
			$(element).find('a').click(function() {
				$(element).find('.modal').modal();
				$(element).find('input.uri').focus().select();
			});
		}
	};
});

directives.directive('gazPlaceNav', function() {
	return {
		restrict: 'E',
		replace: true,
		scope: { place: '=' },
		templateUrl: 'partials/placeNav.html',
		controller: function($scope, $attrs) {
			$scope.isActive = function(view) {
				return ($attrs.activeTab == view) ? 'active' : '';
			};
		}
	};
});

directives.directive('gazPlaceTitle', function() {
	return {
		restrict: 'E',
		replace: true,
		scope: { place: '=' },
		templateUrl: 'partials/placeTitle.html'
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

directives.directive('gazMap', function() {
	
	var map = null;
	var markers = [];
	
	return {
		restrict: 'E',
		replace: true,
		scope: { 
			places: '=',
			bbox: '=',
			zoom: '=',
			height: '@'
		},
		template: '<div id="map_canvas" style="height: {{height}}px;" asdf="{{zoom}}"></div>',
		link: function(scope, elements, attrs) {
			
			var autoScaledMap = true;
			
			// initialize map
			var mapOptions = {
				mapTypeId: google.maps.MapTypeId.TERRAIN
			};
			map = new google.maps.Map(elements[0], mapOptions);
			map.setZoom(parseInt(attrs.zoom));
			map.setCenter(new google.maps.LatLng("0","0"));
			
			// attach event listener to monitor manual bounds changes
			google.maps.event.addListener(map, "idle", function() {
				if (!autoScaledMap) {
					var b = map.getBounds();
					scope.$apply(function(scope) {
						scope.zoom = map.getZoom();
						scope.bbox = [b.getNorthEast().lat(),b.getNorthEast().lng(),
					              b.getSouthWest().lat(),b.getSouthWest().lng()];
					});
				} else {
					autoScaledMap = false;
				}
			});
			
			scope.$watch("zoom", function() {
				if (scope.zoom != map.getZoom()) {
					autoScaledMap = true;
					map.setZoom(parseInt(scope.zoom));
					map.setCenter(new google.maps.LatLng("0","0"));
				}
			});
			
			scope.$watch("places", function() {
				
				for (var i in markers)
					markers[i].setMap(null);
				
				// add markers for locations and auto zoom and center map
				var bounds = new google.maps.LatLngBounds();
				var ll = new google.maps.LatLng("0","0");
				var numLocations = 0;
				for (var i in scope.places) {	
					var place = scope.places[i];		
					var title = "";
					if (place.prefName) title = place.prefName.title;
					if (place.prefLocation) {
						ll = new google.maps.LatLng(place.prefLocation.coordinates[1], place.prefLocation.coordinates[0]);
						markers[i] = new google.maps.Marker({
							position: ll,
							title: title,
							map: map
						});
						bounds.extend(ll);
						numLocations++;
					}
				}
				
				/*autoScaledMap = true;
				if (numLocations > 1)
					map.fitBounds(bounds);
				else if (numLocations > 0)
					map.setCenter(ll);*/
				
			});
			
		}
	};
});