package nine.tests

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import groovy.transform.CompileDynamic

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
