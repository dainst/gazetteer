<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<!doctype html>
<html ng-app="gazetteer">
<head>
<meta charset="utf-8">
<title>iDAI.gazetteer</title>
<link rel="stylesheet" href="../resources/css/app.css" />
<link href="../resources/bootstrap/css/bootstrap.css" rel="stylesheet">
<link href="../resources/bootstrap/css/bootstrap-responsive.css" rel="stylesheet">
</head>
<body>
<!-- Top Navigation Bar -->
<div class="navbar navbar-fixed-top navbar-inverse">
	<div class="navbar-inner">
		<div class="container-fluid">
			<a class="btn btn-navbar" data-toggle="collapse"
				data-target=".nav-collapse"> <span class="icon-bar"></span> <span
				class="icon-bar"></span> <span class="icon-bar"></span>
			</a> <a class="brand" href="/gazetteer">iDAI.gazetteer</a>
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
					<li><a href="/gazetteer">Home</a></li>
					<li><a href="#about">About</a></li>
					<li><a href="#contact">Contact</a></li>
					<li id="extendedSearchBtn">
						<a href="#">
							<s:message code="ui.search.extendedSearch" text="Erweiterte Suche"/>
							<i class="icon-circle-arrow-down icon-white"></i>
						</a>
					</li>
				</ul>
			</div><!--/.nav-collapse -->
			<form novalidate class="navbar-search pull-left" ng-submit="submit()" ng-controller="SearchBoxCtrl">
				<s:message code="ui.search.simpleSearch" text="Einfache Suche" var="titleSimpleSearch"/>
 				<input type="text" class="search-query" placeholder="${titleSimpleSearch}" ng-model="q">
 				<i class="icon-search icon-white"></i>
			</form>
		</div>
	</div>
</div>

<div id="extendedSearchDiv">
	<form:form class="form-inline" action="${searchAction}" method="GET">
		<input type="text" class="search-query input-large" name="q">
		<label class="checkbox">
			<input type="checkbox" name="fuzzy" value="true">
			<s:message code="ui.search.fuzzySearch" text="Unscharfe Suche" />
		</label>
		<button type="submit" class="btn"><s:message code="ui.search.submit" text="Suchen"/></button>
	</form:form>
</div>

<div class="container-fluid">

	<!-- Page title -->
	<div class="page-header">
		<h1>
			{{title}}
			<small>{{subtitle}}</small>
		</h1>
	</div>

	<div ng-view></div>
	
	<!-- Footer -->
	<hr>
	<footer>
		<jsp:useBean id="now" class="java.util.Date" />
		<fmt:formatDate var="year" value="${now}" pattern="yyyy" />
		<p>&copy; Deutsches Archäologisches Institut ${year}</p>
	</footer>
	
</div>

<!-- In production use:
 <script src="//ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min.js"></script>
 -->
<script src="../resources/js/lib/angular/angular.js"></script>
<script src="../resources/js/lib/angular/angular-resource.js"></script>

<script	src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
<script src='http://maps.google.com/maps/api/js?sensor=false'></script>
<script src='../resources/js/lib/jquery.locationpicker.js'></script>
<script src='../resources/js/lib/jquery.jstree.js'></script>
<script src="../resources/bootstrap/js/bootstrap-dropdown.js"></script>
<script src="../resources/bootstrap/js/bootstrap-alert.js"></script>
<script src="../resources/bootstrap/js/bootstrap-modal.js"></script>

<script src="../resources/js/custom.js"></script>
<script src="../widget/lib.js"></script>

<script src="../resources/js/app.js"></script>
<script src="../resources/js/services.js"></script>
<script src="../resources/js/controllers.js"></script>
<script src="../resources/js/filters.js"></script>
<script src="../resources/js/directives.js"></script>
	
</body>
</html>
