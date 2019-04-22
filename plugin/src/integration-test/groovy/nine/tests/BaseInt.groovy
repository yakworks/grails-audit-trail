package nine.tests

import spock.lang.Specification

/*
This is a base test to be extended so the integration tests have access to a user and companyId
*/
class BaseInt extends Specification {

    def grailsApplication


    // Implemented this for a comman way to do our flush() and clear() methods to get to a clean states
    def flushAndClear(){
        grailsApplication.mainContext.sessionFactory.currentSession.flush()
        grailsApplication.mainContext.sessionFactory.currentSession.clear()
    }

    def flush(){
        grailsApplication.mainContext.sessionFactory.currentSession.flush()
    }
}
