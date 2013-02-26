<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<!doctype html>
<html ng-app="gazetteer">
<head>
<meta charset="utf-8">
<title>iDAI.gazetteer</title>
<link href="../resources/bootstrap/css/bootstrap.css" rel="stylesheet">
<link href="../resources/css/angular-ui.css" rel="stylesheet">
<link href="../resources/bootstrap/css/font-awesome.min.css" rel="stylesheet">
<link rel="stylesheet" href="../resources/css/app.css" />
<script	src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
<!-- In production use:
 <script src="//ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min.js"></script>
 -->
<script src="../resources/js/lib/angular/angular.js"></script>
<script src="../resources/js/lib/angular/angular-resource.js"></script>
<script src="../resources/js/lib/angular/angular-ui.js"></script>
</head>
<body class="ng-cloak" ng-controller="AppCtrl">

	<!-- Top Navigation Bar -->
	<div class="navbar navbar-fixed-top navbar-inverse">
		<div class="navbar-inner">
			<div class="container-fluid">
				<a class="btn btn-navbar" data-toggle="collapse"
					data-target=".nav-collapse"> <span class="icon-bar"></span> <span
					class="icon-bar"></span> <span class="icon-bar"></span>
				</a> <a class="brand" href="../">iDAI.gazetteer</a>
				<div class="btn-group pull-right">
					<a class="btn dropdown-toggle" data-toggle="dropdown" href="#">
						<i class="icon-user"></i> Username <span class="caret"></span>
					</a>
					<ul class="dropdown-menu">
						<li><a href="#">Profile</a></li>
						<li class="divider"></li>
						<li><a href="#">Sign Out</a></li>
					</ul>
				</div>
				<div class="nav-collapse">
					<ul class="nav">
						<li><a href="../thesaurus"><s:message code="ui.thesaurus.list" text="ui.thesaurus.list"/></a></li>
						<li>
							<a href="#extended-search">
								<s:message code="ui.search.extendedSearch" text="Erweiterte Suche"/>
							</a>
						</li>
					</ul>
				</div><!--/.nav-collapse -->
				<form novalidate class="navbar-search pull-left" ng-submit="submit()">
					<s:message code="ui.search.simpleSearch" text="Einfache Suche" var="titleSimpleSearch"/>
	 				<input type="text" class="search-query" placeholder="${titleSimpleSearch}" ng-model="q">
	 				<i class="icon-search icon-white"></i>
				</form>
				<img ng-show="loading > 0" src="../resources/img/loading48.gif" style="width:24px; height:24px; margin-top:8px;">
			</div>
		</div>
	</div>
	
	<div class="container-fluid">
	
		<div ng-hide="alerts.length == 0">
			<div ng-repeat="alert in alerts" class="alert" ng-class="alert.alertClass">
				<button type="button" class="close" ng-click="alerts.splice($index,1)">×</button>
				<h4 ng-show="alert.head">{{alert.head}}</h4>
				{{alert.body}}
			</div>
		</div>
	
		<!-- Page title -->
		<div class="page-header">
			<h2>
				<span class="ng-cloak">{{title}}</span>
				<small ng-bind-html-unsafe="subtitle"></small>
			</h2>
		</div>
		
		<div class="row-fluid">
		
			<div class="span5 well" ng-show="showMap">
				<div gaz-map places="activePlaces" height="500" zoom="zoom" bbox="bbox"></div>
			</div>
			
			<div ng-view ng-class="{ 'span7': showMap, 'span12 no-left-margin': !showMap }">
			
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
	
	<script src='http://maps.google.com/maps/api/js?key=${googleMapsApiKey}&amp;sensor=false'></script>
	<script src='../resources/js/lib/jquery.locationpicker.js'></script>
	<script src='../resources/js/lib/jquery.jstree.js'></script>
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
