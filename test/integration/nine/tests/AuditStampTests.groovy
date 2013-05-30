package nine.tests

import groovy.sql.Sql

import org.apache.commons.lang.time.DateUtils

/**
 * Uses the doms domain to test the created by and edited by fields and CreateEditeStamp ASTrandformer
 */
class AuditStampTests extends BaseInt {
	def sessionFactory
	def dataSource
	def grailsApplication

	void test_constraints(){
		def art = grailsApplication.getDomainClass("nine.tests.TestDomain")
		assert art
		assert art.constraints.editedDate.getAppliedConstraint('nullable').isNullable()  == false
		assert art.constraints.createdDate.getAppliedConstraint('nullable').isNullable()  == false
		assert art.constraints.updatedBy.getAppliedConstraint('nullable').isNullable()  == true
		assert art.constraints.updatedBy.getAppliedConstraint('max').maxValue  == 90000l
		//def prop= art.getPropertyByName("updatedBy")
	}

	void testCreateEditInsert() {
		def dom = new TestDomain(name:"blah")
		dom.save(flush:true,failOnError:true)
		assertNotNull(dom.id)
		def sql = new Sql(dataSource)
		def sqlCall = 'select oid, company_id,created_by, created_date, whoUpdated, edited_date from TestDomains where oid = ' + dom.id
		println sqlCall
		//def data = hibSession.createSQLQuery(sqlCall).uniqueResult()
		def data = sql.firstRow(sqlCall)
		assertNotNull(data)
		assertEquals(dom.id, data.oid)
		assertEquals(5L, data.company_id)
		assertNotNull(data.created_date)
		assertNotNull(data.edited_date)
		assertTrue DateUtils.isSameDay(data.created_date, new Date())
		assertTrue DateUtils.isSameDay(data.edited_date, new Date())
		assertEquals(authUser.id, data.whoUpdated)
		assertEquals(authUser.id, data.created_by)
	}

	void testCreateEditInsert_with_companyId() {
		def dom = new TestDomain(name:"blah2",companyId:7)
		assert dom.save(flush:true,failOnError:true)

		def sql = new Sql(dataSource)
		def sqlCall = 'select oid, company_id,created_by, created_date, whoUpdated, edited_date from TestDomains where oid = ' + dom.id

		def data = sql.firstRow(sqlCall)
		assertNotNull(data)
		assertEquals(7L, data.company_id)
	}

	void testCreateEditUpdate() {
		def today = new Date()
		def yesterday = today - 1
		java.sql.Date yesterdaySQL = new java.sql.Date(yesterday.getTime())
		def sql = new Sql(sessionFactory.getCurrentSession().connection())

		sql.execute("insert into TestDomains (oid,version,company_id,name, created_by, created_date, whoUpdated, edited_date) "+
		  " values (?,?,?,?,?,?,?,?)", [2,0,5,"xxx", 0,yesterdaySQL,0,yesterdaySQL])

		def dom = TestDomain.get(2)
		assertNotNull(dom)
		dom.name="new name"
		dom.save(flush:true,failOnError:true)

		def sqlCall = 'select oid, created_by, created_date, whoUpdated, edited_date from TestDomains where oid = ' + dom.id
		println sqlCall
		def data = sql.firstRow(sqlCall)
		assertNotNull(data)
		assertEquals(dom.id, data.oid)
		assertNotNull(data.edited_date)
		assertTrue DateUtils.isSameDay(data.edited_date, new Date())
		assertEquals(authUser.id, data.whoUpdated)
	}
/*
	// Test for checking if ArDocDetail is dirty when we fetch arDocDetails from arDoc
	void testArDocDetailIsDirty(){
		def arDocDetail = ArDocDetail.get(100)
		assertNotNull(arDocDetail)
		assertFalse sessionFactory.getCurrentSession().isDirty()
		def arDoc = ArDoc.get(100)
		println "detail count is ${arDoc.details.size()}"
		if( !arDoc.save(flush:true) ) {
			arDoc.errors.each {
				println it
			}
		}
		assertFalse sessionFactory.getCurrentSession().isDirty()
	}*/
}
