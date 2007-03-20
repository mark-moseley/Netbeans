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
package org.netbeans.modules.vmd.midp.components.general;

import org.netbeans.modules.vmd.api.codegen.*;
import org.netbeans.modules.vmd.api.model.*;
import org.netbeans.modules.vmd.midp.components.MidpTypes;

import javax.swing.text.StyledDocument;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author David Kaspar
 */
public class ClassCode {
    
    private static final String ARRAY_SUFFIX = "Array"; // NOI18N
    private static final String GETTER_PREFIX = "get"; // NOI18N
    
    static final class ClassCodeReferencePresenter extends CodeReferencePresenter {
        
        ClassCodeReferencePresenter() {
        }
        
        protected String generateAccessCode() {
            DesignComponent component = getComponent();
            boolean lazyInit = MidpTypes.getBoolean(component.readProperty(ClassCD.PROP_LAZY_INIT));
            String instanceName = MidpTypes.getString(component.readProperty(ClassCD.PROP_INSTANCE_NAME));
            return lazyInit ? createGetterNameFromInstanceName(instanceName) + " ()" : instanceName;
        }
        
        protected String generateDirectAccessCode() {
            return MidpTypes.getString(getComponent().readProperty(ClassCD.PROP_INSTANCE_NAME));
        }
        
        protected String generateTypeCode() {
            return MidpTypes.getSimpleClassName(getComponent().getType());
        }
        
    }
    
    /**
     * Requires CodeReferencePresenter on the same component.
     */
    static final class CodeLazyInitPresenter extends CodeClassLevelPresenter {
        
        CodeLazyInitPresenter() {
        }
        
        protected void generateFieldSectionCode(MultiGuardedSection section) {
            section.getWriter().write("private " + CodeReferencePresenter.generateTypeCode(getComponent()) + " " + CodeReferencePresenter.generateDirectAccessCode(getComponent()) + ";\n");
        }
        
        protected void generateMethodSectionCode(MultiGuardedSection section) {
        }
        
        public void generateInitializeSectionCode(MultiGuardedSection section) {
            if (ClassSupport.isLazyInitialized(getComponent()))
                return;
            InitCodeGenerator.generateInitializationCode(section, getComponent());
        }
        
        protected void generateClassBodyCode(StyledDocument document) {
            if (! ClassSupport.isLazyInitialized(getComponent()))
                return;
            MultiGuardedSection section = MultiGuardedSection.create(document, getComponent().getComponentID() + "-getter");// NOI18N
            
            section.getWriter().write("public " + CodeReferencePresenter.generateTypeCode(getComponent()) + " " + CodeReferencePresenter.generateAccessCode(getComponent()) + " {\n" // NOI18N
                    + "if (" + CodeReferencePresenter.generateDirectAccessCode(getComponent()) + " == null) {\n").commit(); // NOI18N
            
            section.switchToEditable(getComponent().getComponentID() + "-preInit");
            section.getWriter().write(" // write pre-init user code here\n").commit(); // NOI18N
            
            section.switchToGuarded();
            InitCodeGenerator.generateInitializationCode(section, getComponent());
            section.getWriter().commit();
            
            section.switchToEditable(getComponent().getComponentID() + "-postInit");
            section.getWriter().write(" // write post-init user code here\n").commit(); // NOI18N
            
            section.switchToGuarded();
            section.getWriter().write("}\n"
                    + "return " + CodeReferencePresenter.generateDirectAccessCode(getComponent()) + ";\n" // NOI18N
                    + "}\n").commit(); // NOI18N
            
            section.close();
        }
        
    }
    
    static class CodeClassComponentDependencyPresenter extends RootCode.CodeComponentDependencyPresenter {
        
        protected void collectRequiredComponents(Collection<DesignComponent> requiredComponents) {
            DesignComponent component = getComponent();
            ComponentDescriptor descriptor = component.getComponentDescriptor();
            if (descriptor != null)
                for (PropertyDescriptor property : descriptor.getPropertyDescriptors())
                    Debug.collectAllComponentReferences(component.readProperty(property.getName()), requiredComponents);
        }
    }
    
    static class CodeClassNamePresenter extends CodeNamePresenter {
        
        public List<String> getReservedNames() {
            return getReservedNamesFor(MidpTypes.getString(getComponent().readProperty(ClassCD.PROP_INSTANCE_NAME)));
        }
        
        public List<String> getReservedNamesFor(String suggestedMainName) {
            return Arrays.asList(suggestedMainName, createGetterNameFromInstanceName(suggestedMainName));
        }
        
    }
    
    private static String createGetterNameFromInstanceName(String instanceName) {
        if (instanceName == null || instanceName.length() < 0)
            return null;
        return GETTER_PREFIX + Character.toUpperCase(instanceName.charAt(0)) + instanceName.substring(1);
    }
    
    static String getSuggestedMainName(DesignComponent component) {
        TypeID type = component.getType();
        String instanceName = type.getString();
        int index = instanceName.lastIndexOf('.');
        if (index >= 0)
            instanceName = instanceName.substring(index + 1);
        if (type.getDimension() > 0)
            instanceName += ARRAY_SUFFIX;
        char[] chars = instanceName.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        for (int i = 0 ; i < chars.length ; i++ ) {
            if (Character.isLetter(chars[i]) && !Character.isLowerCase(chars[i])) {
                if (chars.length >= i + 1 && !Character.isLowerCase(chars[i+1]))
                    chars[i] = Character.toLowerCase(chars[i]);
                else
                    break;
            }
        }
        instanceName = String.copyValueOf(chars);
        return instanceName;
    }
    
}
