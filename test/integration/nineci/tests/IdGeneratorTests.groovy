package nineci.tests

import grails.test.*
import groovy.sql.Sql
import java.sql.ResultSet
import org.apache.commons.lang.time.DateUtils

/**
 * Uses the doms domain to test the created by and edited by fields and CreateEditeStamp ASTrandformer
 *
**/
class IdGeneratorTests extends BaseIntTest {
	def sessionFactory

	void setUp() {
		super.setUp();
	}
	

	void testIdGen() {
		def dom = new TestDomain(name:"blah")
		if( !dom.save(flush:true) ) {
			dom.errors.each {
				println it
			}
		}
		assertNotNull(dom.id);
		def sql = new Sql(sessionFactory.getCurrentSession().connection());
		def sqlCall = "select nextId from NewObjectId where KeyName = 'TestDomains.OID'"
		println sqlCall
		def data = sql.firstRow(sqlCall)
		assertNotNull(data)
		println "nextId is ${data.nextId}"
		assert data.nextId > 10000
	}

}
