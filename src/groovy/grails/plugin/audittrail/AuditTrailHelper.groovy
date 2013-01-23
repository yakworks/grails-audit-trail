package grails.plugin.audittrail 

import org.hibernate.EmptyInterceptor
import org.hibernate.type.Type
import org.apache.log4j.Logger
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationContext
import org.springframework.beans.factory.InitializingBean

class AuditTrailHelper implements ApplicationContextAware,InitializingBean{
	private static final Logger log = Logger.getLogger(AuditTrailInterceptor)
	def currentUserClosure
	//injected
	def grailsApplication
	Map fieldPropsMap 
	String companyIdField

	ApplicationContext applicationContext

	static Long ANONYMOUS_USER = 0

	void initializeFields(Object entity) {
		if(log.isDebugEnabled()) log.debug "in beforeValidateInit for $entity"
		//if its not new then just exit as we will assume an updated entity is setup correctly
		if(!isNewEntity(entity)) return
		
		setFieldDefaults(entity)
		
		if(companyIdField){
			def property = entity.metaClass.hasProperty(entity,companyIdField)
			if(property) {
				def curvalue = entity.getProperty(companyIdField)
				if(curvalue==null || curvalue==0 && isUserAuthorized() ){ //only update if its 0 or null
					entity.setProperty(companyIdField,getCompanyId())
				}
			}
		}
	}
	
	void setFieldDefaults(Object entity){
	    def time = System.currentTimeMillis()
	    //assume its a new entity
		['createdDate','editedDate','createdBy','editedBy'].each{ key->
			def field = fieldPropsMap.get(key).name
			def property = entity.hasProperty(field)
			if(property) {
				def valToSet
				if(key == 'createdDate' || key == 'editedDate'){
					valToSet =  property.getType().newInstance([time] as Object[] )
				}else{
					valToSet = currentUserId()
				}
				entity.setProperty(field,valToSet)
			}
		}
	}
	
	boolean isNewEntity(entity){
		def session = applicationContext.sessionFactory.currentSession
		def entry = session.persistenceContext.getEntry(entity)
        if (!entry) {
            return true
        }
	}
	
	def currentUserId() {
		return currentUserClosure(applicationContext)
	}
	
	def getSpringSecurityUser = { ctx ->
		def authPrincipal = ctx.springSecurityService.principal
		// Added check for error coming while creating new company
		if(authPrincipal && authPrincipal != "anonymousUser"){
			return authPrincipal.id
		} else {
			//FIXME this is not ok.
			return 0 //fall back
		}
	}
	
	Boolean isUserAuthorized(){
		def authPrincipal = applicationContext.springSecurityService.principal
		if(authPrincipal && authPrincipal != "anonymousUser"){
			return true
		}else{
			return false
		}
	}
	
	Long getCompanyId() {
		def authPrincipal = applicationContext.springSecurityService.principal
		if(authPrincipal.hasProperty(companyIdField)){
			return authPrincipal.companyId
		}else{
			//FIXME this should not return a 0 I don't think
			return 0
		}
	}
	
	//---------------------------------------------------------------------
	// Implementation of InitializingBean interface
	//---------------------------------------------------------------------

	public void afterPropertiesSet() throws Exception {

		def cfgClosure = grailsApplication.config.grails.plugin.audittrail.currentUserClosure
		if(cfgClosure){
			currentUserClosure = cfgClosure
		}else{
			currentUserClosure = getSpringSecurityUser
		}

	}
	
	/**
	 * mocks this out for a unit test
	 */
	static mockForUnitTest(config){
	    def testHelper = new AuditTrailHelper()
	    testHelper.fieldPropsMap = gorm.FieldProps.buildFieldMap(config)
		testHelper.currentUserClosure = {ctx -> 1 }
		testHelper.metaClass.initializeFields = {Object entity -> testHelper.setFieldDefaults(entity) }
		return testHelper
	}
}

