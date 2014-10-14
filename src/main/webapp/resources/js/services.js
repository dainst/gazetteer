'use strict';

/* Services */

var services = angular.module('gazetteer.services', ['ngResource', 'ngCookies']);

services.factory('Place', function($resource){
	return $resource(
			"../:method/:id/:id2",
			{ id: '@gazId' },
			{
				query: { method:'GET', params: { method:'search' }, isArray:false },
				extendedQuery: { method:'POST', params: { method:'search' }, isArray:false },
				distance: { method:'GET', params: { method:'geoSearch'}, isArray:false },
				children: { method:'GET', params: { method:'children'}, isArray:false },
				suggestions: { method:'GET', params: { method:'suggestions'} },
				get: { method:'GET', params: { method:'doc'} },
				save: { method:'PUT', params: { method:'doc'} },
				remove: { method: 'DELETE', params: { method: 'doc'} },
				merge: { method: 'POST', params: { method:'merge' } },
			});
});

services.factory('Thesaurus', function($resource){
	return $resource("../thesaurus/:id", { id: '@key' });
});