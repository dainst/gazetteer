<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="gaz" tagdir="/WEB-INF/tags/layout" %>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

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
<c:if test="${successMessage eq 'changePassword'}">
	<div class="alert alert-success">
		<s:message code="ui.changePassword.success" text="ui.changePassword.success" />
	</div>
</c:if>
	
<div id="home_map_canvas" style="height: 400px"></div>		
		
<div style="position:relative; top:-235px; z-index:10; text-align:center;">
	<h1 style="font-size: 60px; text-shadow: 0 1px 5px #000000; color:white; margin-bottom: 150px;">
		iDAI.gazetteer
	</h1>
	<form class="form-search simpleSearchForm" ng-submit="submit()" style="margin:0;">
		<div class="well" style="display:inline-block; text-align:left;">
			<div style="position:relative;">
				<div class="input-append">
					<s:message code="ui.search.simpleSearch" text="ui.search.simpleSearch"
							var="titleSimpleSearch" />
					<input class="search-query input-xxlarge" name="homeSearchField" ng-model="searchFieldInput" placeholder="${titleSimpleSearch}" 
							on-arrow-up="selectPreviousSuggestion()" on-arrow-down="selectNextSuggestion()" on-blur="lostFocus()"
							type="text" autocomplete="off" focus-me="true">
					<button class="btn btn-primary" type="submit"><i class="icon-search"></i></button>
				</div>		
				<div class="suggestion-menu" ng-show="homeSearchSuggestions">
					<div ng-repeat="suggestion in homeSearchSuggestions | sortAlphabetically">
						<div class="suggestion" ng-mousedown="submit()" ng-hide="selectedSuggestionIndex == $index" ng-mouseover="setSelectedSuggestionIndex($index)">{{suggestion}}</div>
						<div class="suggestion selected" ng-mousedown="submit()" ng-show="selectedSuggestionIndex == $index">{{suggestion}}</div>
					</div>
				</div>
			</div>
		</div>
	</form>
</div>
		
<div class="row-fluid" style="margin-top:-220px">
	<div class="span12">
		<c:choose>
			<c:when test="${language eq 'ar'}">
		 		<p dir="rtl" lang="ar" class="lead"><s:message code="home.info1" text="home.info1" /></p>
		 	</c:when>
		 	<c:otherwise>
		 		<p class="lead"><s:message code="home.info1" text="home.info1" /></p>
		 	</c:otherwise>
		 </c:choose>
	</div>
</div>

<div>
	<iframe height="315" width="600" src="<s:message code="home.videoLink" text="home.videoLink" />"
		frameborder="0" class="youtubeIframe" allowfullscreen>Introduction</iframe>
</div>

<div class="row-fluid" style="margin-top: 30px;">
	<div class="span12">
		<c:choose>
			<c:when test="${language eq 'ar'}">
				<p dir="rtl" lang="ar" style="max-width: 500px; margin: 0 auto; display: block;"><s:message code="home.info2" text="home.info2" /></p>
			</c:when>
			<c:otherwise>
				<p style="max-width: 1000px; margin: 0 auto; display: block;"><s:message code="home.info2" text="home.info2" /></p>
			</c:otherwise>
		</c:choose>
	</div>
</div>