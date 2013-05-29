<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ page session="false" import="org.dainst.gazetteer.domain.*"%><% response.setHeader("Content-Type", "text/xml"); %><?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
	<c:if test="${no == 1}">
		<url>
			<loc>${baseUri}</loc>
		</url>
	</c:if>
	<c:forEach var="place" items="${places}">
		<url>
			<loc>${baseUri}app/#!/show/${place.id}</loc>
		</url>
	</c:forEach>
</urlset>