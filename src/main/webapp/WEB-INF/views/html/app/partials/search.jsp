<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<div class="subnav">
	<ul class="nav nav-pills">
		<li ng-click="setLimit(10)">
		    <a style="border: none"><i class="icon-stop"></i> 10</a>
		</li>
		<li ng-click="setLimit(100)">
			<a href="" style="border: none"><i class="icon-th-large"></i> 100</a>
		</li>
		<li ng-click="setLimit(1000)">
			<a href="" style="border-left: none"><i class="icon-th"></i> 1000</a>
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
		<li class="pull-right">
			<ul class="pagination">
				<li ng-class="{disabled:(page() == 1)}" ng-click="prevPage()">
					<a>&larr; <s:message code="ui.previous" /></a>
				</li>
				<li>
					<a>
						<s:message code="ui.page" text="Seite" />
						{{page()}} / {{totalPages()}}
					</a>
				</li>
				<li ng-class="{disabled:(page() == totalPages())}" ng-click="nextPage()">
					<a><s:message code="ui.next" text="Vor"/> &rarr;</a>
				</li>
			</ul>
		</li>
	</ul>

</div>

<table class="table table-striped">
	<thead>
		<tr>
			<td>#</td>
			<td><s:message code="domain.placename.title" text="domain.placename.title" /></td>
			<td><s:message code="domain.thesaurus" text="domain.thesaurus" /></td>
			<td><s:message code="domain.place.uri" text="domain.place.uri" /></td>
		</tr>
	</thead>
	<tbody>
		<tr ng-repeat="place in places">
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