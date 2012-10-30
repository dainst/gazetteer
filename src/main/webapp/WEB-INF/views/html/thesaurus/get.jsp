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
			$("#thesaurusTree").bind("loaded.jstree", function (event, data) {
				console.log("loaded tree");
		    }).jstree({ 
				"json_data" : {
					"data": [
						<c:forEach var="place" items="${places}" varStatus="status">
							{
								"data": {
									"title": "${fn:join(place.namesAsArray, " / ")}",
									"attr": { "href": "${baseUri}place/${place.id}" }
								},
								"metadata": { id: "${place.id}" },
								"state" : "closed"
							}<c:if test="${status.count lt fn:length(places)}">,</c:if>
						</c:forEach>
					],
					"ajax": {
						"url": function(n) { return "${baseUri}/place?limit=10000&q=parent:" + n.data("id"); },
						"error":  function(data) {
							console.log("ERROR:");
							console.log(data);
						},
						"success": function(data) {
							console.log(data);
							var result = [];
							$(data).each(function(index, place) {
								result[index] = { 
									data: { 
										title: place.names[0].title,
										attr: { href: "${baseUri}place/" + place.gazId }
									},
									metadata: { id: place.gazId }
								};
								if (place.children) result[index].state = "closed";
							});
							console.log(result);
							return result;
						}
					}
				},
				"themes" : {
					"theme" : "custom",
					"dots" : false,
					"icons" : false
				},
				"plugins" : [ "themes", "json_data" ]
			});
		});
		
		</script>
		
	</jsp:body>

</l:page>