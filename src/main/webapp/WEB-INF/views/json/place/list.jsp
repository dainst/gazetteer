<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ page session="false" import="org.dainst.gazetteer.domain.*,
	java.util.List,
	org.dainst.gazetteer.converter.JsonPlaceSerializer" %>

<% 

response.setHeader("Content-Type", "application/json; charset=utf-8"); 

List<Place> places = (List<Place>) request.getAttribute("places");
String baseUri = (String) request.getAttribute("baseUri");

JsonPlaceSerializer serializer = new JsonPlaceSerializer(baseUri);

StringBuilder sb = new StringBuilder("[");
int i = 0;
for(Place place : places) {
	sb.append(serializer.serialize(place));
	if(++i < places.size()) sb.append(",");
}
sb.append("]");

%>

<%= sb.toString() %>