<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ page session="false" import="org.dainst.gazetteer.domain.*" %>

{ 
	"@id": "${baseUri}place/${place.id}",
	<c:if test="${place.parent != null}">
		"parent": "${baseUri}place/${place.parent.id}",
	</c:if>
	<c:if test="${!empty(place.children)}">
		"children": [
			<c:forEach var="child" items="${place.children}" varStatus="status">
				"${baseUri}place/${child.id}"<c:if test="${status.count lt fn:length(place.children)}">,</c:if>
			</c:forEach>
		],
	</c:if>
	<c:if test="${!empty(place.names)}">
		"names": [
			<c:forEach var="placename" items="${place.names}" varStatus="status">
				{ "title": "${placename.title}", "language": "${placename.language}"}
				<c:if test="${status.count lt fn:length(place.names)}">,</c:if>
			</c:forEach>
		],
	</c:if>	
	<c:if test="${!empty(place.descriptions)}">
		"descriptions": [
			<c:forEach var="description" items="${place.descriptions}" varStatus="status">
				{ "title": "${description.description}", "language": "${description.language}"}
				<c:if test="${status.count lt fn:length(place.descriptions)}">,</c:if>
			</c:forEach>
		],
	</c:if>
	"locations": [
		<c:forEach var="location" items="${place.locations}" varStatus="status">
			{
				<c:if test="!fn:empty(location.descriptions)">
					"descriptions": [
						<c:forEach var="description" items="${location.descriptions}" varStatus="status2">
							{ "title": "${description.description}", "language": "${description.language}"}
							<c:if test="${status2.count lt fn:length(location.descritiptions)}">,</c:if>
						</c:forEach>
					],
				</c:if>
				"coordinates": [${location.lat}, ${location.lng}]
			}			
			<c:if test="${status.count lt fn:length(place.locations)}">,</c:if>
		</c:forEach>
	]
}