
class AuditTrailGrailsPlugin {
    // the plugin version
    def version = "1.2.1"
    def grailsVersion = "1.3 > *"

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
		"web-app/**/*"
    ]

    def loadAfter = ['hibernate']

	def doWithSpring = {

		def cfg = application.config.grails.plugin.audittrail
		//eventTriggeringInterceptor(AuditStampInterceptor)
		entityInterceptor(nineci.hibernate.AuditTrailInterceptor){
			grailsApplication = ref("grailsApplication")
			createdByField = cfg.createdBy.field?:null
			editedByField = cfg.editedBy.field?:null
			editedDateField = cfg.editedDate.field?:null
			createdDateField = cfg.createdDate.field?:null
			companyIdField = cfg.companyId.field?:null
			currentUserClosure = cfg.currentUserClosure?:null
		} 
	}

    def doWithDynamicMethods = { ctx ->
        
    }

    def doWithApplicationContext = { applicationContext ->
        
    }

    def onChange = { event ->
        
    }

	def getFieldNames(application){
		def cfg = application.config.grails.plugin.audittrail
		//try old way
		if(!cfg){
			cfg = application.config.stamp.audit
		}
		return cfg.flatten()
	}

}
