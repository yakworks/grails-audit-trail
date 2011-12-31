package nine.tests

import gorm.AuditStamp;

@AuditStamp
class TestDomain {

	String name

	Long companyId = 0

	static mapping={ table 'TestDomains' } //note you have to declare this or AuditStamp won't add anything

	static constraints = { } //note you have to declare this or AuditStamp won't add anything
}
