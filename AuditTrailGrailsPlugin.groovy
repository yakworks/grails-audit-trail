import gorm.FieldProps
import grails.plugin.audittrail.AuditTrailHelper
import grails.plugin.audittrail.AuditTrailInterceptor

class AuditTrailGrailsPlugin {
	def version = "2.0.4"
	def grailsVersion = "2.0.0 > *"

	def author = "Joshua Burnett"
	def authorEmail = "joshua@greenbill.com"
	def title = "Grails Audit Trail Plugin"
	def description = 'Provides an annotation and Hibernate events to take care of audit trail stamping for your GORM objects'
	def license = "APACHE"
	def organization = [ name: "9ci", url: "http://www.9ci.com/" ]
	def developers = [ [ name: "Joshua Burnet", email: "joshua@greenbill.com" ]]
	def issueManagement = [ system: "github", url: "https://github.com/9ci/grails-audit-trail/issues" ]
	def scm = [ url: "https://github.com/9ci/grails-audit-trail" ]
	def documentation = "http://grails.org/audit-trail"

	def pluginExcludes = [
		"grails-app/views/**/*",
		'grails-app/domain/**',
		'grails-app/controllers/**',
		'grails-app/conf/*Config*',
		'src/groovy/nine/tests/**'
	]

	def loadAfter = ['hibernate','hibernate4']

	def doWithSpring = {

		def cfg = application.config.grails.plugin.audittrail
		def fprops = FieldProps.buildFieldMap(application.config)

		auditTrailHelper(AuditTrailHelper) {
			grailsApplication = ref("grailsApplication")
			fieldPropsMap = fprops
		}

		entityInterceptor(AuditTrailInterceptor) {
			auditTrailHelper = ref("auditTrailHelper")
			fieldPropsMap = fprops
		}
	}
}
