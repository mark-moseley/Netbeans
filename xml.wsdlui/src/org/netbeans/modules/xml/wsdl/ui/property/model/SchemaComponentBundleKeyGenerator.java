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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.ui.property.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.xml.schema.model.All;
import org.netbeans.modules.xml.schema.model.AnyAttribute;
import org.netbeans.modules.xml.schema.model.AnyElement;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.AttributeReference;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.ComplexContent;
import org.netbeans.modules.xml.schema.model.ComplexContentDefinition;
import org.netbeans.modules.xml.schema.model.ComplexContentRestriction;
import org.netbeans.modules.xml.schema.model.ComplexExtension;
import org.netbeans.modules.xml.schema.model.ComplexExtensionDefinition;
import org.netbeans.modules.xml.schema.model.ComplexType;
import org.netbeans.modules.xml.schema.model.ComplexTypeDefinition;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.GroupReference;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.LocalGroupDefinition;
import org.netbeans.modules.xml.schema.model.LocalSimpleType;
import org.netbeans.modules.xml.schema.model.LocalType;
import org.netbeans.modules.xml.schema.model.NameableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Sequence;
import org.netbeans.modules.xml.schema.model.SequenceDefinition;
import org.netbeans.modules.xml.schema.model.SimpleContent;
import org.netbeans.modules.xml.schema.model.SimpleContentRestriction;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.schema.model.SimpleType;
import org.netbeans.modules.xml.schema.model.SimpleTypeRestriction;
import org.netbeans.modules.xml.schema.model.TypeContainer;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;

/**
 *
 * @author radval
 */
public class SchemaComponentBundleKeyGenerator extends AbstractXSDVisitor {

    private List mChildren = new ArrayList();

    private StringBuffer mBuffer = new StringBuffer(200);

    private NameableSchemaComponent mCurrentElement;

    private Map<NameableSchemaComponent, ElementToKeyName> elementToKeyNameMap = new HashMap<NameableSchemaComponent, ElementToKeyName>();

    private Schema mSchema;

    /** Creates a new instance of SchemaComponentBundleKeyGenerator */
    public SchemaComponentBundleKeyGenerator() {
    }



    public String getKey() {
        return mBuffer.toString();
    }

    @Override
    public void visit(LocalAttribute la) {
        visitAttribute(la);
    }

    @Override
    public void visit(AttributeReference reference) {
        NamedComponentReference<GlobalAttribute> ga = reference.getRef();
        if(ga != null) {
            visit(ga.get());
        }
    }

    @Override
    public void visit(GlobalAttribute ga) {
        visitAttribute(ga);
    }

    @Override
    public void visit(AttributeGroupReference agr) {
        NamedComponentReference<GlobalAttributeGroup> aGroup = agr.getGroup();
        if(aGroup != null) {
            visit(aGroup.get());
        }
    }


    @Override
    public void visit(GlobalAttributeGroup gag) {
        List<SchemaComponent> children = gag.getChildren();
        Iterator<SchemaComponent> it = children.iterator();

        while(it.hasNext()) {
            SchemaComponent sc = it.next();
            if(sc instanceof  LocalAttribute) {
                visit((LocalAttribute) sc);
            } else if(sc instanceof AttributeReference) {
                visit((AttributeReference) sc );
            } else if(sc instanceof  AttributeGroupReference) {
                visit((AttributeGroupReference) sc);
            }
        }

    }

    @Override
    public void visit(ElementReference er) {
        NamedComponentReference<GlobalElement> ge = er.getRef();
        if(ge != null && ge.get() != null) {
            visit(ge.get());
        }
    }

    @Override
    public void visit(GlobalElement ge) {
        visitElement(ge);

    }

    @Override
    public void visit(All all) {
        Collection<LocalElement> allElements = all.getElements();
        Iterator<LocalElement> it = allElements.iterator();
        while(it.hasNext()) {
            LocalElement element = it.next();
            visit(element);
        }
    }

    @Override
    public void visit(AnyAttribute anyAttr) {

    }

    @Override
    public void visit(AnyElement any) {

    }


    @Override
    public void visit(Choice choice) {
        List<SchemaComponent> children =  choice.getChildren();
        Iterator<SchemaComponent> it = children.iterator();

        while(it.hasNext()) {
            SchemaComponent comp = it.next();
            if(comp instanceof AnyElement) {
                visit((AnyElement) comp);
            } else if(comp instanceof Choice) {
                visit((Choice) comp);
            } else if(comp instanceof ElementReference) {
                visit((ElementReference) comp);
            } else if(comp instanceof GroupReference) {
                visit((GroupReference) comp);
            } else if(comp instanceof LocalElement) {
                visit((LocalElement) comp);
            } else if(comp instanceof Sequence) {
                visit((Sequence) comp);
            } 
        }
    }
    
