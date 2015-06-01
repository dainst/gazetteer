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

directives.directive('gazPlacePicker', function($document, $timeout) {
	return {
		replace: true,
		scope: { place: '=', id: '=' },
		templateUrl: 'partials/placePicker.html',
		controller: function($scope, $element, Place) {
			
			$scope.queryId = 0;
			
			$scope.search = {
				offset: 0,
				limit: 30,
				type: "prefix",
				q: "",
				noPolygons: true
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
				$scope.selectPlace($scope.places[0]);		
			};
			
			$scope.$watch("search.q", function() {
				$scope.queryId++;
				$scope.search.queryId = $scope.queryId;
				$scope.querySubmitted = true;
				$timeout(function() {
			          if ($scope.querySubmitted)
			        	  $scope.loading = true;
				}, 500);
				Place.query($scope.search, function(result) {
					if ($scope.queryId == result.queryId) {
						$scope.places = result.result;
						$scope.querySubmitted = false;
						$scope.loading = false;
					}
				});
			});
			
		}
	};
});

directives.directive('gazTagField', function($document) {
	return {
		replace: true,
		scope: { tags: '=', fieldwidth: '@', fieldname: '@', number: '=', deactivated: '='},
		templateUrl: 'partials/tagField.html',
		controller: function($scope, $element, Place) {
			
			$scope.inputText = "";
			$scope.suggestions = [];
			$scope.selectedSuggestionIndex = 0;
			$scope.backgroundColor = "#fcfcfc";
			
			$scope.$watch("deactivated", function() {
				if ($scope.deactivated) {
					$scope.backgroundColor = "#e6e6e6";
					$scope.tags = [];
				}
				else
					$scope.backgroundColor = "#fcfcfc";
			}),
			
			$scope.$watch("inputText", function() {
				if ($scope.inputText.slice(-1) == "," || $scope.inputText.slice(-1) == ";")
					$scope.addTag();
				
				$scope.updateSuggestions();
				$scope.selectedSuggestionIndex = 0;
				$scope.textFieldPos = document.getElementsByName("tagTextField")[parseInt($scope.number)].getBoundingClientRect().left;
				$scope.containerPos = document.getElementsByName("container")[0].getBoundingClientRect().left;
				$scope.textFieldPos -= $scope.containerPos;
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
					if ($scope.tags[i] == tagToRemove)
						$scope.tags.splice(i, 1);
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
				$scope.suggestions = [];
				if ($scope.inputText.length > 0) {
					Place.suggestions({ field: $scope.fieldname + ".suggest", text: $scope.inputText }, function(result) {
						for (var i = 0; i < result.suggestions.length; i++) {
							if (!$scope.searchInList(result.suggestions[i]))
								$scope.suggestions.push(result.suggestions[i]);
						}
					});
				}
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
								editable: true
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
						var firstLngLat = pathCoordinates[0];
						var lastLngLat = pathCoordinates[pathCoordinates.length - 1];
						if (firstLngLat[0] != lastLngLat[0] || firstLngLat[1] != lastLngLat[1])
							pathCoordinates.push(firstLngLat);
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
	var parentIcon = "//www.google.com/intl/en_us/mapfiles/ms/micons/pink.png";
	var childIcon = "//www.google.com/intl/en_us/mapfiles/ms/micons/green-dot.png";	
	
	var baseUri = $location.absUrl().substring(0, $location.absUrl().indexOf("app"));
	
	return {
		replace: true,
		scope: {
			places: "=",
			zoom: "=",
			bbox: "=",
			highlight: "=",
			map: "=",
			height: "@",
			mode: "="
		},
		templateUrl: 'partials/map.html',
		controller: function($scope, $attrs, $element) {
			
			$scope.markers = [];
			$scope.markerMap = {};
			$scope.shapes = [];
			$scope.shapeMap = {};
			$scope.highlightedMarker = null;
			$scope.highlightedShape = null;
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
				if (id.indexOf('+') > 0)
					id = id.substring(0, id.indexOf('+'));
				else if (id.indexOf('*') > 0)
					id = id.substring(0, id.indexOf('*'));
				$location.path("/show/" + id);
			}; 
			
			$scope.markerOver = function(id, marker) {
				var index;
				var type;
				var number = marker.number;
				if ((index = id.indexOf("*")) > -1) {
					id = id.substring(0, index);
					if (marker.mapType == "parent" || marker.mapType == "markerParent")
						type = "parentLocation";
					else
						type = "searchResult";
				}
				else if ((index = id.indexOf("+")) > -1) {
					id = id.substring(0, index);
					type = "alternativeLocation";
					number--;
				} else
					type = "prefLocation";
				$scope.highlight = { id: id, type: type, index: number, noCenter: true };
			};
			
			$scope.markerOut = function() {
				$scope.highlight = null;
			};
			
			$scope.$watch("highlight", function() {
				if ($scope.highlightedShape != null) {
					var fillOpacity = $scope.highlightedShape.get("fillOpacity") / 2;
					var strokeOpacity = $scope.highlightedShape.get("strokeOpacity") / 2;
					$scope.highlightedShape.setOptions({ fillOpacity: fillOpacity, strokeOpacity: strokeOpacity });
					$scope.highlightedShape = null;
				}
				
				if ($scope.highlight != null && $scope.highlight.type == "polygon") {
					var id = $scope.highlight.id;
					if ($scope.highlight.index > -1)
						id += "#" + $scope.highlight.index;					
					if ($scope.shapeMap[id]) {
						$scope.highlightedShape = $scope.shapeMap[id];
						var fillOpacity = $scope.highlightedShape.get("fillOpacity") * 2;
						var strokeOpacity = $scope.highlightedShape.get("strokeOpacity") * 2;
						$scope.highlightedShape.setOptions({ fillOpacity: fillOpacity, strokeOpacity: strokeOpacity });
						var bounds = new google.maps.LatLngBounds();
						bounds.extend($scope.highlightedShape.getBounds().getSouthWest());
						bounds.extend($scope.highlightedShape.getBounds().getNorthEast());
						$scope.map.fitBounds(bounds);
					}	
				}
				
				if ($scope.highlightedMarker != null) {
					if ($scope.highlightedMarkerType == "prefLocation")
						$scope.highlightedMarker.setIcon(defaultIcon);
					else if ($scope.highlightedMarkerType == "parent")
						$scope.highlightedMarker.setIcon(getNumberedMarkerIcon($scope.highlightedMarker.number, "pink"));
					else if ($scope.highlightedMarkerType == "searchResult")
						$scope.highlightedMarker.setIcon(getNumberedMarkerIcon($scope.highlightedMarker.number, "red"));
					else
						$scope.highlightedMarker.setIcon(getNumberedMarkerIcon($scope.highlightedMarker.number, "lightred"));
					$scope.highlightedMarker.setZIndex($scope.lastZIndex);
				}
				
				if ($scope.highlight != null) {
					var id = $scope.highlight.id;
					if ($scope.highlight.type == "alternativeLocation")
						id += "+" + $scope.highlight.index;
					else if ($scope.highlight.type == "parentLocation" || $scope.highlight.type == "searchResult")
						id += "*" + $scope.highlight.index;
					if ($scope.markerMap[id]) {
						if ($scope.highlight.type == "alternativeLocation") {
							$scope.highlightedMarker = $scope.markerMap[id];
							$scope.highlightedMarkerType = "alternative";
							$scope.highlightedMarker.number = $scope.highlight.index + 1;
							$scope.markerMap[id].setIcon(getNumberedMarkerIcon($scope.highlight.index + 1, "blue"));
						}
						else if ($scope.highlight.type == "parentLocation") {
							$scope.highlightedMarker = $scope.markerMap[id];
							$scope.highlightedMarkerType = "parent";
							$scope.highlightedMarker.number = $scope.highlight.index;
							$scope.markerMap[id].setIcon(getNumberedMarkerIcon($scope.highlight.index, "blue"));
						}
						else if ($scope.highlight.type == "searchResult") {
							$scope.highlightedMarker = $scope.markerMap[id];
							$scope.highlightedMarkerType = "searchResult";
							$scope.highlightedMarker.setIcon(getNumberedMarkerIcon($scope.highlight.index, "blue"));
						}
						else if ($scope.highlight.type == "prefLocation") {
							$scope.highlightedMarker = $scope.markerMap[id];
							$scope.highlightedMarkerType = "prefLocation";
							$scope.markerMap[id].setIcon(blueIcon);
						}
						if ($scope.highlightedMarker) {
							$scope.lastZIndex = $scope.highlightedMarker.getZIndex();
							$scope.highlightedMarker.setZIndex(1000);
							if (($scope.highlight.type == "prefLocation" || $scope.highlight.type == "alternativeLocation") && !$scope.highlight.noCenter)
								$scope.map.setCenter($scope.highlightedMarker.position);
						}
					}
				}
			});

			// add markers/shapes for locations and auto zoom and center map
			$scope.$watch("places", function() {
				$scope.markerMap = {};
				$scope.shapeMap = {};
				for (var i in $scope.markers)
					$scope.markers[i].setMap(null);

				for (var i in $scope.shapes)
					$scope.shapes[i].setMap(null);

				if ($scope.places.length == 0) return;

				var bounds = new google.maps.LatLngBounds();
				var ll = new google.maps.LatLng("0","0");
				var shape = null;
				var numLocations = 0;
				var childMarkerLocations = [];
				for (var i in $scope.places) {
					var place = $scope.places[i];
					var title = "";
					if (place.prefName) title = place.prefName.title;
					
					if (place.prefLocation) {
						if (place.prefLocation.coordinates && place.mapType != "polygonParent") {
							var icon = defaultIcon;
							if (place.mapType == "searchResults")
								icon = getNumberedMarkerIcon(parseInt(place.markerNumber), "red");
							else if (place.mapType == "markerParent" || place.mapType == "parent") {
								if (place.markerNumber)
									icon = getNumberedMarkerIcon(parseInt(place.markerNumber), "pink");
								else
									icon = parentIcon;
							}
							else if (place.mapType == "markerChild")
								icon = childIcon;

							if (angular.isNumber(place.prefLocation.coordinates[0]) && angular.isNumber(place.prefLocation.coordinates[1])) {
								ll = new google.maps.LatLng(place.prefLocation.coordinates[1], place.prefLocation.coordinates[0]);
								
								if (place.mapType == "markerChildInvisible") {
									childMarkerLocations.push(ll);
									continue;
								}
								
								if (place.mapType == "standard")
									childMarkerLocations.push(ll);
								
								var marker = new google.maps.Marker({
									position: ll,
									title: place.prefName.title,
									map: $scope.map,
									icon: icon,
									number: place.markerNumber,
									mapType: place.mapType
								});
								if (place.mapType == "markerParent" || place.mapType == "parent")
									marker.setZIndex(1);
								else if (place.mapType == "markerChild") {
									marker.setZIndex(1000);
									childMarkerLocations.push(ll);
								}
								else
									marker.setZIndex(2);
								$scope.markers.push(marker);
								if ((place.mapType == "markerParent" || place.mapType == "parent" && place.markerNumber) || place.mapType == "searchResults")
									$scope.markerMap[place.gazId + "*" + place.markerNumber] = marker;
								else
									$scope.markerMap[place.gazId] = marker;
								bounds.extend(ll);
								numLocations++;
							}
						}
						var fillOpacity = 0.35;
						var strokeOpacity = 0.7;
						if (place.mapType == "polygonParent" || place.mapType == "parent") {
							fillOpacity = 0.15;
							strokeOpacity = 0.25;
						}
						if ($scope.mode == "singlePlace" && place.locations != null && place.locations.length > 0) {
							for (var i in place.locations) {
								if (place.locations[i].coordinates && place.mapType == "standard") {
									if (angular.isNumber(place.locations[i].coordinates[0]) && angular.isNumber(place.locations[i].coordinates[1])) {
										ll = new google.maps.LatLng(place.locations[i].coordinates[1], place.locations[i].coordinates[0]);
										var marker = new google.maps.Marker({
											position: ll,
											title: place.prefName.title,
											map: $scope.map,
											icon: getNumberedMarkerIcon(parseInt(i) + 1, "lightred"),
											number: parseInt(i) + 1,
											mapType: place.mapType
										});
										$scope.markers.push(marker);
										$scope.markerMap[place.gazId + "+" + i] = marker;
										bounds.extend(ll);
										numLocations++;
									}
								}
								if (place.locations[i].shape) {
									var shapeCoordinates = convertShapeCoordinates(place.locations[i].shape);

									shape = new google.maps.Polygon({
										paths: shapeCoordinates,
										strokeColor: "#99CCFF",
										strokeOpacity: strokeOpacity,
										strokeWeight: 1,
										fillColor: "#99CCFF",
										fillOpacity: fillOpacity,
										placeId: place.gazId
									});
									shape.setMap($scope.map);
									$scope.shapes.push(shape);
									$scope.shapeMap[place.gazId + "#" + i] = shape;
									addPolygonListener(shape);

									bounds.extend(shape.getBounds().getSouthWest());
									bounds.extend(shape.getBounds().getNorthEast());
								}
							}
						}
						if (place.prefLocation.shape && place.mapType != "markerParent" && place.mapType != "markerChildInvisible") {
							var shapeCoordinates = convertShapeCoordinates(place.prefLocation.shape);
	
							shape = new google.maps.Polygon({
								paths: shapeCoordinates,
								strokeColor: "#000000",
								strokeOpacity: strokeOpacity,
								strokeWeight: 1,
								fillColor: "#000000",
								fillOpacity: fillOpacity,
								placeId: place.gazId
							});
							shape.setMap($scope.map);
							$scope.shapes.push(shape);
							$scope.shapeMap[place.gazId] = shape;
							addPolygonListener(shape);

							bounds.extend(shape.getBounds().getSouthWest());
							bounds.extend(shape.getBounds().getNorthEast());
						}
					}
				}
				
				if (childMarkerLocations.length > 1) {
					bounds = new google.maps.LatLngBounds();
					for (var i in childMarkerLocations) {
						bounds.extend(childMarkerLocations[i]);
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
			
			var addPolygonListener = function(polygon) {
				google.maps.event.addListener(polygon, 'click', function (event) {
					$location.path("/show/" + polygon.placeId);
					$scope.$apply();
				});
			};
			
			var convertShapeCoordinates = function(shape) {
				var shapeCoordinates = [];
				var counter = 0;

				for (var j = 0; j < shape.length; j++) {
					for (var k = 0; k < shape[j].length; k++) {
						var shapePolygonCoordinates = [];
						for (var l = 0; l < shape[j][k].length; l++)
							shapePolygonCoordinates[l] = new google.maps.LatLng(shape[j][k][l][1], shape[j][k][l][0]);
						shapeCoordinates[counter] = shapePolygonCoordinates;
						counter++;
					}
				}
				
				return shapeCoordinates;
			};
			
			var getNumberedMarkerIcon = function(number, color) {
				return baseUri + "markerImage/" + color + "/" + number;
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
            if(event.keyCode === 8) {
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
            if(event.keyCode === 13) {
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
            if(event.keyCode === 38) {
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
            if(event.keyCode === 40) {
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
