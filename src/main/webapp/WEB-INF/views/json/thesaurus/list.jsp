<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ page session="false" import="org.dainst.gazetteer.domain.*,
	java.util.List,
	org.dainst.gazetteer.converter.JsonPlaceSerializer" %>

<% 

response.setHeader("Content-Type", "application/json; charset=utf-8"); 

List<Thesaurus> thesauri = (List<Thesaurus>) request.getAttribute("thesauri");
String baseUri = (String) request.getAttribute("baseUri");

StringBuilder sb = new StringBuilder("[");
int i = 0;
for (Thesaurus thesaurus : thesauri) {	
	sb.append(String.format("{ \"key\": \"%s\", \"title\": \"%s\", \"description\": \"%s\"}", 
			thesaurus.getKey(), thesaurus.getTitle(), thesaurus.getDescription()));
	if(++i < thesauri.size()) sb.append(",");
}
sb.append("]");

%>

<%= sb.toString() %>