    @Override
    public void visit(ComplexContent cc) {

        ComplexContentDefinition ccd = cc.getLocalDefinition();
        if(ccd != null) {
            visit(ccd);
        }
    }




    @Override
    public void visit(GlobalComplexType gct) {
        visit((GlobalType) gct);
    }

    @Override
    public void visit(GlobalGroup gd) {
        LocalGroupDefinition lgd = gd.getDefinition();
        if(lgd != null) {
            if(lgd instanceof Choice) {
                visit((Choice) lgd);
            } else if(lgd instanceof All) {
                visit((All) lgd );
            } else if(lgd instanceof Sequence) {
                visit((Sequence)lgd);
            }
        }
    }

    @Override
    public void visit(GlobalSimpleType gst) {
        visit((GlobalType) gst);
    }

    @Override
    public void visit(GroupReference gr) {
        NamedComponentReference<GlobalGroup> gg = gr.getRef();
        if(gg != null) {
            visit(gg.get());
        }
    }

    @Override
    public void visit(LocalComplexType type) {
        visit((LocalType) type);
    }

    @Override
    public void visit(LocalElement le) {
        visitElement(le);
    }

    @Override
    public void visit(LocalSimpleType type) {
        visit((LocalType) type);
    }

    @Override
    public void visit(Sequence s) {
        visit(s.getContent());
    }

    @Override
    public void visit(SimpleContent sc) {

    }

    @Override
    public void visit(SimpleContentRestriction scr) {

    }

    @Override
    public void visit(SimpleExtension se) {

    }

    @Override
    public void visit(SimpleTypeRestriction str) {

    }

    public void visit(GlobalType gt) {
        if(gt instanceof ComplexType) {
            visit((ComplexType) gt);
        } else if(gt instanceof SimpleType) {
            visit((SimpleType) gt);
        }
    }

    public void visit(LocalType lt) {
        if(lt instanceof ComplexType) {
            visit((ComplexType) lt);
        } else if(lt instanceof SimpleType) {
            visit((SimpleType) lt);
        }
    }

    
    private void visit(SimpleType st) {
    }

    private void visit(ComplexType ct) {
        List<SchemaComponent> children =  ct.getChildren();
        Iterator<SchemaComponent> it = children.iterator();
        while(it.hasNext()) {
            SchemaComponent sc = it.next();
            if(sc instanceof  AnyAttribute) {
                visit((AnyAttribute) sc );
            } else if(sc instanceof AttributeGroupReference) {
                visit((AttributeGroupReference) sc);
            }else if(sc instanceof AttributeReference) {
                visit((AttributeReference) sc);
            }else if(sc instanceof LocalAttribute) {
                visit((LocalAttribute) sc);
            }else if(sc instanceof ComplexTypeDefinition) {
                visit((ComplexTypeDefinition) sc);
            }
        }

        //search TypeContainer
        //getAttributeGroupReferences

    }

    private void visit(ComplexTypeDefinition ctd) {
        if (ctd instanceof All) {
            visit((All) ctd);
        } else if (ctd instanceof Choice) {
            visit((Choice) ctd);
        } else if (ctd instanceof Sequence) {
            visit((Sequence) ctd);    
        } else if(ctd instanceof ComplexContent) {
            visit((ComplexContent) ctd);
        } else if(ctd instanceof GroupReference) {
            visit((GroupReference) ctd);
        } else if (ctd instanceof SimpleContent) {
            visit((SimpleContent) ctd);
        }
    }

    private void visit(ComplexContentDefinition ccd) {

        if(ccd instanceof ComplexContentRestriction) {
            visit((ComplexContentRestriction) ccd);
        } else if(ccd instanceof ComplexExtension) {
            visit((ComplexExtension) ccd);
        }

    }

    @Override
    public void visit(ComplexContentRestriction ccr) {
        NamedComponentReference<GlobalComplexType> baseRef = ccr.getBase();
        if(baseRef != null) {
            GlobalComplexType gType = baseRef.get();
            if(gType != null) {
                visit(gType);
            }
        }

        List children = ccr.getChildren();
        Iterator it = children.iterator();
        while(it.hasNext()) {
            Object child = it.next();

            if(child instanceof AnyAttribute) {
                visit((AnyAttribute) child);
            } else if(child instanceof AttributeGroupReference) {
                visit((AttributeGroupReference) child);
            } else if(child instanceof AttributeReference) {
                visit((AttributeReference) child);
            } else if (child instanceof ComplexTypeDefinition) {
                visit((ComplexTypeDefinition) child);
            }
        }
    }
    
