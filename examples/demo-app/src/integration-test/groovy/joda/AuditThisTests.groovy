package joda

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.apache.commons.lang.time.DateUtils
import spock.lang.Specification

@Integration
@Rollback
class AuditThisTests extends Specification {

    void testSomething() {
        when:
        AuditThis audit = new AuditThis(name:'billy boy')

        then:
        assert audit.save(flush:true,failOnError:true)
        assert audit.createdDate
        assert DateUtils.isSameInstant(audit.createdDate, audit.editedDate)
        assert audit.createdBy == 0
    }
}
