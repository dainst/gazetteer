<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<s:message code="ui.language.notSpecified" text="ui.language.notSpecified" var="langNotSpecified" />

<div class="well">

	<form class="form-horizontal">
	
		<div class="control-group">
			<label class="control-label" for="inputMeta"> <s:message
					code="ui.extendedSearch.meta" text="ui.extendedSearch.meta" />
			</label>
			<div class="controls">
				<input type="text" class="input-xlarge" id="inputMeta">
			</div>
		</div>
		
		<div class="control-group">
			<label class="control-label" for="inputNames"> <s:message
					code="ui.extendedSearch.names" text="ui.extendedSearch.names" />
			</label>
			<div class="controls">
				<input type="text" class="input-xlarge" id="inputNames">
				<select ng-model="comment.language">
					<option value="">${langNotSpecified}</option>
					<c:forEach var="language" items="${languages}">
						<option value="${language.key}">${language.value}</option>
					</c:forEach>
				</select>
			</div>
		</div>
		
		<div class="control-group">
			<label class="control-label" for="inputParent"> <s:message
					code="ui.extendedSearch.parent" text="ui.extendedSearch.parent" />
			</label>
			<div class="controls">
				<input type="text" class="input-xlarge" id="inputParent">
			</div>
		</div>
		
		<div class="control-group">
			<label class="control-label" for="inputTypes"> <s:message
					code="ui.extendedSearch.types" text="ui.extendedSearch.types" />
			</label>
			<div class="controls">
				<input type="text" class="input-xlarge" id="inputTypes">
				<select ng-model="identifier.context">
					<c:forEach var="idType" items="${idTypes}">
						<option value="${idType}">${idType}</option>
					</c:forEach>
				</select>
			</div>
		</div>
		
		<div class="control-group">
			<label class="control-label" for="inputIDs"> <s:message
					code="ui.extendedSearch.ids" text="ui.extendedSearch.ids" />
			</label>
			<div class="controls">
				<input type="text" class="input-xlarge" id="inputIDs">
			</div>
		</div>
		
		<div class="control-group">
			<div class="controls">
				<label class="checkbox"> <input type="checkbox"> <s:message
						code="ui.extendedSearch.fuzzy" text="ui.extendedSearch.fuzzy" />
				</label>
			</div>
		</div>
		
		<div class="control-group">
			<div class="controls">
				<button type="reset" class="btn">
					<s:message code="ui.reset" text="ui.reset" />
					<i class="icon-remove-sign"></i>
				</button>
				<button type="submit" class="btn btn-primary">
					<s:message code="ui.search" text="ui.search" />
					<i class="icon-search"></i>
				</button>
			</div>
		</div>
		
	</form>

</div>