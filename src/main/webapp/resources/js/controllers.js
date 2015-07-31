'use strict';

function AppCtrl($scope, $location, $rootScope, $timeout, Place, GeoSearch, EscapingService, MapTypeService) {
	
	$scope.q = null;
	$scope.type = "";
	$rootScope.pageTitle = "iDAI.gazetteer";
	$rootScope.title = "";
	$rootScope.subtitle = "";
	
	$rootScope.activePlaces = [];
	
	$rootScope.alerts = [];
	
	$rootScope.loading = 0;
	
	$rootScope.isFocused = false;
	
	$rootScope.bbox = [];
	$rootScope.zoom = 2;
	$rootScope.mapMode = "standard";
	
	$scope.highlight = null;
	
	$scope.searchSuggestions = [];
	$scope.selectedSuggestionIndex = -1;
	
	$scope.scrollPosition = 0;
	
	$scope.$watch("q", function() {				
		$scope.updateSuggestions();	
	});
	
	$scope.$watch("showMap", function() {
		MapTypeService.addMap($rootScope.map);
		
		if ($rootScope.showMap) {
			$scope.mapContainerStyle = {};
			window.setTimeout(function() { google.maps.event.trigger($rootScope.map, 'resize'); }, 20);
		}
		else
			$scope.mapContainerStyle = { 'position': 'absolute', 'left': '-10000px' };
	});
	
	$scope.$watch("geoSearch", function() {
		if ($rootScope.geoSearch)
			GeoSearch.activate($rootScope.map);
		else
			GeoSearch.deactivate();
	});
	
	$scope.setUpdateMapPropertiesTimer = function() {
		$timeout($scope.updateMapProperties, 200);
	};
	
	$scope.updateMapProperties = function() {
		MapTypeService.setMapTypeId($rootScope.map.getMapTypeId());
	};
	
	$scope.updateSuggestions = function() {
		$scope.searchSuggestions = [];
		$scope.selectedSuggestionIndex = -1;
		
		if ($scope.q && $scope.q.length > 0) {
			Place.suggestions({ field: "prefName.title.suggest", text: $scope.q }, function(result) {
				$scope.searchSuggestions = result.suggestions;

				Place.suggestions({ field: "names.title.suggest", text: $scope.q }, function(result) {
					for (var newSuggestion in result.suggestions) {
						var sameAsPrefName = false;
						for (var prefNameSuggestion in $scope.searchSuggestions) {
							if ($scope.searchSuggestions[prefNameSuggestion].trim() == result.suggestions[newSuggestion].trim()) {
								sameAsPrefName = true;
								break;
							}
						}
						if (!sameAsPrefName && $scope.searchSuggestions.length < 7)
							$scope.searchSuggestions.push(result.suggestions[newSuggestion]);
					}
				});
			});
		}
	};
	
	$scope.selectPreviousSuggestion = function() {
		if ($scope.searchSuggestions.length > 0) {
			$scope.selectedSuggestionIndex -= 1;
			if ($scope.selectedSuggestionIndex < 0)
				$scope.selectedSuggestionIndex = $scope.searchSuggestions.length - 1;
		}
	};
	
	$scope.selectNextSuggestion = function() {
		if ($scope.searchSuggestions.length > 0) {
			$scope.selectedSuggestionIndex += 1;
			if ($scope.selectedSuggestionIndex >= $scope.searchSuggestions.length)
				$scope.selectedSuggestionIndex = 0;
		}
	};
	
	$scope.setSelectedSuggestionIndex = function(index) {
		$scope.selectedSuggestionIndex = index;
	};
	
	$scope.lostFocus = function() {
		$scope.searchSuggestions = [];
		$rootScope.isFocused = false;
	};
	
	$scope.submit = function() {
		if ($scope.selectedSuggestionIndex != -1)
			$scope.q = $scope.searchSuggestions[$scope.selectedSuggestionIndex];
		
		$scope.zoom = 2;
		$location.path('/search').search({q: EscapingService.escape($scope.q), type: $scope.type});
		$scope.q = null;
	};
	
	$rootScope.addAlert = function(body, head, type) {
		var alert = { body: body };
		if (head != null) alert.head = head;
		if (type != null) alert.alertClass = "alert-" + type;
		$rootScope.alerts.push(alert);
	};
	
	// remove alerts if location changes
	$scope.$watch(function(){ return $location.absUrl(); }, function() {
		if ($location.search().keepAlerts) return;
		$rootScope.alerts = [];
	});
	
	$scope.setHighlight = function(id, type, index) {
		if (id == null)
			$scope.highlight = null;
		else
			$scope.highlight = { id: id, type: type, index: index };
	};
}

