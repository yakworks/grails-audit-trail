grails.servlet.version = "3.0" 
grails.project.work.dir = ".grails"
grails.project.target.level = 1.6
grails.project.source.level = 1.6

grails {
    project {
    dependency.resolver = "maven"
    dependency.resolution = {
    
    inherits("global") 
    log "error" 
    checksums true 

    repositories {
        inherits true 
        grailsPlugins()
        grailsHome()
        grailsCentral()
        mavenCentral()
    }

    dependencies {
		compile "org.jadira.usertype:usertype.jodatime:1.9"
    }

    plugins {
        compile (":view-tools:0.3-grails2"){ 
            export = false 
        }
        compile ":hibernate4:4.3.10"
        compile "org.grails.plugins:joda-time:1.5"
        compile "org.grails.plugins:audit-trail:2.1.2"
    }
}
}
}

//grails.plugin.location.'audit-trail' = "../../.."