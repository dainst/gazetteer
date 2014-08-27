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
<title>iDAI.gazetteer</title>
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

	<div class="archaeo-fixed-menu">
		<div class="container archaeo-fixed-menu-header">
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
   							<a href="">
   								<s:message code="ui.userManagement" text="ui.userManagement"/>
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
			<div id="archaeo-fixed-menu-logo"></div>
			<h3 class="pull-left">
				<small>Deutsches Archäologisches Institut</small> <br>
				<a href="./" style="color:inherit">iDAI.gazetteer</a>
			</h3>
		</div>
		<div class="affix-menu-wrapper">
			<div id="affix-menu" style="z-index: 100000"
				class="navbar navbar-inverse container" data-spy="affix">
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
								<li><a href="app/#!/edit/"> <s:message
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

	<div class="container">
	
		<c:if test="${userDeleted != null}">
			<div class="alert alert-success">
				<s:message code="ui.deleteUser.success" text="ui.deleteUser.success" arguments="${userDeleted}" />
			</div>
		</c:if>
		
		<h3>
			<s:message code="ui.userManagement" text="ui.userManagement" />
		</h3>
	
		<table class="table table-condensed table-hover user-management-table">
			<thead>
				<tr>
					<c:choose>
						<c:when test="${lastSorting eq 'username'}">
							<th><a href="userManagement?sort=username&isDescending=<c:out value="${!isDescending}" />"><s:message code="user.username" text="user.username" /></a></th>
						</c:when>
						<c:otherwise>
							<th><a href="userManagement?sort=username&isDescending=false"><s:message code="user.username" text="user.username" /></a></th>
						</c:otherwise>
					</c:choose>
					
					<c:choose>
						<c:when test="${lastSorting eq 'firstname'}">
							<th><a href="userManagement?sort=firstname&isDescending=<c:out value="${!isDescending}" />"><s:message code="user.firstname" text="user.firstname" /></a></th>
						</c:when>
						<c:otherwise>
							<th><a href="userManagement?sort=firstname&isDescending=false"><s:message code="user.firstname" text="user.firstname" /></a></th>
						</c:otherwise>
					</c:choose>
					
					<c:choose>
						<c:when test="${lastSorting eq 'lastname'}">
							<th><a href="userManagement?sort=lastname&isDescending=<c:out value="${!isDescending}" />"><s:message code="user.lastname" text="user.lastname" /></a></th>
						</c:when>
						<c:otherwise>
							<th><a href="userManagement?sort=lastname&isDescending=false"><s:message code="user.lastname" text="user.lastname" /></a></th>
						</c:otherwise>
					</c:choose>
					
					<c:choose>
						<c:when test="${lastSorting eq 'institution'}">
							<th><a href="userManagement?sort=institution&isDescending=<c:out value="${!isDescending}" />"><s:message code="user.institution" text="user.institution" /></a></th>
						</c:when>
						<c:otherwise>
							<th><a href="userManagement?sort=institution&isDescending=false"><s:message code="user.institution" text="user.institution" /></a></th>
						</c:otherwise>
					</c:choose>
				
					<c:choose>
						<c:when test="${lastSorting eq 'email'}">
							<th><a href="userManagement?sort=email&isDescending=<c:out value="${!isDescending}" />"><s:message code="user.email" text="user.email" /></a></th>
						</c:when>
						<c:otherwise>
							<th><a href="userManagement?sort=email&isDescending=false"><s:message code="user.email" text="user.email" /></a></th>
						</c:otherwise>
					</c:choose>
					
					<c:choose>
						<c:when test="${lastSorting eq 'lastLogin'}">
							<th><a href="userManagement?sort=lastLogin&isDescending=<c:out value="${!isDescending}" />"><s:message code="user.lastLogin" text="user.lastLogin" /></a></th>
						</c:when>
						<c:otherwise>
							<th><a href="userManagement?sort=lastLogin&isDescending=false"><s:message code="user.lastLogin" text="user.lastLogin" /></a></th>
						</c:otherwise>
					</c:choose>
							
					<c:choose>
						<c:when test="${lastSorting eq 'registrationDate'}">
							<th><a href="userManagement?sort=registrationDate&isDescending=<c:out value="${!isDescending}" />"><s:message code="user.registrationDate" text="user.registrationDate" /></a></th>
						</c:when>
						<c:otherwise>
							<th><a href="userManagement?sort=registrationDate&isDescending=false"><s:message code="user.registrationDate" text="user.registrationDate" /></a></th>
						</c:otherwise>
					</c:choose>
							
					<c:choose>
						<c:when test="${lastSorting eq 'admin'}">
							<th><a href="userManagement?sort=admin&isDescending=<c:out value="${!isDescending}" />"><s:message code="user.roles.admin" text="user.roles.admin" /></a></th>
						</c:when>
						<c:otherwise>
							<th><a href="userManagement?sort=admin&isDescending=false"><s:message code="user.roles.admin" text="user.roles.admin" /></a></th>
						</c:otherwise>
					</c:choose>
					
					<c:choose>
						<c:when test="${lastSorting eq 'editor'}">
							<th><a href="userManagement?sort=editor&isDescending=<c:out value="${!isDescending}" />"><s:message code="user.roles.editor" text="user.roles.editor" /></a></th>
						</c:when>
						<c:otherwise>
							<th><a href="userManagement?sort=editor&isDescending=false"><s:message code="user.roles.editor" text="user.roles.editor" /></a></th>
						</c:otherwise>
					</c:choose>
					
					<c:choose>
						<c:when test="${lastSorting eq 'reisestipendium'}">
							<th><a href="userManagement?sort=reisestipendium&isDescending=<c:out value="${!isDescending}" />"><s:message code="user.roles.reisestipendium" text="user.roles.reisestipendium" /></a></th>
						</c:when>
						<c:otherwise>
							<th><a href="userManagement?sort=reisestipendium&isDescending=false"><s:message code="user.roles.reisestipendium" text="user.roles.reisestipendium" /></a></th>
						</c:otherwise>
					</c:choose>
					
					<c:choose>
						<c:when test="${lastSorting == null or lastSorting == ''}">
							<th><a href="userManagement?isDescending=<c:out value="${!isDescending}" />"><s:message code="user.status" text="user.status" /></a></th>
						</c:when>
						<c:otherwise>
							<th><a href="userManagement?isDescending=false"><s:message code="user.status" text="user.status" /></a></th>
						</c:otherwise>
					</c:choose>
					
					<th />
					<th />
				</tr>
			</thead>
			<tbody>
				<c:forEach var="user" items="${users}">
    				<c:choose>
						<c:when test="${user.enabled}">
    						<tr>
								<td>${user.username}</td>
								<td>${user.firstname}</td>
								<td>${user.lastname}</td>
								<td>${user.institution}</td>
								<td>${user.email}</td>
								<td>${user.lastLoginAsText}</td>
								<td>${user.registrationDateAsText}</td>										
								<c:choose>
									<c:when test="${user.hasRole('ROLE_ADMIN')}">
										<td><span class="icon-ok" style="color: #46a546"></span></td>
									</c:when>
									<c:otherwise>
										<td><span class="icon-remove" style="color: #9d261d"></span></td>
									</c:otherwise>
								</c:choose>
								<c:choose>
									<c:when test="${user.hasRole('ROLE_EDITOR')}">
										<td><span class="icon-ok" style="color: #46a546"></span></td>
									</c:when>
									<c:otherwise>
										<td><span class="icon-remove" style="color: #9d261d"></span></td>
									</c:otherwise>
								</c:choose>							
								<c:choose>
									<c:when test="${user.hasRole('ROLE_REISESTIPENDIUM')}">
										<td><span class="icon-ok" style="color: #46a546"></span></td>
									</c:when>
									<c:otherwise>
										<td><span class="icon-remove" style="color: #9d261d"></span></td>
									</c:otherwise>
								</c:choose>			
								<td><s:message code="user.status.activated"></s:message></td>
								<td><a href="editUser?username=${user.username}&r=userManagement" class="btn btn-block btn-primary">&nbsp;<s:message code="ui.edit" text="ui.edit" />&nbsp;</a></td>
								<td><span class="btn btn-block btn-danger disabled">&nbsp;<s:message code="ui.delete" text="ui.delete" />&nbsp;</span></td>
							</tr>
						</c:when>
						<c:otherwise>
							<tr class="warning">
								<td>${user.username}</td>
								<td>${user.firstname}</td>
								<td>${user.lastname}</td>
								<td>${user.institution}</td>
								<td>${user.email}</td>
								<td>${user.lastLoginAsText}</td>
								<td>${user.registrationDateAsText}</td>										
								<c:choose>
									<c:when test="${user.hasRole('ROLE_ADMIN')}">
										<td><span class="icon-ok" style="color: #46a546"></span></td>
									</c:when>
									<c:otherwise>
										<td><span class="icon-remove" style="color: #9d261d"></span></td>
									</c:otherwise>
								</c:choose>
								<c:choose>
									<c:when test="${user.hasRole('ROLE_EDITOR')}">
										<td><span class="icon-ok" style="color: #46a546"></span></td>
									</c:when>
									<c:otherwise>
										<td><span class="icon-remove" style="color: #9d261d"></span></td>
									</c:otherwise>
								</c:choose>							
								<c:choose>
									<c:when test="${user.hasRole('ROLE_REISESTIPENDIUM')}">
										<td><span class="icon-ok" style="color: #46a546"></span></td>
									</c:when>
									<c:otherwise>
										<td><span class="icon-remove" style="color: #9d261d"></span></td>
									</c:otherwise>
								</c:choose>
								<td><s:message code="user.status.notActivated"></s:message></td>
								<td><a href="editUser?username=${user.username}&r=userManagement" class="btn btn-block btn-warning">&nbsp;<s:message code="ui.activate" text="ui.activate" />&nbsp;</a></td>
								<td><a href="#deleteModal_${user.id}" class="btn btn-block btn-danger" data-toggle="modal">&nbsp;<s:message code="ui.delete" text="ui.delete" />&nbsp;</a></td>	
							</tr>
						</c:otherwise>
					</c:choose>
					
					<div class="modal hide fade" id="deleteModal_${user.id}">
						<div class="modal-header">
							<h3><s:message code="ui.deleteUser" text="ui.deleteUser"/>?</h3>
						</div>
						<div class="modal-body">
							<s:message code="ui.deleteUser.really" text="ui.deleteUser.really" arguments="${user.username}"/>
						</div>
						<div class="modal-footer">
							<a href="#" class="btn" data-dismiss="modal" aria-hidden="true"><s:message code="ui.cancel" text="ui.cancel"/></a>
							<a href="userManagement?page=${page}&sort=${lastSorting}&isDescending=${isDescending}&deleteUser=true&deleteUserId=${user.id}" class="btn btn-danger" aria-hidden="true"><s:message code="ui.delete" text="ui.delete"/></a>
						</div>
					</div>
    			</c:forEach>
			</tbody>
		</table>
				
		<ul class="nav nav-pills" style="margin-bottom: 0; margin-left: auto; margin-right: auto; width: 20em;">
			<c:choose>
				<c:when test="${page eq 0}">
					<li class="disabled">
						<a>&larr; <s:message code="ui.previous" /></a>
					</li>
				</c:when>
				<c:otherwise>
					<li style="cursor:pointer">
						<a href="userManagement?page=${page - 1}&sort=${lastSorting}&isDescending=${isDescending}">&larr; <s:message code="ui.previous" /></a>
					</li>
				</c:otherwise>
			</c:choose>
			
			<li class="divider-vertical"></li>
			<li class="disabled">
				<a><s:message code="ui.page" text="ui.page" /> ${page + 1} / ${pages}</a>
			</li>
			<li class="divider-vertical"></li>
			<c:choose>
				<c:when test="${page eq pages - 1}">
					<li class="disabled">
						<a><s:message code="ui.next" text="Vor"/> &rarr;</a>
					</li>	
				</c:when>
				<c:otherwise>
					<li style="cursor:pointer">
						<a href="userManagement?page=${page + 1}&sort=${lastSorting}&isDescending=${isDescending}"><s:message code="ui.next" text="Vor"/> &rarr;</a>
					</li>
				</c:otherwise>
			</c:choose>
			
		</ul>					

		<!-- Footer -->
		<gaz:footer/>
		
	</div>
	
</body>
</html>
