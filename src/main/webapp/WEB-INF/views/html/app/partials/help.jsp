<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<div>
	<div ng-if="!editMode">
		<sec:authorize access="hasRole('ROLE_ADMIN')">
			<button ng-click="edit()" class="btn btn-primary"><s:message code="ui.edit" text="ui.edit"/></button>
		</sec:authorize>
		<span class="markdown" ng-bind-html="shownHelpText"></span>
	</div>
	<div ng-if="editMode">
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