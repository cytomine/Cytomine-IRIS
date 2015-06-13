
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
import org.apache.log4j.DailyRollingFileAppender

// locations to search for config files that get merged into the main config;
// config files can be ConfigSlurper scripts, Java properties files, or classes
// in the classpath in ConfigSlurper format

println "loading conf/Config.groovy..."

// externalized configuration
grails.config.locations = [
        // files located in src/java
//        "classpath:${appName}-config.properties",
//        "classpath:${appName}-"+grails.util.Environment.current.name+"-config.properties",
        "classpath:${appName}-config.groovy",
        "classpath:${appName}-"+grails.util.Environment.current.name+"-config.groovy",
//        "file:${userHome}/.grails/${appName}-config.properties",
//        "file:${userHome}/.grails/${appName}-"+grails.util.Environment.current.name+"-config.properties",
        "file:${userHome}/.grails/${appName}-config.groovy",
        "file:${userHome}/.grails/${appName}-"+grails.util.Environment.current.name+"-config.groovy"]

//if (System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
//}

grails.project.groupId = be.cytomine.apps.iris // change this to alter the default package name and Maven publishing destination

// The ACCEPT header will not be used for content negotiation for user agents containing the following strings (defaults to the 4 major rendering engines)
grails.mime.disable.accept.header.userAgents = ['Gecko', 'WebKit', 'Presto', 'Trident']
grails.mime.types = [ // the first one is the default format
                      all          : '*/*', // 'all' maps to '*' or the first available format in withFormat
                      atom         : 'application/atom+xml',
                      css          : 'text/css',
                      csv          : 'text/csv',
                      form         : 'application/x-www-form-urlencoded',
                      html         : ['text/html', 'application/xhtml+xml'],
                      js           : 'text/javascript',
                      json         : ['application/json', 'text/json'],
                      multipartForm: 'multipart/form-data',
                      rss          : 'application/rss+xml',
                      text         : 'text/plain',
                      hal          : ['application/hal+json', 'application/hal+xml'],
                      xml          : ['text/xml', 'application/xml']
]

// URL Mapping Cache Max Size, defaults to 5000
//grails.urlmapping.cache.maxsize = 1000

// What URL patterns should be processed by the resources plugin
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']

// Legacy setting for codec used to encode data with ${}
grails.views.default.codec = "html"

// The default scope for controllers. May be prototype, session or singleton.
// If unspecified, controllers are prototype scoped.
grails.controllers.defaultScope = 'singleton'

// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside ${}
                scriptlet = 'html' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        filteringCodecForContentType {
            //'text/html' = 'html'
        }
    }
}

// grails converter configuration
grails.converters.encoding = "UTF-8"
grails.converters.json.default.deep = false
//grails.converters.default.pretty.print = true

// scaffolding templates configuration
grails.scaffolding.templates.domainSuffix = 'Instance'

// Set to false to use the new Grails 1.2 JSONBuilder in the render method
grails.json.legacy.builder = false
// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true
// packages to include in Spring bean scanning
grails.spring.bean.packages = []
// whether to disable processing of multi part requests
grails.web.disable.multipart = false

// request parameters to mask when logging exceptions
grails.exceptionresolver.params.exclude = ['password']

// configure auto-caching of queries by default (if false you can cache individual queries with 'cache: true')
grails.hibernate.cache.queries = false


// ###############################
// the IRIS instance configuration
// ###############################
// default Cytomine settings
grails.cytomine = [
        image : [
                host : "http://image{serverID}.cytomine.be"
        ],
        host : "http://beta.cytomine.be",
        web : "http://www.cytomine.be",
        apps : [
                iris : [
                        server : [
                                admin : [
                                        name : "Admin Name",
                                        organization: "Admin Organization",
                                        email : "admin@organization.org"
                                ]
                        ],
                        // configure a demo project for this IRIS instance which will always be enabled to its users
                        // if none is specified, all projects will be disabled by default
                        demoProject : [
                                cmID : 151637920, // specify the Cytomine project ID in the external configuration
                        ],
                        sync : [:]
            ]
        ]
    ]

