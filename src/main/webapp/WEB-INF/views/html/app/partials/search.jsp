<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<div>
	<ul class="nav nav-pills" style="display:inline-block; margin-bottom: 0;">
		<li ng-click="setLimit(10)">
		    <a><i class="icon-stop"></i> 10</a>
		</li>
		<li ng-click="setLimit(100)">
			<a><i class="icon-th-large"></i> 100</a>
		</li>
		<li ng-click="setLimit(1000)">
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
		<li>
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
			<th>
				<!-- TODO add tooltip -->
				<i class="icon-signal"></i>
			</th>
			<th>
				<a ng-click="orderBy('_id')">#</a>
				<i ng-show="search.sort == '_id' && search.order == 'asc'" class="icon-chevron-up"></i>
				<i ng-show="search.sort == '_id' && search.order == 'desc'" class="icon-chevron-down"></i>
			</th>
			<th>
				<a ng-click="orderBy('prefName.title.sort')"><s:message code="domain.placename.title" text="domain.placename.title" /></a>
				<i ng-show="search.sort == 'prefName.title.sort' && search.order == 'asc'" class="icon-chevron-up"></i>
				<i ng-show="search.sort == 'prefName.title.sort' && search.order == 'desc'" class="icon-chevron-down"></i>
			</th>
			<th>
				<a ng-click="orderBy('thesaurus')"><s:message code="domain.thesaurus" text="domain.thesaurus" /></a>
				<i ng-show="search.sort == 'thesaurus' && search.order == 'asc'" class="icon-chevron-up"></i>
				<i ng-show="search.sort == 'thesaurus' && search.order == 'desc'" class="icon-chevron-down"></i>
			</th>
			<th><s:message code="domain.place.uri" text="domain.place.uri" /></th>
		</tr>
	</thead>
	<tbody>
		<tr ng-repeat="place in places">
			<!-- TODO icon for score -->
			<td><i class="icon-signal"></i></td>
			<td>{{place.gazId}}</td>
			<td><gaz-place-title place="place"></gaz-place-title></td>
			<td>{{place.thesaurus}}</td>
			<td>
				<s:message code="ui.copyToClipboard" var="copyMsg" />
				<div class="modal hide" id="copyUriModal-{{place.gazId}}">
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal">×</button>
						<h3>
							<s:message code="ui.copyToClipboardHeading" />
						</h3>
					</div>
					<div class="modal-body">
						<label>${copyMsg}</label>
						<input class="input-xxlarge" type="text" value="${baseUri}place/{{place.gazId}}" id="copyUriInput">
					</div>
				</div>
				<script type="text/javascript">
					$("#copyUriModal").on("shown",function() {
						$("#copyUriInput").focus().select();
					});
				</script>
				<a data-toggle="modal" href="#copyUriModal-{{place.gazId}}"><i class="icon-share"></i></a>
			</td>
		</tr>
	</tbody>
</table>