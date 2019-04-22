package nine.tests

import groovy.transform.CompileDynamic

@CompileDynamic
class ChildDom extends SuperDom {
    String childProp

    static constraints = {
        childProp nullable: false, blank: false
    }
}
