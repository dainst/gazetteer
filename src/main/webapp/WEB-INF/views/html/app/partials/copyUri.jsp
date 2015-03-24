<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<span>
	<s:message code="ui.copyToClipboard" var="copyMsg" />
	<div class="modal hide">
		<div class="modal-header">
			<button type="button" class="close" data-dismiss="modal">×</button>
			<h3>
				<s:message code="ui.copyUriToClipboardHeading" />
			</h3>
		</div>
		<div class="modal-body">
			<label>${copyMsg}</label>
			<input class="uri input-xxlarge" style="width:97%" type="text" value="{{uri}}" id="copyUriInput">
		</div>
	</div>
	<a href><i class="icon-share"></i></a>
</span>