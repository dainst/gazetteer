<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<div class="tag-field" ng-style="{ width: fieldwidth }">
	<span ng-repeat="tag in tags"><span class="label label-info">{{tag}} <i class="icon-remove-sign" style="cursor: pointer;" ng-click="removeTag(tag)" ng-hide="mouseOver" ng-mouseenter="mouseOver = true" ng-mouseleave="mouseOver = false"></i><i class="icon-remove-sign remove-icon-mouseover" style="cursor: pointer;" ng-click="removeTag(tag)" ng-show="mouseOver" ng-mouseenter="mouseOver = true" ng-mouseleave="mouseOver = false"></i></span>&nbsp;</span><input type="text" name="tagTextField" class="input" ng-model="inputText" on-enter="chooseSuggestion()" on-backspace="backspace()" on-arrow-up="selectPreviousSuggestion()" on-arrow-down="selectNextSuggestion()" on-blur="lostFocus()"/>
	<div class="suggestion-menu" style="left: {{textFieldPos + 5}}px; width: auto;" ng-show="suggestions.length > 0">
		<div ng-repeat="suggestion in suggestions">
			<div class="suggestion" ng-mousedown="chooseSuggestion()" ng-hide="selectedSuggestionIndex == $index" ng-mouseover="setSelectedSuggestionIndex($index)"><span ng-show="suggestion.length < 31">{{suggestion}}</span><span ng-hide="suggestion.length < 31">{{suggestion.substring(0,30)}}...</span></div>
			<div class="suggestion selected" ng-mousedown="chooseSuggestion()" ng-show="selectedSuggestionIndex == $index"><span ng-show="suggestion.length < 31">{{suggestion}}</span><span ng-hide="suggestion.length < 31">{{suggestion.substring(0,30)}}...</span></div>
		</div>
	</div>	
</div>