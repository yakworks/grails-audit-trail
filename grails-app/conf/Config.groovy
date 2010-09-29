import static grails.util.Environment.*


stamp{
	audit{
		//the created and edited fields should be present or they won't get added during AST
		createdBy="createdBy" //id who created
		createdDate="createdDate" //
		editedBy="updatedBy" //id who updated/edited
		editedDate="editedDate"//date edited
		//the following are optional and are for mapping
		companyId="companyId" //used for multi-tenant, who this is for
	}
}

//future
//defaultContraints{
	//makes nullable true for fields of the following type (only add this constraint if it doesn't already exist)
//	nullable=['java.math.BigDecimal','java.lang.Integer','java.lang.Long','java.util.Date','java.lang.String','java.lang.Boolean']
//}



// The following properties have been added by the Upgrade process...
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"
grails.gorm.default.mapping = {
	id column: 'OID', generator:'nineci.hibernate.NewObjectIdGenerator'
}