<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page session="false" import="org.dainst.gazetteer.domain.*" %>

<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2">
<Placemark>
	<c:forEach var="placename" items="${place.names}">
	<name><c:out value="${placename.title}" /></name>
	</c:forEach>
	<c:forEach var="location" items="${place.locations}">
	<Point>
		<coordinates><c:out value="${location.lat}" />,<c:out value="${location.lng}" />,0</coordinates>
	</Point>
	</c:forEach>
  </Placemark>

</kml>