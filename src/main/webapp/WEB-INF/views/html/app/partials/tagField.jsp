<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<span>

	<style type="text/css">
		.tag-field {
			background-colour: #fff;
			border-radius: 0 0 0 0;
			border: 1px solid #CCC;
			height: 20px;
			padding	: 0px 4px 6px 4px;
			font-size: 14px;
			line-height: 20px;
			width: 475px;
			display: inline-block;
			overflow: hidden;
			white-space: nowrap;
		}
		.input {
    		border-width: 0px !important;
    		border: none !important;
    		white-space: nowrap;
    		width: 100%;
		}
		.remove-icon-mouseover {
			color: #d9e8ef;
		}
		.suggestion {
			display: block;
			border: 1px solid #CCC;
			width: auto;
			height: 20px;
			margin-top: -6px;
			margin-bottom: 5px;
			padding: 4px;
			background-color: #ffffff;
			z-index: 2000;
			cursor: pointer;
		}
		.selected {
			background-color: #6786ad;
			color: #ffffff;
		}
	</style>
	
	<span>
	
		<div class="tag-field">
			<span ng-repeat="tag in tags"><span class="label label-info">{{tag}} <i class="icon-remove-sign" style="cursor: pointer;" ng-click="removeTag(tag)" ng-hide="mouseOver" ng-mouseenter="mouseOver = true" ng-mouseleave="mouseOver = false"></i><i class="icon-remove-sign remove-icon-mouseover" style="cursor: pointer;" ng-click="removeTag(tag)" ng-show="mouseOver" ng-mouseenter="mouseOver = true" ng-mouseleave="mouseOver = false"></i></span>&nbsp;</span><input type="text" name="tagTextField" class="input" ng-model="inputText" on-enter="chooseSuggestion()" on-backspace="backspace()" on-arrow-up="selectPreviousSuggestion()" on-arrow-down="selectNextSuggestion()"/>
		</div>
		<div style="position: absolute; left: {{textFieldPos + 5}}px; z-index: 2000">
			<div ng-repeat="suggestion in suggestions">
				<div class="suggestion" ng-click="chooseSuggestion()" ng-hide="selectedSuggestionIndex == $index" ng-mouseover="setSelectedSuggestionIndex($index)">{{suggestion}}</div>
				<div class="suggestion selected" ng-click="chooseSuggestion()" ng-show="selectedSuggestionIndex == $index">{{suggestion}}</div>
			</div>
		</div>
			
	</span>

</span>