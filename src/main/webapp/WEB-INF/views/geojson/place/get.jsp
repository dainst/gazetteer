<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %><%@ taglib uri="http://www.springframework.org/tags" prefix="s" %><%@ page session="false" import="org.dainst.gazetteer.domain.*, org.dainst.gazetteer.converter.JsonPlaceSerializer, org.dainst.gazetteer.dao.*" %><%

response.setHeader("Content-Type", "application/json; charset=utf-8");
Place place = (Place) request.getAttribute("place");
Boolean readAccess = (Boolean) request.getAttribute("readAccess");
Boolean editAccess = (Boolean) request.getAttribute("editAccess");
Boolean pretty = request.getAttribute("pretty") == null ? false : (Boolean) request.getAttribute("pretty");
String baseUri = (String) request.getAttribute("baseUri");
UserRepository userDao = (UserRepository) request.getAttribute("userDao");
PlaceChangeRecordRepository changeRecordDao = (PlaceChangeRecordRepository) request.getAttribute("changeRecordDao");
RecordGroupRepository groupDao = (RecordGroupRepository) request.getAttribute("groupDao");

JsonPlaceSerializer serializer = new JsonPlaceSerializer(baseUri, pretty);
serializer.setUserDao(userDao);
serializer.setChangeRecordDao(changeRecordDao);
serializer.setGroupDao(groupDao);

%><%= serializer.serializeGeoJson(place, request, readAccess, editAccess) %>