// default settings for the server and backend
grails.logging.jul.usebridge = true
grails.dbconsole.enabled = true
grails.dbconsole.urlRoot = '/admin/dbconsole'

grails.host = "localhost"
grails.port = "8080"
grails.protocol = "http"
grails.serverURL = grails.protocol + "://" + grails.host + ((grails.port=="")?"":":" + grails.port)
grails.cytomine.apps.iris.host = grails.serverURL + "/iris"

grails.cytomine.apps.iris.sync = [
        // the client identifier (used as properties key for domain object properties in Cytomine)
        clientIdentifier : "IRIS_CLIENT_ID",
        irisHost : grails.host
]

// Job configuration
// disable the jobs using the "disabled"=true flag
PingCytomineHostJob.disabled = false
SynchronizeUserProgressJob.disabled = false


environments {
    development {

    }
    production {

    }
}

// Default mail settings for notification services
grails.mail = [
        default : [
                from: ("cytomine-iris@"+grails.host)
        ],
        // mail relay settings
        host : "smtp.domain.com",
        port : 25,
        username : "user",
        password : "secret",
        props : [
                "mail.smtp.from":("cytomine-iris@"+grails.host),
                "mail.smtp.timeout": 15000,
                "mail.smtp.connectiontimeout": 15000
        ]
    ]


// send a mail using
//sendMail {
//	async true
//	to "rcpt@domain.com"
//	subject "the subject"
//	body 'the body'
//}


// LOG-CONFIGURATION IS DONE IN EXTERNAL CONFIG FILE
//def catalinaBase = System.properties.getProperty('catalina.base')
//if (!catalinaBase) catalinaBase = '.'
//def logDirectory = "${catalinaBase}/logs"
//
//// log4j configuration
//log4j = {
//    appenders {
//        console name: 'stdout', layout: pattern(conversionPattern: '%d [%t] %-5p %c{2} - %m%n')
//        appender new DailyRollingFileAppender(
//                name: 'dailyFileAppender',
//                datePattern: "'.'yyyy-MM-dd",  // See the API for all patterns.
//                fileName: "${logDirectory}/${appName}/${appName}.log",
//                layout: pattern(conversionPattern: '%d [%t] %-5p %c{2} %x - %m%n')
//        )
//
//        rollingFile name: "stacktrace", maxFileSize: 4096,
//                file: "${logDirectory}/${appName}/${appName}-stacktrace.log"
//    }
//
//    root {
//        info 'stdout', 'dailyFileAppender'
//    }
//
//    // common logging
//    error 'org.codehaus.groovy.grails.web.servlet',        // controllers
//            'org.codehaus.groovy.grails.web.pages',          // GSP
//            'org.codehaus.groovy.grails.web.sitemesh',       // layouts
//            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
//            'org.codehaus.groovy.grails.web.mapping',        // URL mapping
//            'org.codehaus.groovy.grails.commons',            // core / classloading
//            'org.codehaus.groovy.grails.plugins',            // plugins
//            'org.codehaus.groovy.grails.orm.hibernate',      // hibernate integration
//            'org.springframework',
//            'org.hibernate',
//            'net.sf.ehcache.hibernate'
//
////	trace 'org.hibernate.type.descriptor.sql.BasicBinder'
//
//    // cytomine java client
//    warn 'be.cytomine.client'
//
//    environments {
//        development {
//            debug 'grails.app.controllers',
//                    'grails.app.services',
//                    'be.cytomine.apps.iris'
////					'org.hibernate.SQL',
//            'grails.assets'
//
//            debug 'grails.app.jobs'
//        }
//        production {
//            // let the application run in debug log mode
//            debug 'grails.app.controllers',
//                    'grails.app.services',
//                    'be.cytomine.apps.iris',
//                    'grails.app.jobs'
//        }
//    }
//}


grails.gorm.default.constraints = {
    '*'(nullable: true)
}

grails.gorm.failOnError = true

// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.userLookup.userDomainClassName = 'be.cytomine.apps.iris.auth.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'be.cytomine.apps.iris.auth.UserRole'
grails.plugin.springsecurity.authority.className = 'be.cytomine.apps.iris.auth.Role'

// use interceptor map configuration
// BE AWARE OF RULE ORDERING -> MOST RESTRICTIVE COMES BEFORE LESS RESTRICTIVE PATTERN!
grails.plugin.springsecurity.securityConfigType = "InterceptUrlMap"
// controller/$action
// OR
// /url (lowercase!)
grails.plugin.springsecurity.interceptUrlMap = [
        '/api/**'           : ['permitAll'], // api call authentication are handled by REST security filter
        '/public/**'        : ['permitAll'], // let all public requests pass
        '/'                 : ['permitAll'],
        '/index'            : ['permitAll'],
//		'/index.gsp':         ['permitAll'], // cannot be found, no mapping defined
        '/iris'             : ['permitAll'], // REDIRECT command to the angular JS user front end, an UrlMapping rule must exist
        '/index.html'       : ['permitAll'], // the angular JS user front end, an UrlMapping rule must exist
        '/index.html/**'    : ['permitAll'], // the angular JS user front end, an UrlMapping rule must exist
        '/assets/'          : ['permitAll'],
        '/assets/*'         : ['permitAll'],
        '/assets/**'        : ['permitAll'],
        '/image/tile/**'    : ['permitAll'], // TILE URLs for openlayers view
        '/**/content/**'    : ['permitAll'],
        '/**/filters/**'    : ['permitAll'],
        '/**/js/**'         : ['permitAll'],
        '/**/controllers/**': ['permitAll'],
        '/**/services/**'   : ['permitAll'],
        '/**/lib/**'        : ['permitAll'],
        '/**/ngmodules/**'  : ['permitAll'],
        '/**/css/**'        : ['permitAll'],
        '/**/images/**'     : ['permitAll'],
        '/**/templates/**'  : ['permitAll'],
        '/**/videos/**'     : ['permitAll'],
        '/**/views/**'      : ['permitAll'],
        '/login/**'         : ['permitAll'],
        '/logout/**'        : ['permitAll'],
        '/admin/**'         : ['ROLE_IRIS_ADMIN', 'ROLE_IRIS_PROJECT_ADMIN', 'ROLE_IRIS_PROJECT_COORDINATOR', 'ROLE_IRIS_ADMIN'], // roles are OR chained
        '/**'               : ['IS_AUTHENTICATED_FULLY']
]

//
//// ANNOTATION SECURITY CONFIGURATION
//grails.plugin.springsecurity.securityConfigType = "Annotation" // this is the default
//// static controller mapping used in conjunction with annotations (may be overridden in the controller via annotations)
//// specify all resources that cannot be annotated in a controller, such as javascript, images, etc.
//// controllerAnnotations.staticRules entries are treated as if they were annotations on the corresponding controller
//// staticRules are also required for pessimistic lockdown
//grails.plugin.springsecurity.controllerAnnotations.staticRules = [
//		'/admin/**':		 			  ['permitAll'],
//]

//grails.assets.bundle = false
//grails.assets.minifyJs = false
//grails.assets.minifyCss = false
//        grails.assets.minifyOptions = [
//                languageMode: 'ES5',
//                targetLanguage: 'ES5', //Can go from ES6 to ES5 for those bleeding edgers
//                optimizationLevel: 'ADVANCED' //Or ADVANCED or WHITESPACE_ONLY
//        ]



// reloadable configuration
grails.plugins.reloadConfig.files = []
grails.plugins.reloadConfig.includeConfigLocations = true
grails.plugins.reloadConfig.interval = 5000
grails.plugins.reloadConfig.enabled = true
grails.plugins.reloadConfig.notifyPlugins = []
grails.plugins.reloadConfig.automerge = true
grails.plugins.reloadConfig.notifyWithConfig = true


println "loaded conf/Config.groovy."