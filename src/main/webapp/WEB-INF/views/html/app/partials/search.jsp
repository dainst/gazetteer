<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<div>
	<ul class="nav nav-pills" style="display:inline-block; margin-bottom: 0;">
		<li ng-click="setLimit(10)" ng-class="{active:(search.limit == 10)}" gaz-tooltip="'ui.search.limit.10.tooltip'">
		    <a><i class="icon-stop"></i> 10</a>
		</li>
		<li ng-click="setLimit(100)" ng-class="{active:(search.limit == 100)}" gaz-tooltip="'ui.search.limit.100.tooltip'">
			<a><i class="icon-th-large"></i> 100</a>
		</li>
		<li ng-click="setLimit(1000)" ng-class="{active:(search.limit == 1000)}" gaz-tooltip="'ui.search.limit.1000.tooltip'">
			<a><i class="icon-th"></i> 1000</a>
		</li>
		<!-- <li class="dropdown">
			<a href="#" class="dropdown-toggle" data-toggle="dropdown">
				<s:message code="ui.search.views" text="ui.search.views" />
				<b class="caret"></b>
			</a>
			<ul class="dropdown-menu">
				<li>
				    <a href="">
						<i class="icon-globe"></i> <i class="icon-list"></i>
						<s:message code="ui.search.view.mapAndTable" text="ui.search.view.mapAndTable" />
					</a>
				</li>
				<li>
					<a href="">
						<i class="icon-globe"></i> <s:message code="ui.search.view.map" />
					</a>
				</li>
				<li>
					<a href="">
						<i class="icon-list"></i> <s:message code="ui.search.view.table" />
					</a>
				</li>
			</ul>
		</li> -->
	</ul>
	<ul class="nav nav-pills pull-right" style="display:inline-block; margin-bottom: 0;">
		<li ng-class="{disabled:(page() == 1)}" ng-click="prevPage()">
			<a>&larr; <s:message code="ui.previous" /></a>
		</li>
		<li class="divider-vertical"></li>
		<li class="disabled">
			<a>
				<s:message code="ui.page" text="Seite" />
				{{page()}} / {{totalPages()}}
			</a>
		</li>
		<li class="divider-vertical"></li>
		<li ng-class="{disabled:(page() == totalPages())}" ng-click="nextPage()">
			<a><s:message code="ui.next" text="Vor"/> &rarr;</a>
		</li>
	</ul>
	<hr>
</div>

<table class="table table-striped">
	<thead>
		<tr>
			<th style="width:30px">
				<!-- TODO add tooltip -->
				<a ng-click="orderBy('_score')" gaz-tooltip="'ui.search.sort.score.tooltip'"><i class="icon-signal"></i></a>
				<i ng-show="search.sort == '_score' && search.order == 'asc'" class="icon-chevron-up"></i>
				<i ng-show="search.sort == '_score' && search.order == 'desc'" class="icon-chevron-down"></i>
			</th>
			<th style="width:50px">
				<a ng-click="orderBy('_id')" gaz-tooltip="'ui.search.sort.id.tooltip'">#</a>
				<i ng-show="search.sort == '_id' && search.order == 'asc'" class="icon-chevron-up"></i>
				<i ng-show="search.sort == '_id' && search.order == 'desc'" class="icon-chevron-down"></i>
			</th>
			<th>
				<a ng-click="orderBy('prefName.title.sort')" gaz-tooltip="'ui.search.sort.name.tooltip'"><s:message code="domain.placename.title" text="domain.placename.title" /></a>
				<i ng-show="search.sort == 'prefName.title.sort' && search.order == 'asc'" class="icon-chevron-up"></i>
				<i ng-show="search.sort == 'prefName.title.sort' && search.order == 'desc'" class="icon-chevron-down"></i>
			</th>
			<th style="width:100px">
				<a ng-click="orderBy('thesaurus')" gaz-tooltip="'ui.search.sort.thesaurus.tooltip'"><s:message code="domain.thesaurus" text="domain.thesaurus" /></a>
				<i ng-show="search.sort == 'thesaurus' && search.order == 'asc'" class="icon-chevron-up"></i>
				<i ng-show="search.sort == 'thesaurus' && search.order == 'desc'" class="icon-chevron-down"></i>
			</th>
			<th><s:message code="domain.place.uri" text="domain.place.uri" /></th>
		</tr>
	</thead>
	<tbody>
		<tr ng-repeat="place in places">
			<td></td>
			<td>{{place.gazId}}</td>
			<td><gaz-place-title place="place"></gaz-place-title></td>
			<td>{{place.thesaurus}}</td>
			<td>
				<gaz-copy-uri uri="place['@id']"></gaz-copy-uri>
			</td>
		</tr>
	</tbody>
</table>