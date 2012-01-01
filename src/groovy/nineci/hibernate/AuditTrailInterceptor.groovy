package nineci.hibernate //grails.plugin.audittrail
import org.hibernate.EmptyInterceptor
import org.hibernate.type.Type
import org.apache.log4j.Logger
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationContext

class AuditTrailInterceptor extends EmptyInterceptor implements ApplicationContextAware{
	private static final Logger log = Logger.getLogger(AuditTrailInterceptor)
	//injected
	def grailsApplication
	def currentUserClosure
	String createdByField
	String editedByField
	String editedDateField
	String createdDateField
	String companyIdField

	ApplicationContext applicationContext

	static Long ANONYMOUS_USER = 0

	boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,Object[] previousState, String[] propertyNames,Type[] types) {
		def metaClass = entity.metaClass
		MetaProperty property = metaClass.hasProperty(entity, editedDateField)
		List fieldList = propertyNames.toList()
	
		if(property) {
			def now = property.getType().newInstance([System.currentTimeMillis()] as Object[] )
			setValue(currentState, fieldList, editedDateField, now)
		}
		property = metaClass.hasProperty(entity,editedByField)
		if(property) {
			setValue(currentState, fieldList, editedByField, getUserID())
		}
		return true
	}

	boolean onSave(Object entity, Serializable id, Object[] state,String[] propertyNames, Type[] types) {
	 	def metaClass = entity.metaClass
		MetaProperty property = metaClass.hasProperty(entity, createdDateField)
		def time = System.currentTimeMillis()
		List fieldList = propertyNames.toList()
		def userId = getUserID()
		
		if(property) {
			def now = property.getType().newInstance([time] as Object[] )
			setValue(state, fieldList, createdDateField, now)
		}
		property = metaClass.hasProperty(entity,editedDateField)
		if(property) {
			def now = property.getType().newInstance([time] as Object[] )
			setValue(state, fieldList, editedDateField, now)
		}
		property = metaClass.hasProperty(entity,editedByField)
		if(property) {
			setValue(state, fieldList, editedByField, userId)
		}
		property = metaClass.hasProperty(entity,createdByField)
		if(property) {
			setValue(state, fieldList, createdByField, userId)
		}
		property = metaClass.hasProperty(entity,companyIdField)
		
		if(property) {
			def curvalue = entity."$companyIdField"
			if(curvalue==null || curvalue==0 && userGoodForCompanyId() ){ //only update if its 0 or null
				setValue(state, fieldList, companyIdField, getCompanyId())
			}
		}
    	return true
  	}

	def setValue(Object[] currentState, List fieldList, String propertyToSet, Object value) {
		int index = fieldList.indexOf(propertyToSet)
		if (index >= 0) {
			currentState[index] = value
		}
	}

	def getUserID() {
		def userClos = currentUserClosure?:getSpringSecurityUser
		return userClos(applicationContext)
	}
	
	def getSpringSecurityUser = { ctx ->
		def authPrincipal = ctx.springSecurityService.principal
		// Added check for error coming while creating new company
		if(authPrincipal && authPrincipal != "anonymousUser"){
			return authPrincipal.id
		} else {
			return 0 //fall back
		}
	}
	
	def userGoodForCompanyId(){
		def authPrincipal = applicationContext.springSecurityService.principal
		if(authPrincipal && authPrincipal != "anonymousUser"){
			return true
		}else{
			return false
		}
	}
	
	Long getCompanyId() {
		def authPrincipal = applicationContext.springSecurityService.principal
		return authPrincipal.hasProperty(companyIdField)?authPrincipal.companyId:0
	}
}

