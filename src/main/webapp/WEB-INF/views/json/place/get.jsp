<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %><%@ taglib uri="http://www.springframework.org/tags" prefix="s" %><%@ page session="false" import="org.dainst.gazetteer.domain.*, org.dainst.gazetteer.converter.JsonPlaceSerializer, org.dainst.gazetteer.dao.*, org.dainst.gazetteer.helpers.PlaceAccessService, java.util.List, java.util.Map" %><%

response.setHeader("Content-Type", "application/json; charset=utf-8");
Place place = (Place) request.getAttribute("place");
List<Place> parents = (List<Place>) request.getAttribute("parents");
PlaceAccessService.AccessStatus accessStatus = (PlaceAccessService.AccessStatus) request.getAttribute("accessStatus");
Map<String, PlaceAccessService.AccessStatus> parentAccessStatusMap = (Map<String, PlaceAccessService.AccessStatus>) request.getAttribute("parentAccessStatusMap");
JsonPlaceSerializer serializer = (JsonPlaceSerializer) request.getAttribute("jsonPlaceSerializer");
String replacing = (String) request.getAttribute("replacing");

%><%= serializer.serialize(place, request, parents, accessStatus, parentAccessStatusMap, replacing, false) %>
