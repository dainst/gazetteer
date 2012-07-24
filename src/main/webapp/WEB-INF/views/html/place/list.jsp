<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="l"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page session="false"%>

<l:page title="Result">

	<jsp:attribute name="menu">	
		<l:map places="${places}" height="500px"/>	
	</jsp:attribute>

	<jsp:body>	
		<table class="table table-striped">
			<thead>
				<tr>
					<td>#</td>
					<td><s:message code="domain.placename.title" text="Name"/></td>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="place" items="${places}">				
					<tr>
						<td>${place.id}</td>
						<td><a href="place/${place.id}">${fn:join(place.namesAsArray, " / ")}</a></td>					
					</tr>				
				</c:forEach>
			</tbody>		
		</table>		
	</jsp:body>

</l:page>