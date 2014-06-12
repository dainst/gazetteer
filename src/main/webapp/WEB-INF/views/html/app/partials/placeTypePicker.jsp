<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<span>

	<style type="text/css">
		.place-type-picker-field {
			background-colour: #fff;
			border-radius: 0 0 0 0;
			border: 1px solid #CCC;
			height: 20px;
			padding: 4px 6px;
			font-size: 14px;
			line-height: 20px;
			width: 210px;
			display: inline-block;
			overflow: hidden;
		}
		.place-type-picker-btn {
			margin-left: -1px;
			border-radius: 0 0 0 0;
			border: 1px solid #cccccc;
			vertical-align: top;
		}
		.place-type-picker-clickable-row {
			cursor: pointer;
		}
	</style>
	
	<span>
	
		<div class="place-type-picker-field">
			<span ng-hide="place.type">
				<em><s:message code="ui.picker.pickAPlaceType" text="ui.picker.pickAPlaceType"/></em>
			</span>
			<span ng-show="place.type">
				<div gaz-translate="'place.types.' + place.type"/>
			</span>
		</div>
		
		<button class="btn gaz-pick-button place-type-picker-btn" type="button" ng-click="openOverlay()">
			<i class="icon-list-alt"></i>
		</button>
	
	</span>
	
	<div modal="showOverlay" close="closeOverlay()">
		<div class='modal-header'>
			<button type='button' class='close' data-dismiss='modal' ng-click="closeOverlay()">×</button>
			<h3><s:message code="ui.picker.pickAPlaceType" text="ui.picker.pickAPlaceType"/></h3>
		</div>
 		<div class="modal-body">
 			<table class="table table-striped table-hover">
				<c:forEach var="placeType" items="${placeTypes}">
					<tr class="place-type-picker-clickable-row" ng-click="selectType('${placeType}')">
						<td>
							<b><span gaz-translate="'place.types.' + '${placeType}'"/></b><br/>
							<span gaz-translate="'place.types.description.' + '${placeType}'"/>
						</td>	
					</tr>
				</c:forEach>					
			</table>
 		</div>
	</div>

</span>