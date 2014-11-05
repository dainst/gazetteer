<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<s:message code="ui.language.notSpecified" text="ui.language.notSpecified" var="langNotSpecified" />
<s:message code="domain.place.recordGroup.noGroup" text="domain.place.recordGroup.noGroup" var="recordGroupNotSpecified"/>

<div gaz-place-nav active-tab="edit" place="place"></div>

<ul class="nav nav-tabs">
	<li class="active">
		<a href="#names" data-toggle="tab">
			<s:message code="domain.place.names" text="domain.place.names"/>
		</a>
	</li>
	<li>
		<a href="#general" data-toggle="tab">
			<s:message code="domain.place.general" text="domain.place.general"/>
		</a>
	</li>
	<li>
		<a href="#identification" data-toggle="tab">
			<s:message code="domain.place.identification" text="domain.place.identification"/>
		</a>
	</li>
	<li>
		<a href="#locations" data-toggle="tab">
			<s:message code="domain.place.locations" text="domain.place.locations"/>
		</a>
	</li>
	<li>
		<a href="#connections" data-toggle="tab">
			<s:message code="domain.place.connections" text="domain.place.connections"/>
		</a>
	</li>
	<sec:authorize access="hasRole('ROLE_REISESTIPENDIUM')">
		<li>
			<a href="#reisestipendium" data-toggle="tab">
				<s:message code="domain.place.reisestipendium" text="domain.place.reisestipendium"/>
			</a>
		</li>
	</sec:authorize>
</ul>

