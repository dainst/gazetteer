<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %><%@ taglib uri="http://www.springframework.org/tags" prefix="s" %><%@ page session="false" import="org.dainst.gazetteer.domain.*, org.dainst.gazetteer.converter.JsonPlaceSerializer, org.dainst.gazetteer.dao.*, java.util.List" %><%

response.setHeader("Content-Type", "application/json; charset=utf-8");
Place place = (Place) request.getAttribute("place");
List<Place> parents = (List<Place>) request.getAttribute("parents");
Boolean readAccess = (Boolean) request.getAttribute("readAccess");
Boolean editAccess = (Boolean) request.getAttribute("editAccess");
Boolean includeAccessInfo = request.getAttribute("includeAccessInfo") == null ? false : (Boolean) request.getAttribute("includeAccessInfo");
Boolean includeChangeHistory = request.getAttribute("includeChangeHistory") == null ? false : (Boolean) request.getAttribute("includeChangeHistory");
Boolean pretty = request.getAttribute("pretty") == null ? false : (Boolean) request.getAttribute("pretty");
String baseUri = (String) request.getAttribute("baseUri");

JsonPlaceSerializer serializer = new JsonPlaceSerializer(baseUri, pretty);
serializer.setIncludeAccessInfo(includeAccessInfo);
serializer.setIncludeChangeHistory(includeChangeHistory);

%><%= serializer.serialize(place, request, parents, readAccess, editAccess) %>
