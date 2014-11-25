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
<title><s:message code="ui.globalChangeHistory" text="ui.globalChangeHistory" /> | iDAI.gazetteer</title>
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
<link href="resources/bootstrap/css/daterangepicker-bs2.css" rel="stylesheet">
<script	src="//ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js"></script>	
<script	src="//arachne.uni-koeln.de/archaeostrap/assets/js/bootstrap.js"></script>	
<script src='//maps.google.com/maps/api/js?key=${googleMapsApiKey}&amp;sensor=false&libraries=visualization'></script>
<script src="resources/js/custom.js"></script>
<script src="resources/bootstrap/js/moment.js"></script>
<script src="resources/bootstrap/js/daterangepicker.js"></script>

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
   					<li>
   						<a href="">
   							<s:message code="ui.globalChangeHistory" text="ui.globalChangeHistory"/>
   						</a>
   					</li>
   					<sec:authorize access="hasRole('ROLE_ADMIN')">
   						<li>
   							<a href="userManagement">
   								<s:message code="ui.userManagement" text="ui.userManagement"/>
   							</a>
   						</li>
   					</sec:authorize>
   					<sec:authorize access="hasRole('ROLE_ADMIN')">
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

	<div class="container">
		
		<h3>
			<s:message code="ui.globalChangeHistory" text="ui.globalChangeHistory" />
		</h3>
		
			<b><s:message code="ui.globalChangeHistory.dateRange" text="ui.globalChangeHistory.dateRange" />:</b><br/>
			<div id="daterange" class="btn" style="display: inline-block; background: #fff; cursor: pointer; padding: 5px 10px; border: 1px solid #ccc">
        		<i class="icon-calendar"></i>&nbsp;
           		<span></span>
            	<b class="caret"></b>
			</div>

		<form name='dateContainerForm'>
			<input type='hidden' name=startDate value="${startDate}" />
			<input type='hidden' name=endDate value="${endDate}" />
			<input type='hidden' name=startDatePres value="${startDatePres}" />
			<input type='hidden' name=endDatePres value="${endDatePres}" />
		</form>

        <script type="text/javascript">
        
 			$(document).ready(function() {
 				
 				startDatePres = document.dateContainerForm.startDatePres.value;
 				endDatePres = document.dateContainerForm.endDatePres.value;
 				
				$("#daterange span").html(startDatePres + " - " + endDatePres);
  				$("#daterange").daterangepicker(
  					{
  						format: "DD.MM.YYYY",
  						minDate: "01.01.2014",
  						maxDate: moment(),
  						startDate: startDatePres,
  						endDate: endDatePres,
  						showDropdowns: true,
  						separator: " - ",
  						applyClass: "btn btn-primary",
  						ranges: {
  							"<s:message code='ui.dateRangePicker.thisWeek' text='ui.dateRangePicker.thisWeek' />":
  								[moment().startOf('week').add('day', 1), moment().endOf('week').add('day', 1)],
  							"<s:message code='ui.dateRangePicker.lastWeek' text='ui.dateRangePicker.lastWeek' />":
  	  							[moment().subtract('week', 1).startOf('week').add('day', 1), moment().subtract('week', 1).endOf('week').add('day', 1)],
  							"<s:message code='ui.dateRangePicker.thisMonth' text='ui.dateRangePicker.thisMonth' />":
  								[moment().startOf('month'), moment().endOf('month')],
  							"<s:message code='ui.dateRangePicker.lastMonth' text='ui.dateRangePicker.lastMonth' />":
  								[moment().subtract('month', 1).startOf('month'), moment().subtract('month', 1).endOf('month')],
  							"<s:message code='ui.dateRangePicker.everything' text='ui.dateRangePicker.everything' />":
  	  							[moment("01012014", "DDMMYYYY"), moment()]
  						},
  						locale: {
  	                        applyLabel: "<s:message code='ui.dateRangePicker.apply' text='ui.dateRangePicker.apply' />",
  	                        cancelLabel: "<s:message code='ui.dateRangePicker.cancel' text='ui.dateRangePicker.cancel' />",
  	                        fromLabel: "<s:message code='ui.dateRangePicker.from' text='ui.dateRangePicker.from' />",
  	                        toLabel: "<s:message code='ui.dateRangePicker.to' text='ui.dateRangePicker.to' />",
  	                        customRangeLabel: "<s:message code='ui.dateRangePicker.customRange' text='ui.dateRangePicker.customRange' />",
  	                        daysOfWeek: ["<s:message code='ui.dateRangePicker.days.sunday' text='ui.dateRangePicker.days.sunday' />",
  	                                   	 "<s:message code='ui.dateRangePicker.days.monday' text='ui.dateRangePicker.days.monday' />",
  	                                 	 "<s:message code='ui.dateRangePicker.days.tuesday' text='ui.dateRangePicker.days.tuesday' />",
  	                               		 "<s:message code='ui.dateRangePicker.days.wednesday' text='ui.dateRangePicker.days.wednesday' />",
  	                             		 "<s:message code='ui.dateRangePicker.days.thursday' text='ui.dateRangePicker.days.thursday' />",
  	                           			 "<s:message code='ui.dateRangePicker.days.friday' text='ui.dateRangePicker.days.friday' />",
  	                         			 "<s:message code='ui.dateRangePicker.days.saturday' text='ui.dateRangePicker.days.saturday' />",
  	                           			],
  	                        monthNames: ["<s:message code='ui.dateRangePicker.months.january' text='ui.dateRangePicker.days.january' />",
  	                                   	 "<s:message code='ui.dateRangePicker.months.february' text='ui.dateRangePicker.days.february' />",
  	                                   	 "<s:message code='ui.dateRangePicker.months.march' text='ui.dateRangePicker.days.march' />",
  	                                 	 "<s:message code='ui.dateRangePicker.months.april' text='ui.dateRangePicker.days.april' />",
  	                               		 "<s:message code='ui.dateRangePicker.months.may' text='ui.dateRangePicker.days.may' />",
  	                             		 "<s:message code='ui.dateRangePicker.months.june' text='ui.dateRangePicker.days.june' />",
  	                           			 "<s:message code='ui.dateRangePicker.months.july' text='ui.dateRangePicker.days.july' />",
  	                         			 "<s:message code='ui.dateRangePicker.months.august' text='ui.dateRangePicker.days.august' />",
  	                       				 "<s:message code='ui.dateRangePicker.months.september' text='ui.dateRangePicker.days.september' />",
  	                     				 "<s:message code='ui.dateRangePicker.months.october' text='ui.dateRangePicker.days.october' />",
  	                   					 "<s:message code='ui.dateRangePicker.months.november' text='ui.dateRangePicker.days.november' />",
  	                 					 "<s:message code='ui.dateRangePicker.months.december' text='ui.dateRangePicker.days.december' />",
  	                                   ],
  	                        firstDay: 1
  	                    }
  					}, function(start, end) {
						
						startDateString = start.format("YYYY-MM-DD");
						endDateString = end.format("YYYY-MM-DD");
						
						window.location = "globalChangeHistory?sort=${lastSorting}&isDescending=${isDescending}&startDate=" + startDateString + "&endDate=" + endDateString;
  					}		
  				);
			});
		</script>
			
		<table class="table table-condensed table-hover">
			<thead>
				<tr>
					<c:choose>
						<c:when test="${lastSorting == null or lastSorting == ''}">
							<th><a href="globalChangeHistory?isDescending=<c:out value="${!isDescending}" />&startDate=<c:out value="${startDate}" />&endDate=<c:out value="${endDate}" />"><s:message code="ui.changeHistory.changeDate" text="ui.changeHistory.changeDate" /></a></th>
						</c:when>
						<c:otherwise>
							<th><a href="globalChangeHistory?isDescending=false&startDate=<c:out value="${startDate}" />&endDate=<c:out value="${endDate}" />"><s:message code="ui.changeHistory.changeDate" text="ui.changeHistory.changeDate" /></a></th>
						</c:otherwise>
					</c:choose>
					
					<sec:authorize access="hasRole('ROLE_ADMIN')">
						<c:choose>
							<c:when test="${lastSorting eq 'username'}">
								<th><a href="globalChangeHistory?sort=username&isDescending=<c:out value="${!isDescending}" />&startDate=<c:out value="${startDate}" />&endDate=<c:out value="${endDate}" />"><s:message code="ui.username" text="ui.username" /></a></th>
							</c:when>
							<c:otherwise>
								<th><a href="globalChangeHistory?sort=username&isDescending=false&startDate=<c:out value="${startDate}" />&endDate=<c:out value="${endDate}" />"><s:message code="ui.username" text="ui.username" /></a></th>
							</c:otherwise>
						</c:choose>
					</sec:authorize>
					
					<c:choose>
						<c:when test="${lastSorting eq 'placeId'}">
							<th><a href="globalChangeHistory?sort=placeId&isDescending=<c:out value="${!isDescending}" />&startDate=<c:out value="${startDate}" />&endDate=<c:out value="${endDate}" />"><s:message code="ui.changeHistory.placeId" text="ui.changeHistory.placeId" /></a></th>
						</c:when>
						<c:otherwise>
							<th><a href="globalChangeHistory?sort=placeId&isDescending=false&startDate=<c:out value="${startDate}" />&endDate=<c:out value="${endDate}" />"><s:message code="ui.changeHistory.placeId" text="ui.changeHistory.placeId" /></a></th>
						</c:otherwise>
					</c:choose>
					
					<c:choose>
						<c:when test="${lastSorting eq 'placename'}">
							<th><a href="globalChangeHistory?sort=placename&isDescending=<c:out value="${!isDescending}" />&startDate=<c:out value="${startDate}" />&endDate=<c:out value="${endDate}" />"><s:message code="ui.changeHistory.placename" text="ui.changeHistory.placename" /></a></th>
						</c:when>
						<c:otherwise>
							<th><a href="globalChangeHistory?sort=placename&isDescending=false&startDate=<c:out value="${startDate}" />&endDate=<c:out value="${endDate}" />"><s:message code="ui.changeHistory.placename" text="ui.changeHistory.placename" /></a></th>
						</c:otherwise>
					</c:choose>
					
					<c:choose>
						<c:when test="${lastSorting eq 'changeType'}">
							<th><a href="globalChangeHistory?sort=changeType&isDescending=<c:out value="${!isDescending}" />&startDate=<c:out value="${startDate}" />&endDate=<c:out value="${endDate}" />"><s:message code="ui.changeHistory.changeType" text="ui.changeHistory.changeType" /></a></th>
						</c:when>
						<c:otherwise>
							<th><a href="globalChangeHistory?sort=changeType&isDescending=false&startDate=<c:out value="${startDate}" />&endDate=<c:out value="${endDate}" />"><s:message code="ui.changeHistory.changeType" text="ui.changeHistory.changeType" /></a></th>
						</c:otherwise>
					</c:choose>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="changeRecord" items="${changes}">
					<tr>
						<td>${changeRecord.changeDateAsText}</td>
						<sec:authorize access="hasRole('ROLE_ADMIN')">
							<c:choose>
								<c:when test="${changeRecord.userId != null}">
									<td><a href="userManagement?showUser=${changeRecord.userId}">${changeRecord.username}</a></td>
								</c:when>
								<c:otherwise>
									<td>${changeRecord.username}</td>
								</c:otherwise>
							</c:choose>
						</sec:authorize>
						<td>
							<c:choose>
								<c:when test="${changeRecord.notFound}">
									${changeRecord.placeId}
								</c:when>
								<c:otherwise>
									<a href="place/${changeRecord.placeId}">${changeRecord.placeId}</a>
								</c:otherwise>
							</c:choose>
						</td>
						<td>
							<c:if test="${changeRecord.notFound or (changeRecord.placename == null or changeRecord.placename eq '')}">
								<em>
							</c:if>
							<c:choose>
								<c:when test="${changeRecord.placename == null or changeRecord.placename eq ''}"><s:message code="domain.place.untitled" text="domain.place.untitled" /></c:when>
								<c:otherwise>${changeRecord.placename}</c:otherwise>
							</c:choose>
							<c:if test="${changeRecord.notFound or (changeRecord.placename == null or changeRecord.placename eq '')}">
								</em>
							</c:if>
						</td>
						<td><s:message code="ui.changeHistory.changeType.${changeRecord.changeType}" text="ui.changeHistory.changeType.${changeRecord.changeType}" /></td>
					</tr>
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
						<a href="globalChangeHistory?page=${page - 1}&sort=${lastSorting}&isDescending=${isDescending}&startDate=<c:out value="${startDate}" />&endDate=<c:out value="${endDate}" />">&larr; <s:message code="ui.previous" /></a>
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
						<a href="globalChangeHistory?page=${page + 1}&sort=${lastSorting}&isDescending=${isDescending}&startDate=<c:out value="${startDate}" />&endDate=<c:out value="${endDate}" />"><s:message code="ui.next" text="Vor"/> &rarr;</a>
					</li>
				</c:otherwise>
			</c:choose>
			
		</ul>					

		<!-- Footer -->
		<gaz:footer/>
		
	</div>
	
</body>
</html>
