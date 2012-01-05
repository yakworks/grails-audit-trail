package nine.tests

import gorm.AuditStamp;

@AuditStamp
class TestDomain {

	String name

	Long companyId = 0

	static mapping={ 
		table 'TestDomains' 
	} 
	
	// def beforeValidate() {
	// 	println 'in validate'
	// } 
/*	static constraints = { ->
		createdDate nullable:true
	}*/ 

	
	
}
