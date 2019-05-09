/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package nine.tests

import groovy.transform.CompileDynamic

import gorm.AuditStamp

@AuditStamp
@CompileDynamic
class TestDomain implements Serializable {

    String name

    static mapping = {
        table 'TestDomains'
    }

}
