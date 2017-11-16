package grails.plugin.audittrail

import gorm.AuditStamp
import gorm.FieldProps
import grails.core.GrailsApplication
import grails.core.GrailsDomainClass
import groovy.transform.CompileStatic
import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.datastore.mapping.core.Datastore
import org.grails.datastore.mapping.engine.EntityAccess
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEvent
import org.grails.datastore.mapping.engine.event.AbstractPersistenceEventListener
import org.grails.datastore.mapping.engine.event.EventType
import org.grails.datastore.mapping.engine.event.PreInsertEvent
import org.grails.datastore.mapping.engine.event.PreUpdateEvent
import org.grails.datastore.mapping.model.PersistentEntity
import org.springframework.context.ApplicationEvent

import javax.annotation.PostConstruct

@CompileStatic
class AuditStampEventListener extends AbstractPersistenceEventListener {
    GrailsApplication grailsApplication

    final List<String> auditStampedEntities = []
    Map<String, FieldProps> fieldProps

    protected AuditStampEventListener(Datastore datastore) {
        super(datastore)
    }

    @PostConstruct
    void init() {
        GrailsDomainClass[] domains = grailsApplication.getArtefacts(DomainClassArtefactHandler.TYPE) as GrailsDomainClass[]
        for(GrailsDomainClass domain : domains) {
            if(domain.clazz.getAnnotation(AuditStamp)) {
                auditStampedEntities << domain.clazz.name
            }
        }
    }

    @Override
    protected void onPersistenceEvent(AbstractPersistenceEvent event) {
        if (event.getEntity() == null || !auditStampedEntities.contains(event.entity.name)) return

        if (event.getEventType() == EventType.PreInsert) {
            beforeInsert(event.getEntity(), event.getEntityAccess());
        }
        else if (event.getEventType() == EventType.PreUpdate) {
            beforeUpdate(event.getEntity(), event.getEntityAccess());
        }

    }

    private boolean beforeInsert(PersistentEntity entity, EntityAccess ea) {
        setDateField(FieldProps.CREATED_DATE_KEY, ea)
        setUserField(FieldProps.CREATED_BY_KEY, ea)
    }

    private boolean beforeUpdate(PersistentEntity entity, EntityAccess ea) {
        setDateField(FieldProps.EDITED_DATE_KEY, ea)
        setUserField(FieldProps.EDITED_BY_KEY, ea)
    }

    void setDateField(String prop, EntityAccess ea) {
        ea.setProperty(fieldProps[prop].name, new Date())
    }

    void setUserField(String prop, EntityAccess ea) {
        ea.setProperty(fieldProps[prop].name, 1L)
    }

    @Override
    boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return PreInsertEvent.class.isAssignableFrom(eventType) || PreUpdateEvent.class.isAssignableFrom(eventType)
    }
}
