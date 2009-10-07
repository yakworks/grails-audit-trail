package nineci.hibernate

import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainBinder
import org.codehaus.groovy.grails.orm.hibernate.cfg.Mapping
import org.codehaus.groovy.grails.orm.hibernate.support.ClosureEventTriggeringInterceptor
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.hibernate.event.SaveOrUpdateEvent
import org.hibernate.event.PreUpdateEventListener
import org.hibernate.event.PostUpdateEventListener
import org.hibernate.event.PostLoadEventListener
import org.hibernate.event.PostDeleteEventListener
import org.hibernate.event.PostInsertEventListener
import org.hibernate.event.PreLoadEventListener
import org.hibernate.event.PreDeleteEventListener
import org.hibernate.event.PreLoadEvent
import org.hibernate.event.PostInsertEvent
import org.hibernate.event.PostDeleteEvent
import org.hibernate.event.PreUpdateEvent
import org.hibernate.event.PreDeleteEvent
import org.hibernate.event.PostUpdateEvent
import org.hibernate.event.PostLoadEvent
import org.hibernate.event.PreInsertEvent
import org.hibernate.event.PreInsertEventListener
import org.codehaus.groovy.grails.orm.hibernate.events.SaveOrUpdateEventListener
import org.apache.commons.lang.ArrayUtils
import org.hibernate.event.AbstractEvent
import org.hibernate.persister.entity.EntityPersister
import org.hibernate.EntityMode
import org.hibernate.engine.EntityEntry
import org.hibernate.type.Type
import org.hibernate.Transaction
import org.hibernate.CallbackException
import org.springframework.security.context.SecurityContextHolder as SCH
import org.apache.log4j.Logger

/**
 * This was for the old way of doing it pre 1.2
 * left it here for reference
 */
class AuditStampInterceptor extends ClosureEventTriggeringInterceptor {
	//in 1.2 we can do this diferently
	// implements org.hibernate.Interceptor, Serializable {
	private static final Logger log = Logger.getLogger(AuditStampInterceptor)

	static final CREATED_BY = 'createdBy'
	static final EDITED_BY = 'editedBy'
	static final CREATED_DATE = 'createdDate'
	static final EDITED_DATE = 'editedDate'
	static final COMPANY_ID = 'companyId'
	static Long ANONYMOUS_USER = 0

	public void onSaveOrUpdate(SaveOrUpdateEvent event) {
		def entity = event.getObject()
		def authPrincipal = SCH?.context?.authentication?.principal
		println "onSaveOrUpdate called"
		if(entity) {
			boolean newEntity = !event.session.contains(entity)
			if(newEntity) {
				log.debug "onSaveOrUpdate ${entity.id} ${entity.class.name} is new and will be stamped"
				def metaClass = entity.metaClass
				MetaProperty property = metaClass.hasProperty(entity, CREATED_DATE)
				def time = System.currentTimeMillis()
				if(property) {
					def now = property.getType().newInstance([time] as Object[] )
					entity."$property.name" = now
				}
				property = metaClass.hasProperty(entity,EDITED_DATE)
				if(property) {
					def now = property.getType().newInstance([time] as Object[] )
					entity."$property.name" = now
				}
				property = metaClass.hasProperty(entity,EDITED_BY)
				if(property) {
					entity."$property.name" = getUserID()
				}
				property = metaClass.hasProperty(entity,CREATED_BY)
				if(property) {
					entity."$property.name" = getUserID()
				}
				property = metaClass.hasProperty(entity,COMPANY_ID)
				// FIX for GB-347 Added check for error coming while logging in to new company since if we don't check at this point,
				// then 0 value gets set for companyId of user object which gives error on dashboard
				if(property && authPrincipal && authPrincipal != "anonymousUser") {
					entity."$property.name" = getCompanyId(authPrincipal)
				}
			}
		}

		super.onSaveOrUpdate event
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


	public void onPreLoad(PreLoadEvent event) {
		super.onPreLoad(event)
	}

	public void onPostLoad(PostLoadEvent event) {
		super.onPostLoad(event)
	}

	public void onPostInsert(PostInsertEvent event) {
		super.onPostInsert(event)
	}

	public boolean onPreUpdate(PreUpdateEvent event) {
		println "onSaveOrUpdate called"
		def entity = event.getEntity()
		def result = super.triggerEvent(BEFORE_UPDATE_EVENT, event.entity, event)

		Mapping m = GrailsDomainBinder.getMapping(entity.getClass())
		boolean shouldTimestamp = m && !m.autoTimestamp ? false : true

		MetaProperty property = entity.metaClass.hasProperty(entity, EDITED_DATE)
		if(property) {
			//log.debug  "onPreUpdate ${entity.id} ${entity.class.name} hase been changed and will be stamped"
				def now = property.getType().newInstance([System.currentTimeMillis()] as Object[] )
				event.getState()[ArrayUtils.indexOf(event.persister.propertyNames, EDITED_DATE)] = now;
				entity."$EDITED_DATE" = now
			//log.debug "onPreUpdate ${entity.id} ${entity.class.name} has been stamped with ${entity.editedDate}"
		}
		property = entity.metaClass.hasProperty(entity, EDITED_BY)
		if(property) {
				event.getState()[ArrayUtils.indexOf(event.persister.propertyNames, EDITED_BY)] = getUserID();
				entity."$EDITED_BY" = getUserID()
			//log.debug  "onPreUpdate ${entity.id} ${entity.class.name} hase been stamped with editedBy user ${entity.editedBy}"
		}
		return result
		//return super.onPreUpdate(event)
	}

	public void onPostUpdate(PostUpdateEvent event) {
		super.onPostUpdate(event)
	}

	public void onPostDelete(PostDeleteEvent event) {
		super.onPostDelete(event)
	}

	public boolean onPreDelete(PreDeleteEvent event) {
		return super.onPreDelete(event)
	}


//*******implements the hibernate interceptor interface. wont work until 1.2 and then we can register it with
//beans = { entityInterceptor(AuditStampInterceptor) }

	/*public boolean onFlushDirty( Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
		//setValue(currentState, propertyNames, "updatedBy", UserUtils.getCurrentUsername())
		//println "onFlushDirty "
		//setValue(currentState, propertyNames, EDITED_DATE, new Date())
		//return true;
		false
	}

	private void setValue(Object[] currentState, String[] propertyNames,String propertyToSet, Object value) {
		def index = propertyNames.toList().indexOf(propertyToSet)
		if (index >= 0) {
			//println "setValue for $propertyToSet "
			currentState[index] = value
		}
	}


	public void onDelete(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {}
	public boolean onLoad( Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) { return false; }
	public boolean onSave( Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) { return false; }
	public void postFlush(Iterator entities) {}
	public void preFlush(Iterator entities) {}
	public Boolean isTransient(Object entity) { return null; }
	public Object instantiate(String entityName, EntityMode entityMode, Serializable id) { return null; }
	public int[] findDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
		return null;
	}
	public String getEntityName(Object object) { return null; }
	public Object getEntity(String entityName, Serializable id) { return null; }
	public void afterTransactionBegin(Transaction tx) {}
	public void afterTransactionCompletion(Transaction tx) {}
	public void beforeTransactionCompletion(Transaction tx) {}
	public String onPrepareStatement(String sql) { return sql; }
	public void onCollectionRemove(Object collection, Serializable key) throws CallbackException {}
	public void onCollectionRecreate(Object collection, Serializable key) throws CallbackException {}
	public void onCollectionUpdate(Object collection, Serializable key) throws CallbackException {}
*/

}