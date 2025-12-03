<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<span>
	<span>

		<div class="place-picker-field">
			<span ng-hide="place.gazId">
				<em><s:message code="ui.picker.pickAPlace" text="ui.picker.pickAPlace"/></em>
			</span>
			<div gaz-place-title ng-show="place.gazId" place="place"></div>
		</div><button class="btn gaz-pick-button place-picker-btn" type="button" ng-click="openOverlay()">
			<i class="icon-search"></i><i class="icon-globe"></i>
		</button>

	</span>

	<div modal="showOverlay" close="closeOverlay()">
		<div class='modal-header'>
			<button type='button' class='close' data-dismiss='modal' ng-click="closeOverlay()">×</button>
			<h3><s:message code="ui.picker.pickAPlace" text="ui.picker.pickAPlace"/></h3>
			<form class="place-picker-search" ng-submit="pickFirst()" autocomplete="off">
 				<input type="text" class="search-query" placeholder="Filter" ng-model="search.q" autocomplete="off" focus-me="showOverlay">
 				<i class="icon-search icon-white"></i>
 				<i class="icon-spinner icon-spin icon-large" style="color: #5572a1; margin-left: 7px; cursor: default;" ng-show="loading"></i>
			</form>
		</div>
 		<div class="modal-body">
 			<table class="table table-striped">
				<tr ng-repeat="place in places" ng-click="selectPlace(place)">
					<td>{{place.gazId}}</td>
					<td>
						<span>
							<a href="">
								{{place.prefName.title}}<em><small ng-repeat="name in place.names | orderBy: ['sort'] | limitTo: 3">,
									{{name.title}}</small><small ng-show="place.names.length > 3">, ...</small></em>
							</a>
						</span>
						<small class="muted">{{parents[place.parent].prefName.title}}</small>
					</td>
					<td><span ng-show="place.type && place.type.length > 0" gaz-translate="'place.types.' + place.types[0]"></span></td>
				</tr>
			</table>
 		</div>
	</div>
</span>
