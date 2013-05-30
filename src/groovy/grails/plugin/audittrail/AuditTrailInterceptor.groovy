package grails.plugin.audittrail

import org.hibernate.EmptyInterceptor
import org.hibernate.type.Type
import org.apache.log4j.Logger
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationContext
import org.springframework.beans.factory.InitializingBean
import org.apache.commons.lang.ArrayUtils
import org.apache.commons.lang.StringUtils

class AuditTrailInterceptor extends EmptyInterceptor {
	private static final Logger log = Logger.getLogger(AuditTrailInterceptor)
	private static final String ORDER_BY_TOKEN = "order by";
	
	//injected
	AuditTrailHelper auditTrailHelper
	Map fieldPropsMap	
	def dbDialect

	boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,Object[] previousState, String[] propertyNames,Type[] types) {

		String field = fieldPropsMap.get("editedDate").name
		MetaProperty property = entity.metaClass.hasProperty(entity, field)
		if(property) {
			def now = property.getType().newInstance([System.currentTimeMillis()] as Object[] )
			setValue(currentState, propertyNames, fieldPropsMap.get("editedDate").name, now)
		}
		field = fieldPropsMap.get("editedBy").name
		property = entity.metaClass.hasProperty(entity,field)
		if(property) {
			setValue(currentState, propertyNames, field, auditTrailHelper.currentUserId())
		}
		return true
	}

	boolean onSave(Object entity, Serializable id, Object[] state,String[] propertyNames, Type[] types) {
		def time = System.currentTimeMillis()
		
		['createdDate','editedDate','createdBy','editedBy'].each{ key->
			def field = fieldPropsMap.get(key).name
			def property = entity.metaClass.hasProperty(entity, field)
			if(property) {
				def valToSet
				if(key == 'createdDate' || key == 'editedDate'){
					valToSet =  property.getType().newInstance([time] as Object[] )
				}else{
					valToSet = auditTrailHelper.currentUserId()
				}
				setValue(state, propertyNames, field, valToSet)
			}
		}

		String companyIdField = auditTrailHelper.companyIdField
		if(companyIdField){
			def property = entity.metaClass.hasProperty(entity,companyIdField)
			if(property) {
				def curvalue = entity."$companyIdField"
				if(curvalue==null || curvalue==0 && auditTrailHelper.userGoodForCompanyId() ){ //only update if its 0 or null
					setValue(state, propertyNames, companyIdField, auditTrailHelper.getCompanyId())
				}
			}
		}
    	return true
  	}

	def setValue(Object[] currentState, String[] propertyNames, String propertyToSet, Object value) {
		int index = ArrayUtils.indexOf(propertyNames, propertyToSet)  //fieldList.indexOf(propertyToSet)
		if (index >= 0) {
			currentState[index] = value
		}
	}

	public String onPrepareStatement(String sql) {
		// run it only for Oracle. In Oracle GORM Criteria descending order shows the NULL values first when it should be last.
		if (!dbDialect.toLowerCase().contains("oracle")) 		return super.onPrepareStatement(sql);

		int orderByStart = sql.toLowerCase().indexOf(ORDER_BY_TOKEN);
		if (orderByStart == -1) {
			return super.onPrepareStatement(sql);
		}
		orderByStart += ORDER_BY_TOKEN.length() + 1;
		int orderByEnd = sql.indexOf(")", orderByStart);
		if (orderByEnd == -1) {
			orderByEnd = sql.indexOf(" UNION ", orderByStart);
			if (orderByEnd == -1) {
				orderByEnd = sql.length();
			}
		}
		String orderByContent = sql.substring(orderByStart, orderByEnd);
		String[] orderByNames = orderByContent.split("\\,");
		for (int i=0; i<orderByNames.length; i++) {
			if (orderByNames[i].trim().length() > 0) {
				if (orderByNames[i].trim().toLowerCase().endsWith("desc")) {
					orderByNames[i] += " NULLS LAST";
				} else {
					orderByNames[i] += " NULLS FIRST";
				}
			}
		}
		orderByContent = StringUtils.join(orderByNames, ",");
		sql = sql.substring(0, orderByStart) + orderByContent + sql.substring(orderByEnd); 
		return super.onPrepareStatement(sql);
	}



}

