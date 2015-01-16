<%@ page import="be.cytomine.apps.iris.Activity" %>



<div class="fieldcontain ${hasErrors(bean: activityInstance, field: 'description', 'error')} ">
	<label for="description">
		<g:message code="activity.description.label" default="Description" />
		
	</label>
	<g:textArea name="description" cols="40" rows="5" maxlength="5000" value="${activityInstance?.description}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: activityInstance, field: 'action', 'error')} ">
	<label for="action">
		<g:message code="activity.action.label" default="Action" />
		
	</label>
	<g:select name="action" from="${be.cytomine.apps.iris.ActivityAction?.values()}" keys="${be.cytomine.apps.iris.ActivityAction.values()*.name()}" value="${activityInstance?.action?.name()}" noSelection="['': '']"/>
</div>

<div class="fieldcontain ${hasErrors(bean: activityInstance, field: 'cmAnnotationID', 'error')} ">
	<label for="cmAnnotationID">
		<g:message code="activity.cmAnnotationID.label" default="Cm Annotation ID" />
		
	</label>
	<g:field name="cmAnnotationID" type="number" value="${activityInstance.cmAnnotationID}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: activityInstance, field: 'cmImageID', 'error')} ">
	<label for="cmImageID">
		<g:message code="activity.cmImageID.label" default="Cm Image ID" />
		
	</label>
	<g:field name="cmImageID" type="number" value="${activityInstance.cmImageID}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: activityInstance, field: 'cmProjectID', 'error')} ">
	<label for="cmProjectID">
		<g:message code="activity.cmProjectID.label" default="Cm Project ID" />
		
	</label>
	<g:field name="cmProjectID" type="number" value="${activityInstance.cmProjectID}"/>
</div>

<div class="fieldcontain ${hasErrors(bean: activityInstance, field: 'createdBy', 'error')} ">
	<label for="createdBy">
		<g:message code="activity.createdBy.label" default="Created By" />
		
	</label>
	<g:select id="createdBy" name="createdBy.id" from="${be.cytomine.apps.iris.IRISUser.list()}" optionKey="id" value="${activityInstance?.createdBy?.id}" class="many-to-one" noSelection="['null': '']"/>
</div>

<div class="fieldcontain ${hasErrors(bean: activityInstance, field: 'lastUpdatedBy', 'error')} ">
	<label for="lastUpdatedBy">
		<g:message code="activity.lastUpdatedBy.label" default="Last Updated By" />
		
	</label>
	<g:select id="lastUpdatedBy" name="lastUpdatedBy.id" from="${be.cytomine.apps.iris.IRISUser.list()}" optionKey="id" value="${activityInstance?.lastUpdatedBy?.id}" class="many-to-one" noSelection="['null': '']"/>
</div>

<div class="fieldcontain ${hasErrors(bean: activityInstance, field: 'user', 'error')} ">
	<label for="user">
		<g:message code="activity.user.label" default="User" />
		
	</label>
	<g:select id="user" name="user.id" from="${be.cytomine.apps.iris.IRISUser.list()}" optionKey="id" value="${activityInstance?.user?.id}" class="many-to-one" noSelection="['null': '']"/>
</div>

