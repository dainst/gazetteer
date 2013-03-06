<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<s:url var="searchAction" value="app/#search" />

<!doctype html>
<html>
<head>
<meta charset="utf-8">
<title>iDAI.gazetteer</title>
<link href="resources/bootstrap/css/bootstrap.css" rel="stylesheet">
<link href="resources/bootstrap/css/font-awesome.min.css" rel="stylesheet">		
<link rel="stylesheet" href="resources/css/app.css" />
<script	src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
</head>
<body>

	<!-- Top Navigation Bar -->
	<div class="navbar navbar-fixed-top navbar-inverse">
		<div class="navbar-inner">
			<div class="container-fluid">
				<a class="btn btn-navbar" data-toggle="collapse"
					data-target=".nav-collapse"> <span class="icon-bar"></span> <span
					class="icon-bar"></span> <span class="icon-bar"></span>
				</a> <a class="brand" href="">iDAI.gazetteer</a>
				<div class="nav-collapse pull-left">
					<ul class="nav">
						<li><a href="thesaurus"><s:message code="ui.thesaurus.list" text="ui.thesaurus.list"/></a></li>
						<li>
							<a href="app/#/extended-search">
								<s:message code="ui.search.extendedSearch" text="Erweiterte Suche"/>
							</a>
						</li>
					</ul>
				</div><!--/.nav-collapse -->
				<form novalidate class="navbar-search pull-left simpleSearchForm" action="${searchAction}">
					<s:message code="ui.search.simpleSearch" text="Einfache Suche" var="titleSimpleSearch"/>
	 				<input type="text" class="search-query" name="q" placeholder="${titleSimpleSearch}">
	 				<i class="icon-search icon-white"></i>
				</form>
			</div>
		</div>
	</div>	
	
	<div id="map_canvas"></div>		
	
	<div style="position:relative; top:-235px; z-index:10; text-align:center;">
		<h1 style="font-size: 60px; text-shadow: 0 1px 5px #000000; color:white; margin-bottom: 150px;">
			iDAI.gazetteer
		</h1>
		<form class="form-search simpleSearchForm" action="${searchAction}" style="margin:0;">
			<div class="well" style="display:inline-block; text-align:left;">
				<div class="input-append">
					<input class="search-query input-xxlarge" name="q" type="text" placeholder="${titleSimpleSearch}">
					<button class="btn btn-primary" type="submit"><i class="icon-search"></i></button>
				</div>
			</div>
		</form>
	</div>
	
	<div class="container-fluid" style="margin-top:-220px">
		
		<div class="row-fluid">
			<div class="span6">
				<p class="lead">Vivamus sagittis lacus vel augue laoreet rutrum faucibus dolor auctor. Duis mollis, est non commodo luctus.</p>
	            <p>Nullam quis risus eget urna mollis ornare vel eu leo. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Nullam id dolor id nibh ultricies vehicula.</p>
	            <p>Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec ullamcorper nulla non metus auctor fringilla. Duis mollis, est non commodo luctus, nisi erat porttitor ligula, eget lacinia odio sem nec elit. Donec ullamcorper nulla non metus auctor fringilla.</p>
	            <p>Maecenas sed diam eget risus varius blandit sit amet non magna. Donec id elit non mi porta gravida at eget metus. Duis mollis, est non commodo luctus, nisi erat porttitor ligula, eget lacinia odio sem nec elit.</p>
			</div>
			<div class="span6">
				<h3>Watch the screencast!</h3>
				<p><img src="resources/img/screencast.jpg"></p>
				<p><br/></p>
				<blockquote>
				  <p>Der Gazetteer hilft mir bei meiner Studienarbeit zu den archäologisch spannendsten Orten dieser Welt.</p>
				  <small>Fabian Zavodnik</small>
				</blockquote>
			</div>
		</div>
		
		<!-- Footer -->
		<hr>
		<footer>
			<jsp:useBean id="now" class="java.util.Date" />
			<fmt:formatDate var="year" value="${now}" pattern="yyyy" />
			<p>&copy; Deutsches Archäologisches Institut ${year}</p>
		</footer>
		
	</div>
	
	<script src='http://maps.google.com/maps/api/js?key=${googleMapsApiKey}&amp;sensor=false&libraries=visualization'></script>
	<script src='resources/js/lib/jquery.locationpicker.js'></script>
	<script src='resources/js/lib/jquery.jstree.js'></script>
	<script src="resources/bootstrap/js/bootstrap.min.js"></script>
	
	<script src="resources/js/custom.js"></script>
	<script type="application/javascript">
	
		var map_canvas = document.getElementById('map_canvas');
	
		map = new google.maps.Map(map_canvas, {
			center: new google.maps.LatLng(20,0),
			zoom: 2,
			mapTypeId: google.maps.MapTypeId.SATELLITE,
			zoomControlOptions: {
				style: google.maps.ZoomControlStyle.SMALL
			}
		});

		var heatmapData = [
			<c:forEach var="place" items="${places}" varStatus="status">
				new google.maps.LatLng(${place.prefLocation.lat}, ${place.prefLocation.lng})<c:if test="${status.count lt fn:length(places)}">,</c:if>        
			</c:forEach>
		];
		
		var heatmap = new google.maps.visualization.HeatmapLayer({
			data: heatmapData
		});
		heatmap.setMap(map);
		
		map_canvas.style.height = "400px";
		map_canvas.style.width = "100%";
		google.maps.event.trigger(map, 'resize');
	
	</script>
	
</body>
</html>
