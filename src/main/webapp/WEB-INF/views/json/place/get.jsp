<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ page session="false" import="org.dainst.gazetteer.domain.*,
	org.dainst.gazetteer.converter.JsonPlaceSerializer,
	org.dainst.gazetteer.dao.*" %>

<%

response.setHeader("Content-Type", "application/json; charset=utf-8");
Place place = (Place) request.getAttribute("place");
String baseUri = (String) request.getAttribute("baseUri");
UserRepository userDao = (UserRepository) request.getAttribute("userDao");
PlaceChangeRecordRepository changeRecordDao = (PlaceChangeRecordRepository) request.getAttribute("changeRecordDao");

JsonPlaceSerializer serializer = new JsonPlaceSerializer(baseUri);

%>

<%= serializer.serialize(place, userDao, changeRecordDao, request) %>
