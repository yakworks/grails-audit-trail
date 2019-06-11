package nine.tests

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
class TestDomainTests extends Specification implements DomainUnitTest<TestDomain> {

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


    void test_new_bindable_SanityCheck() {
        when:
        TestDomain d = new TestDomain()
        d.properties = [name:'test', createdBy:99]

        then:
        //assert config.grails.plugin.audittrail
        d.createdBy == null
    }
}
