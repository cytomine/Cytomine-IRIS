// THIS IS THE DEVELOPMENT EXTERNAL CYTOMINE IRIS CONFIGURATION FILE
// use ConfigSlurper Syntax to configure the settings
println "loading development config..."

grails.logging.jul.usebridge = true

grails.host = "localhost"
grails.port = "8080"
grails.protocol = "http"
grails.serverURL = grails.protocol + "://" + grails.host + ((grails.port=="")?"":":" + grails.port)
grails.cytomine.apps.iris.host = grails.serverURL + "/iris"

// set some synchronization settings
grails.cytomine.apps.iris.sync.clientIdentifier = "IRIS_GRAZ_DEV"
grails.cytomine.apps.iris.sync.irisHost = grails.host

// Job configuration
// disable the jobs using the "disabled"=true flag
PingCytomineHostJob.disabled = false
SynchronizeUserProgressJob.disabled = false

// MAIL SERVER CONFIGURATION
grails.mail.default.from="cytomine-iris@pkainz.com"
grails.mail.username = "cytomine-iris@pkainz.com"
grails.mail.password = "devIRIS2"
grails.mail.host = "smtp.world4you.com"
grails.mail.port = 587
grails.mail.props = [
        "mail.smtp.from":"cytomine-iris@pkainz.com",
        "mail.smtp.timeout": 15000,
        "mail.smtp.connectiontimeout": 15000
		]

println "loaded development config."