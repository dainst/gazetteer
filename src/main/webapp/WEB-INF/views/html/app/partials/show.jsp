<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<div gaz-place-nav active-tab="show" place="place"></div>

<s:message code="ui.copyToClipboard" var="copyMsg"/>
			
<div class="modal hide" id="copyUriModal">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal">×</button>
		<h3><s:message code="ui.copyUriToClipboardHeading"/></h3>
	</div>
	<div class="modal-body">
		<label>${copyMsg}</label>
		<input class="input-xxlarge" style="width:97%" type="text" value="${baseUri}place/{{place.gazId}}" id="copyUriInput"></input>
	</div>
</div>
<div class="modal hide" id="copyCoordinatesModal">
	<div class="modal-header">
		<button type="button" class="close" data-dismiss="modal">×</button>
		<h3><s:message code="ui.copyCoordinatesToClipboardHeading"/></h3>
	</div>
	<div class="modal-body">
		<label>${copyMsg}</label>
		<input class="input-xxlarge" style="width:97%" type="text" value="{{copyCoordinates[1]}},{{copyCoordinates[0]}}" id="copyCoordinatesInput"></input>
	</div>
</div>
<script type="text/javascript">
	$("#copyUriModal").on("shown",function() {
		$("#copyUriInput").focus().select();
	});
	$("#copyCoordinatesModal").on("shown",function() {
		$("#copyCoordinatesInput").focus().select();
	});
</script>

<h3 ng-if="place.gazId && !place.accessDenied"><s:message code="ui.information" text="ui.information"/></h3>

