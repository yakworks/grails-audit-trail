package nine.tests

class ChildDom extends SuperDom{
	String childProp
	
    static constraints = {
		childProp nullable:false, blank:false
    }

}
