'use strict';

function AppCtrl($scope, $location, $rootScope) {
	
	$scope.q = null;
	$scope.type = "";
	$rootScope.title = "";
	$rootScope.subtitle = "";
	
	$rootScope.activePlaces = [];
	
	$rootScope.alerts = [];
	
	$rootScope.loading = 0;
	
	$rootScope.bbox = [];
	$rootScope.zoom = 2;
	$scope.highlight = null;
	
	// search while typing
	$scope.$watch("q", function() {
		if ($scope.q != null && $scope.q.indexOf(':') == -1 && $scope.q.indexOf('*') == -1) {
			$scope.zoom = 2;
			$location.path('/search').search({q:$scope.q, type: "prefix"});
		}
	});
	
	$scope.submit = function() {
		$scope.zoom = 2;
		$location.path('/search').search({q:$scope.q, type: $scope.type});
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
	
	$scope.setHighlight = function(id) {
		$scope.highlight = id;
	};
	
}

function ExtendedSearchCtrl($scope, $rootScope, $location, messages) {
	
	$rootScope.title = messages["ui.extendedSearch"];
	$rootScope.subtitle = "";
	$rootScope.showMap = false;
	$rootScope.activePlaces = [];
	
	$scope.meta = null;
	$scope.type = "";
	$scope.names = { title: "", language: "" };
	$scope.parent = null;
	$scope.type = "";
	$scope.ids = { value: "", context: ""};
	$scope.fuzzy = false;
	$scope.hasCoordinates = false;
	
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
			queries.push({ match: { "parent": $scope.parent	} });
		}
		
		// type
		if ($scope.type !== "") {
			queries.push({ match: { "type": $scope.type	} });
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
		
		// hasCoordinates
		if ($scope.hasCoordinates) {
			queries.push({
				query_string: { query: "_exists_:prefLocation.coordinates" }
			});
		}
		
		var query = { "bool": { "must": queries } };
		
		$location.path('/search').search({q:angular.toJson(query), type: "extended"});
		
	};
	
}

