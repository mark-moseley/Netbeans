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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.xml.schema.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Notation;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Form;
import org.netbeans.modules.xml.schema.model.GlobalAttribute;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalGroup;
import org.netbeans.modules.xml.schema.model.Import;
import org.netbeans.modules.xml.schema.model.Include;
import org.netbeans.modules.xml.schema.model.Redefine;
import org.netbeans.modules.xml.schema.model.ReferenceableSchemaComponent;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.Schema.Block;
import org.netbeans.modules.xml.schema.model.Schema.Final;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelReference;
import org.netbeans.modules.xml.schema.model.visitor.SchemaVisitor;
import org.w3c.dom.Element;

/**
 * @author Vidhya Narayanan
 * @author Nam Nguyen
 */

public class SchemaImpl extends SchemaComponentImpl implements Schema {
    
    public static final String TNS = "tns"; //NOI18N
    
    private Component foreignParent;
    
    /** Creates a new instance of SchemaImpl */
    public SchemaImpl(SchemaModelImpl model) {
        this(model, createNewComponent(SchemaElements.SCHEMA, model));
    }
    
    public SchemaImpl(SchemaModelImpl model, Element e){
        super(model,e);
    }
    
    /**
     *
     *
     */
    public Class<? extends SchemaComponent> getComponentType() {
        return Schema.class;
    }
    
    public Collection<SchemaModelReference> getSchemaReferences() {
        return getChildren(SchemaModelReference.class);
    }
    
    /**
     * Visitor providing
     */
    public void accept(SchemaVisitor visitor) {
        visitor.visit(this);
    }
    
    public Collection<GlobalElement> getElements() {
        return getChildren(GlobalElement.class);
    }
    
    public void removeElement(GlobalElement element) {
        removeChild(ELEMENTS_PROPERTY, element);
    }
    
    public void addElement(GlobalElement element) {
        appendChild(ELEMENTS_PROPERTY, element);
    }
    
    public Collection<GlobalAttributeGroup> getAttributeGroups() {
        return getChildren(GlobalAttributeGroup.class);
    }
    
    public void removeAttributeGroup(GlobalAttributeGroup group) {
        removeChild(ATTRIBUTE_GROUPS_PROPERTY, group);
    }
    
    public void addAttributeGroup(GlobalAttributeGroup group) {
        appendChild(ATTRIBUTE_GROUPS_PROPERTY, group);
    }
    
    public void removeExternalReference(SchemaModelReference ref) {
        removeChild(SCHEMA_REFERENCES_PROPERTY, ref);
    }
    
    public void addExternalReference(SchemaModelReference ref) {
        List<Class<? extends SchemaComponent>> afterList = new ArrayList<Class<? extends SchemaComponent>>();
        afterList.add(Annotation.class);
        afterList.add(SchemaModelReference.class);
        addAfter(SCHEMA_REFERENCES_PROPERTY, ref, afterList);
    }
    
    public Collection<GlobalComplexType> getComplexTypes() {
        return getChildren(GlobalComplexType.class);
    }
    
    public void removeComplexType(GlobalComplexType type) {
        removeChild(COMPLEX_TYPES_PROPERTY, type);
    }
    
    public void addComplexType(GlobalComplexType type) {
        appendChild(COMPLEX_TYPES_PROPERTY, type);
    }
    
    public Collection<GlobalAttribute> getAttributes() {
        return getChildren(GlobalAttribute.class);
    }
    
    public void addAttribute(GlobalAttribute attr) {
        appendChild(ATTRIBUTES_PROPERTY, attr);
    }
    
    public void removeAttribute(GlobalAttribute attr) {
        removeChild(ATTRIBUTES_PROPERTY, attr);
    }
    
    public void setVersion(String ver) {
        setAttribute(VERSION_PROPERTY, SchemaAttributes.VERSION, ver);
    }
    
    public String getVersion() {
        return getAttribute(SchemaAttributes.VERSION);
    }
    
    public void setLanguage(String language) {
        setAttribute(LANGUAGE_PROPERTY, SchemaAttributes.LANGUAGE, language);
    }
    
    public String getLanguage() {
        return getAttribute(SchemaAttributes.LANGUAGE);
    }
    
    public void setFinalDefault(Set<Final> finalDefault) {
        setAttribute(FINAL_DEFAULT_PROPERTY, SchemaAttributes.FINAL_DEFAULT,
                finalDefault == null ? null :
                    Util.convertEnumSet(Final.class, finalDefault));
    }
    
    public Set<Final> getFinalDefault() {
        String s = getAttribute(SchemaAttributes.FINAL_DEFAULT);
        return s == null ? null : Util.valuesOf(Final.class, s);
    }
    
    public Set<Final> getFinalDefaultEffective() {
        Set<Final> v = getFinalDefault();
        return v == null ? getFinalDefaultDefault() : v;
    }
    
    public Set<Final> getFinalDefaultDefault() {
        return new DerivationsImpl.DerivationSet<Final>();
    }
    
    public void setTargetNamespace(String uri) {
        String currentTargetNamespace = getTargetNamespace();
        setAttribute(TARGET_NAMESPACE_PROPERTY, SchemaAttributes.TARGET_NS, uri);
        ensureValueNamespaceDeclared(uri, currentTargetNamespace, TNS);
    }
    
    public String getTargetNamespace() {
        return getAttribute(SchemaAttributes.TARGET_NS);
    }
    
    public void setElementFormDefault(Form form) {
        setAttribute(ELEMENT_FORM_DEFAULT_PROPERTY, SchemaAttributes.ELEM_FORM_DEFAULT, form);
    }
    
    public Form getElementFormDefault() {
        String s = getAttribute(SchemaAttributes.ELEM_FORM_DEFAULT);
        return s == null ? null : Util.parse(Form.class, s);
    }
    
