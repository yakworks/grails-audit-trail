package nine.tests

import gorm.AuditStamp

@AuditStamp
class TestDomain implements Serializable {

	String name

	static mapping = {
		table 'TestDomains'
	}

}
