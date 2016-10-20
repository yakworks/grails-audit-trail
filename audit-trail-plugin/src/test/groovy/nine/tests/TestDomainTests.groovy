package nine.tests
import grails.plugin.audittrail.*
import grails.test.mixin.*
import grails.test.mixin.domain.DomainClassUnitTestMixin
import grails.test.mixin.hibernate.HibernateTestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.junit.*
import gorm.*
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */

@TestMixin(GrailsUnitTestMixin)
@Mock(TestDomain)
class TestDomainTests extends Specification {

    void testBasics() {
		when:
		TestDomain data = new TestDomain()

		then:
		assert data.constraints

		['createdDate','editedDate','createdBy','updatedBy'].each{key->
			assert data.metaClass.hasProperty(data,key)
		}
		assert data.metaClass.hasProperty(data,"constraints")
    }
    
    void testSave() {
		when:
		TestDomain d = new TestDomain()
		d.name = "test"

		then:
		assert config.grails.plugin.audittrail

		when:
		d.auditTrailHelper = AuditTrailHelper.mockForUnitTest(config)
		d.save(failOnError:true)

		then:
		d.createdBy == 1
		d.updatedBy == 1
    }
    
    void test_new_bindable_SanityCheck() {
		when:
		TestDomain d = new TestDomain()
		d.properties = [name:'test', createdBy:99]

		then:
		assert config.grails.plugin.audittrail

		when:
		d.auditTrailHelper = AuditTrailHelper.mockForUnitTest(config)
		d.save(failOnError:true)

		then:
        assert d.createdBy == 1

    }
}
