package nine.tests

import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.stmt.BlockStatement

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
//@TestFor(TestDomain)
class AstTests {

    void test_buildFromString() {
		String constraintsStr = "{->\n" +
		                                "	myVariable nullable: true, fuckit:false\n"+
		                                "}";
		BlockStatement newConstraints = (BlockStatement) new AstBuilder().buildFromString(constraintsStr).get(0)
		assert newConstraints.getStatements().size() == 1
		println newConstraints
		println newConstraints.getStatements()
/*		for (Statement statement: newConstraints.getStatements()) {
			constraintsBlock.addStatement(statement)
		}*/
    }
}