function HomeCtrl($scope, $location, $rootScope, Place, EscapingService) {
	$scope.q = null;
	$scope.type = "";
	$rootScope.showMap = false;
	$rootScope.showHeader = false;
	$rootScope.showNavbarSearch = false;
	$rootScope.viewClass = "";
	$rootScope.pageTitle = "iDAI.gazetteer";
	$rootScope.title = "";
	$rootScope.subtitle = "";
	$rootScope.isFocused = false;
	$rootScope.geoSearch = false;
	
	$scope.homeSearchSuggestions = [];
	$scope.selectedSuggestionIndex = -1;
	
	var map_canvas = document.getElementById('home_map_canvas');
	
	$rootScope.loading++;
	Place.heatmapCoordinates({}, function(result) {
		$scope.homeMap = new google.maps.Map(map_canvas, {
			center: new google.maps.LatLng(20,0),
			zoom: 2,
			disableDefaultUI: true,
			mapTypeId: google.maps.MapTypeId.ROADMAP,
			zoomControlOptions: {
				style: google.maps.ZoomControlStyle.SMALL
			},
			styles: [
	        	{
	       	    "featureType": "administrative",
	       	    "stylers": [
	       	      { "visibility": "off" }
	       	    ]
	       	  },{
	       	    "featureType": "landscape",
	       	    "stylers": [
	       	      { "visibility": "simplified" },
	       	      { "saturation": -100 },
	       	      { "lightness": -31 }
	       	    ]
	       	  },{
	       	    "featureType": "water",
	       	    "stylers": [
	       	      { "saturation": -100 },
	       	      { "lightness": 32 }
	       	    ]
	       	  },{
	       	    "featureType": "road",
	       	    "stylers": [
	       	      { "visibility": "off" }
	       	    ]
	       	  },{
	       	    "featureType": "poi",
	       	    "stylers": [
	       	      { "visibility": "off" }
	       	    ]
	       	  },{
	       	    "elementType": "labels"  }
	       	]
		});
		
		var coordinates = result.coordinates;
		var heatmapData = [];
		for (var i = 0; i < coordinates.length - 1; i+= 2) {
			heatmapData.push(new google.maps.LatLng(coordinates[i], coordinates[i+1]));		
		}
		
		var heatmap = new google.maps.visualization.HeatmapLayer({
	        data: heatmapData,
	        opacity: 0.8,
	        maxIntensity: 10,
	        radius: 3,
			gradient: ['transparent', '#5283d2', '#ffffff']
	   	});
		heatmap.setMap($scope.homeMap);
		$rootScope.loading--;
	});
	
	$scope.$watch("searchFieldInput", function() {
		$scope.updateSuggestions();		
		$scope.selectedSuggestionIndex = -1;
	});
	
	$scope.updateSuggestions = function() {
		$scope.homeSearchSuggestions = [];
		
		if ($scope.searchFieldInput && $scope.searchFieldInput.length > 0) {
			Place.suggestions({ field: "prefName.title.suggest", text: $scope.searchFieldInput }, function(result) {
				$scope.homeSearchSuggestions = result.suggestions;

				Place.suggestions({ field: "names.title.suggest", text: $scope.searchFieldInput }, function(result) {
					for (var newSuggestion in result.suggestions) {
						var sameAsPrefName = false;
						for (var prefNameSuggestion in $scope.homeSearchSuggestions) {
							if ($scope.homeSearchSuggestions[prefNameSuggestion].trim() == result.suggestions[newSuggestion].trim()) {
								sameAsPrefName = true;
								break;
							}
						} 
						if (!sameAsPrefName && $scope.homeSearchSuggestions.length < 7)
							$scope.homeSearchSuggestions.push(result.suggestions[newSuggestion]);
					}
				});
			});
		}
	};
	
	$scope.selectPreviousSuggestion = function() {
		if ($scope.homeSearchSuggestions.length > 0) {
			$scope.selectedSuggestionIndex -= 1;
			if ($scope.selectedSuggestionIndex < 0)
				$scope.selectedSuggestionIndex = $scope.homeSearchSuggestions.length - 1;
		}
	};
	
	$scope.selectNextSuggestion = function() {
		if ($scope.homeSearchSuggestions.length > 0) {
			$scope.selectedSuggestionIndex += 1;
			if ($scope.selectedSuggestionIndex >= $scope.homeSearchSuggestions.length)
				$scope.selectedSuggestionIndex = 0;
		}
	};
	
	$scope.setSelectedSuggestionIndex = function(index) {
		$scope.selectedSuggestionIndex = index;
	};
	
	$scope.lostFocus = function() {
		$scope.homeSearchSuggestions = [];
	};
	
	$scope.submit = function() {
		if ($scope.selectedSuggestionIndex != -1)
			$scope.searchFieldInput = $scope.homeSearchSuggestions[$scope.selectedSuggestionIndex];
		
		$scope.zoom = 2;
		$location.path('/search').search({q: EscapingService.escape($scope.searchFieldInput), type: $scope.type});
		$scope.searchFieldInput = null;
	};
}

function ExtendedSearchCtrl($scope, $rootScope, $location, messages, PolygonValidator, GeoSearch) {
	
	$rootScope.pageTitle = messages["ui.extendedSearch"] + " | iDAI.gazetteer";
	$rootScope.title = messages["ui.extendedSearch"];
	$rootScope.subtitle = "";
	$rootScope.showMap = true;
	$rootScope.showHeader = true;
	$rootScope.showNavbarSearch = true;
	$rootScope.viewClass = "span7";
	$rootScope.activePlaces = [];	
	$rootScope.isFocused = false;
	$rootScope.geoSearch = true;
	$rootScope.mapMode = "standard";
	
	GeoSearch.setCreateMode(true);
	
	$scope.reset = function() {
		$scope.meta = null;
		$scope.type = "";
		$scope.names = { title: "", language: "" };
		$scope.parent = null;
		$scope.grandchildrenSearch = false;
		$scope.type = "";
		$scope.tags = [];
		$scope.provenance = [];
		$scope.ids = { value: "", context: ""};
		$scope.fuzzy = false;
		$scope.filters = {
				coordinatesFilter : false,
				noCoordinatesFilter : false,
				polygonFilter : false,
				noPolygonFilter : false
		};
		GeoSearch.deletePolygon();
	};
	
	$scope.reset();

	$scope.submit = function() {
		
		var queries = [];		
		
		// all fields
		if ($scope.meta !== null) {
			if ($scope.fuzzy)
				queries.push({ fuzzy: { "_all": $scope.meta } });
			else
				queries.push({ match: { "_all": $scope.meta } });
		}
		
		// names
		if ($scope.names.title !== "" && $scope.names.language !== "") {
			queries.push({ 
				bool: { 
					should: [
						{ 
							nested: {
								path: "names",
								query: {
									bool: {
										must: [
										   { match: { "names.title": { query: $scope.names.title, operator: "and" } } },
										   { match: { "names.language": $scope.names.language } }
										]
									}
								}
							}
						},
						{
							bool: {
								must: [
								   { match: { "prefName.title": { query: $scope.names.title, operator: "and" } } },
								   { match: { "prefName.language": $scope.names.language } }
								]
							}
						}
					]
				}
			});
		} else if ($scope.names.title !== "") {
			queries.push({
				bool: {
					should: [
					    { nested: { path: "names", query: { match: { "names.title": { query: $scope.names.title, operator: "and" } } } } },
					    { match: { "prefName.title": { query: $scope.names.title, operator: "and" } } }
					]
				}
			});
		} else if ($scope.names.language !== "") {
			queries.push({
				bool: {
					should: [
					    { nested: { path: "names", query: { match: { "names.language": $scope.names.language } } } },
					    { match: { "prefName.language": $scope.names.language } }
					]
				}
			});
		}
		
		// parent
		if ($scope.parent !== null) {
			if ($scope.grandchildrenSearch) {
				queries.push({
					bool: {
						should: [
						    { match: { "parent": $scope.parent.gazId } },
						    { match: { "grandparents": $scope.parent.gazId } }
						]
					}
				});
			}
			else
				queries.push({ match: { "parent": $scope.parent.gazId } });
		}
		
		// type
		if ($scope.type !== "" && $scope.type != "noType") {
			queries.push({ match: { "types": $scope.type } });
		}
		
		// tags
		if ($scope.tags != []) {
			for (var i = 0; i < $scope.tags.length; i++) {
				queries.push({ match: { "tags": $scope.tags[i] } });
			}
		}
		
		// provenance
		if ($scope.provenance != []) {
			for (var i = 0; i < $scope.provenance.length; i++) {
				queries.push({ match: { "provenance": $scope.provenance[i] } });
			}
		}
		
		// ids
		if ($scope.ids.value !== "" && $scope.ids.context !== "") {
			queries.push({
				nested: {
					path: "ids",
					query: {
						bool: {
							must: [
							   { match: { "ids.value": { query: $scope.ids.value, operator: "and" } } },
							   { match: { "ids.context": $scope.ids.context } }
							]
						}
					}
				}
			});
		} else if ($scope.ids.value !== "") {
			queries.push({
				nested: { path: "ids", query: { match: { "ids.value": $scope.ids.value } } }
			});
		} else if ($scope.ids.context !== "" ) {
			queries.push({
				nested: { path: "ids", query: { match: { "ids.context": $scope.ids.context } } }
			});
		}
		
		// filters
		var filterQuery = "";
		if ($scope.filters.coordinates)
			filterQuery += "_exists_:prefLocation.coordinates";
		else if ($scope.filters.noCoordinates)
			filterQuery += "_missing_:prefLocation.coordinates";
		if ($scope.filters.polygon) {
			if (filterQuery != "")
				filterQuery += " AND ";
			filterQuery += "_exists_:prefLocation.shape";
		}
		else if ($scope.filters.noPolygon) {
			if (filterQuery != "")
				filterQuery += " AND ";
			filterQuery += "_missing_:prefLocation.shape";
		}
		if ($scope.filters.noTags) {
			if (filterQuery != "")
				filterQuery += " AND ";
			filterQuery += "_missing_:tags";
		}
		if ($scope.filters.noProvenance) {
			if (filterQuery != "")
				filterQuery += " AND ";
			filterQuery += "_missing_:provenance";
		}		
		if ($scope.type == "noType") {
			if (filterQuery != "")
				filterQuery += " AND ";
			filterQuery += "_missing_:types";
		}
		
		var query = { "bool": { "must": queries } };

		var geoSearchCoordinates = [];
		if (GeoSearch.getPolygon() != null && GeoSearch.getPolygon().getMap() != null) {
			for (var i = 0; i < GeoSearch.getPolygon().getPath().getLength(); i++) {
				geoSearchCoordinates[i * 2] = GeoSearch.getPolygon().getPath().getAt(i).lng();
				geoSearchCoordinates[i * 2 + 1] = GeoSearch.getPolygon().getPath().getAt(i).lat();
			}
		}
		
		$location.path('/search').search({q:angular.toJson(query), polygonFilterCoordinates: geoSearchCoordinates, fq: filterQuery, type: "extended"});
		
	};
}

