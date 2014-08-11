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
		.place-type-picker-modal {
			overflow: hidden;
		}
		.place-type-picker-typelist {
			margin-left: 25px;
		}
	</style>
	
	<span>
	
		<div class="place-type-picker-field">
			<span ng-hide="place.types.length > 0">
				<em><s:message code="ui.picker.pickAPlaceType" text="ui.picker.pickAPlaceType"/></em>
			</span>
			<span ng-show="place.types.length > 0">
				<span gaz-translate="'place.types.' + place.types[0]"/> <span ng-show="place.types.length > 1">...</span>
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
 		<div class="modal-body place-type-picker-modal">
 			<div class="place-type-picker-typelist">
				<c:forEach var="placeTypeGroup" items="${placeTypeGroups}" varStatus="groupStatus">
					<b><span gaz-translate="'place.types.groups.' + '${placeTypeGroup}'"/></b><br/>
					<c:forEach var="placeTypeGroupId" items="${placeTypeGroupIds}" varStatus="idStatus">
						<c:if test="${groupStatus.index == placeTypeGroupId}">
							<label class="checkbox inline">
								<input type="checkbox" ng-click="add('${placeTypes[idStatus.index]}')" ng-checked="isChecked('${placeTypes[idStatus.index]}')"/>
								<span gaz-translate="'place.types.' + '${placeTypes[idStatus.index]}'"/>
								<i class="icon-info-sign" style="color: #5572a1;" gaz-tooltip="'place.types.description.' + '${placeTypes[idStatus.index]}'"></i>
							</label>						
							<br/>
						</c:if>
					</c:forEach>
					<br/>
				</c:forEach>
			</div>
 		</div>
	</div>

</span>