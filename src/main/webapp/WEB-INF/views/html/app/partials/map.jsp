<div>

	<div ng-repeat="(id, marker) in markerMap" ui-map-marker="marker"
		ui-event="{'map-click': 'markerClick(id)', 'map-mouseover': 'markerOver(id, marker)', 'map-mouseout': 'markerOut()'}">
	</div>

	<div id="map_canvas" style="height: {{height}}px" ui-map="map" ui-options="mapOptions"></div>

</div>