function SearchCtrl($scope, $rootScope, $location, $routeParams, Place, GeoSearch, messages) {
	
	$rootScope.pageTitle = messages["ui.search.results"] + " | iDAI.gazetteer";
	$rootScope.title = messages["ui.search.results"];
	$rootScope.subtitle = "";
	$rootScope.showMap = true;
	$rootScope.showHeader = true;
	$rootScope.showNavbarSearch = true;
	$rootScope.viewClass = "span7";
	$rootScope.isFocused = true;
	$rootScope.mapMode = "standard";
	
	$scope.filters = {
			coordinates : false,
			noCoordinates : false,
			polygon : false,
			noPolygon : false
	};
	
	setSearchFromLocation();
	
	$scope.places = [];
	$scope.total = 0;
	$scope.zoom = 2;
	$scope.facets = null;
	$scope.facetOffsets = {};
	
	GeoSearch.setCreateMode(false);
	
	if (GeoSearch.getPolygon() != null) {
		google.maps.event.addListener(GeoSearch.getPolygon().getPaths().getAt(0), 'insert_at', function() {
			$scope.search.offset = 0;
			$scope.submit();
		});
	
		google.maps.event.addListener(GeoSearch.getPolygon().getPaths().getAt(0), 'set_at', function() {
			$scope.search.offset = 0;
			$scope.submit();
		});
	}
	
	$scope.$on("$destroy", function() {
		if (GeoSearch.getPolygon() != null) {
			google.maps.event.clearListeners(GeoSearch.getPolygon().getPaths().getAt(0), "insert_at");
			google.maps.event.clearListeners(GeoSearch.getPolygon().getPaths().getAt(0), "set_at");
		}
	});
		
	// search while zooming
	$scope.$watch(function() { return $scope.bbox.join(","); }, function() {
		if ($scope.bbox.length == 4) {
			$scope.search.bbox = $scope.bbox.join(",");
			$location.search($scope.search);
		}
	});
	
	$scope.$watch("total", function() {
		$rootScope.subtitle = $scope.total + " " + messages["ui.search.hits"];
	});
	
	$scope.$watch("places", function() {
		for (var i in $scope.places)
			$scope.places[i].mapType = "searchResults";
		$rootScope.activePlaces = $scope.places;
	});
	
	$scope.page = function() {
		return $scope.search.offset / $scope.search.limit + 1;
	};
	
	$scope.totalPages = function() {
		var totalPages = ($scope.total - ($scope.total % $scope.search.limit)) / $scope.search.limit;
		if ($scope.total % $scope.search.limit > 0 || totalPages == 0)
			totalPages += 1;
		return totalPages;
	};
	
	$scope.setLimit = function(limit) {
		$scope.search.limit = limit;
		$scope.search.offset = 0;
		$location.search($scope.search);
	};
	
	$scope.prevPage = function() {
		if ($scope.page() > 1) {
			$scope.search.offset = $scope.search.offset - $scope.search.limit;
			$location.search($scope.search);
		}
	};
	
	$scope.nextPage = function() {
		if ($scope.page() < $scope.totalPages()) {
			$scope.search.offset = $scope.search.offset + $scope.search.limit;
			$location.search($scope.search);
		}
	};
	
	$scope.prevFacetEntries = function(facetName) {
		$scope.facetOffsets[facetName] -= 5;
		if ($scope.facetOffsets[facetName] < 0)
			$scope.facetOffsets[facetName] = 0;
	};
	
	$scope.nextFacetEntries = function(facetName) {
		if ($scope.facetOffsets[facetName] < $scope.facets[facetName].length - 5)
			$scope.facetOffsets[facetName] += 5;
	};
	
	$scope.orderBy = function(sort) {
		if ($scope.search.sort == sort) {
			$scope.search.order = ($scope.search.order == "asc") ? "desc" : "asc";
		} else {
			$scope.search.sort = sort;
			$scope.search.order = "asc";
		}
		$scope.search.offset = 0;
		$location.search($scope.search);
	};
	
	$scope.submit = function() {
		$rootScope.loading++;
		
		$scope.updateFilters();
		$scope.updatePolygonFilterCoordinates();
		Place.query($scope.search, function(result) {
			$scope.places = result.result;
			var markers = 0;
			for (var i in $scope.places) {
				if ($scope.places[i].prefLocation) {
					markers++;
					$scope.places[i].markerNumber = markers;
				}
			}
			$scope.facets = result.facets;
			for (var i in $scope.facets) {
				$scope.facetOffsets[i] = 0;
			}
			$rootScope.loading--;
			if ($scope.total != result.total)
				$scope.total = result.total;
		}, function() {
			$rootScope.addAlert(messages["ui.contactAdmin"], messages["ui.error"], "error");
			$rootScope.loading--;
		});
	};
	
	$scope.setFacet = function(facetName, term) {
		if ($scope.search.type == "extended") {
			var query = angular.fromJson($scope.search.q);
			var match = { match: {} };
			match.match[facetName] = term;
			query.bool.must.push(match);
			$scope.search.q = angular.toJson(query);
		} else {
			if ($scope.search.fq) $scope.search.fq += " AND " + facetName + ":" + term;
			else $scope.search.fq = facetName + ":" + term;
		}
		$scope.search.offset = 0;
		$location.search($scope.search);
	};
	
	$scope.$watch("filters.coordinates", function() { $scope.changeFilters(); });
	
	$scope.$watch("filters.noCoordinates", function() { $scope.changeFilters(); });
	
	$scope.$watch("filters.polygon", function() { $scope.changeFilters(); });
	
	$scope.$watch("filters.noPolygon", function() { $scope.changeFilters(); });
	
	$scope.changeFilters = function() {
		$scope.search.offset = 0;
		$scope.updateFilters();
		$location.search($scope.search);
	};
	
	$scope.updateFilters = function() {
		var filterQuery = "";
		if ($scope.filters.coordinates)
			filterQuery += "_exists_:prefLocation.coordinates";
		else if ($scope.filters.noCoordinates)
			filterQuery += "_missing_:prefLocation.coordinates";
		if ($scope.filters.polygon) {
			if (filterQuery != "")
				filterQuery += " AND ";
			filterQuery += "_exists_:prefLocation.shape";
		}
		else if ($scope.filters.noPolygon) {
			if (filterQuery != "")
				filterQuery += " AND ";
			filterQuery += "_missing_:prefLocation.shape";
		}
		
		if ($scope.search.fq) {
			$scope.search.fq = $scope.search.fq.replace(" AND _exists_:prefLocation.coordinates", "");
			$scope.search.fq = $scope.search.fq.replace("_exists_:prefLocation.coordinates", "");
			$scope.search.fq = $scope.search.fq.replace(" AND _missing_:prefLocation.coordinates", "");
			$scope.search.fq = $scope.search.fq.replace("_missing_:prefLocation.coordinates", "");
			$scope.search.fq = $scope.search.fq.replace(" AND _exists_:prefLocation.shape", "");
			$scope.search.fq = $scope.search.fq.replace("_exists_:prefLocation.shape", "");
			$scope.search.fq = $scope.search.fq.replace(" AND _missing_:prefLocation.shape", "");
			$scope.search.fq = $scope.search.fq.replace("_missing_:prefLocation.shape", "");
				
			if ($scope.search.fq.slice(0, 5) == " AND ")
				$scope.search.fq = $scope.search.fq.slice(4);
			
			$scope.search.fq = $scope.search.fq.trim();
			
			if ($scope.search.fq != "" && filterQuery != "")
				$scope.search.fq += " AND ";
			
			$scope.search.fq += filterQuery;
		}
		else
			$scope.search.fq = filterQuery;
	};
	
	$scope.updatePolygonFilterCoordinates = function() {
		var polygonFilterCoordinates = [];
		if (GeoSearch.getPolygon() != null && GeoSearch.getPolygon().getMap() != null) {
			for (var i = 0; i < GeoSearch.getPolygon().getPath().getLength(); i++) {
				polygonFilterCoordinates[i * 2] = GeoSearch.getPolygon().getPath().getAt(i).lng();
				polygonFilterCoordinates[i * 2 + 1] = GeoSearch.getPolygon().getPath().getAt(i).lat();
			}
		}
		
		$scope.search.polygonFilterCoordinates = polygonFilterCoordinates;
		updatePolygonFilterCoordinatesString();
	};
	
	// needed to keep $scope.search and $location.search() in sync
	// because reloadOnSearch is turned off for this controller
	$scope.$watch(
		function(){ 
			return $location.absUrl();
		},
		function() {
			if ($location.path() == "/search") {
				setSearchFromLocation();
				$scope.submit();
			}
		}
	);
	
	function setSearchFromLocation() {
		$scope.search = {
			offset: ($location.search().offset) ? parseInt($location.search().offset) : 0,
			limit: ($location.search().limit) ? parseInt($location.search().limit) : 10,
			q: ($location.search().q) ? ($location.search().q) : "",
			add: "parents,access,history"
		};
		if ($location.search().fq) $scope.search.fq = $location.search().fq;
		if ($location.search().type) $scope.search.type = $location.search().type;
		if ($location.search().sort) $scope.search.sort = $location.search().sort;
		if ($location.search().order) $scope.search.order = $location.search().order;
		if ($location.search().bbox) $scope.search.bbox = $location.search().bbox;
		if ($location.search().polygonFilterCoordinates) $scope.search.polygonFilterCoordinates = $location.search().polygonFilterCoordinates;
		if ($location.search().showInReview) $scope.search.showInReview = $location.search().showInReview;
		
		updatePolygonFilterCoordinatesString();
		
		$scope.filters.coordinatesFilter = false;
		$scope.filters.noCoordinatesFilter = false;
		$scope.filters.polygonFilter = false;
		$scope.filters.noPolygonFilter = false;
		
		if ($scope.search.fq) {
			if ($scope.search.fq.indexOf("_exists_:prefLocation.coordinates") > -1)
				$scope.filters.coordinates = true;
			else if ($scope.search.fq.indexOf("_missing_:prefLocation.coordinates") > -1)
				$scope.filters.noCoordinates = true;
			if ($scope.search.fq.indexOf("_exists_:prefLocation.shape") > -1)
				$scope.filters.polygon = true;
			else if ($scope.search.fq.indexOf("_missing_:prefLocation.shape") > -1)
				$scope.filters.noPolygon = true;
		}
		
		if ($scope.search.polygonFilterCoordinates)
			$rootScope.geoSearch = true;
		else {
			$rootScope.geoSearch = false;
			GeoSearch.deactivate();
		}
	};
	
	function updatePolygonFilterCoordinatesString() {
		$scope.polygonFilterCoordinatesString = "";
		if ($scope.search.polygonFilterCoordinates && $scope.search.polygonFilterCoordinates.length > 0) {
			for (var i in $scope.search.polygonFilterCoordinates) {
				$scope.polygonFilterCoordinatesString += "&polygonFilterCoordinates=";
				$scope.polygonFilterCoordinatesString += $scope.search.polygonFilterCoordinates[i];
			}
		}
	};
}

