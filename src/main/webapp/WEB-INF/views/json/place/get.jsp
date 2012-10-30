<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ page session="false" import="org.dainst.gazetteer.domain.*" %>

<% response.setHeader("Content-Type", "application/json; charset=utf-8"); %>

{ 
	"@id": "${baseUri}place/${place.id}",
	"gazId": "${place.id}",
	"thesaurus": "${place.thesaurus}",
	<c:if test="${place.type != null}">
		"type": "${place.type}",
	</c:if>
	<c:if test="${parent != null}">
		"parent": "${baseUri}place/${parent.id}",
	</c:if>
	<c:if test="${!empty(children)}">
		"children": [
			<c:forEach var="child" items="${children}" varStatus="status">
				"${baseUri}place/${child.id}"<c:if test="${status.count lt fn:length(children)}">,</c:if>
			</c:forEach>
		],
	</c:if>
	<c:if test="${!empty(relatedPlaces)}">
		"relatedPlaces": [
			<c:forEach var="relatedPlace" items="${relatedPlaces}" varStatus="status">
				"${baseUri}place/${relatedPlace.id}"<c:if test="${status.count lt fn:length(relatedPlaces)}">,</c:if>
			</c:forEach>
		],
	</c:if>
	<c:if test="${!empty(place.locations)}">
		"locations": [
			<c:forEach var="location" items="${place.locations}" varStatus="status">
				{
					"coordinates": [${location.lat}, ${location.lng}],
					"confidence": ${location.confidence}
				}			
				<c:if test="${status.count lt fn:length(place.locations)}">,</c:if>
			</c:forEach>
		],
	</c:if>
	<c:if test="${!empty(place.identifiers)}">
		"identifiers": [
			<c:forEach var="identifier" items="${place.identifiers}" varStatus="status">
				{ "value": "${identifier.value}", "context": "${identifier.context}"}
				<c:if test="${status.count lt fn:length(place.identifiers)}">,</c:if>
			</c:forEach>
		],
	</c:if>
	<c:if test="${!empty(place.comments)}">
		"comments": [
			<c:forEach var="comment" items="${place.comments}" varStatus="status">
				{ "text": "${comment.text}", "language": "${comment.language}"}
				<c:if test="${status.count lt fn:length(place.comments)}">,</c:if>
			</c:forEach>
		],
	</c:if>
	<c:if test="${!empty(place.tags)}">
		"tags": [
			<c:forEach var="tag" items="${place.tags}" varStatus="status">
				{ "text": "${tag.text}", "language": "${tag.language}"}
				<c:if test="${status.count lt fn:length(place.tags)}">,</c:if>
			</c:forEach>
		],
	</c:if>
	"names": [
		<c:forEach var="placename" items="${place.names}" varStatus="status">
			{ "title": "${placename.title}", "language": "${placename.language}"}
			<c:if test="${status.count lt fn:length(place.names)}">,</c:if>
		</c:forEach>
	]
}