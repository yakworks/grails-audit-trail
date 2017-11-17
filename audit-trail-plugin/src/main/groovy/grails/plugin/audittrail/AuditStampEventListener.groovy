package grails.plugin.audittrail

import gorm.AuditStamp
import gorm.FieldProps
import grails.core.GrailsApplication
import grails.core.GrailsDomainClass
import grails.plugin.springsecurity.SpringSecurityService
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.engine.EntityAccess
import org.grails.datastore.mapping.engine.event.*
import org.grails.datastore.mapping.model.PersistentEntity
import org.springframework.context.ApplicationEvent

import javax.annotation.PostConstruct

@CompileStatic
class AuditStampEventListener extends AbstractPersistenceEventListener {
    private static final String DISABLE_AUDITSTAMP_FIELD = 'disableAuditTrailStamp'

    GrailsApplication grailsApplication
    SpringSecurityService springSecurityService

    final List<String> auditStampedEntities = []
    Map<String, FieldProps> fieldProps

    private Closure<Serializable> currentUserClosure

    protected AuditStampEventListener(Datastore datastore) {
        super(datastore)
    }

    @PostConstruct
    void init() {
        GrailsDomainClass[] domains = grailsApplication.getArtefacts(DomainClassArtefactHandler.TYPE) as GrailsDomainClass[]
        for (GrailsDomainClass domain : domains) {
            if (domain.clazz.getAnnotation(AuditStamp)) {
                auditStampedEntities << domain.clazz.name
            }
        }

        initCurrentUserClosure()
    }


    @Override
    protected void onPersistenceEvent(AbstractPersistenceEvent event) {
        EntityAccess ea = event.entityAccess
        PersistentEntity entity = event.entity
        def entityObject = event.entityObject

        if (entity == null || !auditStampedEntities.contains(entity.name) || isAuditStampDisabled(ea, entityObject)) return

        if (event.getEventType() == EventType.PreInsert) {
            beforeInsert(event.getEntity(), event.getEntityAccess())
        } else if (event.getEventType() == EventType.PreUpdate) {
            beforeUpdate(event.getEntity(), event.getEntityAccess())
        } else if (event.getEventType() == EventType.Validation) {
            beforeValidate(event.getEntity(), event.getEntityAccess())
        }

    }

    boolean isAuditStampDisabled(EntityAccess ea, def entity) {
        return entity[DISABLE_AUDITSTAMP_FIELD] == true
    }

    private void beforeInsert(PersistentEntity entity, EntityAccess ea) {
        setDateField(FieldProps.CREATED_DATE_KEY, ea)
        setUserField(FieldProps.CREATED_BY_KEY, ea)
    }

    private void beforeUpdate(PersistentEntity entity, EntityAccess ea) {
        setDateField(FieldProps.EDITED_DATE_KEY, ea)
        setUserField(FieldProps.EDITED_BY_KEY, ea)
    }

    private void beforeValidate(PersistentEntity entity, EntityAccess ea) {
        if (isNewEntity(ea)) {
            setDefaults(ea)
        }
    }

    void setDateField(String prop, EntityAccess ea, Date date = new Date()) {
        ea.setProperty(fieldProps[prop].name, date)
    }

    void setUserField(String prop, EntityAccess ea) {
        ea.setProperty(fieldProps[prop].name, currentUserId)
    }

    void setDefaults(EntityAccess ea) {
        Date now = new Date()

        setDateField(FieldProps.CREATED_DATE_KEY, ea, now)
        setDateField(FieldProps.EDITED_DATE_KEY, ea, now)

        setUserField(FieldProps.CREATED_BY_KEY, ea)
        setUserField(FieldProps.EDITED_BY_KEY, ea)
    }

    /**
     * Checks if the given domain instance is new
     *
     * it first checks for the createdDate property, if property exists and is not null, returns false, true if null
     *
     * @param entity
     * @return boolean
     */
    private boolean isNewEntity(EntityAccess ea) {
        String createdDateFieldName = fieldProps.get(FieldProps.CREATED_DATE_KEY).name
        def value = ea.getPropertyValue(createdDateFieldName)
        return value != null
    }

    Serializable getCurrentUserId() {
        return currentUserClosure(grailsApplication.mainContext)
    }

    @CompileDynamic
    void initCurrentUserClosure() {
        Closure configClosure = grailsApplication.config.getProperty(FieldProps.CONFIG_KEY + ".currentUserClosure", Closure)
        if (configClosure) currentUserClosure = configClosure

        else currentUserClosure = {
            if (springSecurityService.isLoggedIn()) {
                return springSecurityService.principal.id
            } else {
                return 0 //fall back
            }
        }
    }

    @Override
    boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return PreInsertEvent.class.isAssignableFrom(eventType) ||
            PreUpdateEvent.class.isAssignableFrom(eventType) ||
            ValidationEvent.class.isAssignableFrom(eventType)
    }
}
