<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<div>

	<ul class="nav nav-pills" style="display:inline-block; margin-bottom: 0;">
		<li ng-click="setLimit(10)" ng-class="{active:(search.limit == 10)}" gaz-tooltip="'ui.search.limit.10.tooltip'" style="cursor:pointer">
		    <a><i class="icon-stop"></i> 10</a>
		</li>
		<li ng-click="setLimit(50)" ng-class="{active:(search.limit == 50)}" gaz-tooltip="'ui.search.limit.50.tooltip'" style="cursor:pointer">
			<a><i class="icon-th-large"></i> 50</a>
		</li>
		<li ng-click="setLimit(100)" ng-class="{active:(search.limit == 100)}" gaz-tooltip="'ui.search.limit.100.tooltip'" style="cursor:pointer">
			<a><i class="icon-th"></i> 100</a>
		</li>
		<li class="dropdown pull-right">
			<a class="dropdown-toggle" data-toggle="dropdown" href="">
				<i class="icon-file"></i> <b class="caret"></b>
			</a>
			<ul class="dropdown-menu">
				<li>
					<a ng-href="../search.json?q={{search.encodedQ}}&fq={{search.fq}}{{polygonFilterCoordinatesString}}&limit=1000&type={{search.type}}&pretty=true" target="_blank">JSON</a>
				</li>
				<li ng-if="(!filters.noCoordinates || !filters.noPolygon) && !filters.unlocatable">
					<a ng-href="../search.geojson?q={{search.encodedQ}}&fq={{search.fq}}{{polygonFilterCoordinatesString}}&limit=1000&type={{search.type}}&pretty=true" target="_blank">GeoJSON</a>
				</li>
				<li ng-if="(!filters.noCoordinates || !filters.noPolygon) && !filters.unlocatable">
					<a ng-href="../search.kml?q={{search.encodedQ}}&fq={{search.fq}}{{polygonFilterCoordinatesString}}&limit=1000&type={{search.type}}" target="_blank">KML</a>
				</li>
				<li ng-if="(!filters.noCoordinates || !filters.noPolygon) && !filters.unlocatable">
					<a ng-href="../search/shapefile?q={{search.encodedQ}}&fq={{search.fq}}{{polygonFilterCoordinatesString}}&type={{search.type}}" target="_blank">
						Shapefile
					</a>
				</li>
			</ul>
		</li>
	</ul>
	<ul class="nav nav-pills pull-right" style="display:inline-block; margin-bottom: 0;">
		<li ng-class="{disabled:(page() == 1)}" ng-click="prevPage()" style="cursor:pointer">
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
		<li ng-class="{disabled:(page() == totalPages())}" ng-click="nextPage()" style="cursor:pointer">
			<a><s:message code="ui.next" text="Vor"/> &rarr;</a>
		</li>
	</ul>

	<div class="well" style="padding:10px; margin-bottom: 10px;" ng-show="facets">
		<table class="table" style="margin-bottom:0;">
			<thead>
				<tr>
					<th ng-repeat="(facetName,facet) in facets" style="padding:2px 8px;">
						<span gaz-translate="'domain.place.'+facetName"></span>
					</th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td ng-repeat="(facetName,facet) in facets" style="border: 0; vertical-align:top; padding:0 8px;">
						<table style="table-layout: fixed; width: 100%;">
							<tr>
								<td style="vertical-align:top; padding:0;" ng-show="facet.length > 0">
									<div style="text-align: center;">										
										<a ng-class="{disabled: facet.length <= 5 || facetOffsets[facetName] <= 0}"
												ng-click="prevFacetEntries(facetName)"
												class="btn btn-default btn-mini btn-block btn-facet-scroll">
											<i class="icon-caret-up"></i>
										</a>
									</div>
									<div style="height: {{facet.length > 5 ? 105 : facet.length * 21}}px; overflow: hidden;">
										<div style="margin:0px; height: auto; display:inline-block; white-space: nowrap; transform: translateY(-{{facetOffsets[facetName] * 21}}px);
											-webkit-transform: translateY(-{{facetOffsets[facetName] * 21}}px); transition: 0.5s ease-in-out; -webkit-transition: 0.5s ease-in-out;">
											<div ng-repeat="entry in facet | orderBy:'count':true" style="transition: 0.5s linear; -webkit-transition: 0.5s linear;">
												<small>
													<i class="icon-angle-right" style="margin-top: 0px; margin-right: 5px;"></i>
													<a ng-click="setFacet(facetName, entry.term)" href="">
														<span ng-show="entry.label.length < 30 || entry.label.length - 30 <= 3">
															{{entry.label}}
														</span>
														<abbr title="{{entry.label}}" ng-show="entry.label.length >= 30 && entry.label.length - 30 > 3">
															{{entry.label.substring(0, 28)}}...
														</abbr>
													</a>&nbsp;<em class="muted">{{entry.count}}</em>
												</small>
											</div>
										</div>
									</div>
									<div style="text-align: center;">						
										<a ng-class="{disabled: facet.length <= 5 || facetOffsets[facetName] >= facet.length - 5}"
												ng-click="nextFacetEntries(facetName)"
												class="btn btn-default btn-mini btn-block btn-facet-scroll">
											<i class="icon-caret-down"></i>
										</a>
									</div>									
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</tbody>
		</table>
		<table class="table table-condensed" style="margin-bottom: 0px;">
			<thead>
				<tr>
					<th><span gaz-translate="'ui.search.filter'"></span></th>
				</tr>
			</thead>
			<tbody>
				<tr>
					<td ng-hide="filters.noCoordinates || filters.unlocatable">
						<label class="checkbox inline">
							<input type="checkbox" ng-model="filters.coordinates" />
							<span gaz-translate="'ui.search.filter.coordinates'"></span>
						</label>
					</td>
					<td ng-show="filters.noCoordinates || filters.unlocatable">
						<label class="checkbox inline" style="cursor: default !important;">
							<input type="checkbox" ng-model="filters.coordinates" disabled />
							<span gaz-translate="'ui.search.filter.coordinates'" style="color: grey;"></span>
						</label>
					</td>
					<td ng-hide="filters.coordinates || filters.unlocatable" style="padding-left: 2px;">
						<label class="checkbox inline">
							<input type="checkbox" ng-model="filters.noCoordinates" />
							<span gaz-translate="'ui.search.filter.no-coordinates'"></span>
						</label>
					</td>
					<td ng-show="filters.coordinates || filters.unlocatable" style="padding-left: 2px;">
						<label class="checkbox inline" style="cursor: default !important;">
							<input type="checkbox" ng-model="filters.noCoordinates" disabled />
							<span gaz-translate="'ui.search.filter.no-coordinates'" style="color: grey;"></span>
						</label>
					</td>
					<td ng-hide="filters.noPolygon || filters.unlocatable" style="padding-left: 2px;">
						<label class="checkbox inline">
							<input type="checkbox" ng-model="filters.polygon" />
							<span gaz-translate="'ui.search.filter.polygon'"></span>
						</label>
					</td>
					<td ng-show="filters.noPolygon || filters.unlocatable" style="padding-left: 2px;">
						<label class="checkbox inline" style="cursor: default !important;">
							<input type="checkbox" ng-model="filters.polygon" disabled />
							<span gaz-translate="'ui.search.filter.polygon'" style="color: grey;"></span>
						</label>
					</td>
					<td ng-hide="filters.polygon || filters.unlocatable" style="padding-left: 2px;">
						<label class="checkbox inline">
							<input type="checkbox" ng-model="filters.noPolygon" />
							<span gaz-translate="'ui.search.filter.no-polygon'"></span>
						</label>
					</td>
					<td ng-show="filters.polygon || filters.unlocatable" style="padding-left: 2px;">
						<label class="checkbox inline" style="cursor: default !important;">
							<input type="checkbox" ng-model="filters.noPolygon" disabled />
							<span gaz-translate="'ui.search.filter.no-polygon'" style="color: grey;"></span>
						</label>
					</td>
					<td ng-hide="filters.coordinates || filters.noCoordinates || filters.polygon || filters.noPolygon" style="padding-left: 2px;">
						<label class="checkbox inline">
							<input type="checkbox" ng-model="filters.unlocatable" />
							<span gaz-translate="'ui.search.filter.unlocatable'"></span>
						</label>
					</td>
					<td ng-show="filters.coordinates || filters.noCoordinates || filters.polygon || filters.noPolygon" style="padding-left: 2px;">
						<label class="checkbox inline" style="cursor: default !important;">
							<input type="checkbox" ng-model="filters.unlocatable" disabled />
							<span gaz-translate="'ui.search.filter.unlocatable'" style="color: grey;"></span>
						</label>
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
				<a ng-click="orderBy('_uid')" gaz-tooltip="'ui.search.sort.id.tooltip'" style="cursor: pointer;">#</a>
				<i ng-show="search.sort == '_uid' && search.order == 'asc'" class="icon-chevron-up"></i>
				<i ng-show="search.sort == '_uid' && search.order == 'desc'" class="icon-chevron-down"></i>
			</th>
			<th>
				<a ng-click="orderBy('prefName.title.sort')" gaz-tooltip="'ui.search.sort.name.tooltip'" style="cursor: pointer;"><s:message code="domain.placename.title" text="domain.placename.title" /></a>
				<i ng-show="search.sort == 'prefName.title.sort' && search.order == 'asc'" class="icon-chevron-up"></i>
				<i ng-show="search.sort == 'prefName.title.sort' && search.order == 'desc'" class="icon-chevron-down"></i>
			</th>
			<th>
				<a ng-click="orderBy('types')" gaz-tooltip="'ui.search.sort.type.tooltip'" style="cursor: pointer;"><s:message code="domain.place.type" text="domain.place.type" /></a>
				<i ng-show="search.sort == 'types' && search.order == 'asc'" class="icon-chevron-up"></i>
				<i ng-show="search.sort == 'types' && search.order == 'desc'" class="icon-chevron-down"></i>
			</th>
			<th style="width:30px"><s:message code="domain.place.uri" text="domain.place.uri" /></th>
		</tr>
	</thead>
	<tbody>
		<tr ng-repeat="place in places" ng-click="$location.path('show/'+place.gazId)" ng-mouseover="setHighlight(place.gazId, 'searchResult', place.markerNumber)" ng-mouseout="setHighlight(null, null, null)" ng-class="{info: place.gazId==highlight}">
			<td><span style="white-space: nowrap; cursor: default;"><span ng-show="place.prefLocation && !place.prefLocation.shape" class="icon-map-marker"
				style="color: #FD7567; text-shadow: 1px 1px 1px #000000; margin-right: 5px;"></span>
				<div ng-show="place.prefLocation && place.prefLocation.shape" class="polygon-icon"></div></span></td>
			<td>{{place.gazId}}</td>
			<td>
				<div gaz-place-title place="place"></div>
				<small class="muted"><span ng-repeat="parent in place.parents">{{parent.prefName.title}}<span ng-show="$index < place.parents.length - 1">, </span></span></small>
			</td>
			<td><span ng-show="place.types && place.types.length > 0" gaz-translate="'place.types.' + place.types[0]"></span></td>
			<td style="text-align:center;">
				<div gaz-copy-uri uri="place['@id']"></div>
			</td>
		</tr>
	</tbody>
</table>

<ul class="nav nav-pills pull-right" style="display:inline-block; margin-bottom: 0px; margin-top: -15px;">
	<li ng-class="{disabled:(page() == 1)}" ng-click="prevPage()" style="cursor:pointer">
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
	<li ng-class="{disabled:(page() == totalPages())}" ng-click="nextPage()" style="cursor:pointer">
		<a><s:message code="ui.next" text="Vor"/> &rarr;</a>
	</li>
</ul>