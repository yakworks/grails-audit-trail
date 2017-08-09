package joda

import spock.lang.Specification
import spock.lang.Ignore

@Ignore
class AuditUnitSpec extends Specification {

    void testSomething() {
        def audit = new AuditThis(name:'billy boy')
		assert audit
    }
}
