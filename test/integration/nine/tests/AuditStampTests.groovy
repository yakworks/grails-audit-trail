package nine.tests

import grails.plugin.springsecurity.userdetails.GrailsUser
import grails.test.mixin.TestMixin
import grails.test.mixin.integration.IntegrationTestMixin
import groovy.sql.Sql
import org.apache.commons.lang.time.DateUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.binding.DefaultASTDatabindingHelper
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolder as SCH

/**
 * Uses the doms domain to test the created by and edited by fields and CreateEditeStamp ASTrandformer
 *
 **/

@TestMixin(IntegrationTestMixin)
class AuditStampTests {
	def sessionFactory
	def dataSource
	GrailsApplication grailsApplication
	def springSecurityService


	void setUp() {
		login()
	}

	def login(){
		def user = TestUser.findByUsername("joe")
		if(!user){
			user = new TestUser(username:"joe",enabled:true,password:"passwd",accountExpired:false,accountLocked:false,passwordExpired:false)
			user.save(flush:true,failOnError:true)

			def grailsUser = new GrailsUser(user.username, user.password, user.enabled,
					true, !user.passwordExpired, true, AuthorityUtils.createAuthorityList('ROLE_ADMIN'), user.id)
			SCH.context.authentication = new UsernamePasswordAuthenticationToken(grailsUser, user.password, AuthorityUtils.createAuthorityList('ROLE_ADMIN'))
		}
		assert user
		return user
	}

	void test_constraints(){
		def art = grailsApplication.getDomainClass("nine.tests.TestDomain")
		assert art
		assert art.constraints.createdBy.getAppliedConstraint('nullable').isNullable()  == false
		assert art.constraints.editedDate.getAppliedConstraint('nullable').isNullable()  == false
		assert art.constraints.createdDate.getAppliedConstraint('nullable').isNullable()  == false
		assert art.constraints.updatedBy.getAppliedConstraint('nullable').isNullable()  == true
		assert art.constraints.updatedBy.getAppliedConstraint('max').maxValue  == 90000l

		def l = TestDomain."${DefaultASTDatabindingHelper.DEFAULT_DATABINDING_WHITELIST}"
		assert l == ['name']
		assert l.size() == 1

		//def prop= art.getPropertyByName("updatedBy") 
	}

	void testBindable(){
		def d = new TestDomain()
		d.properties = [name:'test',createdBy:99,updatedBy:999]

		assert d.createdBy == null
		assert d.updatedBy == null

		d.save(failOnError:true)//,validate:false)
		assert d.createdBy == springSecurityService.principal.id
		assert d.updatedBy == springSecurityService.principal.id
	}

	void testForAnynymouseUser(){
		def d = new TestDomain()
		d.properties = [name:'test']

		assert d.createdBy == null
		assert d.updatedBy == null

		SecurityContextHolder.clearContext()

		d.save(failOnError:true)//,validate:false)
		assert d.createdBy == 0
		assert d.updatedBy == 0
	}

	void testValidateFalse(){
		def d = new TestDomain()
		d.properties = [name:'test']

		assert d.createdBy == null
		assert d.updatedBy == null

		d.save(failOnError:true, validate:false)
		assert d.createdBy == springSecurityService.principal.id
		assert d.updatedBy == springSecurityService.principal.id
	}

	void testCreateEditInsert() {
		def dom = new TestDomain(name:"blah")
		dom.save(flush:true,failOnError:true)
		assert dom.id != null;
		def sql = new Sql(dataSource);
		def sqlCall = 'select oid, createdBy, createdDate, whoUpdated, editedDate from TestDomains where oid = ' + dom.id
		println sqlCall
		//def data = hibSession.createSQLQuery(sqlCall).uniqueResult();
		def data = sql.firstRow(sqlCall)
		assert data != null
		assert dom.id == data.oid
		assert data.createdDate != null
		assert data.editedDate != null
		assert DateUtils.isSameDay(data.createdDate, new Date())
		assert DateUtils.isSameDay(data.editedDate, new Date())
		def authUser = login()
		assert authUser.id == data.whoUpdated
		assert authUser.id == data.createdBy
	}


	void testCreateEditUpdate() {
		def today = new Date()
		def yesterday = today - 1
		java.sql.Date yesterdaySQL = new java.sql.Date(yesterday.getTime())
		def sql = new Sql(sessionFactory.getCurrentSession().connection())

		sql.execute("insert into TestDomains (oid,version,name, createdBy, createdDate, whoUpdated, editedDate) "+
				" values (?,?,?,?,?,?,?)", [2,0,"xxx", 0,yesterdaySQL,0,yesterdaySQL])


		def dom = TestDomain.get(2)
		assert dom != null
		dom.name="new name"
		dom.save(flush:true,failOnError:true)

		def sqlCall = 'select oid, createdBy, createdDate, whoUpdated, editedDate from TestDomains where oid = ' + dom.id
		println sqlCall
		def data = sql.firstRow(sqlCall)
		assert data != null
		assert dom.id == data.oid
		assert data.editedDate != null
		assert DateUtils.isSameDay(data.editedDate, new Date())
		def authUser = login()
		assert authUser.id == data.whoUpdated
	}

	void test_disableAuditTrailStamp_fail(){
		def d = new TestDomain()
		d.properties = [name:'test']

		assert d.createdBy == null
		assert d.updatedBy == null
		d.disableAuditTrailStamp = true
		try{
			d.save(failOnError:true)
		}catch(e){
			//should have failed
			assert e
			assert d.createdBy == null
			assert d.updatedBy == null
			assert d.createdDate == null
			assert d.editedDate == null
		}
	}

	void test_disableAuditTrailStamp(){
		def d = new TestDomain()
		d.properties = [name:'test']
		d.disableAuditTrailStamp = true
		def theDate = new Date()-1
		d.createdBy = 99
		d.updatedBy = 99
		d.createdDate = theDate
		d.editedDate = theDate
		d.save(failOnError:true)

		assert d.createdBy == 99
		assert d.updatedBy == 99
		assert d.createdDate == theDate
		assert d.editedDate == theDate
	}

	void testSerializeDomain() {
		TestDomain d = new TestDomain(name: "test")

		assert d.name == "test"
		assert d.auditTrailHelper != null //field should have been injected

		//serialize
		ByteArrayOutputStream bout = new ByteArrayOutputStream()
		ObjectOutputStream oout = new ObjectOutputStream(bout)

		try {
			oout.writeObject(d)
			oout.flush()
			oout.close()
		}catch (NotSerializableException e) {
			e.printStackTrace()
			fail("Domain should have been serialized successfully")
		}

		//deserialize
		TestDomain deserialized
		ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray())
		bin.withObjectInputStream(getClass().getClassLoader()) { oin ->
			 deserialized = oin.readObject()
		}

		assert deserialized != null
		assert deserialized.name == "test"
	}


}