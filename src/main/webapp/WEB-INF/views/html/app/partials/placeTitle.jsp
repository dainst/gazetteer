<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<span>
	<a ng-href="#!/show/{{place.gazId}}">
		<span ng-show="place.accessDenied"><s:message code="domain.place.hiddenPlace" text="domain.place.hiddenPlace"/> &num;{{place.gazId}}</span>
		<span ng-hide="place.accessDenied">
			<span ng-hide="place.prefName"><s:message code="domain.place.untitled" text="domain.place.untitled"/></span>
			{{place.prefName.title}}<em><small ng-repeat="name in place.names.slice(0,3)">, 
			{{name.title}}</small><small ng-show="place.names.length > 3">, ...</small></em>
		</span>
	</a>
</span>