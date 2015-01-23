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
				type: "prefix",
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

directives.directive('gazTagField', function($document) {
	return {
		replace: true,
		scope: { tags: '=', fieldwidth: '@', fieldname: '@', number: '='},
		templateUrl: 'partials/tagField.html',
		controller: function($scope, $element, Place) {
			
			$scope.inputText = "";
			$scope.suggestions = [];
			$scope.selectedSuggestionIndex = 0;
			$scope.textFieldPos = document.getElementsByName("tagTextField")[parseInt($scope.number)].getBoundingClientRect().left;
			
			$scope.$watch("inputText", function() {
				if ($scope.inputText.slice(-1) == "," || $scope.inputText.slice(-1) == ";")
					$scope.addTag();
				
				$scope.updateSuggestions();
				$scope.selectedSuggestionIndex = 0;
				$scope.textFieldPos = document.getElementsByName("tagTextField")[parseInt($scope.number)].getBoundingClientRect().left;
			});
			
			$scope.addTag = function() {
				var newTag = $scope.inputText.replace(",", "").replace(";", "").trim();
				if (newTag != "" && !$scope.searchInList(newTag)) {
					if ($scope.tags == undefined || $scope.tags == null)
						$scope.tags = [];
					$scope.tags.push(newTag);
				}
				$scope.inputText = "";
			};
			
			$scope.removeTag = function(tagToRemove) {
				for (var i = 0; i < $scope.tags.length; i++) {
					if ($scope.tags[i] == tagToRemove) {
						$scope.tags.splice(i, 1);
					}
				}
			};
			
			$scope.backspace = function() {
				if ($scope.inputText != "")
					$scope.inputText = $scope.inputText.slice(0, -1);
				else if ($scope.tags != null && $scope.tags != undefined && $scope.tags.length > 0)
					$scope.tags.pop();
			};
			
			$scope.searchInList = function(tag) {
				if ($scope.tags == undefined || $scope.tags == null)
					return false;
				
				for (var i = 0; i < $scope.tags.length; i++) {
					if ($scope.tags[i] == tag)
						return true;
				}
				
				return false;
			};
			
			$scope.updateSuggestions = function() {
				Place.suggestions({ field: $scope.fieldname + ".suggest", text: $scope.inputText }, function(result) {
					$scope.suggestions = [];
					
					for (var i = 0; i < result.suggestions.length; i++) {
						if (!$scope.searchInList(result.suggestions[i]))
							$scope.suggestions.push(result.suggestions[i]);
					}
				});
			};
			
			$scope.chooseSuggestion = function() {
				if ($scope.suggestions.length != 0) {					
					var suggestion = $scope.suggestions[$scope.selectedSuggestionIndex];
				
					if ($scope.tags == undefined || $scope.tags == null)
						$scope.tags = [];
					$scope.tags.push(suggestion);
				
					$scope.inputText = "";
				}
			};
			
			$scope.setSelectedSuggestionIndex = function(index) {
				$scope.selectedSuggestionIndex = index;
			};
			
			$scope.selectPreviousSuggestion = function() {
				if ($scope.suggestions.length > 0) {
					$scope.selectedSuggestionIndex -= 1;
					if ($scope.selectedSuggestionIndex < 0)
						$scope.selectedSuggestionIndex = $scope.suggestions.length - 1;
				}
			};
			
			$scope.selectNextSuggestion = function() {
				if ($scope.suggestions.length > 0) {
					$scope.selectedSuggestionIndex += 1;
					if ($scope.selectedSuggestionIndex >= $scope.suggestions.length)
						$scope.selectedSuggestionIndex = 0;
				}
			};
			
			$scope.lostFocus = function() {
				if ($scope.inputText != "")
					$scope.addTag();
				$scope.suggestions = [];
			};
		}
	};
});

