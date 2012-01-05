package joda

import static org.junit.Assert.*
import org.junit.*

class AuditThisTests {

	def grailsApplication
	
    @Before
    void setUp() {
        // Setup logic here
    }

    @After
    void tearDown() {
        // Tear down logic here
    }


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
