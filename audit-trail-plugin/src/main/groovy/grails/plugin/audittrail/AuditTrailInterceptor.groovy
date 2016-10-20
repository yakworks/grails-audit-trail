package grails.plugin.audittrail

import org.hibernate.EmptyInterceptor
import org.hibernate.type.Type
import org.apache.log4j.Logger
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationContext
import org.springframework.beans.factory.InitializingBean
import org.apache.commons.lang.ArrayUtils

class AuditTrailInterceptor extends EmptyInterceptor {
	private static final Logger log = Logger.getLogger(AuditTrailInterceptor)
	
	//injected
	AuditTrailHelper auditTrailHelper
	Map fieldPropsMap	

	boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,Object[] previousState, String[] propertyNames,Type[] types) {
        //exit fast if its off
        if(disableAuditTrailStamp(entity)) return true
        
		def dval = auditTrailHelper.setDateField(entity,'editedDate', System.currentTimeMillis())
		if(dval)
			setValue(currentState, propertyNames, fieldPropsMap.get('editedDate').name, dval)

		def uval = auditTrailHelper.setUserField(entity,'editedBy')
		if(uval)
		    setValue(currentState, propertyNames, fieldPropsMap.get('editedBy').name, uval)
		
		return true
	}

	boolean onSave(Object entity, Serializable id, Object[] currentState,String[] propertyNames, Type[] types) {
	    //exit fast if its off
        if(disableAuditTrailStamp(entity)) return true
        
	    def time = System.currentTimeMillis()
	    ['createdDate','editedDate'].each{ key->
			def valToSet = auditTrailHelper.setDateField(entity,key, time)
			if(valToSet)
			    setValue(currentState, propertyNames, fieldPropsMap.get(key).name, valToSet)
		}
		['createdBy','editedBy'].each{ key->
			def valToSet = auditTrailHelper.setUserField(entity,key)
			if(valToSet)
			    setValue(currentState, propertyNames, fieldPropsMap.get(key).name, valToSet)
		}

    	return true
  	}

	def setValue(Object[] currentState, String[] propertyNames, String propertyToSet, Object value) {
		int index = ArrayUtils.indexOf(propertyNames, propertyToSet)  //fieldList.indexOf(propertyToSet)
		if (index >= 0) {
			currentState[index] = value
		}
	}
	
	boolean disableAuditTrailStamp(entity){
	    (entity.hasProperty('disableAuditTrailStamp') && entity.disableAuditTrailStamp)?true:false
	}

}

