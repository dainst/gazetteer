<%@ tag description="page layout" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ attribute name="title" required="true" type="java.lang.String"%>
<%@ attribute name="subtitle" type="java.lang.String"%>

<s:url var="searchAction" value="/app/#search" />

<!DOCTYPE HTML>
<html>
<head>
<title>iDAI.gazetteer</title>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link href="//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.1/css/bootstrap-combined.no-icons.min.css" rel="stylesheet">
<link href="//netdna.bootstrapcdn.com/font-awesome/3.0.2/css/font-awesome.css" rel="stylesheet">
<link href="//netdna.bootstrapcdn.com/font-awesome/3.0.2/css/font-awesome-ie7.css" rel="stylesheet">
<link href="resources/css/app.css" rel="stylesheet">
<script src="//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
<script src="resources/js/custom.js"></script>
<script src='resources/js/lib/jquery.jstree.js'></script>
<script src='resources/bootstrap/js/bootstrap.min.js'></script>
</head>
<body>

<!-- Top Navigation Bar -->
<div class="navbar navbar-fixed-top navbar-inverse">
	<div class="navbar-inner">
		<div class="container-fluid">
			<a class="btn btn-navbar" data-toggle="collapse"
				data-target=".nav-collapse"> <span class="icon-bar"></span> <span
				class="icon-bar"></span> <span class="icon-bar"></span>
			</a> <a class="brand" href="./">iDAI.gazetteer</a>
			<div class="nav-collapse">
				<ul class="nav">
					<li><a href="thesaurus"><s:message code="ui.thesaurus.list" text="ui.thesaurus.list"/></a></li>
					<li>
						<a href="app/#/extended-search">
							<s:message code="ui.search.extendedSearch" text="ui.search.extendedSearch"/>
						</a>
					</li>
					<li>
						<a href="app/#/edit/">
							<s:message code="ui.place.create" text="ui.place.create"/>
						</a>
					</li>
				</ul>
			</div><!--/.nav-collapse -->
			<form:form class="navbar-search pull-left simpleSearchForm" action="${searchAction}" method="GET">
				<s:message code="ui.search.simpleSearch" text="Einfache Suche" var="titleSimpleSearch"/>
 				<input type="text" class="search-query" placeholder="${titleSimpleSearch}" name="q">
 				<i class="icon-search icon-white"></i>
			</form:form>
		</div>
	</div>
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
		<p>&copy; Deutsches Arch�ologisches Institut ${year}</p>
	</footer>
	
</div>

</body>
</html>