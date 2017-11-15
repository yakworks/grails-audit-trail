package org.grails.plugins

import grails.plugin.audittrail.AuditTrailHelper
import grails.plugin.audittrail.AuditTrailInterceptor

/**
 * Created by sudhir on 20/10/16.
 */
class AuditTrailsGrailsPluginSupport {

	static Closure doWithSpring = {

		//dont register beans if audit trail is disabled.
		if(grailsApplication.config.grails.plugin.audittrail.enabled == false) return
		def fprops = gorm.FieldProps.buildFieldMap(grailsApplication.config)

		auditTrailHelper(AuditTrailHelper) {
			grailsApplication = grailsApplication
			fieldPropsMap = fprops
		}

		interceptor(AuditTrailInterceptor) {
			auditTrailHelper = ref("auditTrailHelper")
			fieldPropsMap = fprops
		}

	}
}
