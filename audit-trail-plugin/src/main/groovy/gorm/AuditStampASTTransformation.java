package gorm;

import groovy.util.ConfigObject;
import groovy.util.ConfigSlurper;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.builder.AstBuilder;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import static org.codehaus.groovy.ast.MethodNode.ACC_PUBLIC;
import static org.codehaus.groovy.ast.MethodNode.ACC_STATIC;

/**
 * Performs an ast transformation on a class - adds createdBy/createdDate editedBy/EditedDate
 * properties to the subject class.
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class AuditStampASTTransformation implements ASTTransformation {
	private static final ConfigObject CO = new ConfigSlurper().parse(getContents(new File("grails-app/conf/application.groovy")));

	//PropertySourcesConfig config = new PropertySourcesConfig(CO);

	private static final VariableExpression EXPRESSION_THIS = new VariableExpression("this");
	private static final Token OPERATOR_ASSIGNMENT = new Token(Types.EQUAL,"=", -1,-1);

	public void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
		Map<String, FieldProps> fprops = FieldProps.buildFieldMap(CO);

		for (ASTNode astNode : astNodes) {
			if (astNode instanceof ClassNode) {
				ClassNode classNode = (ClassNode) astNode;
				doBeforeValidate(classNode);
				//debugFieldNodes(classNode);

				createUserField( classNode, fprops.get("editedBy"));
				createUserField( classNode, fprops.get("createdBy"));

				createDateField( classNode, fprops.get("editedDate"));
				createDateField( classNode, fprops.get("createdDate"));

			}
		}
	}
	
	public void doBeforeValidate(ClassNode classNode){

		addAuditHelperField(classNode);
		//classNode.addProperty("auditTrailHelper", Modifier.PUBLIC | Modifier.TRANSIENT, new ClassNode(java.lang.Object.class), null, null, null);
		classNode.addProperty("disableAuditTrailStamp", Modifier.PUBLIC | Modifier.TRANSIENT, new ClassNode(java.lang.Object.class), ConstantExpression.FALSE, null, null);

		MethodNode mn = classNode.getMethod("beforeValidate", Parameter.EMPTY_ARRAY);

		if(mn == null){
			classNode.addMethod("beforeValidate", Modifier.PUBLIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY, null, new BlockStatement());
			mn = classNode.getMethod("beforeValidate", Parameter.EMPTY_ARRAY);
			assert mn != null;
		}

		//call auditTrailHelper.initializeFields as last statement of beforeValidate
		String configStr = "auditTrailHelper?.initializeFields(this) ";
		BlockStatement newConfig = (BlockStatement) new AstBuilder().buildFromString(configStr).get(0);
		BlockStatement block = (BlockStatement) mn.getCode();
		block.addStatement(newConfig.getStatements().get(0));
	}

	//We can not make auditTrailHelper a property, because it will fail if domain is serialized (auditTrailHelper is not serializable, and can not be made).
	//So we add a private auditTrailHelper field with a setter but no getter, it will prevent it from serializing, and dependency injection for auditTrailHelper will still work.
	//We need a setter, or else grails will not inject auditTrailHelper, even if field is public.
	private void addAuditHelperField(ClassNode classNode) {

		//add private field.
		FieldNode auditTrailHelperField = new FieldNode("auditTrailHelper", Modifier.PRIVATE | Modifier.TRANSIENT, new ClassNode(Object.class), classNode, null);
		classNode.addField(auditTrailHelperField);

		//add setter
		Parameter parameter = new Parameter(new ClassNode(Object.class), "auditTrailHelper");
		Parameter[] params = {parameter};


		//setter body - (this.auditTrailHelper = auditTrailHelper)
		BlockStatement methodBody = new BlockStatement();
		methodBody.addStatement(
				new ExpressionStatement(
						new BinaryExpression(
								new PropertyExpression(EXPRESSION_THIS, "auditTrailHelper"),
								OPERATOR_ASSIGNMENT,
								new VariableExpression(parameter)
						)
				)
		);

		MethodNode methodNode = new MethodNode("setAuditTrailHelper", Modifier.PUBLIC, ClassHelper.VOID_TYPE, params, null, methodBody);
		classNode.addMethod(methodNode);
	}
	
	public void createUserField(ClassNode classNode,FieldProps fieldProps){
		if(fieldProps==null) return;
		classNode.addProperty(fieldProps.name, Modifier.PUBLIC, new ClassNode(fieldProps.type), null, null, null);
		addSettings("mapping",classNode,fieldProps.name,fieldProps.mapping);
		addSettings("constraints",classNode,fieldProps.name,fieldProps.constraints);
	}
	
	public void createDateField(ClassNode classNode,FieldProps fieldProps){
		if(fieldProps==null) return;
		classNode.addProperty(fieldProps.name, Modifier.PUBLIC, new ClassNode(fieldProps.type), null, null, null);
		addSettings("mapping",classNode,fieldProps.name,fieldProps.mapping);
		addSettings("constraints",classNode,fieldProps.name,fieldProps.constraints);
	}

	public void addSettings(String name,ClassNode classNode,String fieldName,String config){
		if(config==null) return;
		
		String configStr = fieldName + " " + config;
		
		BlockStatement newConfig = (BlockStatement) new AstBuilder().buildFromString(configStr).get(0); 

		FieldNode closure = classNode.getField(name);
		if(closure == null){
			createStaticClosure(classNode, name);
			closure = classNode.getField(name);
			assert closure != null;
		} 
		
		if(!hasFieldInClosure(closure,fieldName)){			
			ReturnStatement returnStatement = (ReturnStatement) newConfig.getStatements().get(0);
			ExpressionStatement exStatment = new ExpressionStatement(returnStatement.getExpression());
			ClosureExpression exp = (ClosureExpression)closure.getInitialExpression();
			BlockStatement block = (BlockStatement) exp.getCode();
			block.addStatement(exStatment);
		}

		assert hasFieldInClosure(closure,fieldName) == true;
	}

	public void createStaticClosure(ClassNode classNode,String name){
		FieldNode field = new FieldNode(name, ACC_PUBLIC | ACC_STATIC, new ClassNode(java.lang.Object.class), new ClassNode(classNode.getClass()),null);
		ClosureExpression expr = new ClosureExpression(Parameter.EMPTY_ARRAY, new BlockStatement());
		expr.setVariableScope(new VariableScope());
		field.setInitialValueExpression(expr);
		classNode.addField(field);
	}

	public boolean hasFieldInClosure(FieldNode closure, String fieldName){
		if(closure != null){
			ClosureExpression exp = (ClosureExpression) closure.getInitialExpression();
			BlockStatement block = (BlockStatement) exp.getCode();
			List<Statement> ments = block.getStatements();
			for(Statement expstat : ments){
				if(expstat instanceof ExpressionStatement && ((ExpressionStatement)expstat).getExpression() instanceof MethodCallExpression){
					MethodCallExpression methexp = (MethodCallExpression)((ExpressionStatement)expstat).getExpression();
					ConstantExpression conexp = (ConstantExpression)methexp.getMethod();
					if(conexp.getValue().equals(fieldName)){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public void debugFieldNodes(ClassNode classNode){
		List<PropertyNode>	fnlist = classNode.getProperties() ;
		for (PropertyNode node : fnlist) {
			System.out.println(classNode.getName() + " : " + node.getName() + "," );
		}
	}


	static public String getContents(File aFile) {
		System.out.println(aFile.getAbsolutePath());
		//...checks on aFile are elided
		StringBuilder contents = new StringBuilder();

		try {
			//use buffering, reading one line at a time
			//FileReader always assumes default encoding is OK!
			BufferedReader input =  new BufferedReader(new FileReader(aFile));
			try {
				String line = null; 
				while (( line = input.readLine()) != null){
					contents.append(line);
					contents.append(System.getProperty("line.separator"));
				}
			}
			finally {
				input.close();
			}
		}
		catch (IOException ex){
			ex.printStackTrace();
		}

		return contents.toString();
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

	//old but kept for reference
	/*
	public void addTableAndIdMapping(ClassNode classNode){
		FieldNode closure = classNode.getDeclaredField("mapping");

		if(closure!=null){
			boolean hasTable=hasFieldInClosure(closure,"table");
			boolean hasId=hasFieldInClosure(closure,"id");

			ClosureExpression exp = (ClosureExpression)closure.getInitialExpression();
			BlockStatement block = (BlockStatement) exp.getCode();

			//this just adds an s to the class name for the table if its not specified 
			Boolean pluralize = (Boolean)getMap(CO,"stamp.mapping.pluralTable");
			if(!hasTable && pluralize!=null && pluralize){
				String tablename = GrailsClassUtils.getShortName(classNode.getName())+"s";
				//LOG.info("Added new mapping to assign table: " + tablename);
				MethodCallExpression tableMeth = new MethodCallExpression(
					VariableExpression.THIS_EXPRESSION,
					new ConstantExpression("table"),
					new ArgumentListExpression(new ConstantExpression(tablename)) 
					);
				//block = (BlockStatement) exp.getCode();
				block.addStatement(new ExpressionStatement(tableMeth));
				//System.out.println(classNode.getName()+" - Added table mapping " + tablename );
			}
			//This adds the ID generator that we use for domian classes
			Map tableconf = (Map)getMap(CO,"stamp.mapping.id");
			if(!hasId && tableconf!=null){
				NamedArgumentListExpression namedarg = new NamedArgumentListExpression();
				if(tableconf.get("column") != null){
					namedarg.addMapEntryExpression(new ConstantExpression("column"), new ConstantExpression(tableconf.get("column").toString()));
				}
				if(tableconf.get("generator") != null){
					namedarg.addMapEntryExpression(new ConstantExpression("generator"), new ConstantExpression(tableconf.get("generator").toString()));
				}
				MethodCallExpression tableMeth = new MethodCallExpression(
					VariableExpression.THIS_EXPRESSION,
					new ConstantExpression("id"),
					namedarg
					);
				//block = (BlockStatement) exp.getCode();
				block.addStatement(new ExpressionStatement(tableMeth));
				//System.out.println(classNode.getName() + " - Added ID mapping with "+ tableconf);
			}
		}
		*/
		//System.out.println(block.toString());
}


