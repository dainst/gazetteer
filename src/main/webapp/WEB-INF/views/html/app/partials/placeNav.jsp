<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<div class="subnav place-nav">
	<ul class="nav nav-pills">
		<li>
			<a href="javascript:history.back()">
				&larr; 
				<s:message code="ui.back" />
			</a>
		</li>
		<li class="dropdown pull-right">
			<a class="dropdown-toggle" data-toggle="dropdown" href="#">
				<i class="icon-file"></i> <b class="caret"></b>
			</a>
			<ul class="dropdown-menu">
				<li><a href="${baseUri}doc/{{place.gazId}}.kml" target="_blank">KML</a></li>
				<li><a href="${baseUri}doc/{{place.gazId}}.json" target="_blank">JSON</a></li>
			</ul>
		</li>
		<li class="pull-right" ng-class="isActive('merge')">	
			<a href="#/merge/{{place.gazId}}">
				<i class="icon-globe"></i> <s:message code="ui.similarPlaces" text="ui.merge"/>
			</a>
		</li>
		<li class="pull-right" ng-class="isActive('edit')">
			<a href="#/edit/{{place.gazId}}">
				<i class="icon-edit"></i> <s:message code="ui.edit" text="ui.edit"/>
			</a>
		</li>
		<li class="pull-right" ng-class="isActive('show')">
			<a href="#/show/{{place.gazId}}">
				<i class="icon-th-list"></i> <s:message code="ui.show" text="ui.show"/>
			</a>
		</li>
	</ul>
	<hr>
</div>