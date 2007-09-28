/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.xml.axi.impl;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.xml.axi.datatype.*;
import org.netbeans.modules.xml.axi.datatype.UnionType;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.netbeans.modules.xml.axi.*;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.visitor.DeepSchemaVisitor;

/**
 *
 * @author Ayub Khan (Ayub.Khan@Sun.Com)
 */
public class DatatypeBuilder extends DeepSchemaVisitor {
    
    /**
     * Creates a new instance of DatatypeBuilder
     */
    DatatypeBuilder() {
    }
    
    /**
     * Creates a new instance of DatatypeBuilder
     */
    public DatatypeBuilder(AXIModel model) {
        this.model = (AXIModelImpl)model;
    }
    
    public void visit(Enumeration enumeration) {
        assert (facetParent instanceof SimpleRestriction);
        if(datatype instanceof NumberBase)
            ((NumberBase)datatype).addEnumeration(
                    NumberBase.toNumber(enumeration.getValue()));
        else if(datatype instanceof BinaryBase)
            ((BinaryBase)datatype).addEnumeration(enumeration.getValue());
        else if(datatype instanceof NotationType)
            ((NotationType)datatype).addEnumeration(enumeration.getValue());
        else if(datatype instanceof QNameType)
            ((QNameType)datatype).addEnumeration(enumeration.getValue());
        else if(datatype instanceof StringBase)
            ((StringBase)datatype).addEnumeration(enumeration.getValue());
        else if(datatype instanceof TimeBase)
            ((TimeBase)datatype).addEnumeration(enumeration.getValue());
    }
    
    public void visit(FractionDigits fractionDigits) {
        assert (facetParent instanceof SimpleRestriction);
        java.util.List<Number> removeList =
                new ArrayList<Number>();
        java.util.List<? extends Number> currentList = datatype.getFractionDigits();
        if(currentList != null && currentList.size() > 0) {
            for(int i=0;i<currentList.size();i++)
                removeList.add(currentList.get(i));
            for(int i=0;i<removeList.size();i++)
                datatype.removeFractionDigits(removeList.get(i));
        }
        datatype.addFractionDigits(fractionDigits.getValue());
    }
    
    public void visit(Length length) {
        assert (facetParent instanceof SimpleRestriction);
        java.util.List<Number> removeList =
                new ArrayList<Number>();
        java.util.List<? extends Number> currentList = datatype.getLengths();
        if(currentList != null && currentList.size() > 0) {
            for(int i=0;i<currentList.size();i++)
                removeList.add(currentList.get(i));
            for(int i=0;i<removeList.size();i++)
                datatype.removeLength(removeList.get(i));
        }
        datatype.addLength(length.getValue());
    }
    
    public void visit(MaxExclusive maxExclusive) {
        assert (facetParent instanceof SimpleRestriction);
        if(datatype instanceof NumberBase) {
            java.util.List<Number> removeList =
                    new ArrayList<Number>();
            java.util.List<? extends Number> currentList =
                    ((NumberBase)datatype).getMaxExclusives();
            if(currentList != null && currentList.size() > 0) {
                for(int i=0;i<currentList.size();i++)
                    removeList.add(currentList.get(i));
                for(int i=0;i<removeList.size();i++)
                    ((NumberBase)datatype).removeMaxExclusive(removeList.get(i));
            }
            ((NumberBase)datatype).addMaxExclusive(
                    NumberBase.toNumber(maxExclusive.getValue()));
        } else if(datatype instanceof TimeBase) {
            java.util.List<String> removeList =
                    new ArrayList<String>();
            java.util.List<String> currentList =
                    ((TimeBase)datatype).getMaxExclusives();
            if(currentList != null && currentList.size() > 0) {
                for(int i=0;i<currentList.size();i++)
                    removeList.add(currentList.get(i));
                for(int i=0;i<removeList.size();i++)
                    ((TimeBase)datatype).removeMaxExclusive(removeList.get(i));
            }
            ((TimeBase)datatype).addMaxExclusive(maxExclusive.getValue());
        }
    }
    
