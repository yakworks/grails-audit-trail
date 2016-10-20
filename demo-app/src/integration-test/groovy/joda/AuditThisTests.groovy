package joda

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import org.junit.Test
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
