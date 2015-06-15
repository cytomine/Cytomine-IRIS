/* Copyright the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


// THIS IS THE DEVELOPMENT EXTERNAL CYTOMINE IRIS CONFIGURATION FILE
// use ConfigSlurper Syntax to configure the settings
println "loading development configuration..."

// #################################
// Java Util Logging Bridge to log4j
// #################################
// for performance reasons, set this to false in production environments
grails.logging.jul.usebridge = true

// ##################
// IRIS host settings
// ##################
// the host name, port and protocol of the machine you are running IRIS on
grails.host = "localhost"
// if standard ports are used, leave grails.port empty
grails.port = "8080"
// either http or https
grails.protocol = "http"
// automatically assembling the server URL
grails.serverURL = grails.protocol + "://" + grails.host + ((grails.port == "") ? "" : ":" + grails.port)
// always use the application context e.g. "/iris" with this URL
grails.cytomine.apps.iris.host = grails.serverURL + "/iris"

// ###########################
// Cytomine-IRIS sync settings
// ###########################
// this is an identifier that gets used in the remote configuration for
// project synchronization (project properties on Cytomine-Core)
grails.cytomine.apps.iris.sync.clientIdentifier = "IRIS_GRAZ_DEV"
grails.cytomine.apps.iris.sync.irisHost = grails.host

// ###############################################
// Background Job Configuration (Quartz Scheduler)
// ###############################################
// JobClassName.disabled = {true|false}
PingCytomineHostJob.disabled = false
SynchronizeUserProgressJob.disabled = false

// ##############################
// SMTP Mail Server Configuration
// ##############################
// see also https://grails.org/plugin/mail for configuration of
// GMAIL, YAHOO, etc. SMTP servers
grails.mail.default.from = "cytomine-iris@pkainz.com"
// if you're using a mail-relay without authentication, comment username and password
grails.mail.username = "cytomine-iris@pkainz.com"
grails.mail.password = "devIRIS2"
grails.mail.host = "smtp.world4you.com"
grails.mail.port = 587
grails.mail.props = [
        "mail.smtp.from"             : "cytomine-iris@pkainz.com",
        "mail.smtp.timeout"          : 15000,
        "mail.smtp.connectiontimeout": 15000
]


println "loaded development configuration."