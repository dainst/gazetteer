'use strict';

/* Services */

var services = angular.module('gazetteer.services', ['ngResource']);

services.factory('Place', function($resource){
	return $resource(
			"../:method/:id",
			{ id: '@gazId' },
			{
				query: { method:'GET', params: { method:'search' }, isArray:false },
				get: { method:'GET', params: { method:'doc'} }
			});
});
