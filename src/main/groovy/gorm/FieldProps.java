package gorm;

import grails.config.Config;

import java.util.HashMap;
import java.util.Map;

class FieldProps {
	private static final String DATE_CONS = "nullable:false, display:false, editable:false, bindable:false";
	private static final String USER_CONS = "nullable:false, display:false, editable:false, bindable:false";
	
	String name;
	Class  type;
	//Object initValue;
	String constraints;
	String mapping;
	
	public static FieldProps init(String defaultName, String defaultType, String defaultCons, String defaultMapping, Config configObj) {
		//System.out.println("ConfigObject : " + co);
		if(configObj == null || configObj.isEmpty()) return null;

		String baseKey = "grails.plugin.audittrail." + defaultName;

		if(getMap(configObj, baseKey) == null){
			return null;
		}

		FieldProps newField = new FieldProps();
		newField.name = (String)configObj.getProperty(baseKey + ".field", defaultName);

		String className = (String)configObj.getProperty(baseKey + ".type", defaultType);

		if(className == null || className=="") {
			className = defaultType;
		}

		try {
			newField.type = Class.forName(className);
		}catch (ClassNotFoundException e) {
			throw new RuntimeException("Class " + className + " could not be found for audittrail setting " + defaultName);
		}
		if(!configObj.containsKey(baseKey+ ".constraints") ){
		 	newField.constraints = defaultCons;
		}else{
		 	newField.constraints = (String)configObj.get(baseKey+ ".constraints");
		}
		if(configObj.containsKey(baseKey+ ".mapping") ){
		    newField.mapping = (String)configObj.get(baseKey + ".mapping");
		}
		
		return newField;
    } 

	public static Map<String, FieldProps> buildFieldMap(Config config){
		Map<String, FieldProps> map = new HashMap<String, FieldProps>();
		map.put("createdBy",FieldProps.init("createdBy","java.lang.Long", USER_CONS, null, config));
		map.put("editedBy",FieldProps.init("editedBy", "java.lang.Long", USER_CONS, null, config));

		map.put("editedDate",FieldProps.init("editedDate", "java.util.Date", DATE_CONS, null, config));
		map.put("createdDate",FieldProps.init("createdDate", "java.util.Date",DATE_CONS, null, config));
		return map;
	}
	
	static public Object getMap(Map configMap, String keypath) {
		String keys[] = keypath.split("\\.");
		Map map = configMap;
		for(String key : keys){
			Object val = map.get(key);
			if(val !=null){
				//System.out.println("got a key for are " +key);
				if(val instanceof Map){
					map = (Map)map.get(key);
				} else{
					return val;
				}
			}else{
				return null;
			}
		}
		return map;	
	}
}