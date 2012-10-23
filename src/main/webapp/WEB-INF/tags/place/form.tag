<%@ tag description="place form" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@ attribute name="place" type="org.dainst.gazetteer.domain.Place" %>
<%@ attribute name="parentPlace" type="org.dainst.gazetteer.domain.Place" %>
<%@ attribute name="children" type="java.util.List" %>
<%@ attribute name="relatedPlaces" type="java.util.List" %>
<%@ attribute name="languages" type="java.util.Map" %>

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
					<s:message code="domain.place.type" text="domain.place.type" />
				</label>
				<div class="controls">
					<form:input path="type" class="input-xlarge" />
				</div>
			</div>
		
			<!-- place names -->
			<h3><s:message code="domain.place.names" text="domain.place.names" /></h3>
			<c:forEach var="name" items="${place.names}" varStatus="loopStatus">
				<div class="control-group">
					<label class="control-label">
						<s:message code="domain.placename.title" text="domain.placename.title" />
					</label>
					<div class="controls">
						<form:input path="names[${loopStatus.index}].title" class="input-xlarge" />
						<s:message code="ui.language.notSpecified" var="langNotSpecified" />
						<form:select path="names[${loopStatus.index}].language">
							<form:option value="" label="${langNotSpecified}" />
							<form:options items="${languages}" />
						</form:select>
						<div class="btn btn-danger minus">-</div>
					</div>
				</div>
			</c:forEach>
			<div class="control-group">
				<label class="control-label">
					<s:message code="domain.placename.title" text="domain.placename.title" />
				</label>
				<div class="controls">
					<input type="text" name="names[].title" class="input-xlarge disabled" disabled />
					<select name="names[].language" class="disabled" disabled>
						<option value="">${langNotSpecified}</option>
						<c:forEach var="lang" items="${languages}">
							<option value="${lang.key}">${lang.value}</option> 
						</c:forEach>
					</select>
					<div class="btn btn-primary plus">+</div>
				</div>
			</div>
			
			<!-- locations -->
			<h3><s:message code="domain.place.locations" text="domain.place.locations" /></h3>
			<c:forEach var="location" items="${place.locations}" varStatus="loopStatus">
				<div class="control-group">
					<label class="control-label">
						<s:message code="domain.location.coordinates" text="domain.location.coordinates" />
					</label>
					<div class="controls">
						<div class="input-append">
							<c:set var="coordinates">${location.lat},${location.lng}</c:set>
							<input type="text" name="locations[${loopStatus.index}].coordinates" value="${coordinates}" class="lnglat"><button class="picker-search-button btn" type="button">
								<i class="icon-map-marker"></i>
							</button>
						</div>
						<form:select path="locations[${loopStatus.index}].confidence">
							<form:option value="1" label="1" />
							<form:option value="2" label="2" />
							<form:option value="3" label="3" />
						</form:select>		
						<div class="btn btn-danger minus">-</div>
					</div>
				</div>
			</c:forEach>
			<div class="control-group">
				<label class="control-label">
					<s:message code="domain.location.coordinates" text="domain.location.coordinates" />
				</label>
				<div class="controls">
					<div class="input-append">
						<input type="text" name="locations[${loopStatus.index}].coordinates" class="lnglat disabled" disabled><button class="picker-search-button btn disabled" disabled type="button">
							<i class="icon-map-marker"></i>
						</button>
					</div>
					<select name="locations[].confidence" class="disabled" disabled>
						<option value="1" label="1" />
						<option value="2" label="2" />
						<option value="3" label="3" />
					</select>
					<div class="btn btn-primary plus">+</div>
				</div>
			</div>
			
			<!-- identifiers -->
			<h3><s:message code="domain.place.identifiers" text="domain.place.identifiers" /></h3>
			<c:forEach var="identifier" items="${place.identifiers}" varStatus="loopStatus">
				<div class="control-group">
					<label class="control-label">
						<s:message code="domain.identifier.value" text="domain.identifier.value" />
					</label>
					<div class="controls">
						<form:input path="identifiers[${loopStatus.index}].value" class="input-large" />
						<s:message code="domain.identifier.context" text="domain.identifier.context" />
						<form:input path="identifiers[${loopStatus.index}].context" class="input-small" />
						<div class="btn btn-danger minus">-</div>
					</div>
				</div>
			</c:forEach>
			<div class="control-group">
				<label class="control-label">
					<s:message code="domain.identifier.value" text="domain.identifier.value" />
				</label>
				<div class="controls">
					<input type="text" name="identifiers[].value" class="input-large disabled" disabled>
					<s:message code="domain.identifier.context" text="domain.identifier.context" />
					<input type="text" name="identifiers[].context" class="input-small disabled" disabled>
					<div class="btn btn-primary plus">+</div>
				</div>
			</div>
			
			<!-- comments -->
			<h3><s:message code="domain.place.comments" text="domain.place.comments" /></h3>
			<c:forEach var="comment" items="${place.comments}" varStatus="loopStatus">
				<div class="control-group">
					<label class="control-label">
						<s:message code="domain.comment.text" text="domain.comment.text" />
					</label>
					<div class="controls">
						<form:textarea path="comments[${loopStatus.index}].text" class="input-xlarge" />
						<s:message code="ui.language.notSpecified" var="langNotSpecified" />
						<form:select path="comments[${loopStatus.index}].language">
							<form:option value="" label="${langNotSpecified}" />
							<form:options items="${languages}" />
						</form:select>
						<div class="btn btn-danger minus">-</div>
					</div>
				</div>
			</c:forEach>
			<div class="control-group">
				<label class="control-label">
					<s:message code="domain.comment.text" text="domain.comment.text" />
				</label>
				<div class="controls">
					<textarea name="comments[].text" class="input-xlarge disabled" disabled></textarea>
					<select name="comments[].language" class="disabled" disabled>
						<option value="">${langNotSpecified}</option>
						<c:forEach var="lang" items="${languages}">
							<option value="${lang.key}">${lang.value}</option> 
						</c:forEach>
					</select>
					<div class="btn btn-primary plus">+</div>
				</div>
			</div>
			
			<!-- tags -->
			<h3><s:message code="domain.place.tags" text="domain.place.tags" /></h3>
			<c:forEach var="tag" items="${place.tags}" varStatus="loopStatus">
				<div class="control-group">
					<label class="control-label">
						<s:message code="domain.tag.text" text="domain.tag.text" />
					</label>
					<div class="controls">
						<form:input path="tags[${loopStatus.index}].text" class="input-xlarge" />
						<s:message code="ui.language.notSpecified" var="langNotSpecified" />
						<form:select path="tags[${loopStatus.index}].language">
							<form:option value="" label="${langNotSpecified}" />
							<form:options items="${languages}" />
						</form:select>
						<div class="btn btn-danger minus">-</div>
					</div>
				</div>
			</c:forEach>
			<div class="control-group">
				<label class="control-label">
					<s:message code="domain.tag.text" text="domain.tag.text" />
				</label>
				<div class="controls">
					<input type="text" name="tags[].text" class="input-xlarge disabled" disabled>
					<select name="tags[].language" class="disabled" disabled>
						<option value="">${langNotSpecified}</option>
						<c:forEach var="lang" items="${languages}">
							<option value="${lang.key}">${lang.value}</option> 
						</c:forEach>
					</select>
					<div class="btn btn-primary plus">+</div>
				</div>
			</div>
			
			<!-- parent -->
			<h3><s:message code="domain.place.parent" text="Übergeordneter Ort" /></h3>
			<div class="control-group">
				<label class="control-label">
					<s:message code="domain.place" text="domain.place" />
				</label>
				<div class="controls">
					<c:choose>
						<c:when test="${parentPlace.id}">
							<gaz:pick name="parent" id="parent" class="input-xlarge" value="${baseUri}place/${parentPlace.id}" returnType="uri"></gaz:pick>
						</c:when>
						<c:otherwise>
							<gaz:pick name="parent" id="parent" class="input-xlarge" value="" returnType="uri"></gaz:pick>
						</c:otherwise>
					</c:choose>
				</div>
			</div>
			
			<!-- children -->
			<h3><s:message code="domain.place.children" text="domain.place.children" /></h3>
			<c:forEach var="child" items="${children}" varStatus="loopStatus">
				<div class="control-group">
					<label class="control-label">
						<s:message code="domain.place" text="domain.place" />
					</label>
					<div class="controls">
						<gaz:pick name="children[${loopStatus.index}]" value="${baseUri}place/${child.id}" class="input-xlarge" returnType="uri"></gaz:pick>
						<div class="btn btn-danger minus">-</div>
					</div>
				</div>
			</c:forEach>
			<div class="control-group">
				<label class="control-label">
					<s:message code="domain.place" text="domain.place" />
				</label>
				<div class="controls">
					<gaz:pick name="children[]" name="children[]" class="input-xlarge disabled" disabled="true" returnType="uri"></gaz:pick>
					<div class="btn btn-primary plus">+</div>
				</div>
			</div>
			
			<!-- related places -->
			<h3><s:message code="domain.place.relatedPlaces" text="domain.place.relatedPlaces" /></h3>
			<c:forEach var="relatedPlace" items="${relatedPlaces}" varStatus="loopStatus">
				<div class="control-group">
					<label class="control-label">
						<s:message code="domain.place" text="domain.place" />
					</label>
					<div class="controls">
						<gaz:pick name="relatedPlaces[${loopStatus.index}]" value="${baseUri}place/${relatedPlace.id}" class="input-xlarge" returnType="uri"></gaz:pick>
						<div class="btn btn-danger minus">-</div>
					</div>
				</div>
			</c:forEach>
			<div class="control-group">
				<label class="control-label">
					<s:message code="domain.place" text="domain.place" />
				</label>
				<div class="controls">
					<gaz:pick name="relatedPlaces[]" name="relatedPlaces[]" class="input-xlarge disabled" disabled="true" returnType="uri"></gaz:pick>
					<div class="btn btn-primary plus">+</div>
				</div>
			</div>
			
			
		</fieldset>
		<div class="form-actions">
            <button type="submit" class="save btn btn-primary"><s:message code="ui.save" text="Speichern"/></button>
            <a class="btn" href="${baseUri}place/${place.id}?limit=${limit}&offset=${offset}&q=${q}&view=${view}"><s:message code="ui.cancel" text="Abbrechen"/></a>
        </div>
	</form:form>

