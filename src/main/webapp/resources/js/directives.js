'use strict';

var directives = angular.module('gazetteer.directives', ['gazetteer.messages', 'ui.bootstrap']);

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

directives.directive('gazChangeHistory', function() {
	return {
		replace: true,
		scope: { changeHistory: '=' },
		templateUrl: 'partials/changeHistory.html',
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

directives.directive('gazPlacePicker', function($document) {
	return {
		replace: true,
		scope: { place: '=', id: '=' },
		templateUrl: 'partials/placePicker.html',
		controller: function($scope, $element, Place) {
			
			$scope.search = {
				offset: 0,
				limit: 30,
				fuzzy: true,
				q: "" 
			};
			
			$scope.showOverlay = false;
			
			$scope.openOverlay = function() {
				$element.find("input").focus();
				$scope.showOverlay = true;
			};
			
			$scope.closeOverlay = function() {
				$scope.showOverlay = false;
			};
			
			$scope.selectPlace = function(place) {
				$scope.place = place;
				$scope.id = place["@id"];
				$scope.showOverlay = false;
			};
			
			$scope.pickFirst = function() {
				Place.query($scope.search, function(result) {
					$scope.places = result.result;
				});
				
				$scope.selectPlace($scope.places[0]);				
			};
			
			$scope.$watch("search.q", function() {
				Place.query($scope.search, function(result) {
					$scope.places = result.result;
				});
			});
			
		}
	};
});

directives.directive('gazPlaceTypePicker', function($document) {
	return {
		replace: true,
		scope: { place: '=' },
		templateUrl: 'partials/placeTypePicker.html',
		controller: function($scope, $element) {
			
			$scope.showOverlay = false;
			
			$scope.openOverlay = function() {
				$element.find("input").focus();
				$scope.showOverlay = true;
			};
			
			$scope.closeOverlay = function() {
				$scope.showOverlay = false;
			};
			
			$scope.selectType = function(placeType) {
				$scope.place.type = placeType;
				$scope.showOverlay = false;
			};
			
		}
	};
});

directives.directive('gazMap', function($location) {
	
	var blueIcon = "//www.google.com/intl/en_us/mapfiles/ms/micons/blue-dot.png";
	var defaultIcon = "//www.google.com/intl/en_us/mapfiles/ms/micons/red-dot.png";
	var defaultShadow = new google.maps.MarkerImage(
		'//maps.gstatic.com/intl/en_us/mapfiles/markers/marker_sprite.png',
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
			$scope.shapes = [];
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
				
				for (var i in $scope.shapes)
					$scope.shapes[i].setMap(null);
				
				if ($scope.places.length == 0) return;
				
				var bounds = new google.maps.LatLngBounds();
				var ll = new google.maps.LatLng("0","0");
				var shape = null;
				var numLocations = 0;
				for (var i in $scope.places) {	
					var place = $scope.places[i];		
					var title = "";
					if (place.prefName) title = place.prefName.title;
					
					if (place.prefLocation) {
						if (angular.isNumber(place.prefLocation.coordinates[0]) && angular.isNumber(place.prefLocation.coordinates[1])) {
							ll = new google.maps.LatLng(place.prefLocation.coordinates[0], place.prefLocation.coordinates[1]);
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
						if (place.prefLocation.shape) {
							var shapeCoordinates = [];
							var counter = 0;
							
							for (var j = 0; j < place.prefLocation.shape.length; j++) {
								for (var k = 0; k < place.prefLocation.shape[j].length; k++) {
									var shapePolygonCoordinates = [];
									for (var l = 0; l < place.prefLocation.shape[j][k].length; l++)
										shapePolygonCoordinates[l] = new google.maps.LatLng(place.prefLocation.shape[j][k][l][1], place.prefLocation.shape[j][k][l][0]);
									shapeCoordinates[counter] = shapePolygonCoordinates;
									counter++;
								}
							}
							
							$scope.shapes[i] = new google.maps.Polygon({
								paths: shapeCoordinates,
								strokeColor: "##6786ad",
								strokeOpacity: 0.7,
								strokeWeight: 1.5,
								fillColor: "##6786ad",
							    fillOpacity: 0.25
							});
							$scope.shapes[i].setMap($scope.map);
							
							shape = $scope.shapes[i];
							bounds.extend(shape.getBounds().getSouthWest());
							bounds.extend(shape.getBounds().getNorthEast());
						}
					}
				}
				
				if (shape != null || numLocations > 1)
					$scope.map.fitBounds(bounds);
				else if (numLocations > 0)
					$scope.map.setCenter(ll);
				else {
					$scope.map.setZoom(parseInt($scope.zoom));
					$scope.map.setCenter(new google.maps.LatLng("0","0"));
				}
				
			});
			
			google.maps.Polygon.prototype.getBounds = function() {
			    var bounds = new google.maps.LatLngBounds();
			    var paths = this.getPaths();
			    var path;        
			    for (var i = 0; i < paths.getLength(); i++) {
			        path = paths.getAt(i);
			        for (var j = 0; j < path.getLength(); j++) {
			            bounds.extend(path.getAt(j));
			        }
			    }
			    return bounds;
			};
		}
	};
});

directives.directive('focusMe', function($timeout, $parse) {
  return {
    link: function(scope, element, attrs) {
      var model = $parse(attrs.focusMe);
      scope.$watch(model, function(value) {
        if(value === true) { 
          $timeout(function() {
            element[0].focus(); 
          });
        }
      });
    }
  };
});
