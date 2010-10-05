package nine.tests

class TestUser {

	String username
	String password
	boolean enabled
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired

	static constraints = {
		username blank: false, unique: true
		password blank: false
	}

	static mapping = {
		password column: '`password`'
	}

	Set<TestRole> getAuthorities() {
		TestUserTestRole.findAllByTestUser(this).collect { it.testRole } as Set
	}
}
