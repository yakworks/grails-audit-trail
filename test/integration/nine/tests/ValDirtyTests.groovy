package nine.tests

import grails.test.*
import groovy.sql.Sql
import java.sql.ResultSet
import org.apache.commons.lang.time.DateUtils

/**
 * Uses the doms domain to test the created by and edited by fields and CreateEditeStamp ASTrandformer
 *
**/
class ValDirtyTests extends BaseInt {
	def sessionFactory
	def dataSource
	def grailsApplication

	void setUp() {
		super.setUp();
	}
	void test_validate(){
		def vd = new ValDirty(name:'gg')
		//vd.name = "gg"
		println "before save"
		vd.save(failOnError:true)
		println "after save"
	}
	void test_dirty(){
		def vd = new ValDirty(name:'gg')
		vd.save(failOnError:true)
		def vd2 = ValDirty.findById(vd.id)
		vd2.name = "hh"
		assert vd2.isDirty()
	}

}
