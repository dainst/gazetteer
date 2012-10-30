<%@ tag description="page layout" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ attribute name="title" required="true" type="java.lang.String"%>
<%@ attribute name="subtitle" type="java.lang.String"%>

<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>iDAI.gazetteer - ${title}</title>
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
<script type="text/javascript" src='http://maps.google.com/maps/api/js?sensor=false'></script>
<script src="/gazetteer/resources/js/custom.js"></script>
<script src="/gazetteer/widget/lib.js"></script>
<script src='/gazetteer/resources/js/jquery.locationpicker.js'></script>
<script src='/gazetteer/resources/js/jquery.jstree.js'></script>
<script src="/gazetteer/resources/bootstrap/js/bootstrap-dropdown.js"></script>
<script src="/gazetteer/resources/bootstrap/js/bootstrap-alert.js"></script>
<script src="/gazetteer/resources/bootstrap/js/bootstrap-modal.js"></script>
<link href="/gazetteer/resources/bootstrap/css/bootstrap.css" rel="stylesheet">
<link href="/gazetteer/resources/bootstrap/css/bootstrap-responsive.css" rel="stylesheet">
<link href="/gazetteer/resources/css/custom.css" rel="stylesheet">
<style type="text/css">
      body {
        padding-top: 60px;
        padding-bottom: 40px;
      }
    </style>
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
			<form class="navbar-search pull-left" action="/gazetteer/place">
				<s:message code="ui.search.simpleSearch" text="Einfache Suche" var="titleSimpleSearch"/>
 				<input type="text" class="search-query" placeholder="${titleSimpleSearch}" name="q">
 				<i class="icon-search icon-white"></i>
			</form>
		</div>
	</div>
</div>

<div id="extendedSearchDiv">
	<form class="form-inline" action="/gazetteer/place">
		<input type="text" class="search-query input-large" name="q">
		<label class="checkbox">
			<input type="checkbox" name="fuzzy" value="true">
			<s:message code="ui.search.fuzzySearch" text="Unscharfe Suche" />
		</label>
		<button type="submit" class="btn"><s:message code="ui.search.submit" text="Suchen"/></button>
	</form>
</div>

<div class="container-fluid">

	<!-- Page title -->
	<div class="page-header">
		<h1>
			${title}
			<small>${subtitle}</small>
		</h1>
	</div>

	<jsp:doBody />
	
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