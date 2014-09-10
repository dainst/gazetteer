<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<s:message code="domain.place.recordGroup.noGroup" text="domain.place.recordGroup.noGroup" var="recordGroupNotSpecified"/>

<div>
	<form novalidate class="form-horizontal" name="createForm">
	
		<fieldset>				
			<!-- type -->
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
					
				<!-- record group -->
				<div class="control-group">
					<label class="control-label">
						<s:message code="domain.place.recordGroup" text="domain.place.recordGroup" />
						<i class="icon-info-sign" style="color: #5572a1;" gaz-tooltip="'ui.place.user-group-info'"></i>
					</label>
					<div class="controls">
						<select ng-model="place.recordGroupId" class="input-large">
							<option value="">${recordGroupNotSpecified}</option>
							<c:forEach var="recordGroup" items="${recordGroups}">
								<option value="${recordGroup.id}">${recordGroup.name}</option>
							</c:forEach>
						</select>
					</div>
				</div>
		</fieldset>
		
	    <div class="form-actions">
           	<button ng-click="save()" class="save btn btn-primary"><s:message code="ui.create" text="ui.create"/></button>
           	<a class="btn" href="javascript:history.back()"><s:message code="ui.cancel" text="ui.cancel"/></a>
		</div>
       	
	</form>
</div>