function SearchCtrl($scope, $rootScope, $location, $routeParams, Place, messages) {
	
	$rootScope.title = messages["ui.search.results"];
	$rootScope.subtitle = "";
	$rootScope.showMap = true;
	
	setSearchFromLocation();
	
	$scope.places = [];
	$scope.parents = {};
	$scope.total = 0;
	$scope.zoom = 2;
	$scope.facets = null;
	
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
	
	$scope.$watch(("places"), function() {
		$rootScope.activePlaces = $scope.places;
	});
	
	$scope.page = function() {
		return $scope.search.offset / $scope.search.limit + 1;
	};
	
	$scope.totalPages = function() {
		return ($scope.total - ($scope.total % $scope.search.limit)) / $scope.search.limit + 1;
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
		Place.query($scope.search, function(result) {
			$scope.parents = {};
			$scope.places = result.result;
			if ($scope.search.type != 'prefix')
				$scope.facets = result.facets;
			else
				$scope.facets = null;
			if ($scope.total != result.total)
				$scope.total = result.total;
			$rootScope.loading--;
			for (var i=0; i < $scope.places.length; i++) {
				$rootScope.loading++;
				if ($scope.places[i].parent && !$scope.parents[$scope.places[i].parent]) {
					var parentId = getIdFromUri($scope.places[i].parent);					
					$scope.parents[$scope.places[i].parent] = Place.get({id:parentId});
				}
				$rootScope.loading--;
			}
		}, function() {
			if($scope.search.type !== "prefix")
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
			q: ($location.search().q) ? ($location.search().q) : ""
		};
		if ($location.search().fq) $scope.search.fq = $location.search().fq;
		if ($location.search().type) $scope.search.type = $location.search().type;
		if ($location.search().sort) $scope.search.sort = $location.search().sort;
		if ($location.search().order) $scope.search.order = $location.search().order;
		if ($location.search().bbox) $scope.search.bbox = $location.search().bbox;
		if ($location.search().showInReview) $scope.search.showInReview = $location.search().showInReview;
	}

}


function PlaceCtrl($scope, $rootScope, $routeParams, $location, Place, messages) {
	
	$scope.location = { confidence: 0 };
	$scope.link = { predicate: "owl:sameAs" };
	$rootScope.showMap = true;
	
	$rootScope.title = "";
	$rootScope.subtitle = "";
	
	if ($routeParams.id) {
		$rootScope.loading++;
		$scope.place = Place.get({
			id: $routeParams.id
		}, function(result) {
			if (result.deleted) {
				$rootScope.addAlert(messages["ui.place.deleted"], null, "error");
			}
			if (result.parent) {
				$rootScope.loading++;
				var parentId = getIdFromUri(result.parent);
				$scope.parent = Place.get({id:parentId});
				$rootScope.loading--;
			}
			$rootScope.loading++;
			Place.query({q: "relatedPlaces:" + $scope.place.gazId}, function(result) {
				$scope.relatedPlaces = result.result;
				$rootScope.loading--;
			}, function() {
				$rootScope.addAlert(messages["ui.contactAdmin"], messages["ui.error"], "error");
				$rootScope.loading--;
			});
			$rootScope.loading++;
			Place.query({q: "parent:" + $scope.place.gazId, sort:"prefName.title.sort"}, function(result) {
				$scope.totalChildren = result.total;
				$scope.children = result.result;
				$scope.offsetChildren = 0;
				$rootScope.loading--;
			}, function() {
				$rootScope.addAlert(messages["ui.contactAdmin"], messages["ui.error"], "error");
				$rootScope.loading--;
			});
			$rootScope.activePlaces = [ result ];
			$rootScope.loading--;
		}, function() {
			$rootScope.addAlert(messages["ui.contactAdmin"], messages["ui.error"], "error");
			$rootScope.loading--;
		});
	} else
		$scope.place = {};
	
	// show live changes of title
	$scope.$watch("place.prefName.title", function() {
		if ($scope.place && $scope.place.prefName)
			$rootScope.title = $scope.place.prefName.title;
	});
	
	// show live changes of location
	$scope.$watch("place.prefLocation.coordinates", function() {
		if ($scope.place && $scope.place.prefLocation)
			$rootScope.activePlaces = [$scope.place];
	});
	
	// show live changes of id
	$scope.$watch("place.gazId", function() {
		if ($scope.place && $scope.place["@id"])
			$rootScope.subtitle = $scope.place["@id"]	+ '<a data-toggle="modal" href="#copyUriModal"><i class="icon-share" style="font-size:0.7em"></i></a>';
	});
	
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
	
	$scope.save = function() {
		$rootScope.loading++;
		if($scope.comment) $scope.addComment();
		if($scope.name) $scope.addName();
		if($scope.location) $scope.addLocation();
		if($scope.identifier) $scope.addIdentifier();
		if($scope.link) $scope.addLink();
		if($scope.relatedPlace) $scope.addRelatedPlace();
		if($scope.commentReisestipendium) $scope.addCommentReisestipendium();
		Place.save(
			$scope.place,
			function(data) {
				if (data.gazId) {
					$scope.place.gazId = data.gazId;
				}
				window.scrollTo(0,0);
				$rootScope.loading--;
				$location.search("keepAlerts").path("show/" + $scope.place.gazId);
				$rootScope.addAlert(messages["ui.place.save.success"], null, "success");
			},
			function(result) {
				$scope.failure = result.data.message; 
				$rootScope.addAlert(messages["ui.place.save.failure"], null, "error");
				window.scrollTo(0,0);
				$rootScope.loading--;
			}
		);
	};
	
	$scope.addComment = function() {
		if (!$scope.comment.text || !$scope.comment.language) return;
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
		if (!$scope.location.coordinates) return;
		if ($scope.place.locations == undefined)
			$scope.place.locations = [];
		$scope.place.locations.push($scope.location);
		$scope.location = { confidence: 0 };
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
		$scope.link = { predicate: "owl:sameAs" };
	};
	
	$scope.addRelatedPlace = function() {
		if (!$scope.relatedPlace['@id']) return;
		var relatedPlaces = $scope.relatedPlaces;
		if (relatedPlaces == undefined)
			relatedPlaces = [];
		relatedPlaces.push($scope.relatedPlace);
		$scope.relatedPlace = { "@id" : null };
		$scope.relatedPlaces = relatedPlaces;
	};
	
	$scope.addCommentReisestipendium = function() {
		if (!$scope.commentReisestipendium.text) return;
		if ($scope.place.commentsReisestipendium == undefined)
			$scope.place.commentsReisestipendium = [];
		$scope.place.commentsReisestipendium.push($scope.commentReisestipendium);
		$scope.commentReisestipendium = {};
	};
	
	$scope.getIdsByContext = function(context) {
		var result = [];
		var ids = $scope.place.identifiers;
		for (var i in ids)
			if (ids[i].context == context)
				result.push(ids[i].value);
		return result;
	};
	
	// update relatedPlaces attribute of place when relatedPlaces in scope changes
	$scope.$watch("relatedPlaces.length", function() {
		if ($scope.place == undefined) return;
		$scope.place.relatedPlaces = [];
		for (var i in $scope.relatedPlaces)
			$scope.place.relatedPlaces.push($scope.relatedPlaces[i]["@id"]);
	});

}

function MergeCtrl($scope, $rootScope, $routeParams, $location, Place, messages) {
	
	if ($routeParams.id) {
		$rootScope.loading++;
		$scope.place = Place.get({
			id: $routeParams.id
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
		var query = "(\"" + $scope.place.prefName.title + "\"~0.5";
		for(var i in $scope.place.names) {
			query += " OR \"" + $scope.place.names[i].title + "\"~0.5";
		}
		query += ") AND NOT _id:" + $scope.place.gazId;
		$rootScope.loading++;
		Place.query({q: query, type: 'queryString'}, function(result) {
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
			lon: $scope.place.prefLocation.coordinates[0],
			lat: $scope.place.prefLocation.coordinates[1],
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
		if ($scope.candidatePlaces)
			activePlaces = activePlaces.concat($scope.candidatePlaces);
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

function ThesaurusCtrl($scope, $rootScope, $location, Place, messages) {
	
	$rootScope.title = messages["ui.thesaurus"];
	$rootScope.subtitle = "";
	$rootScope.showMap = true;
	
	$rootScope.loading++;
	Place.query({
		sort: 'prefName.title.sort',
		limit: 10000,
		q: 'type:continent'
	}, function(result) {
		$rootScope.loading--;
		$scope.places = result.result;
	}, function() {
		$rootScope.addAlert(messages["ui.contactAdmin"], messages["ui.error"], "error");
		$rootScope.loading--;
	});
	
	$scope.open = function(place) {
		place.isOpen = true;
		Place.query({
			sort: 'prefName.title.sort',
			limit: 10000,
			q: 'parent:' + place.gazId
		}, function(result) {
			place.children = result.result;
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
		if (place.prefLocation) {
			$rootScope.activePlaces = [ place ];
			$rootScope.zoom = 6;
		}
	};
	
	$scope.hideMarker = function() {
		$rootScope.activePlaces = [];
	};
	
}