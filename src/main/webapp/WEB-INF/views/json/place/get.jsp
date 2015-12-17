<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %><%@ taglib uri="http://www.springframework.org/tags" prefix="s" %><%@ page session="false" import="org.dainst.gazetteer.domain.*, org.dainst.gazetteer.converter.JsonPlaceSerializer, org.dainst.gazetteer.dao.*, java.util.List" %><%

response.setHeader("Content-Type", "application/json; charset=utf-8");
Place place = (Place) request.getAttribute("place");
List<Place> parents = (List<Place>) request.getAttribute("parents");
Boolean readAccess = (Boolean) request.getAttribute("readAccess");
Boolean editAccess = (Boolean) request.getAttribute("editAccess");
JsonPlaceSerializer serializer = (JsonPlaceSerializer) request.getAttribute("jsonPlaceSerializer");
String replacing = (String) request.getAttribute("replacing");

%><%= serializer.serialize(place, request, parents, readAccess, editAccess, replacing) %>
