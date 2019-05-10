/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package nine.tests

import groovy.transform.CompileDynamic

@CompileDynamic
class ChildDom extends SuperDom {
    String childProp

    static constraints = {
        childProp nullable: false, blank: false
    }
}
