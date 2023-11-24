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
			
			$scope.saveCoordinates = function() {
				$scope.coordinates = [+$scope.marker.getPosition().lng().toFixed(6), +$scope.marker.getPosition().lat().toFixed(6)];
				$scope.closeOverlay();
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

directives.directive('gazMap', function($location, Place) {
	
	var getMarkerSVG = function(type) {
		return `<svg version="1.1" class="${type}" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" x="0px" y="0px" viewBox="0 0 365 560" xml:space="preserve"><g><path d="M182.9,551.7c0,0.1,0.2,0.3,0.2,0.3S358.3,283,358.3,194.6c0-130.1-88.8-186.7-175.4-186.9   C96.3,7.9,7.5,64.5,7.5,194.6c0,88.4,175.3,357.4,175.3,357.4S182.9,551.7,182.9,551.7z M122.2,187.2c0-33.6,27.2-60.8,60.8-60.8   c33.6,0,60.8,27.2,60.8,60.8S216.5,248,182.9,248C149.4,248,122.2,220.8,122.2,187.2z"/></g></svg>`;
	};


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
			    minZoom: 2,
			    attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
			}).addTo(map); 

			var markersAndShapeLayer = L.featureGroup([]).addTo(map);

			$scope.markerClick = function(e) {
				$location.path(`/show/${e.sourceTarget.options.gazId}`);
			}; 
			
			$scope.$watch("highlight", function() {
				var layers = markersAndShapeLayer.getLayers();
				for (var i in layers){
					if($scope.highlight !== null && layers[i].options.gazId === $scope.highlight.id) {
						// Highlight shape or icon.
						if(layers[i]._icon){
							layers[i]._icon.children[0].classList.add("highlight") 
						} else {
							layers[i]._path.classList.add("highlight") 
						}
					} else {
						// Remove highlight from every other shape or icon.
						if(layers[i]._icon){
							layers[i]._icon.children[0].classList.remove("highlight") 
						} else {
							layers[i]._path.classList.remove("highlight") 
						}
					}
				}
			});
			
			// add markers/shapes for locations and auto zoom and center map
			$scope.$watch("places", function() {
				markersAndShapeLayer.clearLayers();

				for (var i in $scope.places) {
					var place = $scope.places[i];

					if (place.prefLocation) {
						 if (place.prefLocation.coordinates && place.mapType != "polygonParent" && place.mapType != "mainPolygon"
								&& (place.prefLocation.shape == null || place.mapType == "polygonAndMarker") && place.mapType !== "markerChildInvisible") {
							var icon;
							
							if (place.mapType === "markerChild") {
								icon = childIcon
							} else {
								icon = defaultIcon;
							}
							
							var marker = L.marker(
								[place.prefLocation.coordinates[1], place.prefLocation.coordinates[0]],
								{icon: icon, gazId: place.gazId}
								)

							markersAndShapeLayer.addLayer(marker);
						}
						
						if (place.prefLocation.shape && place.mapType !== "markerChildInvisible") {
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
							
							var className = "gazShape";
							if(place["mapType"] == "markerChild") 
								className = className + " highlight";
							else if(place["mapType"] == "polygonParent")
								className = className + " parent";
							

							var polygon = L.polygon(shapeCoordinates, {gazId: place.gazId, className: className}).on('click', $scope.markerClick);
							markersAndShapeLayer.addLayer(polygon);
						}
					}
				}
			
				if(markersAndShapeLayer.getLayers().length > 0) {
					map.fitBounds(markersAndShapeLayer.getBounds());
				} else {
					map.fitWorld()
					map.setZoom(2)
				}
			});
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