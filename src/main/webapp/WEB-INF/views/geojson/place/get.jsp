<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %><%@ taglib uri="http://www.springframework.org/tags" prefix="s" %><%@ page session="false" import="org.dainst.gazetteer.domain.*, org.dainst.gazetteer.converter.JsonPlaceSerializer, org.dainst.gazetteer.dao.*, org.dainst.gazetteer.helpers.PlaceAccessService" %><%

response.setHeader("Content-Type", "application/json; charset=utf-8");
Place place = (Place) request.getAttribute("place");
PlaceAccessService.AccessStatus accessStatus = (PlaceAccessService.AccessStatus) request.getAttribute("accessStatus");
JsonPlaceSerializer serializer = (JsonPlaceSerializer) request.getAttribute("jsonPlaceSerializer");

%><%= serializer.serializeGeoJson(place, request, accessStatus) %>