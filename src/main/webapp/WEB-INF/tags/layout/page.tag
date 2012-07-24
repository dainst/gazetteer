<%@ tag description="page layout" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ attribute name="title" required="true" type="java.lang.String"%>
<%@ attribute name="subtitle" type="java.lang.String"%>
<%@ attribute name="menu" fragment="true" %>

<!DOCTYPE HTML>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>iDAI.Gazetteer - ${title}</title>
<link href="/gazetteer/resources/bootstrap/css/bootstrap.css" rel="stylesheet">
<link href="/gazetteer/resources/bootstrap/css/bootstrap-responsive.css" rel="stylesheet">
<style type="text/css">
      body {
        padding-top: 60px;
        padding-bottom: 40px;
      }
      .sidebar-nav {
        padding: 9px 0;
      }
    </style>
</head>
<body>

<!-- Navigation Bar -->
<div class="navbar navbar-fixed-top">
	<div class="navbar-inner">
		<div class="container-fluid">
			<a class="btn btn-navbar" data-toggle="collapse"
				data-target=".nav-collapse"> <span class="icon-bar"></span> <span
				class="icon-bar"></span> <span class="icon-bar"></span>
			</a> <a class="brand" href="#">iDAI.Gazetteer</a>
			<div class="btn-group pull-right">
				<a class="btn dropdown-toggle" data-toggle="dropdown" href="#"> <i
					class="icon-user"></i> Username <span class="caret"></span>
				</a>
				<ul class="dropdown-menu">
					<li><a href="#">Profile</a></li>
					<li class="divider"></li>
					<li><a href="#">Sign Out</a></li>
				</ul>
			</div>
			<div class="nav-collapse">
				<ul class="nav">
					<li class="active"><a href="#">Home</a></li>
					<li><a href="#about">About</a></li>
					<li><a href="#contact">Contact</a></li>
				</ul>
			</div><!--/.nav-collapse -->
			<form class="navbar-search pull-left" action="/gazetteer/place">
 				<input type="text" class="search-query" placeholder="Search" name="q">
			</form>
		</div>
	</div>
</div>

<div class="container-fluid">

	<div class="row-fluid">
	
		<!-- Menu -->
		<div class="span5">
			<div class="well">
				<jsp:invoke fragment="menu" />
			</div>
		</div>
		
		<!-- Body -->
		<div class="span7">
			<!-- Page title -->
			<div class="page-header">
				<h1>
					${title}
					<small>${subtitle}</small>
				</h1>
			</div>
			<jsp:doBody />
		</div>
		
	</div>
	
	<!-- Footer -->
	<hr>
	<footer>
		<p>&copy; Deutsches Archäologisches Institut 2012</p>
	</footer>
	
</div>

</body>
</html>