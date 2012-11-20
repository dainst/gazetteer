'use strict';

/* Controllers */

function SearchBoxCtrl($scope, $location) {
	
	$scope.q = "";
	
	$scope.submit = function() {
		$location.path('/search').search({q:$scope.q});
	};
	
}

function SearchCtrl($scope, $location, $routeParams, Place) {
	
	$scope.search = {
		offset: ($routeParams.offset) ? parseInt($routeParams.offset) : 0,
		limit: ($routeParams.limit) ? parseInt($routeParams.limit) : 10,
		q: ($routeParams.q) ? ($routeParams.q) : "" 
	};
	
	$scope.places = [];
	$scope.total = 0;
	
	$scope.page = function() {
		return $scope.search.offset / $scope.search.limit + 1;
	};
	
	$scope.totalPages = function() {
		return ($scope.total - ($scope.total % $scope.search.limit)) / $scope.search.limit + 1;
	};
	
	$scope.setLimit = function(limit) {
		$scope.search.limit = limit;
		$scope.search.offset = 0;
		$location.search($scope.search);
	};
	
	$scope.prevPage = function() {
		if ($scope.page() > 1) {
			$scope.search.offset = $scope.search.offset - $scope.search.limit;
			$location.search($scope.search);
		}
	};
	
	$scope.nextPage = function() {
		if ($scope.page() < $scope.totalPages()) {
			$scope.search.offset = $scope.search.offset + $scope.search.limit;
			$location.search($scope.search);
		}
	};
	
	$scope.submit = function() {
		Place.query($scope.search, function(result) {
			$scope.places = result.result;
			$scope.total = result.total;
		});
	};
	
	$scope.submit();

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
	
	$scope.save = function() {
		Place.save($scope.place);
	};
	
	$scope.addComment = function() {
		if (!$scope.comment.text || !$scope.comment.language) return;
		if ($scope.place.comments == undefined)
			$scope.place.comments = [];
		$scope.place.comments.push($scope.comment);
		$scope.comment = {};
	};
	
	$scope.addName = function() {
		if (!$scope.name.title) return;
		if ($scope.place.names == undefined)
			$scope.place.names = [];
		$scope.place.names.push($scope.name);
		$scope.name = {};
	};

}