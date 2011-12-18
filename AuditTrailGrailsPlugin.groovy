class AuditTrailGrailsPlugin {
    // the plugin version
    def version = "1.2.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3 > *"
    // the other plugins this plugin depends on
    def dependsOn = [hibernate:"1.3 > *"]
//'spring-security-core':"1.0.1 > *"]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp",
		'grails-app/views/login/*',
		'grails-app/domain/**',
		'grails-app/controllers/*',
		'grails-app/conf/*Config*'
    ]

    def title = "Audit Trail" // Headline display name of the plugin
    def author = "Joshua Burnett"
    def authorEmail = "joshua@greenbill.com"
    def description = 'provides an annotation and hibernate events to take care of audit trail stamping for your gorm objects'

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/audit-trail"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
    def developers = [ [ name: "Konstantinos Kostarellis", email: "kosta.grails@gmail.com" ]]

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "github", url: "https://github.com/9ci/grails-audit-trail/issues" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/9ci/grails-audit-trail" ]

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before
    }

    def doWithSpring = {
		entityInterceptor(nineci.hibernate.AuditTrailInterceptor) 
    }

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
