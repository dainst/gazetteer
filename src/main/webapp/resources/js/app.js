'use strict';

// Declare app level module which depends on filters, and services
angular.module('gazetteer', ['gazetteer.filters', 'gazetteer.services', 'gazetteer.directives', 'ui']).
  config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/search', { templateUrl: 'partials/search.html', reloadOnSearch: false, controller: SearchCtrl});
    $routeProvider.when('/extended-search', { templateUrl: 'partials/extendedSearch.html', controller: ExtendedSearchCtrl});
    $routeProvider.when('/show/:id', { templateUrl: 'partials/show.html', controller: PlaceCtrl});
    $routeProvider.when('/edit/:id', { templateUrl: 'partials/edit.html', controller: PlaceCtrl});
    $routeProvider.when('/merge/:id', { templateUrl: 'partials/merge.html', controller: MergeCtrl});
    $routeProvider.otherwise({ redirectTo: '/search'});
  }]);
