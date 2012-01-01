package gorm;
import groovy.util.ConfigObject;
import java.util.Map;

class NewFieldProps {
	
	String name;
	Class  type;
	
	public static NewFieldProps init(String defaultName,String defaultType, Map co) {
		//System.out.println("ConfigObject : " + co);
		if(co == null || co.isEmpty()) return null;
		NewFieldProps newField = new NewFieldProps();
		newField.name = (String)co.get("grails.plugin.audittrail." + defaultName + ".field");
		if(newField.name == null){
			newField.name = defaultName;
		}
		String className = (String)co.get("grails.plugin.audittrail." + defaultName + ".type");
		if(className == null || className==""){
			className = defaultType;
		}
		try {
			newField.type = Class.forName(className);
		}catch (ClassNotFoundException e) {
			throw new RuntimeException("Class " + className + " could not be found for audittrail setting " + defaultName);
		}
		return newField;
    } 
}