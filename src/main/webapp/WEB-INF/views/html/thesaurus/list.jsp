<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="l"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page import="java.util.*, org.dainst.gazetteer.domain.Thesaurus"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<l:page title="Thesauri">

	<jsp:attribute name="subtitle">
		
	</jsp:attribute>

	<jsp:body>
	
		<ul>
	
		<c:forEach var="thesaurus" items="${thesauri}">
			<li><a href="${baseUri}/thesaurus/${thesaurus.key}">${thesaurus.title}</a></li>
		</c:forEach>
		
		</ul>
	
	</jsp:body>

</l:page>