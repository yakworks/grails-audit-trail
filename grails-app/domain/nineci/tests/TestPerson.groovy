package nineci.tests

/**
 * User domain class. from acegi
 */
class TestPerson {
	static transients = ['pass']
	static hasMany = [authorities: TestAuthority]
	static belongsTo = TestAuthority

	/** Username */
	String username
	/** User Real Name*/
	String userRealName
	/** MD5 Password */
	String passwd
	/** enabled */
	boolean enabled

	String email
	boolean emailShow

	/** description */
	String description = ''

	/** plain password to create a MD5 password */
	String pass = '[secret]'
	
	Long companyId = 5

	static constraints = {
		username(blank: false, unique: true)
		userRealName(blank: false)
		passwd(blank: false)
		enabled()
	}
}
