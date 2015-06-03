'use strict';

/* Services */

var services = angular.module('gazetteer.services', ['ngResource', 'ngCookies']);

services.factory('Place', function($resource){
	return $resource(
			"../:method/:id/:id2",
			{ id: '@gazId' },
			{
				query: { method:'GET', params: { method:'search' }, isArray:false },
				extendedQuery: { method:'POST', params: { method:'search' }, isArray:false },
				distance: { method:'GET', params: { method:'geoSearch'}, isArray:false },
				children: { method:'GET', params: { method:'children'}, isArray:false },
				suggestions: { method:'GET', params: { method:'suggestions'} },
				get: { method:'GET', params: { method:'doc'} },
				save: { method:'PUT', params: { method:'doc'} },
				remove: { method: 'DELETE', params: { method: 'doc'} },
				merge: { method: 'POST', params: { method:'merge' } },
				heatmapCoordinates: { method:'GET', params: { method:'heatmapCoordinates'} }
			});
});

services.factory('Thesaurus', function($resource){
	return $resource("../thesaurus/:id", { id: '@key' });
});

services.factory('PolygonValidator', function() {	
	var checkForLineIntersection = function(latlng1, latlng2, latlng3, latlng4)	{
	    var a1 = latlng2.lat() - latlng1.lat();
	    var b1 = latlng1.lng() - latlng2.lng();
	    var c1 = a1 * latlng1.lng() + b1 * latlng1.lat();
	    
	    var a2 = latlng4.lat() - latlng3.lat();
	    var b2 = latlng3.lng() - latlng4.lng();
	    var c2 = a2 * latlng3.lng() + b2 * latlng3.lat();

	    var determinate = a1 * b2 - a2 * b1;

	    var intersection;
	    if (determinate != 0) {
	        var x = (b2 * c1 - b1 * c2) / determinate;
	        var y = (a1 * c2 - a2 * c1) / determinate;
	        
	        var intersect = new google.maps.LatLng(y, x);
	        
	        if (isInBoundedBox(latlng1, latlng2, intersect) && isInBoundedBox(latlng3, latlng4, intersect))
	            intersection = intersect;
	        else
	            intersection = null;
	    } else
	        intersection = null;
	        
	    return intersection;
	};

	var isInBoundedBox = function(latlng1, latlng2, latlng3) {
	    var betweenLats;
	    var betweenLngs;
	    
	    if (latlng1.lat() < latlng2.lat())
	        betweenLats = (latlng1.lat() <= latlng3.lat() && latlng2.lat() >= latlng3.lat());
	    else
	        betweenLats = (latlng1.lat() >= latlng3.lat() && latlng2.lat() <= latlng3.lat());
	        
	    if (latlng1.lng() < latlng2.lng())
	        betweenLngs = (latlng1.lng() <= latlng3.lng() && latlng2.lng() >= latlng3.lng());
	    else
	    	betweenLngs = (latlng1.lng() >= latlng3.lng() && latlng2.lng() <= latlng3.lng());
	    
	    return (betweenLats && betweenLngs);
	};
	
	return {	
		checkForPathIntersection: function(path1, path2) {
			var path1Data = path1.getArray();
			var path2Data = path2.getArray();
			for (var i = 0; i < path1Data.length; i++) {
				var path1Point1 = path1Data[i];
				if (i + 1 < path1Data.length)
					var path1Point2 = path1Data[i + 1];
				else
					var path1Point2 = path1Data[0];
				
				for (var j = 0; j < path2Data.length; j++) {
					var path2Point1 = path2Data[j];
					if (j + 1 < path2Data.length)
						var path2Point2 = path2Data[j + 1];
					else
						var path2Point2 = path2Data[0];
					
					if (path1Point1 != path2Point1 && path1Point2 != path2Point2 &&
							path1Point1 != path2Point2 && path1Point2 != path2Point1 &&
							checkForLineIntersection(path1Point1, path1Point2, path2Point1, path2Point2))
						return true; 
				}
			}

			return false;
		}
	};
});

