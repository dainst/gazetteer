<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %><%@ taglib uri="http://www.springframework.org/tags" prefix="s" %><%@ page session="false" import="org.dainst.gazetteer.domain.*, java.util.List, java.util.ArrayList, java.util.Map, org.dainst.gazetteer.converter.JsonPlaceSerializer, org.dainst.gazetteer.dao.*" %><% 

response.setHeader("Content-Type", "application/json; charset=utf-8"); 

List<Place> places = (List<Place>) request.getAttribute("places");
Map<String, Boolean> readAccessMap = (Map<String, Boolean>) request.getAttribute("readAccessMap");
Map<String, Boolean> editAccessMap = (Map<String, Boolean>) request.getAttribute("editAccessMap");
Boolean pretty = request.getAttribute("pretty") == null ? false : (Boolean) request.getAttribute("pretty");
String baseUri = (String) request.getAttribute("baseUri");
RecordGroupRepository groupDao = (RecordGroupRepository) request.getAttribute("groupDao");

JsonPlaceSerializer serializer = new JsonPlaceSerializer(baseUri, pretty);
serializer.setGroupDao(groupDao);

StringBuilder sb = new StringBuilder("{\n");
sb.append("  \"type\": \"FeatureCollection\",\n  \"features\": [\n");
int numberOfPlaces = 0;
List<String> accessGrantedPlaces = new ArrayList<String>();
List<String> accessDeniedPlaces = new ArrayList<String>();
for (Place place : places) {
	boolean readAccess = readAccessMap.get(place.getId());
	boolean editAccess = editAccessMap.get(place.getId());

	String serializedPlace = serializer.serializeGeoJson(place, request, readAccess, editAccess);
	if (serializedPlace != null) {
		serializedPlace = "    " + serializedPlace.replace("\n", "\n    ");
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
sb.append("\n  ]\n}");

%><%= sb.toString() %>