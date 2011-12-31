package nine.tests

import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUser
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder as SCH

/*
This is a base test to be extended so the integration tests have access to a user and companyId
*/
class BaseInt extends GroovyTestCase {
	//def sessionFactory
	def authUser
	
	void setUp() {
		//def ucontroller = new UserController()
		//ServletContextHolder.setServletContext(ucontroller.request.getServletContext())
		super.setUp()
		def u = TestUser.findByUsername("joe")
		if(!u){
			genUser()
		}else{
			authUser = u
		}
		def user = TestUser.get(authUser.id)    // or create a new one if one doesn't exist
		assertNotNull user
		BaseInt.authenticate(user, AuthorityUtils.createAuthorityList('ROLE_GBill_Company'))
		
	}

	static void authenticate(user, authorities) {
		GrailsUser.metaClass.getCompanyId{ ->	// This is a test case.  It only fakes a value for tests.
			return 5L
		}
		def grailsUser = new GrailsUser(user.username, user.password, user.enabled,
			true, !user.passwordExpired, true, authorities, user.id)
		SCH.context.authentication = new UsernamePasswordAuthenticationToken(grailsUser, user.password, AuthorityUtils.createAuthorityList('ROLE_ADMIN'))
	}
	
	def genUser(){
		def person = new TestUser(username:"joe",enabled:true,password:"passwd",accountExpired:false,accountLocked:false,passwordExpired:false)
		if( !person.save(flush:true) ) {
			person.errors.each {
				println it
			}
		}
		//assert person.save(flush:true)
		authUser = person
	}
	
	// Implemented this for a comman way to do our flush() and clear() methods to get to a clean states
	def flushAndClear(){
		AH.application.mainContext.sessionFactory.currentSession.flush()
		AH.application.mainContext.sessionFactory.currentSession.clear()
	}
	def flush(){
		AH.application.mainContext.sessionFactory.currentSession.flush()
	}
	
}
