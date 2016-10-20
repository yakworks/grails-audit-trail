package joda

import spock.lang.Specification
import spock.lang.Ignore

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */

@Ignore
class AuditUnitSpec extends Specification {

    void testSomething() {
        def audit = new AuditThis(name:'billy boy')
		assert audit
    }
}
