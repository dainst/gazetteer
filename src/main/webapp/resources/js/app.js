'use strict';

// Declare app level module which depends on filters, and services
angular.module('gazetteer', ['gazetteer.filters',
                             'gazetteer.services',
                             'gazetteer.directives',
                             'ngRoute',
                             'ui',
                             'ngSanitize',
                             'ng-showdown']).
  config(['$routeProvider', function($routeProvider) {
	$routeProvider.when('/home', { templateUrl: 'partials/home.html', controller: HomeCtrl });
    $routeProvider.when('/search', { templateUrl: 'partials/search.html', reloadOnSearch: false, controller: SearchCtrl });
    $routeProvider.when('/extended-search', { templateUrl: 'partials/extendedSearch.html', controller: ExtendedSearchCtrl });
    $routeProvider.when('/thesaurus', { templateUrl: 'partials/thesaurus.html', controller: ThesaurusCtrl });
    $routeProvider.when('/show/:id', { templateUrl: 'partials/show.html', controller: PlaceCtrl });
    $routeProvider.when('/edit/:id/:msg?', { templateUrl: 'partials/edit.html', controller: PlaceCtrl });
    $routeProvider.when('/create', { templateUrl: 'partials/create.html', controller: CreateCtrl });
    $routeProvider.when('/merge/:id', { templateUrl: 'partials/merge.html', controller: MergeCtrl });
    $routeProvider.when('/change-history/:id', { templateUrl: 'partials/changeHistory.html', controller: PlaceCtrl });
    $routeProvider.when('/about', { templateUrl: 'partials/about.html', controller: AboutCtrl });
    $routeProvider.when('/help', { templateUrl: 'partials/help.html', controller: HelpCtrl });
    $routeProvider.otherwise({ redirectTo: '/home'});
  }]).
  config(['$locationProvider', function($locationProvider) {
	$locationProvider.hashPrefix('!');
  }]);