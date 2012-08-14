<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="l"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page import="java.util.*, org.dainst.gazetteer.domain.Place"%>
<%@ page import="org.springframework.context.annotation.Import"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>


<%
// create list with one entry for map display
List<Place> places = new ArrayList<Place>();
places.add((Place) request.getAttribute("place"));
request.setAttribute("places", places);
%>

<l:page title="${nativePlaceName.title}">

	<jsp:attribute name="subtitle">
		${baseUri}place/${place.id}
		<s:message code="ui.copyToClipboard" text="In die Zwischenablage kopieren mit Strg+C / Cmd+C" var="copyMsg"/>
		<a data-toggle="modal" href="#copyUriModal"><i class="icon-share"></i></a>
	</jsp:attribute>

	<jsp:body>
		
		<div class="modal hide" id="copyUriModal">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal">×</button>
				<h3><s:message code="ui.copyToClipboardHeading" text="URI in die Zwischenablage kopieren"/></h3>
			</div>
			<div class="modal-body">
				<label>${copyMsg}</label>
				<input class="input-xxlarge" type="text" value="${baseUri}place/${place.id}" id="copyUriInput">
			</div>
		</div>
		<script type="text/javascript">
			$("#copyUriModal").on("shown",function() {
				$("#copyUriInput").focus().select();
			});
		</script>
		
		<div class="row-fluid">
		
			<div class="span5 well">
				<l:map places="${places}" height="500px"/>	
			</div>
			
			<div class="span7">
				
				<div class="pull-right">
					<a class="btn btn-primary" href="?layout=edit">
						<i class="icon-edit icon-white"></i>
					</a>
					<a class="btn btn-danger" data-toggle="modal" href="#deleteModal">
						<i class="icon-trash icon-white"></i>
					</a>
					<div class="modal hide" id="deleteModal">
					  <div class="modal-header">
					    <button type="button" class="close" data-dismiss="modal">×</button>
					    <h3><s:message code="ui.delete.really" text="Möchten Sie diesen Ort wirklich löschen?"/></h3>
					  </div>
					  <div class="modal-footer">
					    <a href="#" class="btn" data-dismiss="modal"><s:message code="ui.cancel" text="Abbrechen"/></a>
					    <a href="#" class="btn btn-danger"><s:message code="ui.delete" text="Löschen"/></a>
					  </div>
					</div>
				</div>
				
				<h1><s:message code="domain.placename.title" text="Ortsnamen" />:</h1>
				<ul>
					<c:forEach var="placename" items="${place.names}">
						<li>${placename.title}</li>
					</c:forEach>
				</ul>
				
				<c:if test="${place.parent != null}">
					<h1><s:message code="domain.place.parent" text="Übergeordneter Ort" />:</h1>	
					<ul><li><a href="${place.parent.id}">${place.parent.nameMap[language].title}</a></li></ul>
				</c:if>
				
				<c:if test="${!empty(place.children)}">
					<h1><s:message code="domain.place.children" text="Untergeordnete Orte" />:</h1>
					<ul>
						<c:forEach var="child" items="${place.children}">
							<li><a href="${child.id}">${child.nameMap[language].title}</a></li>
						</c:forEach>
					</ul>
				</c:if>
			
				<h1><s:message code="domain.placename.title" text="Lage" />:</h1>
				<ul>
					<c:forEach var="location" items="${place.locations}">
						<li>
							<s:message code="domain.location.latitude" text="Breite"/>: ${location.lat}
							<s:message code="domain.location.latitude" text="Länge"/>: ${location.lng}
						</li>
					</c:forEach>
				</ul>
			</div>
			
		</div>
			
	</jsp:body>

</l:page>