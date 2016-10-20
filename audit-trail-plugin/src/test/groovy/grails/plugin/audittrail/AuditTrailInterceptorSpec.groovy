package grails.plugin.audittrail

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import nine.tests.TestDomain
import spock.lang.Specification

@TestMixin(GrailsUnitTestMixin)
class AuditTrailInterceptorSpec extends Specification {
	
	static final Long CURRENT_TIME = 100
	static final Long CURRENT_USER = 5

	AuditTrailInterceptor interceptor
	AuditTrailHelper auditTrailHelper

	def setup() {
		auditTrailHelper = AuditTrailHelper.mockForUnitTest(grailsApplication.config)
		auditTrailHelper.currentUserClosure = {ctx -> CURRENT_USER }
		
		interceptor = new AuditTrailInterceptor(fieldPropsMap:auditTrailHelper.fieldPropsMap, auditTrailHelper:auditTrailHelper)
		
		System.metaClass.'static'.currentTimeMillis = {CURRENT_TIME}
	}

	def testNoNullPointerExceptionForMapEntityOnSaveOrUpdate() {
		given:
		def entity = [prop1:"a", prop2:"b", prop3:"c"]

		when:
		boolean saveSuccess = interceptor.onSave(entity, null, null, null, null)
		boolean updateSuccess = interceptor.onFlushDirty(entity, null, null, null, null, null)

		then:
		saveSuccess
		updateSuccess
	}
	
	def testSetsAuditFieldsOnSave(){
		given:
		def entity = new TestDomain(name:"my domain")
		def state = new Object[4]
		def propertyNames = ["createdBy", "updatedBy", "createdDate", "editedDate"] as String[]

		when:
		boolean success = interceptor.onSave(entity, null, state, propertyNames, null)

		then:
		success
		state[0] == CURRENT_USER					//createdBy
		state[1] == CURRENT_USER					//updatedBy
		state[2] == new Date(CURRENT_TIME)		//createdDate
		state[3] == new Date(CURRENT_TIME)		//editedDate
	}
	
	def testSetsAuditFieldsOnUpdate(){
		given:
		def entity = new TestDomain(name:"my domain")
		def state = new Object[4]
		def propertyNames = ["createdBy", "updatedBy", "createdDate", "editedDate"] as String[]

		when:
		boolean success = interceptor.onFlushDirty(entity, null, state, null, propertyNames, null)

		then:
		success
		state[0] == null							//createdBy
		state[1] == CURRENT_USER					//updatedBy
		state[2] == null							//createdDate
		state[3] == new Date(CURRENT_TIME)		//editedDate
	}
	
}
