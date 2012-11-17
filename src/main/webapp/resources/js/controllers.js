'use strict';

/* Controllers */

function SearchBoxCtrl($scope, $location) {
	
	$scope.q = "";
	
	$scope.submit = function() {
		$location.path('/search').search({q:$scope.q});
	};
	
}

function SearchCtrl($scope, $routeParams, Place) {
	
	$scope.title = "Suche"
	$scope.offset = 0;
	$scope.limit = 10;
	$scope.q = ($routeParams.q) ? ($routeParams.q) : "";
	$scope.places = [];
	$scope.total = 0;
	
	$scope.page = function() {
		return $scope.offset / $scope.limit + 1;
	};
	
	$scope.totalPages = function() {
		return ($scope.total - ($scope.total % $scope.limit)) / $scope.limit + 1;
	};
	
	$scope.setLimit = function(limit) {
		$scope.limit = limit;
		$scope.search();
	};
	
	$scope.prevPage = function() {
		if ($scope.page() > 1) {
			$scope.offset = $scope.offset-$scope.limit;
			$scope.search();
		}
	};
	
	$scope.nextPage = function() {
		if ($scope.page() < $scope.totalPages()) {
			$scope.offset = $scope.offset+$scope.limit;
			$scope.search();
		}
	};
	
	$scope.search = function() {
		Place.query({
			offset: $scope.offset,
			limit: $scope.limit,
			q: $scope.q
		}, function(result) {
			$scope.places = result.result;
			$scope.total = result.total;
		});
	};
	
	$scope.search();

}


function PlaceCtrl($scope, $routeParams, Place, $http) {
	
	$scope.place = Place.get({
		id: $routeParams.id
	}, function(result) {
		$http.get(result.parent).success(function(result) {
			$scope.parent = result;
			console.log(result);
		});
	});

}