package nine.tests

/**
 * Authority domain class. from acegi
 */
class TestAuthority {

	static hasMany = [people: TestPerson]

	/** description */
	String description
	/** ROLE String */
	String authority

	static constraints = {
		authority(blank: false, unique: true)
		description()
	}
}
