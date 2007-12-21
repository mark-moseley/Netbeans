package org.netbeans.modules.iep.model.impl;

import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.iep.model.IEPComponent;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.IEPVisitor;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.w3c.dom.Element;

public class SchemaComponentImpl extends ComponentImpl implements SchemaComponent {

	public SchemaComponentImpl(IEPModel model) {
		super(model);
		setType("/IEP/Metadata/Schema"); //NOI18N
	}

	public SchemaComponentImpl(IEPModel model, Element element) {
		super(model, element);
		setType("/IEP/Metadata/Schema"); //NOI18N
	}
	
	public IEPComponent createChild (Element childEl) {
		IEPComponent child = null;
        
        if (childEl != null) {
            String localName = childEl.getLocalName();
            if (localName == null || localName.length() == 0) {
                    localName = childEl.getTagName();
            }
            if (localName.equals(COMPONENT_CHILD)) {
            		child = new SchemaAttributeImpl(getModel(), childEl);
            } else {
            	child = super.createChild(childEl);
            }
        }
        
        return child;
	}
	
	 public void accept(IEPVisitor visitor) {
    	visitor.visitSchemaComponent(this);
     }
	 

	public List<SchemaAttribute> getSchemaAttributes() {
		return getChildren(SchemaAttribute.class);
	}


	public void setSchemaAttributes(List<SchemaAttribute> attrs)  {
		if(attrs != null) {
			//first remove existing attributes
			removeAllSchemaAttributes();
			
			Iterator<SchemaAttribute> it = attrs.iterator();
			while(it.hasNext()) {
				SchemaAttribute sa = it.next();
//				String attrName = sa.getAttributeName();
//				if(attrName != null) {
//					SchemaAttribute existingSA = findSchemaAttribute(attrName);
//					if(existingSA != null) {
//						removeSchemaAttribute(existingSA);
//					}
//				}
				
				addSchemaAttribute(sa);
			}
		}
	}

	public SchemaAttribute findSchemaAttribute(String attributeName) {
		SchemaAttribute attr = null;
		
		if(attributeName == null) {
			return null;
		}
		
		List<SchemaAttribute> schemaAttributes = getSchemaAttributes();
		Iterator<SchemaAttribute> it = schemaAttributes.iterator();
		
		while(it.hasNext()) {
			SchemaAttribute sa = it.next();
			
			if(attributeName.equals(sa.getName())) {
				attr = sa;
				break;
			}
		}
		
		return attr;
	}

	public void addSchemaAttribute(SchemaAttribute sa) {
		if(sa != null) {
			String attrName = sa.getAttributeName();
			if(attrName != null) {
				SchemaAttribute existingSA = findSchemaAttribute(attrName);
				if(existingSA != null) {
					removeSchemaAttribute(existingSA);
				}
				
				addChildComponent(sa);
			}
		}
		
	}
	
	public void removeSchemaAttribute(SchemaAttribute sa) {
		if(sa != null) {
			removeChildComponent(sa);
		}
		
	}
	
	private void removeAllSchemaAttributes() {
		Iterator<SchemaAttribute> it = getSchemaAttributes().iterator();
		while(it.hasNext()) {
			SchemaAttribute sa = it.next();
			removeSchemaAttribute(sa);
		}
	}
	
	public String toString() {
		StringBuffer resultStrBuffer = new StringBuffer();
		
		resultStrBuffer.append("schema name: ");
		resultStrBuffer.append(getName());
		
		return resultStrBuffer.toString();
	}
}
