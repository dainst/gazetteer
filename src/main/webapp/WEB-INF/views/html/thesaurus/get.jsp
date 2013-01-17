<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib tagdir="/WEB-INF/tags/layout" prefix="l"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page import="java.util.*, org.dainst.gazetteer.domain.Thesaurus"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<l:page title="${thesaurus.title}">

	<jsp:attribute name="subtitle">
		
	</jsp:attribute>

	<jsp:body>
	
		<div class="row-fluid">
		
			<div class="span7 well">
				<div id="thesaurusTree" style="height:500px; overflow: auto;"></div>
			</div>
			
			<div class="span5 well">
				<l:map height="500px"/>
			</div>
			
		</div>
		
		<script type="text/javascript">		

		$(function () {
			$("#thesaurusTree").bind("hover_node.jstree", function(event, data) {
				zoomToPlace($(data.rslt.obj[0]).data("id"));
			}).bind("dehover_node.jstree", function(event, data) {
				resetMarker();
			}).bind("select_node.jstree", function(event, data) {
				window.location.href = "${baseUri}app/#/show/" + $(data.rslt.obj[0]).data("id");
			}).jstree({ 
				"json_data" : {
					"data": [
						<c:forEach var="place" items="${places}" varStatus="status">
							{
								"data": {
									"title": "${place.prefName.title}",
									"attr": { "href": "${baseUri}app/#/show/${place.id}" }
								},
								"metadata": { id: "${place.id}" },
								"state" : "closed"
							}<c:if test="${status.count lt fn:length(places)}">,</c:if>
						</c:forEach>
					],
					"ajax": {
						"url": function(n) { 
							return "${baseUri}search?sort=prefName.title.sort&limit=10000"
									+ "&q=parent:" + n.data("id") + " AND thesauri:${thesaurus.key}"; 
						},
						"error":  function(data) {
							console.log("ERROR:");
							console.log(data);
						},
						"success": function(data) {
							var result = [];
							$(data.result).each(function(index, place) {
								result[index] = { 
									data: { 
										title: place.prefName.title,
										attr: { 
											href: "${baseUri}app/#/show/" + place.gazId
										}
									},
									metadata: { id: place.gazId }
								};
								result[index].state = "closed";
							});
							return result;
						}
					}
				},
				"themes" : {
					"theme" : "custom",
					"dots" : false,
					"icons" : false
				},
				"plugins" : [ "themes", "json_data", "ui" ]
			});
		});
		
		</script>
		
	</jsp:body>

</l:page>