//FUTURE 
/**
java.math.BigDecimal
java.lang.Integer
java.lang.Long
java.util.Date
java.lang.String
java.lang.Boolean
*/

/**
since grails has everything default to nullable:false, we change that to nullable:true here since omost of the time we condider it ok
explicity set nullable:false as the exception

public void addConstraintDefaults(ClassNode classNode){
	List<FieldNode>  fnlist = classNode.getFields();
	for(FieldNode fnode : fnlist){
		if(!fnode.isStatic()){
			//check if the type is in our list
			System.out.println("*" + fnode.getName() + " - " + fnode.getType().getName());
		}
	}
	
	boolean hasConstraint=false;

}
**/

/*
org.codehaus.groovy.ast.stmt.BlockStatement@f4b2da[
	org.codehaus.groovy.ast.stmt.ExpressionStatement@a0a4a[
		expression:org.codehaus.groovy.ast.expr.MethodCallExpression@29aa5a[
			object: org.codehaus.groovy.ast.expr.VariableExpression@6f0383[variable: this] 
			method: ConstantExpression[discDate] 
			arguments: org.codehaus.groovy.ast.expr.NamedArgumentListExpression@4fb195[
				org.codehaus.groovy.ast.expr.MapEntryExpression@13becc(key: ConstantExpression[nullable], value: ConstantExpression[true])
			]
		]
	],.....

/*
{ org.codehaus.groovy.ast.stmt.BlockStatement@f0bc0[
	org.codehaus.groovy.ast.stmt.ExpressionStatement@cc9e15[
		expression:org.codehaus.groovy.ast.expr.MethodCallExpression@9e94e8[
			object: org.codehaus.groovy.ast.expr.VariableExpression@3c2282[variable: this] 
			method: ConstantExpression[table] 
			arguments: org.codehaus.groovy.ast.expr.ArgumentListExpression@42428a[ConstantExpression[SyncSteps]]
		]
	], 
	org.codehaus.groovy.ast.stmt.ExpressionStatement@1eafb4[
		expression:org.codehaus.groovy.ast.expr.MethodCallExpression@a17663[
			object: org.codehaus.groovy.ast.expr.VariableExpression@3c2282[variable: this] 
			method: ConstantExpression[id] 
			arguments: org.codehaus.groovy.ast.expr.NamedArgumentListExpression@636202[
				org.codehaus.groovy.ast.expr.MapEntryExpression@b781ea(
					key: ConstantExpression[column], value: ConstantExpression[OID]
				), 
				org.codehaus.groovy.ast.expr.MapEntryExpression@b25934(
					key: ConstantExpression[generator], value: ConstantExpression[xx.hibernate.NewObjectIdGenerator]
				)
			]
		]
	], org.codehaus.groovy.ast.stmt.ExpressionStatement@fe6f06[
		expression:org.codehaus.groovy.ast.expr.MethodCallExpression@2b0459[
			object: org.codehaus.groovy.ast.expr.VariableExpression@3c2282[variable: this] 
			method: ConstantExpression[syncBatch] 
			arguments: org.codehaus.groovy.ast.expr.NamedArgumentListExpression@2a938f[
				org.codehaus.groovy.ast.expr.MapEntryExpression@3dbf04(key: ConstantExpression[column], value: ConstantExpression[SyncBatchId])]]]] }


*/

