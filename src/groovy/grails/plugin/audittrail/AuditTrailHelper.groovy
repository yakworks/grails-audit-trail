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
		
		//if its not new then just exit as we will assume an updated entity is setup correctly
		if(!isNewEntity(entity)) return
		//exit fast if its off
        if(entity.hasProperty('disableAuditTrailStamp') && entity.disableAuditTrailStamp) return
        
		if(log.isDebugEnabled()) log.debug "initializeFields for new $entity"
		setFieldDefaults(entity)

	}
	
	void setFieldDefaults(Object entity){
	    def time = System.currentTimeMillis()
	    //assume its a new entity
		['createdDate','editedDate'].each{ key->
			setDateField(entity,key, time)
		}
		['createdBy','editedBy'].each{ key->
			setUserField(entity,key)
		}
	}
	
	def setDateField(entity,String fieldName, time = System.currentTimeMillis()){
	    def field = fieldPropsMap.get(fieldName).name
		def property = entity.hasProperty(field)
		def valToSet
		if(property) {
			valToSet =  property.getType().newInstance([time] as Object[] )
			entity.setProperty(field,valToSet)
		}
		return valToSet
	}
	
	def setUserField(entity, String fieldName ){
	    def field = fieldPropsMap.get(fieldName).name
		def property = entity.hasProperty(field)
		def valToSet
		if(property) {
		    valToSet = currentUserId()
			entity.setProperty(field,valToSet)
		}
		return valToSet
	}
	
	boolean isNewEntity(entity){
	    
		def session = applicationContext.sessionFactory.currentSession
		def entry = session.persistenceContext.getEntry(entity)
        return !entry
	}
	
	boolean isDisableAuditStamp(entity){
		def session = applicationContext.sessionFactory.currentSession
		def entry = session.persistenceContext.getEntry(entity)
        return !entry
	}
	
	def currentUserId() {
		return currentUserClosure(applicationContext)
	}
	
	def getSpringSecurityUser = { ctx ->
        def springSecurityService = ctx.springSecurityService
        if (springSecurityService.isLoggedIn()) {
            return springSecurityService.principal.id
        } else {
            //FIXME this is not ok.
            return 0 //fall back
        }
	}
	
	Boolean isUserAuthorized(){
		def springSecurityService = applicationContext.springSecurityService
		if (springSecurityService.isLoggedIn()) {
			return true
		} else {
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
	static mockForUnitTest(config, userVal = 1){
	    def testHelper = new AuditTrailHelper()
	    testHelper.fieldPropsMap = gorm.FieldProps.buildFieldMap(config)
		testHelper.currentUserClosure = {ctx -> userVal }
		testHelper.metaClass.initializeFields = {Object entity -> testHelper.setFieldDefaults(entity) }
		return testHelper
	}
}

