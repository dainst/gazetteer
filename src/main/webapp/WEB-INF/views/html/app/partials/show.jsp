<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<div gaz-place-nav active-tab="show" place="place"></div>

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
		<em ng-show="place.prefName.ancient && !place.prefName.transliterated">
			(<small gaz-translate="'place.name.ancient'"></small>)
		</em>
		<em ng-show="!place.prefName.ancient && place.prefName.transliterated">
			(<small gaz-translate="'place.name.transliterated'"></small>)
		</em>
		<em ng-show="place.prefName.ancient && place.prefName.transliterated">
			(<small gaz-translate="'place.name.ancient'"></small>/<small gaz-translate="'place.name.transliterated'"></small>)
		</em>
		<small ng-show="place.prefName.language">
			<em gaz-translate="'languages.' + place.prefName.language"></em>
		</small>
	</dd>
	<dd ng-repeat="placename in place.names | orderBy:['language','title']">
		{{placename.title}}
		<em ng-show="placename.ancient && !placename.transliterated">
			(<small gaz-translate="'place.name.ancient'"></small>)
		</em>
		<em ng-show="!placename.ancient && placename.transliterated">
			(<small gaz-translate="'place.name.transliterated'"></small>)
		</em>
		<em ng-show="placename.ancient && placename.transliterated">
			(<small gaz-translate="'place.name.ancient'"></small><small>/</small><small gaz-translate="'place.name.transliterated'"></small>)
		</em>
		<small ng-hide="!placename.language">
			<em gaz-translate="'languages.' + placename.language"></em>
		</small>
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
			<div gaz-place-title place="parent"></div>
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!children || children.length < 1">
		<dt><s:message code="domain.place.children" text="domain.place.children" /></dt>
		<dd>
			<em><s:message code="ui.numberOfPlaces" text="ui.numberOfPlaces" arguments="{{totalChildren}}" />:</em>
			<a gaz-tooltip="'ui.place.children.search'" ng-href="#!/search?q=parent:{{place.gazId}}"><i class="icon-search"></i></a>
			<i class="icon-circle-arrow-left" ng-show="offsetChildren == 0"></i>
			<a ng-click="prevChildren()" ng-hide="offsetChildren == 0"><i class="icon-circle-arrow-left"/></i></a>
			<i class="icon-circle-arrow-right" ng-show="offsetChildren+10 >= totalChildren"></i>
			<a ng-click="nextChildren()" ng-hide="offsetChildren+10 >= totalChildren"><i class="icon-circle-arrow-right"/></i></a>
		</dd>
		<dd>
			<ul>
				<li ng-repeat="child in children">
					<div gaz-place-title place="child"></div>
				</li>
			</ul>
		</dd>
		<br/>
	</span>					
	
	<span ng-hide="!relatedPlaces || relatedPlaces.length < 1">
		<dt><s:message code="domain.place.relatedPlaces" text="domain.place.relatedPlaces" /></dt>
		<dd>
			<ul>
				<li ng-repeat="relatedPlace in relatedPlaces | orderBy:'prefName.title'">
					<div gaz-place-title place="relatedPlace"></div>
				</li>
			</ul>
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!place.prefLocation">
		<dt><s:message code="domain.place.locations" text="domain.place.locations" /></dt>
		<dd>
			<span ng-show="place.prefLocation.coordinates">
			<em><s:message code="domain.location.latitude" text="domain.location.latitude" />: </em>{{place.prefLocation.coordinates[0]}},
			<em><s:message code="domain.location.longitude" text="domain.location.longitude" />: </em>{{place.prefLocation.coordinates[1]}}
			<span ng-show="place.type == 'archaeological-site' && !place.prefLocation.publicSite">
				<sec:authorize access="hasRole('ROLE_USER')">
					(<em><s:message code="domain.location.confidence" text="domain.location.confidence" />:</em>
					<span gaz-translate="'location.confidence.'+place.prefLocation.confidence"></span>)
				</sec:authorize>
				<sec:authorize access="!hasRole('ROLE_USER')">
					(<span><s:message code="domain.location.rounded" text="domain.location.rounded" /> <i class="icon-info-sign" style="color: #5572a1;" gaz-tooltip="'ui.place.archaeological-site-info'"></i></span>)
				</sec:authorize>
			</span>
			<span ng-hide="place.type == 'archaeological-site' && !place.prefLocation.publicSite">
				(<em><s:message code="domain.location.confidence" text="domain.location.confidence" />: </em>
				<span gaz-translate="'location.confidence.'+place.prefLocation.confidence"></span>)
			</span>
			<br />
			</span>
			<em ng-show="place.prefLocation.shape"><s:message code="domain.location.polygon" text="domain.location.polygon" /></em>
		</dd>
		<dd ng-repeat="location in place.locations">
			<em><s:message code="domain.location.latitude" text="domain.location.latitude" />: </em>{{location.coordinates[0]}},
			<em><s:message code="domain.location.longitude" text="domain.location.longitude" />: </em>{{location.coordinates[1]}}
			<span ng-show="place.type == 'archaeological-site' && !location.publicSite">
				<sec:authorize access="hasRole('ROLE_USER')">
					(<em><s:message code="domain.location.confidence" text="domain.location.confidence" />:</em>
					<span gaz-translate="'location.confidence.'+location.confidence"></span>)
				</sec:authorize>
				<sec:authorize access="!hasRole('ROLE_USER')">
					(<span><s:message code="domain.location.rounded" text="domain.location.rounded" /> <i class="icon-info-sign" style="color: #5572a1;" gaz-tooltip="'ui.place.archaeological-site-info'"></i></span>)
				</sec:authorize>
			</span>
			<span ng-hide="place.type == 'archaeological-site' && !location.publicSite">
				(<em><s:message code="domain.location.confidence" text="domain.location.confidence" />:</em>
				<span gaz-translate="'location.confidence.'+location.confidence"></span>)
			</span>
			<br />
			<em ng-show="location.shape"><s:message code="domain.location.polygon" text="domain.location.polygon" /></em>
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!place.type">
		<dt><s:message code="domain.place.type" text="domain.place.type" /></dt>
		<dd><span gaz-translate="'place.types.' + place.type"></span></dd>
		<br/>
	</span>
	
	<span>
		<dt><s:message code="ui.contexts" text="ui.contexts"/></dt>
		<dd>
			<a ng-href="http://arachne.uni-koeln.de/arachne/index.php?view[layout]=search_result_overview&view[category]=overview&search[constraints]=Gazetteerid:%22{{place.gazId}}%22" target="_blank">
				<s:message code="ui.link.arachne" text="ui.link.arachne"/>
				<i class="icon-external-link"></i>
			</a>
		</dd>
		<dd ng-show="getIdsByContext('zenon-thesaurus') != false">
			<a ng-href="http://zenon.dainst.org/#search?q=f999_1:({{getIdsByContext('zenon-thesaurus').join(' OR ')}})" target="_blank">
				<s:message code="ui.link.zenon" text="ui.link.zenon"/>
				<i class="icon-external-link"></i>
			</a>
		</dd>
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
			<em>{{link.predicate}}:</em> <a ng-href="{{link.object}}" target="_blank">{{link.object}}</a>
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
	
	<span ng-hide="!place.noteReisestipendium">
		<dt><s:message code="domain.place.noteReisestipendium" text="domain.place.noteReisestipendium" /></dt>
		<dd>
			<blockquote ng-bind-html-unsafe="place.noteReisestipendium | parseUrlFilter | parseLineBreakFilter"></blockquote>
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!place.commentsReisestipendium">
		<dt><s:message code="domain.place.commentsReisestipendium" text="domain.place.commentsReisestipendium" /></dt>
		<dd ng-repeat="comment in place.commentsReisestipendium">
			<blockquote>
				{{comment.text}}
				<small ng-show="comment.user && comment.user != null">{{comment.user}}</small>
			</blockquote>
		</dd>
		<br/>
	</span>
	
	<span ng-repeat="identifier in place.identifiers | filter:{context:'pleiades'} | limitTo:1" >
		<dt></dt>
		<dd>
			<em><s:message code="ui.attribution.pleiades" text="ui.attribution.pleiades"/></em>
		</dd>
	</span>
	
	<span ng-repeat="identifier in place.identifiers | filter:{context:'geonames'} | limitTo:1" >
		<dt></dt>
		<dd>
			<em><s:message code="ui.attribution.geonames" text="ui.attribution.geonames"/></em>
		</dd>
	</span>

</dl>
