<%@ page contentType="text/html; charset=utf-8" session="false"%>

<span>
	<a href="#/show/{{place.gazId}}">
		{{place.prefName.title}}
		<em><small ng-show="place.type" gaz-translate="'place.types.' + place.type"></small></em>
	</a>
</span>