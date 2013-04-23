<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<s:message code="ui.language.notSpecified" text="ui.language.notSpecified" var="langNotSpecified" />

<div gaz-place-nav active-tab="edit" place="place"></div>

<ul class="nav nav-tabs">
	<li class="active">
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
		<a href="#names" data-toggle="tab">
			<s:message code="domain.place.names" text="domain.place.names"/>
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
</ul>

<form novalidate class="form-horizontal" name="editForm">
	
		<fieldset>

			<div class="tab-content">
			
				<div class="tab-pane active" id="general">
		
					<legend><s:message code="domain.place.general" text="domain.place.general"/></legend>
				
					<!-- type -->
					<div class="control-group">
						<label class="control-label">
							<s:message code="domain.place.type" text="domain.place.type" />
						</label>
						<div class="controls">
							<input type="text" ng-model="place.type" />
						</div>
					</div>
					
					<!-- tags -->
					<div class="control-group">
						<label class="control-label">
							<s:message code="domain.place.tags" text="domain.place.tags" />
						</label>
						<div class="controls">
							<input type="text" ng-model="place.tags" ng-list />
							<div>
								<span ng-repeat="tag in place.tags">
									<span class="label label-info">{{tag}}</span>&nbsp; 
								</span>
							</div>
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
								<div ng-repeat="identifier in place.identifiers | orderBy:['context','value']">
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
								<div ng-repeat="link in place.links | orderBy:['predicate','object']">
									<a ng-click="place.links.splice($index,1)"><i class="icon-remove-sign"></i></a>
									<em>{{link.predicate}}:</em> <a href="{{link.object}}" target="_blank">{{link.object}}</a>
								</div>
							</div>
						</div>
					</div>
					
				</div>
			
				<div class="tab-pane" id="names">
				
					<legend><s:message code="domain.place.names" text="domain.place.names"/></legend>
					
					<!-- preferred name -->
					<div class="control-group">
						<label class="control-label">
							<s:message code="domain.place.prefName" text="domain.place.prefName" />
						</label>
						<div class="controls">
							<div class="form-inline">
								<input type="text" ng-model="place.prefName.title" required />
								<select ng-model="place.prefName.language" class="input-small">
									<option value="">${langNotSpecified}</option>
									<c:forEach var="language" items="${languages}">
										<option value="${language.key}">${language.value}</option>
									</c:forEach>
								</select>
								<label class="checkbox inline">
									<input type="checkbox" ng-model="place.prefName.ancient">
									<span gaz-translate="'place.name.ancient'"></span>
								</label>
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
								<input type="text" ng-model="name.title" />
								<select ng-model="name.language" class="input-small">
									<option value="">${langNotSpecified}</option>
									<c:forEach var="language" items="${languages}">
										<option value="${language.key}">${language.value}</option>
									</c:forEach>
								</select>
								<label class="checkbox">
									<input type="checkbox" ng-model="name.ancient">
									<span gaz-translate="'place.name.ancient'"></span>
								</label>
								<button class="btn btn-primary plus" ng-click="addName()" ng-disabled="!name.title">
									<i class="icon-plus icon-white"></i>
								</button>
							</div>
							<div ng-repeat="placename in place.names | orderBy:['language','title']">
								<a ng-click="place.names.splice($index,1)"><i class="icon-remove-sign"></i></a> {{placename.title}}
								<em ng-show="placename.ancient">
									(<small gaz-translate="'place.name.ancient'"></small>)
								</em>
								<small ng-hide="!placename.language">
									<em gaz-translate="'languages.' + placename.language"></em>
								</small>
							</div>
						</div>
					</div>
				
				</div>
				
				<div class="tab-pane" id="locations">
				
					<legend><s:message code="domain.place.locations" text="domain.place.locations"/></legend>
					
					<!-- preferred location -->
					<div class="control-group">
						<label class="control-label">
							<s:message code="domain.place.prefLocation" text="domain.place.prefLocation" />
						</label>
						<div class="controls">
							<div gaz-location-picker coordinates="place.prefLocation.coordinates"></div>
							<select path="prefLocation.confidence" class="input-small">
								<option value="0" gaz-translate="'location.confidence.0'">
								<option value="1" gaz-translate="'location.confidence.1'">
								<option value="2" gaz-translate="'location.confidence.2'">
								<option value="3" gaz-translate="'location.confidence.3'">
							</select>
						</div>
					</div>
					
					<!-- additional locations -->
					<div class="control-group">
						<label class="control-label">
							<s:message code="domain.place.otherLocations" text="domain.place.otherLocations" />
						</label>
						<div class="controls">
							<div gaz-location-picker coordinates="location.coordinates"></div>
							<select ng-model="location.confidence" class="input-small">
								<option value="0" gaz-translate="'location.confidence.0'">
								<option value="1" gaz-translate="'location.confidence.1'">
								<option value="2" gaz-translate="'location.confidence.2'">
								<option value="3" gaz-translate="'location.confidence.3'">
							</select>
							<div class="btn btn-primary plus" ng-click="addLocation()" ng-disabled="!location.coordinates">
								<i class="icon-plus icon-white"></i>
							</div>
							<div ng-repeat="location in place.locations">
								<a ng-click="place.locations.splice($index,1)"><i class="icon-remove-sign"></i></a>
								<em><s:message code="domain.location.latitude" text="domain.location.latitude" />:</em> {{location.coordinates[1]}},
								<em><s:message code="domain.location.longitude" text="domain.location.longitude" />:</em> {{location.coordinates[0]}}
								(<em><s:message code="domain.location.confidence" text="domain.location.confidence" />:</em>
								<span gaz-translate="'location.confidence.'+location.confidence"></span>)
							</div>
						</div>
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
							<div ng-repeat="relatedPlace in relatedPlaces | orderBy:'prefName.title'">
								<a ng-click="relatedPlaces.splice($index,1)"><i class="icon-remove-sign"></i></a>
								<div gaz-place-title place="relatedPlace"></div>
							</div>
						</div>
					</div>
					
				</div>
	
			</div>
		
		</fieldset>
		
	    <div class="form-actions">
           	<button ng-click="save()" class="save btn btn-primary"><s:message code="ui.save" text="ui.save"/></button>
           	<a class="btn" href="javascript:history.back()"><s:message code="ui.cancel" text="ui.cancel"/></a>
           	<button href="#deleteModal" class="btn btn-danger" data-toggle="modal"><s:message code="ui.delete" text="ui.delete"/></button>
			<div class="modal hide fade" id="deleteModal">
				<div class="modal-header">
					<button type="button" class="close" data-dismiss="modal"
						aria-hidden="true">&times;</button>
					<h3><s:message code="ui.delete" text="ui.delete"/>?</h3>
				</div>
				<div class="modal-body">
					<p><s:message code="ui.delete.really" text="ui.delete.really"/></p>
				</div>
				<div class="modal-footer">
					<a href="#" class="btn" data-dismiss="modal" aria-hidden="true">Close</a>
					<a ng-click="remove()" data-dismiss="modal" class="btn btn-danger"><s:message code="ui.delete" text="ui.delete"/></a>
				</div>
			</div>
	</div>
       	
	</form>
	
</div>