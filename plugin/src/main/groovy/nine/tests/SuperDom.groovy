/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package nine.tests

@gorm.AuditStamp
@SuppressWarnings('CompileStatic')
abstract class SuperDom {
    String superProp

    static constraints = {
        superProp nullable:true
    }
}