function CreateCtrl($scope, $rootScope, $routeParams, $location, Place, messages) {
	
	$scope.place = { prefLocation: { publicSite: true }, recordGroup: {} };
	$rootScope.pageTitle = messages["ui.create"] + " | iDAI.gazetteer";
	$rootScope.title = messages["ui.create"];
	$rootScope.subtitle = "";
	$rootScope.showMap = true;
	$rootScope.showHeader = true;
	$rootScope.showNavbarSearch = true;
	$rootScope.viewClass = "span7";
	$rootScope.activePlaces = [];
	$rootScope.isFocused = true;
	$rootScope.geoSearch = false;
	$rootScope.mapMode = "standard";
	
	$scope.addPlaceType = function(placeType) {
		var pos = $scope.getListPos($scope.place.types, placeType);
		
		if (pos == -1) {
			if (!$scope.place.types)
				$scope.place.types = [];
			$scope.place.types.push(placeType);
		}
		else
			$scope.place.types.splice(pos, 1);
	};
	
	$scope.getListPos = function(list, placeType) {				
		if (!list)
			return -1;			
		
	    for (var i = 0; i < list.length; i++) {
	        if (list[i] == placeType)
	            return i;
	    }
	    
	    return -1;
	};
	
	$scope.hasType = function(placeType) {		
		if (!$scope.place || !$scope.place.types)
			return false;
		
	   if ($scope.getListPos($scope.place.types, placeType) != -1)
		   return true;
	   else
		   return false;
	};
	
	$scope.save = function() {
		$rootScope.loading++;
		
		if ($scope.place.types && "archaeological-site" in $scope.place.types) {
			$scope.place.prefLocation.publicSite = false;
		}
		
		if ($scope.place.recordGroup == {})
			$scope.place.recordGroup = undefined;
		
		Place.save(
			$scope.place,
			function(data) {
				if (data.message == null) {
					if (data.gazId) {
						$scope.place.gazId = data.gazId;
					}
					window.scrollTo(0,0);
					$rootScope.loading--;
					$location.path("edit/" + $scope.place.gazId); 
					$rootScope.addAlert(messages["ui.place.save.success"], null, "success");
				} else {
					$scope.failure = data.message;
					$rootScope.addAlert(messages["ui.place.save.failure." + $scope.failure], null, "error");
					window.scrollTo(0,0); 
					$rootScope.loading--;
				}
				
			},
			function(result) {
				$scope.failure = result.data.message;
				$rootScope.addAlert(messages["ui.place.save.failure"], null, "error");
				window.scrollTo(0,0);
				$rootScope.loading--;
			}
		);
	};
}


