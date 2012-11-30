<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<gaz-place-nav active-tab="merge" place="place"></gaz-place-nav>

<ul class="nav nav-tabs">
	<li class="active">
		<a ng-click="getCandidatesByName()" data-toggle="tab">
			<s:message code="ui.place.placesWithSimilarNames" text="ui.place.placesWithSimilarNames"/>
		</a>
	</li>
	<li>
		<a ng-click="getCandidatesByLocation()" data-toggle="tab">
			<s:message code="ui.place.placesWithSimilarLocation" text="ui.place.placesWithSimilarLocation"/>
		</a>
	</li>
</ul>

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
				<td><a href="#/show/{{place.gazId}}">{{place.prefName.title}}</a></td>
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