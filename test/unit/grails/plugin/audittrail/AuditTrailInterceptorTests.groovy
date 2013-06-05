package grails.plugin.audittrail

import static org.junit.Assert.*
import grails.test.mixin.*
import grails.test.mixin.support.*

import java.util.Formatter.DateTime

import nine.tests.TestDomain

import org.junit.*

@TestMixin(GrailsUnitTestMixin)
class AuditTrailInterceptorTests {
	
	static final Long CURRENT_TIME = 100
	static final Long CURRENT_USER = 5

	AuditTrailInterceptor interceptor
	AuditTrailHelper auditTrailHelper

	void setUp() {
		auditTrailHelper = AuditTrailHelper.mockForUnitTest(config)
		auditTrailHelper.currentUserClosure = {ctx -> CURRENT_USER }
		
		interceptor = new AuditTrailInterceptor(fieldPropsMap:auditTrailHelper.fieldPropsMap, auditTrailHelper:auditTrailHelper)
		
		System.metaClass.'static'.currentTimeMillis = {CURRENT_TIME}
	}

	void testNoNullPointerExceptionForMapEntityOnSaveOrUpdate() {
		def entity = [prop1:"a", prop2:"b", prop3:"c"]

		boolean saveSuccess = interceptor.onSave(entity, null, null, null, null)
		boolean updateSuccess = interceptor.onFlushDirty(entity, null, null, null, null, null)
		
		assert saveSuccess
		assert updateSuccess
	}
	
	void testSetsAuditFieldsOnSave(){
		def entity = new TestDomain(name:"my domain")
		def state = new Object[4]
		def propertyNames = ["createdBy", "updatedBy", "createdDate", "editedDate"] as String[]
		
		boolean success = interceptor.onSave(entity, null, state, propertyNames, null)
		
		assert success
		assert state[0] == CURRENT_USER					//createdBy
		assert state[1] == CURRENT_USER					//updatedBy
		assert state[2] == new Date(CURRENT_TIME)		//createdDate
		assert state[3] == new Date(CURRENT_TIME)		//editedDate
	}
	
	void testSetsAuditFieldsOnUpdate(){
		def entity = new TestDomain(name:"my domain")
		def state = new Object[4]
		def propertyNames = ["createdBy", "updatedBy", "createdDate", "editedDate"] as String[]
		
		boolean success = interceptor.onFlushDirty(entity, null, state, null, propertyNames, null)
		
		assert success
		assert state[0] == null							//createdBy
		assert state[1] == CURRENT_USER					//updatedBy
		assert state[2] == null							//createdDate
		assert state[3] == new Date(CURRENT_TIME)		//editedDate
	}
	
}