    public void visit(MaxInclusive maxInclusive) {
        assert (facetParent instanceof SimpleRestriction);
        if(datatype instanceof NumberBase) {
            java.util.List<Number> removeList =
                    new ArrayList<Number>();
            java.util.List<? extends Number> currentList =
                    ((NumberBase)datatype).getMaxInclusives();
            if(currentList != null && currentList.size() > 0) {
                for(int i=0;i<currentList.size();i++)
                    removeList.add(currentList.get(i));
                for(int i=0;i<removeList.size();i++)
                    ((NumberBase)datatype).removeMaxInclusive(removeList.get(i));
            }
            ((NumberBase)datatype).addMaxInclusive(
                    NumberBase.toNumber(maxInclusive.getValue()));
        } else if(datatype instanceof TimeBase) {
            java.util.List<String> removeList =
                    new ArrayList<String>();
            java.util.List<String> currentList =
                    ((TimeBase)datatype).getMaxInclusives();
            if(currentList != null && currentList.size() > 0) {
                for(int i=0;i<currentList.size();i++)
                    removeList.add(currentList.get(i));
                for(int i=0;i<removeList.size();i++)
                    ((TimeBase)datatype).removeMaxInclusive(removeList.get(i));
            }
            ((TimeBase)datatype).addMaxInclusive(maxInclusive.getValue());
        }
    }
    
    public void visit(MinExclusive minExclusive) {
        assert (facetParent instanceof SimpleRestriction);
        if(datatype instanceof NumberBase) {
            java.util.List<Number> removeList =
                    new ArrayList<Number>();
            java.util.List<? extends Number> currentList =
                    ((NumberBase)datatype).getMinExclusives();
            if(currentList != null && currentList.size() > 0) {
                for(int i=0;i<currentList.size();i++)
                    removeList.add(currentList.get(i));
                for(int i=0;i<removeList.size();i++)
                    ((NumberBase)datatype).removeMinExclusive(removeList.get(i));
            }
            ((NumberBase)datatype).addMinExclusive(
                    NumberBase.toNumber(minExclusive.getValue()));
        } else if(datatype instanceof TimeBase) {
            java.util.List<String> removeList =
                    new ArrayList<String>();
            java.util.List<String> currentList =
                    ((TimeBase)datatype).getMinExclusives();
            if(currentList != null && currentList.size() > 0) {
                for(int i=0;i<currentList.size();i++)
                    removeList.add(currentList.get(i));
                for(int i=0;i<removeList.size();i++)
                    ((TimeBase)datatype).removeMinExclusive(removeList.get(i));
            }
            ((TimeBase)datatype).addMinExclusive(minExclusive.getValue());
        }
    }
    
    public void visit(MinInclusive minInclusive) {
        assert (facetParent instanceof SimpleRestriction);
        if(datatype instanceof NumberBase) {
            java.util.List<Number> removeList =
                    new ArrayList<Number>();
            java.util.List<? extends Number> currentList =
                    ((NumberBase)datatype).getMinInclusives();
            if(currentList != null && currentList.size() > 0) {
                for(int i=0;i<currentList.size();i++)
                    removeList.add(currentList.get(i));
                for(int i=0;i<removeList.size();i++)
                    ((NumberBase)datatype).removeMinInclusive(removeList.get(i));
            }
            ((NumberBase)datatype).addMinInclusive(
                    NumberBase.toNumber(minInclusive.getValue()));
        } else if(datatype instanceof TimeBase) {
            java.util.List<String> removeList =
                    new ArrayList<String>();
            java.util.List<String> currentList =
                    ((TimeBase)datatype).getMinInclusives();
            if(currentList != null && currentList.size() > 0) {
                for(int i=0;i<currentList.size();i++)
                    removeList.add(currentList.get(i));
                for(int i=0;i<removeList.size();i++)
                    ((TimeBase)datatype).removeMinInclusive(removeList.get(i));
            }
            ((TimeBase)datatype).addMinInclusive(minInclusive.getValue());
        }
    }
    
    public void visit(MaxLength maxLength) {
        assert (facetParent instanceof SimpleRestriction);
        java.util.List<Number> removeList =
                new ArrayList<Number>();
        java.util.List<? extends Number> currentList = datatype.getMaxLengths();
        if(currentList != null && currentList.size() > 0) {
            for(int i=0;i<currentList.size();i++)
                removeList.add(currentList.get(i));
            for(int i=0;i<removeList.size();i++)
                datatype.removeMaxLength(removeList.get(i));
        }
        datatype.addMaxLength(maxLength.getValue());
    }
    
