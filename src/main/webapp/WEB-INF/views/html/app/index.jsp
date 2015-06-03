<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="gaz" tagdir="/WEB-INF/tags/layout" %>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<!doctype html>
<html ng-app="gazetteer" ng-controller="AppCtrl">
<head>
<title ng-bind="pageTitle">iDAI.gazetteer</title>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="icon" href="../resources/ico/favicon.ico" type="image/x-icon">
<link rel="apple-touch-icon" sizes="144x144" href="../resources/ico/apple-touch-icon-144.png">
<link rel="apple-touch-icon" sizes="114x114" href="../resources/ico/apple-touch-icon-114.png">
<link rel="apple-touch-icon" sizes="72x72" href="../resources/ico/apple-touch-icon-72.png">
<link rel="apple-touch-icon" href="../resources/ico/apple-touch-icon-57.png">
<link href="//arachne.uni-koeln.de/archaeostrap/assets/css/bootstrap.css" rel="stylesheet">
<link href="//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.1/css/bootstrap-responsive.min.css" rel="stylesheet">
<link href="//netdna.bootstrapcdn.com/font-awesome/3.0.2/css/font-awesome.css" rel="stylesheet">
<link href="//netdna.bootstrapcdn.com/font-awesome/3.0.2/css/font-awesome-ie7.css" rel="stylesheet">
<link href="../resources/css/angular-ui.css" rel="stylesheet">
<link href="../resources/css/app.css" rel="stylesheet">
<script	src="//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.2.26/angular.min.js"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.2.26/angular-resource.min.js"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.2.26/angular-cookies.min.js"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.2.26/angular-route.min.js"></script>
<script src="../resources/js/lib/angular/angular-ui.js"></script>
<script src="../resources/js/lib/angular/ui-bootstrap-custom-0.4.0.min.js"></script>
</head>
<body class="ng-cloak">

	<div scroll-position="scrollPosition"></div>
	
	<div class="container">
	
		<div class="archaeo-fixed-menu">
			<div class="gaz-container archaeo-fixed-menu-header">
				<sec:authorize access="isAnonymous()">
					<div class="btn-group pull-right" style="margin-top:12px">
						<a href="javascript:window.location.href='../login?r=' + window.location.hash.substring(3);" class="btn btn-small btn-primary">
							<s:message code="ui.login" text="ui.login"/>
						</a>
						<a href="javascript:window.location.href='../register?r=' + window.location.hash.substring(3);" class="btn btn-small btn-primary">
							<s:message code="ui.register" text="ui.register"/>
						</a>
					</div>
				</sec:authorize>
				<sec:authorize access="isAuthenticated()">
					<div class="btn-group pull-right" style="margin-top:12px">
						<button type="button" class="btn btn-primary btn-small dropdown-toggle" data-toggle="dropdown">
	   						<sec:authentication property="principal.username" /> <span class="caret"></span>
						</button>	
						<ul class="dropdown-menu pull-right" role="menu">
		   					<li>
	   							<a href="javascript:window.location.href='../editUser?username=${pageContext['request'].userPrincipal.name}&r=' + window.location.hash.substring(3);">
	   								<s:message code="ui.userSettings" text="ui.userSettings"/>
	   							</a>
	   						</li>
	   						<sec:authorize access="hasRole('ROLE_EDITOR')">
	   							<li>
	   								<a href="../globalChangeHistory">
	   									<s:message code="ui.globalChangeHistory" text="ui.globalChangeHistory"/>
	   								</a>
	   							</li>
	   						</sec:authorize>
	   						<sec:authorize access="hasRole('ROLE_ADMIN')">
	   							<li>
	   								<a href="../userManagement">
	   									<s:message code="ui.userManagement" text="ui.userManagement"/>
	   								</a>
	   							</li>
	   						</sec:authorize>
	   						<sec:authorize access="hasRole('ROLE_ADMIN')">
	   							<li>
	   								<a href="../recordGroupManagement">
	   									<s:message code="ui.recordGroupManagement" text="ui.recordGroupManagement"/>
	   								</a>
	   							</li>
	   						</sec:authorize>
	   						<li class="divider"></li>
	  						<li>
	   							<a href="../logout">
	   								<s:message code="ui.logout" text="ui.logout"/>
	   							</a>
	   						</li>   					
						</ul>
					</div>
				</sec:authorize>
				<div id="archaeo-fixed-menu-logo"></div>
				<h3 class="pull-left">
					<small>Deutsches Archäologisches Institut</small> <br>
					<a href="#!/home" style="color:inherit">iDAI.gazetteer</a>
				</h3>
			</div>
			<div class="affix-menu-wrapper">
				<div id="affix-menu" style="z-index: 100000"
					class="navbar navbar-inverse gaz-container" data-spy="affix">
					<div class="navbar-inner">
						<div id="archaeo-fixed-menu-icon"></div>
						<a class="btn btn-navbar" data-toggle="collapse"
							data-target=".nav-collapse"> <span class="icon-bar"></span> <span
							class="icon-bar"></span> <span class="icon-bar"></span>
						</a>
						<a class="brand" href="../">iDAI.gazetteer</a>
						<div class="nav-collapse pull-left">
							<ul class="nav">
								<li><a href="#!/thesaurus/"><s:message
											code="ui.thesaurus.list" text="ui.thesaurus.list" /></a></li>
								<li><a href="#!/extended-search/"> <s:message
											code="ui.search.extendedSearch" text="ui.search.extendedSearch" />
								</a></li>
								<sec:authorize access="hasRole('ROLE_EDITOR')">
									<li><a href="#!/create/"> <s:message
												code="ui.place.create" text="ui.place.create" />
									</a></li>
								</sec:authorize>							
								<sec:authorize access="hasRole('ROLE_REISESTIPENDIUM')">
									<li><a href="#!/search?q=%7B%22bool%22:%7B%22must%22:%5B%7B%22query_string%22:%7B%22query%22:%22_exists_:noteReisestipendium%22%7D%7D%5D%7D%7D&type=extended"> <s:message
												code="ui.search.reisestipendium" text="ui.search.reisestipendium" />
									</a></li>
								</sec:authorize>
							</ul>
							<form novalidate class="navbar-search pull-left" ng-show="showNavbarSearch" ng-submit="submit()">
								<s:message code="ui.search.simpleSearch" text="ui.search.simpleSearch"
									var="titleSimpleSearch" />
								
								<div style="width:206px; position:relative;">
									<input type="text" class="search-query input-block-level" name="searchField" ng-model="q"	placeholder="${titleSimpleSearch}" 
										on-arrow-up="selectPreviousSuggestion()" on-arrow-down="selectNextSuggestion()" on-blur="lostFocus()" autocomplete="off" focus-me="isFocused"> <i class="icon-search"></i>
										
									<div name="suggestionsContainer" class="suggestion-menu" ng-show="searchSuggestions">
										<div ng-repeat="suggestion in searchSuggestions">
											<div class="suggestion" ng-mousedown="submit()" ng-hide="selectedSuggestionIndex == $index" ng-mouseover="setSelectedSuggestionIndex($index)"><span ng-show="suggestion.length < 30">{{suggestion}}</span><span ng-hide="suggestion.length < 30">{{suggestion.substring(0,29)}}...</span></div>
											<div class="suggestion selected" ng-mousedown="submit()" ng-show="selectedSuggestionIndex == $index"><span ng-show="suggestion.length < 30">{{suggestion}}</span><span ng-hide="suggestion.length < 30">{{suggestion.substring(0,29)}}...</span></div>
										</div>
									</div>
								</div>
								
							</form>
							<div style="margin-top:8px; margin-left: 10px; float: right;" ng-show="loading > 0">
								<i class="icon-spinner icon-spin icon-large" style="color:white; cursor: default;"></i>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
		
		<div class="gaz-container" name="container">
		
			<c:if test="${successMessage eq 'register'}">
				<div class="alert alert-success">
					<s:message code="ui.register.success" text="ui.register.success" />
				</div>
			</c:if>
			<c:if test="${successMessage eq 'passwordChangeRequest'}">
				<div class="alert alert-success">
					<s:message code="ui.passwordChangeRequest.success" text="ui.passwordChangeRequest.success" />
				</div>
			</c:if>
		
			<div class="alerts" ng-cloak ng-hide="alerts.length == 0">
				<div ng-repeat="alert in alerts" class="alert" ng-class="alert.alertClass">
					<button type="button" class="close" ng-click="alerts.splice($index,1)">&times;</button>
					<h4 ng-show="alert.head">{{alert.head}}</h4>
					{{alert.body}}
				</div>
			</div>
		
			<!-- Page title -->
			<div ng-show="showHeader" class="page-header">
				<h2>
					<span ng-show="title">{{title}}</span>
					<span ng-hide="title">&nbsp;</span>
					<small ng-bind-html="subtitle | toTrusted"></small>
				</h2>
			</div>
			
			<div class="row-fluid" style="position:relative;">
				<div class="span5" id="map-well-wrapper" ng-style="mapContainerStyle">
					<div class="well" id="map-well">
						<div gaz-map map="$root.map" places="activePlaces" height="500" zoom="zoom" bbox="bbox" highlight="highlight" mode="mapMode" ng-mouseup="setUpdateMapPropertiesTimer()"></div>
					</div>
				</div>
				
				<div class="{{viewClass}}" ng-view style="min-height: 530px;"></div>
				
			</div>
			
			<!-- Footer -->
			<gaz:footer/>
			
		</div>
		
	</div>
	
	<script src='//maps.google.com/maps/api/js?key=${googleMapsApiKey}&amp;sensor=false&libraries=visualization,drawing'></script>
	
	<script src="../resources/bootstrap/js/bootstrap.min.js"></script>
	
	<script src="../resources/js/custom.js"></script>
	
	<script src="../resources/js/app.js"></script>
	<script src="../resources/js/services.js"></script>
	<script src="../resources/js/controllers.js"></script>
	<script src="../resources/js/filters.js"></script>
	<script src="../resources/js/directives.js"></script>
	<script src="../resources/js/i18n/messages_${language}.js"></script>
	

	
</body>
</html>
