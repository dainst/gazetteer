<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<div>
	<div ng-if="!editMode">
		<sec:authorize access="hasRole('ROLE_ADMIN')">
			<button ng-click="edit()" class="btn btn-primary"><s:message code="ui.edit" text="ui.edit"/></button>
		</sec:authorize>
		<span ng-bind-html="shownHelpText"></span>
	</div>
	<div ng-if="editMode">
		<div markdown-text-editor markdown-text="helpTexts['deu']['false']" placeholder="Text"></div>
		<button ng-click="changeText('deu', false)" class="btn btn-primary"><s:message code="ui.save" text="ui.save"/></button>
	</div>
</div>