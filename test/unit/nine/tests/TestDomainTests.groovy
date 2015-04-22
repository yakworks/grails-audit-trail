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
		assert d.createdBy == 1
		assert d.updatedBy == 1
    }
    
    void test_new_bindable_SanityCheck() {
		def d = new TestDomain()
		d.properties = [name:'test',createdBy:99]
		assert config.grails.plugin.audittrail
		d.auditTrailHelper = AuditTrailHelper.mockForUnitTest(config)
        
        assert d.createdBy == null
        
		d.save(failOnError:true)
		
    }
}
