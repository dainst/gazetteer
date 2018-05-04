<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<hr>
<footer>
	<div class="dai-footer-logo"><a href="http://www.dainst.org/"><img src="../resources/img/logo_dai.png" alt="Deutsches Arch�ologisches Institut" /></a></div>
	<c:choose>
		<c:when test="${language eq 'ar'}">
	 		<p dir="rtl" lang="ar"><a href="https://www.dainst.org/de/datenschutz" target="_blank"><s:message code="ui.policy" text="ui.policy"/></a><br/><s:message code="ui.license" text="ui.license"/><br/><s:message code="ui.mailingList" text="ui.mailingList"/><br/><small><em>Version: ${version}</em></small></p>
	 	</c:when>
	 	<c:otherwise>
	 		<p><a href="https://www.dainst.org/de/datenschutz" target="_blank"><s:message code="ui.policy" text="ui.policy"/></a><br/><s:message code="ui.license" text="ui.license"/><br/><s:message code="ui.mailingList" text="ui.mailingList"/><br/><small><em>Version: ${version}</em></small></p>
	 	</c:otherwise>
	 </c:choose>
</footer>