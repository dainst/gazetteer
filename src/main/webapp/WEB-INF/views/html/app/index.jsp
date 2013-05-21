<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<!doctype html>
<html ng-app="gazetteer">
<head>
<title>iDAI.gazetteer</title>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="http://arachne.uni-koeln.de/archaeostrap/assets/css/bootstrap.css" rel="stylesheet">
<link href="//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.1/css/bootstrap-responsive.min.css" rel="stylesheet">
<link href="//netdna.bootstrapcdn.com/font-awesome/3.0.2/css/font-awesome.css" rel="stylesheet">
<link href="//netdna.bootstrapcdn.com/font-awesome/3.0.2/css/font-awesome-ie7.css" rel="stylesheet">
<link href="../resources/css/angular-ui.css" rel="stylesheet">
<link href="../resources/css/app.css" rel="stylesheet">
<script	src="//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.0.5/angular.min.js"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.0.5/angular-resource.min.js"></script>
<script src="../resources/js/lib/angular/angular-ui.js"></script>
</head>
<body class="ng-cloak" ng-controller="AppCtrl">

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
					<a class="brand" href="../">iDAI.gazetteer</a>
					<div class="nav-collapse pull-left">
						<ul class="nav">
							<li><a href="#/thesaurus"><s:message
										code="ui.thesaurus.list" text="ui.thesaurus.list" /></a></li>
							<li><a href="#/extended-search"> <s:message
										code="ui.search.extendedSearch" text="ui.search.extendedSearch" />
							</a></li>
							<li><a href="#/edit/"> <s:message
										code="ui.place.create" text="ui.place.create" />
							</a></li>
						</ul>
						<form novalidate class="navbar-search pull-left" ng-submit="submit()">
							<s:message code="ui.search.simpleSearch" text="Einfache Suche"
								var="titleSimpleSearch" />
							<input type="text" class="search-query" ng-model="q"
								placeholder="${titleSimpleSearch}"> <i class="icon-search"></i>
						</form>
					</div>
				</div>
			</div>
		</div>
	</div>
	
	<div class="container">
	
		<div class="alerts" ng-cloak ng-hide="alerts.length == 0">
			<div ng-repeat="alert in alerts" class="alert" ng-class="alert.alertClass">
				<button type="button" class="close" ng-click="alerts.splice($index,1)">&times;</button>
				<h4 ng-show="alert.head">{{alert.head}}</h4>
				{{alert.body}}
			</div>
		</div>
	
		<!-- Page title -->
		<div class="page-header">
			<h2>
				<span ng-show="title">{{title}}</span>
				<span ng-hide="title">&nbsp;</span>
				<small ng-bind-html-unsafe="subtitle"></small>
			</h2>
		</div>
		
		<div class="row-fluid">
		
			<div class="span5" id="map-well-wrapper">
				<div class="well" id="map-well"">
					<div gaz-map places="activePlaces" height="500" zoom="zoom" bbox="bbox" highlight="highlight"></div>
				</div>
			</div>
			
			<div ng-view class="span7">
			
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
	
	<script src='//maps.google.com/maps/api/js?key=${googleMapsApiKey}&amp;sensor=false&libraries=visualization'></script>
	
	<script src='../resources/js/lib/jquery.locationpicker.js'></script>
	<script src="../resources/bootstrap/js/bootstrap.min.js"></script>
	
	<script src="../resources/js/custom.js"></script>
	<script src="../widget/lib.js"></script>
	
	<script src="../resources/js/app.js"></script>
	<script src="../resources/js/services.js"></script>
	<script src="../resources/js/controllers.js"></script>
	<script src="../resources/js/filters.js"></script>
	<script src="../resources/js/directives.js"></script>
	<script src="../resources/js/i18n/messages_${language}.js"></script>
	
</body>
</html>
