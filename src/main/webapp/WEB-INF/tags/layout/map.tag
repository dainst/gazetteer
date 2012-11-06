<%@ tag description="page layout"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ attribute name="places" required="false" type="java.util.List"%>
<%@ attribute name="width" required="false" type="java.lang.String"%>
<%@ attribute name="height" required="false" type="java.lang.String"%>
<%@ attribute name="googleMapsApiKey" required="false" type="java.lang.String"%>

<c:if test="${empty width}"><c:set var="width" value="100%" /></c:if>
<c:if test="${empty height}"><c:set var="height" value="100%" /></c:if>

<div id="map_canvas" style="width: ${width}; height: ${height}"></div>
	
<script type="text/javascript">
<!--

var mapsApiCallback;
var map;
var markers = [];

function requireGoogleMaps(callback, apiKey) {
	
	/******** Load google maps api if not present *********/
	if (typeof window.google === "undefined") {
		mapsApiCallback = callback;
	    var script_tag = document.createElement('script');
	    script_tag.setAttribute("type","text/javascript");
	    var src = "https://maps.google.com/maps/api/js?sensor=false&callback=mapsApiCallback&key=" + apiKey;
	    script_tag.setAttribute("src", src);
	    // Try to find the head, otherwise default to the documentElement
	    (document.getElementsByTagName("head")[0] || document.documentElement).appendChild(script_tag);
	} else {
	    // called if google maps api already present
	    callback();
	}
	
}

(function() {

	requireGoogleMaps(showMap, "${googleMapsApiKey}");

	/******** Actual logic that uses the google maps api ********/
	function showMap() {
		
		// initialize map
		var mapOptions = {
			zoom: 4,
			center: new google.maps.LatLng(0, 0),
			mapTypeId: google.maps.MapTypeId.ROADMAP
		};
		map = new google.maps.Map(document.getElementById("map_canvas"), mapOptions);

		// add markers for locations and auto zoom and center map
		var bounds = new google.maps.LatLngBounds();
		var activeinfowindow;
		var ll;
		<c:forEach var="place" items="${places}">	
			<c:forEach var="location" items="${place.locations}">
				<c:set var="placeUri" value="${baseUri}place/${place.id}"/>
				<c:set var="title" value="${place.prefName.title}"/>
				ll = new google.maps.LatLng("${location.lat}", "${location.lng}");
				markers['${place.id}'] = new google.maps.Marker({
					position: ll,
					title: "${title}",
					map: map
				});
				var infowindow${place.id} = new google.maps.InfoWindow({
				    content: "<h4>${place.prefName.title}</h4><p><a href=\"${placeUri}\">${placeUri}</a></p>",
				});
				google.maps.event.addListener(markers['${place.id}'], 'click', function() {
					if (activeinfowindow) activeinfowindow.close();  
					infowindow${place.id}.open(map,markers['${place.id}']);
					activeinfowindow = infowindow${place.id};
				});
				bounds.extend(ll);
			</c:forEach>
		</c:forEach>
		
		<c:choose>
			<c:when test="${fn:length(places) > 1}">		
				map.fitBounds(bounds);
			</c:when>
			<c:otherwise>
				map.setCenter(ll);
			</c:otherwise>
		</c:choose>
	    
	}

})(); // We call our anonymous function immediately

var dynMarker;

function zoomToPlace(placeId) {
	
	$.getJSON('${baseUri}doc/'+ placeId +'.json', function(place) {
		if (place.locations && place.locations[0]) {
			var ll = new google.maps.LatLng(place.locations[0].coordinates[1], place.locations[0].coordinates[0]);
			dynMarker = new google.maps.Marker({
				position: ll,
				title: place.prefName.title,
				map: map
			});
			map.setZoom(5);
			map.panTo(dynMarker.position);
		}
	});
	
}

function resetMarker() {
	if (dynMarker) dynMarker.setMap(null);
}

//-->
</script>

