package org.netbeans.modules.iep.model;

import java.util.List;

import org.netbeans.modules.xml.xam.dom.Attribute;
import org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement.StringAttribute;

public interface PlanComponent extends Component {

	static final String TARGETNAMESPACE_PROPERTY = "targetNamespace";
	
	static final Attribute ATTR_TARGETNAMESPACE = new StringAttribute(TARGETNAMESPACE_PROPERTY);
	
	OperatorComponentContainer getOperatorComponentContainer();
	
	LinkComponentContainer getLinkComponentContainer();
	
	SchemaComponentContainer getSchemaComponentContainer();
	
    List<Import> getImports();
        
    String getTargetNamespace();
    
    void setTargetNamespace(String targetNamespace);
}
