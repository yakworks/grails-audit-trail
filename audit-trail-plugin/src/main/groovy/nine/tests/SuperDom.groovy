package nine.tests

@gorm.AuditStamp
abstract class SuperDom {
	String superProp

	static constraints = {
		superProp nullable:true
	}
}
