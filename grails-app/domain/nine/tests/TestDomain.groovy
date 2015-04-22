package nine.tests

import gorm.AuditStamp

@AuditStamp
class TestDomain {

	String name

	static mapping = {
		table 'TestDomains'
	}

	// def beforeValidate() {
	// 	println 'in validate'
	// }
/*	static constraints = { ->
		createdDate nullable:true
	}*/
}