    @Override
    public void visit(ComplexExtension ce) {
        NamedComponentReference<GlobalType> baseRef = ce.getBase();
        if(baseRef != null) {
            GlobalType gType = baseRef.get();
            if(gType != null) {
                visit(gType);
            }
        }

        List children = ce.getChildren();
        Iterator it = children.iterator();
        while(it.hasNext()) {
            Object child = it.next();

            if(child instanceof LocalAttribute) {
                visit((LocalAttribute) child);
            }else if(child instanceof AnyAttribute) {
                visit((AnyAttribute) child);
            } else if(child instanceof AttributeGroupReference) {
                visit((AttributeGroupReference) child);
            } else if(child instanceof AttributeReference) {
                visit((AttributeReference) child);
            } else if (child instanceof ComplexExtensionDefinition) {
                visit((ComplexExtensionDefinition) child);
            }
        }
    }


    private void visit(ComplexExtensionDefinition ced) {
        if (ced instanceof All) {
            visit((All) ced);
        } else if (ced instanceof Choice) {
            visit((Choice) ced);
        } else if (ced instanceof Sequence) {
            visit((Sequence) ced);    
        } else if(ced instanceof GroupReference) {
            visit((GroupReference) ced);
        } 
    }

    private void visit(List<SequenceDefinition> sdList) {
        Iterator<SequenceDefinition> it = sdList.iterator();
        while(it.hasNext()) {
            SequenceDefinition sd = it.next();
            if(sd instanceof Sequence) {
                visit((Sequence) sd);
            } else if(sd instanceof AnyElement) {
                visit((AnyElement) sd);
            } else if(sd instanceof Choice) {
                visit((Choice) sd);
            } else if(sd instanceof ElementReference) {
                visit((ElementReference) sd);
            } else if(sd instanceof GroupReference) {
                visit((GroupReference) sd);
            } else if(sd instanceof LocalElement) {
                visit((LocalElement) sd);
            }

        }
    }

    void visitAttribute(NameableSchemaComponent la) {
        StringBuffer key = new StringBuffer(20);
        String elementPrefix = getElementPrefix();
        if(elementPrefix != null) {
            key.append(elementPrefix);
            key.append("_");
        }

        key.append(la.getName());
        storeKeyValue(key.toString(), la.getName());
    }

    private void visitElement(NameableSchemaComponent ge) {
        StringBuffer key = new StringBuffer(20);

        ElementToKeyName ekn = elementToKeyNameMap.get(mCurrentElement);
        if(ekn != null) {
            String prefix = ekn.getKeyPrefix();
            key.append(prefix);
            key.append("_");
        }
        key.append(ge.getName());
        storeKeyValue(key.toString(), ge.getName());

        ElementToKeyName newEkn = new ElementToKeyName(ge, key.toString());
        elementToKeyNameMap.put(ge, newEkn);

        if(ge instanceof TypeContainer) {
            TypeContainer tc = (TypeContainer) ge;
            LocalType lt = tc.getInlineType();
            NamedComponentReference gtRef = tc.getType();
            if(lt != null) {
                visit(lt);
            } else if(gtRef != null && gtRef.getType() != null) {
                visit((GlobalType) gtRef.get());
            }
        }
    }

    private String getElementPrefix() {
        String elementPrefix = null;
        if(mCurrentElement != null) {
            ElementToKeyName ekn = elementToKeyNameMap.get(mCurrentElement);
            if(ekn != null) {
                elementPrefix =  ekn.getKeyPrefix();
            }

        }

        return elementPrefix;
    }
    private void storeKeyValue(String key, String value) {
        mBuffer.append(key);
        mBuffer.append("=");
        mBuffer.append(value);
        mBuffer.append("\n");
    }


    class ElementToKeyName {

        private NameableSchemaComponent mElement;

        private String mKeyPrefix;


        ElementToKeyName(NameableSchemaComponent element, String keyPrefix) {
            this.mElement = element;
            this.mKeyPrefix = keyPrefix;

        }


        public NameableSchemaComponent getElement() {
            return this.mElement;
        }

        public String getKeyPrefix() {
            return this.mKeyPrefix;
        }

    }

}