</div>

<s:message code="ui.success" text="Änderungen gespeichert" var="successMsg"/>
<s:message code="ui.failure" text="Fehler" var="failureMsg"/>

<script type="text/javascript">

$("#place-form .minus").click(function() {
	$(this).closest(".control-group").slideUp("normal", function() { $(this).remove(); });
});

$("#place-form .plus").click(function() {
	var oldGroup = $(this).closest(".control-group");
	var newGroup = oldGroup.clone(true).hide();
	oldGroup.before(newGroup);
	newGroup.find(".plus").toggleClass("plus minus btn-primary btn-danger").unbind().html("-").click(function() {
		$(this).closest(".control-group").slideUp("normal", function() { $(this).remove(); });
	});
	newGroup.find("input, select, button, textarea").toggleClass("disabled").removeAttr("disabled");
	var indices = {
		"names": 0,
		"locations": 0,
		"identifiers": 0,
		"comments": 0,
		"tags": 0,
		"children": 0,
		"relatedPlaces": 0
	};
	$("#place-form .control-group").each(function() {
		var name = $(this).find("input, textarea").first().attr("name");
		if (name.indexOf("[") == -1) return true;
		var obj = name.substring(0,name.indexOf("["));
		$(this).find("input:not(.disabled), select:not(.disabled), textarea:not(.disabled)").each(function() {
			$(this).attr("name",$(this).attr("name").replace(/\[.?\]/,"["+indices[obj].toString()+"]"));
		});
		indices[obj]++;
	});
	newGroup.slideDown();
	$('input.lnglat').locationPicker();
});

