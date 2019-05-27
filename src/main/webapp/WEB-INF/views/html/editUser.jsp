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
		<title><s:message code="ui.editUser" text="ui.editUser" /> | iDAI.gazetteer</title>
		<meta charset="utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<meta name="google-site-verification" content="axehIuQKDs9bKUYzUl7hj1IvFMePho1--MppShoNQWk" />
		<link rel="icon" href="resources/ico/favicon.ico">
		<link rel="apple-touch-icon" sizes="144x144" href="resources/ico/apple-touch-icon-144.png">
		<link rel="apple-touch-icon" sizes="114x114" href="resources/ico/apple-touch-icon-114.png">
		<link rel="apple-touch-icon" sizes="72x72" href="resources/ico/apple-touch-icon-72.png">
		<link rel="apple-touch-icon" href="resources/ico/apple-touch-icon-57.png">
		<link href="//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.1/css/bootstrap-responsive.min.css" rel="stylesheet">
		<link href="../resources/archaeostrap/css/bootstrap.css" rel="stylesheet">
		<link href="../resources/font-awesome/css/font-awesome.min.css" rel="stylesheet">
		<link href="resources/css/app.css" rel="stylesheet">
		<script	src="//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>	
		<script	src="../resources/archaeostrap/js/bootstrap.js"></script>
		<script src="resources/js/custom.js"></script>
		
		<!-- Piwik -->
		<script type="text/javascript">
			var _paq = _paq || [];
			_paq.push(["setDomains", ["*.gazetteer.dainst.org"]]);
			_paq.push(['trackPageView']);
			_paq.push(['enableLinkTracking']);
			(function() {
				var u="//piwik.dainst.org/";
		    	_paq.push(['setTrackerUrl', u+'piwik.php']);
		    	_paq.push(['setSiteId', 8]);
		    	var d=document, g=d.createElement('script'), s=d.getElementsByTagName('script')[0];
		    	g.type='text/javascript'; g.async=true; g.defer=true; g.src=u+'piwik.js'; s.parentNode.insertBefore(g,s);
			})();
		</script>
		<noscript><p><img src="//piwik.dainst.org/piwik.php?idsite=8" style="border:0;" alt="" /></p></noscript>
		<!-- End Piwik Code -->
	</head>
	<body>
		<div class="container-fluid">
			<div class="archaeo-fixed-menu">
				<div class="gaz-container archaeo-fixed-menu-header">
					<div class="btn-group pull-right" style="margin-top:12px">
							<button type="button" class="btn btn-primary btn-small dropdown-toggle" data-toggle="dropdown">
		   						<sec:authentication property="principal.username" /> <span class="caret"></span>
							</button>	
							<ul class="dropdown-menu pull-right" role="menu">
			   					<li>
		   							<a href="editUser?username=${pageContext['request'].userPrincipal.name}&r=${r}">
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
							<div class="nav-collapse pull-left">
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
							<ul class="nav pull-right">
								<li><a href="app/#!/about/"><s:message code="ui.about" text="ui.about" /></a></li>
								<li><a href="app/#!/help/"><s:message code="ui.help" text="ui.help" /><i class="icon-question-sign" style="color: white; margin: 0px 0px 5px 3px;"></i></a></li>
  								<li class="dropdown">
	    							<a href="" class="dropdown-toggle" data-toggle="dropdown">
	    								<s:message code="ui.language.current" text="ui.language.current" />
	    								<b class="caret"></b>
	    							</a>
	    							<ul class="dropdown-menu">
	      								<li><a href="?username=${user.username}&lang=en">English</a></li>
	      								<li><a href="?username=${user.username}&lang=de">Deutsch</a></li>
	      								<li><a href="?username=${user.username}&lang=ar">العربية</a></li>
	    							</ul>
	  							</li>
	  							<li class="dropdown">
	    							<a href="" class="dropdown-toggle" data-toggle="dropdown">iDAI.welt <b class="caret"></b></a>
	    							<div class="dropdown-menu">
	    								<iframe src="https://idai.world/config/idai-nav.html" frameborder="0" style="height: 100vh; width: 200px"></iframe>
	      							</div>
	  							</li>
							</ul>
							<!--/.nav-collapse -->
						</div>
					</div>
				</div>
			</div>
			
			<% String usernameControlGroup;
			String firstnameControlGroup;
			String lastnameControlGroup;
			String emailControlGroup;
			String passwordControlGroup;
			
			usernameControlGroup = "control-group";
			firstnameControlGroup = "control-group";
			lastnameControlGroup = "control-group";
			emailControlGroup = "control-group";
			passwordControlGroup = "control-group"; %>
		
			<div class="gaz-container">
				<c:if test="${failure eq 'missingUsername'}">
					<div class="alert alert-error">
						<% usernameControlGroup = "control-group error"; %>
						<s:message code="ui.editUser.error.missingUsername" text="ui.editUser.error.missingUsername" />
					</div>
				</c:if>
				<c:if test="${failure eq 'usernameExists'}">
					<div class="alert alert-error">
						<% usernameControlGroup = "control-group error"; %>
						<s:message code="ui.editUser.error.usernameExists" text="ui.editUser.error.usernameExists" />
					</div>
				</c:if>	
				<c:if test="${failure eq 'missingFirstname'}">
					<div class="alert alert-error">
						<% firstnameControlGroup = "control-group error"; %>
						<s:message code="ui.editUser.error.missingFirstname" text="ui.editUser.error.missingFirstname" />
					</div>
				</c:if>	
				<c:if test="${failure eq 'missingLastname'}">
					<div class="alert alert-error">
						<% lastnameControlGroup = "control-group error"; %>
						<s:message code="ui.editUser.error.missingLastname" text="ui.editUser.error.missingLastname" />
					</div>
				</c:if>	
				<c:if test="${failure eq 'emailExists'}">
					<div class="alert alert-error">
						<% emailControlGroup = "control-group error"; %>
						<s:message code="ui.editUser.error.emailExists" text="ui.editUser.error.emailExists" />
					</div>
				</c:if>
				<c:if test="${failure eq 'invalidEmail'}">
					<div class="alert alert-error">
						<% emailControlGroup = "control-group error"; %>
						<s:message code="ui.editUser.error.invalidEmail" text="ui.editUser.error.invalidEmail" />
					</div>
				</c:if>
				<c:if test="${failure eq 'passwordLength'}">
					<div class="alert alert-error">
						<% passwordControlGroup = "control-group error"; %>
						<s:message code="ui.editUser.error.passwordLength" text="ui.editUser.error.passwordLength" />
					</div>
				</c:if>
				<c:if test="${failure eq 'passwordInequality'}">
					<div class="alert alert-error">
						<% passwordControlGroup = "control-group error"; %>	
						<s:message code="ui.editUser.error.passwordInequality" text="ui.editUser.error.passwordInequality" />
					</div>
				</c:if>
		
				<div class="well" style="width: 550px; margin: 0 auto;">
					<form class="form-horizontal" name="f" action="checkEditUserForm?username=${user.username}&r=${r}" accept-charset="UTF-8" method="POST">
						<h3>
							<s:message code="ui.editUser" text="ui.editUser" />
						</h3>
						<c:if test="${adminEdit}">
							<div class="<%=usernameControlGroup%>">
								<label class="control-label"> <s:message
										code="user.username" text="user.username" />
								</label>
								<div class="controls">
									<input type="text" name="edit_user_username" value="${edit_user_username_value}" />
								</div>
							</div>
						</c:if>
						<div class="<%=firstnameControlGroup%>">
							<label class="control-label"> <s:message
									code="user.firstname" text="user.firstname" />
							</label>
							<div class="controls">
								<input type="text" name="edit_user_firstname" value="${edit_user_firstname_value}" />
							</div>
						</div>
						<div class="<%=lastnameControlGroup%>">
							<label class="control-label"> <s:message
									code="user.lastname" text="user.lastname" />
							</label>
							<div class="controls">
								<input type="text" name="edit_user_lastname" value="${edit_user_lastname_value}" />
							</div>
						</div>
						<div class="control-group">
							<label class="control-label"> <s:message
									code="user.institution" text="user.institution" />
							</label>
							<div class="controls">
								<input type="text" name="edit_user_institution" value="${edit_user_institution_value}" />
							</div>
						</div>
						<div class="<%=emailControlGroup%>">
							<label class="control-label"> <s:message
									code="user.email" text="user.email" />
							</label>
							<div class="controls">
								<input type="text" name="edit_user_email" value="${edit_user_email_value}" />
							</div>
						</div>
						
						<c:if test="${userEdit}">
							<div class="<%=passwordControlGroup%>">
								<label class="control-label"> <s:message
										code="ui.editUser.newPassword" text="ui.editUser.newPassword" />
								</label>
								<div class="controls">
									<input type="password" name="edit_user_new_password" value="${edit_user_new_password}" />
								</div>
							</div>					
							<div class="<%=passwordControlGroup%>">
								<label class="control-label"> <s:message
										code="ui.editUser.newPasswordConfirmation" text="ui.editUser.newPasswordConfirmation" />
								</label>
								<div class="controls">
									<input type="password" name="edit_user_new_password_confirmation" value="${edit_user_new_password_confirmation}" />
								</div>
							</div>
						</c:if>
						
						<c:if test="${adminEdit}">
							<div class="control-group">
								<div class="controls">
									<label>
										<c:choose>
											<c:when test="${edit_user_activated_value}">
												<input type="checkbox" class="edit-user-checkbox" name="edit_user_activated" checked />
											</c:when>
											<c:otherwise>
												<input type="checkbox" class="edit-user-checkbox" name="edit_user_activated" />
											</c:otherwise>
										</c:choose>
										<s:message code="ui.editUser.activated" text="ui.editUser.activated" />
									</label>
								
									<label>
										<c:choose>
											<c:when test="${edit_user_role_admin_value}">
												<input type="checkbox" class="edit-user-checkbox" name="edit_user_role_admin" checked />
											</c:when>
											<c:otherwise>
												<input type="checkbox" class="edit-user-checkbox" name="edit_user_role_admin" />
											</c:otherwise>
										</c:choose>
								
										<s:message code="ui.editUser.roleAdmin" text="ui.editUser.roleAdmin" />
									</label>
									
									<label>
										<c:choose>
											<c:when test="${edit_user_role_editor_value}">
												<input type="checkbox" class="edit-user-checkbox" name="edit_user_role_editor" checked />
											</c:when>
											<c:otherwise>
												<input type="checkbox" class="edit-user-checkbox" name="edit_user_role_editor" />
											</c:otherwise>
										</c:choose>
								
										<s:message code="ui.editUser.roleEditor" text="ui.editUser.roleEditor" />
									</label>
								
									<label>
										<c:choose>
											<c:when test="${edit_user_role_reisestipendium_value}">
												<input type="checkbox" class="edit-user-checkbox" name="edit_user_role_reisestipendium" checked />
											</c:when>
											<c:otherwise>
												<input type="checkbox" class="edit-user-checkbox" name="edit_user_role_reisestipendium" />
											</c:otherwise>
										</c:choose>
								
										<s:message code="ui.editUser.roleReisestipendium" text="ui.editUser.roleReisestipendium" />
									</label>
								</div>
								<div class="control-group">
									<label class="control-label"> <s:message code="ui.editUser.groupAdminRights" text="ui.editUser.groupAdminRights" /></label>
									<div class="controls">	
										<select name="edit_group_admins" size="${recordGroupsSize}" multiple>
											<c:forEach var="recordGroup" items="${recordGroups}">
												<c:choose>
													<c:when test="${recordGroupValues[recordGroup.id]}">
														<option value="${recordGroup.id}" selected>${recordGroup.name}</option>
													</c:when>
													<c:otherwise>
														<option value="${recordGroup.id}">${recordGroup.name}</option>
													</c:otherwise>
												</c:choose>
											</c:forEach>
										</select>
									</div>
								</div>
							</div>
						</c:if>
						
						<div class="control-group">
							<label class="control-label">
								&nbsp;
							</label>
							<div class="controls">
								<c:choose>
									<c:when test="${r eq 'userManagement' }">
										<a href="userManagement" class="btn" data-dismiss="modal" aria-hidden="true"><s:message
											code="ui.cancel" text="ui.cancel" /></a>
									</c:when>
									<c:otherwise>
										<a href="redirect?r=${r}" class="btn" data-dismiss="modal" aria-hidden="true"><s:message
											code="ui.cancel" text="ui.cancel" /></a>
									</c:otherwise>
								</c:choose>
								<s:message code="ui.ok" text="ui.ok" var="submitValue" />
								<input type="submit" class="btn btn-primary" value="${submitValue}" />
							</div>
						</div>
					</form>
				</div>		
		
				<!-- Footer -->
				<gaz:footer/>
				
			</div>
		</div>
	</body>
</html>
