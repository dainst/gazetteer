<%@ taglib uri="http://www.springframework.org/tags" prefix="s"%>
<%@ page contentType="text/html; charset=utf-8" session="false"%>

<div class="subnav">
	<ul class="nav nav-pills">
		<li ng-click="offset=0; limit=10; search()">
		    <a style="border: none"><i class="icon-stop"></i> 10</a>
		</li>
		<li ng-click="offset=0; limit=100; search()">
			<a href="" style="border: none"><i class="icon-th-large"></i> 100</a>
		</li>
		<li ng-click="offset=0; limit=1000; search()">
			<a href="" style="border-left: none"><i class="icon-th"></i> 1000</a>
		</li>
		<li class="dropdown">
			<a href="#" class="dropdown-toggle" data-toggle="dropdown">
				<s:message code="ui.list.view" text="ui.list.view" />
				<b class="caret"></b>
			</a>
			<ul class="dropdown-menu">
				<li>
				    <a href="">
						<i class="icon-globe"></i> <i class="icon-list"></i>
						<s:message code="ui.search.view.mapAndTable" text="ui.search.view.mapAndTable" />
					</a>
				</li>
				<li>
					<a href="">
						<i class="icon-globe"></i> <s:message code="ui.search.view.map" />
					</a>
				</li>
				<li>
					<a href="">
						<i class="icon-list"></i> <s:message code="ui.search.view.table" />
					</a>
				</li>
			</ul>
		</li>
		<li class="pull-right">
			<ul class="pagination">
				<li ng-class="{disabled:(offset == 0)}" ng-click="offset=offset-limit; search()">
					<a>&larr; <s:message code="ui.previous" /></a>
				</li>
				<li>
					<a><s:message code="ui.page" text="Seite" /> 
								<%--
									<c:set var="currentPage" value="${fn:substringBefore(offset/limit + 1, '.')}"/>
									<c:set var="totalPages" value="${fn:substringBefore(hits/limit + 0.999, '.')}"/>
									<select style="width: auto; display: inline">
										<c:forEach var="i" begin="1" end="${totalPages}">
											<c:choose>
												<c:when test="${i == currentPage}">
													<option selected="selected">${i}</option>
												</c:when>
												<c:otherwise>
													<option>${i}</option>
												</c:otherwise>
											</c:choose>
										</c:forEach>
									</select>
								--%> 
						{{page()}} / {{limit}}
					</a>
				</li>
				<li ng-click="offset=offset+limit; search()">
					<a><s:message code="ui.next" text="Vor"/> &rarr;</a>
				</li>
			</ul>
		</li>
	</ul>

</div>

<div class="row-fluid">

	<div class="span5 well">
	
	</div>
	
	<div class="span7">

		<table class="table table-striped">
			<thead>
				<tr>
					<td>#</td>
					<td><s:message code="domain.placename.title" text="domain.placename.title" /></td>
					<td><s:message code="domain.thesaurus" text="domain.thesaurus" /></td>
					<td><s:message code="domain.place.uri" text="domain.place.uri" /></td>
				</tr>
			</thead>
			<tbody>
					<tr ng-repeat="place in places">
						<td>{{place.gazId}}</td>
						<td><a href="">{{place.prefName.title}}</a></td>
						<td>{{place.thesaurus}}</td>
						<td>
							<s:message code="ui.copyToClipboard" var="copyMsg" />
							<div class="modal hide" id="copyUriModal">
								<div class="modal-header">
									<button type="button" class="close" data-dismiss="modal">×</button>
									<h3>
										<s:message code="ui.copyToClipboardHeading" />
									</h3>
								</div>
								<div class="modal-body">
									<label>${copyMsg}</label>
									<input class="input-xxlarge" type="text" value="{{baseUri}}place/{{place.id}}" id="copyUriInput">
								</div>
							</div>
							<script type="text/javascript">
								$("#copyUriModal").on("shown",function() {
									$("#copyUriInput").focus().select();
								});
							</script>
							<a data-toggle="modal" href="#copyUriModal"><i class="icon-share"></i></a></td>
					</tr>
				</c:forEach>
			</tbody>
		</table>

	</div>

</div>