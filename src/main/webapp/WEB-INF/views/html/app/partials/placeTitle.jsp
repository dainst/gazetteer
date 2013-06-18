<%@ page contentType="text/html; charset=utf-8" session="false"%>

<span>
	<a ng-href="#!/show/{{place.gazId}}">
		{{place.prefName.title}}<em><small ng-repeat="name in place.names.slice(0,3)">, 
			{{name.title}}</small><small ng-show="place.names.length > 3">, ...</small></em>
	</a>
</span>