    public void setAttributeFormDefault(Form form) {
        setAttribute(ATTRIBUTE_FORM_DEFAULT_PROPERTY, SchemaAttributes.ATTR_FORM_DEFAULT, form);
    }
    
    public Form getAttributeFormDefault() {
        String s = getAttribute(SchemaAttributes.ATTR_FORM_DEFAULT);
        return s == null ? null : Util.parse(Form.class, s);
    }
    
    public Collection<GlobalSimpleType> getSimpleTypes() {
        return getChildren(GlobalSimpleType.class);
    }
    
    public void removeSimpleType(GlobalSimpleType type) {
        removeChild(SIMPLE_TYPES_PROPERTY, type);
    }
    
    public void addSimpleType(GlobalSimpleType type) {
        appendChild(SIMPLE_TYPES_PROPERTY, type);
    }
    
    public Collection<GlobalGroup> getGroups() {
        return getChildren(GlobalGroup.class);
    }
    
    public void removeGroup(GlobalGroup group) {
        removeChild(GROUPS_PROPERTY, group);
    }
    
    public void addGroup(GlobalGroup group) {
        appendChild(GROUPS_PROPERTY, group);
    }
    
    public Collection<Notation> getNotations() {
        return getChildren(Notation.class);
    }
    
    public void removeNotation(Notation notation) {
        removeChild(NOTATIONS_PROPERTY, notation);
    }
    
    public void addNotation(Notation notation) {
        appendChild(NOTATIONS_PROPERTY, notation);
    }
    
    public void setBlockDefault(Set<Block> blockDefault) {
        setAttribute(BLOCK_DEFAULT_PROPERTY, SchemaAttributes.BLOCK_DEFAULT,
                blockDefault == null ? null :
                    Util.convertEnumSet(Block.class, blockDefault));
    }
    
    public Set<Block> getBlockDefault() {
        String s = getAttribute(SchemaAttributes.BLOCK_DEFAULT);
        return s == null ? null : Util.valuesOf(Block.class, s);
    }
    
    public Set<Block> getBlockDefaultEffective() {
        Set<Block> v = getBlockDefault();
        return v == null ? getBlockDefaultDefault() : v;
    }
    
    public Set<Block> getBlockDefaultDefault() {
        return new DerivationsImpl.DerivationSet<Block>();
    }
    
    public Form getElementFormDefaultEffective() {
        Form v = getElementFormDefault();
        return v == null ? getElementFormDefaultDefault() : v;
    }
    
    public Form getElementFormDefaultDefault() {
        return Form.UNQUALIFIED;
    }
    
    public Form getAttributeFormDefaultEffective() {
        Form v = getAttributeFormDefault();
        return v == null ? getAttributeFormDefaultDefault() : v;
    }
    
    public Form getAttributeFormDefaultDefault() {
        return Form.UNQUALIFIED;
    }
    
    public Collection<Redefine> getRedefines() {
        return getChildren(Redefine.class);
    }
    
    public Collection<Include> getIncludes() {
        return getChildren(Include.class);
    }
    
    public Collection<Import> getImports() {
        return getChildren(Import.class);
    }
    
    public Collection<GlobalElement> findAllGlobalElements() {
        Collection<GlobalElement> result = new ArrayList<GlobalElement>();
        Collection<GlobalElement> tempCollection = this.getElements();
        if(tempCollection != null) {
            result.addAll(tempCollection);
        }
	// TODO need to add redefined elements to search
        result.addAll(getExternalGlobalElements(getImports()));
        result.addAll(getExternalGlobalElements(getIncludes()));
        result.addAll(getExternalGlobalElements(getRedefines()));
        return result;
    }
    
    private Collection<GlobalElement> getExternalGlobalElements(
	Collection<? extends SchemaModelReference> externalRefs){
	
	Collection<GlobalElement> result = new ArrayList<GlobalElement>();
	for(SchemaModelReference smr : externalRefs) {
	    try {
		SchemaModel sm = smr.resolveReferencedModel();
		if (sm.getState().equals(SchemaModel.State.VALID)) {
		    result.addAll(sm.getSchema().findAllGlobalElements());
		}
	    } catch (CatalogModelException ex) {
		// we are swalling this exception as the model cannot be found
		// we still want to continue to try and find the reference though
	    }
	}
	return result;
    }
    
    public Collection<GlobalType> findAllGlobalTypes() {
        Collection<GlobalType> result = new ArrayList<GlobalType>();
        //add all SimpleTypes
        Collection<? extends GlobalType> tempCollection = this.getSimpleTypes();
        if(tempCollection != null)
            result.addAll(tempCollection);
        //add all complex types
        tempCollection = this.getComplexTypes();
        if(tempCollection != null)
            result.addAll(tempCollection);
        //add from all the referenced docs
        result.addAll(getExternalGlobalTypes(getImports()));
        result.addAll(getExternalGlobalTypes(getIncludes()));
        result.addAll(getExternalGlobalTypes(getRedefines()));
        return result;
    }
    
    private Collection<GlobalType> getExternalGlobalTypes(
	Collection<? extends SchemaModelReference> externalRefs){
	
        Collection<GlobalType> result = new ArrayList<GlobalType>();
        for(SchemaModelReference smr : externalRefs){
	    try {
		SchemaModel sm = smr.resolveReferencedModel();
		if (sm.getState().equals(SchemaModel.State.VALID)) {
		    result.addAll(sm.getSchema().findAllGlobalTypes());
		}
	    } catch (CatalogModelException ex) {
		// swallow this exception to allow some resolution to occur
	    }
	}
        return result;
    }

    public Component getForeignParent() {
        return foreignParent;
    }

    public void setForeignParent(Component component) {
        foreignParent = component;
    }
}
