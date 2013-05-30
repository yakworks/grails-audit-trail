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

the following show the options and defaults. For a field to be added by the annotation at least on config setting needs to be present.

	grails{
		plugin{
			audittrail{
				// ** if field is not specified then it will default to 'createdBy'
				createdBy.field = "createdBy"  // createdBy is default
				// ** fully qualified class name for the type
				createdBy.type   = "java.lang.Long" //Long is the default
				// ** the constraints settings
				createdBy.constraints = "nullable:false,display:false,editable:false"
				// ** the mapping you want setup
				createdBy.mapping = "column: 'inserted_by'" //<-example as there are NO defaults for mapping

				createdDate.field = "createdDate"
				createdDate.type  = "java.util.DateTime"
				createdDate.constraints = "nullable:false,display:false,editable:false"
				createdDate.mapping = "column: 'date_created'" //<-example as there are NO defaults for mapping

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

# changes from 1.2 -> 2.0 (many are breaking)

* defaults on the added fields are now to set nullable:true and not have default values
* changed the name space in config from stamp.audit to grails.plugin.audittrail
* major config overhall so you can set types,constraints etc for each audit field
* there is now an ability to set your own currrentUserClosure and the dependency on SpringSecurity is gone.
