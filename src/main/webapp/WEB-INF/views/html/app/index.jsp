<!doctype html>
<html lang="en" ng-app="myApp">
<head>
  <meta charset="utf-8">
  <title>My AngularJS App</title>
  <link rel="stylesheet" href="../resources/css/app.css"/>
</head>
<body>
  <ul class="menu">
    <li><a href="#/view1">view1</a></li>
    <li><a href="#/view2">view2</a></li>
  </ul>

  <div ng-view></div>

  <div>Angular seed app: v<span app-version></span></div>

  <!-- In production use:
  <script src="//ajax.googleapis.com/ajax/libs/angularjs/1.0.2/angular.min.js"></script>
  -->
  <script src="../resources/js/lib/angular/angular.js"></script>
  <script src="../resources/js/app.js"></script>
  <script src="../resources/js/services.js"></script>
  <script src="../resources/js/controllers.js"></script>
  <script src="../resources/js/filters.js"></script>
  <script src="../resources/js/directives.js"></script>
</body>
</html>
