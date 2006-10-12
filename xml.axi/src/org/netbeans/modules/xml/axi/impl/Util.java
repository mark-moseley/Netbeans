/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.axi.impl;

import java.util.ArrayList;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIComponentFactory;
import org.netbeans.modules.xml.axi.AXIDocument;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.axi.Attribute;
import org.netbeans.modules.xml.axi.Compositor;
import org.netbeans.modules.xml.axi.ContentModel;
import org.netbeans.modules.xml.axi.Element;
import org.netbeans.modules.xml.axi.AnyElement;
import org.netbeans.modules.xml.axi.AnyAttribute;
import org.netbeans.modules.xml.axi.datatype.Datatype;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.impl.ElementImpl.AnonymousType;
/**
 * Utility class.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class Util {
    
    /**
     * Creates a new instance of Util
     */
    private Util() {
    }
            
    public static void moveChildren(AXIComponent oldParent, AXIComponent newParent) {
        List<AXIComponent> children = new ArrayList<AXIComponent>();
        for(AXIComponent c: oldParent.getChildren()) {
            children.add(c);
        }
        for(AXIComponent c: children) {
            oldParent.removeChild(c);
            newParent.appendChild(c);
        }
    }
    
    /**
     * Adds proxy children to the specified parent. The proxy children are created against
     * each shared child. If called during bootstrapping the children list is updated, else
     * added to the parent.
     */
    public static void addProxyChildren(AXIComponent parent, AXIComponent shared, List<AXIComponent> children) {
        for(AXIComponent child : shared.getChildren()) {
            AXIComponentFactory factory = parent.getModel().getComponentFactory();
            AXIComponent proxy = factory.createProxy(child);
            if(children != null)
                children.add(proxy);
            else
                parent.appendChild(proxy);
        }
        
        if(shared instanceof ContentModel) {
            shared.addListener(parent);
        }
    }    
    
    /**
     * Finds an AXI component against the specified global schema component.
     */
    public static AXIComponent lookup(AXIModel axiModel, SchemaComponent gsc) {
        AXIModelImpl model = (AXIModelImpl)axiModel;
        if(model.fromSameSchemaModel(gsc)) {
            return model.lookup(gsc);
        }
        
        return model.lookupFromOtherModel(gsc);
    }
    
    public static boolean canSetType(AXIType oldValue, AXIType newValue) {
        if(oldValue == newValue)
            return false;
        
        if(oldValue instanceof Datatype && newValue instanceof Datatype) {
            if(((Datatype)oldValue).getKind() == ((Datatype)newValue).getKind()) {
                return false;
            }
        }
        
        if(oldValue instanceof AnonymousType && newValue instanceof AnonymousType) {
            if(((AnonymousType)oldValue).getPeer() == ((AnonymousType)newValue).getPeer()) {
                return false;
            }
        }
        
        if(oldValue instanceof ContentModel && newValue instanceof ContentModel) {
            if(((ContentModel)oldValue).getPeer() == ((ContentModel)newValue).getPeer()) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Returns an element's type.
     */
    public static SchemaComponent getSchemaType(SchemaComponent schemaComponent) {
        if(schemaComponent instanceof GlobalElement) {
            GlobalElement ge = (GlobalElement)schemaComponent;
            NamedComponentReference ref = ge.getType();
            if(ref != null) {
                return (SchemaComponent)ref.get();
            }
            return ge.getInlineType();
        }
        
        if(schemaComponent instanceof LocalElement) {
            LocalElement le = (LocalElement)schemaComponent;
            NamedComponentReference ref = le.getType();
            if(ref != null) {
                return (SchemaComponent)ref.get();
            }
            return le.getInlineType();
        }
        
        return null;
    }
    
    public static AXIType getAXIType(Element element, SchemaComponent type) {
        if(type == null)
            return null;
        if(type instanceof SimpleType) {
            DatatypeBuilder builder = new DatatypeBuilder(element.getModel());
            return builder.getDatatype(element.getPeer());
        }        
        if(type instanceof LocalComplexType) {
            return new AnonymousType(type);
        }
        if(type instanceof GlobalComplexType) {
            AXIModelImpl modelImpl = (AXIModelImpl)element.getModel();
            return (ContentModel)lookup(modelImpl, type);                    
        }
        return null;
    }
        
    public static String getProperty(AXIComponent child) {
        if(child instanceof Compositor)
            return Compositor.PROP_COMPOSITOR;
        if(child instanceof AbstractElement)
            return AbstractElement.PROP_ELEMENT;
        if(child instanceof AbstractAttribute)
            return AbstractAttribute.PROP_ATTRIBUTE;
        if(child instanceof ContentModel)
            return ContentModel.PROP_CONTENT_MODEL;
        
        return null;
    }
    
    public static Element findParentElement(AXIComponent component) {
        AXIComponent parent = (AXIComponent) component.getParent();
        if(parent == null)
            return null;
        
        if(parent instanceof Element)
            return (Element)parent;
        else
            return findParentElement(parent);
    }
    
    public static Datatype getDatatype(AXIModel model, SchemaComponent component) {
        DatatypeBuilder builder = new DatatypeBuilder(model);
        return builder.getDatatype(component);
    }
        
    public static void updateAnyElement(AnyElement element) {
        org.netbeans.modules.xml.schema.model.AnyElement any = 
                (org.netbeans.modules.xml.schema.model.AnyElement)element.getPeer();
        element.setMinOccurs(String.valueOf(any.getMinOccursEffective()));
        element.setMaxOccurs(any.getMaxOccursEffective());
        element.setTargetNamespace(any.getNameSpaceEffective());
        element.setProcessContents(any.getProcessContentsEffective());
    }
    
    public static void updateAnyAttribute(AnyAttribute attribute) {
        org.netbeans.modules.xml.schema.model.AnyAttribute anyAttr = 
                (org.netbeans.modules.xml.schema.model.AnyAttribute)attribute.getPeer();
        attribute.setProcessContents(anyAttr.getProcessContentsEffective());
        attribute.setTargetNamespace(anyAttr.getNameSpaceEffective());
    }
    
    public static void updateAXIDocument(AXIDocument document) {
        Schema schema = (Schema)document.getPeer();
        document.setTargetNamespace(schema.getTargetNamespace());
        document.setVersion(schema.getVersion());
        document.setLanguage(schema.getLanguage());
        document.setAttributeFormDefault(schema.getAttributeFormDefaultEffective());
        document.setElementFormDefault(schema.getElementFormDefaultEffective());
    }
    
    public static void updateGlobalAttribute(Attribute attribute) {
        GlobalAttribute component = (GlobalAttribute)attribute.getPeer();
        attribute.setName(component.getName());
        attribute.setDefault(component.getDefault());
        attribute.setFixed(component.getFixed());
        attribute.setType(getDatatype(attribute.getModel(), component));
    }
    
    public static void updateLocalAttribute(Attribute attribute) {
        LocalAttribute component = (LocalAttribute)attribute.getPeer();
        attribute.setName(component.getName());
        attribute.setDefault(component.getDefault());
        attribute.setFixed(component.getFixed());
        attribute.setForm(component.getFormEffective());
        attribute.setUse(component.getUseEffective());
        attribute.setType(getDatatype(attribute.getModel(), component));
    }
    
    public static void updateAttributeReference(Attribute attribute) {
        AttributeReference component = (AttributeReference)attribute.getPeer();
        //for AttributeRef, only use, default and fixed needs to be updated.
        attribute.setDefault(component.getDefault());
        attribute.setFixed(component.getFixed());
        attribute.setUse(component.getUseEffective());
    }
        
    public static void updateGlobalElement(Element element) {
        GlobalElement component = (GlobalElement)element.getPeer();
        element.setName(component.getName());
        element.setFixed(component.getFixed());
        element.setDefault(component.getDefault());
        element.setAbstract(component.getAbstractEffective());
        element.setNillable(component.getNillableEffective());
        //element.setContentType(getDatatype(element.getModel(), component));
        //element.setFinal();
        //element.setBlock();
    }
    
    public static void updateLocalElement(Element element) {
        LocalElement component = (LocalElement)element.getPeer();
        element.setName(component.getName());
        element.setMaxOccurs(component.getMaxOccursEffective());
        element.setMinOccurs(String.valueOf(component.getMinOccursEffective()));
        element.setFixed(component.getFixed());
        element.setDefault(component.getDefault());
        element.setNillable(component.getNillableEffective());
        element.setForm(component.getFormEffective());        
        //element.setBlock(component.getBlockEffective());
        //element.setContentType(getDatatype(element.getModel(), component));
        //element.setFinal();
    }
    
    public static void updateElementReference(Element elementRef) {
        ElementReference component = (ElementReference)elementRef.getPeer();
        //for an element-ref, get the min and max from that ref
        elementRef.setMaxOccurs(component.getMaxOccursEffective());
        elementRef.setMinOccurs(String.valueOf(component.getMinOccursEffective()));
    }
        
    public static void updateCompositor(Compositor compositor) {
        switch(compositor.getType()) {
            case SEQUENCE: {
                Sequence component = (Sequence)compositor.getPeer();
                Cardinality c = component.getCardinality();
                if (c != null) {
                    compositor.setMaxOccurs(c.getMaxOccursEffective());
                    compositor.setMinOccurs(String.valueOf(c.getMinOccursEffective()));
                } else {
                    compositor.setMaxOccurs("1");
                    compositor.setMinOccurs("1");
                }
                break;
            }
            case CHOICE: {
                Choice component = (Choice)compositor.getPeer();
                Cardinality c = component.getCardinality();
                if (c != null) {
                    compositor.setMaxOccurs(c.getMaxOccursEffective());
                    compositor.setMinOccurs(String.valueOf(c.getMinOccursEffective()));
                } else {
                    compositor.setMaxOccurs("1");
                    compositor.setMinOccurs("1");
                }                
                break;
            }
            case ALL: {
                All component = (All)compositor.getPeer();
                //Compositor compositor = new All(model, component);
                //compositor.setMaxOccurs(component.getMaxOccursEffective());
                compositor.setMinOccurs(String.valueOf(component.getMinOccursEffective()));
                break;
            }            
        }
        
    }	

    public static void updateContentModel(ContentModel contentModel) {
        SchemaComponent peer = contentModel.getPeer();
        if(peer instanceof GlobalComplexType) {
            contentModel.setName(((GlobalComplexType)peer).getName());
            return;
        }
        if(peer instanceof GlobalAttributeGroup) {
            contentModel.setName(((GlobalAttributeGroup)peer).getName());
            return;
        }
        if(peer instanceof GlobalGroup) {
            contentModel.setName(((GlobalGroup)peer).getName());
            return;
        }
    }
}
