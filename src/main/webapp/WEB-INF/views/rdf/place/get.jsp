<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page session="false" import="org.dainst.gazetteer.domain.*" %>
<%@ page trimDirectiveWhitespaces="true"%>
<%@ page contentType="text/xml" %>
<% response.setHeader("Content-Type", "application/vnd.google-earth.kml+xml; charset=utf-8"); %>
<?xml version="1.0" encoding="UTF-8"?>
<rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:crm="http://www.cidoc-crm.org/rdfs/cidoc-crm#"
	xmlns:wgs84_pos="http://www.w3.org/2003/01/geo/wgs84_pos#"
	xmlns:dcterms="http://purl.org/dc/terms/"
	xmlns:skos="http://www.w3.org/2004/02/skos/core#"
	xmlns:dc="http://purl.org/dc/elements/1.1/"
>
<c:if test="${accessStatus == 'READ' || accessStatus == 'LIMITED_READ' || accessStatus == 'EDIT'}">
<crm:E53_Place rdf:about="${baseUri}place/${place.id}">
	<c:choose>
		<c:when test="${place.prefName.language != null}">
	<skos:prefLabel xml:lang="${langHelper.getLocaleForISO3Language(prefLanguage).getLanguage()}">${place.prefName.title}</skos:prefLabel>
		</c:when>
		<c:otherwise>
	<skos:prefLabel>${place.prefName.title}</skos:prefLabel>
		</c:otherwise>
	</c:choose>
	<c:forEach var="name" items="${place.names}">
		<c:choose>
			<c:when test="${name.language != null}">
	<skos:prefLabel xml:lang="${langHelper.getLocaleForISO3Language(name.language).getLanguage()}">${name.title}</skos:prefLabel>
			</c:when>
			<c:otherwise>
	<skos:altLabel>${name.title}</skos:altLabel>
			</c:otherwise>
		</c:choose>
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
</crm:E53_Place>
</c:if>
</rdf:RDF>