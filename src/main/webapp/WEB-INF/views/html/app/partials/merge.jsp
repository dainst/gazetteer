<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<div class="subnav">
	<ul class="nav nav-pills">
		<li>
			<a href="javascript:history.back()">
				&larr; 
				<s:message code="ui.back" />
			</a>
		</li>
		<li class="pull-right active">	
			<a href="#/merge/{{place.gazId}}">
				<i class="icon-filter"></i> <s:message code="ui.merge" text="ui.merge"/>
			</a>
		</li>
		<li class="pull-right">
			<a href="#/edit/{{place.gazId}}">
				<i class="icon-edit"></i> <s:message code="ui.edit" text="ui.edit"/>
			</a>
		</li>
		<li class="pull-right">
			<a href="#/get/{{place.gazId}}">
				<i class="icon-th-list"></i> <s:message code="ui.show" text="ui.show"/>
			</a>
		</li>
	</ul>
</div>

<div class="row-fluid" id="contentDiv">

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
			<tr ng-repeat="place in candidatePlaces">
				<td>{{place.gazId}}</td>
				<td><a href="#/get/{{place.gazId}}">{{place.prefName.title}}</a></td>
				<td>{{place.thesaurus}}</td>
				<td>
					<s:message code="ui.copyToClipboard" var="copyMsg" />
					<div class="modal hide" id="copyUriModal">
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
					<a data-toggle="modal" href="#copyUriModal"><i class="icon-share"></i></a>
				</td>
			</tr>
		</tbody>
	</table>
	
</div>