package nine.tests

@nineci.greenbill.Stamp
class TestDomain {

	String name
	
	Long companyId = 0
	
	static mapping={} //note you have to decalre this or AuditStamp won't add anything
	static constraints = {} //note you have to declare this or AuditStamp won't add anything
	
}
