<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<span>
	<span>
		<input type="text" ng-model="coordinatesText" ng-disabled="deactivated" on-enter="checkForGeocoding()"></input>
		<button ng-hide="deactivated" class="btn gaz-pick-button location-edit-btn" type="button" ng-click="openOverlay()">
			<i class="icon-map-marker"></i>
		</button>
		<button ng-show="deactivated" class="btn gaz-pick-button location-edit-btn" type="button" ng-click="openOverlay()" disabled>
			<i class="icon-map-marker"></i>
		</button>
	</span>
	
	<div modal="showOverlay" close="closeOverlay()">
		<div class='modal-header'>
			<button type='button' class='close' data-dismiss='modal' ng-click="closeOverlay()">×</button>
			<h3><s:message code="ui.locationPicker.heading" text="ui.locationPicker.heading" /></h3>
		</div>
 		<div class="modal-body gmap">
 			<div id="shape_editor_map_canvas" style="height: 400px" ui-map="map" ui-options="mapOptions" ng-mousemove="resize()" ng-mouseup="setUpdateMapPropertiesTimer()"></div>
 		</div>
 		<div class="modal-footer">
 			<button type="button" class="btn btn-primary" ng-click="saveCoordinates()"><s:message code="ui.ok" text="ui.ok" /></button>
 			<button type="button" class="btn" ng-click="closeOverlay()"><s:message code="ui.cancel" text="ui.cancel" /></button>
 		</div>
	</div>
</span>