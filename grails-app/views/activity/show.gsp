
<%@ page import="be.cytomine.apps.iris.Activity" %>
<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'activity.label', default: 'Activity')}" />
		<title><g:message code="default.show.label" args="[entityName]" /></title>
	</head>
	<body>
		<a href="#show-activity" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
		<div class="nav" role="navigation">
			<ul>
				<li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
				<li><g:link class="list" action="index"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
				<li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
			</ul>
		</div>
		<div id="show-activity" class="content scaffold-show" role="main">
			<h1><g:message code="default.show.label" args="[entityName]" /></h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<ol class="property-list activity">
			
				<g:if test="${activityInstance?.description}">
				<li class="fieldcontain">
					<span id="description-label" class="property-label"><g:message code="activity.description.label" default="Description" /></span>
					
						<span class="property-value" aria-labelledby="description-label"><g:fieldValue bean="${activityInstance}" field="description"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${activityInstance?.action}">
				<li class="fieldcontain">
					<span id="action-label" class="property-label"><g:message code="activity.action.label" default="Action" /></span>
					
						<span class="property-value" aria-labelledby="action-label"><g:fieldValue bean="${activityInstance}" field="action"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${activityInstance?.cmAnnotationID}">
				<li class="fieldcontain">
					<span id="cmAnnotationID-label" class="property-label"><g:message code="activity.cmAnnotationID.label" default="Cm Annotation ID" /></span>
					
						<span class="property-value" aria-labelledby="cmAnnotationID-label"><g:fieldValue bean="${activityInstance}" field="cmAnnotationID"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${activityInstance?.cmImageID}">
				<li class="fieldcontain">
					<span id="cmImageID-label" class="property-label"><g:message code="activity.cmImageID.label" default="Cm Image ID" /></span>
					
						<span class="property-value" aria-labelledby="cmImageID-label"><g:fieldValue bean="${activityInstance}" field="cmImageID"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${activityInstance?.cmProjectID}">
				<li class="fieldcontain">
					<span id="cmProjectID-label" class="property-label"><g:message code="activity.cmProjectID.label" default="Cm Project ID" /></span>
					
						<span class="property-value" aria-labelledby="cmProjectID-label"><g:fieldValue bean="${activityInstance}" field="cmProjectID"/></span>
					
				</li>
				</g:if>
			
				<g:if test="${activityInstance?.createdBy}">
				<li class="fieldcontain">
					<span id="createdBy-label" class="property-label"><g:message code="activity.createdBy.label" default="Created By" /></span>
					
						<span class="property-value" aria-labelledby="createdBy-label"><g:link controller="IRISUser" action="show" id="${activityInstance?.createdBy?.id}">${activityInstance?.createdBy?.encodeAsHTML()}</g:link></span>
					
				</li>
				</g:if>
			
				<g:if test="${activityInstance?.dateCreated}">
				<li class="fieldcontain">
					<span id="dateCreated-label" class="property-label"><g:message code="activity.dateCreated.label" default="Date Created" /></span>
					
						<span class="property-value" aria-labelledby="dateCreated-label"><g:formatDate date="${activityInstance?.dateCreated}" /></span>
					
				</li>
				</g:if>
			
				<g:if test="${activityInstance?.lastUpdated}">
				<li class="fieldcontain">
					<span id="lastUpdated-label" class="property-label"><g:message code="activity.lastUpdated.label" default="Last Updated" /></span>
					
						<span class="property-value" aria-labelledby="lastUpdated-label"><g:formatDate date="${activityInstance?.lastUpdated}" /></span>
					
				</li>
				</g:if>
			
				<g:if test="${activityInstance?.lastUpdatedBy}">
				<li class="fieldcontain">
					<span id="lastUpdatedBy-label" class="property-label"><g:message code="activity.lastUpdatedBy.label" default="Last Updated By" /></span>
					
						<span class="property-value" aria-labelledby="lastUpdatedBy-label"><g:link controller="IRISUser" action="show" id="${activityInstance?.lastUpdatedBy?.id}">${activityInstance?.lastUpdatedBy?.encodeAsHTML()}</g:link></span>
					
				</li>
				</g:if>
			
				<g:if test="${activityInstance?.user}">
				<li class="fieldcontain">
					<span id="user-label" class="property-label"><g:message code="activity.user.label" default="User" /></span>
					
						<span class="property-value" aria-labelledby="user-label"><g:link controller="IRISUser" action="show" id="${activityInstance?.user?.id}">${activityInstance?.user?.encodeAsHTML()}</g:link></span>
					
				</li>
				</g:if>
			
			</ol>
			<g:form url="[resource:activityInstance, action:'delete']" method="DELETE">
				<fieldset class="buttons">
					<g:link class="edit" action="edit" resource="${activityInstance}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
					<g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
				</fieldset>
			</g:form>
		</div>
	</body>
</html>
