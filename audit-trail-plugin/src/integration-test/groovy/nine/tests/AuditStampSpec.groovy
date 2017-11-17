package nine.tests

import grails.core.GrailsApplication
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.userdetails.GrailsUser
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback
import groovy.sql.GroovyResultSet
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.apache.commons.lang.time.DateUtils
import org.grails.core.DefaultGrailsDomainClass
import org.grails.web.databinding.DefaultASTDatabindingHelper
import org.hibernate.SessionFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolder as SCH
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Issue

import javax.sql.DataSource

/**
 * Uses the doms domain to test the created by and edited by fields and CreateEditeStamp ASTrandformer
 *
 **/

@Integration
@Rollback
class AuditStampSpec extends Specification {
	SessionFactory sessionFactory
	DataSource dataSource
	GrailsApplication grailsApplication
	SpringSecurityService springSecurityService

	TestUser currentUser

	void setup() {
		currentUser = login()
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
		when:
		DefaultGrailsDomainClass art = grailsApplication.getDomainClass("nine.tests.TestDomain")

		then:
		assert art
		assert art.constrainedProperties.createdBy.getAppliedConstraint('nullable').isNullable()  == false
		assert art.constrainedProperties.editedDate.getAppliedConstraint('nullable').isNullable()  == false
		assert art.constrainedProperties.createdDate.getAppliedConstraint('nullable').isNullable()  == false
		assert art.constrainedProperties.updatedBy.getAppliedConstraint('nullable').isNullable()  == true
		assert art.constrainedProperties.updatedBy.getAppliedConstraint('max').maxValue  == 90000l

		when:
		List l = TestDomain."${DefaultASTDatabindingHelper.DEFAULT_DATABINDING_WHITELIST}"

		then:
		l.contains('name')
		!l.contains('createdBy')
		!l.contains('createdDate')
		!l.contains('editedDate')
		!l.contains('updatedBy')

		//def prop= art.getPropertyByName("updatedBy")
	}

	void testBindable(){
		when:
		TestDomain d = new TestDomain()
		d.properties = [name:'test',createdBy:99,updatedBy:999]

		then:
		assert d.createdBy == null
		assert d.updatedBy == null

		when:
		d.save(failOnError:true)//,validate:false)

		then:
		assert d.createdBy == springSecurityService.principal.id
		assert d.updatedBy == springSecurityService.principal.id
	}

	void testForAnynymouseUser(){
		when:
		TestDomain d = new TestDomain()
		d.properties = [name:'test']

		then:
		assert d.createdBy == null
		assert d.updatedBy == null

		when:
		SecurityContextHolder.clearContext()
		d.save(failOnError:true)//,validate:false)

		then:
		assert d.createdBy == 0
		assert d.updatedBy == 0
	}


	void testValidateFalse(){
		when:
		TestDomain d = new TestDomain()
		d.properties = [name:'test']

		then:
		assert d.createdBy == null
		assert d.updatedBy == null

		when:
		d.save(failOnError:true, validate:false, flush:true)

		then:
        Exception e = thrown()
		//assert d.createdBy == springSecurityService.principal.id
		assert d.updatedBy == null
	}

	void testCreateEditInsert() {
		when:
		TestDomain dom = new TestDomain(name:"blah")
		dom.save(flush:true,failOnError:true)

		then:
		assert dom.id != null

		when:
		Sql sql = new Sql(dataSource);
		def sqlCall = 'select oid, createdBy, createdDate, whoUpdated, editedDate from TestDomains where oid = ' + dom.id
		println sqlCall
		//def data = hibSession.createSQLQuery(sqlCall).uniqueResult();
		def data = sql.firstRow(sqlCall)

		then:
		assert data != null
		assert dom.id == data.oid
		assert data.createdDate != null
		assert data.editedDate != null
		assert DateUtils.isSameDay(data.createdDate, new Date())
		assert DateUtils.isSameDay(data.editedDate, new Date())

		assert currentUser.id == data.whoUpdated
		assert currentUser.id == data.createdBy
	}