<form novalidate class="form-horizontal" name="editForm">
	
		<fieldset>

			<div class="tab-content">
			
				<div class="tab-pane active" id="names">
				
					<legend><s:message code="domain.place.names" text="domain.place.names"/></legend>
					
					<!-- preferred name -->
					<div class="control-group">
						<label class="control-label">
							<s:message code="domain.place.prefName" text="domain.place.prefName" />
						</label>
						<div class="controls">
							<div class="form-inline">
								<table>
									<tbody>
										<tr>
											<td>
												<input type="text" ng-model="place.prefName.title" required />
												<select ng-model="place.prefName.language" class="input-small">
													<option value="">${langNotSpecified}</option>
													<c:forEach var="language" items="${languages}">
														<option value="${language.key}">${language.value}</option>
													</c:forEach>
												</select>
											</td>
										</tr>
										<tr>
											<td>
												<label class="checkbox inline">
													<input type="checkbox" ng-model="place.prefName.ancient" />
													<span gaz-translate="'place.name.ancient'"></span>
												</label>
												<label class="checkbox inline">
													<input type="checkbox" ng-model="place.prefName.transliterated" />
													<span gaz-translate="'place.name.transliterated'"></span>
												</label>
											</td>
										</tr>
									</tbody>
								</table>
							</div>
						</div>
					</div>
					
					<!-- additional names -->
					<div class="control-group">
						<label class="control-label">
							<s:message code="domain.place.otherNames" text="domain.place.otherNames" />
						</label>
						<div class="controls">
							<div class="form-inline">
								<table>
									<tbody>
										<tr>
											<td>
												<input type="text" ng-model="name.title" />
											</td>
											<td>
												<select ng-model="name.language" class="input-small">
													<option value="">${langNotSpecified}</option>
													<c:forEach var="language" items="${languages}">
														<option value="${language.key}">${language.value}</option>
													</c:forEach>
												</select>
											</td>
											<td>
												<button class="btn btn-primary plus" ng-click="addName()" ng-disabled="!name.title">
													<i class="icon-plus icon-white"></i>
												</button>
											</td>
										</tr>
										<tr>
											<td>
												<label class="checkbox inline">
													<input type="checkbox" ng-model="name.ancient" />
													<span gaz-translate="'place.name.ancient'"></span>
												</label>
												<label class="checkbox inline">
													<input type="checkbox" ng-model="name.transliterated" />
													<span gaz-translate="'place.name.transliterated'"></span>
												</label>
											</td>
										</tr>
									</tbody>
								</table>
							</div>
							<div ng-repeat="placename in place.names">
								<a ng-click="place.names.splice($index,1)"><i class="icon-remove-sign"></i></a> {{placename.title}}
								<em ng-show="placename.ancient && !placename.transliterated">
									(<small gaz-translate="'place.name.ancient'"></small>)
								</em>
								<em ng-show="!placename.ancient && placename.transliterated">
									(<small gaz-translate="'place.name.transliterated'"></small>)
								</em>
								<em ng-show="placename.ancient && placename.transliterated">
									(<small gaz-translate="'place.name.ancient'"></small>/<small gaz-translate="'place.name.transliterated'"></small>)
								</em>
								<small ng-hide="!placename.language">
									<em gaz-translate="'languages.' + placename.language"></em>
								</small>
							</div>
						</div>
					</div>
				
				</div>
							
				<div class="tab-pane" id="general">
		
					<legend><s:message code="domain.place.general" text="domain.place.general"/></legend>
				
					<!-- types -->
					<div class="control-group">
						<label class="control-label">
							<s:message code="domain.place.type" text="domain.place.type" />
						</label>
						<div class="controls well">
							<table>
								<tbody>
									<c:forEach var="placeTypeGroup" items="${placeTypeGroups}" varStatus="groupStatus">
										<tr>
											<td colspan="2">
												<c:if test="${groupStatus.index != 0}">
													<br/>
												</c:if>								
												<b><span gaz-translate="'place.types.groups.' + '${placeTypeGroup}'"/></b>
											</td>
										</tr>
										<c:set var="placeTypeCounter" value="0"/>
										<c:forEach var="placeTypeGroupId" items="${placeTypeGroupIds}" varStatus="idStatus">
											<c:if test="${groupStatus.index == placeTypeGroupId}">
												<c:if test="${placeTypeCounter == 0}">
													<tr>
												</c:if>
												<c:if test="${placeTypeCounter != 0 && placeTypeCounter % 2 == 0}">
													</tr><tr>
												</c:if>
												<td>
													<c:set var="placeTypeCounter" value="${placeTypeCounter + 1}"/>
													<label class="checkbox inline">
														<input type="checkbox" ng-click="addPlaceType('${placeTypes[idStatus.index]}')" ng-checked="hasType('${placeTypes[idStatus.index]}')"/>
														<span gaz-translate="'place.types.' + '${placeTypes[idStatus.index]}'"/>
														<i class="icon-info-sign" style="color: #5572a1;" gaz-tooltip="'place.types.description.' + '${placeTypes[idStatus.index]}'"></i>
														&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
													</label>
												</td>										
											</c:if>
										</c:forEach>
										</tr>								
									</c:forEach>
								</tbody>
							</table>
						</div>
					</div>
					
					<!-- tags -->
					<div class="control-group">
						<label class="control-label">
							<s:message code="domain.place.tags" text="domain.place.tags" />
						</label>
						<div class="controls">
							<div gaz-tag-field tags="place.tags" fieldname="tags" number="0"></div>
						</div>
					</div>
					
					<!-- provenance -->
					<div class="control-group">
						<label class="control-label">
							<s:message code="domain.place.provenance" text="domain.place.provenance" />
						</label>
						<div class="controls">
							<div gaz-tag-field tags="place.provenance" fieldname="provenance" number="1"></div>	
						</div>
					</div>
					
					<!-- comments -->
					<div class="control-group">
						<label class="control-label">
							<s:message code="domain.place.comments" text="domain.place.comments" />
						</label>
						<div class="controls">
							<textarea ng-model="comment.text"></textarea>
							<select ng-model="comment.language" class="input-small">
								<option value="">${langNotSpecified}</option>
								<c:forEach var="language" items="${languages}">
									<option value="${language.key}">${language.value}</option>
								</c:forEach>
							</select>
							<div class="btn btn-primary plus" ng-click="addComment()" ng-disabled="!comment.text || !comment.language">
								<i class="icon-plus icon-white"></i>
							</div>
							<div ng-hide="!place.comments" style="margin-top: 1em">
								<blockquote ng-repeat="comment in place.comments">
									<a ng-click="place.comments.splice($index,1)"><i class="icon-remove-sign"></i></a>
									<p>{{comment.text}}</p>
									<small ng-hide="!comment.language" gaz-translate="'languages.' + comment.language"></small>
								</blockquote>
							</div>
						</div>
					</div>
					
				</div>
				
				<div class="tab-pane" id="identification">
				
					<legend><s:message code="domain.place.identification" text="domain.place.identification"/></legend>
					
					<!-- identifiers -->
					<div class="control-group">
						<label class="control-label">
							<s:message code="domain.identifier.value" text="domain.identifier.value" />
						</label>
						<div class="controls">
							<input type="text" ng-model="identifier.value" class="input-small">
							<s:message code="domain.identifier.context" text="domain.identifier.context" />
							<select ng-model="identifier.context" class="input-medium">
								<c:forEach var="idType" items="${idTypes}">
									<option value="${idType}">${idType}</option>
								</c:forEach>
							</select>
							<div class="btn btn-primary plus" ng-click="addIdentifier()" ng-disabled="!identifier.value || !identifier.context">
								<i class="icon-plus icon-white"></i>
							</div>
							<div type="text" ng.hide="!place.identifiers">
								<div ng-repeat="identifier in place.identifiers">
									<a ng-click="place.identifiers.splice($index,1)"><i class="icon-remove-sign"></i></a>
									<em>{{identifier.context}}:</em> {{identifier.value}}
								</div>
							</div>
						</div>
					</div>
					
					<!-- URI -->
					<div class="control-group">
						<label class="control-label">
							<s:message code="domain.place.links" text="domain.place.links"/>
						</label>
						<div class="controls">
							<input type="url" name="link-object" ng-model="link.object" required>
							<select ng-model="link.predicate" class="input-small">
								<option value="owl:sameAs">owl:sameAs</option>
								<option value="rdfs:seeAlso">rdfs:seeAlso</option>
							</select>
							<div class="btn btn-primary plus" ng-click="addLink()"
									ng-disabled="!link.object || !link.predicate">
								<i class="icon-plus icon-white"></i>
							</div>
							<div type="text" ng.hide="!place.linkss">
								<div ng-repeat="link in place.links">
									<a ng-click="place.links.splice($index,1)"><i class="icon-remove-sign"></i></a>
									<em>{{link.predicate}}:</em> <a href="{{link.object}}" target="_blank">{{link.object}}</a>
								</div>
							</div>
						</div>
					</div>
					
				</div>
				
				<div class="tab-pane" id="locations">
				
					<legend><s:message code="domain.place.prefLocation" text="domain.place.prefLocation"/></legend>
					
					<!-- preferred location -->
					<div class="control-group">
						<label class="control-label">
							<s:message code="domain.location.coordinates" text="domain.location.coordinates" />
						</label>
						<div class="controls">
							<div gaz-location-picker coordinates="prefLocationCoordinates"></div>
							<select ng-model="place.prefLocation.confidence" class="input-medium">
								<option value="0" gaz-translate="'location.confidence.0'">
								<option value="4" gaz-translate="'location.confidence.4'">
								<option value="1" gaz-translate="'location.confidence.1'">
								<option value="2" gaz-translate="'location.confidence.2'">
								<option value="3" gaz-translate="'location.confidence.3'">
							</select>
						</div>
					</div>
					<div class="control-group">
						<label class="control-label">
							<s:message code="domain.location.altitudeInMeters" text="domain.location.altitudeInMeters" />
						</label>
						<div class="controls">							
							<input type="text" ng-model="place.prefLocation.altitude" />
						</div>
					</div>
					<div class="control-group">
						<label class="control-label">
							<s:message code="domain.location.polygon" text="domain.location.polygon" />
						</label>
						<div class="controls">
							<div gaz-shape-editor shape="place.prefLocation.shape" pos="place.prefLocation.coordinates" editor-name="'prefLocationShapeEditor'"></div>
						</div>
					</div>
					<div class="control-group">
						<div class="controls">
							<label class="checkbox inline">
								<input type="checkbox" ng-model="place.prefLocation.publicSite"/>
								<span gaz-translate="'location.public'"></span>
							</label>
						</div>
					</div>
					
					<legend><s:message code="domain.place.otherLocations" text="domain.place.otherLocations"/></legend>
					
					<!-- additional locations -->
					<div class="control-group">
						<label class="control-label">
							<s:message code="domain.location.coordinates" text="domain.location.coordinates" />
						</label>
						<div class="controls">
							<div gaz-location-picker coordinates="location.coordinates"></div>
							<select ng-model="location.confidence" class="input-medium">
								<option value="0" gaz-translate="'location.confidence.0'">
								<option value="4" gaz-translate="'location.confidence.4'">
								<option value="1" gaz-translate="'location.confidence.1'">
								<option value="2" gaz-translate="'location.confidence.2'">
								<option value="3" gaz-translate="'location.confidence.3'">
							</select>
							<div class="btn btn-primary plus" ng-click="addLocation()" ng-disabled="!location.coordinates && !location.shape">
								<i class="icon-plus icon-white"></i>
							</div>
						</div>
					</div>
					<div class="control-group">
						<label class="control-label">
							<s:message code="domain.location.altitudeInMeters" text="domain.location.altitudeInMeters" />
						</label>
						<div class="controls">							
							<input type="text" ng-model="location.altitude" />
						</div>
					</div>
					<div class="control-group">
						<label class="control-label">
							<s:message code="domain.location.polygon" text="domain.location.polygon" />
						</label>
						<div class="controls">
							<div gaz-shape-editor shape="location.shape" pos="location.coordinates" editor-name="'additionalLocationsShapeEditor'"></div>
						</div>
					</div>
					<div class="control-group">
						<div class="controls">
							<label class="checkbox inline">
								<input type="checkbox" ng-model="location.publicSite" />
								<span gaz-translate="'location.public'"></span>
							</label>
						</div>
					</div>
					<div ng-repeat="location in place.locations">
						<br /><a ng-click="place.locations.splice($index,1)"><i class="icon-remove-sign"></i></a>
						<span>
							<span ng-show="location.coordinates"><em><s:message code="domain.location.latitude" text="domain.location.latitude" />:</em> {{location.coordinates[1]}},</span>
							<span ng-show="location.coordinates"><em><s:message code="domain.location.longitude" text="domain.location.longitude" />:</em> {{location.coordinates[0]}}</span><span ng-show="location.coordinates && location.altitude">,</span>
							<span ng-show="location.altitude"><em><s:message code="domain.location.altitude" text="domain.location.altitude" />:</em> {{location.altitude}}</span>
							<br ng-show="location.coordinates || location.altitude"/>
							<em ng-show="location.shape"><s:message code="domain.location.polygon" text="domain.location.polygon" /></em>
							(<em><s:message code="domain.location.confidence" text="domain.location.confidence" />:</em>
							<span gaz-translate="'location.confidence.'+location.confidence"></span>,
							<em gaz-translate="'location.public'"></em>:
							<span ng-show="location.publicSite"><s:message code="ui.yes" text="ui.yes" /></span><span ng-hide="location.publicSite"><s:message code="ui.no" text="ui.no" /></span>)
						</span>								
					</div>
				</div>
				
				<div class="tab-pane" id="connections">
				
					<legend><s:message code="domain.place.connections" text="domain.place.connections"/></legend>
					
					<!-- parent -->
					<div class="control-group">
						<label class="control-label">
							<s:message code="domain.place.parent" text="domain.place.parent" />
						</label>
						<div class="controls">
							<div gaz-place-picker place="parent" id="place.parent"></div>
						</div>
					</div>
					
					<!-- related places -->
					<div class="control-group">
						<label class="control-label">
							<s:message code="domain.place.relatedPlaces" text="domain.place.relatedPlaces" />
						</label>
						<div class="controls">
							<div gaz-place-picker place="relatedPlace" id="relatedPlace['@id']"></div>
							<div class="btn btn-primary plus" style="vertical-align:top" ng-click="addRelatedPlace()" ng-disabled="!relatedPlace['@id']">
								<i class="icon-plus icon-white"></i>
							</div>
							<div ng-repeat="relatedPlace in allRelatedPlaces">
								<a ng-click="allRelatedPlaces.splice($index,1)"><i class="icon-remove-sign"></i></a>
								<div gaz-place-title place="relatedPlace"></div>
							</div>
						</div>
					</div>
					
				</div>
				
				<sec:authorize access="hasRole('ROLE_REISESTIPENDIUM')">
					<div class="tab-pane" id="reisestipendium">
					
						<legend><s:message code="domain.place.reisestipendium" text="domain.place.reisestipendium"/></legend>
						
						<!-- notes -->
						<div class="control-group">
							<label class="control-label">
								<s:message code="domain.place.noteReisestipendium" text="domain.place.noteReisestipendium" />
							</label>
							<div class="controls">
								<textarea rows="6" class="span12" ng-model="place.noteReisestipendium"></textarea>
							</div>
						</div>
						
						<!-- comments -->
						<div class="control-group">
							<label class="control-label">
								<s:message code="domain.place.commentsReisestipendium" text="domain.place.commentsReisestipendium" />
							</label>
							<div class="controls">
								<textarea rows="6" class="span10" ng-model="commentReisestipendium.text"></textarea>
								<div class="btn btn-primary plus" ng-click="addCommentReisestipendium()" ng-disabled="!commentReisestipendium.text">
									<i class="icon-plus icon-white"></i>
								</div>
								<div ng-hide="!place.commentsReisestipendium" style="margin-top: 1em">
									<blockquote ng-repeat="comment in place.commentsReisestipendium">
										<a ng-click="place.commentsReisestipendium.splice($index,1)"><i class="icon-remove-sign"></i></a>
										{{comment.text}}
										<small ng-hide="!comment.user">{{comment.user}}</small>
									</blockquote>
								</div>
							</div>
						</div>
						
					</div>
				</sec:authorize>
				
			</div>
		
		</fieldset>
		
	    <div class="form-actions">
           	<button ng-click="save()" class="save btn btn-primary"><s:message code="ui.save" text="ui.save"/></button>
           	<a class="btn" href="javascript:history.back()"><s:message code="ui.cancel" text="ui.cancel"/></a>
           	<button href="#deleteModal" class="btn btn-danger" data-toggle="modal"><s:message code="ui.delete" text="ui.delete"/></button>
			<div class="modal hide fade" id="deleteModal">
				<span ng-hide="(children && children.length > 0) || (relatedPlaces && relatedPlaces.length > 0)">
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal"
							aria-hidden="true">&times;</button>
						<h3><s:message code="ui.delete" text="ui.delete"/>?</h3>
					</div>
					<div class="modal-body">
						<p><s:message code="ui.delete.really" text="ui.delete.really"/></p>
					</div>
					<div class="modal-footer">
						<a href="#" class="btn" data-dismiss="modal" aria-hidden="true"><s:message code="ui.cancel" text="ui.cancel"/></a>
						<a ng-click="remove()" data-dismiss="modal" class="btn btn-danger"><s:message code="ui.delete" text="ui.delete"/></a>
					</div>
				</span>
				<span ng-show="(children && children.length > 0) || (relatedPlaces && relatedPlaces.length > 0)">
					<div class="modal-header">
						<button type="button" class="close" data-dismiss="modal"
							aria-hidden="true">&times;</button>
						<h3><s:message code="ui.delete.notAllowed" text="ui.delete.notAllowed"/></h3>
					</div>
					<div class="modal-body">
						<p><s:message code="ui.delete.children" text="ui.delete.children"/></p>
						<span ng-hide="!children || children.length < 1">
							<dd>
								<em>{{totalChildren}} <s:message code="ui.children" text="ui.children"/>:</em>
								<a gaz-tooltip="'ui.place.children.search'" ng-href="#!/search?q=parent:{{place.gazId}}" data-dismiss="modal"><i class="icon-search"></i></a>
								<i class="icon-circle-arrow-left" ng-show="offsetChildren == 0"></i>
								<a ng-click="prevChildren()" ng-hide="offsetChildren == 0"><i class="icon-circle-arrow-left"/></i></a>
								<i class="icon-circle-arrow-right" ng-show="offsetChildren+10 >= totalChildren"></i>
								<a ng-click="nextChildren()" ng-hide="offsetChildren+10 >= totalChildren"><i class="icon-circle-arrow-right"/></i></a>
							</dd>
							<dd>
								<ul>
									<li ng-repeat="child in children">
										<div gaz-place-title place="child" data-dismiss="modal"></div>
									</li>
								</ul>
							</dd>
						</span>
						<span ng-hide="!relatedPlaces || relatedPlaces.length < 1">
							<dd>
								<em>{{totalRelatedPlaces}} <s:message code="ui.relatedPlaces" text="ui.relatedPlaces"/>:</em>
								<a gaz-tooltip="'ui.place.children.search'" ng-href="#!/search?q=relatedPlaces:{{place.gazId}}" data-dismiss="modal"><i class="icon-search"></i></a>
								<i class="icon-circle-arrow-left" ng-show="offsetRelatedPlaces == 0"></i>
								<a ng-click="prevRelatedPlaces()" ng-hide="offsetRelatedPlaces == 0"><i class="icon-circle-arrow-left"/></i></a>
								<i class="icon-circle-arrow-right" ng-show="offsetRelatedPlaces+10 >= totalRelatedPlaces"></i>
								<a ng-click="nextRelatedPlaces()" ng-hide="offsetRelatedPlaces+10 >= totalRelatedPlaces"><i class="icon-circle-arrow-right"/></i></a>
							</dd>
							<dd>
								<ul>
									<li ng-repeat="relatedPlace in relatedPlaces">
										<div gaz-place-title place="relatedPlace" data-dismiss="modal"></div>
									</li>
								</ul>
							</dd>
						</span>
					</div>
					<div class="modal-footer">
						<a href="#" class="btn" data-dismiss="modal" aria-hidden="true"><s:message code="ui.ok" text="ui.ok"/></a>
					</div>
				</span>
			</div>
	</div>
       	
	</form>
	
</div>