directives.directive('gazShapeEditor', function($document, PolygonValidator) {
	return {
		replace: true,
		scope: {
			shape: '=',
			pos: '=',
			editorName: '='
		},
		templateUrl: 'partials/shapeEditor.html',
		controller: function($scope, $element) {
			
			$scope.gmapsShapes = [];
			$scope.initialized = false;
			
			$scope.mapOptions = {
				center: new google.maps.LatLng(0, 0),
				zoom: 10,
				mapTypeId: google.maps.MapTypeId.TERRAIN,
				scaleControl: true
			};
	
			$scope.showOverlay = false;
			
			$scope.openOverlay = function() {
				$scope.showOverlay = true;
				window.setTimeout(function() { $scope.initialize(); }, 20);
			};
			
			$scope.closeOverlay = function() {
				$scope.showOverlay = false;
				$scope.initialized = false;
			};
			
			$scope.initialize = function() {
				if (!$scope.initialized) {
					google.maps.event.trigger($scope.map, 'resize');
					$scope.initialized = true;
					$scope.gmapsShapes = [];
					
					if ($scope.pos && $scope.pos.length != 0)
						$scope.map.setCenter(new google.maps.LatLng($scope.pos[1], $scope.pos[0]));
				
					var drawingManager = new google.maps.drawing.DrawingManager({
						drawingControl: true,
						drawingControlOptions: {
							position: google.maps.ControlPosition.TOP_CENTER,
							drawingModes: [google.maps.drawing.OverlayType.POLYGON]
						},
						polygonOptions: {
							strokeColor: "000000",
							strokeOpacity: 0.7,
							strokeWeight: 1.5,
							fillColor: "#000000",
							fillOpacity: 0.25,
							editable: true
						}
					});

					drawingManager.setMap($scope.map);
					
					google.maps.event.addListener(drawingManager, 'polygoncomplete', function(polygon) {
						$scope.addPolygon(polygon);
					});
					
					if ($scope.shape) {
						var bounds = new google.maps.LatLngBounds();
						
						for (var i = 0; i < $scope.shape.length; i++) {
							var polygonCoordinates = [];		
							
							for (var j = 0; j < $scope.shape[i].length; j++) {
								var pathCoordinates = [];
								for (var k = 0; k < $scope.shape[i][j].length; k++)
									pathCoordinates[k] = new google.maps.LatLng($scope.shape[i][j][k][1], $scope.shape[i][j][k][0]);
								if (pathCoordinates[0].lat() == pathCoordinates[pathCoordinates.length - 1].lat() &&
										pathCoordinates[0].lng() == pathCoordinates[pathCoordinates.length - 1].lng()) {
									pathCoordinates.splice(pathCoordinates.length - 1, 1);
								}
								polygonCoordinates[j] = pathCoordinates;
							}				
							
							$scope.gmapsShapes[i] = new google.maps.Polygon({
								paths: polygonCoordinates,
								strokeColor: "#000000",
								strokeOpacity: 0.7,
								strokeWeight: 1.5,
								fillColor: "#000000",
								fillOpacity: 0.25,
								editable: true,
							});
							
							$scope.gmapsShapes[i].setMap($scope.map);
							
							bounds.extend($scope.gmapsShapes[i].getBounds().getSouthWest());
							bounds.extend($scope.gmapsShapes[i].getBounds().getNorthEast());
							
							$scope.addListeners($scope.gmapsShapes[i]);
						}

						$scope.map.fitBounds(bounds);
					}
				}
			};
			
			$scope.addPolygon = function(polygon) {
				var insidePolygon = false;
				var polygonInside = false;

				if (polygon.getPath().getLength() < 3 || $scope.checkForIntersections(polygon.getPath()))
					polygon.setMap(null);
				else {
					for (var i = 0; i < $scope.gmapsShapes.length; i++) {						
						$scope.gmapsShapes[i].getPaths().forEach(function(path) {
							if (google.maps.geometry.poly.containsLocation(path.getAt(0), polygon))
								polygonInside = true;
						});
					}
					
					if (!polygonInside) {
						for (var i = 0; i < $scope.gmapsShapes.length; i++) {						
							if (google.maps.geometry.poly.containsLocation(polygon.getPath().getAt(0), $scope.gmapsShapes[i])) {						
								if ($scope.isClockwise(polygon.getPath()) == $scope.isClockwise($scope.gmapsShapes[i].getPath())) {
									var pathArray = polygon.getPath().getArray();
									pathArray.reverse();
									polygon.setPath(new google.maps.MVCArray(pathArray));
								}
								
								$scope.gmapsShapes[i].getPaths().push(polygon.getPath());
								$scope.addPathListeners(polygon.getPath());
								insidePolygon = true;
							}
						}
					}
					
					if (!polygonInside && !insidePolygon) {
						$scope.gmapsShapes.push(polygon);
						$scope.addListeners(polygon);
					} else
						polygon.setMap(null);
				}
			};
			
			$scope.addListeners = function(polygon) {
				polygon.addListener("rightclick", function(event) {
					if (event.path != null && event.vertex != null) {
						var path = this.getPaths().getAt(event.path);
						if (path.getLength() > 3)
							path.removeAt(event.vertex);
						else
							this.getPaths().removeAt(event.path);
					}
				});
				
				polygon.getPaths().forEach(function(path) {
					$scope.addPathListeners(path);
				});
			};
			
			$scope.addPathListeners = function(path) {
				google.maps.event.addListener(path, 'insert_at', function(index) {
					if ($scope.checkForIntersections(this))
						this.removeAt(index);
				});
				
				google.maps.event.addListener(path, 'set_at', function(index, oldLatLng) {
					if ($scope.checkForIntersections(this))
						this.setAt(index, oldLatLng);
					else {						
						for (var i = 0; i < $scope.gmapsShapes.length; i++) {
							if (google.maps.geometry.poly.containsLocation(this.getAt(0), $scope.gmapsShapes[i])) {
								var path = this;
								if ($scope.gmapsShapes[i].getPath() != path && $scope.isClockwise(path) == $scope.isClockwise($scope.gmapsShapes[i].getPath())) {
									var tempArray = path.getArray();
									tempArray.reverse();
									
									for (var j = 0; j < tempArray.length; j++) {
										path.setAt(j, tempArray[j]);
									}
								}
							}
						}
					}
				});
			};
			
			$scope.saveShape = function() {
				var shapeCoordinates = [];
				var emptyPolygons = 0;
				for (var i = 0; i < $scope.gmapsShapes.length; i++) {
					var polygonCoordinates = [];
					var pathCounter = 0;
					
					$scope.gmapsShapes[i].getPaths().forEach(function(path) {
						var pathCoordinates = [];
						var pathData = path.getArray();
						for (var j = 0; j < pathData.length; j++) {
							var lngLat = [pathData[j].lng(), pathData[j].lat()];						
							pathCoordinates[j] = lngLat;
						}
						if (pathCoordinates.length == 3)
							pathCoordinates[3] = pathCoordinates[0];
						polygonCoordinates[pathCounter] = pathCoordinates;
						pathCounter++;
					});
					if (polygonCoordinates.length == 0)
						emptyPolygons++;
					else					
						shapeCoordinates[i - emptyPolygons] = polygonCoordinates;
				}
				
				if (shapeCoordinates.length == 0)
					$scope.shape = null;
				else
					$scope.shape = shapeCoordinates;
				
				$scope.closeOverlay();
			};
			
			$scope.isClockwise = function(path) {
				var pathData = path.getArray();
				var result = 0;
				for (var i = 0; i < pathData.length; i++) {
					if (i + 1 < pathData.length) {
						var latResult = pathData[i + 1].lat() - pathData[i].lat();
						var lngResult = pathData[i + 1].lng() + pathData[i].lng();
						result += (latResult * lngResult);
					}
					else {
						var latResult = pathData[0].lat() - pathData[i].lat();
						var lngResult = pathData[0].lng() + pathData[i].lng();
						result += (latResult * lngResult);
					}
				}
				if (result >= 0)
					return true;
				else
					return false;
			};
			
			$scope.checkForIntersections = function(pathToCheck) {
				var intersecting = false;
				
				for (var i = 0; i < $scope.gmapsShapes.length; i++) {					
					$scope.gmapsShapes[i].getPaths().forEach(function(path) {
						if (PolygonValidator.checkForPathIntersection(pathToCheck, path))
							intersecting = true;
					});
				}
				
				if (PolygonValidator.checkForPathIntersection(pathToCheck, pathToCheck))
					intersecting = true;
				
				return intersecting;
			};
			
			$scope.deleteShape = function() {
				$scope.shape = null;
			};
		}
	};
});

