'use strict';

/* Services */


//Demonstrate how to register services
//In this case it is a simple value service.
angular.module('gazetteer.services', ['ngResource']).
factory('Place', function($resource){
	return $resource(
			"../:method/:id",
			{ id: '@gazId' },
			{
				query: {method:'GET', params:{method:'search'}, isArray:false}
			});
});
