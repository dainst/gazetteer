<%@ tag description="page layout"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ attribute name="places" required="true" type="java.util.List"%>
<%@ attribute name="width" required="false" type="java.lang.String"%>
<%@ attribute name="height" required="false" type="java.lang.String"%>

<c:if test="${empty width}"><c:set var="width" value="100%" /></c:if>
<c:if test="${empty height}"><c:set var="height" value="100%" /></c:if>

<div id="map_canvas" style="width: ${width}; height: ${height}"></div>

<script type="text/javascript"
	src="https://maps.google.com/maps/api/js?sensor=false"></script>
	
<script type="text/javascript">
<!--

// initialize map
var mapOptions = {
  zoom: 4,
  center: new google.maps.LatLng(0, 0),
  mapTypeId: google.maps.MapTypeId.ROADMAP
};
var map = new google.maps.Map(document.getElementById("map_canvas"), mapOptions);

// add markers for locations and auto zoom and center map
var bounds = new google.maps.LatLngBounds();
var activeinfowindow;
<c:forEach var="place" items="${places}">	
	<c:forEach var="location" items="${place.locations}">
		<c:set var="placeUri" value="${baseUri}place/${place.id}"/>
		<c:set var="title" value="${fn:join(place.namesAsArray, ' / ')}"/>
		var ll = new google.maps.LatLng("${location.lat}", "${location.lng}");
		var marker${place.id} = new google.maps.Marker({
			position: ll,
			title: "${title}",
			map: map
		});
		var infowindow${place.id} = new google.maps.InfoWindow({
		    content: "<h4>${fn:join(place.namesAsArray, ' / ')}</h4><p><a href=\"${placeUri}\">${placeUri}</a></p>",
		});
		google.maps.event.addListener(marker${place.id}, 'click', function() {
			if (activeinfowindow) activeinfowindow.close();  
			infowindow${place.id}.open(map,marker${place.id});
			activeinfowindow = infowindow${place.id};
		});
		bounds.extend(ll);
	</c:forEach>
</c:forEach>
map.fitBounds(bounds);

//-->
</script>

