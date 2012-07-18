<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="l"%>
<%@ page session="false"%>

<l:page title="Result">

	<jsp:body> 

		<c:forEach var="place" items="${places}">
		
			<div class="row-fluid">
			
				<div class="span12">
		
				<c:set var="title" value="${place.names[0].title}" />
				<c:forEach var="name" items="${place.names}" begin="1" end="3">
					<c:set var="title" value="${title} / ${name.title}" />
				</c:forEach>
		
				<h4><a href="place/${place.id}">${title}</a></h4>
				</div>
			
			</div>
		
		</c:forEach>
		
	</jsp:body>

</l:page>