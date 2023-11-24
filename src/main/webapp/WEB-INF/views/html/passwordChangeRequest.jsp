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
		<title><s:message code="ui.passwordChangeRequest" text="ui.passwordChangeRequest" /> | iDAI.gazetteer</title>
		<meta charset="utf-8">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
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
		<script	src="../resources/archaeostrap//js/bootstrap.js"></script>
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
						<a href="register?r=${r}" class="btn btn-small btn-primary">
							<s:message code="ui.register" text="ui.register"/>
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
						<div class="navbar-inner" style="height: 43px">
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
		
			<div class="gaz-container">
				<c:if test="${failure eq 'missingUsername'}">
					<div class="alert alert-error">
						<s:message code="ui.passwordChangeRequest.error.missingUsername" text="ui.passwordChangeRequest.error.missingUsername" />
					</div>
				</c:if>
				<c:if test="${failure eq 'userNotFound'}">
					<div class="alert alert-error">
						<s:message code="ui.passwordChangeRequest.error.userNotFound" text="ui.passwordChangeRequest.error.userNotFound" />
					</div>
				</c:if>
				<c:if test="${failure eq 'requestExists'}">
					<div class="alert alert-error">
						<s:message code="ui.passwordChangeRequest.error.requestExists" text="ui.passwordChangeRequest.error.requestExists" />
					</div>
				</c:if>
		
				<div class="well" style="width: 550px; margin: 0 auto;">
					<form class="form-horizontal" name="f" action="checkPasswordChangeRequestForm?r=${r}" accept-charset="UTF-8" method="POST">
						<h3>
							<s:message code="ui.passwordChangeRequest" text="ui.passwordChangeRequest" />
						</h3>
						<div class="control-group">
								<label class="control-label"> <s:message
										code="user.username" text="user.username" />
								</label>
								<div class="controls">
									<input type="text" name="password_change_request_username" value="${password_change_request_username_value}" />
								</div>
						</div>
						
						<div class="control-group">
							<label class="control-label">
								&nbsp;
							</label>
							<div class="controls">
								<a href="redirect?r=${r}" class="btn" data-dismiss="modal" aria-hidden="true"><s:message
											code="ui.cancel" text="ui.cancel" /></a>
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