<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page session="false"%>

<% response.setHeader("Content-Type", "text/html; charset=utf-8"); %>

<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
<style type="text/css">
html {
	height: 100%
}

body {
	height: 100%;
	margin: 0px;
	padding: 0px
}

#map_canvas {
	height: 100%
}
</style>
<script type="text/javascript"
	src="https://maps.google.com/maps/api/js?sensor=false">
	
</script>
<script type="text/javascript">
	function initialize() {
		var latlng = new google.maps.LatLng(0, 0);
		var options = {
			zoom : 0,
			center : latlng,
			mapTypeId : google.maps.MapTypeId.ROADMAP
		};
		var map = new google.maps.Map(document.getElementById("map_canvas"), options);
		var kmlLayer = new google.maps.KmlLayer("${baseUri}place/${place.id}.kml");
		kmlLayer.setMap(map);
	}
</script>
</head>
<body onload="initialize()">

	<h1><s:message code="domain.placename.title" text="Ortsnamen" />:</h1>
	<ul>
		<c:forEach var="placename" items="${place.names}">
			<li>${placename.title}</li>
		</c:forEach>
	</ul>
	
	<c:if test="${place.parent != null}">
		<h1><s:message code="domain.place.parent" text="Übergeordneter Ort" />:</h1>	
		<ul><li><a href="${place.parent.id}">${place.parent.nameMap[language].title}</a></li></ul>
	</c:if>
	
	<c:if test="${!empty(place.children)}">
		<h1><s:message code="domain.place.children" text="Untergeordnete Orte" />:</h1>
		<ul>
			<c:forEach var="child" items="${place.children}">
				<li><a href="${child.id}">${child.nameMap[language].title}</a></li>
			</c:forEach>
		</ul>
	</c:if>

	<h1><s:message code="domain.placename.title" text="Lage" />:</h1>
	<div id="map_canvas" style="width: 400px; height: 300px"></div>
	
</body>
</html>