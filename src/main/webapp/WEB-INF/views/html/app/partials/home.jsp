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
			<div class="input-append">
				<s:message code="ui.search.simpleSearch" text="ui.search.simpleSearch"
						var="titleSimpleSearch" />
				<input class="search-query input-xxlarge" name="homeSearchField" ng-model="searchFieldInput" placeholder="${titleSimpleSearch}" 
						on-arrow-up="selectPreviousSuggestion()" on-arrow-down="selectNextSuggestion()" on-blur="lostFocus()"
						type="text" autocomplete="off" focus-me="true">
				<button class="btn btn-primary" type="submit"><i class="icon-search"></i></button>
			</div>
		</div>
	</form>
</div>
	
<div ng-style="suggestionsStyle" class="suggestion-menu" ng-show="homeSearchSuggestions">
	<div ng-repeat="suggestion in homeSearchSuggestions">
		<div class="suggestion" ng-mousedown="submit()" ng-hide="selectedSuggestionIndex == $index" ng-mouseover="setSelectedSuggestionIndex($index)">{{suggestion}}</div>
		<div class="suggestion selected" ng-mousedown="submit()" ng-show="selectedSuggestionIndex == $index">{{suggestion}}</div>
	</div>
</div>
		
<div class="row-fluid" style="margin-top:-220px">
	<div class="span12">
		 <p class="lead">Der DAI-Gazetteer ist ein Webservice, der Ortsnamen mit Koordinaten verbindet und in zwei Richtungen wirken soll. Nach innen dient er als Normdatenvokabular für sämtliche ortsbezogenen Informationen und Informationssysteme des DAI. Nach außen soll er diese mit den weltweiten Gazetteer-Systemen verbinden. Weitere Funktionen sind in einem <a href="http://youtu.be/mISUGMFkQvU" target="_blank">Screencast</a> zusammengefasst.</p>
	</div>
</div>
		
<div class="row-fluid">
	<div class="span6">
    	<p>Der DAI-Gazetteer ist außerdem ein Werkzeug, um die Ortsdaten-Struktur innerhalb des DAI sukzessive zu optimieren, d. h. sowohl die Zahl der mit Ortsdaten versehenen Informationsobjekte zu erhöhen, diese dann in die weltweiten Ortsdatensysteme einzubinden, und auch die im DAI schon vorhandenen Informationsobjekte mit Ortsdaten zu vereinheitlichen. Der DAI-Gazetteer ist somit der Auftakt zu einem großen, neuen Querschnitts-Arbeitsfeld.</p>
     	<p>Geodaten sind ein hinreichend vereinbarungsfähiges, aber auch umfassend genug anwendbares Kontextualisierungskriterium. Ihre Bedeutung für die Kontextualisierung nimmt zu, wenn über die bidirektionale Verknüpfung hinaus eine Drei- oder Vielecksverknküpfung zustande kommt. Daher ist der Gazetteer u. a. auch eine Kontextualisierungsmaschine, die ortsbasierte Suchen über mehrere Informationssysteme hinweg erlaubt, etwa über <a href="http://arachne.uni-koeln.de" target="_blank">Arachne</a> und <a href="http://opac.dainst.org" target="_blank">ZENON</a>.</p>
    	<p>Inhaltliche Ergänzungen und Korrekturvorschläge senden Sie bitte an: <a href="mailto:gazetteer-eingabe@dainst.de">gazetteer-eingabe@dainst.de</a>.</p>
	</div>
	<iframe height="315" class="span6" src="//www.youtube.com/embed/KYDC9qsIH0o" frameborder="0" allowfullscreen></iframe>
</div>