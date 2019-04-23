package nine.tests

import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.stmt.BlockStatement
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
//@TestFor(TestDomain)
class AstTests extends Specification {

    void test_buildFromString() {
        given:
        String constraintsStr = "{->\n" +
                "	myVariable nullable: true, fuckit:false\n" +
                "}";
        BlockStatement newConstraints = (BlockStatement) new AstBuilder().buildFromString(constraintsStr).get(0)
        println newConstraints
        println newConstraints.getStatements()

        expect:
        newConstraints.getStatements().size() == 1
/*      for (Statement statement: newConstraints.getStatements()) {
            constraintsBlock.addStatement(statement)
        }*/
    }
}
