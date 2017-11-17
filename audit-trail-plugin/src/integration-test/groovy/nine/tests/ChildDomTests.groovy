package nine.tests

import grails.artefact.DomainClass
import grails.core.GrailsDomainClass
import grails.test.mixin.integration.Integration
import grails.transaction.Rollback

@Integration
@Rollback
class ChildDomTests extends BaseInt {

	def grailsApplication

	void testConstaints() {
		when:
		GrailsDomainClass art = grailsApplication.getDomainClass("nine.tests.ChildDom")

		then:
		assert art
		assert art.constrainedProperties.childProp.getAppliedConstraint('nullable').isNullable() == false
		assert art.constrainedProperties.childProp.getAppliedConstraint('blank').isBlank() == false
		assert art.constrainedProperties.superProp.getAppliedConstraint('nullable').isNullable() == true
		assert art.constrainedProperties.updatedBy.getAppliedConstraint('nullable').isNullable() == true
		assert art.constrainedProperties.createdDate.getAppliedConstraint('nullable').isNullable() == false

		art.constrainedProperties.each{k,v->
			println "$k:$v"
		}
	}

	void testConstaintInhertence() {
		when:
		ChildDom cdom = new ChildDom(childProp:"test")

		then:
		cdom.save(flush:true, failOnError:true)
	}
}