	void testCreateEditUpdate() {
		given:
		Date today = new Date()
		Date yesterday = today - 1
		java.sql.Date yesterdaySQL = new java.sql.Date(yesterday.getTime())
		Sql sql = new Sql(sessionFactory.getCurrentSession().connection())

		sql.execute("insert into TestDomains (oid,version,name, createdBy, createdDate, whoUpdated, editedDate) "+
				" values (?,?,?,?,?,?,?)", [2,0,"xxx", 0, yesterdaySQL,0, yesterdaySQL])


		when:
		TestDomain dom = TestDomain.get(2)

		then:
		assert dom != null

		when:
		dom.name = "new name"
		dom.save(flush:true,failOnError:true)
		dom.refresh()

		String sqlCall = 'select oid, createdBy, createdDate, whoUpdated, editedDate, version from TestDomains where oid = ' + dom.id
		GroovyRowResult data = sql.firstRow(sqlCall)

		then:
		dom.version > 0
		assert data != null
		assert dom.id == data.oid
		assert data.editedDate != null
		assert DateUtils.isSameDay(data.editedDate, new Date())
		assert currentUser.id == data.whoUpdated
	}

	@Issue("https://github.com/9ci/grails-audit-trail/issues/41")
	void test_update_doesnot_change_createdDate_when_session_is_cleared() {
		Date today = new Date()
		Date yesterday = today - 1
		java.sql.Date yesterdaySQL = new java.sql.Date(yesterday.getTime())
		Sql sql = new Sql(sessionFactory.getCurrentSession().connection())

		sql.execute("insert into TestDomains (oid,version,name, createdBy, createdDate, whoUpdated, editedDate) "+
				" values (?,?,?,?,?,?,?)", [2,0,"xxx", 0, yesterdaySQL,0, yesterdaySQL])


		TestDomain dom = TestDomain.get(2)
		assert dom != null
		dom.name="new name"

		//clear session to test that createdDate does not get reset for detached objects.
		sessionFactory.currentSession.clear()
		DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP.get().clear()

		dom.save(flush:true,failOnError:true)

		String sqlCall = 'select oid, createdBy, createdDate, whoUpdated, editedDate from TestDomains where oid = ' + dom.id

		Map data = sql.firstRow(sqlCall)
		assert data != null
		assert dom.id == data.oid
		assert data.editedDate != null
		assert data.createdDate != null

		assert DateUtils.isSameDay(data.editedDate, new Date()), "edited Date should have been set to today"
		assert DateUtils.isSameDay(data.createdDate, yesterday), "Created date should have been changed"

	}


    @Ignore
	void test_disableAuditTrailStamp(){
		when:
		TestDomain d = new TestDomain()
		d.properties = [name:'test']
        TestDomain.disableAuditTrailStamp = true
		def theDate = new Date()-1
		d.createdBy = 99
		d.updatedBy = 99
		d.createdDate = theDate
		d.editedDate = theDate
		d.save(failOnError:true)

		then:
		assert d.createdBy == 99
		assert d.updatedBy == 99
		assert d.createdDate == theDate
		assert d.editedDate == theDate
	}

	void testSerializeDomain() {
		when:
		TestDomain d = new TestDomain(name: "test")

		then:
		assert d.name == "test"

		when:
		//serialize
		ByteArrayOutputStream bout = new ByteArrayOutputStream()
		ObjectOutputStream oout = new ObjectOutputStream(bout)
			oout.writeObject(d)
			oout.flush()
			oout.close()

		then:
		notThrown(NotSerializableException)

		when:
		//deserialize
		TestDomain deserialized
		ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray())
		bin.withObjectInputStream(getClass().getClassLoader()) { oin ->
			 deserialized = oin.readObject()
		}

		then:
		assert deserialized != null
		assert deserialized.name == "test"
	}


}
