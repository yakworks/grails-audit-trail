package nine.tests

import org.springframework.security.context.SecurityContextHolder as SCH
import org.springframework.security.providers.TestingAuthenticationToken
import org.springframework.security.GrantedAuthority
import org.springframework.security.Authentication
import org.springframework.security.GrantedAuthorityImpl
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.mock.web.MockHttpServletRequest
import nineci.greenbill.*
import org.grails.plugins.springsecurity.service.AuthenticateService
import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
import org.codehaus.groovy.grails.web.context.ServletContextHolder;
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
		def u = TestPerson.findByUsername("joe")
		if(!u){
			genUser()
		}else{
			authUser = u
		}
		def user = TestPerson.get(authUser.id)    // or create a new one if one doesn't exist
		assertNotNull user
		BaseIntTest.authenticate(user, [new GrantedAuthorityImpl('ROLE_GBill_Company')])
		
	}

	static void authenticate(user, authorities) {
		def principal = new Expando()
		principal.domainClass = user
		TestingAuthenticationToken authentication = new TestingAuthenticationToken(principal,null, authorities as GrantedAuthority[])
		authentication.authenticated = true
		SCH.context.authentication = authentication
		AuthenticateService.metaClass.userDomain = { -> return principal.domainClass }
		
	}
	
	def genUser(){
		def person = new TestPerson(username:"joe",userRealName:"joe",enabled:true,passwd:"passwd",email:"joe@joe.com")
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
