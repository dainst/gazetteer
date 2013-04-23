<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<script type="text/ng-template" id="thesaurus-item.html">
	<span>
		<i class="icon-circle-arrow-right" ng-show="!place.isOpen" ng-click="open(place)"></i>
		<i class="icon-circle-arrow-down" ng-show="place.isOpen && place.children" ng-click="close(place)"></i>
		<i class="icon-circle-arrow-right icon-spin" ng-show="place.isOpen && !place.children"></i>
		<i class="icon-circle" ng-show="place.isOpen && place.children.length == 0"></i>
		<span ng-mouseover="showMarker(place)" ng-mouseout="hideMarker()">
			<span gaz-place-title place="place"></span>
		</span>
		<i class="icon-map-marker" ng-show="place.prefLocation"></i>
		<ul style="list-style:none">
			<li class="thesaurus-row" ng-repeat="place in place.children" ng-include="'thesaurus-item.html'"></li>
		</ul>
	</span>
</script>

<ul style="list-style:none">
	<li class="thesaurus-row" ng-repeat="place in places" ng-include="'thesaurus-item.html'"></li>
</ul>