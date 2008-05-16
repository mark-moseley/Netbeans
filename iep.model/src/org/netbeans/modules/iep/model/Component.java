package org.netbeans.modules.iep.model;

import java.util.Collection;
import java.util.List;

import org.netbeans.modules.iep.model.lib.TcgComponentType;
import org.netbeans.modules.xml.wsdl.model.spi.GenericExtensibilityElement.StringAttribute;
import org.netbeans.modules.xml.xam.dom.Attribute;

public interface Component extends IEPComponent {

        static final String COMPONENT_CHILD = "component";
        
        static final String PROPERTY_CHILD = "property";
        
        static final String IMPORT_CHILD = "import";
        
        static final String DOCUMENTATION_CHILD = "documentation";
        
    static final String NAME_PROPERTY = "name";

    static final String TITLE_PROPERTY = "title";

    static final String TYPE_PROPERTY = "type";
    
        static final Attribute ATTR_NAME = new StringAttribute(NAME_PROPERTY);
        
        static final Attribute ATTR_TITLE = new StringAttribute(TITLE_PROPERTY);
        
        static final Attribute ATTR_TYPE = new StringAttribute(TYPE_PROPERTY);
        
    String getName();
        
    void setName(String name);
    
    String getTitle();
    
    void setTitle(String title);
    
    String getType();
    
    void setType(String type);
    
    List<Component> getChildComponents();

    List<Property> getProperties();
    
    void addChildComponent(Component child);
    
    void removeChildComponent(Component child);
    
    
    void addProperty(Property property);
    
    void removeProperty(Property property);
    
    Property getProperty(String name);
    
    void setDocumentation(Documentation doc);
    
    Documentation getDocumentation();
    
    TcgComponentType getComponentType();
}
