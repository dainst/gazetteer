<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ page session="false" import="org.dainst.gazetteer.domain.*,
	org.dainst.gazetteer.converter.JsonPlaceSerializer" %>

<%

response.setHeader("Content-Type", "application/json; charset=utf-8");
Place place = (Place) request.getAttribute("place");
String baseUri = (String) request.getAttribute("baseUri");

JsonPlaceSerializer serializer = new JsonPlaceSerializer(baseUri);

%>

<%= serializer.serialize(place) %>