<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="l"%>
<%@ taglib prefix="json" uri="http://www.atg.com/taglibs/json" %>
<%@ page import="java.util.*, org.dainst.gazetteer.domain.Place"%>
<%@ page contentType="application/javascript; charset=utf-8" session="false"%>

<c:set var="html">
	<span class="gaz-pick">
		<div class="input-append">
			<input type="text" class="gaz-result ${cssClass}" name="${name}" id="${id}" value="${value}" autocomplete="off" disabled size="50">
			<c:choose>
				<c:when test="${disabled}">
					<button class="btn disabled" disabled type="button">
						<i class="icon-search"></i><i class="icon-globe"></i>
					</button>
				</c:when>
				<c:otherwise>
					<button class="btn" type="button">
						<i class="icon-search"></i><i class="icon-globe"></i>
					</button>
				</c:otherwise>
			</c:choose>
		</div>
		<div class="gaz-pick-overlay" style="display:none;">
			<div class="navbar navbar-inverse">
				<div class="navbar-inner">
					<form class="navbar-search pull-left" action="/gazetteer/place" autocomplete="off">
						<s:message code="ui.pick.search" text="Suche" var="titleSearch"/>
		 				<input type="text" class="search-query" placeholder="${titleSearch}" name="q" autocomplete="off">
		 				<i class="icon-search icon-white"></i>
					</form>
				</div>
			</div>
			<div class="gaz-pick-results">
			
			</div>
		</div>
	</span>
</c:set>

${callback}(
<json:object>
	<json:property name="html" value="${html}"/>
</json:object>
);
