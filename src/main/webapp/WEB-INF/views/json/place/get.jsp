<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page session="false" import="org.dainst.gazetteer.domain.*" %>

{ 
	"type": "FeatureCollection",
	"properties": {
		"names": [
			<c:forEach var="placename" items="${place.names}" varStatus="status">
				{ "title": "${placename.title}", "language": "${placename.language}"}
				<c:if test="${status.getCount() lt fn:length(place.names)}">,</c:if>
			</c:forEach>
		],
		"descriptions": [
			<c:forEach var="description" items="${place.descriptions}" varStatus="status">
				{ "title": "${description.description}", "language": "${description.language}"}
				<c:if test="${status.getCount() lt fn:length(place.names)}">,</c:if>
			</c:forEach>
		]
	},
	"features": [
		<c:forEach var="location" items="${place.locations}" varStatus="status">
			{
				"type": "Feature",
				"geometry": { "type": "Point", "coordinates": [${location.lat}, ${location.lng}] },
				"properties": {
					"descriptions": [
						<c:forEach var="description" items="${location.descriptions}" varStatus="status">
							{ "title": "${description.description}", "language": "${description.language}"}
							<c:if test="${status.getCount() lt fn:length(place.names)}">,</c:if>
						</c:forEach>
					]
				}
			}
			<c:if test="${status.getCount() lt fn:length(place.locations)}">,</c:if>
		</c:forEach>
	]
}