package nine.tests



import grails.test.mixin.*

import org.junit.*

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
//@TestFor(TestDomain)
class TestDomainTests {

    void testSomething() {
		def data = new TestDomain()
		assert TestDomain.constraints
		
		assert 0 == data.companyId

		['createdDate','editedDate','createdBy','updatedBy'].each{key->
			assert data.metaClass.hasProperty(data,key)
		}
		assert data.metaClass.hasProperty(data,"constraints")
    }
}
