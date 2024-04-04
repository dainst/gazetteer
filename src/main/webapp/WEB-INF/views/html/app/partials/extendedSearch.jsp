<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<s:message code="ui.extendedSearch.namesLanguage" text="ui.extendedSearch.namesLanguage" var="namesLanguage" />
<s:message code="ui.language.title" text="ui.language.title" var="namesLanguageTitle" />
<s:message code="ui.extendedSearch.typesSelect" text="ui.extendedSearch.typesSelect" var="typesSelect" />
<s:message code="domain.identifier.context" text="domain.identifier.context" var="idContext" />
<s:message code="ui.extendedSearch.idsNoType" text="ui.extendedSearch.idsNoType" var="idsNoType" />
<s:message code="ui.extendedSearch.recordGroup" text="ui.extendedSearch.recordGroup" var="recordGroup" />
<s:message code="ui.extendedSearch.recordGroupNo" text="ui.extendedSearch.recordGroupNo" var="recordGroupNo" />

<div class="well extended-search">

	<br/>

	<form class="form-horizontal" ng-submit="submit()">
	
		<div class="control-group">
			<label class="control-label" for="inputMeta"> <s:message
					code="ui.extendedSearch.meta" text="ui.extendedSearch.meta" />
			</label>
			<div class="controls">
				<div class="inline">
					<input type="text" class="input-xlarge" id="inputMeta" ng-model="meta" focus-me="true" style="margin-right: 10px">
					<label class="checkbox inline" style="width: 140px; padding-top: 0;">
						<input type="checkbox" ng-model="fuzzy">
						<s:message code="ui.extendedSearch.fuzzy" text="ui.extendedSearch.fuzzy" />
					</label>
				</div>
			</div>
		</div>
		
		<div class="control-group">
			<label class="control-label" for="inputNames"> <s:message
					code="ui.extendedSearch.names" text="ui.extendedSearch.names" />
			</label>
			<div class="controls">
				<input type="text" class="input-xlarge" id="inputNames" ng-model="names.title">
				<select ng-model="names.language" class="input-medium">
					<option label="${namesLanguageTitle}" selected disabled></option>
					<option value="">${namesLanguage}</option>
					<c:forEach var="language" items="${languages}">
						<option value="${language.key}">${language.value}</option>
					</c:forEach>
				</select>
			</div>
		</div>
		
		<div class="control-group">
			<label class="control-label"> <s:message
					code="ui.extendedSearch.parent" text="ui.extendedSearch.parent" />
			</label>
			<div class="controls">
				<div gaz-place-picker place="parent" id="parent.gazId" style="margin-right: 10px"></div>
				<label class="checkbox inline" style="vertical-align: top;">
					<input type="checkbox" ng-model="grandchildrenSearch" />
					<span gaz-translate="'ui.search.grandchildren-search'"></span>
				</label>
			</div>
		</div>
		
		<div class="control-group" style="margin-top: -5px;">
			<label class="control-label" for="inputTypes"> <s:message
					code="ui.extendedSearch.types" text="ui.extendedSearch.types" />
			</label>
			<div class="controls">
				<select ng-model="type" style="width: 284px;">
					<option label="${typesSelect}" selected disabled></option>
					<option value="noType" gaz-translate="'place.types.no-type'"></option>
					<c:forEach var="placeType" items="${placeTypes}">
						<option value="${placeType}" gaz-translate="'place.types.' + '${placeType}'"></option>
					</c:forEach>
				</select>
			</div>
		</div>
		
		<div class="control-group">
			<label class="control-label"> <s:message
					code="ui.extendedSearch.tags" text="ui.extendedSearch.tags" />
			</label>
			<div class="controls">
				<div gaz-tag-field tags="tags" fieldname="tags" fieldwidth="274px" number="0" deactivated="filters.noTags" style="margin-right: 10px"></div>
				<label class="checkbox inline" style="vertical-align: top;">
					<input type="checkbox" ng-model="filters.noTags" />
					<span gaz-translate="'ui.search.filter.no-tags'"></span>
				</label>
			</div>
		</div>

		<div class="control-group">
			<label class="control-label"> <s:message
					code="ui.extendedSearch.provenance" text="ui.extendedSearch.provenance" />
			</label>
			<div class="controls">
				<div gaz-tag-field tags="provenance" fieldname="provenance" fieldwidth="274px" number="1" deactivated="filters.noProvenance" style="margin-right: 10px"></div>
				<label class="checkbox inline" style="vertical-align: top;">
					<input type="checkbox" ng-model="filters.noProvenance" />
					<span gaz-translate="'ui.search.filter.no-provenance'"></span>
				</label>
			</div>
		</div>
		
		<div class="control-group">
			<label class="control-label" for="inputIDs"> <s:message
					code="ui.extendedSearch.ids" text="ui.extendedSearch.ids" />
			</label>
			<div class="controls">
				<input type="text" class="input-xlarge" id="inputIDs" ng-model="ids.value">
				<select ng-model="ids.context" class="input-medium">
					<option label="${idContext}" selected disabled></option>
					<option value="">${idsNoType}</option>
					<c:forEach var="idType" items="${idTypes}">
						<option value="${idType}">${idType}</option>
					</c:forEach>
				</select>
			</div>
		</div>
		
		<c:if test="${not empty recordGroups}">
			<div class="control-group">
				<label class="control-label"> <s:message
						code="domain.place.groupInternalData" text="domain.place.groupInternalData" />
				</label>
				<div class="controls">
					<input type="text" class="input-xlarge" id="inputIDs" ng-model="groupInternalData.text">
					<select ng-model="groupInternalData.groupId" class="input-medium">
						<option label="${recordGroup}" selected disabled></option>
						<option value="">${recordGroupNo}</option>
						<c:forEach var="recordGroup" items="${recordGroups}">
							<option value="${recordGroup.id}">${recordGroup.name}</option>
						</c:forEach>
					</select>
				</div>
			</div>
		</c:if>
		
		<div class="control-group">
			<label class="control-label">
				<span gaz-translate="'ui.search.filter'"></span>
			</label>
			<div class="controls">
				<table class="table table-condensed" style="width: auto; margin-bottom: 0px; margin-top: -4px; border: none !important;">
					<tbody>
						<tr>
							<td ng-hide="filters.noCoordinates || filters.unlocatable" style="border: none !important;">
								<label class="checkbox inline">
									<input type="checkbox" ng-model="filters.coordinates" />
									<span gaz-translate="'ui.search.filter.coordinates'"></span>
								</label>
							</td>
							<td ng-show="filters.noCoordinates || filters.unlocatable" style="border: none !important;">
								<label class="checkbox inline" style="cursor: default !important;">
									<input type="checkbox" ng-model="filters.coordinates" disabled />
									<span gaz-translate="'ui.search.filter.coordinates'" style="color: grey;"></span>
								</label>
							</td>
							
							<td ng-hide="filters.noPolygon || filters.unlocatable" style="padding-left: 10px; border: none !important;">
								<label class="checkbox inline">
									<input type="checkbox" ng-model="filters.polygon" />
									<span gaz-translate="'ui.search.filter.polygon'"></span>
								</label>
							</td>
							<td ng-show="filters.noPolygon || filters.unlocatable" style="padding-left: 10px; border: none !important;">
								<label class="checkbox inline" style="cursor: default !important;">
									<input type="checkbox" ng-model="filters.polygon" disabled />
									<span gaz-translate="'ui.search.filter.polygon'" style="color: grey;"></span>
								</label>
							</td>
							
							<td ng-hide="filters.coordinates || filters.noCoordinates || filters.polygon || filters.noPolygon"
									style="padding-left: 10px; border: none !important;">
								<label class="checkbox inline">
									<input type="checkbox" ng-model="filters.unlocatable" />
									<span gaz-translate="'ui.search.filter.unlocatable'"></span>
								</label>
							</td>
							<td ng-show="filters.coordinates || filters.noCoordinates || filters.polygon || filters.noPolygon"
									style="padding-left: 10px; border: none !important;">
								<label class="checkbox inline" style="cursor: default !important;">
									<input type="checkbox" ng-model="filters.unlocatable" disabled />
									<span gaz-translate="'ui.search.filter.unlocatable'" style="color: grey;"></span>
								</label>
							</td>
						</tr>
						<tr>							
							<td ng-hide="filters.coordinates || filters.unlocatable" style="border: none !important;">
								<label class="checkbox inline">
									<input type="checkbox" ng-model="filters.noCoordinates" />
									<span gaz-translate="'ui.search.filter.no-coordinates'"></span>
								</label>
							</td>
							<td ng-show="filters.coordinates || filters.unlocatable" style="border: none !important;">
								<label class="checkbox inline" style="cursor: default !important;">
									<input type="checkbox" ng-model="filters.noCoordinates" disabled />
									<span gaz-translate="'ui.search.filter.no-coordinates'" style="color: grey;"></span>
								</label>
							</td>
							
							<td ng-hide="filters.polygon || filters.unlocatable" style="padding-left: 10px; border: none !important;">
								<label class="checkbox inline">
									<input type="checkbox" ng-model="filters.noPolygon" />
									<span gaz-translate="'ui.search.filter.no-polygon'"></span>
								</label>
							</td>
							<td ng-show="filters.polygon || filters.unlocatable" style="padding-left: 10px; border: none !important;">
								<label class="checkbox inline" style="cursor: default !important;">
									<input type="checkbox" ng-model="filters.noPolygon" disabled />
									<span gaz-translate="'ui.search.filter.no-polygon'" style="color: grey;"></span>
								</label>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</div>
		
		<div class="control-group">
			<div class="controls">
				<button type="button" class="btn" ng-click="reset()">
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