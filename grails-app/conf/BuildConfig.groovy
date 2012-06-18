grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.work.dir = '.grails'
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsCentral()
        //mavenCentral()
        //mavenLocal()
        //mavenRepo "http://snapshots.repository.codehaus.org"

    }
    dependencies {
		if("$grailsVersion" < "2.0.0"){
			runtime('com.h2database:h2:1.2.147'){ export = false }
		}
    }

    plugins {
        build(":tomcat:$grailsVersion",":release:2.0.3") {
            export = false
        }
		compile (":hibernate:$grailsVersion"){
			export = false
		}
		
		compile (':spring-security-core:1.2.6'){
			export = false
		}
		
    }
}