    public void visit(MinLength minLength) {
        assert (facetParent instanceof SimpleRestriction);
        java.util.List<Number> removeList = new ArrayList<Number>();
        java.util.List<? extends Number> currentList = datatype.getMinLengths();
        if(currentList != null && currentList.size() > 0) {
            for(int i=0;i<currentList.size();i++)
                removeList.add(currentList.get(i));
            for(int i=0;i<removeList.size();i++)
                datatype.removeMinLength(removeList.get(i));
        }
        datatype.addMinLength(minLength.getValue());
    }
    
    public void visit(Pattern pattern) {
        assert (facetParent instanceof SimpleRestriction);
        datatype.addPattern(pattern.getValue());
    }
    
    public void visit(TotalDigits totalDigits) {
        assert (facetParent instanceof SimpleRestriction);
        java.util.List<Number> removeList =
                new ArrayList<Number>();
        java.util.List<? extends Number> currentList = datatype.getTotalDigits();
        if(currentList != null && currentList.size() > 0) {
            for(int i=0;i<currentList.size();i++)
                removeList.add(currentList.get(i));
            for(int i=0;i<removeList.size();i++)
                datatype.removeTotalDigits(removeList.get(i));
        }
        datatype.addTotalDigits(totalDigits.getValue());
    }
    
    public void visit(Whitespace facet) {
        assert (facetParent instanceof SimpleRestriction);
        java.util.List<Whitespace.Treatment> removeList =
                new ArrayList<Whitespace.Treatment>();
        java.util.List<Whitespace.Treatment> currentList = datatype.getWhiteSpaces();
        if(currentList != null && currentList.size() > 0) {
            for(int i=0;i<currentList.size();i++)
                removeList.add(currentList.get(i));
            for(int i=0;i<removeList.size();i++)
                datatype.removeWhitespace(removeList.get(i));
        }
        datatype.addWhitespace(facet.getValue());
    }
    
    /**
     * Creates an AXI Datatype, given a schema component.
     */
    Datatype getDatatype(SchemaComponent component) {
        if(component instanceof LocalAttribute)
            return createDatatype((LocalAttribute)component);
        if(component instanceof GlobalAttribute)
            return createDatatype((GlobalAttribute)component);
        if(component instanceof AttributeReference)
            return createDatatype(((AttributeReference)component).getRef().get());
        if(component instanceof LocalElement)
            return createDatatype((LocalElement)component);
        if(component instanceof GlobalElement)
            return createDatatype((GlobalElement)component);
        if(component instanceof ElementReference)
            return createDatatype(((ElementReference)component).getRef().get());
        if(component instanceof GlobalSimpleType)
            return new CustomDatatype(
                    ((GlobalSimpleType)component).getName(),
                    createDatatype((SimpleType) component));
        
        return null;
    }
    
    /**
     * Creates an AXI Datatype, given a schema component.
     */
    Datatype getDatatype(SimpleType st) {
        return createDatatype(st);
    }
    
    /**
     * Creates an AXI Datatype, given a schema LocalAttribute.
     */
    Datatype createDatatype(LocalAttribute attribute) {
        if(attribute.getType() != null) {
            datatype = createDatatype(attribute.getType().get());
        } else {
            datatype = createDatatype(attribute.getInlineType());
        }
        return datatype;
    }
    
    /**
     * Creates an AXI Datatype, given a schema GlobalAttribute.
     */
    Datatype createDatatype(GlobalAttribute attribute) {
        if(attribute.getType() != null) {
            datatype = createDatatype(attribute.getType().get());
        } else {
            datatype = createDatatype(attribute.getInlineType());
        }
        return datatype;
    }
    
    /**
     * Creates an AXI Datatype, given a schema LocalAttribute.
     */
    Datatype createDatatype(LocalElement le) {
        if(le.getType() != null) {
            if(le.getType().get() instanceof SimpleType)
                datatype = createDatatype((SimpleType) le.getType().get());
        } else {
            if(le.getInlineType() instanceof SimpleType)
                datatype = createDatatype((SimpleType) le.getInlineType());
        }
        return datatype;
    }
    
