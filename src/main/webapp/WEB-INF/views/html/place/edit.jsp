<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="l"%>
<%@ taglib tagdir="/WEB-INF/tags/place" prefix="p"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<l:page title="${nativePlaceName.title}">

	<jsp:attribute name="subtitle">
		<s:message code="ui.edit" text="Bearbeiten"/>
	</jsp:attribute>
	
	<jsp:body>
		<p:form place="${place}" />
	</jsp:body>

</l:page>