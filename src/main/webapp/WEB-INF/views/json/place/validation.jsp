<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ page session="false" import="org.dainst.gazetteer.domain.*" %>

<% response.setHeader("Content-Type", "application/json; charset=utf-8"); %>

{
	<c:choose>
		<c:when test="${result.success}">
			"success": "true"
		</c:when>
		<c:otherwise>
			"success": "false",
			<c:set var="message" value="${fn:replace(result.message,'\"','')}"/>
			"message": "${message}"
		</c:otherwise>
	</c:choose>
}