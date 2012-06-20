<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page session="false" import="org.dainst.gazetteer.domain.*" %>

<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2">
	<Document id="${place.mainUri}">
		<c:forEach var="placename" items="${place.names}">
			<name xml:lang="${placename.language}"><c:out value="${placename.title}" /></name>
		</c:forEach>
		<c:forEach var="description" items="${place.descriptions}">
			<description xml:lang="${description.language}"><c:out value="${description.description}" /></description>
		</c:forEach>
		<c:forEach var="location" items="${place.locations}">
			<Placemark id="${place.mainUri}/location/${location.id}">
				<c:forEach var="description" items="${location.descriptions}">
					<description xml:lang="${description.language}"><c:out value="${description.description}" /></description>
				</c:forEach>
				<Point>
					<coordinates><c:out value="${location.lat}" />,<c:out value="${location.lng}" />,0</coordinates>
				</Point>
			</Placemark>
		</c:forEach>
	</Document>
</kml>