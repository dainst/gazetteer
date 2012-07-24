<%@page import="org.springframework.context.annotation.Import"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="l"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page import="java.util.*, org.dainst.gazetteer.domain.Place" session="false"%>


<%
// create list with one entry for map display
List<Place> places = new ArrayList<Place>();
places.add((Place) request.getAttribute("place"));
request.setAttribute("places", places);
%>

<l:page title="${fn:join(place.namesAsArray, ' / ')}">

	<jsp:attribute name="menu">
		<l:map places="${places}" height="500px"/>	
	</jsp:attribute>

	<jsp:body>

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
		<ul>
			<c:forEach var="location" items="${place.locations}">
				<li>
					<s:message code="domain.location.latitude" text="Breite"/>: ${location.lat}
					<s:message code="domain.location.latitude" text="Länge"/>: ${location.lng}
				</li>
			</c:forEach>
		</ul>
			
	</jsp:body>

</l:page>