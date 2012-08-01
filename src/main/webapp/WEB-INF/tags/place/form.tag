<%@ tag description="place form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ attribute name="place" type="org.dainst.gazetteer.domain.Place" %>

<c:choose>
	<c:when test="${place == null}">
		<c:set var="method" value="POST" />
		<c:set var="action" value="" />
	</c:when>
	<c:otherwise>
		<c:set var="method" value="PUT" />
		<c:set var="action" value="${place.id}" />
	</c:otherwise>
</c:choose>

<div id="place-form-div">

	<form:form method="${method}" modelAttribute="place" class="form-horizontal" id="place-form">
		<c:if test="${place != null}">
			<input type="hidden" name="uri" value="${baseUri}place/${place.id}"/>
		</c:if>
		<fieldset>
		
			<div class="control-group">
				<label class="control-label">
					<s:message code="domain.place.type" text="Typ" />
				</label>
				<div class="controls">
					<form:input path="type" class="input-xlarge" />
				</div>
			</div>
		
			<!-- place names -->
			<h3><s:message code="domain.place.names" text="Ortsnamen" /></h3>
			<c:forEach var="name" items="${place.names}" varStatus="loopStatus">
				<div class="control-group">
					<label class="control-label">
						<s:message code="domain.placename.title" text="Titel" />
					</label>
					<div class="controls">
						<form:input path="names[${loopStatus.index}].title" class="input-xlarge" />
						<form:input path="names[${loopStatus.index}].language" class="input-small" />
					</div>
				</div>
			</c:forEach>
			
			<!-- locations -->
			<h3><s:message code="domain.place.locations" text="Lage" /></h3>
			<c:forEach var="location" items="${place.locations}" varStatus="loopStatus">
				<div class="control-group">
					<label class="control-label">
						<s:message code="domain.location.coordinates" text="Koordinaten" />
					</label>
					<div class="controls">
						<form:input path="locations[${loopStatus.index}].lng" class="input-small" />
						<form:input path="locations[${loopStatus.index}].lat" class="input-small" />
					</div>
				</div>
			</c:forEach>
			
			<!-- parent -->
			<h3><s:message code="domain.place.parent" text="Übergeordneter Ort" /></h3>
			<div class="control-group">
				<label class="control-label">
					<s:message code="domain.place" text="Ort" />
				</label>
				<div class="controls">
					<form:input path="parent" class="input-small" />
				</div>
			</div>
			
			<!-- children -->
			<h3><s:message code="domain.place.children" text="Untergeordnete Orte" /></h3>
			<c:forEach var="child" items="${place.children}" varStatus="loopStatus">
				<label class="control-label">
					<s:message code="domain.place" text="Ort" />
				</label>
				<div class="controls">
					<form:input path="child.id" class="input-small" />
				</div>
			</c:forEach>
			
			
		</fieldset>
		<div class="form-actions">
            <button type="submit" class="save btn btn-primary"><s:message code="ui.save" text="Speichern"/></button>
            <button class="btn"><s:message code="ui.cancel" text="Abbrechen"/></button>
        </div>
	</form:form>

</div>

<script type="text/javascript">

$("#place-form").submit(function(event) {
	
	event.preventDefault(); 
	var form = $(this);
	
	var place = {
		"names": [],
		"locations": []
	};
	
	var uri = form.find('input[name="uri"]').val();
	if (uri) place["@id"] = uri;
	
	var inputs = form.find('fieldset input');
	inputs.each(function(i, input) {
		var name = $(input).attr("name");
		if (name.indexOf("[") != -1) {
			var index = name.substring(name.indexOf("[")+1,name.indexOf("]"));
			var obj = name.substring(0,name.indexOf("["));
			var field = name.substring(name.indexOf(".")+1);
			if ($(input).val()) {
				if (place[obj][index] == undefined) place[obj][index] = {};
				if (field === "lng") {
					if (place[obj][index]["coordinates"] == undefined)
						place[obj][index]["coordinates"] = [];
					place[obj][index]["coordinates"][0] = $(input).val();
				} else if (field === "lat") {
					if (place[obj][index]["coordinates"] == undefined)
						place[obj][index]["coordinates"] = [];
					place[obj][index]["coordinates"][1] = $(input).val();
				} else {
					place[obj][index][field] = $(input).val();
				}
			}
		} else {
			if ($(input).val())	place[name] = $(input).val();
		}
	});
	
	$.ajax({
		type: "PUT",
		url: "${action}",
		contentType: "application/json",
		data: JSON.stringify(place)
	}).done(function(data) {
		console.log("success!");
		console.log(data);
	});
	
});

</script>
