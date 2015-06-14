<%--
  Created by IntelliJ IDEA.
  User: phil
  Date: 15/01/15
  Time: 20:07
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
    <meta name='layout' content='main'/>
    %{--<link rel="stylesheet" href="lib/bootstrap/css/bootstrap.css">--}%
    <title>Cytomine IRIS ADMIN interface</title>
    <style type="text/css">
        h4 {
            margin-top: 15px;
        }
    </style>
</head>

<body>

<h3>Cytomine IRIS Administrator Interface</h3>
<h4>Database</h4>
Find the H2 database console
<a href="${grailsApplication.config.grails.cytomine.apps.iris.host}${grailsApplication.config.grails.dbconsole.urlRoot}">here</a>.

<h4>Spring Security Users (Backend)</h4>
Edit spring security users in
<a href="${createLink(controller: 'user', action: 'index')}">${createLink(uri: '/user')}</a>

<h4>Mail Configuration Check</h4>
Send a test email to the IRIS administrator <a href="${createLink(controller: 'admin', action:'testMailConfig')}">here</a>.

<h4>Activity Log</h4>
Show the automatic activity log
<a href="${createLink(controller: 'activity', action: 'index')}">here</a>.

</body>
</html>