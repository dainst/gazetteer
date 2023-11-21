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

directives.directive('gazTextTooltip', function() {
	return {
		link: function(scope, element, attrs) {
			scope.$watch(attrs.gazTextTooltip, function(messageText) {
				$(element).tooltip({title: messageText});
			});
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
		scope: { place: '=', id: '=', excludeId: '=' },
		templateUrl: 'partials/placePicker.html',
		controller: function($scope, $element, Place) {
			
			$scope.queryId = 0;
			
			$scope.search = {
				offset: 0,
				limit: 30,
				type: "prefix",
				q: "",
				fq: "",
				noPolygons: true,
				add: "sort"
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
				if (place) {
					$scope.place = place;
					$scope.id = place["@id"];
					$scope.showOverlay = false;
				}
			};
			
			$scope.pickFirst = function() {
				$scope.selectPlace($scope.places[0]);		
			};
			
			$scope.$watch("excludeId", function() {
				
				if (!$scope.excludeId || $scope.excludeId == "") {
					$scope.search.fq = "";
				} else {
					$scope.search.fq = "NOT _id:" + $scope.excludeId;
				}
			});
			
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
			$scope.queryId = 0;
			
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
				$scope.queryId++;
				if ($scope.inputText.length > 0) {
					Place.suggestions({ field: $scope.fieldname + ".suggest", text: $scope.inputText, queryId: $scope.queryId }, function(result) {
						if (result.queryId[0] == $scope.queryId) {
							for (var i = 0; i < result.suggestions.length; i++) {
								if (!$scope.searchInList(result.suggestions[i]))
									$scope.suggestions.push(result.suggestions[i]);
							}
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

directives.directive('gazLocationPicker', function($document, $timeout, MapTypeService) {
	return {
		replace: true,
		scope: {
			coordinates: '=',
			deactivated: '='
		},
		templateUrl: 'partials/locationPicker.html',
		controller: function($scope, $element) {
			
			$scope.initialized = false;
			$scope.loaded = false;
			
			$scope.mapOptions = {
				center: new google.maps.LatLng(0, 0),
				zoom: 10,
				mapTypeId: MapTypeService.getMapTypeId(),
				scaleControl: true
			};
			
			$scope.setUpdateMapPropertiesTimer = function() {
				$timeout($scope.updateMapProperties, 200);
			};
			
			$scope.updateMapProperties = function() {
				MapTypeService.setMapTypeId($scope.map.getMapTypeId());
			};
	
			$scope.showOverlay = false;
			
			$scope.openOverlay = function() {
				if (!$scope.validCoordinates) {
					var geocoder = new google.maps.Geocoder();
					geocoder.geocode({'address': $scope.coordinatesText}, function(results, status) {
						if (status == google.maps.GeocoderStatus.OK) {
							$scope.marker.setPosition(results[0].geometry.location);
							if (results[0].viewport) {
								$scope.map.fitBounds(results[0].viewport);
								$scope.map.setZoom($scope.map.getZoom() + 2);
							} else
								$scope.map.panTo(results[0].geometry.location);
						}
					});
				}
				
				$scope.mapOptions.mapTypeId = MapTypeService.getMapTypeId();
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
					
					var latLng;
					if ($scope.coordinates && $scope.coordinates.length != 0)
						latLng = new google.maps.LatLng($scope.coordinates[1], $scope.coordinates[0]);
					else
						latLng = new google.maps.LatLng(0, 0);
					
					$scope.map.setCenter(latLng);
					
					$scope.marker = new google.maps.Marker({
					    position: latLng,
					    map: $scope.map,
					    draggable: true
					});
					
					MapTypeService.addMap($scope.map);
					
					google.maps.event.addListener($scope.map, "click", function(event) {
					    $scope.marker.setPosition(new google.maps.LatLng(event.latLng.lat(), event.latLng.lng()));
					});
				}
			};
			
			$scope.saveCoordinates = function() {
				$scope.coordinates = [+$scope.marker.getPosition().lng().toFixed(6), +$scope.marker.getPosition().lat().toFixed(6)];
				$scope.closeOverlay();
			};
			
			$scope.checkForGeocoding = function() {
				if (!$scope.validCoordinates) {
					var geocoder = new google.maps.Geocoder();
					geocoder.geocode({'address': $scope.coordinatesText}, function(results, status) {
						if (status == google.maps.GeocoderStatus.OK)
							$scope.coordinates = [+results[0].geometry.location.lng().toFixed(6), +results[0].geometry.location.lat().toFixed(6)];
							$scope.$apply();
					});
				}
				
				$scope.openOverlay();
			};
			
			$scope.$watch("coordinates", function() {
				if ($scope.coordinates)
					$scope.coordinatesText = $scope.coordinates.slice(0).reverse().join(",");
				else
					$scope.coordinatesText = "";
			});
			
			$scope.$watch("coordinatesText", function() {
				if ($scope.coordinatesText == "" && $scope.loaded)
					$scope.coordinates = [];
				var trimmedCoordinatesText = $scope.coordinatesText.trim();
				if (/^-?\d+\.?\d*\°?s*,\s*-?\d+\.?\d*°?$/.test(trimmedCoordinatesText)) {
					var index = trimmedCoordinatesText.indexOf(",");
					$scope.coordinates[1] = parseFloat(trimmedCoordinatesText.substring(0, index));
					$scope.coordinates[0] = parseFloat(trimmedCoordinatesText.substring(index + 1));
					$scope.validCoordinates = true;
				} else
					$scope.validCoordinates = false;
				$scope.loaded = true;
			});
		}
	};
});

directives.directive('gazShapeEditor', function($document, $timeout, $http, PolygonValidator, MapTypeService) {
	return {
		replace: true,
		scope: {
			shape: '=',
			pos: '=',
			editorName: '=',
			deactivated: '='
		},
		templateUrl: 'partials/shapeEditor.html',
		controller: function($scope, $element) {
			
			$scope.gmapsShapes = [];
			$scope.initialized = false;
			$scope.coordinatesString = "";
			$scope.coordinatesStringFormat = "geojson";
			$scope.parsingError = undefined;
			$scope.showMapOverlay = false;
			$scope.showTextInputOverlay = false;
			$scope.loading = 0;
			
			$scope.mapOptions = {
				center: new google.maps.LatLng(0, 0),
				zoom: 10,
				mapTypeId: MapTypeService.getMapTypeId(),
				scaleControl: true
			};
			
			$scope.setUpdateMapPropertiesTimer = function() {
				$timeout($scope.updateMapProperties, 200);
			};
			
			$scope.updateMapProperties = function() {
				MapTypeService.setMapTypeId($scope.map.getMapTypeId());
			};
			
			$scope.openMapOverlay = function() {
				$scope.mapOptions.mapTypeId = MapTypeService.getMapTypeId();
				$scope.showMapOverlay = true;
				window.setTimeout(function() { $scope.initialize(); }, 20);
			};
			
			$scope.closeMapOverlay = function() {
				$scope.showMapOverlay = false;
				$scope.initialized = false;
			};
			
			$scope.openTextInputOverlay = function() {
				$scope.reloadCoordinatesString();
				$scope.parsingError = undefined;
				$scope.showTextInputOverlay = true;
			};
			
			$scope.closeTextInputOverlay = function() {
				if ($scope.loading == 0) {
					$scope.showTextInputOverlay = false;
				}	
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
							editable: true,
							draggable: true
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
									pathCoordinates[k] = new google.maps.LatLng(Number(($scope.shape[i][j][k][1]).toFixed(13)), Number(($scope.shape[i][j][k][0]).toFixed(13)));
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
								draggable: true
							});
							
							$scope.gmapsShapes[i].setMap($scope.map);
							
							bounds.extend($scope.gmapsShapes[i].getBounds().getSouthWest());
							bounds.extend($scope.gmapsShapes[i].getBounds().getNorthEast());
							
							$scope.addListeners($scope.gmapsShapes[i]);
						}

						$scope.map.fitBounds(bounds);
					}
					
					MapTypeService.addMap($scope.map);
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
				
				$scope.closeMapOverlay();
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
			
			$scope.parseCoordinatesString = function() {
				$scope.loading++;
				$scope.parsingError = undefined;
				
				var shapeCoordinates;
				
				switch ($scope.coordinatesStringFormat) {
				case "wkt":
					shapeCoordinates = parseWkt();
					break;
				case "geojson":
					shapeCoordinates = parseGeoJson();
					break;
				}
				
				if (shapeCoordinates) {
					$http.post("../validation/multipolygon", shapeCoordinates)
						.success(function(result) {
							$scope.loading--;
							if (result.success) {
								$scope.shape = shapeCoordinates;
								$scope.closeTextInputOverlay();
							} else {
								$scope.parsingError = { msgKey: "validation." + result.messageKey, data: result.messageData };
							}
						})
					.error(function() {
						$scope.loading--;
						$scope.parsingError = { msgKey: "validation.genericvalidationerror" };
					});
				} else {
					$scope.loading--;
				}
			};
			
			$scope.reloadCoordinatesString = function() {
				$scope.parsingError = undefined;
				
				if ($scope.shape && $scope.shape.length > 0) {
				
					switch ($scope.coordinatesStringFormat) {
					case "wkt":
						$scope.coordinatesString = getShapeCoordinatesAsWkt();
						break;
					case "geojson":
						$scope.coordinatesString = getShapeCoordinatesAsGeoJson();
						break;
					}
				}
			}
			
			var parseWkt = function() {
				var tempString = $scope.coordinatesString.toLowerCase();
				
				if (tempString.indexOf("multipolygon(((") > -1 || tempString.indexOf("multipolygon (((") > -1) { 
				    tempString = tempString.replace("multipolygon", "");
				    tempString = tempString.substring(1);
				} else if (tempString.indexOf("polygon((") > -1 || tempString.indexOf("polygon ((") > -1) {
					tempString = tempString.replace("polygon", "");
				} else {
					$scope.parsingError = { msgKey: "wkt.invalidgeometrytype" };
					return null;
				}
				
				tempString = tempString.replace(/^\s+|\s+$/g, '');
				
				var multipolygon = [];				
			    var level = "multipolygon";
			    var i = 0;
			    var j = 0;

			    while (tempString.length > 0) {
			    	if (tempString[0] == "(") {
			    		tempString = tempString.substring(1);
			    		if (level == "multipolygon") {
			    			multipolygon[i] = [];
			    			level = "polygon";
			    		} else if (level == "polygon") {
			    			level = "path";
			    		} 
			    	} else if (tempString[0] == ")") {
			    		tempString = tempString.substring(1);
			    		if (level == "multipolygon") {
			    			break;
			    		} else if (level == "polygon") {
			    			level = "multipolygon";
			    			i++;
			    			j = 0;
			    		} else if (level == "path") {
			    			level = "polygon";
			    			j++;
			    		}
			    	} else if (tempString[0] == "," || tempString[0] == " ") {
			    		tempString = tempString.substring(1);
			    	} else {
			    		var index = tempString.indexOf(")");
			    		if (index == -1) {
			    			$scope.parsingError = { msgKey: "wkt.missingbracket" };
			    			return null;
			    		}
			    		var substring = tempString.substring(0, index);
			    		tempString = tempString.substring(index);
			    		var points = substring.split(",");
			    		var pointsArray = [];
			    		for (var k in points) {
			    			points[k] = points[k].replace(/^\s+|\s+$/g, '');
			    			var pointArray = points[k].split(' ');
			    			var floatArray = [];
			    			for (var l in pointArray) {
			    				var floatCoordinate = parseFloat(pointArray[l]);
			    				if (floatCoordinate == NaN) {
			    					$scope.parsingError = { msgKey: "wkt.notanumber", data: pointArray[l] };
			    					return null;
			    				}
			    				floatArray.push(floatCoordinate);
			    			}
			    			pointsArray.push(floatArray);
			    		}
			    		multipolygon[i][j] = pointsArray;
			    	}
			    }

			    if (multipolygon.length > 0 && multipolygon[0].length > 0 && multipolygon[0][0].length > 0) {
			    	return multipolygon;
			    } else {
			    	$scope.parsingError = { msgKey: "wkt.genericparsingerror" };
			    	return null;
			    }	
			};
			
			var parseGeoJson = function() {
				var geoJson;
				try {
					geoJson = JSON.parse($scope.coordinatesString);
				} catch (err) {
					$scope.parsingError = { msgKey: "geojson.invalidjson" };
					return null;
				}	
				
				if (!geoJson.type || geoJson.type != "Feature") {
					$scope.parsingError = { msgKey: "geojson.invalidtype" };
					return null;
				}
				
				if (!geoJson.geometry) {
					$scope.parsingError = { msgKey: "geojson.nogeometry" };
					return null;
				}
				
				var geometry;
				if (geoJson.geometry.type == "GeometryCollection") {
					for (var i in geoJson.geometry.geometries) {
						if (geoJson.geometry.geometries[i].type == "Polygon"
								|| geoJson.geometry.geometries[i].type == "MultiPolygon") {
							geometry = geoJson.geometry.geometries[i];
							break;
						}
					}
				} else if (geoJson.geometry.type == "Polygon" || geoJson.geometry.type == "MultiPolygon") {
					geometry = geoJson.geometry;
				} else {
					$scope.parsingError = { msgKey: "geojson.invalidgeometrytype", data: geoJson.geometry.type };
					return null;
				}
				
				if (!geometry) {
					$scope.parsingError = { msgKey: "geojson.nogeometry" };
					return null;
				}
				
				var multipolygon;
				if (geometry.type == "Polygon") {
					multipolygon = [ geometry.coordinates ];
				} else {
					multipolygon = geometry.coordinates;
				}
				
				if (multipolygon.length > 0 && multipolygon[0].length > 0 && multipolygon[0][0].length > 0) {
			    	return multipolygon;
			    } else {
			    	$scope.parsingError = { msgKey: "geojson.emptycoordinates" };
			    	return null;
			    }
			};
			
			var getShapeCoordinatesAsWkt = function() {
				var wkt = "MULTIPOLYGON(";
				
				for (var i = 0; i < $scope.shape.length; i++) {
					wkt += "(";
					
					for (var j = 0; j < $scope.shape[i].length; j++) {
						wkt += "(";
						
						for (var k = 0; k < $scope.shape[i][j].length; k++) {
							if (k > 0) wkt += ",";
							wkt += $scope.shape[i][j][k][0] + " " + $scope.shape[i][j][k][1];
						}
						
						wkt += ")";
						if (j < $scope.shape[i].length - 1) {
							wkt += ",";
						}
					}
					
					wkt += ")";
					if (i < $scope.shape.length - 1) {
						wkt += ",";
					}
				}
				
				wkt += ")";
				
				return wkt;
			}
			
			var getShapeCoordinatesAsGeoJson = function() {
				var geoJson = {
					"type": "Feature",
					"geometry": {
					"type": "MultiPolygon",
					"coordinates": $scope.shape
					}
				};
				
				return JSON.stringify(geoJson);
			};
		}
	};
});

directives.directive('gazMap', function($location, Place) {
	
	var getMarkerSVG = function(type) {
		return `<svg version="1.1" class="${type}" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" viewBox="0 0 365 560" xml:space="preserve"><g><path d="M182.9,551.7c0,0.1,0.2,0.3,0.2,0.3S358.3,283,358.3,194.6c0-130.1-88.8-186.7-175.4-186.9   C96.3,7.9,7.5,64.5,7.5,194.6c0,88.4,175.3,357.4,175.3,357.4S182.9,551.7,182.9,551.7z M122.2,187.2c0-33.6,27.2-60.8,60.8-60.8   c33.6,0,60.8,27.2,60.8,60.8S216.5,248,182.9,248C149.4,248,122.2,220.8,122.2,187.2z"/></g></svg>`;
	};

	var highlightIcon = "//www.google.com/intl/en_us/mapfiles/ms/micons/yellow-dot.png";
	//var defaultIcon = "//www.google.com/intl/en_us/mapfiles/ms/micons/red-dot.png";
	var childIcon = "//www.google.com/intl/en_us/mapfiles/ms/micons/green-dot.png";
	
	var defaultIcon =  L.divIcon({
		iconSize: [12, 18],
		iconAnchor: [6, 18],
	    html: getMarkerSVG("standardMarker")   
    })
 
	var childIcon =  L.divIcon({
		iconSize: [12, 18],
		iconAnchor: [6, 18],
	    html: getMarkerSVG("childMarker")   
    })
	
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
			
			var map = L.map('leaflet_map').fitWorld();
			
			L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
			    maxZoom: 19,
			    attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
			}).addTo(map); 
			 
			var markersAndShapeLayer = L.featureGroup([]).addTo(map);
			

			/*
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
			
			*/
			$scope.markerClick = function(e) {
				$location.path(`/show/${e.sourceTarget.options.gazId}`);
			}; 
			
			$scope.markerOver = function(id, marker) {
				console.log("TODO: markerOver");
				/*
				var index;
				var type;
				var number = marker.number;
				if ((index = id.indexOf("*")) > -1) {
					id = id.substring(0, index);
					type = "searchResult";
				}
				else if ((index = id.indexOf("+")) > -1) {
					id = id.substring(0, index);
					type = "alternativeLocation";
					number--;
				} else
					type = "prefLocation";
				$scope.highlight = { id: id, type: type, index: number, noCenter: true };
				*/
			};
			
			/*
			
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
				
				if ($scope.highlight != null && ($scope.highlight.type == "polygon" || $scope.highlight.type == "searchResult")) {
					var id = $scope.highlight.id;
					if ($scope.highlight.index > -1)
						id += "#" + $scope.highlight.index;					
					if ($scope.shapeMap[id]) {
						$scope.highlightedShape = $scope.shapeMap[id];
						var fillOpacity = $scope.highlightedShape.get("fillOpacity") * 2;
						var strokeOpacity = $scope.highlightedShape.get("strokeOpacity") * 2;
						$scope.highlightedShape.setOptions({ fillOpacity: fillOpacity, strokeOpacity: strokeOpacity });
						if ($scope.highlight.type == "polygon") {
							var bounds = new google.maps.LatLngBounds();
							bounds.extend($scope.highlightedShape.getBounds().getSouthWest());
							bounds.extend($scope.highlightedShape.getBounds().getNorthEast());
							$scope.map.fitBounds(bounds);
						}
					}	
				}
				
				if ($scope.highlightedMarker != null) {
					if ($scope.highlightedMarkerType == "prefLocation")
						$scope.highlightedMarker.setIcon(defaultIcon);
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
					else if ($scope.highlight.type == "searchResult")
						id += "*" + $scope.highlight.index;
					if ($scope.markerMap[id]) {
						if ($scope.highlight.type == "alternativeLocation") {
							$scope.highlightedMarker = $scope.markerMap[id];
							$scope.highlightedMarkerType = "alternative";
							$scope.highlightedMarker.number = $scope.highlight.index + 1;
							$scope.markerMap[id].setIcon(getNumberedMarkerIcon($scope.highlight.index + 1, "yellow"));
						}
						else if ($scope.highlight.type == "searchResult") {
							$scope.highlightedMarker = $scope.markerMap[id];
							$scope.highlightedMarkerType = "searchResult";
							$scope.highlightedMarker.setIcon(getNumberedMarkerIcon($scope.highlight.index, "yellow"));
						}
						else if ($scope.highlight.type == "prefLocation") {
							$scope.highlightedMarker = $scope.markerMap[id];
							$scope.highlightedMarkerType = "prefLocation";
							$scope.markerMap[id].setIcon(highlightIcon);
						}
						if ($scope.highlightedMarker) {
							$scope.lastZIndex = $scope.highlightedMarker.getZIndex();
							$scope.highlightedMarker.setZIndex(1000);
							if (($scope.highlight.type == "prefLocation" || $scope.highlight.type == "alternativeLocation") && !$scope.highlight.noCenter)
								$scope.map.setCenter($scope.highlightedMarker.position);
						}
					}
				}
			});*/
			
			// add markers/shapes for locations and auto zoom and center map
			$scope.$watch("places", function() {

				//$scope.markerMap = {};
				//$scope.shapeMap = {};
//				for (var i in $scope.markers)
//					$scope.markers[i].setMap(null);
				//$scope.markers = [];

//				for (var i in $scope.shapes)
//					$scope.shapes[i].setMap(null);
				//$scope.shapes = [];
				markersAndShapeLayer.clearLayers();

				//if ($scope.places.length == 0) return;

				for (var i in $scope.places) {
					var place = $scope.places[i];
					
					var title = "";
					if (place.prefName && place.prefName.title)
						title = place.prefName.title;

					if (place.prefLocation) {
						 if (place.prefLocation.coordinates && place.mapType != "polygonParent" && place.mapType != "mainPolygon"
								&& (place.prefLocation.shape == null || place.mapType == "polygonAndMarker")) {
							var marker = L.marker(
								[place.prefLocation.coordinates[1], place.prefLocation.coordinates[0]],
								{icon: defaultIcon, gazId: place.gazId}
								).on('click', $scope.markerClick)
								//color: #FD7567;
							//marker.bindTooltip(`${place.markerNumber}`, {permanent: true, direction: 'center'})
							markersAndShapeLayer.addLayer(marker);
						}
						
						if (place.prefLocation.shape) {
							var shape = place.prefLocation.shape;
							var shapeCoordinates = [];
							var counter = 0;
							
							var shapeCoordinates = []
							for (var j = 0; j < shape.length; j++) {
								for (var k = 0; k < shape[j].length; k++) {
									var shapePolygonCoordinates = [];
									for (var l = 0; l < shape[j][k].length; l++)
										shapePolygonCoordinates[l] = L.latLng(shape[j][k][l][1], shape[j][k][l][0]);
									shapeCoordinates[counter] = shapePolygonCoordinates;
									counter++;
								}
							}

							var polygon = L.polygon(shapeCoordinates, {fillOpacity: 0.1,  gazId: place.gazId}).on('click', $scope.markerClick);
							markersAndShapeLayer.addLayer(polygon);
						}
					}
				}
			
				if(markersAndShapeLayer.getLayers().length > 0) {
					map.fitBounds(markersAndShapeLayer.getBounds(), {maxZoom: 6});
				} else {
					map.fitWorld()
				}				
			});
							/* 
							var icon = defaultIcon;
							if (place.mapType == "searchResults")
								icon = getNumberedMarkerIcon(parseInt(place.markerNumber), "red");
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
									title: title,
									map: $scope.map,
									icon: icon,
									number: place.markerNumber,
									mapType: place.mapType
								});
								if (place.mapType == "markerChild") {
									marker.setZIndex(4);
									childMarkerLocations.push(ll);
								} else if (place.mapType == "standard")
									marker.setZIndex(3);
								else
									marker.setZIndex(2);
								$scope.markers.push(marker);
								if (place.mapType == "searchResults")
									$scope.markerMap[place.gazId + "*" + place.markerNumber] = marker;
								else
									$scope.markerMap[place.gazId] = marker;
								bounds.extend(ll);
								numLocations++;
							}
						}
						var fillOpacity = 0.35;
						var strokeOpacity = 0.7;
						if (place.mapType == "polygonParent") {
							fillOpacity = 0.15;
							strokeOpacity = 0.25;
						}
						/* 
						if ($scope.mode == "singlePlace" && place.locations != null && place.locations.length > 0) {
							for (var i in place.locations) {
								/* if (place.locations[i].coordinates && place.mapType == "standard") {
									if (angular.isNumber(place.locations[i].coordinates[0]) && angular.isNumber(place.locations[i].coordinates[1])) {
										ll = new google.maps.LatLng(place.locations[i].coordinates[1], place.locations[i].coordinates[0]);
										var marker = new google.maps.Marker({
											position: ll,
											title: title,
											map: $scope.map,
											icon: getNumberedMarkerIcon(parseInt(i) + 1, "lightred"),
											number: parseInt(i) + 1,
											mapType: place.mapType,
											zIndex: 2
										});
										$scope.markers.push(marker);
										$scope.markerMap[place.gazId + "+" + i] = marker;
										bounds.extend(ll);
										numLocations++;
									}
								}
								if (place.locations[i].shape) {
									var polygon = L.polygon(place.locations[i].shape).addTo(map);
									/*
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
						if (place.prefLocation.shape) {
							
									var polygon = L.polygon(place.prefLocation.shape).addTo(map);
									/*
							var shapeCoordinates = convertShapeCoordinates(place.prefLocation.shape);
							
							var tempShape = new google.maps.Polygon({
								paths: shapeCoordinates,
								strokeColor: "#000000",
								strokeOpacity: strokeOpacity,
								strokeWeight: 1,
								fillColor: "#000000",
								fillOpacity: fillOpacity,
								placeId: place.gazId
							});
							
							if (place.mapType != "markerChildInvisible") {
								shape = tempShape;
								shape.setMap($scope.map);
								$scope.shapes.push(shape);
								$scope.shapeMap[place.gazId] = shape;
								addPolygonListener(shape);

								bounds.extend(shape.getBounds().getSouthWest());
								bounds.extend(shape.getBounds().getNorthEast());
							}
							
							if (place.mapType == "markerChild" || place.mapType == "markerChildInvisible"
								|| place.mapType == "standard") {
								childMarkerLocations.push(tempShape.getBounds().getSouthWest());
								childMarkerLocations.push(tempShape.getBounds().getNorthEast());
							}
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
				
			});*/
			/*
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
			};*/
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

directives.directive('markdownTextEditor', function($timeout) {
    return {
        restrict: 'A',
        scope: {
            markdownText: '=',
            placeholder: '@'
        },
        templateUrl: 'partials/markdownTextEditor.html',
        link: function(scope, element) {
            var textField = angular.element(element.children()[0]).children()[1].children[0];

            if (!scope.markdownText)
                scope.markdownText = "";

            scope.formatText = function(formatOption) {
            	if (document.selection != undefined) {
                    textField.focus();
                    scope.selectionRange = document.selection.createRange().duplicate();
                    scope.selectedText = scope.selectionRange.text;

                    var textFieldRange = textField.createTextRange();
                    var bookmark = scope.selectionRange.getBookmark();

                    textFieldRange.moveToBookmark(bookmark);
                } else if (textField.selectionStart != undefined) {
                    scope.selectedTextStart = textField.selectionStart;
                    scope.selectedTextEnd = textField.selectionEnd;
                    scope.selectedText = scope.markdownText.substring(scope.selectedTextStart, scope.selectedTextEnd);
                }

                scope.modifiedText = "";
                scope.additionalCharacters = 0;

                if (formatOption == "link") {
                    scope.link = { description: scope.selectedText };
                } else {
                    var blankSpaceSelection = false;

                    switch (formatOption) {
                        case "bold":
                            if (scope.selectedText == "") {
                                scope.selectedText = " ";
                                scope.additionalCharacters = 2;
                                blankSpaceSelection = true;
                            } else
                                scope.additionalCharacters = 4;
                            scope.modifiedText = "**" + scope.selectedText + "**";
                            break;

                        case "italic":
                            if (scope.selectedText == "") {
                                scope.selectedText = " ";
                                scope.additionalCharacters = 1;
                                blankSpaceSelection = true;
                            } else
                                scope.additionalCharacters = 2;
                                ;
                            scope.modifiedText = "*" + scope.selectedText + "*";       
                            break;

                        case "heading1":
                            scope.modifiedText = "# " + scope.selectedText;
                            scope.additionalCharacters = 2;
                            break;

                        case "heading2":
                            scope.modifiedText = "## " + scope.selectedText;
                            scope.additionalCharacters = 3;
                            break;

                        case "heading3":
                            scope.modifiedText = "### " + scope.selectedText;
                            scope.additionalCharacters = 4;
                            break;

                        case "listBullets":
                            var lines = scope.selectedText.split("\n");
                            for (var i = 0; i < lines.length; i++) {
                                scope.modifiedText += "* " + lines[i];
                                if (i != lines.length - 1)
                                    scope.modifiedText += "\n"
                                scope.additionalCharacters += 2;
                            }
                            break;

                        case "listNumbers":
                            var lines = scope.selectedText.split("\n");
                            for (var i = 0; i < lines.length; i++) {
                                scope.modifiedText += (i + 1).toString() + ". " + lines[i];
                                if (i != lines.length - 1)
                                    scope.modifiedText += "\n"
                                if (i < 9)
                                    scope.additionalCharacters += 3;
                                else
                                    scope.additionalCharacters += 4;
                            }
                            break;
                    }

                    updateSelection(blankSpaceSelection);
                }            
                
                textField.focus();

            };

            var updateSelection = function(blankSpaceSelection) { 
                if (scope.selectionRange) {
                    $timeout(function() {
                        scope.selectionRange.text = scope.modifiedText;
                        scope.selectionRange.collapse(true);
                        if (blankSpaceSelection)
                            scope.selectionRange.moveEnd('character', scope.selectedTextEnd + scope.additionalCharacters + 1);
                        else
                            scope.selectionRange.moveEnd('character', scope.selectedTextEnd + scope.additionalCharacters);
                        scope.selectionRange.moveStart('character', scope.selectedTextEnd + scope.additionalCharacters);
                        scope.selectionRange.select();
                    });
                } else {
                    scope.markdownText = scope.markdownText.substring(0, scope.selectedTextStart) + scope.modifiedText + scope.markdownText.substring(scope.selectedTextEnd);
                    $timeout(function() {
                        if (blankSpaceSelection)
                            textField.setSelectionRange(scope.selectedTextEnd + scope.additionalCharacters, scope.selectedTextEnd + scope.additionalCharacters + 1);
                        else
                            textField.setSelectionRange(scope.selectedTextEnd + scope.additionalCharacters, scope.selectedTextEnd + scope.additionalCharacters);
                    });
                }
            };
            
            scope.createLink = function(type) {
            	if (type == "image")
            		scope.modifiedText = "!";
            	else
            		scope.modifiedText = "";
            	
            	if (type == "link" || type == "image")
            		scope.modifiedText += "[" + scope.link.description + "](" + scope.link.url + ")";
            	else
            		scope.modifiedText += "%YOUTUBE=" + scope.link.url + "%!";
            	 
                 scope.additionalCharacters = (scope.link.description.length - scope.selectedText.length) + scope.link.url.length + 4;
                 updateSelection(scope.modifiedText, scope.selectionRange, scope.selectedTextStart, scope.selectedTextEnd, scope.additionalCharacters, false);
            };
        }
    }
});

directives.directive('tableOfContents', function($timeout) {
    return {
        restrict:'A',
        require:'?ngModel',
        link: function(scope, element, attrs, ngModel) {
        	var timeoutPromise;
        	
            var updateHeadlines = function() {
            	if (!scope.headlines || scope.headlines.length == 0) {  
	                scope.headlines = [];
	                angular.forEach(element[0].querySelectorAll('h1,h2'), function(e) {
	                    scope.headlines.push({ 
	                        level: e.tagName[1], 
	                        label: angular.element(e).text(),
	                        element: e
	                    });
	                });
	                if (timeoutPromise) $timeout.cancel(timeoutPromise);
	                timeoutPromise = $timeout(updateHeadlines, 100);
            	}
            };

            scope.$on('$destroy',function() {
                scope.headlines = [];
            });
            
            scope.scrollTo = function(headline) {
            	window.scrollTo(0, 0);
            	window.scrollTo(0, headline.element.getBoundingClientRect().top - 40);
            }

            ngModel.$render = updateHeadlines;
            updateHeadlines();
        }
    }
});