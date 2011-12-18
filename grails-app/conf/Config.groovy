// configuration for plugin testing - will not be included in the plugin zip

log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    //appenders {
    //    console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n')
    //}

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'

    warn   'org.mortbay.log'
}

stamp{
	audit{
		//the created and edited fields should be present or they won't get added during AST
		createdBy="createdBy" //id who created
		createdDate="createdDate" //
		editedBy="updatedBy" //id who updated/edited
		editedDate="editedDate"//date edited
		//the following are optional and are for mapping
		companyId="companyId" //used for multi-tenant, who this is for
	}
}

grails.gorm.default.mapping = {
	id column: 'OID', generator:'nineci.hibernate.NewObjectIdGenerator'
}

// Added by the Spring Security Core plugin:
grails.plugins.springsecurity.userLookup.userDomainClassName = 'nine.tests.TestUser'
grails.plugins.springsecurity.userLookup.authorityJoinClassName = 'nine.tests.TestUserTestRole'
grails.plugins.springsecurity.authority.className = 'nine.tests.TestRole'
