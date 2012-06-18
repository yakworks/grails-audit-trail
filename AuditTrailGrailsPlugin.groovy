import gorm.*

class AuditTrailGrailsPlugin {
    // the plugin version
    def version = "2.0.1"
    def grailsVersion = "1.3.6 > *"

    def author = "Joshua Burnett"
    def authorEmail = "joshua@greenbill.com"
    def title = ""
    def description = 'provides an annotation and hibernate events to take care of audit trail stamping for your gorm objects'
	def license = "APACHE"
	def organization = [ name: "9ci", url: "http://www.9ci.com/" ]
	def developers = [ [ name: "Konstantinos Kostarellis", email: "kosta.grails@gmail.com" ]]
	def issueManagement = [ system: "github", url: "https://github.com/9ci/grails-audit-trail/issues" ]
	def scm = [ url: "https://github.com/9ci/grails-audit-trail" ]
    def documentation = "http://grails.org/AuditTrail+Plugin"

    def pluginExcludes = [
		"grails-app/views/**/*",
		'grails-app/domain/**',
		'grails-app/controllers/**',
		'grails-app/conf/*Config*',
		'src/groovy/nine/tests/**',
		"web-app/**/*"
    ]

    def loadAfter = ['hibernate']

	def doWithSpring = {

		def cfg = application.config.grails.plugin.audittrail
		def fprops = FieldProps.buildFieldMap(application.config)

		auditTrailHelper(nineci.hibernate.AuditTrailHelper){
			grailsApplication = ref("grailsApplication")
			fieldPropsMap = fprops
			companyIdField = cfg.companyId.field?:null
		}
		
		entityInterceptor(nineci.hibernate.AuditTrailInterceptor){
			auditTrailHelper = ref("auditTrailHelper")
			fieldPropsMap = fprops
		}
		
	}

    def doWithDynamicMethods = { ctx ->
        
    }

    def doWithApplicationContext = { applicationContext ->
        
    }

    def onChange = { event ->
        
    }


}
