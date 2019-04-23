package nine.tests

import gorm.AuditStamp
import groovy.transform.CompileDynamic

@AuditStamp
@CompileDynamic
class TestDomain implements Serializable {

    String name

    static mapping = {
        table 'TestDomains'
    }

}
