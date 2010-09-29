package nineci.hibernate //grails.plugin.audittrail
import org.hibernate.EmptyInterceptor
import org.hibernate.type.Type
import org.springframework.security.context.SecurityContextHolder as SCH
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.ConfigurationHolder


class AuditTrailInterceptor extends EmptyInterceptor {
	private static final Logger log = Logger.getLogger(AuditTrailInterceptor)
	static final Properties CONF = ConfigurationHolder.config.toProperties()
	static final String CREATED_BY = CONF.getProperty("stamp.audit.createdBy")
	static final String EDITED_BY = CONF.getProperty("stamp.audit.editedBy")
	static final String EDITED_DATE = CONF.getProperty("stamp.audit.editedDate")
	static final String CREATED_DATE = CONF.getProperty("stamp.audit.createdDate")
	static final String COMPANY_ID = CONF.getProperty("stamp.audit.companyId","companyId")
	static Long ANONYMOUS_USER = 0

	boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,Object[] previousState, String[] propertyNames,Type[] types) {
		def metaClass = entity.metaClass
		MetaProperty property = metaClass.hasProperty(entity, EDITED_DATE)
		List fieldList = propertyNames.toList()
		
		if(property) {
			def now = property.getType().newInstance([System.currentTimeMillis()] as Object[] )
			setValue(currentState, fieldList, EDITED_DATE, now)
		}
		property = metaClass.hasProperty(entity,EDITED_BY)
		if(property) {
			setValue(currentState, fieldList, EDITED_BY, getUserID())
		}
		return true
	}

  boolean onSave(Object entity, Serializable id, Object[] state,String[] propertyNames, Type[] types) {
	 	def metaClass = entity.metaClass
		MetaProperty property = metaClass.hasProperty(entity, CREATED_DATE)
		def time = System.currentTimeMillis()
		List fieldList = propertyNames.toList()
		
		if(property) {
			def now = property.getType().newInstance([time] as Object[] )
			setValue(state, fieldList, CREATED_DATE, now)
		}
		property = metaClass.hasProperty(entity,EDITED_DATE)
		if(property) {
			def now = property.getType().newInstance([time] as Object[] )
			setValue(state, fieldList, EDITED_DATE, now)
		}
		property = metaClass.hasProperty(entity,EDITED_BY)
		if(property) {
			setValue(state, fieldList, EDITED_BY, getUserID())
		}
		property = metaClass.hasProperty(entity,CREATED_BY)
		if(property) {
			setValue(state, fieldList, CREATED_BY, getUserID())
		}
		property = metaClass.hasProperty(entity,COMPANY_ID)
		def authPrincipal = SCH?.context?.authentication?.principal
		if(property && authPrincipal && authPrincipal != "anonymousUser") {
			def curvalue = entity."$COMPANY_ID"
			if(curvalue==null || curvalue==0){
				//println "setting companyId to ${getCompanyId(authPrincipal)}"
				setValue(state, fieldList, COMPANY_ID, getCompanyId(authPrincipal))
			}
		}
    	return true
  }

  def setValue(Object[] currentState, List fieldList, String propertyToSet, Object value) {
    def index = fieldList.indexOf(propertyToSet)
    if (index >= 0) {
      currentState[index] = value
    }
  }

	Long getUserID() {
		def authPrincipal = SCH?.context?.authentication?.principal
		// Added check for error coming while creating new company
		if(authPrincipal && authPrincipal != "anonymousUser"){
			return authPrincipal.domainClass.id
		} else {
			return ANONYMOUS_USER
		}
	}
	
	Long getCompanyId(authPrincipal) {
		return authPrincipal.domainClass?.companyId
	}
}

