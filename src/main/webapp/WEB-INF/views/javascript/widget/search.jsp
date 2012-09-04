<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ page session="false" import="org.dainst.gazetteer.domain.*" %>

<% response.setHeader("Content-Type", "application/javascript; charset=utf-8"); %>

${callback}({
	"places": [
		<c:forEach var="place" items="${places}">				
			{
				"@id": "${baseUri}place/${place.id}",
				"names": [
					<c:forEach var="placename" items="${place.names}" varStatus="status">
						{ "title": "${placename.title}", "language": "${placename.language}"}
						<c:if test="${status.count lt fn:length(place.names)}">,</c:if>
					</c:forEach>
				]
			},
		</c:forEach>
	]
});