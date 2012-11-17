'use strict';

// Declare app level module which depends on filters, and services
angular.module('gazetteer', ['gazetteer.filters', 'gazetteer.services', 'gazetteer.directives']).
  config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/search', {templateUrl: 'partials/search.html', controller: SearchCtrl});
    $routeProvider.when('/place/:id', {templateUrl: 'partials/place.html', controller: PlaceCtrl});
    $routeProvider.otherwise({redirectTo: '/search'});
  }]);
