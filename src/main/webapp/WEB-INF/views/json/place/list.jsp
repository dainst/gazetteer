<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %><%@ taglib uri="http://www.springframework.org/tags" prefix="s" %><%@ page session="false" import="org.dainst.gazetteer.domain.*, java.util.List, java.util.ArrayList, java.util.Map, org.dainst.gazetteer.converter.JsonPlaceSerializer, org.dainst.gazetteer.dao.*" %><% 

response.setHeader("Content-Type", "application/json; charset=utf-8"); 

List<Place> places = (List<Place>) request.getAttribute("places");
Map<String, List<Place>> parents = (Map<String, List<Place>>) request.getAttribute("parents");
Map<String, Boolean> readAccessMap = (Map<String, Boolean>) request.getAttribute("readAccessMap");
Map<String, Boolean> editAccessMap = (Map<String, Boolean>) request.getAttribute("editAccessMap");
Boolean includeAccessInfo = request.getAttribute("includeAccessInfo") == null ? false : (Boolean) request.getAttribute("includeAccessInfo");
Boolean pretty = request.getAttribute("pretty") == null ? false : (Boolean) request.getAttribute("pretty");
String baseUri = (String) request.getAttribute("baseUri");
Long hits = (Long) request.getAttribute("hits");
String queryId = (String) request.getAttribute("queryId");

JsonPlaceSerializer serializer = new JsonPlaceSerializer(baseUri, pretty);
serializer.setIncludeAccessInfo(includeAccessInfo);

StringBuilder sb = new StringBuilder("{");
sb.append("\n\"total\": ").append(hits);
sb.append(",\n\"result\": [\n");
int numberOfPlaces = 0;
List<String> accessGrantedPlaces = new ArrayList<String>();
List<String> accessDeniedPlaces = new ArrayList<String>();
for (Place place : places) {
	List<Place> placeParents = null;
	if (parents != null)
		placeParents = parents.get(place.getId());
	
	boolean readAccess = readAccessMap.get(place.getId());
	boolean editAccess = editAccessMap.get(place.getId());

	String serializedPlace = serializer.serialize(place, placeParents, readAccess, editAccess);
	if (serializedPlace != null) {
		if (readAccess)
			accessGrantedPlaces.add(serializedPlace);
		else
			accessDeniedPlaces.add(serializedPlace);
		numberOfPlaces++;
	}	
}
int appendedPlaces = 0;
for (String serializedPlace : accessGrantedPlaces) {
	sb.append(serializedPlace);
	appendedPlaces++;
	if (numberOfPlaces > appendedPlaces)
		sb.append(",\n");
}
for (String serializedPlace : accessDeniedPlaces) {
	sb.append(serializedPlace);
	appendedPlaces++;
	if (numberOfPlaces > appendedPlaces)
		sb.append(",\n");
}
sb.append("]");

Map<String, List<String[]>> facets = (Map<String, List<String[]>>) request.getAttribute("facets");
int i = 0;
if (facets != null) {
	sb.append(",\n\"facets\": {");
	for (Map.Entry<String,List<String[]>> facet : facets.entrySet()) {
		if (facet.getValue().size() == 0) {
			continue;
		}
		sb.append("\n  \"").append(facet.getKey()).append("\": [");
		int j = 0;
		for (String[] entry : facet.getValue()) {
		    sb.append(String.format("\n    { \"label\": \"%s\", \"term\": \"%s\", \"count\": %s }", entry[0], entry[1], entry[2]));
		    if(++j < facet.getValue().size()) sb.append(",");
		}
		sb.append("\n  ]");
		if(++i < facets.size()) sb.append(",");
	}
	if (sb.charAt(sb.length()-1) == ',')
		sb.deleteCharAt(sb.length()-1);
	sb.append("}");
}

if (queryId != null)
	sb.append(", \"queryId\": \"" + queryId + "\"");

sb.append("\n}");

%><%= sb.toString() %>