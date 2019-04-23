package org.grails.plugins

import grails.plugin.audittrail.AuditStampEventListener

/**
 * Created by sudhir on 20/10/16.
 */
class AuditTrailsGrailsPluginSupport {

    static Closure doWithSpring = {

        //dont register beans if audit trail is disabled.
        if (grailsApplication.config.grails.plugin.audittrail.enabled == false) return
        Map fprops = gorm.FieldProps.buildFieldMap(grailsApplication.config)

        auditStampEventListener(AuditStampEventListener, ref('hibernateDatastore')) {
            grailsApplication = grailsApplication
            springSecurityService = ref("springSecurityService")
            fieldProps = fprops
        }

    }
}
