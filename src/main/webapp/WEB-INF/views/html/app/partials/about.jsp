<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<div>
	<c:choose>
		<c:when test="${language eq 'ar'}">
			<p dir="rtl" lang="ar"><s:message code="about.info1" text="about.info1"/></p>
			<p dir="rtl" lang="ar"><s:message code="about.info2" text="about.info2"/></p>
			<p dir="rtl" lang="ar"><s:message code="about.info3" text="about.info3"/></p>
		</c:when>
		<c:otherwise>
			<p><s:message code="about.info1" text="about.info1"/></p>
			<p><s:message code="about.info2" text="about.info2"/></p>
			<p><s:message code="about.info3" text="about.info3"/></p>
		</c:otherwise>
	</c:choose>
</div>

<div>
	<a href="<s:message code="about.videoLink" text="about.videoLink"/>">Youtube Screencast</a>
</div>