function PlaceCtrl($scope, $rootScope, $routeParams, $location, $timeout, Place, messages) {
	
	$scope.location = { confidence: 0, publicSite: true, coordinates: [] };
	$scope.link = { predicate: "owl:sameAs", description: "" };
	$rootScope.showMap = true;
	$rootScope.showHeader = true;
	$rootScope.showNavbarSearch = true;
	$rootScope.viewClass = "span7";
	$rootScope.isFocused = true;
	$rootScope.geoSearch = false;
	$rootScope.mapMode = "singlePlace";
	
	$rootScope.pageTitle = "iDAI.gazetteer";
	$rootScope.title = "";
	$rootScope.subtitle = "";
	
	$scope.namesDisplayed = 4;

	if ($routeParams.id) {
		$rootScope.loading++;
		$scope.place = Place.get({
			id: $routeParams.id,
			add: "parents,access,history"
		}, function(result) {
			if (result.deleted) {
				$rootScope.addAlert(messages["ui.place.deleted"], null, "error");
			}
			if ($scope.place.accessDenied) {
				$rootScope.title = messages["ui.place.hiddenPlace"];
				$rootScope.pageTitle = messages["ui.place.hiddenPlace"] + " | iDAI.gazetteer";
			}
			if (!$scope.place.prefLocation) {
				if ($scope.hasType("archaeological-site"))
					$scope.place.prefLocation = { confidence: 0, publicSite: false, coordinates: [] };
				else
					$scope.place.prefLocation = { confidence: 0, publicSite: true, coordinates: [] };
			} 
			if ($scope.hasType("archaeological-site"))
				$scope.location.publicSite = false;
			if (result.parent) {
				$rootScope.loading++;
				Place.get({id:getIdFromUri(result.parent)}, function(result) {
					$scope.parent = result;
				});
				$rootScope.loading--;
			}
			$rootScope.loading++;
			Place.query({q: "relatedPlaces:" + $scope.place.gazId, sort:"prefName.title.sort", showHiddenPlaces: true }, function(result) {
				$scope.totalRelatedPlaces = result.total;
				$scope.relatedPlaces = result.result;
				$scope.offsetRelatedPlaces = 0;
				$rootScope.loading--;
			}, function() {
				$rootScope.addAlert(messages["ui.contactAdmin"], messages["ui.error"], "error");
				$rootScope.loading--;
			});
			$rootScope.loading++;
			Place.query({q: "relatedPlaces:" + $scope.place.gazId, sort:"prefName.title.sort", limit: "1000", showHiddenPlaces: true }, function(result) {
				$scope.allRelatedPlaces = result.result;
				$rootScope.loading--;
			}, function() {
				$rootScope.addAlert(messages["ui.contactAdmin"], messages["ui.error"], "error");
				$rootScope.loading--;
			});
			$rootScope.loading++;
			Place.query({q: "parent:" + $scope.place.gazId, sort:"prefName.title.sort", showHiddenPlaces: true}, function(result) {
				$scope.totalChildren = result.total;
				$scope.children = result.result;
				$scope.offsetChildren = 0;
				$rootScope.loading--;
			}, function() {
				$rootScope.addAlert(messages["ui.contactAdmin"], messages["ui.error"], "error");
				$rootScope.loading--;
			});
			$scope.place.mapType = "standard";
			$rootScope.loading--;
		}, function(response) {
			if (response.status == 403) {
				$scope.place = { gazId: $routeParams.id, accessDenied: true};
				$rootScope.title = messages["ui.place.hiddenPlace"];
				$rootScope.pageTitle = messages["ui.place.hiddenPlace"] + " | iDAI.gazetteer";
			} else {
				$rootScope.addAlert(messages["ui.contactAdmin"], messages["ui.error"], "error");
			}
			$rootScope.loading--;
		});
	} else {
		$scope.place = { prefLocation: { publicSite: true } };
		$scope.newPlace = true;
	}
	
	// show live changes of title
	$scope.$watch("place.prefName.title", function() {
		if ($scope.place && $scope.place.prefName) {
			$rootScope.title = $scope.place.prefName.title;
			$rootScope.pageTitle = $scope.place.prefName.title + " | iDAI.gazetteer";
		}
	});
	
	// show live changes of location coordinates
	$scope.$watch("place.prefLocation.coordinates", function() {
		if ($scope.place && $scope.place.prefLocation)
			$scope.updateMap();
	});
	
	// show live changes of location shape
	$scope.$watch("place.prefLocation.shape", function() {
		if ($scope.place && $scope.place.prefLocation)
			$scope.updateMap();
	});
	
	// show live changes of id
	$scope.$watch("place.gazId", function() {
		if ($scope.place && $scope.place["@id"])
			$rootScope.subtitle = $scope.place["@id"] + ' <a data-toggle="modal" href="#copyUriModal"><i class="icon-share" style="font-size:0.7em"></i></a>';
	});

	$scope.updateMap = function() {
		if (!$scope.place)
			return;

		var polygonPlace = null;
		var markerPlaces = [];
		var parentsWithCoordinates = [];
		for (var i in $scope.place.parents) {
			if ((!$scope.place.prefLocation || !$scope.place.prefLocation.shape) && $scope.place.parents[i].prefLocation
					&& $scope.place.parents[i].prefLocation.shape && !polygonPlace)
				polygonPlace = $scope.place.parents[i];
			if ($scope.place.parents[i].prefLocation && $scope.place.parents[i].prefLocation.coordinates
					&& $scope.place.parents[i].prefLocation.coordinates.length > 0) {
				$scope.place.parents[i].mapType = "markerParent";
				parentsWithCoordinates.push($scope.place.parents[i]);
				markerPlaces.push($scope.place.parents[i]);
			}
		}
		for (var i in parentsWithCoordinates) {
			parentsWithCoordinates[i].markerNumber = parentsWithCoordinates.length - i; 
		}
		if (!polygonPlace && markerPlaces.length == 0)
			$rootScope.activePlaces = [ $scope.place ];
		else if (polygonPlace && markerPlaces.length == 0) {
			polygonPlace.mapType = "polygonParent";
			$rootScope.activePlaces = [ $scope.place, polygonPlace ];
		} else if (!polygonPlace && markerPlaces.length > 0)
			$rootScope.activePlaces = [ $scope.place ].concat(markerPlaces);
		else {
			var index = markerPlaces.indexOf(polygonPlace);
			if (index > -1) {
				markerPlaces[index].mapType = "parent";
				$rootScope.activePlaces = [ $scope.place ].concat(markerPlaces);
			} else {
				polygonPlace.mapType = "polygonParent";
				$rootScope.activePlaces = [ $scope.place, polygonPlace ].concat(markerPlaces);
			}
		}
	};
	
	$scope.changeNumberOfDisplayedNames = function() {
		if ($scope.namesDisplayed == 4)
			$scope.namesDisplayed = 10000;
		else
			$scope.namesDisplayed = 4;
	};

	$scope.prevChildren = function() {
		$scope.offsetChildren -= 10;
		$rootScope.loading++;
		Place.query({q: "parent:" + $scope.place.gazId, sort:"prefName.title.sort", offset:$scope.offsetChildren }, function(result) {
			$scope.children = result.result;
			$rootScope.loading--;
		}, function() {
			$rootScope.addAlert(messages["ui.contactAdmin"], messages["ui.error"], "error");
			$rootScope.loading--;
		});
	};
	
	$scope.nextChildren = function() {
		$scope.offsetChildren += 10;
		$rootScope.loading++;
		Place.query({q: "parent:" + $scope.place.gazId, sort:"prefName.title.sort", offset:$scope.offsetChildren }, function(result) {
			$scope.children = result.result;
			$rootScope.loading--;
		}, function() {
			$rootScope.addAlert(messages["ui.contactAdmin"], messages["ui.error"], "error");
			$rootScope.loading--;
		});
	};
	
	$scope.prevRelatedPlaces = function() {
		$scope.offsetRelatedPlaces -= 10;
		$rootScope.loading++;
		Place.query({q: "relatedPlaces:" + $scope.place.gazId, sort:"prefName.title.sort", offset:$scope.offsetRelatedPlaces }, function(result) {
			$scope.relatedPlaces = result.result;
			$rootScope.loading--;
		}, function() {
			$rootScope.addAlert(messages["ui.contactAdmin"], messages["ui.error"], "error");
			$rootScope.loading--;
		});
	};
	
	$scope.nextRelatedPlaces = function() {
		$scope.offsetRelatedPlaces += 10;
		$rootScope.loading++;
		Place.query({q: "relatedPlaces:" + $scope.place.gazId, sort:"prefName.title.sort", offset:$scope.offsetRelatedPlaces }, function(result) {
			$scope.relatedPlaces = result.result;
			$rootScope.loading--;
		}, function() {
			$rootScope.addAlert(messages["ui.contactAdmin"], messages["ui.error"], "error");
			$rootScope.loading--;
		});
	};
	
	$scope.remove = function() {
		Place.remove({ id: $scope.place.gazId }, function(data) {
			window.scrollTo(0,0);
			$rootScope.loading--;
			$location.path("show/" + $scope.place.gazId);
		}, function(result) {
			$scope.failure = result.data.message; 
			$rootScope.addAlert(messages["ui.place.delete.failure"], null, "error");
			window.scrollTo(0,0);
			$rootScope.loading--;
		});
	};
	
	$scope.publish = function() {
		$scope.place.recordGroupId = "";
		$scope.save();
	};
	
	$scope.duplicate = function() {
		$rootScope.loading++;
		$scope.prepareSave();
		$scope.gazId = $scope.place.gazId;
		$scope.place.gazId = undefined;
		Place.duplicate(
			$scope.place,
			function(data) {
				if (data.message == null) {
					$scope.place = data;
					window.scrollTo(0,0);
					$rootScope.loading--;
					$location.search("keepAlerts").path("show/" + $scope.place.gazId);
					$rootScope.addAlert(messages["ui.place.duplicate.success"], null, "success");
				} else {
					$scope.place.gazId = $scope.gazId;
					$scope.gazId = undefined;
					$scope.failure = data.message;
					$rootScope.addAlert(messages["ui.place.duplicate.failure." + $scope.failure], null, "error");
					window.scrollTo(0,0);
					$rootScope.loading--;
				}
				
			},
			function(result) {
				$scope.place.gazId = $scope.gazId;
				$scope.gazId = undefined;
				$scope.failure = result.data.message;
				$rootScope.addAlert(messages["ui.place.duplicate.failure"], null, "error");
				window.scrollTo(0,0);
				$rootScope.loading--;
			}
		);
	};
	
	$scope.save = function() {
		$rootScope.loading++;
		$scope.prepareSave();

		Place.save(
			$scope.place,
			function(data) {
				if (data.message == null) {
					if (data.gazId) {
						$scope.place.gazId = data.gazId;
					}
					window.scrollTo(0,0);
					$rootScope.loading--;
					$location.search("keepAlerts").path("show/" + $scope.place.gazId);
					$rootScope.addAlert(messages["ui.place.save.success"], null, "success");
				} else {
					$scope.failure = data.message;
					$rootScope.addAlert(messages["ui.place.save.failure." + $scope.failure], null, "error");
					window.scrollTo(0,0);
					$rootScope.loading--;
				}
				
			},
			function(result) {
				$scope.failure = result.data.message;
				$rootScope.addAlert(messages["ui.place.save.failure"], null, "error");
				window.scrollTo(0,0);
				$rootScope.loading--;
			}
		);
	};
	
	$scope.prepareSave = function() {
		if($scope.place.prefLocation && !$scope.place.prefLocation.coordinates && !$scope.place.prefLocation.shape)
			$scope.place.prefLocation = undefined;
		if($scope.comment) $scope.addComment();
		if($scope.name) $scope.addName();
		if($scope.location) $scope.addLocation();
		if($scope.identifier) $scope.addIdentifier();
		if($scope.link) $scope.addLink();
		if($scope.relatedPlace)
			$scope.addRelatedPlace();
		$scope.updateRelatedPlaces();
		if($scope.commentReisestipendium) $scope.addCommentReisestipendium();
	};
	
	$scope.addComment = function() {
		if (!$scope.comment.text)
			return;
		if ($scope.place.comments == undefined)
			$scope.place.comments = [];
		$scope.place.comments.push($scope.comment);
		$scope.comment = {};
	};
	
	$scope.addName = function() {
		if (!$scope.name.title) return;
		if ($scope.place.names == undefined)
			$scope.place.names = [];
		$scope.place.names.push($scope.name);
		$scope.name = {};
	};
	
	$scope.addLocation = function() {
		if ($scope.location.shape && $scope.location.shape.coordinates && $scope.location.shape.coordinates.length == 0)
			$scope.location.shape = null;
		
		if ((!$scope.location.coordinates || $scope.location.coordinates.length == 0) && !$scope.location.shape) return;
		if ($scope.place.locations == undefined)
			$scope.place.locations = [];
		$scope.place.locations.push($scope.location);
		if ($scope.hasType("archaeological-site")) 
			$scope.location = { confidence: 0, publicSite: false, coordinates: [] };
		else
			$scope.location = { confidence: 0, publicSite: true, coordinates: [] };
	};
	
	$scope.addIdentifier = function() {
		if (!$scope.identifier.value || !$scope.identifier.context) return;
		if ($scope.place.identifiers == undefined)
			$scope.place.identifiers = [];
		$scope.place.identifiers.push($scope.identifier);
		$scope.identifier = { };
	};
	
	$scope.addLink = function() {
		if (!$scope.link.object || !$scope.link.predicate) return;
		if ($scope.place.links == undefined)
			$scope.place.links = [];
		$scope.place.links.push($scope.link);
		$scope.link = { predicate: "owl:sameAs", description: "" };
	};
	
	$scope.addRelatedPlace = function() {
		if (!$scope.relatedPlace['@id']) return;
		var relatedPlaces = $scope.allRelatedPlaces;
		if (relatedPlaces == undefined)
			relatedPlaces = [];
		relatedPlaces.push($scope.relatedPlace);
		$scope.relatedPlace = { "@id" : null };
		$scope.allRelatedPlaces = relatedPlaces;
	};
	
	$scope.addCommentReisestipendium = function() {
		if (!$scope.commentReisestipendium.text) return;
		if ($scope.place.commentsReisestipendium == undefined)
			$scope.place.commentsReisestipendium = [];
		$scope.place.commentsReisestipendium.push($scope.commentReisestipendium);
		$scope.commentReisestipendium = {};
	};
	
	$scope.addPlaceType = function(placeType) {
		var pos = $scope.getListPos($scope.place.types, placeType);
		
		if (pos == -1) {
			if (!$scope.place.types)
				$scope.place.types = [];
			$scope.place.types.push(placeType);
		}
		else
			$scope.place.types.splice(pos, 1);
	};
	
	$scope.getListPos = function(list, placeType) {				
		if (!list)
			return -1;			
		
	    for (var i = 0; i < list.length; i++) {
	        if (list[i] == placeType)
	            return i;
	    }
	    
	    return -1;
	};
	
	$scope.hasType = function(placeType) {		
		if (!$scope.place || !$scope.place.types)
			return false;
		
	   if ($scope.getListPos($scope.place.types, placeType) != -1)
		   return true;
	   else
		   return false;
	};
	
	$scope.getZenonSearchQuery = function() {
		var zenonIds = $scope.getIdsByContext("zenon-thesaurus");
		for (var i in zenonIds) {
			zenonIds[i] = "%22" + zenonIds[i] + "%22";
		}		
		return zenonIds.join(" OR ");
	};
	
	$scope.getIdsByContext = function(context) {
		var result = [];
		var ids = $scope.place.identifiers;
		for (var i in ids)
			if (ids[i].context == context)
				result.push(ids[i].value);
		return result;
	};
	
	$scope.updateRelatedPlaces = function() {
		if ($scope.place == undefined) return;
		$scope.place.relatedPlaces = [];
		for (var i in $scope.allRelatedPlaces)
			$scope.place.relatedPlaces.push($scope.allRelatedPlaces[i]["@id"]);
	};
	
	$scope.decodeUri = function(uri) {
		var decodedURI = decodeURI(uri); 
		return decodedURI.replace("%2C", ",");
	};

	$scope.setCopyCoordinates = function(coordinates) {
		$scope.copyCoordinates = coordinates;
	};
	
	var removeChildrenFromMap = function() {
		var places = $rootScope.activePlaces.slice();		
		for (var i in $scope.children) {
			var index = places.indexOf($scope.children[i]);
			if (index > -1)
				places.splice(index, 1);
		}
		$rootScope.activePlaces = places;
	};
	
	$scope.showChildMarker = function(child) {
		removeChildrenFromMap();
		var places = $rootScope.activePlaces;
		if ($scope.activeTimeout) {
			$timeout.cancel($scope.activeTimeout);
			$scope.activeTimeout = null;
		}
		for (var i in $scope.children) {
			if ($scope.children[i] != child)
				$scope.children[i].mapType = "markerChildInvisible";
			else
				$scope.children[i].mapType = "markerChild";
			if (places.indexOf($scope.children[i]) == -1)
				places = places.concat($scope.children[i]);
		}
		
		$rootScope.activePlaces = places;
	};
	
	$scope.hideChildMarker = function() {
		$scope.activeTimeout = $timeout(removeChildrenFromMap, 200);		
	};

	// update relatedPlaces attribute of place when relatedPlaces in scope changes
	$scope.$watch("allRelatedPlaces.length", $scope.updateRelatedPlaces());
};

