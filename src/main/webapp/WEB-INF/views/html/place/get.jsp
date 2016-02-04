<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<s:url var="searchAction" value="app/#!/search" />

<!doctype html>
<html>
<head>
<title>
	<c:choose>
		<c:when test="${accessStatus == 'READ' || accessStatus == 'LIMITED_READ' || accessStatus == 'EDIT'}">
			iDAI.gazetteer - ${place.prefName.title}
			<c:forEach var="placename" items="${place.names}"> / ${placename.title}</c:forEach>
		</c:when>
		<c:otherwise>
			iDAI.gazetteer - <s:message code="domain.place.hiddenPlace" text="domain.place.hiddenPlace"/>
		</c:otherwise>
	</c:choose>
</title>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="//arachne.uni-koeln.de/archaeostrap/assets/css/bootstrap.css" rel="stylesheet">
<link href="//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.1/css/bootstrap-responsive.min.css" rel="stylesheet">
<link href="//netdna.bootstrapcdn.com/font-awesome/3.0.2/css/font-awesome.css" rel="stylesheet">
<link href="//netdna.bootstrapcdn.com/font-awesome/3.0.2/css/font-awesome-ie7.css" rel="stylesheet">
<link href="../resources/css/app.css" rel="stylesheet">
<script	src="//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>	
<script	src="//arachne.uni-koeln.de/archaeostrap/assets/js/bootstrap.js"></script>	
<script src='//maps.google.com/maps/api/js?key=${googleMapsApiKey}&amp;sensor=false&libraries=visualization'></script>
<script src="../resources/js/custom.js"></script>
<link rel="alternate" type="application/rdf+xml" href="${baseUri}doc/${place.id}.rdf">
<link rel="alternate" type="application/json" href="${baseUri}doc/${place.id}.json">
<link rel="alternate" type="application/vnd.google-earth.kml+xml" href="${baseUri}doc/${place.id}.kml">
</head>
<body>

	<div class="archaeo-fixed-menu">
		<div class="container archaeo-fixed-menu-header">
			<div id="archaeo-fixed-menu-logo"></div>
			<h3 class="pull-left">
				<small>Deutsches Archäologisches Institut</small> <br>
				<a href="../" style="color:inherit">iDAI.gazetteer</a>
			</h3>
		</div>
		<div class="affix-menu-wrapper">
			<div id="affix-menu" style="z-index: 100000"
				class="navbar navbar-inverse container" data-spy="affix">
				<div class="navbar-inner">
					<div id="archaeo-fixed-menu-icon"></div>
					<a class="btn btn-navbar" data-toggle="collapse"
						data-target=".nav-collapse"> <span class="icon-bar"></span> <span
						class="icon-bar"></span> <span class="icon-bar"></span>
					</a>
					<a class="brand" href="">iDAI.gazetteer</a>
					<div class="nav-collapse pull-left">
						<ul class="nav">
							<li><a href="app/#!/thesaurus"><s:message
										code="ui.thesaurus.list" text="ui.thesaurus.list" /></a></li>
							<li><a href="app/#!/extended-search"> <s:message
										code="ui.search.extendedSearch" text="ui.search.extendedSearch" />
							</a></li>
							<li><a href="app/#!/create/"> <s:message
										code="ui.place.create" text="ui.place.create" />
							</a></li>
						</ul>
					<form novalidate class="navbar-search pull-right simpleSearchForm"
						action="${searchAction}">
						<s:message code="ui.search.simpleSearch" text="Einfache Suche"
							var="titleSimpleSearch" />
						<input type="text" class="search-query" name="q"
							placeholder="${titleSimpleSearch}"> <i class="icon-search"></i>
					</form>
					</div>
					<!--/.nav-collapse -->
				</div>
			</div>
		</div>
	</div>
	
	<div class="container" itemscope itemtype="http://schema.org/Place">
	
	<!-- Page title -->
		<div class="page-header">
			<h2>
				<c:choose>
					<c:when test="${accessStatus == 'READ' || accessStatus == 'EDIT'}">
						<span itemprop="name">${place.prefName.title}</span>
					</c:when>
					<c:otherwise>
						<s:message code="domain.place.hiddenPlace" text="domain.place.hiddenPlace"/>
					</c:otherwise>
				</c:choose>
				
				<small><a href="${baseUri}place/${place.id}" itemprop="url">${baseUri}place/${place.id}</a> <a data-toggle="modal" href="#copyUriModal"><i class="icon-share" style="font-size:0.7em"></i></a></small>
			</h2>
		</div>

		<s:message code="ui.copyToClipboard" var="copyMsg"/>
					
		<div class="modal hide" id="copyUriModal">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal">×</button>
				<h3><s:message code="ui.copyUriToClipboardHeading"/></h3>
			</div>
			<div class="modal-body">
				<label>${copyMsg}</label>
				<input class="input-xxlarge" style="width:97%" type="text" value="${baseUri}place/${place.id}" id="copyUriInput"></input>
			</div>
		</div>
		<script type="text/javascript">
			$("#copyUriModal").on("shown",function() {
				$("#copyUriInput").focus().select();
			});
		</script>
		
		<h2><s:message code="ui.otherFormats" text="ui.otherFormats"/></h2>
			<ul>
				<li><a href="${baseUri}doc/${place.id}.rdf" target="_blank">RDF/XML</a></li>
				<li><a href="${baseUri}doc/${place.id}.json" target="_blank">JSON</a></li>
				<li><a href="${baseUri}doc/${place.id}.geojson" target="_blank">GeoJSON</a></li>
				<li><a href="${baseUri}doc/${place.id}.kml" target="_blank">KML</a></li>
			</ul>
			
			<c:if test="${accessStatus == 'READ' || accessStatus == 'EDIT'}">
		
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
						<a href="${baseUri}place/${parent.id}" itemprop="containedIn">${parent.prefName.title}
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
				<c:if test="${place.prefLocation != null}">
					<h2><s:message code="domain.place.locations" text="domain.place.locations" /></h2>
					<ul>
						<li itemprop="geo" itemscope itemtype="http://schema.org/GeoCoordinates">
							<em><s:message code="domain.location.latitude" text="domain.location.latitude" />:</em> ${place.prefLocation.lat},
							<em><s:message code="domain.location.longitude" text="domain.location.longitude" />:</em> ${place.prefLocation.lng}
							(<em><s:message code="domain.location.confidence" text="domain.location.confidence" />:</em>
							<s:message code="confidence.${place.prefLocation.confidence}" text="${place.prefLocation.confidence}"/>)
							<meta itemprop="latitude" content="${place.prefLocation.lat}" />
						    <meta itemprop="longitude" content="${place.prefLocation.lng}" />
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
				
				<c:if test="${place.types != null && !empty(place.types)}">
					<h2><s:message code="domain.place.type" text="domain.place.type" /></h2>
					<ul>
						<c:forEach var="type" items="${place.types}">
							<li><s:message code="place.types.${type}" text="${type}"/></li>
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
			
			</c:if>
		
		<!-- Footer -->
		<hr>
		<footer>
			<jsp:useBean id="now" class="java.util.Date" />
			<fmt:formatDate var="year" value="${now}" pattern="yyyy" />
			<p>&copy; Deutsches Archäologisches Institut ${year}</p>
		</footer>
		
	</div>
	
</body>
</html>
