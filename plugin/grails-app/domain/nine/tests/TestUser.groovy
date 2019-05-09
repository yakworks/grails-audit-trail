/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package nine.tests

import groovy.transform.CompileDynamic

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders

@CompileDynamic
class TestUser {
    String username
    String password
    boolean enabled
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired

    static constraints = {
        username blank: false, unique: true
        password blank: false
    }

    static mapping = {
        password column: '`password`'
    }

    Set<TestRole> getAuthorities() {
        TestUserTestRole.findAllByTestUser(this)*.testRole as Set
    }

    def beforeInsert() {
        encodePassword()
    }

    def beforeUpdate() {
        if (isDirty('password')) {
            encodePassword()
        }
    }

    protected void encodePassword() {
        SpringSecurityService springSecurityService = Holders.grailsApplication.mainContext.getBean(SpringSecurityService)
        password = springSecurityService.encodePassword(password)
    }
}
