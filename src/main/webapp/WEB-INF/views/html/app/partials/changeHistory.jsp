<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<div gaz-place-nav active-tab="change-history" place="place"></div>

<s:message code="ui.copyToClipboard" var="copyMsg"/>
			
<div class="modal hide" id="copyUriModal">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal">×</button>
		<h3><s:message code="ui.copyToClipboardHeading"/></h3>
	</div>
	<div class="modal-body">
		<label>${copyMsg}</label>
		<input class="input-xxlarge" style="width:97%" type="text" value="${baseUri}place/{{place.gazId}}" id="copyUriInput"></input>
	</div>
</div>
<script type="text/javascript">
	$("#copyUriModal").on("shown",function() {
		$("#copyUriInput").focus().select();
	});
</script>

<h3><s:message code="ui.changeHistory" text="ui.changeHistory"/></h3>

<table class="table table-condensed table-hover">
	<thead>
		<tr>
			<th><s:message code="ui.changeHistory.changeDate" text="ui.changeHistory.changeDate"/></th>
			<th><s:message code="ui.username" text="ui.username"/></th>
		</tr>
	</thead>
	<tbody ng-repeat="placeChangeRecord in place.changeHistory">
		<tr>
			<td>{{placeChangeRecord.changeDate}}</td>
			<td>{{placeChangeRecord.username}}</td>			
		</tr>		
	</tbody>
</table>