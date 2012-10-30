<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ page session="false" import="org.dainst.gazetteer.domain.*,
	com.fasterxml.jackson.databind.ObjectMapper,
	com.fasterxml.jackson.databind.node.ArrayNode,
	com.fasterxml.jackson.databind.node.ObjectNode,
	java.util.List" %>

<% 

response.setHeader("Content-Type", "application/json; charset=utf-8"); 

List<Place> places = (List<Place>) request.getAttribute("places");
String baseUri = (String) request.getAttribute("baseUri");

ObjectMapper mapper = new ObjectMapper();
ArrayNode placesNode = mapper.createArrayNode();

for (Place place : places) {
	
	ObjectNode placeNode = mapper.createObjectNode();
	placeNode.put("@id", baseUri + "place/" + place.getId());
	placeNode.put("gazId", place.getId());
	
	if (!place.getChildren().isEmpty()) {
		ArrayNode childrenNode = mapper.createArrayNode();
		for (String childId : place.getChildren()) {
			childrenNode.add(baseUri + "place/" + childId);
		}
		placeNode.put("children", childrenNode);
	}
	
	ArrayNode namesNode = mapper.createArrayNode();
	for (PlaceName name : place.getNames()) {
		ObjectNode nameNode = mapper.createObjectNode();
		nameNode.put("title", name.getTitle());
		if (name.getLanguage() != null) nameNode.put("language", name.getLanguage());
		namesNode.add(nameNode);
	}
	placeNode.put("names", namesNode);
	
	placesNode.add(placeNode);
	
}

%>

<%= mapper.writeValueAsString(placesNode) %>