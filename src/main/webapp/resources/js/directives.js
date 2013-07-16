'use strict';

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
		replace: true,
		scope: { coordinates: '=' },
		template: '<div class="input-append"><input type="text" ng-model="coordinates" ng-list class="lnglat"></input>'
			+ '<button class="btn" type="button">'
			+ '<i class="icon-map-marker"></i></button></div>',
		link: function(scope, element, attrs) {
			$(element).find('input.lnglat').locationPicker();
		}
	};	
});

directives.directive('gazCopyUri', function() {
	return {
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
		replace: true,
		scope: { place: '=' },
		templateUrl: 'partials/placeTitle.html'
	};
});

directives.directive('gazPlacePicker', function() {
	return {
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
			
		}
	};
});

directives.directive('gazMap', function($location) {
	
	var blueIcon = "http://www.google.com/intl/en_us/mapfiles/ms/micons/blue-dot.png";
	var defaultIcon = "http://www.google.com/intl/en_us/mapfiles/ms/micons/red-dot.png";
	var defaultShadow = new google.maps.MarkerImage(
		'http://maps.gstatic.com/intl/en_us/mapfiles/markers/marker_sprite.png',
		new google.maps.Size(37,34),
		new google.maps.Point(20, 0),
		new google.maps.Point(10, 34)
	);
	
	return {
		replace: true,
		scope: {
			places: "=",
			zoom: "=",
			bbox: "=",
			highlight: "=",
			height: "@"
		},
		templateUrl: 'partials/map.html',
		controller: function($scope, $attrs, $element) {
			
			$scope.markers = [];
			$scope.markerMap = {};
			$scope.highlightedMarker = null;
			$scope.mapOptions = {
				center: new google.maps.LatLng(0, 0),
				zoom: $scope.zoom,
				mapTypeId: google.maps.MapTypeId.TERRAIN
			};
			
			$attrs.$observe('height', function(height) {
				$element[0].style.height = height + "px";
				//google.maps.event.trigger($scope.map, 'resize');
			});
			
			$scope.$watch("zoom", function() {
				if ($scope.zoom != $scope.map.getZoom()) {
					$scope.map.setZoom(parseInt($scope.zoom));
					$scope.map.setCenter(new google.maps.LatLng("0","0"));
				}
			});
			
			$scope.markerClick = function(id) {
				$location.path("/show/" + id);
			};
			
			$scope.markerOver = function(id) {
				$scope.highlight = id;
			};
			
			$scope.markerOut = function(id) {
				$scope.highlight = null;
			};
			
			$scope.$watch("highlight", function() {
				if ($scope.highlightedMarker != null) {
					$scope.highlightedMarker.setIcon(defaultIcon);
					$scope.highlightedMarker.setZIndex($scope.lastZIndex);
				}
				if ($scope.highlight != null && $scope.markerMap[$scope.highlight]) {
					$scope.markerMap[$scope.highlight].setIcon(blueIcon);
					$scope.highlightedMarker = $scope.markerMap[$scope.highlight];
					$scope.lastZIndex = $scope.highlightedMarker.getZIndex();
					$scope.highlightedMarker.setZIndex(1000);
				}
			});
			
			// add markers for locations and auto zoom and center map
			$scope.$watch("places", function() {
				
				$scope.markerMap = {};
				for (var i in $scope.markers)
					$scope.markers[i].setMap(null);
				
				if ($scope.places.length == 0) return;
				
				var bounds = new google.maps.LatLngBounds();
				var ll = new google.maps.LatLng("0","0");
				var numLocations = 0;
				for (var i in $scope.places) {	
					var place = $scope.places[i];		
					var title = "";
					if (place.prefName) title = place.prefName.title;
					if (place.prefLocation && angular.isNumber(place.prefLocation.coordinates[0])
							&& angular.isNumber(place.prefLocation.coordinates[1])) {
						ll = new google.maps.LatLng(place.prefLocation.coordinates[1], place.prefLocation.coordinates[0]);
						$scope.markers[i] = new google.maps.Marker({
							position: ll,
							title: title,
							map: $scope.map,
							icon: defaultIcon,
							shadow: defaultShadow
						});
						$scope.markerMap[place.gazId] = $scope.markers[i];
						bounds.extend(ll);
						numLocations++;
					}
				}
				
				if (numLocations > 1)
					$scope.map.fitBounds(bounds);
				else if (numLocations > 0)
					$scope.map.setCenter(ll);
				else {
					$scope.map.setZoom(parseInt($scope.zoom));
					$scope.map.setCenter(new google.maps.LatLng("0","0"));
				}
				
			});
			
		}
	};
});
