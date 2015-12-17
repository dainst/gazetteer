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
	
<!-- 	<div class="gaz-pick-overlay-inner" ng-show="showOverlay"> -->
<!-- 		<div class="navbar navbar-inverse"> -->
<!-- 			<div class="navbar-inner"> -->
<!-- 				<a href="" ng-click="closeOverlay()" class="pull-left icon-large" style="color:black"> -->
<!-- 					<i class="icon-remove-sign"></i> -->
<!-- 				</a> -->
<!-- 				<form class="navbar-search pull-left" action="/gazetteer/place" autocomplete="off"> -->
<!-- 	 				<input type="text" class="search-query" placeholder="Suche" ng-model="search.q" autocomplete="off"> -->
<!-- 	 				<i class="icon-search icon-white"></i> -->
<!-- 				</form> -->
<!-- 			</div> -->
<!-- 		</div> -->
<!-- 		<div class="gaz-pick-results"> -->
<!-- 			<div class="gaz-pick-result-row" ng-repeat="place in places"> -->
<!-- 				<a ng-click="selectPlace(place)">{{place.prefName.title}} <em><small>&#35;{{place.gazId}}</small></em></a> -->
<!-- 			</div> -->
<!-- 		</div> -->
<!-- 	</div> -->

</span>