<dl class="dl-horizontal">

	<span ng-show="place.prefName || (place.names && place.names.length > 0)">
		<dt><s:message code="domain.place.names" /></dt>
		<span ng-show="place.prefName">
			<dd>
				<em><s:message code="domain.place.prefName" text="domain.place.prefName"/>: </em>
				{{place.prefName.title}}
				<span ng-show="place.prefName.ancient">
					(<small gaz-translate="'place.name.ancient'"></small>)
				</span>
				<small ng-show="place.prefName.language">
					<c:forEach var="language" items="${languages}">
						<em ng-show="'${language.key}' == place.prefName.language">${language.value}</em>
					</c:forEach>
				</small>
				<em ng-show="place.prefName.transliterated">
					(<small gaz-translate="'place.name.transliterated'"></small>)
				</em>
			</dd>
		</span>
		<dd ng-repeat="placename in place.names | orderBy:['sort'] | limitTo: namesDisplayed">
			{{placename.title}}
			<span ng-show="placename.ancient">
				(<small gaz-translate="'place.name.ancient'"></small>)
			</span>
			<small ng-hide="!placename.language">
				<c:forEach var="language" items="${languages}">
					<em ng-show="'${language.key}' == placename.language">${language.value}</em>
				</c:forEach>
			</small>
			<em ng-show="placename.transliterated">
				(<small gaz-translate="'place.name.transliterated'"></small>)
			</em>
		</dd>
		<dd ng-show="place.names.length > 4">
			<a href="" ng-click="changeNumberOfDisplayedNames()">
				<small ng-show="namesDisplayed == 4">(<span gaz-translate="'ui.place.names.more'"></span>)</small>
				<small ng-hide="namesDisplayed == 4">(<span gaz-translate="'ui.place.names.less'"></span>)</small>
			</a>
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!place.tags">
		<dt><s:message code="domain.place.tags" text="domain.place.tags" /></dt>
		<dd>
			<span ng-repeat="tag in place.tags">
				<a class="label label-info" href="#!/search?q=%7B%22bool%22%3A%7B%22must%22%3A%5B%7B%22match%22%3A%7B%22tags%22%3A%22{{tag}}%22%7D%7D%5D%7D%7D&type=extended">{{tag}}</a>&nbsp; 
			</span>
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!place.provenance">
		<dt><s:message code="domain.place.provenance" text="domain.place.provenance" /></dt>
		<dd>
			<span ng-repeat="provenanceEntry in place.provenance">
				<a class="label label-info"  href="#!/search?q=%7B%22bool%22%3A%7B%22must%22%3A%5B%7B%22match%22%3A%7B%22provenance%22%3A%22{{provenanceEntry}}%22%7D%7D%5D%7D%7D&type=extended">{{provenanceEntry}}</a>&nbsp; 
			</span>
			<i class="icon-info-sign" style="color: #5572a1; cursor: pointer;" gaz-tooltip="'ui.place.provenance-info'"></i>
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!place.parents || place.parents.length == 0" ng-cloak>
		<dt><s:message code="domain.place.parent" text="domain.place.parent" /></dt>
		<dd ng-repeat="parent in place.parents | reverse">
			<div style="margin-left: {{$index * 16}}px;"><i ng-show="$index != 0" class="icon-circle-arrow-right" style="cursor: default;"></i>
				<div gaz-place-title place="parent" ng-hide="parent.prefLocation && parent.prefLocation.shape"></div>
				<div gaz-place-title place="parent" ng-show="parent.prefLocation && parent.prefLocation.shape" ng-mouseover="showPolygon(parent)" ng-mouseout="showMainPolygon()"></div>
			</div>
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!children || children.length < 1">
		<dt><s:message code="domain.place.children" text="domain.place.children" /></dt>
		<dd style="cursor: default;">
			<em><s:message code="ui.numberOfPlaces" text="ui.numberOfPlaces" arguments="{{totalChildren}}" />:</em>
			<a gaz-tooltip="'ui.place.children.search'" ng-href="#!/search?q=parent:{{place.gazId}}"><i class="icon-search"></i></a>
			<i class="icon-circle-arrow-left muted" ng-show="offsetChildren == 0"></i>
			<i ng-click="prevChildren()" class="icon-circle-arrow-left" style="color: #5572a1; cursor: pointer;" ng-hide="offsetChildren == 0"></i>
			<i class="icon-circle-arrow-right muted" ng-show="offsetChildren+10 >= totalChildren"></i>
			<i ng-click="nextChildren()" class="icon-circle-arrow-right" style="color: #5572a1; cursor: pointer;" ng-hide="offsetChildren+10 >= totalChildren"/></i>
		</dd>
		<dd>
			<div class="place-list-container">
				<li ng-repeat="child in children">
					<div ng-if="child.prefLocation && child.prefLocation.coordinates && child.prefLocation.coordinates.length > 0"
							ng-mouseover="showChildMarker(child)" ng-mouseout="hideChildMarker()">
						<span class="icon-map-marker" ng-show="child.prefLocation && child.prefLocation.coordinates && child.prefLocation.coordinates.length > 0"
							style="margin-left: 3px; margin-right: 5px; cursor: default; color: #00E64D; text-shadow: 1px 1px 1px #000000;"></span>
						<div gaz-place-title place="child"></div>
					</div>
					<div ng-if="!child.prefLocation || !child.prefLocation.coordinates || child.prefLocation.coordinates.length == 0" gaz-place-title place="child"></div>
				</li>
			</div>
		</dd>
		<br/>
	</span>					
	
	<span ng-hide="!relatedPlaces || relatedPlaces.length < 1">
		<dt><s:message code="domain.place.relatedPlaces" text="domain.place.relatedPlaces" /></dt>
		<dd>
			<em><s:message code="ui.numberOfPlaces" text="ui.numberOfPlaces" arguments="{{totalRelatedPlaces}}" />:</em>
			<a gaz-tooltip="'ui.place.children.search'" ng-href="#!/search?q=relatedPlaces:{{place.gazId}}"><i class="icon-search"></i></a>
			<i class="icon-circle-arrow-left" ng-show="offsetRelatedPlaces == 0"></i>
			<a ng-click="prevRelatedPlaces()" ng-hide="offsetRelatedPlaces == 0"><i class="icon-circle-arrow-left"/></i></a>
			<i class="icon-circle-arrow-right" ng-show="offsetRelatedPlaces+10 >= totalRelatedPlaces"></i>
			<a ng-click="nextRelatedPlaces()" ng-hide="offsetRelatedPlaces+10 >= totalRelatedPlaces"><i class="icon-circle-arrow-right"/></i></a>
		</dd>
		<dd>
			<ul>
				<li ng-repeat="relatedPlace in relatedPlaces">
					<div gaz-place-title place="relatedPlace"></div>
				</li>
			</ul>
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!place.unlocatable && (!place.prefLocation || !((place.prefLocation.coordinates && place.prefLocation.coordinates.length > 0) || place.prefLocation.shape))">
		<dt><s:message code="domain.place.locations" text="domain.place.locations" /></dt>
		<dd>
			<span ng-show="place.prefLocation.coordinates && place.prefLocation.coordinates.length > 0" ng-show="location.coordinates" ng-mouseover="setHighlight(place.gazId, 'prefLocation', 0)" ng-mouseout="setHighlight(null, null, null)">
				<span class="icon-map-marker" style="margin-right: 5px; cursor: default; color: #FD7567; text-shadow: 1px 1px 1px #000000;"></span>
				<span style="text-decoration:none; border-bottom: 1px dotted black; cursor: pointer;">
					<em><s:message code="domain.location.latitude" text="domain.location.latitude" />: </em>{{place.prefLocation.coordinates[1]}},
					<em><s:message code="domain.location.longitude" text="domain.location.longitude" />: </em>{{place.prefLocation.coordinates[0]}}</span>
					<a data-toggle="modal" href="#copyCoordinatesModal" ng-click="setCopyCoordinates(place.prefLocation.coordinates)"><i class="icon-share" style="font-size:0.7em"></i></a>
					<span ng-show="place.prefLocation.coordinates && place.prefLocation.coordinates.length > 0 && place.prefLocation.altitude">,</span>
					<span ng-show="place.prefLocation.altitude"><em><s:message code="domain.location.altitude" text="domain.location.altitude" />: </em>{{place.prefLocation.altitude}} m</span>
					<span ng-show="place.prefLocation.coordinates && place.prefLocation.coordinates.length > 0 && !place.prefLocation.publicSite">
						<sec:authorize access="hasRole('ROLE_USER')">
							<span ng-show="place.limitedReadAccess">
								(<span><s:message code="domain.location.rounded" text="domain.location.rounded" /> <i class="icon-info-sign" style="color: #5572a1;" gaz-tooltip="'ui.place.protected-site-group-info'"></i></span>)
							</span>
							<span ng-hide="place.limitedReadAccess">
								(<em><s:message code="domain.location.confidence" text="domain.location.confidence" />:</em>
								<span gaz-translate="'location.confidence.'+place.prefLocation.confidence"></span>)
							</span>
						</sec:authorize>
						<sec:authorize access="!hasRole('ROLE_USER')">
							(<span><s:message code="domain.location.rounded" text="domain.location.rounded" /> <i class="icon-info-sign" style="color: #5572a1;" gaz-tooltip="'ui.place.protected-site-login-info'"></i></span>)
						</sec:authorize>
					</span>
					<span ng-hide="!place.prefLocation.publicSite">
						(<em><s:message code="domain.location.confidence" text="domain.location.confidence" />: </em>
						<span gaz-translate="'location.confidence.'+place.prefLocation.confidence"></span>)
					</span>
					<br ng-show="(place.prefLocation.coordinates && place.prefLocation.coordinates.length > 0) || place.prefLocation.altitude"/>
				</span>
			</span>
			<span ng-show="place.unlocatable" gaz-translate="'place.unlocatable'"></span>
			<em ng-show="place.prefLocation.shape" ng-mouseover="setHighlight(place.gazId, 'polygon', -1)" ng-mouseout="setHighlight(null, null, null)" style="text-decoration:none; border-bottom: 1px dotted black; cursor: pointer;"><s:message code="domain.location.polygonSpecified" text="domain.location.polygonSpecified" /></em>
		</dd>
		<span ng-hide="!place.locations">
			<br />
			<dt><s:message code="domain.place.otherLocations" text="domain.place.otherLocations" /></dt>
			<dd>
					<div ng-repeat="location in place.locations">
						<span style="cursor: default;"><span class="icon-map-marker" style="color: #FFA9A1; text-shadow: 1px 1px 1px #000000; margin-right: 3px;"></span>{{$index + 1}}</span>
						<span ng-show="location.coordinates" ng-mouseover="setHighlight(place.gazId, 'alternativeLocation', $index)" ng-mouseout="setHighlight(null, null, null)" style="text-decoration:none; border-bottom: 1px dotted black; cursor: pointer; margin-left: 3px;">
							<em><s:message code="domain.location.latitude" text="domain.location.latitude" />: </em>{{location.coordinates[1]}},&nbsp;
							<em><s:message code="domain.location.longitude" text="domain.location.longitude" />: </em>{{location.coordinates[0]}}</span>
							<a data-toggle="modal" href="#copyCoordinatesModal" ng-click="setCopyCoordinates(location.coordinates)"><i class="icon-share" style="font-size:0.7em"></i></a><span ng-show="location.coordinates && location.altitude">,&nbsp;</span>
						<span ng-show="location.altitude"><em><s:message code="domain.location.altitude" text="domain.location.altitude" />: </em>{{location.altitude}} m</span><span ng-show="(location.coordinates || location.altitude) && location.shape">,</span>
						<em ng-show="location.shape" ng-mouseover="setHighlight(place.gazId, 'polygon', $index)" ng-mouseout="setHighlight(null, null, null)" style="text-decoration:none; border-bottom: 1px dotted black; cursor: pointer;"><s:message code="domain.location.polygonSpecified" text="domain.location.polygonSpecified" /></em>
						<span ng-show="!location.publicSite">
							<sec:authorize access="hasRole('ROLE_USER')">
								(<em><s:message code="domain.location.confidence" text="domain.location.confidence" />:</em>
								<span gaz-translate="'location.confidence.'+location.confidence"></span>)
							</sec:authorize>
							<sec:authorize access="!hasRole('ROLE_USER')">
								(<span><s:message code="domain.location.rounded" text="domain.location.rounded" /> <i class="icon-info-sign" style="color: #5572a1;" gaz-tooltip="'ui.place.protected-site-info'"></i></span>)
							</sec:authorize>
						</span>
						<span ng-hide="!location.publicSite">
							(<em><s:message code="domain.location.confidence" text="domain.location.confidence" />:</em>
							<span gaz-translate="'location.confidence.'+location.confidence"></span>)
						</span>
					</div>
			</dd>
		</span>
		<br />
	</span>
	
	<span ng-hide="!place.types || place.types.length == 0">
		<dt><s:message code="domain.place.type" text="domain.place.type" /></dt>
		<c:forEach var="placeType" items="${placeTypes}">
			<dd ng-show="hasType('${placeType}')">
				<span gaz-translate="'place.types.' + '${placeType}'"></span>
				<br/>
			</dd>
		</c:forEach>
		<br/>
	</span>
	
	<span ng-hide="!place.recordGroup || place.recordGroup.id.length == 0">
		<dt><s:message code="domain.place.recordGroup" text="domain.place.recordGroup" /></dt>
		<dd>
			<a data-toggle="modal" href="#recordGroupModal">{{place.recordGroup.name}}</a>
		</dd>
		<br/>
	</span>
	
	<div class="modal large fade hide" id="recordGroupModal">
		<div class="modal-header">
			<button type="button" class="close" data-dismiss="modal">×</button>
			<h3><s:message code="ui.recordGroupModal.header" text="ui.recordGroupModal.header" /></h3>
		</div>
		<div class="modal-body large">
			<h4>
				<s:message code="ui.recordGroupModal.info.header" text="ui.recordGroupModal.info.header" />
			</h4>
			<div><s:message code="ui.recordGroupModal.info.main" text="ui.recordGroupModal.info" arguments="{{place.recordGroup.name}}" /></div>
			<div>
				<sec:authorize access="!hasRole('ROLE_USER')">
					<s:message code="ui.recordGroupModal.info.notLoggedIn" text="ui.recordGroupModal.info.notLoggedIn" />
				</sec:authorize>
				<sec:authorize access="hasRole('ROLE_USER')">
					<span ng-show="place.accessDenied">
						<s:message code="ui.recordGroupModal.info.noAccess" text="ui.recordGroupModal.info.noAccess" />
					</span>
					<span ng-show="place.limitedReadAccess">
						<s:message code="ui.recordGroupModal.info.limitedReadAccess" text="ui.recordGroupModal.info.limitedReadAccess" />
					</span>
					<span ng-show="!place.limitedReadAccess && place.editAccessDenied">
						<s:message code="ui.recordGroupModal.info.readAccess" text="ui.recordGroupModal.info.readAccess" />
					</span>
					<span ng-show="!place.accessDenied && !place.editAccessDenied">
						<s:message code="ui.recordGroupModal.info.editAccess" text="ui.recordGroupModal.info.editAccess" />
					</span>
				</sec:authorize>				
			</div>
			
			<div ng-hide="place.accessDenied">
				<br>
				<h4>
					<s:message code="ui.recordGroupModal.search.header" text="ui.recordGroupModal.search.header" />
				</h4>
				<a ng-click="recordGroupSearch()" data-dismiss="modal" style="cursor: pointer;">
					<i class="icon-search"></i> <s:message code="ui.recordGroupModal.search.link" text="ui.recordGroupModal.search.link" />
				</a>
			</div>
			<br>			
			<div>
				<h4>
					<s:message code="ui.recordGroupModal.contact.header" text="ui.recordGroupModal.contact.header" />
				</h4>
				<sec:authorize access="!hasRole('ROLE_USER')">
					<s:message code="ui.recordGroupModal.contact.loggedOutInfo" text="ui.recordGroupModal.contact.loggedOutInfo" />
					<textarea name="message" class="contact-form-textarea" disabled></textarea>
					<a class="btn btn-primary" disabled>
						<s:message code="ui.recordGroupModal.contact.submit" text="ui.recordGroupModal.contact.submit"/>
					</a>
				</sec:authorize>
				<sec:authorize access="hasRole('ROLE_USER')">
					<s:message code="ui.recordGroupModal.contact.info" text="ui.recordGroupModal.contact.info" />
					<textarea name="message" ng-model="contactMessage" class="contact-form-textarea"></textarea>
					<a class="btn btn-primary" data-dismiss="modal" ng-click="sendRecordGroupContactMail()">
						<s:message code="ui.recordGroupModal.contact.submit" text="ui.recordGroupModal.contact.submit"/>
					</a>
				</sec:authorize>
			</div>
		</div>
	</div>
	
	<span ng-show="place.gazId && !place.accessDenied">
		<dt><s:message code="ui.contexts" text="ui.contexts"/></dt>
		<dd>
			<a ng-href="http://arachne.uni-koeln.de/arachne/index.php?view[layout]=search_result_overview&view[category]=overview&search[constraints]=Gazetteerid:%22{{place.gazId}}%22" target="_blank">
				<s:message code="ui.link.arachne" text="ui.link.arachne"/>
				<i class="icon-external-link"></i>
			</a>
		</dd>
		<dd ng-show="getIdsByContext('zenon-thesaurus') != false">
			<a ng-href="http://zenon.dainst.org/Search/Results?lookfor={{getZenonSearchQuery()}}&type=Thesaurus" target="_blank">
				<s:message code="ui.link.zenon" text="ui.link.zenon"/>
				<i class="icon-external-link"></i>
			</a>
		</dd>
		<dd ng-show="getIdsByContext('pleiades') != false">
			<a ng-href="http://pelagios.org/api/places/http%3A%2F%2Fpleiades.stoa.org%2Fplaces%2F{{getIdsByContext('pleiades')[0]}}" target="_blank">
				<s:message code="ui.link.pelagios" text="ui.link.pelagios"/>
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
			<div ng-hide="link.description">
				<em>{{link.predicate}}:</em>
				<a ng-href="{{link.object}}" target="_blank">{{decodeUri(link.object)}}</a>
			</div>
			<div ng-show="link.description">
				<a ng-href="{{link.object}}" target="_blank">{{link.description}}</a>
				<i class="icon-info-sign" style="color: #5572a1; cursor: pointer;" gaz-text-tooltip="link.predicate + ': ' + decodeUri(link.object)"></i>
			</div>
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!place.groupInternalData">
		<dt><s:message code="domain.place.groupInternalData" text="domain.place.groupInternalData" /></dt>
		<dd ng-repeat="data in place.groupInternalData">
			<div style="white-space: pre;">{{data.text}}</div>
			<small><em>{{data.recordGroup.name}}</em></small>
			<br/><br/>
		</dd>
	</span>
	
	<span ng-hide="!place.comments">
		<dt><s:message code="domain.place.comments" text="domain.place.comments" /></dt>
		<dd ng-repeat="comment in place.comments">
			<blockquote style="white-space: pre;">{{comment.text}}</blockquote>
		</dd>
		<br/>
	</span>
	
	<span ng-hide="!place.noteReisestipendium">
		<dt><s:message code="domain.place.noteReisestipendium" text="domain.place.noteReisestipendium" /></dt>
		<dd>
			<blockquote ng-bind-html="place.noteReisestipendium | parseUrlFilter | parseLineBreakFilter | toTrusted"></blockquote>
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
	
</dl>
