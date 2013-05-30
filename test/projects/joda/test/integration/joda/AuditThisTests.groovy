package joda

import org.junit.Test

class AuditThisTests {

	@Test
	void testSomething() {
		def audit = new AuditThis(name:'billy boy')
		assert audit.save(flush:true,failOnError:true)
		assert audit.createdDate
		assert audit.createdDate == audit.editedDate
		assert audit.createdBy == "RON"
		assert audit.editedBy == "RON"
	}
}