$("#place-form").submit(function(event) {
	
	event.preventDefault(); 
	var form = $(this);
	
	var place = {
		"names": [],
		"locations": [],
		"identifiers": [],
		"comments": [],
		"tags": [],
		"children": [],
		"relatedPlaces": []
	};
	
	var uri = form.find('input[name="uri"]').val();
	if (uri) place["@id"] = uri;
	
	var inputs = form.find('fieldset input:not(.disabled), fieldset select:not(.disabled), fieldset textarea:not(.disabled)');
	inputs.each(function(i, input) {
		var name = $(input).attr("name");
		if (name.indexOf("[") != -1) {
			var index = name.substring(name.indexOf("[")+1,name.indexOf("]"));
			var obj = name.substring(0,name.indexOf("["));
			var field = name.substring(name.indexOf(".")+1);
			if ($(input).val()) {
				if (place[obj][index] == undefined) place[obj][index] = {};
				if (field === "coordinates") {
					place[obj][index]["coordinates"] = $(input).val().split(",");
				} else if (field == name) {
					place[obj][index] = $(input).val();
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
		dataType: "json",
		data: JSON.stringify(place)
	}).done(function() {
		$("#place-form-div").prepend("<div class='alert alert-success'><button type='button' class='close' data-dismiss='alert'>×</button><strong>${successMsg}!</strong></div>");
	}).fail(function(jqXHR) {
		var data = $.parseJSON(jqXHR.responseText);
		$("#place-form-div").prepend("<div class='alert alert-error'><button type='button' class='close' data-dismiss='alert'>×</button><strong>${failureMsg}!</strong> Message: "+data.message+"</div>");
	});
	
});

</script>
