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

.row {
	border-bottom: 1px solid grey;
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
		<c:forEach var="place" items="${places}">
			var kmlLayer = new google.maps.KmlLayer("${baseUri}place/${place.id}.kml");
			kmlLayer.setMap(map);
		</c:forEach>
	}
</script>
</head>
<body onload="initialize()">

	<div id="map_canvas" style="width: 400px; height: 300px"></div>

	<c:forEach var="place" items="${places}">
	
		<div class="row">

			<h1><a href="place/${place.id}">${place.nameMap[language].title}</a></h1>
			<p>${place.descriptionMap[language].description}</p>
		
		</div>
	
	</c:forEach>
	
</body>
</html>