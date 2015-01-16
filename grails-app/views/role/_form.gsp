<%@ page import="be.cytomine.apps.iris.auth.Role" %>



<div class="fieldcontain ${hasErrors(bean: roleInstance, field: 'authority', 'error')} ">
	<label for="authority">
		<g:message code="role.authority.label" default="Authority" />
		
	</label>
	<g:textField name="authority" value="${roleInstance?.authority}"/>
</div>

