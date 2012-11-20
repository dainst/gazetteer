angular.module('placeapi', ['ngResource']).

factory('PlaceApi', function($resource) {
	
	var PlaceApi = $resource('/gazetteer/doc/:id.json',
		{ update: { method: 'PUT' }	}
	);

	return PlaceApi;
	
});