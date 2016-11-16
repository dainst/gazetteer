<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<span>
	<span>
		<div class="shape-editor-field" ng-style="{ 'background-color': backgroundColor }">
			<em ng-show="shape"><s:message code="ui.shapeEditor.editPolygon" text="ui.shapeEditor.editPolygon" /></em>
			<em ng-hide="shape"><s:message code="ui.shapeEditor.createPolygon" text="ui.shapeEditor.createPolygon" /></em>
		</div>
		<button ng-hide="deactivated" class="btn gaz-pick-button location-edit-btn" type="button" ng-click="openMapOverlay()">
			<i class="icon-pencil"></i>
		</button>
		<button ng-show="deactivated" class="btn gaz-pick-button location-edit-btn disabled" type="button">
			<i class="icon-pencil"></i>
		</button>
		<button ng-hide="deactivated" class="btn gaz-pick-button location-edit-btn" ng-click="openTextInputOverlay()">
			<span class="fa fa-i-cursor"></span>
		</button>
		<button ng-show="deactivated" class="btn gaz-pick-button location-edit-btn disabled">
			<span class="fa fa-i-cursor"></span>
		</button>
		<button ng-show="shape && !deactivated" class="btn location-edit-btn" href="#deleteShapeModal_{{editorName}}" data-toggle="modal">
			<i class="icon-remove"></i>
		</button>
		<button ng-hide="shape && !deactivated" class="btn location-edit-btn disabled">
			<i class="icon-remove"></i>
		</button>
	</span>
	
	<div modal="showMapOverlay" close="closeMapOverlay()">
		<div class="modal-header">
			<button type="button" class="close" data-dismiss="modal" ng-click="closeMapOverlay()">×</button>
			<h3 ng-show="shape"><s:message code="ui.shapeEditor.editPolygon" text="ui.shapeEditor.editPolygon" /></h3>
			<h3 ng-hide="shape"><s:message code="ui.shapeEditor.createPolygon" text="ui.shapeEditor.createPolygon" /></h3>
		</div>
 		<div class="modal-body gmap">
 			<div id="shape_editor_map_canvas" style="height: 400px" ui-map="map" ui-options="mapOptions" ng-mousemove="resize()" ng-mouseup="setUpdateMapPropertiesTimer()"></div>
 		</div>
 		<div class="modal-footer">
 			<button type="button" class="btn btn-primary" ng-click="saveShape()"><s:message code="ui.ok" text="ui.ok" /></button>
 			<button type="button" class="btn" ng-click="closeMapOverlay()"><s:message code="ui.cancel" text="ui.cancel" /></button>
 		</div>
	</div>
	
	<div modal="showTextInputOverlay" close="closeTextInputOverlay()">
		<span>
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal"
					aria-hidden="true">&times;</button>
				<h3><s:message code="ui.inputShapeCoordinates" text="ui.inputShapeCoordinates"/></h3>
			</div>
			<div class="modal-body">
				<textarea type="text" rows="10" style="width: 100%; box-sizing: border-box" ng-model="coordinatesString" />
				<select ng-model="coordinatesStringFormat" style="margin-top: 10px;">
					<option value="geojson">GeoJSON</option>
					<option value="wkt">WKT</option>
				</select>
			</div>
			<div class="modal-footer">
				<a href="#" class="btn" ng-click="closeTextInputOverlay()"><s:message code="ui.cancel" text="ui.cancel"/></a>
				<a ng-click="parseCoordinatesString()" class="btn btn-primary"><s:message code="ui.save" text="ui.save"/></a>
			</div>
			<div ng-if="parsingError">
				<s:message code="ui.shapeEditor.error.{{parsingError.msgKey}}" text="ui.shapeEditor.error.{{parsingError.msgKey}}"/>
			</div>
		</span>
	</div>
	
	<div class="modal hide fade" id="deleteShapeModal_{{editorName}}">
		<span>
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal"
					aria-hidden="true">&times;</button>
				<h3><s:message code="ui.deleteShape" text="ui.deleteShape"/>?</h3>
			</div>
			<div class="modal-body">
				<p><s:message code="ui.deleteShape.really" text="ui.deleteShape.really"/></p>
			</div>
			<div class="modal-footer">
				<a href="#" class="btn" data-dismiss="modal" aria-hidden="true"><s:message code="ui.cancel" text="ui.cancel"/></a>
				<a ng-click="deleteShape(shape)" data-dismiss="modal" class="btn btn-danger"><s:message code="ui.delete" text="ui.delete"/></a>
			</div>
		</span>
	</div>
</span>