services.factory('GeoSearch', function(PolygonValidator) {
	
	var polygon = null;	
	var map = null;
	var createMode = true;
	
	return {
		activate: function(targetMap) {
			if (map == null) {			
				map = targetMap;
				
				if (polygon == null) {
					polygon = new google.maps.Polygon({
						strokeColor: "#FF0000",
						strokeOpacity: 0.8,
						strokeWeight: 2,
						fillColor: "#FF0000",
						fillOpacity: 0.35,
						draggable: true,
						editable: true
					});
				}
				
				google.maps.event.addListener(map, "click", function(event) {
				
					if (createMode) {
						var scale = Math.pow(2, map.getZoom());
						var clickPosition = map.getProjection().fromLatLngToPoint(event.latLng);	
						var northWestPosition = map.getProjection().fromLatLngToPoint(
								new google.maps.LatLng(map.getBounds().getNorthEast().lat(), map.getBounds().getSouthWest().lng()));
						var clickPositionX = Math.floor((clickPosition.x - northWestPosition.x) * scale);
						var clickPositionY = Math.floor((clickPosition.y - northWestPosition.y) * scale);		
						
						var polygonCoordinates = [ map.getProjection().fromPointToLatLng(new google.maps.Point((clickPositionX - 50) / scale + northWestPosition.x, (clickPositionY - 50) / scale + northWestPosition.y)), 
						                           map.getProjection().fromPointToLatLng(new google.maps.Point((clickPositionX + 50) / scale + northWestPosition.x, (clickPositionY - 50) / scale + northWestPosition.y)),
												   map.getProjection().fromPointToLatLng(new google.maps.Point((clickPositionX + 50) / scale + northWestPosition.x, (clickPositionY + 50) / scale + northWestPosition.y)),
												   map.getProjection().fromPointToLatLng(new google.maps.Point((clickPositionX - 50) / scale + northWestPosition.x, (clickPositionY + 50) / scale + northWestPosition.y)) 
												 ];
						
						polygon.setPath(polygonCoordinates);		
						polygon.setMap(map);
						
						google.maps.event.addListener(polygon.getPaths().getAt(0), "insert_at", function(index) {
							if (PolygonValidator.checkForPathIntersection(this, this))
								this.removeAt(index);
						});
						
						google.maps.event.addListener(polygon.getPaths().getAt(0), "set_at", function(index, oldLatLng) {
							if (PolygonValidator.checkForPathIntersection(this, this))
								this.setAt(index, oldLatLng);
						});
						
						map.setOptions({ disableDoubleClickZoom: true });
					}
				});
				
				google.maps.event.addListener(map, "rightclick", function(event) {
					if (polygon.getMap() == null)
						map.setOptions({ disableDoubleClickZoom: false });
					else {
						polygon.setPath([]);
						polygon.setMap(null);
					}
				});
			}
		},
		
		deactivate: function() {
			if (map != null) {
				polygon.setPath([]);
				polygon.setMap(null);		
				google.maps.event.clearListeners(map, "click");
				google.maps.event.clearListeners(map, "rightclick");
				map.setOptions({ disableDoubleClickZoom: false });
				map = null;
			}
		},
		
		getPolygon: function() { 
			return polygon;
		},
		
		setCreateMode: function(mode) {
			createMode = mode;
			
			if (polygon != null) {
				if (createMode)
					polygon.setOptions({draggable: true});
				else
					polygon.setOptions({draggable: false});
			}
		}
	};
});

services.factory('EscapingService', function() {
	return {
		escape: function(text) {
			if (!text)
				return null;

			return text.replace(/[\\\/\(\)\{\}\[\]\"\'\&\+\~\-\^]/g, "\\$&");
		}
	};
});

services.factory('MapTypeService', function() {
	var mapTypeId = "terrain";
	var maps = [];
	
	return {
		setMapTypeId: function(newMapTypeId) {
			mapTypeId = newMapTypeId;
			for (var i in maps) {
				maps[i].setMapTypeId(mapTypeId);
			}
		},
		
		getMapTypeId: function() {
			return mapTypeId;
		},
		
		addMap: function(newMap) {
			if (newMap && maps.indexOf(newMap) == -1)
				maps.push(newMap);
		}
	};
});