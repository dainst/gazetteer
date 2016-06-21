<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<div>
	<div ng-if="!editMode">
		<sec:authorize access="hasRole('ROLE_ADMIN')">
			<button ng-click="edit()" class="pull-right btn btn-primary"><s:message code="ui.edit" text="ui.edit"/></button>
		</sec:authorize>
		<div class="markdown">
			<div ng-if="headlines && headlines.length >= 5">
				<h1><s:message code="ui.help.tableOfContents" text="ui.help.tableOfContents"/></h3></h1>
				<div ng-repeat="headline in headlines">
					<a ng-click="scrollTo(headline)" style="cursor: pointer; margin-left: {{2 + (20 * (headline.level - 1))}}px">{{headline.label}}</a>
				</div>
				<br>
			</div>			
			<span table-of-contents ng-model="shownHelpText" ng-bind-html="shownHelpText"></span>
		</div>
	</div>
	<div ng-if="editMode">
		<button ng-click="show()" class="pull-right btn btn-primary"><s:message code="ui.back" text="ui.back"/></button>
		<select ng-model="editorLanguage" ng-change="resetPreview()" class="input-medium">
			<option value="eng"><s:message code="ui.language.english" text="ui.language.english"/></button></option>
			<option value="deu"><s:message code="ui.language.german" text="ui.language.german"/></option>
			<option value="ara"><s:message code="ui.language.arabic" text="ui.language.arabic"/></option>
		</select>
		<select ng-model="editorLoginNeeded" ng-change="resetPreview()" class="input-medium" style="width: 350px;">
			<option value="false"><s:message code="ui.help.noLoginNeeded" text="ui.help.noLoginNeeded"/></button></option>
			<option value="true"><s:message code="ui.help.loginNeeded" text="ui.help.loginNeeded"/></option>
		</select>
		<div markdown-text-editor markdown-text="helpTexts[editorLanguage][editorLoginNeeded]" placeholder="Text"></div>
		<button ng-click="changeText(editorLanguage, editorLoginNeeded)" class="btn btn-primary"><s:message code="ui.save" text="ui.save"/></button>
		<button ng-click="showPreview(editorLanguage, editorLoginNeeded)" class="btn btn-default"><s:message code="ui.preview" text="ui.preview"/></button>
		<button ng-click="restoreText(editorLanguage, editorLoginNeeded)" class="btn btn-default"><s:message code="ui.restore" text="ui.restore"/></button>
		
		<div ng-show="previewText">
			<span class="markdown" ng-bind-html="previewText"></span>
		</div>
	</div>
</div>