    /**
     * Creates an AXI Datatype, given a schema GlobalAttribute.
     */
    Datatype createDatatype(GlobalElement ge) {
        if(ge.getType() != null) {
            if(ge.getType().get() instanceof SimpleType)
                datatype = createDatatype((SimpleType) ge.getType().get());
        } else {
            if(ge.getInlineType() instanceof SimpleType)
                datatype = createDatatype((SimpleType) ge.getInlineType());
        }
        return datatype;
    }
    
    Datatype createDatatype(final SimpleType simpleType) {
        datatype = null;
        try {
            datatype = doCreateDatatype(simpleType);
            if(simpleType instanceof GlobalSimpleType &&
                    this.model != null) {
                String name = ((GlobalSimpleType)simpleType).getName();
                for(GlobalSimpleType gst:this.model.getSchemaModel().getSchema().getSimpleTypes())
                    if(gst.getName().equals(name))
                        return new CustomDatatype(name, datatype);
            }
        }catch(Throwable th) {
            th.printStackTrace();
        }
        return datatype;
    }
    
    Datatype doCreateDatatype(final SimpleType simpleType) {
        if(simpleType == null) return null;
        if(simpleType instanceof GlobalSimpleType) {
            datatype = DatatypeFactory.getDefault().
                    createPrimitive(((GlobalSimpleType)simpleType).getName());
        }
        if(datatype == null) {//not a built-in type
            SimpleTypeDefinition def = simpleType.getDefinition();
            if(def instanceof SimpleTypeRestriction) {
                GlobalSimpleType baseType = null;
                if(((SimpleTypeRestriction)def).getBase() != null) {
                    baseType = (GlobalSimpleType)((SimpleTypeRestriction)def).getBase().get();
                    datatype = doCreateDatatype(baseType);
                    SimpleRestriction r = (SimpleRestriction)def;
                    facetParent = r;
                    findFacets(r);
                } else
                    datatype = doCreateDatatype(((SimpleTypeRestriction)def).getInlineType());
            } else if(def instanceof org.netbeans.modules.xml.schema.model.List) {
                org.netbeans.modules.xml.schema.model.List l =
                        (org.netbeans.modules.xml.schema.model.List)def;
                if(l.getType() != null)
                    datatype = doCreateDatatype(l.getType().get());
                else
                    datatype = doCreateDatatype(l.getInlineType());
            } else if(def instanceof Union) {
                Union u = (Union)def;
                Datatype unionType = new UnionType();
                
                if(u.getMemberTypes() != null) {
                    for(NamedComponentReference<GlobalSimpleType> gst:u.getMemberTypes()) {
                        Datatype memberType = doCreateDatatype(gst.get());
                        if(memberType != null)
                            ((UnionType)unionType).addMemberType(memberType);
                    }
                }
                if(u.getInlineTypes() != null) {
                    for(LocalSimpleType lst:u.getInlineTypes()) {
                        datatype = null;
                        Datatype memberType = doCreateDatatype(lst);
                        if(memberType != null)
                            ((UnionType)unionType).addMemberType(memberType);
                    }
                    if(u.getInlineTypes().size() > 0)
                        ((UnionType)unionType).setHasFacets(true);
                }
                return unionType;
            }
        }
        return datatype;
    }
    
    void findFacets(final SimpleRestriction r) {
        findFacets(r.getEnumerations());
        findFacets(r.getFractionDigits());
        findFacets(r.getLengths());
        findFacets(r.getMaxExclusives());
        findFacets(r.getMaxInclusives());
        findFacets(r.getMaxLengths());
        findFacets(r.getMinExclusives());
        findFacets(r.getMinInclusives());
        findFacets(r.getMinLengths());
        findFacets(r.getPatterns());
        findFacets(r.getTotalDigits());
        findFacets(r.getWhitespaces());
    }
    
    Datatype findPrimitive(Schema schema, String typeName) {
        Collection<GlobalSimpleType> gsts = schema.getSimpleTypes();
        for(GlobalSimpleType gst:gsts) {
            if(gst.getName().equals(typeName)) {
                return createDatatype(gst);
            }
        }
        return null;
    }
    
    void findFacets(Collection<? extends SchemaComponent> facets) {
        for(SchemaComponent facet:facets) {
            facet.accept(this);
        }
    }
    
    Datatype getDatatype() {
        return datatype;
    }
    
    private Datatype datatype;
    private SimpleRestriction facetParent;
    AXIModelImpl model;
}
