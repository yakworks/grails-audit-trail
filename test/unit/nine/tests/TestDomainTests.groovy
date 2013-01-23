package nine.tests
import grails.plugin.audittrail.*
import grails.test.mixin.*

import org.junit.*
import gorm.*

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
//@TestFor(TestDomain)
class TestDomainTests {

    void testBasics() {
		def data = new TestDomain()
		assert TestDomain.constraints
		
		assert 0 == data.companyId

		['createdDate','editedDate','createdBy','updatedBy'].each{key->
			assert data.metaClass.hasProperty(data,key)
		}
		assert data.metaClass.hasProperty(data,"constraints")
    }
    
    void testSave() {
		def d = new TestDomain()
		d.name = "test"
		assert config.grails.plugin.audittrail
		d.auditTrailHelper = AuditTrailHelper.mockForUnitTest(config)

		d.save(failOnError:true)
		
    }
}