directives.directive('gazMap', function($location, Place) {
	
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
			map: "=",
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
				mapTypeId: google.maps.MapTypeId.TERRAIN,
				scaleControl: true
			};
			
			$attrs.$observe('height', function(height) {
				$element[0].style.height = height + "px";
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

			// add markers/shapes for locations and auto zoom and center map
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
				var markerLocationIds = [];
				for (var i in $scope.places) {
					var place = $scope.places[i];
					var title = "";
					if (place.prefName) title = place.prefName.title;
					var markerLocationInfo = getMarkerLocation(place);

					if (markerLocationInfo) {
						var alreadyDisplayed = false;
						for (var i in markerLocationIds) {
							if (markerLocationIds[i] == markerLocationInfo.placeId)
								alreadyDisplayed = true;
						}
						if (!alreadyDisplayed) {
							markerLocationIds.push(markerLocationInfo.placeId);
							var markerLocation = markerLocationInfo.location;							
							if (markerLocation.coordinates) {
								if (angular.isNumber(markerLocation.coordinates[0]) && angular.isNumber(markerLocation.coordinates[1])) {
									ll = new google.maps.LatLng(markerLocation.coordinates[1], markerLocation.coordinates[0]);
									$scope.markers[i] = new google.maps.Marker({
										position: ll,
										title: markerLocationInfo.name,
										map: $scope.map,
										icon: defaultIcon,
										shadow: defaultShadow
									});
									$scope.markerMap[place.gazId] = $scope.markers[i];
									bounds.extend(ll);
									numLocations++;
								}
							}
							if (markerLocation.shape) {
								var shapeCoordinates = [];
								var counter = 0;
	
								for (var j = 0; j < markerLocation.shape.length; j++) {
									for (var k = 0; k < markerLocation.shape[j].length; k++) {
										var shapePolygonCoordinates = [];
										for (var l = 0; l < markerLocation.shape[j][k].length; l++)
											shapePolygonCoordinates[l] = new google.maps.LatLng(markerLocation.shape[j][k][l][1], markerLocation.shape[j][k][l][0]);
										shapeCoordinates[counter] = shapePolygonCoordinates;
										counter++;
									}
								}
	
								$scope.shapes[i] = new google.maps.Polygon({
									paths: shapeCoordinates,
									strokeColor: "#000000",
									strokeOpacity: 0.7,
									strokeWeight: 1.5,
									fillColor: "#000000",
									fillOpacity: 0.25,
								});
								$scope.shapes[i].setMap($scope.map);
	
								shape = $scope.shapes[i];
								bounds.extend(shape.getBounds().getSouthWest());
								bounds.extend(shape.getBounds().getNorthEast());
							}
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
			
			var getMarkerLocation = function(place) {
				var markerPlace = null;
				
				if (place.prefLocation &&
						((place.prefLocation.coordinates && place.prefLocation.coordinates.length > 0) || place.prefLocation.shape)) {
					markerPlace = place;
				}
				else if (place.parents) {
					for (var i = 0; i < place.parents.length; i++) {
						if (place.parents[i].prefLocation && (place.parents[i].prefLocation.coordinates || place.parents[i].prefLocation.shape)) {
							markerPlace = place.parents[i];
							break;
						}
					}
				}
				
				if (markerPlace) {
					var placeName = "";
					if (markerPlace.prefName)
						placeName = markerPlace.prefName.title;
					return { name: placeName, location: markerPlace.prefLocation, placeId: markerPlace.gazId };
				}
				
				return null;
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

directives.directive('onBackspace', function () {
    return function (scope, element, attrs) {
        element.bind("keydown keypress", function (event) {
            if(event.which === 8) {
                scope.$apply(function (){
                    scope.$eval(attrs.onBackspace);
                });
 
                event.preventDefault();
            }
        });
    };
});

directives.directive('onEnter', function () {
    return function (scope, element, attrs) {
        element.bind("keydown keypress", function (event) {
            if(event.which === 13) {
                scope.$apply(function () {
                    scope.$eval(attrs.onEnter);
                });
 
                event.preventDefault();
            }
        });
    };
});

directives.directive('onArrowUp', function () {
    return function (scope, element, attrs) {
        element.bind("keydown keypress", function (event) {
            if(event.which === 38) {
                scope.$apply(function () {
                    scope.$eval(attrs.onArrowUp);
                });
 
                event.preventDefault();
            }
        });
    };
});

directives.directive('onArrowDown', function () {
    return function (scope, element, attrs) {
        element.bind("keydown keypress", function (event) {
            if(event.which === 40) {
                scope.$apply(function () {
                    scope.$eval(attrs.onArrowDown);
                });
 
                event.preventDefault();
            }
        });
    };
});

directives.directive('onBlur', function() {
    return function(scope, element, attrs) {
    	element.bind("blur", function() {
    		scope.$apply(function () {
    			scope.$eval(attrs.onBlur);
    		});
    	});
    };
  });

directives.directive('scrollPosition', function($window) {
	  return {
	    scope: {
	      scroll: '=scrollPosition'
	    },
	    link: function(scope, element, attrs) {
	      var windowElement = angular.element($window);
	      var handler = function() {
	        scope.scroll = windowElement.scrollTop();
	      };
	      windowElement.on('scroll', scope.$apply.bind(scope, handler));
	      handler();
	    }
	  };
	});