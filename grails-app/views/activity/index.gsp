
<%@ page import="be.cytomine.apps.iris.Activity" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'activity.label', default: 'Activity')}" />
		<title><g:message code="default.list.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#list-activity" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="list-activity" class="content scaffold-list" role="main">
			<h1><g:message code="default.list.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
				<div class="message" role="status">${flash.message}</div>
			</g:if>
			<table>
			<thead>
					<tr>
					
						<g:sortableColumn property="description" title="${message(code: 'activity.description.label', default: 'Description')}" />
					
						<g:sortableColumn property="action" title="${message(code: 'activity.action.label', default: 'Action')}" />
					
						<g:sortableColumn property="cmAnnotationID" title="${message(code: 'activity.cmAnnotationID.label', default: 'Cm Annotation ID')}" />
					
						<g:sortableColumn property="cmImageID" title="${message(code: 'activity.cmImageID.label', default: 'Cm Image ID')}" />
					
						<g:sortableColumn property="cmProjectID" title="${message(code: 'activity.cmProjectID.label', default: 'Cm Project ID')}" />
					
						<th><g:message code="activity.createdBy.label" default="Created By" /></th>
					
					</tr>
				</thead>
				<tbody>
				<g:each in="${activityInstanceList}" status="i" var="activityInstance">
					<tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
					
						<td><g:link action="show" id="${activityInstance.id}">${fieldValue(bean: activityInstance, field: "description")}</g:link></td>
					
						<td>${fieldValue(bean: activityInstance, field: "action")}</td>
					
						<td>${fieldValue(bean: activityInstance, field: "cmAnnotationID")}</td>
					
						<td>${fieldValue(bean: activityInstance, field: "cmImageID")}</td>
					
						<td>${fieldValue(bean: activityInstance, field: "cmProjectID")}</td>
					
						<td>${fieldValue(bean: activityInstance, field: "createdBy")}</td>
					
					</tr>
				</g:each>
				</tbody>
			</table>
			<div class="pagination">
				<g:paginate total="${activityInstanceCount ?: 0}" />
			</div>
		</div>
	</body>
</html>
