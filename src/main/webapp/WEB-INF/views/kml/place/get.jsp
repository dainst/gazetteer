<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %><%@ taglib uri="http://www.springframework.org/tags" prefix="s"%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %><%@ page session="false" import="org.dainst.gazetteer.domain.*" %><% response.setHeader("Content-Type", "application/vnd.google-earth.kml+xml; charset=utf-8"); %><?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2">
	<c:if test="${accessStatus == 'READ' || accessStatus == 'LIMITED_READ' || accessStatus == 'EDIT'}">
		<Placemark id="place_${place.id}">
			<name><c:out value="${place.prefName.title}"/></name>
			<description>
				<![CDATA[
				<p><a href="${baseUri}place/${place.id}">${baseUri}place/${place.id}</a></p>
				
				<h2><s:message code="ui.otherFormats" text="ui.otherFormats"/></h2>
				<ul>
					<li><a href="${baseUri}doc/${place.id}.html" target="_blank">HTML</a></li>
					<li><a href="${baseUri}doc/${place.id}.json" target="_blank">JSON</a></li>
				</ul>
				
				<!-- names -->
				<h2><s:message code="domain.place.names"/></h2>
				<ul>
					<li>
						<em><s:message code="domain.place.prefName" text="domain.place.prefName"/>: </em>
						${place.prefName.title}
						<c:if test="${languages[place.prefName.language] != null}">
							<em>(${languages[place.prefName.language]})</em>
						</c:if>
					</li>
					<c:forEach var="placename" items="${place.names}">
						<li>
							${placename.title}
							<c:if test="${languages[placename.language] != null}">
								<em>(${languages[placename.language]})</em>
							</c:if>
						</li>
					</c:forEach>
				</ul>
				
				<!-- parent -->
				<c:if test="${parent != null}">
					<h2><s:message code="domain.place.parent" text="domain.place.parent" /></h2>
					<p>
						<a href="${baseUri}place/${parent.id}">${parent.prefName.title}
							<c:if test="${parent.types != null && !empty(parent.types)}">
										<em>(<s:message code="place.types.${parent.types.toArray()[0]}" text="${parent.types.toArray()[0]}"/>)</em>
									</c:if>
						</a>
					</p>
				</c:if>
				
				<!-- children -->
				<c:if test="${!empty(children)}">
					<h2><s:message code="domain.place.children" text="domain.place.children" /></h2>
					<p>
						<a href="${baseUri}search.kml?q=parent:${place.id}&limit=${fn:length(children)}">
							<s:message code="ui.numberOfPlaces" text="ui.numberOfPlaces" arguments="${fn:length(children)}" />
						</a>
					</p>
				</c:if>	
				
				<!-- related places -->
				<c:if test="${!empty(relatedPlaces)}">
					<h2><s:message code="domain.place.relatedPlaces" text="domain.place.relatedPlaces" /></h2>
					<ul>
						<c:forEach var="relatedPlace" items="${relatedPlaces}">
							<li>
								<a href="${baseUri}place/${relatedPlace.id}">${relatedPlace.prefName.title}
									<c:if test="${relatedPlace.types != null && !empty(relatedPlace.types)}">
										<em>(<s:message code="place.types.${relatedPlace.types.toArray()[0]}" text="${relatedPlace.types.toArray()[0]}"/>)</em>
									</c:if>
								</a>
							</li>
						</c:forEach>
					</ul>
				</c:if>
				
				<!-- locations -->
				<c:if test="${place.prefLocation != null && place.prefLocation.coordinates != null && fn:length(place.prefLocation.coordinates) > 0}">
					<h2><s:message code="domain.place.locations" text="domain.place.locations" /></h2>
					<ul>
						<li>
							<em><s:message code="domain.location.latitude" text="domain.location.latitude" />:</em> ${place.prefLocation.lat},
							<em><s:message code="domain.location.longitude" text="domain.location.longitude" />:</em> ${place.prefLocation.lng}
							(<em><s:message code="domain.location.confidence" text="domain.location.confidence" />:</em>
							<s:message code="confidence.${place.prefLocation.confidence}" text="${place.prefLocation.confidence}"/>)
						</li>
						<c:forEach var="location" items="${place.locations}">
							<li>
								<em><s:message code="domain.location.latitude" text="domain.location.latitude" />:</em> ${location.lat},
								<em><s:message code="domain.location.longitude" text="domain.location.longitude" />:</em> ${location.lng}
								(<em><s:message code="domain.location.confidence" text="domain.location.confidence" />:</em>
								<s:message code="confidence.${location.confidence}" text="${location.confidence}"/>)
							</li>
						</c:forEach>
					</ul>
				</c:if>
				
				<c:if test="${!empty(place.types)}">
					<h2><s:message code="domain.place.type" text="domain.place.type" /></h2>
					<ul>
						<c:forEach var="type" items="${place.types}">
							<li>
								<s:message code="place.types.${type}" text="${type}"/>
							</li>
						</c:forEach>
					</ul>
				</c:if>
				
				<c:if test="${not empty place.arachneId or not empty place.zenonId}">
					<h2><s:message code="ui.contexts" text="ui.contexts"/></h2>
					<ul>
						<c:if test="${not empty place.arachneId}">
							<li>
								<a href="http://arachne.uni-koeln.de/arachne/index.php?view[layout]=search_result_overview&view[category]=overview&search[constraints]=FS_OrtID:%22${place.arachneId}%22" target="_blank">
									<s:message code="ui.link.arachne" text="ui.link.arachne"/>
									<i class="icon-external-link"></i>
								</a>
							</li>
						</c:if>
						<c:if test="${not empty place.zenonId}">
							<li>
								<a href="http://zenon.dainst.org/#search?q=f999_1:${place.zenonId}" target="_blank">
									<s:message code="ui.link.zenon" text="ui.link.zenon"/>
									<i class="icon-external-link"></i>
								</a>
							</li>
						</c:if>
					</ul>
				</c:if>
				
				<c:if test="${!empty(place.identifiers)}">
					<h2><s:message code="domain.place.identifiers" text="domain.place.identifiers" /></h2>
					<ul>
						<c:forEach var="identifier" items="${place.identifiers}">
							<li>
								<em>${identifier.context}:</em> ${identifier.value}
							</li>
						</c:forEach>
					</ul>
				</c:if>
				
				<c:if test="${!empty(place.links)}">
					<h2><s:message code="domain.place.links" text="domain.place.links" /></h2>
					<ul>
						<c:forEach var="link" items="${place.links}">
							<li>
								<em>${link.predicate}:</em> <a href="${link.object}" target="_blank">${link.object}</a>
							</li>
						</c:forEach>
					</ul>
				</c:if>
				
				<c:if test="${!empty(place.comments)}">
					<h2><s:message code="domain.place.comments" text="domain.place.comments" /></h2>
					<ul>
						<c:forEach var="comment" items="${place.comments}">
							<li>${comment.text}</li>
						</c:forEach>
					</ul>
				</c:if>
				
				<c:if test="${!empty(place.tags)}">
					<h2><s:message code="domain.place.tags" text="domain.place.tags" /></h2>
					<ul>
						<c:forEach var="tag" items="${place.tags}">
							<li>
								${tag}
							</li>
						</c:forEach>
					</ul>
				</c:if>
				]]>
			</description>
			<MultiGeometry>
				<c:if test="${place.prefLocation != null && place.prefLocation.coordinates != null && fn:length(place.prefLocation.coordinates) > 0}">
					<Point>
						<coordinates><c:out value="${place.prefLocation.lng}" />,<c:out value="${place.prefLocation.lat}" /></coordinates>
					</Point>
				</c:if>
				<c:if test="${place.prefLocation != null && place.prefLocation.shape != null && place.prefLocation.shape.coordinates != null && !empty(place.prefLocation.shape.coordinates)}">
					<c:forEach var="polygon" items="${place.prefLocation.shape.coordinates}">
						<Polygon>
							<c:forEach var="ring" items="${polygon}" varStatus="ringStatus">
								<c:choose>
									<c:when test="${ringStatus.index == 0}">
										<outerBoundaryIs>
											<LinearRing>
												<coordinates>
													<c:forEach var="coordinates" items="${ring}">
														${coordinates[0]},${coordinates[1]}
													</c:forEach>
													<c:if test="${ring[0][0] != ring[fn:length(ring) - 1][0] || ring[0][1] != ring[fn:length(ring) - 1][1]}">
														${ring[0][0]},${ring[0][1]}
													</c:if>
												</coordinates>
											</LinearRing>
										</outerBoundaryIs>
									</c:when>
									<c:otherwise>
										<innerBoundaryIs>
											<LinearRing>
												<coordinates>
													<c:forEach var="coordinates" items="${ring}">
														${coordinates[0]},${coordinates[1]}
													</c:forEach>
													<c:if test="${ring[0][0] != ring[fn:length(ring) - 1][0] || ring[0][1] != ring[fn:length(ring) - 1][1]}">
														${ring[0][0]},${ring[0][1]}
													</c:if>
												</coordinates>
											</LinearRing>
										</innerBoundaryIs>
									</c:otherwise>
								</c:choose>
							</c:forEach>
						</Polygon>
					</c:forEach>
				</c:if>
			</MultiGeometry>	
		</Placemark>
	</c:if>
</kml>