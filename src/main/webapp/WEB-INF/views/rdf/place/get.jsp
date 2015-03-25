<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%><%@ taglib uri="http://www.springframework.org/tags" prefix="s"%><%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %><%@ page session="false" import="org.dainst.gazetteer.domain.*" %><% response.setHeader("Content-Type", "application/vnd.google-earth.kml+xml; charset=utf-8"); %><?xml version="1.0" encoding="UTF-8"?>
<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:edm="http://www.europeana.eu/schemas/edm/"
	xmlns:wgs84_pos="http://www.w3.org/2003/01/geo/wgs84_pos#"
	xmlns:dcterms="http://purl.org/dc/terms/"
	xmlns:skos="http://www.w3.org/2004/02/skos/core#"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:owl ="http://www.w3.org/2002/07/owl#"
	xmlns:gaz_id="http://gazetteer.dainst.org/types/id#">
	
	<edm:Place rdf:about="${baseUri}place/${place.id}">
		<skos:prefLabel xml:lang="${langHelper.getLocaleForISO3Language(place.prefName.language).language}">${place.prefName.title}</skos:prefLabel>
		<c:forEach var="name" items="${place.names}">
			<skos:altLabel xml:lang="${langHelper.getLocaleForISO3Language(name.language).language}">${name.title}</skos:altLabel>
		</c:forEach>
		<c:if test="${place.prefLocation != null}">
			<wgs84_pos:lat>${place.prefLocation.lat}</wgs84_pos:lat>
			<wgs84_pos:long>${place.prefLocation.lng}</wgs84_pos:long>
		</c:if>
		<c:forEach var="identifier" items="${place.identifiers}">
			<dc:identifier>${identifier.context}:${identifier.value}</dc:identifier>
		</c:forEach>
		<c:forEach var="link" items="${place.links}">
			<${link.predicate} rdf:resource="${link.object}"/>
		</c:forEach>
		<dcterms:isPartOf rdf:resource="${baseUri}place/${place.parent}" />
	</edm:Place>

</rdf:RDF>