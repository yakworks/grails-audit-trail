package nine.tests

@gorm.AuditStamp
@SuppressWarnings('CompileStatic')
abstract class SuperDom {
    String superProp

    static constraints = {
        superProp nullable:true
    }
}
