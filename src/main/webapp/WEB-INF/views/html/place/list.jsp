<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="l"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<s:message code="ui.search.result" text="Suchergebnis" var="pageTitle"/>
<s:message code="ui.search.hits" text="Treffer" var="pageSubtitleHits"/>

<l:page title="${pageTitle}">

	<jsp:attribute name="subtitle">
		${hits} ${pageSubtitleHits}
	</jsp:attribute>

	<jsp:attribute name="menu">	
		<l:map places="${places}" height="500px"/>	
	</jsp:attribute>

	<jsp:body>
		
		<c:choose>
			<c:when test="${fn:length(places) > 0}">
			
				<div class="row-fluid">
					<div class="span4">
						<p class="lead"><s:message code="ui.page" text="Seite"/> ${fn:substringBefore(offset/limit + 1, '.')} / ${fn:substringBefore(hits/limit + 0.999, '.')}</p>
					</div>
					<div class="span8 pager" style="text-align: right">
						<ul>				
							<c:choose>
								<c:when test="${offset-limit >= 0}">
									<li><a href="${baseUri}place?limit=${limit}&offset=${offset-limit}">&larr; <s:message code="ui.previous" text="Zurück"/></a></li>
								</c:when>
								<c:otherwise>
									<li class="disabled"><a href="#">&larr; <s:message code="ui.back" text="Zurück"/></a></li>
								</c:otherwise>
							</c:choose>
							<c:choose>
								<c:when test="${offset+limit < hits}">
									<li><a href="${baseUri}place?limit=${limit}&offset=${offset+limit}"><s:message code="ui.next" text="Vor"/> &rarr;</a></li>
								</c:when>
								<c:otherwise>
									<li class="disabled"><a href="#"><s:message code="ui.next" text="Vor"/> &rarr;</a></li>
								</c:otherwise>
							</c:choose>
						</ul>		
					</div>
				</div>
				
				<table class="table table-striped">
					<thead>
						<tr>
							<td>#</td>
							<td><s:message code="domain.placename.title" text="Name"/></td>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="place" items="${places}">				
							<tr>
								<td>${place.id}</td>
								<td><a href="place/${place.id}">${fn:join(place.namesAsArray, " / ")}</a></td>					
							</tr>				
						</c:forEach>
					</tbody>		
				</table>
			
			</c:when>
			<c:otherwise>
				<div class="row-fluid">
					<div class="span12 lead">
						<s:message code="ui.search.emptyResult" text="Es wurden keine Ergebnisse gefunden"/>
					</div>
				</div>
			</c:otherwise>
			
		</c:choose>
				
	</jsp:body>

</l:page>