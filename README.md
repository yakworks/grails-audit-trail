![CircleCI](https://img.shields.io/circleci/project/github/9ci/grails-audit-trail.svg?longCache=true&style=for-the-badge)

# Overview

This plugin lets you add an annotation to your domain classes so the necessary created/updated audit fields will get added. On save() the domain will get "stamped" after a new insert or update. This eliminates the need for setting up a base class.
It will automatically add fields based on your settings in Config.groovy.
Provides an AST transformation annotation and hibernate events to take care of "stamping" for your gorm objects with the user who edited and/or created it as well as the edited and created dates.

# Goals

* DRY - setup config and then a single @gorm.AuditStamp on your domain will give the fields
* Eliminate the need for a base class to store audit fields
* Provide more the ability to configure the names of the date and user fields. It was a big break in our standard to use "dateCreated" and "lastUpdated" 
* Keep the nullable:false constraint on the audit fields with the ability to configure and override it if need be.
* work with Joda or with normal Date

# Using the @gorm.AuditStamp annotation

Add to your config.groovy each field you want added

grails{
	plugin{
		audittrail{	
			createdBy.field   = "createdBy" //add whatever names you want used for the 
			editedBy.field    = "editedBy" 
			createdDate.field = "createdDate"
			editedDate.field  = "editedDate" 
		}
	}
}

Add the annotation to your domain class

	@gorm.AuditStamp
	class Note{
		String note
	}

During compile time the AST transformation will add fields just as if you wrote your domain like so:

	class Note{
		String note
		
		Long createdBy 
		Long editedBy 
		Date editedDate
		Date createdDate 
		
		static constaints = {
			createdBy   nullable:false,display:false,editable:false
			editedBy    nullable:false,display:false,editable:false
			editedDate  nullable:false,display:false,editable:false
			createdDate nullable:false,display:false,editable:false
		}
		
		def beforeValidate() { //if this already existed then it just append the code
			//this sets the fields if this is a new (about to be inserted) instance 
			...applicationContext.getBean('auditTrailHelper').initializeFields(this)
		}
		
	}

## No annotation

The annotation is just an AST transformation as a convenience. You can add the fields manually to your domains that match whats you have configured in  config.grooy and the events will fire on those fields. This includes other hibernate/java entities.
It uses the AuditTrailInterceptor to stamp the fields on the hibernate objects if they exists.

# Events and the interceptor

As seen in the above example, this allows you to keep your fields set to "nullable:false" since this annotation will add/append code to the beforeValidate() to make sure the fields are initialized properly. It also setups 

## Security

The plugin defaults to using Spring Security but it is not dependent on it. If no currentUserClosure

# Configuration Options

The following show the options and defaults. For a field to be added by the annotation at least on config setting needs to be present.
NOTE: Remember to clean and re-compile after changing the config settings. All of the mods to the domain happen with and AST at compile time.

	grails{
		plugin{
			audittrail{	
				// ** if field is not specified then it will default to 'createdBy'
				createdBy.field = "createdBy"  // createdBy is default
				// ** fully qualified class name for the type	
				createdBy.type   = "java.lang.Long" //Long is the default
				// ** the constraints settings
				createdBy.constraints = "nullable:false,display:false,editable:false,bindable:false" 
				// ** the mapping you want setup
				createdBy.mapping = "column: 'inserted_by'" //<-example as there are NO defaults for mapping
				
				createdDate.field = "createdDate"
				createdDate.type  = "java.util.DateTime" 
				createdDate.constraints = "nullable:false,display:false,editable:false,bindable:false" 
				createdDate.mapping = "column: 'date_created'" //<-NOTE: example as there are NO defaults for mapping
				
				etc.....
				
				//custom closure to return the current user who is logged in
				currentUserClosure = {ctx->
					//ctx is the applicationContext
					//default is basically
					return springSecurityService.principal?.id
				}
				//there are NO defaults for companyId.
				companyId.field   = "companyId" //used for multi-tenant apps and is just the name of the field to use
			}

## Joda Time Example

this also shows how you can set your own currentUserClosure for stamping the user fields

	grails{
		plugin{
			audittrail{			
				createdBy.type   = "java.lang.String" 
			
				editedBy.type   = "java.lang.String" 
			
				createdDate.type  = "org.joda.time.DateTime" 
				createdDate.mapping = "type: org.jadira.usertype.dateandtime.joda.PersistentDateTime"
			
				editedDate.type  = "org.joda.time.DateTime" 
				editedDate.mapping = "type: org.jadira.usertype.dateandtime.joda.PersistentDateTime"
			
				currentUserClosure = {ctx->
					return ctx.mySecurityService.currentUserLogin()
				}
			}
		}
	}


# Unit Testing #

In Grails 2 the config is available in your unit tests so it makes setting things up a bit easier now.
grails.plugin.audittrail.AuditTrailHelper has a mockForUnitTest(config,userVal=1) to make unit testing easier.
pass userVal in as something else if you want some other default or some other type for your createdBy and editedBy.
Take a look at the source if you want to see what its doing.


    void testSave() {
		def d = new TestDomain()
		d.name = "test"
		//the AST from @gorm.AuditStamp adds a property "auditTrailHelper" to your domains
		//at run time it gets injected with the auditTrailHelper bean from the applicationContext
		d.auditTrailHelper = AuditTrailHelper.mockForUnitTest(config)
		d.save(failOnError:true)
		assert d.createdBy == 1 
    }
# changes from 1.2 -> 2.0 (many are breaking)

* defaults on the added fields are now to set nullable:true and not have default values
* changed the name space in config from stamp.audit to grails.plugin.audittrail
* major config overhall so you can set types,constraints etc for each audit field
* there is now an ability to set your own currrentUserClosure and the dependency on SpringSecurity is gone.

# changes in 2.0.4

* added mockForUnitTest in AuditTrailHelper to make unit testing easier
* a transient auditTrailHelper to get the injected bean for each domain that is marked with @gorm.auditStamp. beforeValidate then calls this if its not null. This will make it easier to test

# changes in 2.1 #

* added default constraint for fields of bindable:false
* you can turn off audit trail for an instance by setting domainInstance.disableAuditTrailStamp = true
* refactor common stamp functions to AuditTrailHelper


### Using AuditTrail in gradle multimodule projects
AuditTrail AST Transoformation reads audit trail related settings from application.groovy.
As long as the project with AuditStamp annotation is root gradle project, it works just fine.
However when the project is a module of a multimodule gradle project, A system property needs to be set to aid AST tranformation class find the correct application.groovy from module directory.

This can be achieved by setting the ```module.path``` in build.gradle of submodule as shown below.

```groovy
compileGroovy {
    groovyOptions.fork = true
    String path = projectDir.absolutePath
    groovyOptions.forkOptions.jvmArgs = ['-Dmodule.path=' + path]
}
```
