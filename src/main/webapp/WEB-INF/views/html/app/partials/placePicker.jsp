<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<span>

	<style type="text/css">
		.place-picker-field {
			background-colour: #fff;
			border-radius: 0 0 0 0;
			border: 1px solid #CCC;
			height: 20px;
			padding: 4px 6px;
			font-size: 14px;
			line-height: 20px;
			width: 210px;
			display: inline-block;
		}
		.place-picker-btn {
			margin-left: -1px;
			border-radius: 0 0 0 0;
			border: 1px solid #cccccc;
			vertical-align: top;
		}
	</style>
	
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
			<form class="navbar-search" style="position:static; float:none;" action="/gazetteer/place" autocomplete="off">
 				<input type="text" class="search-query" placeholder="Filter" ng-model="search.q" autocomplete="off">
 				<i class="icon-search icon-white"></i>
			</form>
		</div>
 		<div class="modal-body">
 			<table class="table table-striped">
				<tr ng-repeat="place in places" ng-click="selectPlace(place)">
					<td>{{place.gazId}}</td>
					<td>
						<span>
							<a href="">
								{{place.prefName.title}}<em><small ng-repeat="name in place.names.slice(0,3)">, 
									{{name.title}}</small><small ng-show="place.names.length > 3">, ...</small></em>
							</a>
						</span>
						<small class="muted">{{parents[place.parent].prefName.title}}</small>
					</td>
					<td><span ng-show="place.type" gaz-translate="'place.types.' + place.type"></span></td>
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