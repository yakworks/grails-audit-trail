package nine.tests

import grails.test.*
import groovy.sql.Sql
import java.sql.ResultSet
import org.apache.commons.lang.time.DateUtils

/**
 * Uses the doms domain to test the created by and edited by fields and CreateEditeStamp ASTrandformer
 *
**/
class AuditStampTests extends BaseInt {
	def sessionFactory
	def dataSource

	void setUp() {
		super.setUp();
	}
	

	void testCreateEditInsert() {
		def dom = new TestDomain(name:"blah")
		if( !dom.save(flush:true) ) {
			dom.errors.each {
				println it
			}
		}
		assertNotNull(dom.id);
		def sql = new Sql(dataSource);
		def sqlCall = 'select oid, companyId,createdBy, createdDate, updatedBy, editedDate from TestDomains where oid = ' + dom.id
		println sqlCall
		//def data = hibSession.createSQLQuery(sqlCall).uniqueResult();
		def data = sql.firstRow(sqlCall)
		assertNotNull(data)
		assertEquals(dom.id, data.oid)
		assertEquals(5L, data.companyId)
		assertNotNull(data.createdDate)
		assertNotNull(data.editedDate)
		assertTrue DateUtils.isSameDay(data.createdDate, new Date())
		assertTrue DateUtils.isSameDay(data.editedDate, new Date())
		assertEquals(authUser.id, data.updatedBy)
		assertEquals(authUser.id, data.createdBy)
	}
	
	void testCreateEditInsert_with_companyId() {
		def dom = new TestDomain(name:"blah2",companyId:7)
		assert dom.save(flush:true)
		
		def sql = new Sql(dataSource);
		def sqlCall = 'select oid, companyId,createdBy, createdDate, updatedBy, editedDate from TestDomains where oid = ' + dom.id

		def data = sql.firstRow(sqlCall)
		assertNotNull(data)
		assertEquals(7L, data.companyId)
	}

	void testCreateEditUpdate() {
		def today = new Date()
		def yesterday = today - 1
		java.sql.Date yesterdaySQL = new java.sql.Date(yesterday.getTime())
		def sql = new Sql(sessionFactory.getCurrentSession().connection())
		
		sql.execute("insert into TestDomains (oid,version,companyId,name, createdBy, createdDate, updatedBy, editedDate) "+
		  " values (?,?,?,?,?,?,?,?)", [2,0,5,"xxx", 0,yesterdaySQL,0,yesterdaySQL])
		
		
		def dom = TestDomain.get(2)
		assertNotNull(dom);
		dom.name="new name"
		if( !dom.save(flush:true) ) {
			dom.errors.each {
				println it
			}
		}
		
		def sqlCall = 'select oid, createdBy, createdDate, updatedBy, editedDate from TestDomains where oid = ' + dom.id
		println sqlCall
		def data = sql.firstRow(sqlCall)
		assertNotNull(data)
		assertEquals(dom.id, data.oid)
		assertNotNull(data.editedDate)
		assertTrue DateUtils.isSameDay(data.editedDate, new Date())
		assertEquals(authUser.id, data.updatedBy)
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
