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

<s:url var="searchAction" value="/search" />

<c:choose>
	<c:when test="${nativePlaceName != null && nativePlaceName.title != ''}">
		<c:set var="placeTitle" value="${nativePlaceName.title}" />
	</c:when>
	<c:otherwise>
		<c:set var="placeTitle" value="${place.prefName.title}" />
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
						<a href="${searchAction}?limit=${limit}&offset=${offset}&q=${q}&view=${view}">
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
						<c:choose>
							<c:when test="${fn:length(place.locations) > 0}">
								<l:map places="${places}" height="500px" zoom="7"/>
							</c:when>
							<c:otherwise>
								<div style="height: 500px; text-align: center;">
									<em><s:message code="ui.noLocation" text="ui.noLocation"/></em>
								</div>
							</c:otherwise>
						</c:choose>
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
						
						<h3><s:message code="ui.information" text="ui.information"/></h3>
						
						<dl class="dl-horizontal">
						
							<dt><s:message code="domain.place.names" /></dt>
							<dd>
								<em><s:message code="domain.place.prefName" text="domain.place.prefName"/>: </em>
								${place.prefName.title}
								<c:if test="${languages[place.prefName.language] != null}">
									<em>(${languages[place.prefName.language]})</em>
								</c:if>
							</dd>
							<c:forEach var="placename" items="${place.names}">
								<dd>
									${placename.title}
									<c:if test="${languages[placename.language] != null}">
										<em>(${languages[placename.language]})</em>
									</c:if>
								</dd>
							</c:forEach>
							<br/>
							
							<c:if test="${parent != null}">
								<dt><s:message code="domain.place.parent" text="domain.place.parent" /></dt>
								<dd>
									<a href="${parent.id}?limit=${limit}&offset=${offset}&q=${q}&view=${view}">${parent.prefName.title}
										<c:if test="${parent.type != null}">
											<em>(<s:message code="types.${parent.type}" text="${parent.type}"/>)</em>
										</c:if>
									</a>
								</dd>
								<br/>
							</c:if>
							
							<c:if test="${!empty(children)}">
								<dt><s:message code="domain.place.children" text="domain.place.children" /></dt>
								<c:choose>
									<c:when test="${fn:length(children) <= 10}">
										<c:forEach var="child" items="${children}">
											<dd>
												<a href="${child.id}?limit=${limit}&offset=${offset}&q=${q}&view=${view}">${child.prefName.title}
													<c:if test="${child.type != null}">
														<em>(${child.type})</em>
													</c:if>
												</a>
											</dd>
										</c:forEach>
									</c:when>
									<c:otherwise>
										<dd>
											<a href="${searchAction}?q=parent:${place.id}">
												<s:message code="ui.numberOfPlaces" text="ui.numberOfPlaces" arguments="${fn:length(children)}" />
											</a>
										</dd>
									</c:otherwise>
								</c:choose>
								<br/>
							</c:if>					
							
							<c:if test="${!empty(relatedPlaces)}">
								<dt><s:message code="domain.place.relatedPlaces" text="domain.place.relatedPlaces" /></dt>
								<c:forEach var="relatedPlace" items="${relatedPlaces}">
									<dd>
										<a href="${relatedPlace.id}?limit=${limit}&offset=${offset}&q=${q}&view=${view}">${relatedPlace.prefName.title}
											<c:if test="${relatedPlace.type != null}">
												<em>(${relatedPlace.type})</em>
											</c:if>	
										</a>
									</dd>
								</c:forEach>
								<br/>
							</c:if>
							
							<c:if test="${fn:length(place.locations) > 0}">
								<dt><s:message code="domain.place.locations" text="domain.place.locations" /></dt>
								<c:forEach var="location" items="${place.locations}">
									<dd>
										<em><s:message code="domain.location.latitude" text="domain.location.latitude" />:</em> ${location.lat},
										<em><s:message code="domain.location.longitude" text="domain.location.longitude" />:</em> ${location.lng}
										(<em><s:message code="domain.location.confidence" text="domain.location.confidence" />:</em>
										<s:message code="confidence.${location.confidence}" text="${location.confidence}"/>)
									</dd>
								</c:forEach>
								<br/>
							</c:if>
							
							<c:if test="${!empty(place.type)}">
								<dt><s:message code="domain.place.type" text="domain.place.type" /></dt>
								<dd><s:message code="types.${place.type}" text="${place.type}"/></dd>
								<br/>
							</c:if>
							
							<c:if test="${!empty(place.thesaurus)}">
								<dt><s:message code="domain.thesaurus" text="domain.thesaurus" /></dt>
								<dd>${place.thesaurus}</dd>
								<br/>
							</c:if>
							
							<c:if test="${!empty(place.identifiers)}">
								<dt><s:message code="domain.place.identifiers" text="domain.place.identifiers" /></dt>
								<c:forEach var="identifier" items="${place.identifiers}">
									<dd>
										<em>${identifier.context}:</em> ${identifier.value}
									</dd>
								</c:forEach>
								<br/>
							</c:if>
							
							<c:if test="${!empty(place.links)}">
								<dt><s:message code="domain.place.links" text="domain.place.links" /></dt>
								<c:forEach var="link" items="${place.links}">
									<dd>
										<em>${link.predicate}:</em> <a href="${link.object}" target="_blank">${link.object}</a>
									</dd>
								</c:forEach>
								<br/>
							</c:if>
							
							<c:if test="${!empty(place.comments)}">
								<dt><s:message code="domain.place.comments" text="domain.place.comments" /></dt>
								<c:forEach var="comment" items="${place.comments}">
									<dd><blockquote>${comment.text}</blockquote></dd>
								</c:forEach>
								<br/>
							</c:if>
							
							<c:if test="${!empty(place.tags)}">
								<dt><s:message code="domain.place.tags" text="domain.place.tags" /></dt>
								<c:forEach var="tag" items="${place.tags}">
									<dd>
										${tag.text}
									</dd>
								</c:forEach>
								<br/>
							</c:if>
						
						</dl>
						
					</div>
					
				</div>
			
			</c:otherwise>
			
		</c:choose>
			
	</jsp:body>

</l:page>