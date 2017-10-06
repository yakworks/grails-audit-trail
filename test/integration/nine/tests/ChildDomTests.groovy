package nine.tests

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import org.codehaus.groovy.grails.commons.GrailsDomainClass

class ChildDomTests extends BaseInt {

	def grailsApplication

	void testConstaints() {
		DefaultGrailsDomainClass art = grailsApplication.getDomainClass("nine.tests.ChildDom")
		assert art
		assert art.constraints.childProp.getAppliedConstraint('nullable').isNullable() == false
		assert art.constraints.childProp.getAppliedConstraint('blank').isBlank() == false
		assert art.constraints.superProp.getAppliedConstraint('nullable').isNullable() == true
		assert art.constraints.updatedBy.getAppliedConstraint('nullable').isNullable() == true
		assert art.constraints.createdDate.getAppliedConstraint('nullable').isNullable() == false
		art.constraints.each{k,v->
			println "$k:$v"
		}
	}

	void testConstaintInhertence() {
		def cdom = new ChildDom(childProp:"test")
		assert cdom.save()
	}
}
