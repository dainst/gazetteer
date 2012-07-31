<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="l"%>
<%@ taglib prefix="json" uri="http://www.atg.com/taglibs/json" %>
<%@ page import="java.util.*, org.dainst.gazetteer.domain.Place"%>
<%@ page contentType="application/javascript; charset=utf-8" session="false"%>

<%
// create list with one entry for map display
List<Place> places = new ArrayList<Place>();
places.add((Place) request.getAttribute("place"));
request.setAttribute("places", places);
%>

<c:set var="html">
	<div class="gaz-show">
		<div class="gaz-info">
			<h4><a href="${baseUri}place/${place.id}">#${place.id} - ${fn:join(place.namesAsArray, " / ")}</a></h4>
			<ul>
				<c:forEach var="location" items="${place.locations}" varStatus="status">
				<li>
					<strong>
						<c:if test="${status.count > 1}"><s:message code="domain.placename.alternative" text="Alternative"/></c:if>
						<s:message code="domain.placename.title" text="Lage" />:
					</strong>
					<ul>
						<li><em><s:message code="domain.location.latitude" text="Breite"/></em>: ${location.lat}</li>
						<li><em><s:message code="domain.location.latitude" text="Länge"/></em>: ${location.lng}</li>
					</ul>
				</li>
				</c:forEach>
			</ul>
		</div>
		<div class="gaz-map">
			<l:map places="${places}" height="${mapHeight}px"/>
		</div>	
	</div>
</c:set>

${callback}(
<json:object>
	<json:property name="html" value="${html}"/>
</json:object>
);
