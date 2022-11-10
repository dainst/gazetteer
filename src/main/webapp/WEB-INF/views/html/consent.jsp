<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="gaz" tagdir="/WEB-INF/tags/layout" %>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>iDAI.gazetteer</title>

<link rel="icon"
	href="<%=request.getContextPath()%>/resources/ico/favicon.ico"
	type="image/x-icon">
<link rel="apple-touch-icon" sizes="144x144"
	href="<%=request.getContextPath()%>/resources/ico/apple-touch-icon-144.png">
<link rel="apple-touch-icon" sizes="114x114"
	href="<%=request.getContextPath()%>/resources/ico/apple-touch-icon-114.png">
<link rel="apple-touch-icon" sizes="72x72"
	href="<%=request.getContextPath()%>/resources/ico/apple-touch-icon-72.png">
<link rel="apple-touch-icon"
	href="<%=request.getContextPath()%>/resources/ico/apple-touch-icon-57.png">
<link
	href="//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.1/css/bootstrap-responsive.min.css"
	rel="stylesheet">
<link
	href="<%=request.getContextPath()%>/resources/archaeostrap/css/bootstrap.css"
	rel="stylesheet">
<link
	href="<%=request.getContextPath()%>/resources/font-awesome/css/font-awesome.min.css"
	rel="stylesheet">
<link href="<%=request.getContextPath()%>/resources/css/angular-ui.css"
	rel="stylesheet">
<link href="<%=request.getContextPath()%>/resources/css/app.css"
	rel="stylesheet">
</head>

<body>

	<div class="container-fluid">

		<div class="archaeo-fixed-menu">

			<div class="gaz-container archaeo-fixed-menu-header">
				<div id="gaz-logo"></div>
				<div>
					<h3 class="pull-left">
						<small>Deutsches Archäologisches Institut</small> <br> <a
							href="#!/home" style="color: inherit">iDAI.gazetteer</a>
					</h3>
				</div>
			</div>
		</div>
	</div>
	<div class="container">
		<div class="jumbotron">
			<c:choose>
				<c:when test="${language eq 'ar'}">
					<h1 dir="rtl" lang="ar" class="display-4"><s:message code="consent.heading" text="consent.heading" /></h1>
					<p  dir="rtl" lang="ar" class="lead"><s:message code="consent.text" text="consent.text" /></p>
					<hr class="my-4">
					<p dir="rtl" lang="ar">
						<form action="<%=request.getContextPath()%>/consent">
							<input type="hidden" id="location-input" name="redirectTo" /> 
							<input style="float: right;" class="btn btn-primary" type="submit" value="<s:message code="ok" text="ok" />" />
						</form>
					</p>
				</c:when>
				<c:otherwise>
					<h1 class="display-4"><s:message code="consent.heading" text="consent.heading" /></h1>
					<p class="lead"><s:message code="consent.text" text="consent.text" /></p>
					<hr class="my-4">
					<p>
						<form action="<%=request.getContextPath()%>/consent">
							<input type="hidden" id="location-input" name="redirectTo" /> 
							<input class="btn btn-primary" type="submit" value="<s:message code="ui.ok" text="OK" />" />
						</form>
					</p>
				</c:otherwise>
			</c:choose>
		</div>
	</div>
		
	<script>
		var input = document.getElementById("location-input");
		input.value = window.location.href;
	</script>
</body>

</html>
