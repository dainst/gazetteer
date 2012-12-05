<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<gaz-place-nav active-tab="merge" place="place"></gaz-place-nav>

<ul class="nav nav-tabs">
	<li class="active">
		<a ng-click="getCandidatesByName()" data-toggle="tab">
			<s:message code="ui.place.placesWithSimilarNames" text="ui.place.placesWithSimilarNames"/>
		</a>
	</li>
	<li ng-show="place.prefLocation != null">
		<a ng-click="getCandidatesByLocation()" data-toggle="tab">
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
				<td>#</td>
				<td><s:message code="domain.placename.title" text="domain.placename.title" /></td>
				<td><s:message code="domain.thesaurus" text="domain.thesaurus" /></td>
				<td><s:message code="ui.merge" text="ui.merge" /></td>
			</tr>
		</thead>
		<tbody>
			<tr ng-repeat="candidatePlace in candidatePlaces">
				<td>{{candidatePlace.gazId}}</td>
				<td><gaz-place-title place="candidatePlace"></gaz-place-title></td>
				<td>{{candidatePlace.thesaurus}}</td>
				<td>
					<s:message code="ui.copyToClipboard" var="copyMsg" />
					<div class="modal hide" id="mergeModal-{{candidatePlace.gazId}}">
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
					<div style="text-align:center;">
						<a data-toggle="modal" href="#mergeModal-{{candidatePlace.gazId}}"><i class="icon-link"></i></a>
					</div>
				</td>
			</tr>
		</tbody>
	</table>
	
</div>