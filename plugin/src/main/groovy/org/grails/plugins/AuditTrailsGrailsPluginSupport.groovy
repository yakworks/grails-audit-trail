/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package org.grails.plugins

import gorm.AuditStampConfigLoader
import grails.plugin.audittrail.AuditStampEventListener

/**
 * Created by sudhir on 20/10/16.
 */
class AuditTrailsGrailsPluginSupport {

    static Closure doWithSpring = {

        //dont register beans if audit trail is disabled.
        if (grailsApplication.config.grails.plugin.audittrail.enabled == false) return
        Map fprops = gorm.FieldProps.buildFieldMap(new AuditStampConfigLoader().load())

        auditStampEventListener(AuditStampEventListener, ref('hibernateDatastore')) {
            grailsApplication = grailsApplication
            springSecurityService = ref("springSecurityService")
            fieldProps = fprops
        }

    }
}
