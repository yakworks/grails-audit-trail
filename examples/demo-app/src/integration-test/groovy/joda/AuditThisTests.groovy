package joda

import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
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
        assert audit.createdDate == audit.editedDate
        assert audit.createdBy == 0
    }
}
