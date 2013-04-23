<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="l"%>
<%@ taglib prefix="json" uri="http://www.atg.com/taglibs/json" %>
<%@ page import="java.util.*, org.dainst.gazetteer.domain.Place"%>
<%@ page contentType="application/javascript; charset=utf-8" session="false"%>

<c:set var="html">
	<div class="gaz-show">
		<div class="gaz-title"><a href="${baseUri}" target="_blank">iDAI.gazetteer</a></div>
		<c:if test="${showInfo}">
			<c:forEach var="place" items="${places}">
				<div class="gaz-info">
					<h4><a href="${baseUri}place/${place.id}">#${place.id} - ${place.prefName.title}</a></h4>
					<ul>
						<li><em><s:message code="domain.location.latitude" text="Breite"/></em>: ${place.prefLocation.lat}</li>
						<li><em><s:message code="domain.location.longitude" text="Länge"/></em>: ${place.prefLocation.lng}</li>
					</ul>
				</div>
			</c:forEach>
		</c:if>
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
