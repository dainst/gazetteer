<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<div class="subnav place-nav">
	<ul class="nav nav-pills">
		<li>
			<a href="javascript:history.back()">
				&larr; 
				<s:message code="ui.back" />
			</a>
		</li>
		<li class="dropdown pull-right" ng-hide="place.accessDenied || place.deleted">
			<a class="dropdown-toggle" data-toggle="dropdown" href="">
				<i class="icon-file"></i> <b class="caret"></b>
			</a>
			<ul class="dropdown-menu">
				<li><a href="../doc/{{place.gazId}}.rdf" target="_blank">RDF/XML</a></li>
				<li><a href="../doc/{{place.gazId}}.json?pretty=true" target="_blank">JSON</a></li>
				<li ng-if="place.prefLocation && (place.prefLocation.coordinates && place.prefLocation.coordinates.length > 0) || place.prefLocation.shape">
					<a href="../doc/{{place.gazId}}.geojson?pretty=true" target="_blank">GeoJSON</a>
				</li>
				<li ng-if="place.prefLocation && (place.prefLocation.coordinates && place.prefLocation.coordinates.length > 0) || place.prefLocation.shape">
					<a href="../doc/{{place.gazId}}.kml" target="_blank">KML</a>
				</li>
				<li ng-if="place.prefLocation && (place.prefLocation.coordinates && place.prefLocation.coordinates.length > 0) || place.prefLocation.shape">
					<a href="../doc/shapefile/{{place.gazId}}" target="_blank">Shapefile</a>
				</li>
			</ul>
		</li>
		<sec:authorize access="hasRole('ROLE_EDITOR')">
			<li class="pull-right" ng-class="isActive('change-history')" ng-hide="place.editAccessDenied || place.accessDenied || place.deleted">
				<a ng-if="place.gazId" href="#!/change-history/{{place.gazId}}">
					<i class="icon-eye-open"></i> <s:message code="ui.changeHistory" text="ui.changeHistory"/>
				</a>
			</li>
		</sec:authorize>
		<li class="pull-right" ng-class="isActive('merge')" ng-hide="place.accessDenied || place.deleted">
			<a ng-if="place.gazId" href="#!/merge/{{place.gazId}}">
				<i class="icon-globe"></i> <s:message code="ui.similarPlaces" text="ui.merge"/>
			</a>
		</li>
		<sec:authorize access="hasRole('ROLE_EDITOR')">
			<li class="pull-right" ng-class="isActive('edit')" ng-hide="place.editAccessDenied || place.accessDenied || place.deleted">
				<a ng-if="place.gazId" href="#!/edit/{{place.gazId}}">
					<i class="icon-edit"></i> <s:message code="ui.edit" text="ui.edit"/>
				</a>
			</li>
		</sec:authorize>
		<li class="pull-right" ng-class="isActive('show')">
			<a ng-if="place.gazId" href="#!/show/{{place.gazId}}">
				<i class="icon-th-list"></i> <s:message code="ui.show" text="ui.show"/>
			</a>
		</li>
	</ul>
	<hr>
</div>