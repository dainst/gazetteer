<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<span>
	<style type="text/css">
		.shape-editor-field {
			background-colour: #fff;
			border-radius: 0 0 0 0;
			border: 1px solid #CCC;
			height: 20px;
			padding: 4px 6px;
			font-size: 14px;
			line-height: 20px;
			width: 206px;
			display: inline-block;
			overflow: hidden;
		}
		.shape-editor-btn {
			margin-left: -3px;
			border-radius: 0 0 0 0;
			border-top: 1px solid #cccccc;
			border-bottom: 1px solid #cccccc;
			border-right: 1px solid #cccccc;
			vertical-align: top;
			text-align: center;
			display: inline-block;
			padding: 4px 9px 4px 10px;
		}
	</style>
	
	<span>
		<div class="shape-editor-field">
			<em ng-show="shape"><s:message code="ui.shapeEditor.editPolygon" text="ui.shapeEditor.editPolygon" /></em>
			<em ng-hide="shape"><s:message code="ui.shapeEditor.createPolygon" text="ui.shapeEditor.createPolygon" /></em>
		</div>
		<button class="btn gaz-pick-button shape-editor-btn" type="button" ng-click="openOverlay()">
			<i class="icon-pencil"></i>
		</button>
		<button ng-show="shape" class="btn shape-editor-btn" href="#deleteShapeModal_{{editorName}}" data-toggle="modal">
			<i class="icon-remove"></i>
		</button>
		<button ng-hide="shape" class="btn shape-editor-btn disabled">
			<i class="icon-remove"></i>
		</button>
	</span>
	
	<div modal="showOverlay" close="closeOverlay()">
		<div class='modal-header'>
			<button type='button' class='close' data-dismiss='modal' ng-click="closeOverlay()">×</button>
			<h3 ng-show="shape"><s:message code="ui.shapeEditor.editPolygon" text="ui.shapeEditor.editPolygon" /></h3>
			<h3 ng-hide="shape"><s:message code="ui.shapeEditor.createPolygon" text="ui.shapeEditor.createPolygon" /></h3>
		</div>
 		<div class="modal-body gmap">
 			<div id="shape_editor_map_canvas" style="height: 400px" ui-map="map" ui-options="mapOptions" ng-mousemove="resize()"></div>
 		</div>
 		<div class="modal-footer">
 			<button type="button" class="btn btn-primary" ng-click="saveShape()"><s:message code="ui.ok" text="ui.ok" /></button>
 			<button type="button" class="btn" ng-click="closeOverlay()"><s:message code="ui.cancel" text="ui.cancel" /></button>
 		</div>
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
	</div>
</span>