<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<div gaz-place-nav active-tab="merge" place="place"></div>

<s:message code="ui.copyToClipboard" var="copyMsg"/>

<div class="modal hide" id="copyUriModal">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal">×</button>
		<h3><s:message code="ui.copyUriToClipboardHeading"/></h3>
	</div>
	<div class="modal-body">
		<label>${copyMsg}</label>
		<input class="input-xxlarge" style="width:97%" type="text" value="${baseUri}place/{{place.gazId}}" id="copyUriInput"></input>
	</div>
</div>
<script type="text/javascript">
	$("#copyUriModal").on("shown",function() {
		$("#copyUriInput").focus().select();
	});
</script>

<ul class="nav nav-tabs">
	<li class="active">
		<a ng-click="getCandidatesByName()" style="cursor: pointer;" data-toggle="tab">
			<s:message code="ui.place.placesWithSimilarNames" text="ui.place.placesWithSimilarNames"/>
		</a>
	</li>
	<li ng-show="place.prefLocation.coordinates != null">
		<a ng-click="getCandidatesByLocation()" style="cursor: pointer;" data-toggle="tab">
			<s:message code="ui.place.placesWithSimilarLocation" text="ui.place.placesWithSimilarLocation"/>
		</a>
	</li>
</ul>

<div class="row-fluid" id="contentDiv">

	<div ng-show="candidatePlaces.length == 0">
		<em><s:message code="ui.search.emptyResult" text="ui.search.emptyResult"/></em>
	</div>

	<table class="table table-striped" ng-show="candidatePlaces.length > 0">
		<thead>
			<tr>
				<td style="width:50px;">#</td>
				<td><s:message code="domain.placename.title" text="domain.placename.title" /></td>
				<td style="width:30px;"></td>
				<td style="width:30px;"></td>
			</tr>
		</thead>
		<tbody>
			<tr ng-repeat="candidatePlace in candidatePlaces">
				<td>{{candidatePlace.gazId}}</td>
				<td><div gaz-place-title place="candidatePlace"></div><small class="muted">{{parents[candidatePlace.parent].prefName.title}}</small></td>
				<td>
					<sec:authorize access="hasRole('ROLE_EDITOR')">
						<div ng-hide="place.editAccessDenied || place.accessDenied">
							<div class="modal hide" id="linkModal-{{candidatePlace.gazId}}">
								<div class="modal-header">
									<button type="button" class="close" data-dismiss="modal">×</button>
									<h3><s:message code="ui.merge.dialog.head" text="ui.merge.dialog.head"/></h3>
								</div>
								<div class="modal-body">
									<p><s:message code="ui.link.dialog.body" text="ui.link.dialog.body"/></p>
									<ul>
										<li><a href="#/show/{{place.gazId}}">{{place.prefName.title}}</a></li>
										<li><a href="#/show/{{candidatePlace.gazId}}">{{candidatePlace.prefName.title}}</a></li>
									</ul>
								</div>
								<div class="modal-footer">
									<button class="btn" data-dismiss="modal"><s:message code="ui.cancel" text="ui.cancel"/></button>
									<a ng-click="link(place, candidatePlace)" data-dismiss="modal" class="btn btn-primary"><s:message code="ui.ok" text="ui.ok"/></a>
								</div>
							</div>
							<div style="text-align:center;">
								<a gaz-tooltip="'ui.link.tooltip'" data-placement="left" data-toggle="modal" href="#linkModal-{{candidatePlace.gazId}}"><i class="icon-link"></i></a>
							</div>
						</div>
					</sec:authorize>
				</td>
				<td>
					<sec:authorize access="hasRole('ROLE_EDITOR')">
						<div ng-hide="place.editAccessDenied || place.accessDenied">
							<div class="modal hide" id="mergeModal-{{candidatePlace.gazId}}">
								<div ng-show="place.recordGroupId == candidatePlace.recordGroupId">
									<div class="modal-header">
										<button type="button" class="close" data-dismiss="modal">×</button>
										<h3><s:message code="ui.merge.dialog.head" text="ui.merge.dialog.head"/></h3>
									</div>
									<div class="modal-body">
										<p><s:message code="ui.merge.dialog.body" text="ui.merge.dialog.body"/></p>
										<ul>
											<li><a href="#/show/{{place.gazId}}">{{place.prefName.title}}</a></li>
											<li><a href="#/show/{{candidatePlace.gazId}}">{{candidatePlace.prefName.title}}</a></li>
										</ul>
									</div>
									<div class="modal-footer">
										<button class="btn" data-dismiss="modal"><s:message code="ui.cancel" text="ui.cancel"/></button>
										<a ng-click="merge(place, candidatePlace)" data-dismiss="modal" class="btn btn-primary"><s:message code="ui.ok" text="ui.ok"/></a>
									</div>
								</div>
								<div ng-hide="place.recordGroupId == candidatePlace.recordGroupId">
									<div class="modal-header">
										<button type="button" class="close" data-dismiss="modal">×</button>
										<h3><s:message code="ui.merge.notAllowed.head" text="ui.merge.notAllowed.head"/></h3>
									</div>
									<div class="modal-body">
										<p><s:message code="ui.merge.notAllowed.body" text="ui.merge.notAllowed.body"/></p>
										<ul>
											<li><a href="#/show/{{place.gazId}}">{{place.prefName.title}}</a></li>
											<li><a href="#/show/{{candidatePlace.gazId}}">{{candidatePlace.prefName.title}}</a></li>
										</ul>
									</div>
									<div class="modal-footer">
										<button class="btn" data-dismiss="modal"><s:message code="ui.ok" text="ui.ok"/></button>
									</div>
								</div>
							</div>
							<div style="text-align:center;">
								<a gaz-tooltip="'ui.merge.tooltip'" data-placement="left" data-toggle="modal" href="#mergeModal-{{candidatePlace.gazId}}"><i class="icon-magnet"></i></a>
							</div>
						</div>
					</sec:authorize>
				</td>
			</tr>
		</tbody>
	</table>
	
</div>