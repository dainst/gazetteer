<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib uri="http://www.springframework.org/tags" prefix="s"%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %><%@ page session="false" import="org.dainst.gazetteer.domain.*" %><% response.setHeader("Content-Type", "application/vnd.google-earth.kml+xml; charset=utf-8"); %><?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2">
	<Folder>
		<c:forEach var="place" items="${places}">		
			<NetworkLink>
				<name>${place.prefName.title}</name>
				<Link>
					<href>${baseUri}doc/${place.id}.kml</href>
				</Link>
			</NetworkLink>		
		</c:forEach>
	</Folder>
</kml>