function MergeCtrl($scope, $rootScope, $routeParams, $location, Place, EscapingService, messages) {
	
	if ($routeParams.id) {
		$rootScope.loading++;
		$scope.place = Place.get({
			id: $routeParams.id,
			add: "parents,access,history"
		}, function(result) {
			$rootScope.title = result.prefName.title,
			$rootScope.subtitle = result["@id"]	+ ' <a data-toggle="modal" href="#copyUriModal"><i class="icon-share" style="font-size:0.7em"></i></a>';
			$scope.getCandidatesByName();
			$rootScope.loading--;
		},
		function() {
			$rootScope.addAlert(messages["ui.contactAdmin"], messages["ui.error"], "error");
			$rootScope.loading--;
		});
	}
	
	$scope.getCandidatesByName = function() {
		var query = "(\"" + EscapingService.escape($scope.place.prefName.title) + "\"~0.5";
		for(var i in $scope.place.names) {
			query += " OR \"" + EscapingService.escape($scope.place.names[i].title) + "\"~0.5";
		}
		query += ") AND NOT _id:" + $scope.place.gazId;
		$rootScope.loading++;
		Place.query({q: query, type: 'queryString', add: 'access'}, function(result) {
			$scope.candidatePlaces = result.result;
			$scope.parents = {};
			for(var i in $scope.candidatePlaces) {
				if ($scope.candidatePlaces[i].parent && !$scope.parents[$scope.candidatePlaces[i].parent]) {
					var parentId = getIdFromUri($scope.candidatePlaces[i].parent);					
					$scope.parents[$scope.candidatePlaces[i].parent] = Place.get({id:parentId});
				}
			}
			$rootScope.loading--;
		}, function() {
			$rootScope.addAlert(messages["ui.contactAdmin"], messages["ui.error"], "error");
			$rootScope.loading--;
		});
	};
	
	$scope.getCandidatesByLocation = function() {
		$rootScope.loading++;
		Place.distance({
			lon: $scope.place.prefLocation.coordinates[1],
			lat: $scope.place.prefLocation.coordinates[0],
			distance: 50,
			filter: "NOT _id:" + $scope.place.gazId
		}, function(result) {
			$scope.candidatePlaces = result.result;
			$scope.parents = {};
			for(var i in $scope.candidatePlaces) {
				if ($scope.candidatePlaces[i].parent && !$scope.parents[$scope.candidatePlaces[i].parent]) {
					var parentId = getIdFromUri($scope.candidatePlaces[i].parent);					
					$scope.parents[$scope.candidatePlaces[i].parent] = Place.get({id:parentId});
				}
			}
			$rootScope.loading--;
		}, function() {
			$rootScope.addAlert(messages["ui.contactAdmin"], messages["ui.error"], "error");
			$rootScope.loading--;
		});
	};
	
	$scope.$watch("candidatePlaces", function() {
		var activePlaces = [];
		if ($scope.candidatePlaces) {
			for (var i in $scope.candidatePlaces)
				$scope.candidatePlaces[i].mapType = "standard";
			activePlaces = activePlaces.concat($scope.candidatePlaces);
		}
		$scope.place.mapType = "standard";
		activePlaces.push($scope.place);
		$rootScope.activePlaces = activePlaces;
	});
	
	$scope.link = function(place1, place2) {
		if (place1.relatedPlaces == undefined)
			place1.relatedPlaces = [];
		place1.relatedPlaces.push(place2["@id"]);
		$rootScope.loading++;
		Place.save(place1, function(result) {
			$rootScope.addAlert(messages["ui.place.save.success"], place1.prefName.title, "success");
			$rootScope.loading--;
		}, function(result) {
			$rootScope.addAlert(messages["ui.place.save.failure"], place1.prefName.title, "error");
			$rootScope.loading--;
		});
		if (place2.relatedPlaces == undefined)
			place2.relatedPlaces = [];
		place2.relatedPlaces.push(place1["@id"]);
		$rootScope.loading++;
		Place.save(place2, function(result) {
			$rootScope.addAlert(messages["ui.place.save.success"], place2.prefName.title, "success");
			$rootScope.loading--;
		}, function(result) {
			$rootScope.addAlert(messages["ui.place.save.failure"], place2.prefName.title, "error");
			$rootScope.loading--;
		});
	};
	
	$scope.merge = function(place1, place2) {
		$rootScope.loading++;
		place1.$merge({id2: place2.gazId }, function(result) {
			$rootScope.addAlert(messages["ui.merge.success.body"],
					messages["ui.merge.success.head"], "success");
			$location.path("/edit/" + result.gazId);
			$rootScope.loading--;
		}, function() {
			$rootScope.addAlert(messages["ui.place.save.failure"], place1.prefName.title, "error");
			$rootScope.loading--;
		});
	};
	
}

