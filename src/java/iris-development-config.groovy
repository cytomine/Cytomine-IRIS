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
grails.mail.default.from=("cytomine-iris@"+grails.host)
grails.mail.username = "os1133424"
grails.mail.password = "pwk\$2910@mug"
grails.mail.host = "smtp.medunigraz.at"
grails.mail.port = 25
grails.mail.props = [
        "mail.smtp.from":("cytomine-iris@"+grails.host),
        "mail.smtp.timeout": 15000,
        "mail.smtp.connectiontimeout": 15000
		]

println "loaded development config."