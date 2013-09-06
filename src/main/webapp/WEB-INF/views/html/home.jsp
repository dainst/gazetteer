<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<s:url var="searchAction" value="app/#!/search" />

<!doctype html>
<html>
<head>
<title>iDAI.gazetteer</title>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="google-site-verification" content="axehIuQKDs9bKUYzUl7hj1IvFMePho1--MppShoNQWk" />
<link rel="icon" href="resources/ico/favicon.ico">
<link rel="apple-touch-icon" sizes="144x144" href="resources/ico/apple-touch-icon-144.png">
<link rel="apple-touch-icon" sizes="114x114" href="resources/ico/apple-touch-icon-114.png">
<link rel="apple-touch-icon" sizes="72x72" href="resources/ico/apple-touch-icon-72.png">
<link rel="apple-touch-icon" href="resources/ico/apple-touch-icon-57.png">
<link href="http://arachne.uni-koeln.de/archaeostrap/assets/css/bootstrap.css" rel="stylesheet">
<link href="//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.1/css/bootstrap-responsive.min.css" rel="stylesheet">
<link href="//netdna.bootstrapcdn.com/font-awesome/3.0.2/css/font-awesome.css" rel="stylesheet">
<link href="//netdna.bootstrapcdn.com/font-awesome/3.0.2/css/font-awesome-ie7.css" rel="stylesheet">
<link href="resources/css/app.css" rel="stylesheet">
<script	src="//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>	
<script	src="http://arachne.uni-koeln.de/archaeostrap/assets/js/bootstrap.js"></script>	
<script src='//maps.google.com/maps/api/js?key=${googleMapsApiKey}&amp;sensor=false&libraries=visualization'></script>
<script src="resources/js/custom.js"></script>
</head>
<body>

	<div class="archaeo-fixed-menu">
		<div class="container archaeo-fixed-menu-header">
			<sec:authorize access="isAnonymous()">
				<div class="btn-group pull-right" style="margin-top:12px">
					<a href="login" class="btn btn-small btn-primary">
						<s:message code="ui.login" text="ui.login"/>
					</a>
				</div>
			</sec:authorize>
			<sec:authorize access="isAuthenticated()">
				<div class="btn-group pull-right" style="margin-top:12px">
					<p class="btn btn-small">
						<s:message code="ui.loggedInAs" text="ui.loggedInAs"/>: <sec:authentication property="principal.username" />
					</p>
					<a href="logout" class="btn btn-small btn-primary">
						<s:message code="ui.logout" text="ui.logout"/>
					</a>
				</div>
			</sec:authorize>
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
							<sec:authorize access="hasRole('ROLE_USER')">
								<li><a href="app/#!/edit/"> <s:message
											code="ui.place.create" text="ui.place.create" />
								</a></li>
							</sec:authorize>							
							<sec:authorize access="hasRole('ROLE_REISESTIPENDIUM')">
								<li><a href="app/#!/search?q=%7B%22bool%22:%7B%22must%22:%5B%7B%22query_string%22:%7B%22query%22:%22_exists_:noteReisestipendium%22%7D%7D%5D%7D%7D&type=extended"> <s:message
											code="ui.search.reisestipendium" text="ui.search.reisestipendium" />
								</a></li>
							</sec:authorize>
						</ul>
					</div>
					<!--/.nav-collapse -->
				</div>
			</div>
		</div>
	</div>
	
	<div class="container">

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
		
		<div class="row-fluid" style="margin-top:-220px">
			<div class="span12">
				 <p class="lead">Der DAI-Gazetteer ist ein Webservice, der Ortsnamen mit Koordinaten verbindet und in zwei Richtungen wirken soll. Nach innen dient er als Normdatenvokabular für sämtliche ortsbezogenen Informationen und Informationssysteme des DAI. Nach außen soll er diese mit den weltweiten Gazetteer-Systemen verbinden. Weitere Funktionen sind in einem <a href="http://youtu.be/mISUGMFkQvU" target="_blank">Screencast</a> zusammengefasst.</p>
			</div>
		</div>
		
		<div class="row-fluid"">
			<div class="span6">
	            <p>Der DAI-Gazetteer ist außerdem ein Werkzeug, um die Ortsdaten-Struktur innerhalb des DAI sukzessive zu optimieren, d. h. sowohl die Zahl der mit Ortsdaten versehenen Informationsobjekte zu erhöhen, diese dann in die weltweiten Ortsdatensysteme enizubinden, und auch die im DAI schon vorhandenen Informationsobjekte mit Ortsdaten zu vereinheitlichen. Der DAI-Gazetteer ist somit der Auftakt zu einerm grossen, neuen Querschnitts-Arbeitsfeld.</p>
	            <p>Geodaten sind ein hinreichend vereinbarungsfähiges, aber auch umfassend genug anwendbares Kontextualisierungskriterium. Ihre Bedeutung für die Kontextualisierung nimmmt zu, wenn über die bidirektionale Verknüpfung hinaus eine Drei- oder Vielecksverknküpfung zustande kommt. Daher ist der Gazetteer u. a. auch eine Kontextualisierungsmaschine, die ortsbasierte Suchen über mehrere Informationssysteme hinweg erlaubt, etwa über <a href="http://arachne.uni-koeln.de" target="_blank">Arachne</a> und <a href="http://opac.dainst.org" target="_blank">ZENON</a>.</p>
			</div>
			<iframe height="315" class="span6" src="http://www.youtube.com/embed/mISUGMFkQvU" frameborder="0" allowfullscreen></iframe>
		</div>
		
		<!-- Footer -->
		<hr>
		<footer>
			<jsp:useBean id="now" class="java.util.Date" />
			<fmt:formatDate var="year" value="${now}" pattern="yyyy" />
			<p>&copy; Deutsches Archäologisches Institut ${year}</p>
		</footer>
		
	</div>
	
	<script type="application/javascript">
	
		$('#affix-menu').affix({ offset: {top: 176} });
	
		var map_canvas = document.getElementById('map_canvas');
	
		map = new google.maps.Map(map_canvas, {
			center: new google.maps.LatLng(20,0),
			zoom: 2,
			mapTypeId: google.maps.MapTypeId.ROADMAP,
			zoomControlOptions: {
				style: google.maps.ZoomControlStyle.SMALL
			},
			styles: [
         	{
        	    "featureType": "administrative",
        	    "stylers": [
        	      { "visibility": "off" }
        	    ]
        	  },{
        	    "featureType": "landscape",
        	    "stylers": [
        	      { "visibility": "simplified" },
        	      { "saturation": -100 },
        	      { "lightness": -31 }
        	    ]
        	  },{
        	    "featureType": "water",
        	    "stylers": [
        	      { "saturation": -100 },
        	      { "lightness": 32 }
        	    ]
        	  },{
        	    "featureType": "road",
        	    "stylers": [
        	      { "visibility": "off" }
        	    ]
        	  },{
        	    "featureType": "poi",
        	    "stylers": [
        	      { "visibility": "off" }
        	    ]
        	  },{
        	    "elementType": "labels"  }
        	]
		});

		var heatmapData = [
			<c:forEach var="place" items="${places}" varStatus="status"> new google.maps.LatLng(${place.prefLocation.lat}, ${place.prefLocation.lng})<c:if test="${status.count lt fn:length(places)}">,</c:if></c:forEach>
		];
		
		var heatmap = new google.maps.visualization.HeatmapLayer({
            data: heatmapData,
            opacity: 0.8,
            maxIntensity: 10,
            radius: 3,
            gradient: ['transparent', '#5283d2', '#ffffff']
    	});
		heatmap.setMap(map);
		
		map_canvas.style.height = "400px";
		map_canvas.style.width = "100%";
		google.maps.event.trigger(map, 'resize');
	
	</script>
	
</body>
</html>
