<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="gaz" tagdir="/WEB-INF/tags/layout" %>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<!doctype html>
<html>
	<head>
		<title><s:message code="ui.recordGroupManagement" text="ui.recordGroupManagement" /> | iDAI.gazetteer</title>
		<meta charset="utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<meta name="google-site-verification" content="axehIuQKDs9bKUYzUl7hj1IvFMePho1--MppShoNQWk" />
		<link rel="icon" href="resources/ico/favicon.ico">
		<link rel="apple-touch-icon" sizes="144x144" href="resources/ico/apple-touch-icon-144.png">
		<link rel="apple-touch-icon" sizes="114x114" href="resources/ico/apple-touch-icon-114.png">
		<link rel="apple-touch-icon" sizes="72x72" href="resources/ico/apple-touch-icon-72.png">
		<link rel="apple-touch-icon" href="resources/ico/apple-touch-icon-57.png">
		<link href="//arachne.uni-koeln.de/archaeostrap/assets/css/bootstrap.css" rel="stylesheet">
		<link href="//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.1/css/bootstrap-responsive.min.css" rel="stylesheet">
		<link href="//netdna.bootstrapcdn.com/font-awesome/3.0.2/css/font-awesome.css" rel="stylesheet">
		<link href="//netdna.bootstrapcdn.com/font-awesome/3.0.2/css/font-awesome-ie7.css" rel="stylesheet">
		<link href="resources/css/app.css" rel="stylesheet">
		<script	src="//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>	
		<script	src="//arachne.uni-koeln.de/archaeostrap/assets/js/bootstrap.js"></script>	
		<script src='//maps.google.com/maps/api/js?key=${googleMapsApiKey}&amp;sensor=false&libraries=visualization'></script>
		<script src="resources/js/custom.js"></script>
	</head>
	<body>
		<div class="container">
			<div class="archaeo-fixed-menu">
				<div class="gaz-container archaeo-fixed-menu-header">
					<div class="btn-group pull-right" style="margin-top:12px">
						<button type="button" class="btn btn-primary btn-small dropdown-toggle" data-toggle="dropdown">
		   					<sec:authentication property="principal.username" /> <span class="caret"></span>
						</button>	
						<ul class="dropdown-menu pull-right" role="menu">
			   				<li>
		   						<a href="editUser?username=${pageContext['request'].userPrincipal.name}&r=userManagement">
		   							<s:message code="ui.userSettings" text="ui.userSettings"/>
		   						</a>
		   					</li>
		   					<sec:authorize access="hasRole('ROLE_EDITOR')">
		   						<li>
		   							<a href="globalChangeHistory">
		   								<s:message code="ui.globalChangeHistory" text="ui.globalChangeHistory"/>
		   							</a>
		   						</li>
		   					</sec:authorize>
		   					<sec:authorize access="hasRole('ROLE_ADMIN')">
		   						<li>
		   							<a href="userManagement">
		   								<s:message code="ui.userManagement" text="ui.userManagement"/>
		   							</a>
		   						</li>
		   					</sec:authorize>
		   					<sec:authorize access="hasRole('ROLE_USER')">
		   						<li>
		   							<a href="recordGroupManagement">
		   								<s:message code="ui.recordGroupManagement" text="ui.recordGroupManagement"/>
		   							</a>
		   						</li>
		   					</sec:authorize>
		   					<li class="divider"></li>
		  					<li>
		   						<a href="logout">
		   							<s:message code="ui.logout" text="ui.logout"/>
		   						</a>
		   					</li>   					
						</ul>
					</div>
					<div id="gaz-logo"></div>
					<div>
						<h3 class="pull-left">
							<small>Deutsches Archäologisches Institut</small> <br>
							<a href="app/#!/home" style="color:inherit">iDAI.gazetteer</a>
						</h3>
					</div>
					<div id="archaeo-fixed-menu-logo"></div>
				</div>
				<div class="affix-menu-wrapper">
					<div id="affix-menu" style="z-index: 100000"
						class="navbar navbar-inverse gaz-container" data-spy="affix">
						<div class="navbar-inner">
							<div id="archaeo-fixed-menu-icon"></div>
							<a class="btn btn-navbar" data-toggle="collapse"
								data-target=".nav-collapse"> <span class="icon-bar"></span> <span
								class="icon-bar"></span> <span class="icon-bar"></span>
							</a>
							<a class="brand" href="">iDAI.gazetteer</a>
							<div class="nav-collapse pull-left ">
								<ul class="nav">
									<li><a href="app/#!/thesaurus"><s:message
												code="ui.thesaurus.list" text="ui.thesaurus.list" /></a></li>
									<li><a href="app/#!/extended-search"> <s:message
												code="ui.search.extendedSearch" text="ui.search.extendedSearch" />
									</a></li>
									<sec:authorize access="hasRole('ROLE_EDITOR')">
										<li><a href="app/#!/create/"> <s:message
													code="ui.place.create" text="ui.place.create" />
										</a></li>
									</sec:authorize>							
									<sec:authorize access="hasRole('ROLE_REISESTIPENDIUM')">
										<li><a href="app/#!/search?q=%7B%22bool%22:%7B%22must%22:%5B%7B%22query_string%22:%7B%22query%22:%22_exists_:noteReisestipendium%22%7D%7D%5D%7D%7D&type=extended"> <s:message
													code="ui.search.reisestipendium" text="ui.search.reisestipendium" />
										</a></li>
									</sec:authorize>
								</ul>
							</div>
							<!--/.nav-collapse -->
						</div>
					</div>
				</div>
			</div>
			
			<div class="gaz-container">
				<c:if test="${failure eq 'notFound'}">
					<div class="alert alert-error">
						<s:message code="ui.recordGroupUserManagement.error.userNotFound" text="ui.recordGroupUserManagement.error.userNotFound" />
					</div>
				</c:if>
			</div>
			
			<div class="gaz-container">
				<c:if test="${failure eq 'alreadyInGroup'}">
					<div class="alert alert-error">
						<s:message code="ui.recordGroupUserManagement.error.userAlreadyInGroup" text="ui.recordGroupUserManagement.error.userAlreadyInGroup" />
					</div>
				</c:if>
			</div>
			
			<div class="gaz-container">
				<c:if test="${failure eq 'noInput'}">
					<div class="alert alert-error">
						<s:message code="ui.recordGroupUserManagement.error.noInput" text="ui.recordGroupUserManagement.error.noInput" />
					</div>
				</c:if>
			</div>
			
			<div class="gaz-container">
				<h3>
					${recordGroup.name}
				</h3>
				
				<form class="form-horizontal" name="form" action="checkAddUserToGroupForm?groupId=${recordGroup.id}" accept-charset="UTF-8" method="POST">
					<s:message code="ui.recordGroupUserManagement.emailOrUsername" text="ui.recordGroupUserManagement.emailOrUsername" var="defaultMessage" />
					<input type="text" name="email_or_username" value="${emailOrUsername}" placeholder="${defaultMessage}" />
					<s:message code="ui.recordGroupUserManagement.addUser" text="ui.recordGroupUserManagement.addUser" var="submitValue" />
					<input type="submit" class="btn btn-primary" value="${submitValue}" />	
				</form>
			
				<table class="table table-condensed table-hover user-management-table">
					<thead>
						<tr>
							<th><s:message code="user.username" text="user.username"/></th>
							<th><s:message code="user.firstname" text="user.firstname"/></th>
							<th><s:message code="user.lastname" text="user.lastname"/></th>
							<th><s:message code="user.institution" text="user.institution"/></th>							
							<th><s:message code="user.email" text="user.email"/></th>
							<th><s:message code="ui.recordGroupUserManagement.access" text="ui.recordGroupUserManagement.access"/></th>
							<th></th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="user" items="${users}">							
		    				<tr>
								<td>${user.username}</td>
								<td>${user.firstname}</td>
								<td>${user.lastname}</td>
								<td>${user.institution}</td>
								<td>${user.email}
								<td>
									<form name="form" action="checkRecordGroupUserForm?groupId=${recordGroup.id}&userId=${user.id}" accept-charset="UTF-8" method="POST">	
										<select name="access" onchange="this.form.submit()" style="margin-bottom: -16px; margin-left: -80px; margin-right: -150px;">
											<c:forEach var="role" items="admin,edit,read">
												<c:choose>
													<c:when test="${roles[user.id].roleType == role}">
														<option value="${role}" selected><s:message code="ui.recordGroupUserManagement.access.${role}" text="ui.recordGroupUserManagement.access.${role}"/></option>
													</c:when>
													<c:otherwise>
														<option value="${role}"><s:message code="ui.recordGroupUserManagement.access.${role}" text="ui.recordGroupUserManagement.access.${role}"/></option>
													</c:otherwise>
												</c:choose>
											</c:forEach>
										</select>
									</form>
								</td>
								<td><a href="#removeUserModal_${user.id}" class="btn btn-block btn-danger" data-toggle="modal">&nbsp;<s:message code="ui.recordGroupUserManagement.removeUser" text="ui.recordGroupUserManagement.removeUser" />&nbsp;</a></td>
							</tr>
							<div class="modal hide fade" id="removeUserModal_${user.id}">
								<div class="modal-header">
									<h3><s:message code="ui.recordGroupUserManagement.removeUser" text="ui.recordGroupUserManagement.removeUser"/>?</h3>
								</div>
								<div class="modal-body">
									<s:message code="ui.recordGroupUserManagement.removeUser.really" text="ui.recordGroupUserManagement.removeUser.really" arguments="${user.username},${recordGroup.name}"/>
								</div>
								<div class="modal-footer">
									<a href="#" class="btn" data-dismiss="modal" aria-hidden="true"><s:message code="ui.cancel" text="ui.cancel"/></a>
									<a href="removeUserFromGroup?groupId=${recordGroup.id}&userId=${user.id}" class="btn btn-danger" aria-hidden="true"><s:message code="ui.remove" text="ui.remove"/></a>
								</div>
							</div>
						</c:forEach>
					</tbody>
				</table>
						
				<!-- Footer -->
				<gaz:footer/>
				
			</div>
		</div>
	</body>
</html>