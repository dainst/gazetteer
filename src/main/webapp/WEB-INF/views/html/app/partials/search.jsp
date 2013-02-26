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

	<div class="well" style="padding:10px; margin-bottom: 10px;">
		<table class="table" style="width: auto; margin-bottom:0;">
			<thead>
				<tr>
					<th ng-repeat="(facetName,facet) in facets" style="padding:2px 8px;">
						<span gaz-translate="'domain.place.'+facetName"></span>
					</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td ng-repeat="(facetName,facet) in facets" style="border: 0; vertical-align:top; padding:2px 8px;">
						<table>
							<tr>
								<td ng-show="facet.length == 1">
									<ul class="unstyled" style="margin:0;">
										<li>
											<small><i class="icon-angle-right"></i>{{facet[0].label}}</small>
										</li>
									</ul>
								</td>
								<td style="vertical-align:top;" ng-show="facet.length > 1">
									<ul class="unstyled" style="margin:0;">
										<li ng-repeat="entry in facet | orderBy:'count':true | limitTo:5">
											<small>
												<i class="icon-angle-right"></i>
												<a ng-click="setFacet(facetName, entry.term)">
													<span ng-show="entry.label.length < 15">
														{{entry.label}}
													</span>
													<abbr title="{{entry.label}}" ng-show="entry.label.length >= 15">
														{{entry.label.substring(0, 14)}}...
													</abbr>
												</a>&nbsp;<em class="muted">{{entry.count}}</em>
											</small>
										</li>
									</ul>
								</td>
								<td ng-show="facet.length > 5">
									<ul class="unstyled" style="margin:0; padding-left:5px;">
										<li ng-repeat="entry in facet | orderBy:'count':true | limitTo:-5">
											<small>
												<i class="icon-angle-right"></i>
												<a ng-click="setFacet(facetName, entry.term)">
													<span ng-show="entry.label.length < 15">
														{{entry.label}}
													</span>
													<abbr title="{{entry.label}}" ng-show="entry.label.length >= 15">
														{{entry.label.substring(0, 14)}}...
													</abbr>
												</a>&nbsp;<em class="muted">{{entry.count}}</em>
											</small>
										</li>
									</ul>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</tbody>
		</table>
	</div>
	
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
			<th>
				<a ng-click="orderBy('type')" gaz-tooltip="'ui.search.sort.type.tooltip'"><s:message code="domain.place.type" text="domain.place.type" /></a>
				<i ng-show="search.sort == 'type' && search.order == 'asc'" class="icon-chevron-up"></i>
				<i ng-show="search.sort == 'type' && search.order == 'desc'" class="icon-chevron-down"></i>
			</th>
			<th style="width:30px"><s:message code="domain.place.uri" text="domain.place.uri" /></th>
		</tr>
	</thead>
	<tbody>
		<tr ng-repeat="place in places">
			<td></td>
			<td>{{place.gazId}}</td>
			<td><div gaz-place-title place="place"></div></td>
			<td><span ng-show="place.type" gaz-translate="'place.types.' + place.type"></span></td>
			<td style="text-align:center;">
				<div gaz-copy-uri uri="place['@id']"></div>
			</td>
		</tr>
	</tbody>
</table>