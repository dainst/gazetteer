<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ page session="false" import="org.dainst.gazetteer.domain.*,
	java.util.List, java.util.Map,
	org.dainst.gazetteer.converter.JsonPlaceSerializer" %>

<% 

response.setHeader("Content-Type", "application/json; charset=utf-8"); 

List<Place> places = (List<Place>) request.getAttribute("places");
String baseUri = (String) request.getAttribute("baseUri");
Long hits = (Long) request.getAttribute("hits");

JsonPlaceSerializer serializer = new JsonPlaceSerializer(baseUri);

StringBuilder sb = new StringBuilder("{");
sb.append("\"total\": ").append(hits);
sb.append(", \"result\": [");
int i = 0;
for (Place place : places) {
	sb.append(serializer.serialize(place, 1));
	if(++i < places.size()) sb.append(",");
}
sb.append("]");

Map<String, List<String[]>> facets = (Map<String, List<String[]>>) request.getAttribute("facets");
i = 0;
if (facets != null) {
	sb.append(", \"facets\": {");
	for (Map.Entry<String,List<String[]>> facet : facets.entrySet()) {
		if (facet.getValue().size() == 0) {
			continue;
		}
		sb.append("\"").append(facet.getKey()).append("\": [");
		int j = 0;
		for (String[] entry : facet.getValue()) {
		    sb.append(String.format("{ \"label\": \"%s\", \"term\": \"%s\", \"count\": %s }", entry[0], entry[1], entry[2]));
		    if(++j < facet.getValue().size()) sb.append(",");
		}
		sb.append("]");
		if(++i < facets.size()) sb.append(",");
	}
	if (sb.charAt(sb.length()-1) == ',')
		sb.deleteCharAt(sb.length()-1);
	sb.append("}");
}

sb.append("}");

%>

<%= sb.toString() %>