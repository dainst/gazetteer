<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<gaz-place-nav active-tab="show" place="place"></gaz-place-nav>

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

<h3><s:message code="ui.information" text="ui.information"/></h3>

<dl class="dl-horizontal">

	<dt><s:message code="domain.place.names" /></dt>
	<dd>
		<em><s:message code="domain.place.prefName" text="domain.place.prefName"/>: </em>
		{{place.prefName.title}}
		<em ng-hide="!place.prefName.language">
			(<span gaz-translate="'languages.' + place.prefName.language"></span>)
		</em>
	</dd>
	<dd ng-repeat="placename in place.names | orderBy:['language','title']">
		{{placename.title}}
		<em ng-hide="!placename.language">
			(<span gaz-translate="'languages.' + placename.language"></span>)
		</em>
	</dd>
	<br/>
	
	<span ng-hide="!place.tags">
		<dt><s:message code="domain.place.tags" text="domain.place.tags" /></dt>
		<dd>
			<span ng-repeat="tag in place.tags">
				<span class="label label-info">{{tag}}</span>&nbsp; 
			</span>
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!parent">
		<dt><s:message code="domain.place.parent" text="domain.place.parent" /></dt>
		<dd>
			<gaz-place-title place="parent"></gaz-place-title>
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!place.children">
		<dt><s:message code="domain.place.children" text="domain.place.children" /></dt>
		<dd>
			<a href="#/search?q=parent:{{place.gazId}}">
				<s:message code="ui.numberOfPlaces" text="ui.numberOfPlaces" arguments="{{place.children.length}}" />
			</a>
		</dd>
		<br/>
	</span>					
	
	<span ng-hide="!relatedPlaces || relatedPlaces.length < 1">
		<dt><s:message code="domain.place.relatedPlaces" text="domain.place.relatedPlaces" /></dt>
			<dd ng-repeat="relatedPlace in relatedPlaces | orderBy:'prefName.title'">
				<gaz-place-title place="relatedPlace"></gaz-place-title>
			</dd>
		<br/>
	</span>
	
	<span ng-hide="!place.prefLocation">
		<dt><s:message code="domain.place.locations" text="domain.place.locations" /></dt>
		<dd>
			<em><s:message code="domain.location.latitude" text="domain.location.latitude" />:</em> {{place.prefLocation.coordinates[1]}},
			<em><s:message code="domain.location.longitude" text="domain.location.longitude" />:</em> {{place.prefLocation.coordinates[0]}}
			(<em><s:message code="domain.location.confidence" text="domain.location.confidence" />:</em>
			<span gaz-translate="'location.confidence.'+place.prefLocation.confidence"></span>)
		</dd>
		<dd ng-repeat="location in place.locations">
			<em><s:message code="domain.location.latitude" text="domain.location.latitude" />:</em> {{location.coordinates[1]}},
			<em><s:message code="domain.location.longitude" text="domain.location.longitude" />:</em> {{location.coordinates[0]}}
			(<em><s:message code="domain.location.confidence" text="domain.location.confidence" />:</em>
			<span gaz-translate="'location.confidence.'+location.confidence"></span>)
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!place.type">
		<dt><s:message code="domain.place.type" text="domain.place.type" /></dt>
		<dd><span gaz-translate="'place.types.' + place.type"></span></dd>
		<br/>
	</span>
	
	<span ng-hide="!place.thesaurus">
		<dt><s:message code="domain.thesaurus" text="domain.thesaurus" /></dt>
		<dd>{{place.thesaurus}}</dd>
		<br/>
	</span>
	
	<span ng-hide="!place.identifiers">
		<dt><s:message code="domain.place.identifiers" text="domain.place.identifiers" /></dt>
		<dd ng-repeat="identifier in place.identifiers | orderBy:['context','value']">
			<em>{{identifier.context}}:</em> {{identifier.value}}
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!place.links">
		<dt><s:message code="domain.place.links" text="domain.place.links" /></dt>
		<dd ng-repeat="link in place.links | orderBy:['predicate','object']">
			<em>{{link.predicate}}:</em> <a href="{{link.object}}" target="_blank">{{link.object}}</a>
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!place.comments">
		<dt><s:message code="domain.place.comments" text="domain.place.comments" /></dt>
		<dd ng-repeat="comment in place.comments">
			<blockquote>{{comment.text}}</blockquote>
		</dd>
		<br/>
	</span>

</dl>