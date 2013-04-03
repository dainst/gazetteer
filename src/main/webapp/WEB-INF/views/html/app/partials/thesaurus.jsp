<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<script type="text/ng-template" id="thesaurus-item.html">
	<i class="icon-caret-right" ng-show="!place.isOpen" ng-click="open(place)"></i>
	<i class="icon-caret-down" ng-show="place.isOpen" ng-click="close(place)"></i>
	<span gaz-place-title place="place"></span>
	<ul style="list-style:none">
		<li class="thesaurus-row" ng-repeat="place in place.children" ng-include="'thesaurus-item.html'"></tr>
	</ul>
</script>

<ul style="list-style:none">
	<li class="thesaurus-row" ng-repeat="place in places" ng-include="'thesaurus-item.html'"></tr>
</ul>