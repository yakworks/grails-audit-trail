package nine.tests

import gorm.AuditStamp;

class ValDirty {

	String name
	//Date dt = new Date()

	def beforeValidate(List blah) {
		println 'ValDirty beforeValidate'
	} 
}
