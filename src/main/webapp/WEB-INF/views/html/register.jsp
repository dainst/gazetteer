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
		<title><s:message code="ui.register" text="ui.register" /> | iDAI.gazetteer</title>
		<meta charset="utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<meta name="google-site-verification" content="axehIuQKDs9bKUYzUl7hj1IvFMePho1--MppShoNQWk" />
		<link rel="icon" href="resources/ico/favicon.ico">
		<link rel="apple-touch-icon" sizes="144x144" href="resources/ico/apple-touch-icon-144.png">
		<link rel="apple-touch-icon" sizes="114x114" href="resources/ico/apple-touch-icon-114.png">
		<link rel="apple-touch-icon" sizes="72x72" href="resources/ico/apple-touch-icon-72.png">
		<link rel="apple-touch-icon" href="resources/ico/apple-touch-icon-57.png">
		<link href="../resources/archaeostrap/css/bootstrap.css" rel="stylesheet">
		<link href="//netdna.bootstrapcdn.com/twitter-bootstrap/2.3.1/css/bootstrap-responsive.min.css" rel="stylesheet">
		<link href="//netdna.bootstrapcdn.com/font-awesome/3.0.2/css/font-awesome.css" rel="stylesheet">
		<link href="//netdna.bootstrapcdn.com/font-awesome/3.0.2/css/font-awesome-ie7.css" rel="stylesheet">
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
						<a href="login?r=${r}" class="btn btn-small btn-primary">
							<s:message code="ui.login" text="ui.login"/>
						</a>
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
								</ul>
							</div>
							<ul class="nav pull-right">
								<li><a href="app/#!/about/"><s:message code="ui.about" text="ui.about" /></a></li>
								<li><a href="app/#!/help/"><s:message code="ui.help" text="ui.help" /></a></li>
  								<li class="dropdown">
	    							<a href="#" class="dropdown-toggle" data-toggle="dropdown">
	    								<s:message code="ui.language.current" text="ui.language.current" />
	    								<b class="caret"></b>
	    							</a>
	    							<ul class="dropdown-menu">
	      								<li><a href="?lang=en">English</a></li>
	      								<li><a href="?lang=de">Deutsch</a></li>
	      								<li><a href="?lang=ar">العربية</a></li>
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
			String policyControlGroup;
			
			usernameControlGroup = "control-group";
			firstnameControlGroup = "control-group";
			lastnameControlGroup = "control-group";
			emailControlGroup = "control-group";
			passwordControlGroup = "control-group";
			policyControlGroup = "control-group"; %>
		
			<div class="gaz-container">
				<c:if test="${failure eq 'missingUsername'}">
					<% usernameControlGroup = "control-group error"; %>
					<div class="alert alert-error">
						<s:message code="ui.register.error.missingUsername" text="ui.register.error.missingUsername" />
					</div>
				</c:if>
				<c:if test="${failure eq 'usernameExists'}">
					<% usernameControlGroup = "control-group error"; %>
					<div class="alert alert-error">
						<s:message code="ui.register.error.usernameExists" text="ui.register.error.usernameExists" />
					</div>
				</c:if>	
				<c:if test="${failure eq 'missingFirstname'}">
					<% firstnameControlGroup = "control-group error"; %>
					<div class="alert alert-error">
						<s:message code="ui.register.error.missingFirstname" text="ui.register.error.missingFirstname" />
					</div>
				</c:if>	
				<c:if test="${failure eq 'missingLastname'}">
					<% lastnameControlGroup = "control-group error"; %>
					<div class="alert alert-error">
						<s:message code="ui.register.error.missingLastname" text="ui.register.error.missingLastname" />
					</div>
				</c:if>	
				<c:if test="${failure eq 'emailExists'}">
					<% emailControlGroup = "control-group error"; %>
					<div class="alert alert-error">
						<s:message code="ui.register.error.emailExists" text="ui.register.error.emailExists" />
					</div>
				</c:if>
				<c:if test="${failure eq 'invalidEmail'}">
					<% emailControlGroup = "control-group error"; %>
					<div class="alert alert-error">
						<s:message code="ui.register.error.invalidEmail" text="ui.register.error.invalidEmail" />
					</div>
				</c:if>
				<c:if test="${failure eq 'passwordLength'}">
					<% passwordControlGroup = "control-group error"; %>
					<div class="alert alert-error">
						<s:message code="ui.register.error.passwordLength" text="ui.register.error.passwordLength" />
					</div>
				</c:if>
				<c:if test="${failure eq 'passwordInequality'}">
					<% passwordControlGroup = "control-group error"; %>
					<div class="alert alert-error">
						<s:message code="ui.register.error.passwordInequality" text="ui.register.error.passwordInequality" />
					</div>
				</c:if>
				<c:if test="${failure eq 'policyUnconfirmed'}">
					<% policyControlGroup = "control-group error"; %>
					<div class="alert alert-error">
						<s:message code="ui.register.error.policyUnconfirmed" text="ui.register.error.policyUnconfirmed" />
					</div>
				</c:if>

				<div class="well" style="width: 550px; margin: 0 auto;">
					<form class="form-horizontal" name="f" action="checkRegisterForm?r=${r}" accept-charset="UTF-8" method="POST">
						<c:choose>
							<c:when test="${language eq 'ar'}">
								<p dir="rtl" lang="ar" class="lead" style="font-size: 1.8em; margin-bottom: 12px; margin-top: 15px;"><s:message code="ui.register" text="ui.register" /></p>
							</c:when>
							<c:otherwise>
								<p class="lead" style="font-size: 1.8em; margin-bottom: 12px; margin-top: 15px;"><s:message code="ui.register" text="ui.register" /></p>
							</c:otherwise>
						</c:choose>
						<div class="well" style="padding: 12px;">
							<c:choose>
								<c:when test="${language eq 'ar'}">
									<p dir="rtl" lang="ar" class="lead" style="font-size: 15px; margin-bottom: 0px;"><s:message code="ui.register.expl" text="ui.register.expl" /></p>
								</c:when>
								<c:otherwise>
									<p class="lead" style="font-size: 15px; margin-bottom: 0px;"><s:message code="ui.register.expl" text="ui.register.expl" /></p>
								</c:otherwise>
							</c:choose>
						</div>
						<div class="<%=usernameControlGroup%>">
							<label class="control-label"> <s:message
									code="user.username" text="user.username" />
							</label>
							<div class="controls">
								<input type="text" name="register_username" value="${register_username_value}">
							</div>
						</div>
						<div class="<%=firstnameControlGroup%>">
							<label class="control-label"> <s:message
									code="user.firstname" text="user.firstname" />
							</label>
							<div class="controls">
								<input type="text" name="register_firstname" value="${register_firstname_value}">
							</div>
						</div>
						<div class="<%=lastnameControlGroup%>">
							<label class="control-label"> <s:message
									code="user.lastname" text="user.lastname" />
							</label>
							<div class="controls">
								<input type="text" name="register_lastname" value="${register_lastname_value}">
							</div>
						</div>
						<div class="control-group">
							<label class="control-label"> <s:message
									code="user.institution" text="user.institution" />
							</label>
							<div class="controls">
								<input type="text" name="register_institution" value="${register_institution_value}">
							</div>
						</div>
						<div class="<%=emailControlGroup%>">
							<label class="control-label"> <s:message
									code="user.email" text="user.email" />
							</label>
							<div class="controls">
								<input type="text" name="register_email" value="${register_email_value}">
							</div>
						</div>
						<div class="<%=passwordControlGroup%>">
							<label class="control-label"> <s:message
									code="ui.password" text="ui.password" />
							</label>
							<div class="controls">
								<input type="password" name="register_password">
							</div>
						</div>
						<div class="<%=passwordControlGroup%>">
							<label class="control-label"> <s:message
									code="ui.passwordConfirmation" text="ui.passwordConfirmation" />
							</label>
							<div class="controls">
								<input type="password" name="register_password_confirmation">
							</div>
						</div>
						<div class="<%=policyControlGroup%>" id="policyConf">
							<label class="controls checkbox inline">
								<input type="checkbox" name="register_policy" value="Confirmed">
								<s:message code="ui.register.policy" text="ui.register.policy" />
							</label>
						</div>
						<div class="control-group">
							<label class="control-label">
								&nbsp;
							</label>
							<div class="controls">
								<a href="redirect?r=${r}" class="btn" data-dismiss="modal" aria-hidden="true"><s:message
										code="ui.cancel" text="ui.cancel" /></a>
								<s:message code="ui.register" text="ui.register" var="submitValue" />
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
