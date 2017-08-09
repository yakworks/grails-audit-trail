
grails.plugin.springsecurity.userLookup.userDomainClassName = 'nine.tests.TestUser'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'nine.tests.TestUserTestRole'
grails.plugin.springsecurity.authority.className = 'nine.tests.TestRole'

grails{
	plugin{
		audittrail{
			createdBy.field  = "createdBy"
			createdBy.type   = "java.lang.Long" //fully qualified class name if not a java.lang.(String,Long,etc..)

			createdDate {
				field = "createdDate" //
				type  = "java.util.Date" //the class name type
			}
			//Will try a joda time on this one
			editedDate.field  = "editedDate"//date edited

			editedBy.field  = "updatedBy" //id who updated/edited
			editedBy.type   = "java.lang.Long" //fully qualified class name if not a java.lang.(String,Long,etc..)
			editedBy.constraints = "nullable:true, max:90000l,bindable:false"
			editedBy.mapping = "column: 'whoUpdated'"

		}
	}
}