function ThesaurusCtrl($scope, $rootScope, $location, Place, messages, $route) {
	
	$rootScope.pageTitle = messages["ui.thesaurus"] + " | iDAI.gazetteer";
	$rootScope.title = messages["ui.thesaurus"];
	$rootScope.subtitle = "";
	$rootScope.showMap = true;
	$rootScope.showHeader = true;
	$rootScope.showNavbarSearch = true;
	$rootScope.viewClass = "span7";
	$rootScope.activePlaces = [];
	$rootScope.isFocused = true;
	$rootScope.geoSearch = false;
	$rootScope.mapMode = "standard";
	
	$rootScope.loading++;
	Place.query({
		sort: 'prefName.title.sort',
				limit: 10000,
				q: 'types:continent'
		}, function(result) {
			$rootScope.loading--;
			$scope.places = result.result;		
	}, function() {
		$rootScope.addAlert(messages["ui.contactAdmin"], messages["ui.error"], "error");
		$rootScope.loading--;
	});	
	
	$scope.open = function(place) {
		place.isOpen = true;
		Place.children({
			id: place.gazId
		}, function(result) {
			place.children = result.result;
			for (var i in place.children) {
				place.children[i].parentPlace = place;
			}
		}, function() {
			$rootScope.addAlert(messages["ui.contactAdmin"], messages["ui.error"], "error");
			$rootScope.loading--;
		});
	};
	
	$scope.close = function(place) {
		place.children = null;
		place.isOpen = false;
	};
	
	$scope.showMarker = function(place) {
		place.mapType = "standard";
		var polygonPlace = null;
		var markerPlaces = [];
		if (!place.prefLocation || !place.prefLocation.shape)
			polygonPlace = getPolygonPlace(place, false);
		markerPlaces = getMarkerPlaces(place, false);
		if (!polygonPlace && markerPlaces.length == 0)
			$rootScope.activePlaces = [ place ];
		else if (polygonPlace && markerPlaces.length == 0) {
			polygonPlace.mapType = "polygonParent";
			$rootScope.activePlaces = [ place, polygonPlace ];
		} else if (!polygonPlace && markerPlaces.length > 0)
			$rootScope.activePlaces = [ place ].concat(markerPlaces);
		else {
			var index = markerPlaces.indexOf(polygonPlace);
			if (index > -1) {
				markerPlaces[index].mapType = "parent";
				$rootScope.activePlaces = [ place ].concat(markerPlaces);
			} else {
				polygonPlace.mapType = "polygonParent";
				$rootScope.activePlaces = [ place, polygonPlace ].concat(markerPlaces);
			}
		}
		$rootScope.zoom = 6;
	};
	
	$scope.hideMarker = function() {
		$rootScope.activePlaces = [];
	};
	
	var getPolygonPlace = function(place, isParent) {
		if (isParent && place.prefLocation && place.prefLocation.shape)
			return place;
		else if (place.parentPlace)
			return getPolygonPlace(place.parentPlace, true);
		return null;
	};

	var getMarkerPlaces = function(place, isParent) {
		var markerPlaces = [];
		if (isParent && place.prefLocation && place.prefLocation.coordinates && place.prefLocation.coordinates.length > 0)  {
			place.mapType = "markerParent";
			markerPlaces.push(place);
		}
		if (place.parentPlace) {
			markerPlaces = markerPlaces.concat(getMarkerPlaces(place.parentPlace, true));
		}
		return markerPlaces;
	};
}