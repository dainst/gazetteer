<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<s:message code="ui.language.notSpecified" var="langNotSpecified" />

<!-- Page title -->
<div class="page-header">
	<h1>
		{{place.prefName.title}}
		<small>
			{{place["@id"]}}
		</small>
	</h1>
</div>

<div class="subnav">
	<ul class="nav nav-pills">
		<li>
			<a href="javascript:history.back()">
				&larr; 
				<s:message code="ui.back" />
			</a>
		</li>
	</ul>
</div>

<div class="row-fluid" id="contentDiv">

	<form novalidate class="form-horizontal"">
	
		<fieldset>

			<div class="span6">
		
				<legend><s:message code="domain.place.general" text="domain.place.general"/></legend>
			
				<!-- type -->
				<div class="control-group">
					<label class="control-label">
						<s:message code="domain.place.type" text="domain.place.type" />
					</label>
					<div class="controls">
						<input type="text" ng-model="place.type" />
					</div>
				</div>
				
				<!-- tags -->
				<div class="control-group">
					<label class="control-label">
						<s:message code="domain.place.tags" text="domain.place.tags" />
					</label>
					<div class="controls">
						<input type="text" ng-model="place.tags" ng-list />
						<div>
							<span ng-repeat="tag in place.tags">
								<span class="label label-info">{{tag}}</span>&nbsp; 
							</span>
						</div>
					</div>
				</div>
				
				<!-- comments -->
				<div class="control-group">
					<label class="control-label">
						<s:message code="domain.place.comments" text="domain.place.comments" />
					</label>
					<div class="controls">
						<textarea ng-model="comment.text"></textarea>
						<select ng-model="comment.language" class="input-small">
							<option value="" label="${langNotSpecified}">
							<c:forEach var="language" items="${languages}">
								<option value="${language.key}" label="${language.value}">
							</c:forEach>
						</select>
						<div class="btn btn-primary plus" ng-click="addComment()" ng-disabled="!comment.text || !comment.language">
							<i class="icon-plus icon-white"></i>
						</div>
						<div ng-hide="!place.comments" style="margin-top: 1em">
							<blockquote ng-repeat="comment in place.comments">
								<a ng-click="place.comments.splice($index,1)"><i class="icon-remove-sign"></i></a>
								<p>{{comment.text}}</p>
								<small ng-hide="!comment.language" gaz-translate="'languages.' + comment.language"></small>
							</blockquote>
						</div>
					</div>
				</div>
				
			</div>
			
			<div class="span6">
			
				<legend><s:message code="domain.place.names" text="domain.place.names"/></legend>
				
				<!-- preferred name -->
				<div class="control-group">
					<label class="control-label">
						<s:message code="domain.place.prefName" text="domain.place.prefName" />
					</label>
					<div class="controls">
						<input type="text" ng-model="place.prefName.title" required />
						<select ng-model="place.prefName.language" class="input-small">
							<option value="" label="${langNotSpecified}">
							<c:forEach var="language" items="${languages}">
								<option value="${language.key}" label="${language.value}">
							</c:forEach>
						</select>
					</div>
				</div>
				
				<!-- additional names -->
				<div class="control-group">
					<label class="control-label">
						<s:message code="domain.place.otherNames" text="domain.place.otherNames" />
					</label>
					<div class="controls">
						<input type="text" ng-model="name.title" />
						<select ng-model="name.language" class="input-small">
							<option value="" label="${langNotSpecified}">
							<c:forEach var="language" items="${languages}">
								<option value="${language.key}" label="${language.value}">
							</c:forEach>
						</select>
						<div class="btn btn-primary plus" ng-click="addName()" ng-disabled="!name.title">
							<i class="icon-plus icon-white"></i>
						</div>
						<div ng-repeat="placename in place.names">
							<a ng-click="place.names.splice($index,1)"><i class="icon-remove-sign"></i></a> {{placename.title}}
							<em ng-hide="!placename.language">
								(<span gaz-translate="'languages.' + placename.language"></span>)
							</em>
						</div>
					</div>
				</div>
				
				<legend><s:message code="domain.place.locations" text="domain.place.locations"/></legend>
				
				<!-- preferred location -->
				<div class="control-group">
					<label class="control-label">
						<s:message code="domain.place.prefLocation" text="domain.place.prefLocation" />
					</label>
					<div class="controls">
						<div class="input-append">
							<gaz-location-picker coordinates="place.prefLocation.coordinates">
							<button class="picker-search-button btn" type="button">
								<i class="icon-map-marker"></i>
							</button>
						</div>
						<form:select path="prefLocation.confidence">
							<form:option value="1" label="1" />
							<form:option value="2" label="2" />
							<form:option value="3" label="3" />
						</form:select>
						{{place.prefLocation.coordinates.join(",")}}
					</div>
				</div>

			</div>
		
		</fieldset>
	    <div class="form-actions">
           	<button ng-click="save()" class="save btn btn-primary"><s:message code="ui.save" text="ui.save"/></button>
           	<a class="btn" href="javascript:history.back()"><s:message code="ui.cancel" text="ui.cancel"/></a>
       	</div>
	</form>
	
</div>