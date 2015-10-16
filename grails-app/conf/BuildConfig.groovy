grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.work.dir = "target/work"
grails.project.target.level = 1.7
grails.project.source.level = 1.7
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.fork = [
    // configure settings for compilation JVM, note that if you alter the Groovy version forked compilation is required
    //  compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],

    // configure settings for the test-app JVM, uses the daemon by default
    test: [maxMemory: 2048, minMemory: 1024, debug: false, maxPerm: 512, daemon:true],
    // configure settings for the run-app JVM
    run: [maxMemory: 2048, minMemory: 1024, debug: false, maxPerm: 512, forkReserve:false],
    // configure settings for the run-war JVM
    war: [maxMemory: 2048, minMemory: 1024, debug: false, maxPerm: 512, forkReserve:false],
    // configure settings for the Console UI JVM
    console: [maxMemory: 2048, minMemory: 1024, debug: false, maxPerm: 512]
]

grails.project.dependency.resolver = "maven" // or ivy
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
    }
    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve
    legacyResolve false // whether to do a secondary resolve on plugin installation, not advised and here for backwards compatibility

    repositories {
        inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
        grailsHome()
        mavenLocal()
        grailsCentral()
        mavenCentral()
        // uncomment these (or add new ones) to enable remote dependency resolution from public Maven repositories
        // mavenRepo 'http://central.maven.org/maven2/'
        // mavenRepo "http://repository.codehaus.org"
        // mavenRepo "http://download.java.net/maven/2/"
        // mavenRepo "http://repository.jboss.com/maven2/"
        mavenRepo 'https://oss.sonatype.org/content/repositories/snapshots'
        mavenRepo 'http://repo.spring.io/milestone/'
        //mavenRepo "http://www.hibernatespatial.org/repository"
    }

    dependencies {
        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes e.g.
        // runtime 'mysql:mysql-connector-java:5.1.27'
        // runtime 'org.postgresql:postgresql:9.3-1100-jdbc41'
        compile 'com.vividsolutions:jts:1.13'
    }

//   GRAILS 2.3.5 plugins {
//        // plugins for the build system only
//        build ":tomcat:7.0.50"
//
//        // plugins for the compile step
//        compile ":scaffolding:2.0.1"
//        compile ":cache:1.1.1"
//		compile ":quartz:1.0.2"
//		compile ":mail:1.0.7"
//        compile ":executor:0.3"
////        compile ":spring-security-core:2.0-RC4"
//		//compile ":rest:0.8"
//
//        // plugins needed at runtime but not for compilation
//        runtime ":hibernate:3.6.10.7" // or ":hibernate4:4.1.11.6"
//        runtime ":database-migration:1.3.8"
//        //runtime ":jquery:1.10.2.2"
//
//        //runtime ":resources:1.2.1"
//        // Uncomment these (or add new ones) to enable additional resources capabilities
//        //runtime ":zipped-resources:1.0.1"
//       	//runtime ":cached-resources:1.1"
//        //runtime ":yui-minify-resources:0.1.5"
//    }


    // GRAILS 2.3.11
    plugins {
        // plugins for the build system only
        build ':tomcat:7.0.54'

        // plugins for the compile step
//        compile ":asset-pipeline:2.1.0"
        compile ':cache:1.1.7'
        compile ':scaffolding:2.0.1'

        compile ":quartz:1.0.2"
        compile ":executor:0.3"

        // file server
        compile ":file-server:0.2"

        // security
        compile ":spring-security-core:2.0-RC4"
        // mail
        compile ":mail:1.0.7"
        // configuration
        runtime ":external-config-reload:1.4.1"

//        compile ":jquery-ui:1.10.4"
//        compile ":jquery:1.11.1"
//        compile ":famfamfam:1.0.1"

//        compile ":spring-security-ui:1.0-RC2"


        // runtime ":cors:1.1.6"
//        compile ":spring-security-rest:1.4.0.RC5", {
//            excludes ('cors','spring-security-core')
//        }

        // plugins needed at runtime but not for compilation
        runtime ":database-migration:1.4.0"
        runtime ":hibernate:3.6.10.16"
    }
}
