package nine.tests

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class ValDirty {

    String name

    @SuppressWarnings(['Println'])
    def beforeValidate(List blah) {
        println 'ValDirty beforeValidate'
    }
}
