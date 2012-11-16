'use strict';

/* Controllers */

function SearchCtrl($scope, $routeParams, Place) {
	
	$scope.offset = 0;
	$scope.limit = 10;
	$scope.q = "";
	$scope.places = [];
	
	$scope.page = function() {
		return $scope.offset / $scope.limit + 1;
	};
	
	$scope.search = function() {
		$scope.places = Place.query({
			offset: $scope.offset,
			limit: $scope.limit,
			q: $scope.q
		});
	};
	
	$scope.search();

}


function PlaceCtrl($scope, $routeParams, Place) {
	
	$scope.place = Place.get({phoneId: $routeParams.phoneId});

}