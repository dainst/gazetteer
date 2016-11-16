<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<!doctype html>

<html>
	<head>
		<title>iDAI.gazetteer | Validation Error</title>
		<meta charset="utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
	</head>
	
	<body>
		<!-- Page title -->
		<div class="page-header">
			<h2>Validation error</h2>
		</div>
		
		<div>${result.message}</div>

		<!-- Footer -->
		<hr>
		<footer>
			<jsp:useBean id="now" class="java.util.Date" />
			<fmt:formatDate var="year" value="${now}" pattern="yyyy" />
			<p>&copy; Deutsches Archäologisches Institut ${year}</p>
		</footer>
	</body>
</html>
