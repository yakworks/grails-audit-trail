package nine.tests

import grails.test.mixin.integration.Integration
import grails.transaction.Rollback

/**
 * Uses the doms domain to test the created by and edited by fields and CreateEditeStamp ASTrandformer
 */
@Integration
@Rollback
class ValDirtyTests extends BaseInt {
	def sessionFactory
	def grailsApplication

	void test_validate(){
		when:
		ValDirty vd = new ValDirty(name:'gg')
		//vd.name = "gg"
		println "before save"
		vd.save(failOnError:true)

		then:
		noExceptionThrown()

	}

	void test_dirty(){
		when:
		ValDirty vd = new ValDirty(name:'gg')
		vd.save(failOnError:true)

		then:
		noExceptionThrown()

		when:
		ValDirty vd2 = ValDirty.findById(vd.id)
		vd2.name = "hh"

		then:
		assert vd2.isDirty()
	}
}
