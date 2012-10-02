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

<c:choose>
	<c:when test="${nativePlaceName != null && nativePlaceName.title != ''}">
		<c:set var="placeTitle" value="${nativePlaceName.title}" />
	</c:when>
	<c:otherwise>
		<c:set var="placeTitle" value="${place.names[0].title}" />
	</c:otherwise>
</c:choose>

<l:page title="${placeTitle}">

	<jsp:attribute name="subtitle">
		${baseUri}place/${place.id}
		<s:message code="ui.copyToClipboard" var="copyMsg"/>
		<a data-toggle="modal" href="#copyUriModal"><i class="icon-share"></i></a>
	</jsp:attribute>

	<jsp:body>
	
		<div class="subnav">
			<ul class="nav nav-pills">
				<c:if test="${limit != null}">
					<li>
						<a href="${baseUri}place?limit=${limit}&offset=${offset}&q=${q}&view=${view}">
							&larr; 
							<s:message code="ui.search.back" />
						</a>
					</li>
				</c:if>
			</ul>
		</div>
	
		<c:choose>
		
			<c:when test="${place.deleted}">
				<div class="alert"><s:message code="ui.place.deleted" text="ui.place.deleted" /></div>
			</c:when>
			
			<c:otherwise>
			
				<div class="modal hide" id="copyUriModal">
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal">×</button>
						<h3><s:message code="ui.copyToClipboardHeading"/></h3>
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
				
				<div class="row-fluid" id="contentDiv">
				
					<div class="span5 well">
						<l:map places="${places}" height="500px"/>	
					</div>
					
					<div class="span7">
						
						<div class="pull-right">
							<a class="btn btn-primary" href="?layout=edit&limit=${limit}&offset=${offset}&q=${q}&view=${view}">
								<i class="icon-edit icon-white"></i>
							</a>
							<a class="btn btn-danger" data-toggle="modal" href="#deleteModal">
								<i class="icon-trash icon-white"></i>
							</a>
							<div class="modal hide" id="deleteModal">
							  <div class="modal-header">
							    <button type="button" class="close" data-dismiss="modal">×</button>
							    <h3><s:message code="ui.delete.really" /></h3>
							  </div>
							  <div class="modal-footer">
							    <a href="#" class="btn" data-dismiss="modal"><s:message code="ui.cancel" text="ui.cancel" /></a>
							    <a href="#" class="btn btn-danger" id="deleteBtn" data-dismiss="modal"><s:message code="ui.delete" text="ui.delete" /></a>
							    <s:message code="ui.failure" var="failureMsg"/>
							    <script type="text/javascript">
									$("#deleteBtn").click(function() {
										$.ajax({
											type: "DELETE",
											url: "${baseUri}place/${place.id}"
										}).done(function() {
											location.reload(true);
										}).fail(function(jqXHR) {
											var data = $.parseJSON(jqXHR.responseText);
											$("#place-form-div").prepend("<div class='alert alert-error'><button type='button' class='close' data-dismiss='alert'>×</button><strong>${failureMsg}!</strong> "+data.message+"</div>");
										});
									});
								</script>
							  </div>
							</div>
						</div>
						
						<h3><s:message code="domain.place.names" />:</h3>
						<ul>
							<c:forEach var="placename" items="${place.names}">
								<li>
									${placename.title}
									<c:if test="${languages[placename.language] != null}">
										<em>(${languages[placename.language]})</em>
									</c:if>
								</li>
							</c:forEach>
						</ul>
						
						<c:if test="${place.parent != null}">
							<h3><s:message code="domain.place.parent" text="domain.place.parent" />:</h3>	
							<ul>
								<li>
									<a href="${place.parent.id}?limit=${limit}&offset=${offset}&q=${q}&view=${view}">${fn:join(place.parent.namesAsArray, " / ")}
										<c:if test="${place.parent.type != null}">
											<em>(${place.parent.type})</em>
										</c:if>
									</a>
								</li>
							</ul>
						</c:if>
						
						<c:if test="${!empty(place.children)}">
							<h3><s:message code="domain.place.children" text="domain.place.children" />:</h3>
							<ul>
								<c:forEach var="child" items="${place.children}">
									<li>
										<a href="${child.id}?limit=${limit}&offset=${offset}&q=${q}&view=${view}">${fn:join(child.namesAsArray, " / ")}
											<c:if test="${child.type != null}">
												<em>(${child.type})</em>
											</c:if>
										</a>
									</li>
								</c:forEach>
							</ul>
						</c:if>					
						
						<c:if test="${!empty(place.relatedPlaces)}">
							<h3><s:message code="domain.place.relatedPlaces" text="domain.place.relatedPlaces" />:</h3>
							<ul>
								<c:forEach var="relatedPlace" items="${place.relatedPlaces}">
									<li>
										<a href="${relatedPlace.id}?limit=${limit}&offset=${offset}&q=${q}&view=${view}">${fn:join(relatedPlace.namesAsArray, " / ")}
											<c:if test="${relatedPlace.type != null}">
												<em>(${relatedPlace.type})</em>
											</c:if>	
										</a>
									</li>
								</c:forEach>
							</ul>
						</c:if>
						
						<h3><s:message code="domain.place.locations" text="domain.place.locations" />:</h3>
						<ul>
							<c:forEach var="location" items="${place.locations}">
								<li>
									<strong><s:message code="domain.location.latitude" text="domain.location.latitude" />:</strong> ${location.lat}
									<strong><s:message code="domain.location.longitude" text="domain.location.longitude" />:</strong> ${location.lng}
									<em><strong><s:message code="domain.location.confidence" text="domain.location.confidence" />:</strong> ${location.confidence}</em>
								</li>
							</c:forEach>
						</ul>
						
						<c:if test="${!empty(place.type)}">
							<h3><s:message code="domain.place.type" text="domain.place.type" />: </h3>
							<p>${place.type}</p>
						</c:if>
						
						<c:if test="${!empty(place.thesaurus)}">
							<h3><s:message code="domain.thesaurus" text="domain.thesaurus" />: </h3>
							<p>${place.thesaurus.title}</p>
						</c:if>
						
						<c:if test="${!empty(place.identifiers)}">
							<h3><s:message code="domain.place.identifiers" text="domain.place.identifiers" />:</h3>
							<ul>
								<c:forEach var="identifier" items="${place.identifiers}">
									<li>
										<strong>${identifier.context}:</strong> ${identifier.value}
									</li>
								</c:forEach>
							</ul>
						</c:if>
						
						<c:if test="${!empty(place.comments)}">
							<h3><s:message code="domain.place.comments" text="domain.place.comments" />:</h3>
							<c:forEach var="comment" items="${place.comments}">
								<blockquote>
									${comment.text}
								</blockquote>
							</c:forEach>
						</c:if>
						
						<c:if test="${!empty(place.tags)}">
							<h3><s:message code="domain.place.tags" text="domain.place.tags" />:</h3>
							<ul>
								<c:forEach var="tag" items="${place.tags}">
									<li>
										${tag.text}
									</li>
								</c:forEach>
							</ul>
						</c:if>
						
					</div>
					
				</div>
			
			</c:otherwise>
			
		</c:choose>
			
	</jsp:body>

</l:page>