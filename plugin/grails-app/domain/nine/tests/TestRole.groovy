package nine.tests

import groovy.transform.CompileDynamic

@CompileDynamic
class TestRole {

    String authority

    static mapping = {
        cache true
    }

    static constraints = {
        authority blank: false, unique: true
    }
}
