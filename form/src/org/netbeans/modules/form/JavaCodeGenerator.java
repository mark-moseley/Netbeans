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

package org.netbeans.modules.form;

import javax.swing.JEditorPane;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.text.IndentEngine;

import org.netbeans.api.editor.fold.*;

import org.netbeans.api.java.classpath.ClassPath;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.swing.JEditorPane;
import org.netbeans.api.editor.guards.InteriorSection;
import org.netbeans.api.editor.guards.SimpleSection;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

import org.netbeans.modules.form.editors.ModifierEditor;
import org.netbeans.modules.form.editors.CustomCodeEditor;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.layoutsupport.LayoutSupportManager;
import org.netbeans.modules.form.layoutdesign.LayoutComponent;
import org.netbeans.modules.form.layoutdesign.support.SwingLayoutCodeGenerator;

import java.awt.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.*; 
import java.util.*;

/**
 * JavaCodeGenerator is the default code generator which produces a Java source
 * for the form.
 *
 * @author Ian Formanek, Jan Stola
 */

class JavaCodeGenerator extends CodeGenerator {

    static final String PROP_VARIABLE_MODIFIER = "modifiers"; // NOI18N
    static final String PROP_TYPE_PARAMETERS = "typeParameters"; // NOI18N
    static final String PROP_VARIABLE_LOCAL = "useLocalVariable"; // NOI18N
    static final String PROP_SERIALIZE_TO = "serializeTo"; // NOI18N
    static final String PROP_CODE_GENERATION = "codeGeneration"; // NOI18N
    static final String PROP_CREATE_CODE_PRE = "creationCodePre"; // NOI18N
    static final String PROP_CREATE_CODE_POST = "creationCodePost"; // NOI18N
    static final String PROP_CREATE_CODE_CUSTOM = "creationCodeCustom"; // NOI18N
    static final String PROP_INIT_CODE_PRE = "initCodePre"; // NOI18N
    static final String PROP_INIT_CODE_POST = "initCodePost"; // NOI18N
    static final String PROP_LISTENERS_POST = "listenersCodePost"; // NOI18N
    static final String PROP_ADDING_PRE = "addingCodePre"; // NOI18N
    static final String PROP_ADDING_POST = "addingCodePost"; // NOI18N
    static final String PROP_LAYOUT_PRE = "layoutCodePre"; // NOI18N
    static final String PROP_LAYOUT_POST = "layoutCodePost"; // NOI18N
    static final String PROP_ALL_SET_POST = "allCodePost"; // NOI18N
    static final String PROP_DECLARATION_PRE = "declarationPre"; // NOI18N
    static final String PROP_DECLARATION_POST = "declarationPost"; // NOI18N
    static final String PROP_GENERATE_MNEMONICS = "generateMnemonicsCode"; // Mnemonics support // NOI18N
    static final String PROP_LISTENER_GENERATION_STYLE = "listenerGenerationStyle"; // NOI18N

    static final String AUX_VARIABLE_MODIFIER =
        "JavaCodeGenerator_VariableModifier"; // NOI18N
    static final String AUX_TYPE_PARAMETERS =
        "JavaCodeGenerator_TypeParameters"; // NOI18N
    static final String AUX_VARIABLE_LOCAL =
        "JavaCodeGenerator_VariableLocal"; // NOI18N
    static final String AUX_SERIALIZE_TO =
        "JavaCodeGenerator_SerializeTo"; // NOI18N
    static final String AUX_CODE_GENERATION =
        "JavaCodeGenerator_CodeGeneration"; // NOI18N
    static final String AUX_CREATE_CODE_PRE =
        "JavaCodeGenerator_CreateCodePre"; // NOI18N
    static final String AUX_CREATE_CODE_POST =
        "JavaCodeGenerator_CreateCodePost"; // NOI18N
    static final String AUX_CREATE_CODE_CUSTOM =
        "JavaCodeGenerator_CreateCodeCustom"; // NOI18N
    static final String AUX_INIT_CODE_PRE =
        "JavaCodeGenerator_InitCodePre"; // NOI18N
    static final String AUX_INIT_CODE_POST =
        "JavaCodeGenerator_InitCodePost"; // NOI18N
    static final String AUX_LISTENERS_POST =
        "JavaCodeGenerator_ListenersCodePost"; // NOI18N
    static final String AUX_ADDING_PRE =
        "JavaCodeGenerator_AddingCodePre"; // NOI18N
    static final String AUX_ADDING_POST =
        "JavaCodeGenerator_AddingCodePost"; // NOI18N
    static final String AUX_LAYOUT_PRE =
        "JavaCodeGenerator_LayoutCodePre"; // NOI18N
    static final String AUX_LAYOUT_POST =
        "JavaCodeGenerator_LayoutCodePost"; // NOI18N
    static final String AUX_ALL_SET_POST =
        "JavaCodeGenerator_allCodePost"; // NOI18N
    static final String AUX_DECLARATION_PRE =
        "JavaCodeGenerator_DeclarationPre"; // NOI18N
    static final String AUX_DECLARATION_POST =
        "JavaCodeGenerator_DeclarationPost"; // NOI18N

    static final Integer VALUE_GENERATE_CODE = new Integer(0);
    static final Integer VALUE_SERIALIZE = new Integer(1);

    // types of code generation of event listeners
    static final int ANONYMOUS_INNERCLASSES = 0;
    static final int CEDL_INNERCLASS = 1;
    static final int CEDL_MAINCLASS = 2;

    // types of code generation of layout code
    static final int LAYOUT_CODE_AUTO = 0;
    static final int LAYOUT_CODE_JDK6 = 1;
    static final int LAYOUT_CODE_LIBRARY = 2;

    private static final String EVT_SECTION_PREFIX = "event_"; // NOI18N

    private static final String DEFAULT_LISTENER_CLASS_NAME = "FormListener"; // NOI18N

    static final String CUSTOM_CODE_MARK = "\u001F"; // NOI18N
    private static final String CODE_MARK = "*/\n\\"; // NOI18N
    private static final String MARKED_PROPERTY_CODE = "*/\n\\0"; // NOI18N
    private static final String PROPERTY_LINE_COMMENT = "*/\n\\1"; // NOI18N

    private static final String RESOURCE_BUNDLE_OPENING_CODE = "java.util.ResourceBundle.getBundle("; // NOI18N
    private static final String RESOURCE_BUNDLE_CLOSING_CODE = ")."; // NOI18N
    private Map<String,String> bundleVariables;

    private static Class bindingContextClass = javax.beans.binding.BindingContext.class;
    private String bindingContextVariable;
    private Map<String,String> bindingVariables;
    private static String variablesHeader;
    private static String variablesFooter;
    private static String eventDispatchCodeComment;

    /** The FormLoaderSettings instance */
    private static FormLoaderSettings formSettings = FormLoaderSettings.getInstance();

    private FormModel formModel;
    private FormEditorSupport formEditorSupport;

    private boolean initialized = false;
    private boolean canGenerate = true;
    private boolean codeUpToDate = true;

    private String listenerClassName;
    private String listenerVariableName;

    // data needed when listener generation style is CEDL_MAINCLASS
    private Class[] listenersInMainClass;
    private Class[] listenersInMainClass_lastSet;

    private int emptyLineCounter;
    private int emptyLineRequest;

    private Map constructorProperties;
    private Map<RADComponent, java.util.List<FormProperty>> parentDependentProperties;
    private Map<RADComponent, java.util.List<FormProperty>> childrenDependentProperties;

    private SwingLayoutCodeGenerator swingGenerator;

    private static class PropertiesFilter implements FormProperty.Filter {
		
	private final java.util.List properties;
	
	public PropertiesFilter(java.util.List properties) {
	    this.properties = properties;
	}
	
        public boolean accept(FormProperty property) {	    		     
	    return (property.isChanged()
                       && !ResourceSupport.isInjectedProperty(property)
                       && (properties == null
                           || !properties.contains(property)))
                    || property.getPreCode() != null
                    || property.getPostCode() != null;
        }
    };

    /** Creates new JavaCodeGenerator */

//    public JavaCodeGenerator() {
//    }

    public void initialize(FormModel formModel) {
        if (!initialized) {
            this.formModel = formModel;
            FormDataObject formDO = FormEditor.getFormDataObject(formModel);
            formEditorSupport = formDO.getFormEditorSupport();

            if (formDO.getPrimaryFile().canWrite()) {
                canGenerate = true;
                formModel.addFormModelListener(new FormListener());
            }
            else canGenerate = false;

            SimpleSection initComponentsSection = formEditorSupport.getInitComponentSection();
            SimpleSection variablesSection = formEditorSupport.getVariablesSection();

            if (initComponentsSection == null || variablesSection == null) {
                System.err.println("ERROR: Cannot initialize guarded sections... code generation is disabled."); // NOI18N
                canGenerate = false;
            }

            initialized = true;
        }
    }

    /**
     * Alows the code generator to provide synthetic properties for specified
     * component which are specific to the code generation method.  E.g. a
     * JavaCodeGenerator will return variableName property, as it generates
     * global Java variable for every component
     * @param component The RADComponent for which the properties are to be obtained
     */

    public Node.Property[] getSyntheticProperties(final RADComponent component) {
        ResourceBundle bundle = FormUtils.getBundle();
        java.util.List propList = new ArrayList();
        if (component == null) {
            propList.add(new VariablesModifierProperty());
            propList.add(new LocalVariablesProperty());
            propList.add(new GenerateMnemonicsCodeProperty());
            propList.add(new ListenerGenerationStyleProperty());
            propList.add(new LayoutCodeTargetProperty());
        } else if (component != formModel.getTopRADComponent()) {
            
            propList.add(createBeanClassNameProperty(component));
            
            propList.add(new PropertySupport.ReadWrite(
                RADComponent.PROP_NAME,
                String.class,
                bundle.getString("MSG_JC_VariableName"), // NOI18N
                bundle.getString("MSG_JC_VariableDesc")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof String))
                        throw new IllegalArgumentException();

                    component.rename((String)value);
                    component.getNodeReference().firePropertyChangeHelper(
                        RADComponent.PROP_NAME, null, null); // NOI18N
                }

                public Object getValue() {
                    return component.getName();
                }

                public boolean canWrite() {
                    return JavaCodeGenerator.this.canGenerate && !component.isReadOnly();
                }
            });

            final FormProperty modifProp = new FormProperty(
                PROP_VARIABLE_MODIFIER,
                Integer.class,
                bundle.getString("MSG_JC_VariableModifiers"), // NOI18N
                null)
            {
                public void setTargetValue(Object value) {
                    if (!(value instanceof Integer))
                        throw new IllegalArgumentException();

                    Object oldValue = getTargetValue();

                    CodeStructure codeStructure = formModel.getCodeStructure();
                    CodeExpression exp = component.getCodeExpression();
                    int varType = exp.getVariable().getType();
                    String varName = component.getName();

                    varType &= ~CodeVariable.ALL_MODIF_MASK;
                    varType |= ((Integer)value).intValue() & CodeVariable.ALL_MODIF_MASK;

                    if ((varType & CodeVariable.ALL_MODIF_MASK)
                            != (formModel.getSettings().getVariablesModifier()
                                & CodeVariable.ALL_MODIF_MASK))
                    {   // non-default value
                        component.setAuxValue(AUX_VARIABLE_MODIFIER,
                                new Integer(varType & CodeVariable.ALL_MODIF_MASK)); // value
                    }
                    else { // default value
                        varType = 0x30DF; // default
                        if (component.getAuxValue(AUX_VARIABLE_MODIFIER) != null) {
                            component.getAuxValues().remove(AUX_VARIABLE_MODIFIER);
                        }
                    }

                    String typeParameters = exp.getVariable().getDeclaredTypeParameters();
                    codeStructure.removeExpressionFromVariable(exp);
                    codeStructure.createVariableForExpression(
                                         exp, varType, typeParameters, varName);
                }

                public Object getTargetValue() {
                    Object val = component.getAuxValue(AUX_VARIABLE_MODIFIER);
                    if (val != null)
                        return val;

                    return new Integer(formModel.getSettings().getVariablesModifier());
                }

                public boolean supportsDefaultValue() {
                    return component.getAuxValue(AUX_VARIABLE_LOCAL) == null;
                }

                public Object getDefaultValue() {
                    return component.getAuxValue(AUX_VARIABLE_LOCAL) == null ?
                           new Integer(formModel.getSettings().getVariablesModifier()) : null;
                }

                protected void propertyValueChanged(Object old, Object current) {
                    super.propertyValueChanged(old, current);
                    if (isChangeFiring()) {
                        formModel.fireSyntheticPropertyChanged(
                            component, getName(), old, current);
                        if (component.getNodeReference() != null) {
                            component.getNodeReference().firePropertyChangeHelper(
                                getName(), null, null);
                        }
                    }
                }

                public boolean canWrite() {
                    return JavaCodeGenerator.this.canGenerate && !component.isReadOnly();
                }

                public PropertyEditor getExpliciteEditor() { // getPropertyEditor
                    Boolean local = (Boolean) component.getAuxValue(AUX_VARIABLE_LOCAL);
                    if (local == null)
                        local = Boolean.valueOf(formModel.getSettings().getVariablesLocal());
                    return Boolean.TRUE.equals(local) ?
                        new ModifierEditor(Modifier.FINAL)
                        :
                        new ModifierEditor(Modifier.PUBLIC
                                           | Modifier.PROTECTED
                                           | Modifier.PRIVATE
                                           | Modifier.STATIC
                                           | Modifier.FINAL
                                           | Modifier.TRANSIENT
                                           | Modifier.VOLATILE);
                }
            };
            modifProp.setShortDescription(bundle.getString("MSG_JC_VariableModifiersDesc")); // NOI18N
            propList.add(modifProp);
            
            final FormProperty paramTypesProp = new FormProperty(
                PROP_TYPE_PARAMETERS,
                String.class,
                bundle.getString("MSG_JC_TypeParameters"), // NOI18N
                null)
            {
                public void setTargetValue(Object value) {
                    if ((value != null) && !(value instanceof String))
                        throw new IllegalArgumentException();
                    
                    // PENDING check for syntax of the value
                    
                    component.setAuxValue(AUX_TYPE_PARAMETERS, value);

                    CodeStructure codeStructure = formModel.getCodeStructure();
                    CodeExpression exp = component.getCodeExpression();
                    int varType = exp.getVariable().getType();
                    String varName = component.getName();

                    codeStructure.removeExpressionFromVariable(exp);
                    codeStructure.createVariableForExpression(
                                         exp, varType, (String)value, varName);
                }

                public Object getTargetValue() {
                    Object value = component.getAuxValue(AUX_TYPE_PARAMETERS);
                    return (value == null) ? "" : value; // NOI18N
                }

                public boolean supportsDefaultValue() {
                    return true;
                }

                public Object getDefaultValue() {
                    return ""; // NOI18N
                }

                protected void propertyValueChanged(Object old, Object current) {
                    super.propertyValueChanged(old, current);
                    if (isChangeFiring()) {
                        formModel.fireSyntheticPropertyChanged(
                            component, getName(), old, current);
                        if (component.getNodeReference() != null) {
                            component.getNodeReference().firePropertyChangeHelper(
                                getName(), null, null);
                        }
                    }
                }

                public boolean canWrite() {
                    return JavaCodeGenerator.this.canGenerate && !component.isReadOnly();
                }

                public PropertyEditor getExpliciteEditor() {
                    // PENDING replace by property editor that is able to determine
                    // formal type parameters of this class and can offer you
                    // a nice visual customizer
                    return super.getExpliciteEditor();
                }
            };
            paramTypesProp.setShortDescription(bundle.getString("MSG_JC_TypeParametersDesc")); // NOI18N
            propList.add(paramTypesProp);

            FormProperty localProp = new FormProperty(
                PROP_VARIABLE_LOCAL,
                Boolean.TYPE,
                bundle.getString("MSG_JC_UseLocalVar"), // NOI18N
                null)
            {
                public void setTargetValue(Object value) {
                    if (!(value instanceof Boolean))
                        throw new IllegalArgumentException();

                    Boolean oldValue = (Boolean) getTargetValue();
//                    if (value.equals(oldValue)) return;

                    CodeStructure codeStructure = formModel.getCodeStructure();
                    CodeExpression exp = component.getCodeExpression();
                    int varType = exp.getVariable().getType();
                    String varName = component.getName();

                    varType &= CodeVariable.FINAL
                               | ~(CodeVariable.ALL_MODIF_MASK | CodeVariable.SCOPE_MASK);
                    if (Boolean.TRUE.equals(value))
                        varType |= CodeVariable.LOCAL;
                    else
                        varType |= CodeVariable.FIELD
                                   | formModel.getSettings().getVariablesModifier();

                    if (((varType & CodeVariable.LOCAL) != 0)
                            != (formModel.getSettings().getVariablesLocal()))
                    {   // non-default value
                        component.setAuxValue(AUX_VARIABLE_LOCAL, value);
                        try {
                            modifProp.setValue(new Integer(varType & CodeVariable.ALL_MODIF_MASK));
                        }
                        catch (Exception ex) { // should not happen
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        }
                    }
                    else { // default value
                        varType = 0x30DF; // default
                        if (component.getAuxValue(AUX_VARIABLE_LOCAL) != null) {
                            component.getAuxValues().remove(AUX_VARIABLE_LOCAL);
                        }
                        try {
                            modifProp.restoreDefaultValue();
                        }
                        catch (Exception ex) { // should not happen
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        }
                    }

                    String typeParameters = exp.getVariable().getDeclaredTypeParameters();
                    codeStructure.removeExpressionFromVariable(exp);
                    codeStructure.createVariableForExpression(
                                         exp, varType, typeParameters, varName);
                }

                public Object getTargetValue() {
                    Object val = component.getAuxValue(AUX_VARIABLE_LOCAL);
                    if (val != null)
                        return val;

                    return Boolean.valueOf(formModel.getSettings().getVariablesLocal());
                }

                public boolean supportsDefaultValue() {
                    return true;
                }

                public Object getDefaultValue() {
                    return Boolean.valueOf(formModel.getSettings().getVariablesLocal());
                }

                protected void propertyValueChanged(Object old, Object current) {
                    super.propertyValueChanged(old, current);
                    if (isChangeFiring()) {
                        formModel.fireSyntheticPropertyChanged(
                            component, getName(), old, current);
                        if (component.getNodeReference() != null) {
                            component.getNodeReference().firePropertyChangeHelper(
                                getName(), null, null);
                        }
                    }
                }

                public boolean canWrite() {
                    return JavaCodeGenerator.this.canGenerate && !component.isReadOnly();
                }
            };
            localProp.setShortDescription(bundle.getString("MSG_JC_UseLocalVarDesc")); // NOI18N
            propList.add(localProp);

            // Mnemonics support - start -
            if (javax.swing.JLabel.class.isAssignableFrom(component.getBeanClass())
                    || javax.swing.AbstractButton.class.isAssignableFrom(component.getBeanClass()))
                propList.add(new PropertySupport.ReadWrite(
                    PROP_GENERATE_MNEMONICS, 
                    Boolean.TYPE,
                    bundle.getString("PROP_GENERATE_MNEMONICS"), // NOI18N
                    bundle.getString("HINT_GENERATE_MNEMONICS2")) // NOI18N
                {
                    public void setValue(Object value) {
                        Object oldValue = getValue();
                        component.setAuxValue(PROP_GENERATE_MNEMONICS, value);
                        formModel.fireSyntheticPropertyChanged(
                            component, PROP_GENERATE_MNEMONICS, oldValue, value);
                        component.getNodeReference().firePropertyChangeHelper(
                            PROP_GENERATE_MNEMONICS, null, null); // NOI18N
                    }

                    public Object getValue() {
                        return isUsingMnemonics(component) ?
                               Boolean.TRUE : Boolean.FALSE;
                    }

                    public boolean canWrite() {
                        return JavaCodeGenerator.this.canGenerate && !component.isReadOnly();
                    }

                    public boolean supportsDefaultValue() {
                        return true;
                    }

                    public void restoreDefaultValue() {
                        setValue(null);
                    }
                });
            // Mnemonics support - end -

            propList.add(new CodeProperty(
                    component,
                    PROP_CREATE_CODE_CUSTOM, AUX_CREATE_CODE_CUSTOM,
                    bundle.getString("MSG_JC_CustomCreationCode"), // NOI18N
                    bundle.getString("MSG_JC_CustomCreationCodeDesc"))); // NOI18N

            propList.add(new CodeProperty(
                    component,
                    PROP_CREATE_CODE_PRE, AUX_CREATE_CODE_PRE,
                    bundle.getString("MSG_JC_PreCreationCode"), // NOI18N
                    bundle.getString("MSG_JC_PreCreationCodeDesc"))); // NOI18N
            propList.add(new CodeProperty(
                    component,
                    PROP_CREATE_CODE_POST, AUX_CREATE_CODE_POST,
                    bundle.getString("MSG_JC_PostCreationCode"), // NOI18N
                    bundle.getString("MSG_JC_PostCreationCodeDesc"))); // NOI18N

            propList.add(new CodeProperty(
                    component,
                    PROP_INIT_CODE_PRE, AUX_INIT_CODE_PRE,
                    bundle.getString("MSG_JC_PreInitCode"), // NOI18N
                    bundle.getString("MSG_JC_PreInitCodeDesc"))); // NOI18N
            propList.add(new CodeProperty(
                    component,
                    PROP_INIT_CODE_POST, AUX_INIT_CODE_POST,
                    bundle.getString("MSG_JC_PostInitCode"), // NOI18N
                    bundle.getString("MSG_JC_PostInitCodeDesc"))); // NOI18N

            propList.add(new CodeProperty(
                    component,
                    PROP_LISTENERS_POST, AUX_LISTENERS_POST,
                    bundle.getString("MSG_JC_PostListenersCode"), // NOI18N
                    bundle.getString("MSG_JC_PostListenersCodeDesc"))); // NOI18N

            if (component.getParentComponent() != null) {
                propList.add(new CodeProperty(
                        component,
                        PROP_ADDING_PRE, AUX_ADDING_PRE,
                        bundle.getString("MSG_JC_PreAddCode"), // NOI18N
                        bundle.getString("MSG_JC_PreAddCodeDesc"))); // NOI18N
                propList.add(new CodeProperty(
                        component,
                        PROP_ADDING_POST, AUX_ADDING_POST,
                        bundle.getString("MSG_JC_PostAddCode"), // NOI18N
                        bundle.getString("MSG_JC_PostAddCodeDesc"))); // NOI18N
            }

            if (component instanceof ComponentContainer) {
                propList.add(new CodeProperty(
                        component,
                        PROP_LAYOUT_PRE, AUX_LAYOUT_PRE,
                        bundle.getString("MSG_JC_PrePopulationCode"), // NOI18N
                        bundle.getString("MSG_JC_PrePopulationCodeDesc"))); // NOI18N
                propList.add(new CodeProperty(
                        component,
                        PROP_LAYOUT_POST, AUX_LAYOUT_POST,
                        bundle.getString("MSG_JC_PostPopulationCode"), // NOI18N
                        bundle.getString("MSG_JC_PostPopulationCodeDesc"))); // NOI18N
            }

            propList.add(new CodeProperty(
                    component,
                    PROP_ALL_SET_POST, AUX_ALL_SET_POST,
                    bundle.getString("MSG_JC_AfterAllSetCode"), // NOI18N
                    bundle.getString("MSG_JC_AfterAllSetCodeDesc"))); // NOI18N

            propList.add(new CodeProperty(
                    component,
                    PROP_DECLARATION_PRE, AUX_DECLARATION_PRE,
                    bundle.getString("MSG_JC_PreDeclaration"), // NOI18N
                    bundle.getString("MSG_JC_PreDeclarationDesc"))); // NOI18N
            propList.add(new CodeProperty(
                    component,
                    PROP_DECLARATION_POST, AUX_DECLARATION_POST,
                    bundle.getString("MSG_JC_PostDeclaration"), // NOI18N
                    bundle.getString("MSG_JC_PostDeclarationDesc"))); // NOI18N

            propList.add(new PropertySupport.ReadWrite(
                PROP_CODE_GENERATION,
                Integer.TYPE,
                bundle.getString("MSG_JC_CodeGeneration"), // NOI18N
                bundle.getString("MSG_JC_CodeGenerationDesc")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof Integer))
                        throw new IllegalArgumentException();

                    Object oldValue = getValue();

                    if (!getDefaultValue().equals(value))
                        component.setAuxValue(AUX_CODE_GENERATION, value);
                    else if (component.getAuxValue(AUX_CODE_GENERATION) != null) {
                        component.getAuxValues().remove(AUX_CODE_GENERATION);
                    }

                    if (value.equals(VALUE_SERIALIZE)
                            && component.getAuxValue(AUX_SERIALIZE_TO) == null)
                        component.setAuxValue(AUX_SERIALIZE_TO,
                                              getDefaultSerializedName(component));

                    formModel.fireSyntheticPropertyChanged(
                        component, PROP_CODE_GENERATION, oldValue, value);
                    component.getNodeReference().firePropertyChangeHelper(
                        PROP_CODE_GENERATION, null, null); // NOI18N
                }

                public Object getValue() {
                    Object value = component.getAuxValue(AUX_CODE_GENERATION);
                    if (value == null)
                        value = getDefaultValue();
                    return value;
                }

                public boolean canWrite() {
                    return JavaCodeGenerator.this.canGenerate && !component.isReadOnly();
                }

                public PropertyEditor getPropertyEditor() {
                    return new CodeGenerateEditor(component);
                }

                private Object getDefaultValue() {
                    return component.hasHiddenState() ?
                                VALUE_SERIALIZE : VALUE_GENERATE_CODE;
                }
            });

            propList.add(new PropertySupport.ReadWrite(
                PROP_SERIALIZE_TO,
                String.class,
                bundle.getString("MSG_JC_SerializeTo"), // NOI18N
                bundle.getString("MSG_JC_SerializeToDesc")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof String))
                        throw new IllegalArgumentException();

                    Object oldValue = getValue();

                    if (!"".equals(value)) // NOI18N
                        component.setAuxValue(AUX_SERIALIZE_TO, value);
                    else if (component.getAuxValue(AUX_SERIALIZE_TO) != null) {
                        component.getAuxValues().remove(AUX_SERIALIZE_TO);
                    }

                    formModel.fireSyntheticPropertyChanged(
                        component, PROP_SERIALIZE_TO, oldValue, value);
                    component.getNodeReference().firePropertyChangeHelper(
                        PROP_SERIALIZE_TO, null, null); // NOI18N
                }

                public Object getValue() {
                    Object value = component.getAuxValue(AUX_SERIALIZE_TO);
                    if (value == null)
                        value = getDefaultSerializedName(component);
                    return value;
                }

                public boolean canWrite() {
                    return JavaCodeGenerator.this.canGenerate && !component.isReadOnly();
                }
            });
        } else if (component instanceof RADVisualComponent) {
            propList.add(new PropertySupport.ReadOnly(
                FormDesigner.PROP_DESIGNER_SIZE,
                Dimension.class,
                bundle.getString("MSG_DesignerSize"), // NOI18N
                bundle.getString("HINT_DesignerSize")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof Dimension))
                        throw new IllegalArgumentException();
                    if (!getDefaultValue().equals(value))
                        component.setAuxValue(FormDesigner.PROP_DESIGNER_SIZE, value);
                    else if (component.getAuxValue(FormDesigner.PROP_DESIGNER_SIZE) != null) {
                        component.getAuxValues().remove(FormDesigner.PROP_DESIGNER_SIZE);
                    }
                }

                public Object getValue() {
                    Object value = component.getAuxValue(FormDesigner.PROP_DESIGNER_SIZE);
                    if (value == null)
                        value = getDefaultValue();
                    return value;
                }

                private Object getDefaultValue() {
                    return new Dimension(400, 300);
                }
            });
        }

        Node.Property[] props = new Node.Property[propList.size()];
        propList.toArray(props);
        return props;
    }

    public static PropertySupport createBeanClassNameProperty(final RADComponent component) {
        final ResourceBundle bundle = FormUtils.getBundle();
        
        return new PropertySupport.ReadOnly(
                "beanClass", // NOI18N
                String.class,
                bundle.getString("MSG_JC_BeanClass"), // NOI18N
                bundle.getString("MSG_JC_BeanClassDesc")) // NOI18N
            {
		String invalid = null;
                public Object getValue() {
                    if(!component.isValid()) {
			if(invalid==null) {
			    invalid = bundle.getString("CTL_LB_InvalidComponent");  // NOI18N
			}
                        return component.getMissingClassName() + ": [" + invalid + "]"; // NOI18N
                    }
                    Class beanClass = component.getBeanClass();
                    if(beanClass!=null) {
                        return beanClass.toString();
                    }
                    return ""; // NOI18N
                }

                public boolean canWrite() {
                    return false;                    
                }     
                
                public PropertyEditor getPropertyEditor() {
                    return new PropertyEditorSupport(){};
                }
            };
    }    
    //
    // Private Methods
    //

    private String getDefaultSerializedName(RADComponent component) {
        return component.getFormModel().getName()
            + "_" + component.getName(); // NOI18N
    }

    void regenerateInitComponents() {
        if (!initialized || !canGenerate)
            return;

        // find indent engine to use or imitate
        IndentEngine indentEngine = IndentEngine.find(
                                        formEditorSupport.getDocument());

        final SimpleSection initComponentsSection = formEditorSupport.getInitComponentSection();
        int initComponentsOffset = initComponentsSection.getCaretPosition().getOffset();

        // create Writer for writing the generated code in
        StringWriter initCodeBuffer = new StringWriter(1024);
        CodeWriter initCodeWriter;
        if (formSettings.getUseIndentEngine()) { // use original indent engine
            initCodeWriter = new CodeWriter(
                    indentEngine.createWriter(formEditorSupport.getDocument(),
                                              initComponentsOffset,
                                              initCodeBuffer),
                    true);
        }
        else {
            initCodeWriter = new CodeWriter(initCodeBuffer, true);
        }
        // optimization - only properties need to go through CodeWriter
        Writer writer = initCodeWriter.getWriter();

        cleanup();

        try {
            boolean expandInitComponents = false;
            boolean foldGeneratedCode = formSettings.getFoldGeneratedCode();
            if (foldGeneratedCode) {
                String foldDescription = " " + FormUtils.getBundleString("MSG_GeneratedCode"); // NOI18N
                javax.swing.JEditorPane editorPane = formEditorSupport.getEditorPane();
                if (editorPane != null) {
                    FoldHierarchy foldHierarchy = FoldHierarchy.get(editorPane);
                    Fold fold = FoldUtilities.findNearestFold(foldHierarchy, initComponentsOffset);
                    expandInitComponents = (fold != null) && foldDescription.equals(fold.getDescription()) && !fold.isCollapsed();
                }
                writer.write("// <editor-fold defaultstate=\"collapsed\" desc=\""); // NOI18N
                writer.write(foldDescription);
                writer.write("\">\n"); // NOI18N
            }

            writer.write("private void initComponents() {\n"); // NOI18N

            addLocalVariables(writer);

            if (bindingContextVariable != null) {
                initCodeWriter.write(bindingContextVariable + " = new " + bindingContextClass.getName() + "();\n\n"); // NOI18N
            }

            emptyLineRequest++;
            Collection<RADComponent> otherComps = formModel.getOtherComponents();
            for (RADComponent metacomp : otherComps) {
                addCreateCode(metacomp, initCodeWriter);
            }
            RADComponent top = formModel.getTopRADComponent();
            addCreateCode(top, initCodeWriter);

            if (formModel.getSettings().getListenerGenerationStyle() == CEDL_INNERCLASS
                && anyEvents())
            {
                emptyLineRequest++;
                addDispatchListenerDeclaration(writer);
            }

            for (RADComponent metacomp : otherComps) {
                addInitCode(metacomp, initCodeWriter, null);
            }
            addInitCode(top, initCodeWriter, null);

            if (bindingContextVariable != null) {
                initCodeWriter.write("\n" + bindingContextVariable + ".bind();\n"); // NOI18N
            }

            generateFormSizeCode(writer);
            bindingContextVariable = null;
            bindingVariables = null;

            writer.write("}"); // no new line because of fold footer // NOI18N

            int listenerCodeStyle = formModel.getSettings().getListenerGenerationStyle();
            if ((listenerCodeStyle == CEDL_INNERCLASS
                  || listenerCodeStyle == CEDL_MAINCLASS)
                && anyEvents())
            {
                writer.write("\n\n"); // NOI18N
                writer.write(getEventDispatchCodeComment());
                writer.write("\n"); // NOI18N

                generateDispatchListenerCode(writer);
            }
            else listenersInMainClass = null;

            if (foldGeneratedCode) {
                writer.write("// </editor-fold>\n"); // NOI18N
            }
            else {
                writer.write("\n"); // NOI18N
            }
            writer.close();

             // set the text into the guarded block
            String newText = initCodeBuffer.toString();
            if (!formSettings.getUseIndentEngine()) {
                newText = indentCode(newText, 1, indentEngine);
            }
            initComponentsSection.setText(newText);
            
            if (expandInitComponents) {
                FoldHierarchy foldHierarchy = FoldHierarchy.get(formEditorSupport.getEditorPane());
                Fold fold = FoldUtilities.findNearestFold(foldHierarchy, initComponentsOffset);
                if (fold != null) {
                    foldHierarchy.expand(fold);
                }
            }
            clearUndo();
        }
        catch (IOException e) { // should not happen
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
        }

        cleanup();
    }

    private void cleanup() {
        emptyLineCounter = 0;
        emptyLineRequest = 0;
        if (constructorProperties != null)
            constructorProperties.clear();
        if (parentDependentProperties != null)
            parentDependentProperties.clear();
        if (childrenDependentProperties != null)
            childrenDependentProperties.clear();
        formModel.getCodeStructure().clearExternalVariableNames();
        bundleVariables = null;
        // preventive cleanup
        if (bindingContextVariable != null) { // we need to keep this variable registered
            bindingContextVariable = formModel.getCodeStructure().getExternalVariableName(
                    bindingContextClass, bindingContextVariable, true);
        }
    }

    private void regenerateVariables() {
        if (!initialized || !canGenerate)
            return;
        
        IndentEngine indentEngine = IndentEngine.find(
                                        formEditorSupport.getDocument());

        StringWriter variablesBuffer = new StringWriter(1024);
        CodeWriter variablesWriter;
        final SimpleSection variablesSection = formEditorSupport.getVariablesSection();

        if (formSettings.getUseIndentEngine()) {
            variablesWriter = new CodeWriter(
                    indentEngine.createWriter(formEditorSupport.getDocument(),
                                              variablesSection.getCaretPosition().getOffset(),
                                              variablesBuffer),
                    false);
        }
        else {
            variablesWriter = new CodeWriter(variablesBuffer, false);
        }
	    
        try {
	    variablesWriter.write(getVariablesHeaderComment());
            variablesWriter.write("\n"); // NOI18N

            addFieldVariables(variablesWriter);
            
            variablesWriter.write(getVariablesFooterComment());
            variablesWriter.write("\n"); // NOI18N
            variablesWriter.getWriter().close();

            String newText = variablesBuffer.toString();
            if (!formSettings.getUseIndentEngine())
                newText = indentCode(newText, 1, indentEngine);

            variablesSection.setText(newText);        
            clearUndo();
        }
        catch (IOException e) { // should not happen
            e.printStackTrace();
        }
    }   
    
    private void addCreateCode(RADComponent comp, CodeWriter initCodeWriter)
        throws IOException
    {
        if (comp == null)
            return;

        if (comp != formModel.getTopRADComponent()) {
            generateComponentCreate(comp, initCodeWriter, true, null);
        }
        if (comp instanceof ComponentContainer) {
            RADComponent[] children =((ComponentContainer)comp).getSubBeans();
            for (int i = 0; i < children.length; i++) {
                addCreateCode(children[i], initCodeWriter);
            }
        }
    }

    private void addInitCode(RADComponent comp,
                             CodeWriter initCodeWriter,
                             CustomCodeData codeData)
        throws IOException
    {
        if (comp == null)
            return;

        Writer writer = initCodeWriter.getWriter();

        int counter0 = emptyLineCounter;
        int request0 = emptyLineRequest;
        emptyLineRequest++;

        generateComponentProperties(comp, initCodeWriter, codeData);
        generateComponentEvents(comp, initCodeWriter, codeData);

        if (comp instanceof ComponentContainer) {
            boolean freeDesign = RADVisualContainer.isFreeDesignContainer(comp);
            ComponentContainer cont = (ComponentContainer) comp;
            if (!freeDesign) // layout and pre-population code before sub-components
                generateOldLayout(cont, initCodeWriter, codeData);

            if (codeData == null) { // normal code generation
                // generate code of sub-components
                RADComponent[] subBeans = cont.getSubBeans();
                for (RADComponent subcomp : subBeans) {
                    addInitCode(subcomp, initCodeWriter, null);
                }
                if (freeDesign) { // generate complete layout code
                    // GroupLayout setup code also adds all sub-components
                    RADVisualContainer visualCont = (RADVisualContainer) cont;
                    emptyLineRequest++;
                    generatePrePopulationCode(visualCont, writer, null);
                    emptyLineRequest++;
                    for (RADComponent subcomp : visualCont.getSubComponents()) {
                        generateComponentAddPre(subcomp, writer, null);
                    }
                    emptyLineRequest++;
                    generateFreeDesignLayoutCode(visualCont, initCodeWriter); // this always generates something
                    emptyLineRequest++;
                    // some code of sub-components is generated after adding
                    // them to the container (a11y, after-all-set)
                    for (RADComponent subcomp : visualCont.getSubComponents()) { // excluding menu
                        generateComponentAddPost(subcomp, initCodeWriter, null);
                        generateAccessibilityCode(subcomp, initCodeWriter, null);
                        generateInjectionCode(subcomp, writer, null);
                        generateAfterAllSetCode(subcomp, writer, null);
                    }
                    emptyLineRequest++;
                }
                else if (subBeans.length > 0)
                    emptyLineRequest++; // empty line after sub-components
            }
            else { // build code data for editing
                if (RADVisualContainer.isFreeDesignContainer(comp)) {
                    String substCode = "// " + FormUtils.getBundleString("CustomCode-SubstSub"); // NOI18N
                    codeData.addGuardedBlock(substCode);
                    generatePrePopulationCode(comp, writer, codeData);
                    substCode = "// " + FormUtils.getBundleString("CustomCode-SubstLayout"); // NOI18N
                    codeData.addGuardedBlock(substCode);
                }
                else { // with LM, the pre-layout code is elsewhere (before properties)
                    String substCode = "// " + FormUtils.getBundleString("CustomCode-SubstSubAndLayout"); // NOI18N
                    codeData.addGuardedBlock(substCode);
                }
            }

            int counter1 = emptyLineCounter;
            emptyLineRequest++;
            generatePostPopulationCode(comp, initCodeWriter, codeData);
            if (emptyLineCounter == counter1)
                emptyLineRequest--; // no post-population code, don't force empty line
            else
                emptyLineRequest++; // force empty line after post-population
        }

        if (emptyLineCounter == counter0)
            emptyLineRequest = request0; // no code was generated, don't force empty line

        if (!RADVisualContainer.isInFreeDesign(comp)) { // in container with LM, or menu component
            // add to parent container (if not root itself)
            generateComponentAddCode(comp, initCodeWriter, codeData);
            boolean endingCode = false;
            if (generateAccessibilityCode(comp, initCodeWriter, codeData))
                endingCode = true;
            if (generateInjectionCode(comp, writer, codeData))
                endingCode = true;
            if (generateAfterAllSetCode(comp, writer, codeData))
                endingCode = true;
            if (endingCode)
                emptyLineRequest++; // force empty line after
        }
        else if (codeData != null) { // build code data for editing
            // In free design this is generated with parent container (see above).
            // But building code data is invoked only for the component itself,
            // not for its parent, so we must do it here.
            generateComponentAddPre(comp, writer, codeData);
            String substCode = "// " + FormUtils.getBundleString("CustomCode-SubstAdding"); // NOI18N
            codeData.addGuardedBlock(substCode);
            generateComponentAddPost(comp, initCodeWriter, codeData);
            generateAccessibilityCode(comp, initCodeWriter, codeData);
            generateInjectionCode(comp, writer, codeData);
            generateAfterAllSetCode(comp, writer, codeData);
        }
    }

    private void generateOldLayout(ComponentContainer cont,
                                   CodeWriter initCodeWriter,
                                   CustomCodeData codeData)
        throws IOException
    {
        RADVisualContainer visualCont = cont instanceof RADVisualContainer ?
                                        (RADVisualContainer) cont : null;
        LayoutSupportManager layoutSupport = visualCont != null ?
                                             visualCont.getLayoutSupport() : null;

        if (layoutSupport != null) { // setLayout code for old layout support
            if (layoutSupport.isLayoutChanged()) {
                Iterator it = layoutSupport.getLayoutCode().getStatementsIterator();
                if (codeData == null && it.hasNext())
                    generateEmptyLineIfNeeded(initCodeWriter.getWriter());
                while (it.hasNext()) {
                    CodeStatement statement = (CodeStatement) it.next();
                    initCodeWriter.write(getStatementJavaString(statement, "")); // NOI18N
                    initCodeWriter.write("\n"); // NOI18N
                }

                if (codeData != null) { // build code data for editing
                    String code = indentCode(initCodeWriter.extractString());
                    codeData.addGuardedBlock(code);
                }
            }
        }

        generatePrePopulationCode((RADComponent)cont, initCodeWriter.getWriter(), codeData);
    }

    private boolean generateAfterAllSetCode(RADComponent comp,
                                            Writer writer,
                                            CustomCodeData codeData)
        throws IOException
    {
        boolean generated = false;

        String postCode = (String) comp.getAuxValue(AUX_ALL_SET_POST);
        if (codeData != null) { // build code data for editing
            codeData.addEditableBlock(postCode,
                                      (FormProperty) comp.getSyntheticProperty(PROP_ALL_SET_POST),
                                      0,
                                      FormUtils.getBundleString("CustomCode-AfterAllSet"), // NOI18N
                                      FormUtils.getBundleString("MSG_JC_PostPopulationCodeDesc")); // NOI18N
        }
        // normal code generation
        else if (postCode != null && !postCode.equals("")) { // NOI18N
            generateEmptyLineIfNeeded(writer);
            writer.write(postCode);
            if (!postCode.endsWith("\n")) // NOI18N
                writer.write("\n"); // NOI18N
            generated = true;
        }

        return generated;
    }

    private void generateComponentCreate(RADComponent comp,
                                         CodeWriter initCodeWriter,
                                         boolean insideMethod, // if this for initComponents
                                         CustomCodeData codeData)
        throws IOException
    {
        if (comp instanceof RADMenuItemComponent
            && ((RADMenuItemComponent)comp).getMenuItemType()
                   == RADMenuItemComponent.T_SEPARATOR)
        { // do not generate anything for AWT separator as it is not a real component
            return;
        }

        // optimization - only properties need to go through CodeWriter
        Writer writer = initCodeWriter.getWriter();

        CodeVariable var = comp.getCodeExpression().getVariable();
        int varType = var.getType();
        boolean localVariable = (varType & CodeVariable.SCOPE_MASK) == CodeVariable.LOCAL;

        if (insideMethod) {
            if (isFinalFieldVariable(varType))
                return; // is generated in field variables (here we are in initComponents)

            String preCode = (String) comp.getAuxValue(AUX_CREATE_CODE_PRE);
            if (codeData != null) { // build code data for editing
                codeData.addEditableBlock(preCode,
                                          (FormProperty) comp.getSyntheticProperty(PROP_CREATE_CODE_PRE),
                                          2, // preference index
                                          FormUtils.getBundleString("CustomCode-PreCreation"), // NOI18N
                                          FormUtils.getBundleString("MSG_JC_PreCreationCodeDesc")); // NOI18N
            }
            else if (preCode != null && !preCode.equals("")) { // NOI18N
                // normal generation of custom pre-creation code
                generateEmptyLineIfNeeded(writer);
                writer.write(preCode);
                if (!preCode.endsWith("\n")) // NOI18N
                    writer.write("\n"); // NOI18N
            }

            if (localVariable)
                generateDeclarationPre(comp, writer, codeData);
        }

        String customCode = null; // for code data editing
        boolean codeCustomized = false; // for code data editing

        Integer generationType = (Integer)comp.getAuxValue(AUX_CODE_GENERATION);
        if (comp.hasHiddenState() || VALUE_SERIALIZE.equals(generationType)) {
            // generate code for restoring serialized component [only works for field variables]
            if (!insideMethod)
                return;

            String serializeTo = (String)comp.getAuxValue(AUX_SERIALIZE_TO);
            if (serializeTo == null) {
                serializeTo = getDefaultSerializedName(comp);
                comp.setAuxValue(AUX_SERIALIZE_TO, serializeTo);
            }
            if (codeData == null)
                generateEmptyLineIfNeeded(writer);
            writer.write("try {\n"); // NOI18N
            writer.write(comp.getName());
            writer.write(" =("); // NOI18N
            writer.write(getSourceClassName(comp.getBeanClass()));
            writer.write(")java.beans.Beans.instantiate(getClass().getClassLoader(), \""); // NOI18N

            // write package name
            FileObject fo = formEditorSupport.getFormDataObject().getPrimaryFile();
            ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
            String packageName = cp.getResourceName(fo.getParent());
            if (!"".equals(packageName)) { // NOI18N
                writer.write(packageName + "."); // NOI18N
            }
            writer.write(serializeTo);
            writer.write("\");\n"); // NOI18N
            writer.write("} catch (ClassNotFoundException e) {\n"); // NOI18N
            writer.write("e.printStackTrace();\n"); // NOI18N
            writer.write("} catch (java.io.IOException e) {\n"); // NOI18N
            writer.write("e.printStackTrace();\n"); // NOI18N
            writer.write("}\n"); // NOI18N
        }
        else { // generate standard component creation code
            if (codeData == null)
                generateEmptyLineIfNeeded(writer);

            StringBuffer varBuf = new StringBuffer();

            if (localVariable || isFinalFieldVariable(varType)) { // also generate declaration
                varBuf.append(Modifier.toString(
                                varType & CodeVariable.ALL_MODIF_MASK));
                varBuf.append(" "); // NOI18N
                varBuf.append(getSourceClassName(comp.getBeanClass()));
                
                String typeParameters = var.getDeclaredTypeParameters();
                if ((typeParameters != null) && !"".equals(typeParameters)) { // NOI18N
                    varBuf.append(typeParameters);
                }

                varBuf.append(" "); // NOI18N
            }

            varBuf.append(var.getName());

            String customCreateCode = (String) comp.getAuxValue(AUX_CREATE_CODE_CUSTOM);
            if (customCreateCode != null && !customCreateCode.equals("")) { // NOI18N
                // there is a custom creation code provided
                if (codeData == null) { // normal code generation
                    writer.write(varBuf.toString());
                    writer.write(" = "); // NOI18N
                    writer.write(customCreateCode);
                    if (!customCreateCode.endsWith(";")) // NOI18N
                        writer.write(";"); // NOI18N
                    writer.write("\n"); // NOI18N
                }
                else { // build code data for editing
                    if (customCreateCode.endsWith(";")) // NOI18N
                        customCreateCode = customCreateCode.substring(0, customCreateCode.length()-1);
                    customCode = composeCustomCreationCode(varBuf, customCreateCode);
                    codeCustomized = true;
                }
            }
            if (customCreateCode == null || customCreateCode.equals("") || codeData != null) { // NOI18N
                // compose default creation code
                CreationDescriptor desc = CreationFactory.getDescriptor(
                                                              comp.getBeanClass());
                if (desc == null)
                    desc = new CreationDescriptor(comp.getBeanClass());

                CreationDescriptor.Creator creator =
                    desc.findBestCreator(comp.getKnownBeanProperties(),
                                         CreationDescriptor.CHANGED_ONLY);
                if (creator == null) // known properties are not enough...
                    creator = desc.findBestCreator(comp.getAllBeanProperties(),
                                               CreationDescriptor.CHANGED_ONLY);

                Class[] exceptions = creator.getExceptionTypes();
                if (insideMethod && needTryCode(exceptions)) {
                    if (localVariable) {
                        writer.write(varBuf.toString());
                        writer.write(";\n"); // NOI18N
                    }
                    writer.write("try {\n"); // NOI18N
                    writer.write(var.getName());
                }
                else {
                    writer.write(varBuf.toString());
                    exceptions = null;
                }

                writer.write(" = "); // NOI18N

                String[] propNames = creator.getPropertyNames();		
                FormProperty[] props;
                if (propNames.length > 0) {
                    if (constructorProperties == null)
                        constructorProperties = new HashMap();

		    java.util.List usedProperties = new ArrayList(propNames.length);
                    props = new FormProperty[propNames.length];

                    for (int i=0; i < propNames.length; i++) {
                        FormProperty prop = comp.getBeanProperty(propNames[i]);
                        props[i] = prop;
			usedProperties.add(prop);                        
                    }
		    constructorProperties.put(comp, usedProperties);
                }
                else props = RADComponent.NO_PROPERTIES;

                String defaultCreationCode = creator.getJavaCreationCode(props, null);
                initCodeWriter.write(defaultCreationCode + ";\n"); // NOI18N

                if (codeData != null && customCode == null) // get default custom code (without try/catch)
                    customCode = composeCustomCreationCode(varBuf, defaultCreationCode);

                if (exceptions != null)
                    generateCatchCode(exceptions, writer);
            }
        }
        if (codeData != null) {
            String defaultCode = indentCode(initCodeWriter.extractString());
            codeData.addGuardedBlock(defaultCode, customCode, CUSTOM_CODE_MARK, codeCustomized,
                                     (FormProperty) comp.getSyntheticProperty(PROP_CREATE_CODE_CUSTOM),
                                     FormUtils.getBundleString("CustomCode-Creation"), // NOI18N
                                     FormUtils.getBundleString("CustomCode-Creation_Hint")); // NOI18N
        }

        if (insideMethod) {
            if (localVariable)
                generateDeclarationPost(comp, writer, codeData);

            String postCode = (String) comp.getAuxValue(AUX_CREATE_CODE_POST);
            if (codeData != null) { // build code data for editing
                codeData.addEditableBlock(postCode,
                                          (FormProperty) comp.getSyntheticProperty(PROP_CREATE_CODE_POST),
                                          0, // preference index
                                          FormUtils.getBundleString("CustomCode-PostCreation"), // NOI18N
                                          FormUtils.getBundleString("MSG_JC_PostCreationCodeDesc")); // NOI18N
            }
            else if (postCode != null && !postCode.equals("")) { // NOI18N
                // normal generation of post-creation code
                writer.write(postCode);
                if (!postCode.endsWith("\n")) // NOI18N
                    writer.write("\n"); // NOI18N
            }
        }
    }

    // used only when building "code data" for editing
    private String composeCustomCreationCode(StringBuffer buf, String creationCode) {
        buf.append(" = "); // NOI18N
        buf.append(CUSTOM_CODE_MARK);
        buf.append(creationCode);
        buf.append(CUSTOM_CODE_MARK);
        buf.append(";\n"); // NOI18N
        return indentCode(buf.toString());
    }

    private void generateComponentProperties(RADComponent comp,
                                             CodeWriter initCodeWriter,
                                             CustomCodeData codeData)
        throws IOException
    {
        Writer writer = initCodeWriter.getWriter();

        String preCode = (String) comp.getAuxValue(AUX_INIT_CODE_PRE);
        if (codeData != null) { // build code data for editing
            codeData.addEditableBlock(preCode,
                                      (FormProperty) comp.getSyntheticProperty(PROP_INIT_CODE_PRE),
                                      10, // preference index
                                      FormUtils.getBundleString("CustomCode-PreInit"), // NOI18N
                                      FormUtils.getBundleString("MSG_JC_PreInitCodeDesc")); // NOI18N
        }
        else if (preCode != null && !preCode.equals("")) { // NOI18N
            generateEmptyLineIfNeeded(writer);
            writer.write(preCode);
            if (!preCode.endsWith("\n")) // NOI18N
                writer.write("\n"); // NOI18N
        }

        Object genType = comp.getAuxValue(AUX_CODE_GENERATION);
        if (!comp.hasHiddenState() 
                && (genType == null || VALUE_GENERATE_CODE.equals(genType)))
        {   // not serialized, generate properties
	    java.util.List usedProperties = constructorProperties != null ? (java.util.List)constructorProperties.get(comp) : null;
            Iterator it = comp.getBeanPropertiesIterator(new PropertiesFilter(usedProperties), false);
            while (it.hasNext()) {
                FormProperty prop = (FormProperty) it.next();

                java.util.List<FormProperty> depPropList = null;
                if (FormUtils.isMarkedParentDependentProperty(prop)) {
                    // needs to be generated after the component is added to the parent container
                    if (parentDependentProperties != null)
                        depPropList = parentDependentProperties.get(comp);
                    else {
                        parentDependentProperties = new HashMap();
                        depPropList = null;
                    }
                    if (depPropList == null) {
                        depPropList = new LinkedList();
                        parentDependentProperties.put(comp, depPropList);
                    }
                    depPropList.add(prop);
                }
                if (FormUtils.isMarkedChildrenDependentProperty(prop)) {
                    // needs to be added after all sub-components are added to this container
                    if (childrenDependentProperties != null)
                        depPropList = childrenDependentProperties.get(comp);
                    else {
                        childrenDependentProperties = new HashMap();
                        depPropList = null;
                    }
                    if (depPropList == null) {
                        depPropList = new LinkedList();
                        childrenDependentProperties.put(comp, depPropList);
                    }
                    depPropList.add(prop);
                }
                
                if (depPropList == null) { // independent property, generate here directly
                    generateProperty(prop, comp, null, initCodeWriter, codeData);
                }
            }
        }

        generateComponentBindings(comp, initCodeWriter);

        String postCode = (String) comp.getAuxValue(AUX_INIT_CODE_POST);
        if (codeData != null) { // build code data for editing
            codeData.addEditableBlock(postCode,
                                      (FormProperty) comp.getSyntheticProperty(PROP_INIT_CODE_POST),
                                      7, // preference index
                                      FormUtils.getBundleString("CustomCode-PostInit"), // NOI18N
                                      FormUtils.getBundleString("MSG_JC_PostInitCodeDesc")); // NOI18N
        }
        else if (postCode != null && !postCode.equals("")) { // NOI18N
            generateEmptyLineIfNeeded(writer);
            writer.write(postCode);
            if (!postCode.endsWith("\n")) // NOI18N
                writer.write("\n"); // NOI18N
        }
    }
    
    private void generateComponentBindings(RADComponent comp,
                                           CodeWriter initCodeWriter)
        throws IOException
    {
        boolean anyBinding = false;
        for (BindingProperty prop : comp.getKnownBindingProperties()) {
            MetaBinding bindingDef = (MetaBinding) prop.getValue();
            if (bindingDef != null) {
                if (!anyBinding) {
                    initCodeWriter.write("\n"); // NOI18N
                    anyBinding = true;
                }
                StringBuilder buf = new StringBuilder();
                Class descriptionType = BindingDesignSupport.getBindingDescriptionType(bindingDef);
                int updateStrategy = bindingDef.getUpdateStratedy();
                String variable = null;
                if (bindingDef.hasSubBindings()) {
                    variable = getBindingDescriptionVariable(descriptionType, buf);
                    buf.append(variable);
                    buf.append(" = new "); // NOI18N
                    buf.append(descriptionType.getName());
                    buf.append("("); // NOI18N
                    buildBindingParamsCode(bindingDef, buf);
                    buf.append(");\n"); // NOI18N
                    initCodeWriter.write(buf.toString());

                    for (MetaBinding sub : bindingDef.getSubBindings()) {
                        buf = new StringBuilder();
                        buf.append(variable);
                        buf.append(".addBinding("); // NOI18N
                        buf.append("\""); // NOI18N
                        buf.append(sub.getSourcePath());
                        buf.append("\", "); // NOI18N
                        buf.append(sub.getTargetPath());
                        Map parameters = sub.getParameters();
                        Iterator<Map.Entry> iter = parameters.entrySet().iterator();
                        while (iter.hasNext()) {
                            Map.Entry entry = iter.next();
                            buf.append(", "); // NOI18N
                            buf.append(entry.getKey());
                            buf.append(", "); // NOI18N
                            buf.append(entry.getValue());
                        }
                        buf.append(");\n"); // NOI18N
                        initCodeWriter.write(buf.toString());
                    }
                }
                else {
                    boolean useVariable = (updateStrategy != MetaBinding.UPDATE_STRATEGY_READ_WRITE)
                        || bindingDef.isIncompletePathValueSpecified()
                        || bindingDef.isNullValueSpecified()
                        || bindingDef.isConverterSpecified()
                        || bindingDef.isValidatorSpecified()
                        || bindingDef.isBindImmediately();
                    if (!useVariable) {
                        buf.append(bindingContextVariable);
                        buf.append(".addBinding("); // NOI18N                        
                    } else {
                        variable = getBindingDescriptionVariable(descriptionType, buf);
                        buf.append(variable);
                        buf.append(" = "); // NOI18N
                        buf.append("new "); // NOI18N
                        buf.append(descriptionType.getName());
                        buf.append("("); // NOI18N
                    }
                    buildBindingParamsCode(bindingDef, buf);
                    buf.append(");\n"); // NOI18N
                    initCodeWriter.write(buf.toString());
                }
                if (updateStrategy != MetaBinding.UPDATE_STRATEGY_READ_WRITE) {
                    initCodeWriter.write(variable + ".setUpdateStrategy(javax.beans.binding.Binding.UpdateStrategy."); // NOI18N
                    if (updateStrategy == MetaBinding.UPDATE_STRATEGY_READ_FROM_SOURCE) {
                        initCodeWriter.write("READ_FROM_SOURCE);\n"); // NOI18N
                    } else {
                        assert (updateStrategy == MetaBinding.UPDATE_STRATEGY_READ_ONCE);
                        initCodeWriter.write("READ_ONCE);\n"); // NOI18N
                    }
                }
                if (bindingDef.isNullValueSpecified()) {
                    generateComponentBinding0(initCodeWriter, prop.getNullValueProperty(), variable + ".setNullSourceValue"); // NOI18N
                }
                if (bindingDef.isIncompletePathValueSpecified()) {
                    generateComponentBinding0(initCodeWriter, prop.getIncompleteValueProperty(), variable + ".setValueForIncompleteSourcePath"); // NOI18N
                }
                if (bindingDef.isConverterSpecified()) {
                    generateComponentBinding0(initCodeWriter, prop.getConverterProperty(), variable + ".setConverter"); // NOI18N
                }
                if (bindingDef.isValidatorSpecified()) {
                    generateComponentBinding0(initCodeWriter, prop.getValidatorProperty(), variable + ".setValidator"); // NOI18N
                }
                if (bindingDef.isNameSpecified()) {
                    // PENDING
                }
                if (variable != null) {
                    // PENDING the following check should not be there - the binding should be always
                    // added to context, but BindingContext.bindingBecameBound() must be fixed before that
                    // e.g. bindingBecameBound() should contain unbound.remove(binding);
                    if (!bindingDef.isBindImmediately()) {
                        initCodeWriter.write(bindingContextVariable + ".addBinding(" + variable + ");\n"); // NOI18N
                    }
                    if (bindingDef.isBindImmediately()) {
                        initCodeWriter.write(variable + ".bind();"); // NOI18N
                    }
                }
            }
        }
        if (anyBinding) {
            initCodeWriter.write("\n"); // NOI18N
        }
    }

    private void generateComponentBinding0(CodeWriter initCodeWriter, FormProperty property, String method) throws IOException {
        try {
            Object value = property.getValue();
            if (value != null) {
                initCodeWriter.write(method + "(" + property.getJavaInitializationString() + ");\n"); // NOI18N
            }
        } catch (IllegalAccessException iaex) {
            iaex.printStackTrace();
        } catch (InvocationTargetException itex) {
            itex.printStackTrace();
        }
    }

    private String getBindingDescriptionVariable(Class descriptionType, StringBuilder buf) {
        String variable = null;
        if (bindingVariables == null)
            bindingVariables = new HashMap();
        else
            variable = bindingVariables.get(descriptionType.getName());

        if (variable == null) {
            String name = descriptionType.getSimpleName();
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
            variable = formModel.getCodeStructure().getExternalVariableName(
                    descriptionType, name, true);
            bindingVariables.put(descriptionType.getName(), variable);

            buf.append(descriptionType.getName());
            buf.append(" "); // NOI18N
        }
        return variable;
    }

    private static void buildBindingParamsCode(MetaBinding bindingDef, StringBuilder buf) {
        String sourcePath = bindingDef.getSourcePath();
        String targetPath = bindingDef.getTargetPath();
        buf.append(getExpressionJavaString(bindingDef.getSource().getCodeExpression(), "this")); // NOI18N
        buf.append(", "); // NOI18N
        if (sourcePath != null) {
            buf.append("\""); // NOI18N
            buf.append(sourcePath);
            buf.append("\""); // NOI18N
        }
        else buf.append("null"); // NOI18N
        buf.append(", "); // NOI18N
        buf.append(getExpressionJavaString(bindingDef.getTarget().getCodeExpression(), "this")); // NOI18N
        buf.append(", "); // NOI18N
        if (targetPath != null) {
            buf.append("\""); // NOI18N
            buf.append(targetPath);
            buf.append("\""); // NOI18N
        }
        else buf.append("null"); // NOI18N
        Map parameters = bindingDef.getParameters();
        Iterator<Map.Entry> iter = parameters.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = iter.next();
            buf.append(", "); // NOI18N
            buf.append(entry.getKey());
            buf.append(", "); // NOI18N
            buf.append(entry.getValue());
        }
    }

    private boolean generateAccessibilityCode(RADComponent comp,
                                              CodeWriter initCodeWriter,
                                              CustomCodeData codeData)
        throws IOException
    {
        boolean generated = false;
        Object genType = comp.getAuxValue(AUX_CODE_GENERATION);
        if (!comp.hasHiddenState() 
                && (genType == null || VALUE_GENERATE_CODE.equals(genType)))
        {   // not serialized
            FormProperty[] props;
            if (comp instanceof RADVisualComponent)
                props = ((RADVisualComponent)comp).getAccessibilityProperties();
            else return false;

            for (int i=0; i < props.length; i++) {
                boolean gen = generateProperty(props[i], comp, null, initCodeWriter, codeData);
                if (gen)
                    generated = true;
            }
        }
        return generated;
    }

    private boolean generateInjectionCode(RADComponent metacomp, Writer writer, CustomCodeData codeData)
        throws IOException
    {
        String injectionCode = ResourceSupport.getInjectionCode(
                metacomp, getComponentParameterString(metacomp, true));
        if (injectionCode != null) {
            if (!injectionCode.endsWith("\n")) // NOI18N
                injectionCode = injectionCode + "\n"; // NOI18N
            if (codeData == null) {
                writer.write(injectionCode);
            }
            else { // build code data for editing
                String code = indentCode(injectionCode);
                codeData.addGuardedBlock(code);
            }
            return true;
        }
        else return false;
    }

    private void generateComponentAddCode(RADComponent comp,
                                          CodeWriter initCodeWriter,
                                          CustomCodeData codeData)
        throws IOException
    {
        RADComponent parent = comp.getParentComponent();
        if (parent == null)
            return;

        // optimization - only properties need to go through CodeWriter
        Writer writer = initCodeWriter.getWriter();

        generateComponentAddPre(comp, initCodeWriter.getWriter(), codeData);

        if (comp instanceof RADVisualComponent) {
            if (comp == ((RADVisualContainer)parent).getContainerMenu()) { // 
                assert comp.getBeanInstance() instanceof javax.swing.JMenuBar
                       && parent.getBeanInstance() instanceof javax.swing.RootPaneContainer;
                if (codeData == null) {
                    generateEmptyLineIfNeeded(writer);
                }
                writer.write(getComponentInvokeString(parent, true));
                writer.write("setJMenuBar("); // NOI18N
                writer.write(getComponentParameterString(comp, true));
                writer.write(");\n"); // NOI18N
            } else { // adding visual component to container with old layout support
                LayoutSupportManager laysup = ((RADVisualComponent)comp).getParentLayoutSupport();
                CodeGroup componentCode = laysup != null ?
                    laysup.getComponentCode((RADVisualComponent)comp) : null;
                if (componentCode != null) {
                    Iterator it = componentCode.getStatementsIterator();
                    if (codeData == null && it.hasNext())
                        generateEmptyLineIfNeeded(writer);
                    while (it.hasNext()) {
                        CodeStatement statement = (CodeStatement) it.next();
                        initCodeWriter.write(getStatementJavaString(statement, "")); // NOI18N
                        initCodeWriter.write("\n"); // NOI18N
                    }
                }
            } // this method is not called for visual components in freee design
        }
        else if (comp instanceof RADMenuItemComponent) { // AWT menu
            if (parent instanceof RADVisualContainer) { // menu bar to visual container
                assert comp.getBeanInstance() instanceof java.awt.MenuBar //getMenuItemType() == RADMenuItemComponent.T_MENUBAR
                       && parent.getBeanInstance() instanceof java.awt.Frame;
                if (codeData == null) {
                    generateEmptyLineIfNeeded(writer);
                }
                writer.write(getComponentInvokeString(parent, true));
                writer.write("setMenuBar("); // NOI18N
                writer.write(getComponentParameterString(comp, true));
                writer.write(");\n"); // NOI18N
            }
            else { // menu component to another component
                assert parent instanceof RADMenuComponent;
                RADMenuItemComponent menuComp = (RADMenuItemComponent) comp;
                if (codeData == null) {
                    generateEmptyLineIfNeeded(writer);
                }
                if (menuComp.getMenuItemType() == RADMenuItemComponent.T_SEPARATOR) {
                    // treat AWT Separator specially - it is not a regular component
                    writer.write(getComponentInvokeString(parent, true));
                    writer.write("addSeparator();"); // NOI18N
                }
                else {
                    writer.write(getComponentInvokeString(parent, true));
                    writer.write("add("); // NOI18N
                    writer.write(getComponentParameterString(comp, true));
                    writer.write(");\n"); // NOI18N
                }
            }
        }
        // no other type of adding supported [assert false ?]

        if (codeData != null) { // build code data for editing
            String code = initCodeWriter.extractString();
            if (code != null && !code.equals("")) // NOI18N
                codeData.addGuardedBlock(indentCode(code));
        }

        generateComponentAddPost(comp, initCodeWriter, codeData);
    }

    private void generateComponentAddPre(RADComponent comp,
                                         Writer writer,
                                         CustomCodeData codeData)
        throws IOException
    {
        String preCode = (String) comp.getAuxValue(AUX_ADDING_PRE);
        if (codeData != null) { // build code data for editing
            codeData.addEditableBlock(preCode,
                                      (FormProperty) comp.getSyntheticProperty(PROP_ADDING_PRE),
                                      0, // preference index
                                      FormUtils.getBundleString("CustomCode-PreAdding"), // NOI18N
                                      FormUtils.getBundleString("MSG_JC_PreAddCodeDesc")); // NOI18N
        }
        else if (preCode != null && !preCode.equals("")) { // NOI18N
            generateEmptyLineIfNeeded(writer);
            writer.write(preCode);
            if (!preCode.endsWith("\n")) // NOI18N
                writer.write("\n"); // NOI18N
        }
    }

    private void generateComponentAddPost(RADComponent comp,
                                          CodeWriter initCodeWriter,
                                          CustomCodeData codeData)
        throws IOException
    {
        // some known (i.e. hardcoded) properties need to be set after
        // the component is added to the parent container
        java.util.List<FormProperty> postProps;
        if (parentDependentProperties != null
            && (postProps = parentDependentProperties.get(comp)) != null)
        {
            for (FormProperty prop : postProps) {
                generateProperty(prop, comp, null, initCodeWriter, codeData);
            }
        }

        String postCode = (String) comp.getAuxValue(AUX_ADDING_POST);
        if (codeData != null) { // build code data for editing
            codeData.addEditableBlock(postCode,
                                      (FormProperty) comp.getSyntheticProperty(PROP_ADDING_POST),
                                      0, // preference index
                                      FormUtils.getBundleString("CustomCode-PostAdding"), // NOI18N
                                      FormUtils.getBundleString("MSG_JC_PostAddCodeDesc")); // NOI18N
        }
        else if (postCode != null && !postCode.equals("")) { // NOI18N
            generateEmptyLineIfNeeded(initCodeWriter.getWriter());
            initCodeWriter.getWriter().write(postCode);
            if (!postCode.endsWith("\n")) // NOI18N
                initCodeWriter.getWriter().write("\n"); // NOI18N
        }
    }

    private void generateFreeDesignLayoutCode(RADVisualContainer cont, CodeWriter initCodeWriter)
        throws IOException
    {
        LayoutComponent layoutCont = formModel.getLayoutModel().getLayoutComponent(cont.getId());
        if (layoutCont == null)
            return;

        // optimization - only properties need to go through CodeWriter
        Writer writer = initCodeWriter.getWriter();

        RADVisualComponent[] comps = cont.getSubComponents();

        // layout code and adding sub-components
        generateEmptyLineIfNeeded(writer);
        SwingLayoutCodeGenerator.ComponentInfo[] infos = new SwingLayoutCodeGenerator.ComponentInfo[comps.length];
        for (int i=0; i<comps.length; i++) {
            RADVisualComponent subComp = comps[i];
            SwingLayoutCodeGenerator.ComponentInfo info = new SwingLayoutCodeGenerator.ComponentInfo();
            info.id = subComp.getId();
            info.variableName = getExpressionJavaString(subComp.getCodeExpression(), ""); // NOI18N
            info.clazz = subComp.getBeanClass();
            Node.Property minProp = subComp.getPropertyByName("minimumSize"); // NOI18N
            Node.Property prefProp = subComp.getPropertyByName("preferredSize"); // NOI18N
            Node.Property maxProp = subComp.getPropertyByName("maximumSize"); // NOI18N
            info.sizingChanged = !(((minProp == null) || minProp.isDefaultValue())
                && ((prefProp == null) || prefProp.isDefaultValue())
                && ((maxProp == null) || maxProp.isDefaultValue()));
            info.minSize = ((Component)subComp.getBeanInstance()).getMinimumSize();
            infos[i] = info;
        }
        CodeExpression contExpr = LayoutSupportManager.containerDelegateCodeExpression(
                                    cont, formModel.getCodeStructure());
        String contExprStr = getExpressionJavaString(contExpr, ""); // NOI18N
        CodeVariable contVar = cont.getCodeExpression().getVariable();
        String contVarName = (contVar == null) ? null : contVar.getName();
        SwingLayoutCodeGenerator swingGenerator = getSwingGenerator();
        swingGenerator.generateContainerLayout(
            writer,
            layoutCont,
            contExprStr,
            contVarName,
            infos,
            formModel.getSettings().getLayoutCodeTarget() == LAYOUT_CODE_LIBRARY);
    }

    private SwingLayoutCodeGenerator getSwingGenerator() {
        if (swingGenerator == null) {
            swingGenerator = new SwingLayoutCodeGenerator(formModel.getLayoutModel());
        }
        return swingGenerator;
    }

    private void generatePrePopulationCode(RADComponent cont,
                                           Writer writer,
                                           CustomCodeData codeData)
        throws IOException
    {
        String preCode = (String) cont.getAuxValue(AUX_LAYOUT_PRE);
        if (codeData != null) { // build code data for editing
            codeData.addEditableBlock(preCode,
                                      (FormProperty) cont.getSyntheticProperty(PROP_LAYOUT_PRE),
                                      2, // preference index
                                      FormUtils.getBundleString("CustomCode-PrePopulation"), // NOI18N
                                      FormUtils.getBundleString("MSG_JC_PrePopulationCodeDesc")); // NOI18N
        }
        else if (preCode != null && !preCode.equals("")) { // NOI18N
            generateEmptyLineIfNeeded(writer);
            writer.write(preCode);
            if (!preCode.endsWith("\n")) // NOI18N
                writer.write("\n"); // NOI18N
        }
    }

    private void generatePostPopulationCode(RADComponent cont,
                                            CodeWriter initCodeWriter,
                                            CustomCodeData codeData)
        throws IOException
    {
        // some known (i.e. hardcoded) container properties need to be set after
        // all sub-components are added
        java.util.List<FormProperty> postProps;
        if (childrenDependentProperties != null
            && (postProps = childrenDependentProperties.get(cont)) != null)
        {
            for (FormProperty prop : postProps) {
                generateProperty(prop, cont, null, initCodeWriter, codeData);
            }
        }

        // custom post-layout (post-population) code
        String postCode = (String) cont.getAuxValue(AUX_LAYOUT_POST);
        if (codeData != null) { // build code data for editing
            codeData.addEditableBlock(postCode,
                                      (FormProperty) cont.getSyntheticProperty(PROP_LAYOUT_POST),
                                      4, // preference index
                                      FormUtils.getBundleString("CustomCode-PostPopulation"), // NOI18N
                                      FormUtils.getBundleString("MSG_JC_PostPopulationCodeDesc")); // NOI18N
        }
        else if (postCode != null && !postCode.equals("")) { // NOI18N
            generateEmptyLineIfNeeded(initCodeWriter.getWriter());
            initCodeWriter.getWriter().write(postCode);
            if (!postCode.endsWith("\n")) // NOI18N
                initCodeWriter.getWriter().write("\n"); // NOI18N
        }
    }

    private void generateFormSizeCode(Writer writer) throws IOException {
        if (formModel.getTopRADComponent() instanceof RADVisualFormContainer) {
            RADVisualFormContainer visualForm =
                (RADVisualFormContainer) formModel.getTopRADComponent();

            // generate size code according to form size policy
            int formPolicy = visualForm.getFormSizePolicy();
            boolean genSize = visualForm.getGenerateSize();
            boolean genPosition = visualForm.getGeneratePosition();
            boolean genCenter = visualForm.getGenerateCenter();
            Dimension formSize = visualForm.getFormSize();
            Point formPosition = visualForm.getFormPosition();

            String sizeText = ""; // NOI18N

            if (formPolicy == RADVisualFormContainer.GEN_PACK)
                sizeText = "pack();\n"; // NOI18N
            else if (formPolicy == RADVisualFormContainer.GEN_BOUNDS) {
                if (genCenter) {
                    StringBuffer sizeBuffer = new StringBuffer();
                    if (genSize) {
//                                sizeBuffer.append("pack();\n"); // NOI18N
                        sizeBuffer.append("java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();\n"); // NOI18N
                        sizeBuffer.append("setBounds((screenSize.width-"+formSize.width+")/2, (screenSize.height-"+formSize.height+")/2, "+formSize.width + ", " + formSize.height + ");\n"); // NOI18N
//                                sizeBuffer.append("setSize(new java.awt.Dimension("+formSize.width + ", " + formSize.height + "));\n"); // NOI18N
//                                sizeBuffer.append("setLocation((screenSize.width-"+formSize.width+")/2,(screenSize.height-"+formSize.height+")/2);\n"); // NOI18N
                    }
                    else {
                        sizeBuffer.append("pack();\n"); // NOI18N
                        sizeBuffer.append("java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();\n"); // NOI18N
                        sizeBuffer.append("java.awt.Dimension dialogSize = getSize();\n"); // NOI18N
                        sizeBuffer.append("setLocation((screenSize.width-dialogSize.width)/2,(screenSize.height-dialogSize.height)/2);\n"); // NOI18N
                    }
                    sizeText = sizeBuffer.toString();
                }
                else if (genPosition && genSize) // both size and position
                    sizeText = "setBounds("+formPosition.x + ", " // NOI18N
                               + formPosition.y +", " // NOI18N
                               + formSize.width + ", " // NOI18N
                               + formSize.height + ");\n"; // NOI18N
                else if (genPosition) // position only
                    sizeText = "setLocation(new java.awt.Point(" // NOI18N
                               + formPosition.x + ", " // NOI18N
                               + formPosition.y + "));\n"; // NOI18N
                else if (genSize) // size only
                    sizeText = "setSize(new java.awt.Dimension(" // NOI18N
                               + formSize.width + ", " // NOI18N
                               + formSize.height + "));\n"; // NOI18N
            }

            if (!sizeText.equals("")) { // NOI18N
                emptyLineRequest++;
                generateEmptyLineIfNeeded(writer);
                writer.write(sizeText);
            }
        }
    }

    private boolean generateProperty(FormProperty prop,
                                     RADComponent comp,
                                     String setterVariable,
                                     CodeWriter initCodeWriter,
                                     CustomCodeData codeData)
        throws IOException
    {
        String preCode = prop.getPreCode();
        String postCode = prop.getPostCode();
        boolean valueSet = prop.isChanged();

        if ((preCode == null || preCode.equals("")) // NOI18N
            && (postCode == null || postCode.equals("")) // NOI18N
            && !valueSet)
            return false; // nothing set

        if (codeData == null)
            generateEmptyLineIfNeeded(initCodeWriter.getWriter());

        // 1. pre-initialization code
        if (codeData != null) { // build code data for editing
            String name;
            if (prop.getWriteMethod() != null)
                name = prop.getWriteMethod().getName();
            else {
                name = prop.getName();
                if (name.indexOf('.') >= 0)
                    name = name.substring(name.lastIndexOf('.')+1);
            }
            codeData.addEditableBlock(
                    preCode, prop, 0, // preference index
                    FormUtils.getFormattedBundleString("CustomCode-PreProperty_Format", // NOI18N
                                                       new Object[] { name }),
                    FormUtils.getBundleString("CustomCode-PreProperty_Hint"), // NOI18N
                    true, false);
        }
        else if (preCode != null && !preCode.equals("")) { // NOI18N
            initCodeWriter.getWriter().write(preCode);
            if (!preCode.endsWith("\n")) // NOI18N
                initCodeWriter.getWriter().write("\n"); // NOI18N
        }

        // 2. property setter code
        if (valueSet && !ResourceSupport.isInjectedProperty(prop)) {
	    if (setterVariable == null)
		setterVariable = getComponentInvokeString(comp, true);

            generatePropertySetter(prop, comp, setterVariable, initCodeWriter, codeData);

            if (codeData != null) { // build code data for editing
                String customCode = indentCode(initCodeWriter.extractString());
                String defaultCode;
                boolean codeCustomized = isPropertyWithCustomCode(prop);
                if (codeCustomized)
                    defaultCode = "// " + FormUtils.getBundleString("CustomCode-SubstNoValue"); // NOI18N
                else {
                    generatePropertySetter(prop, comp, setterVariable, initCodeWriter, null);
                    defaultCode = indentCode(initCodeWriter.extractString());
                }
                codeData.addGuardedBlock(defaultCode, customCode, CUSTOM_CODE_MARK, codeCustomized,
                                         prop,
                                         FormUtils.getBundleString("CustomCode-Property"), // NOI18N
                                         FormUtils.getBundleString("CustomCode-Property_Hint")); // NOI18N
            }
        }

        // 3. post-initialization code
        if (codeData != null) { // build code data for editing
            String name;
            if (prop.getWriteMethod() != null)
                name = prop.getWriteMethod().getName();
            else {
                name = prop.getName();
                if (name.indexOf('.') >= 0)
                    name = name.substring(name.lastIndexOf('.')+1);
            }
            codeData.addEditableBlock(
                    postCode, prop, 0, // preference index
                    FormUtils.getFormattedBundleString("CustomCode-PostProperty_Format", // NOI18N
                                                       new Object[] { name }),
                    FormUtils.getBundleString("CustomCode-PostProperty_Hint"), // NOI18N
                    false, true);
        }
        else if (postCode != null && !postCode.equals("")) { // NOI18N
            initCodeWriter.getWriter().write(postCode);
            if (!postCode.endsWith("\n")) // NOI18N
                initCodeWriter.getWriter().write("\n"); // NOI18N
        }

        return true;
    }

    static boolean isPropertyWithCustomCode(Node.Property prop) {
        try {
            Object value = prop.getValue();
            return value instanceof RADConnectionPropertyEditor.RADConnectionDesignValue
                   && ((RADConnectionPropertyEditor.RADConnectionDesignValue)value).getType()
                        == RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_CODE;
        }
        catch (Exception ex) {} // should not happen
        return false;
    }

    private void generatePropertySetter(FormProperty prop,
                                        RADComponent comp,
                                        String setterVariable,
                                        CodeWriter initCodeWriter,
                                        CustomCodeData codeData)
        throws IOException
    {
        Object value = null;
        try {
            value = prop.getValue();
        }
        catch (Exception ex) { // should not happen
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return;
        }

        PropertyEditor currentEditor = prop.getCurrentEditor();
        if (currentEditor instanceof BeanPropertyEditor && value != null) {
            generatePropertyBeanSetterCode(prop, value, setterVariable, initCodeWriter, codeData);
        } else if (currentEditor instanceof FormCodeAwareEditor) {
            if (currentEditor.getValue() != value) {
                currentEditor.setValue(value);
            }
            String code = ((FormCodeAwareEditor)currentEditor).getSourceCode();
            if (code != null) {
                initCodeWriter.write(code);
            }
        } else {
            String propValueCode = prop.getJavaInitializationString();
            if (codeData != null) // building code data for editing
                propValueCode = CUSTOM_CODE_MARK + propValueCode + CUSTOM_CODE_MARK;
            String javaStr = null;

            if ((javaStr = prop.getWholeSetterCode(propValueCode)) != null) { // button group property
                initCodeWriter.write(javaStr);
                if (!javaStr.endsWith("\n")) // NOI18N
                    initCodeWriter.write("\n"); // NOI18N
            }
            // Mnemonics support - start -
            else if (comp != null
                     && "text".equals(prop.getName()) // NOI18N
                     && canUseMnemonics(comp) && isUsingMnemonics(comp))
            {
                if (propValueCode != null) {
                    initCodeWriter.write("org.openide.awt.Mnemonics.setLocalizedText(" // NOI18N
                        + comp.getName() + ", " + propValueCode + ");\n"); // NOI18N
                }
            }
            // Mnemonics support - end -
            else if ((javaStr = prop.getPartialSetterCode(propValueCode)) != null) {
                // this is a normal property
                generateSimpleSetterCode(prop, javaStr, setterVariable, initCodeWriter);
            }
        }
    }

    private void generatePropertyBeanSetterCode(FormProperty prop, 	
						Object value,
						String setterVariable, 						
						CodeWriter initCodeWriter,
                                                CustomCodeData codeData)
	throws IOException
    {
	
	FormProperty[] properties = null;
	Class propertyType = null;
	Object realValue = prop.getRealValue(value);
	propertyType = realValue.getClass();	   
			    
	prop.getCurrentEditor().setValue(value);
	BeanPropertyEditor beanPropertyEditor = (BeanPropertyEditor) prop.getCurrentEditor();	    		    
	properties = (FormProperty[]) beanPropertyEditor.getProperties();	    
        if ((properties == null) || (properties.length == 0)) return;

	CreationDescriptor.Creator creator = getPropertyCreator(propertyType, properties);
	java.util.List creatorProperties = getCreatorProperties(creator, properties);
															
	java.util.List remainingProperties = new ArrayList();		
	if(properties !=null) {
	    for (int i = 0; i < properties.length; i++) {
		if( properties[i].isChanged() && 
	            !creatorProperties.contains(properties[i]) ) 
		{		    
		    remainingProperties.add(properties[i]);			
		}
	    }					    						
	}

	String propertyInitializationString = 
		creator.getJavaCreationCode(
		    (FormProperty[])creatorProperties.toArray(new FormProperty[creatorProperties.size()]), prop.getValueType());
        if (codeData != null)
            propertyInitializationString = CUSTOM_CODE_MARK + propertyInitializationString + CUSTOM_CODE_MARK;

	if(remainingProperties.size() == 0) {		    		    
	    generateSimpleSetterCode(prop, 
				     prop.getPartialSetterCode(propertyInitializationString),
				     setterVariable, 
				     initCodeWriter);
	} else if(remainingProperties.size() > 0) {
	    generateWholePropertyInitialization(prop, propertyType, setterVariable, 
				 propertyInitializationString, remainingProperties, initCodeWriter);	    	    
	}
    }

    private java.util.List getCreatorProperties(CreationDescriptor.Creator creator, FormProperty[] properties) {
	String[] propNames = creator.getPropertyNames();	
	java.util.List creatorProperties; 
	if (propNames.length > 0) {
	    creatorProperties = new ArrayList(propNames.length);		    
	    for (int i=0; i < propNames.length; i++) {
		for (int j = 0; j < properties.length; j++) {
		    if(properties[j].getName().equals(propNames[i])) {
			creatorProperties.add(properties[j]);							
			break;
		    }			    			    
		}                        
	    }
	} else {
	    creatorProperties = new ArrayList(0);
	}
	return creatorProperties;
    }
    
    private CreationDescriptor.Creator getPropertyCreator(Class clazz, FormProperty[] properties) {	
	CreationDescriptor creationDesc = CreationFactory.getDescriptor(clazz);
	return creationDesc.findBestCreator(properties,
					    // XXX CHANGED_ONLY ???
					    CreationDescriptor.CHANGED_ONLY | CreationDescriptor.PLACE_ALL);	
    }
    
    private void generateWholePropertyInitialization(FormProperty prop,
					      Class propertyType, 
					      String setterVariable,
					      String propertyInitializationString, 
					      java.util.List remainingProperties,
					      CodeWriter initCodeWriter)
	throws IOException					    
    {
	String variableName = formModel.getCodeStructure().getExternalVariableName(propertyType, null, true);

	String javaStr = propertyType.getName() + " " + variableName + " = " + propertyInitializationString; // NOI18N		
	initCodeWriter.write(javaStr);
	initCodeWriter.write(";\n"); // NOI18N		    		

	for (Iterator it = remainingProperties.iterator(); it.hasNext();) {
	    generateProperty((FormProperty) it.next(), null, variableName + ".", initCodeWriter, null); // NOI18N 	
	}

	generateSimpleSetterCode(prop,
				 prop.getWriteMethod().getName() + "(" + variableName + ")", // NOI18N
				 setterVariable,
				 initCodeWriter);
    }
    
    private void generateSimpleSetterCode(FormProperty prop,
				          String partialSetterCode,
	                                  String setterVariable,
				          CodeWriter initCodeWriter)
	throws IOException
    {

	// if the setter throws checked exceptions,
	// we must generate try/catch block around it.
	Class[] exceptions = null;
	Method writeMethod = prop.getWriteMethod(); 
	if (writeMethod != null) {
	    exceptions = writeMethod.getExceptionTypes();
	    if (needTryCode(exceptions))
		initCodeWriter.write("try {\n"); // NOI18N
	    else
		exceptions = null;
	}

	initCodeWriter.write(setterVariable + partialSetterCode + ";\n"); // NOI18N

	// add the catch code if needed
	if (exceptions != null)
	    generateCatchCode(exceptions, initCodeWriter.getWriter());
    }    

    // generates code for handling events of one component
    // (all component.addXXXListener() calls)
    private void generateComponentEvents(RADComponent component,
                                         CodeWriter initCodeWriter,
                                         CustomCodeData codeData)
        throws IOException
    {
        Writer writer = initCodeWriter.getWriter();

        EventSetDescriptor lastEventSetDesc = null;
        java.util.List listenerEvents = null;

        // we must deal somehow with the fact that for some (pathological)
        // events only anonymous innerclass listener can be generated
        // (CEDL cannot be used)
        int defaultMode = formModel.getSettings().getListenerGenerationStyle();
        int mode = defaultMode;
        boolean mixedMode = false;

        Event[] events = component.getKnownEvents();
        for (int i=0; i < events.length; i++) {
            Event event = events[i];
            if (!event.hasEventHandlers())
                continue;

            EventSetDescriptor eventSetDesc = event.getEventSetDescriptor();
            if (eventSetDesc != lastEventSetDesc) {
                if (lastEventSetDesc != null) {
                    // new listener encountered, generate the previous one
                    if (codeData == null)
                        generateEmptyLineIfNeeded(writer);
                    generateListenerAddCode(component, lastEventSetDesc, listenerEvents, mode, writer);
                    if (mixedMode)
                        generateListenerAddCode(component, lastEventSetDesc, listenerEvents, defaultMode, writer);
                    if (listenerEvents != null)
                        listenerEvents.clear();
                }

                lastEventSetDesc = eventSetDesc;
            }

            if (defaultMode != ANONYMOUS_INNERCLASSES)
                if (mode == defaultMode) {
                    if (!event.isInCEDL())
                        mode = ANONYMOUS_INNERCLASSES;
                }
                else if (event.isInCEDL())
                    mixedMode = true;

            if (defaultMode == ANONYMOUS_INNERCLASSES || !event.isInCEDL()) {
                if (listenerEvents == null)
                    listenerEvents = new ArrayList();
                listenerEvents.add(event);
            }
        }

        if (lastEventSetDesc != null) {
            // generate the last listener
            if (codeData == null)
                generateEmptyLineIfNeeded(writer);
            generateListenerAddCode(component, lastEventSetDesc, listenerEvents, mode, writer);
            if (mixedMode)
                generateListenerAddCode(component, lastEventSetDesc, listenerEvents, defaultMode, writer);
        }

        String postCode = (String) component.getAuxValue(AUX_LISTENERS_POST);
        if (codeData != null) { // build code data for editing
            String code = initCodeWriter.extractString();
            if (code != null && !code.equals("")) // NOI18N
                codeData.addGuardedBlock(indentCode(code));
            codeData.addEditableBlock(postCode,
                                      (FormProperty) component.getSyntheticProperty(PROP_LISTENERS_POST),
                                      0, // preference index
                                      FormUtils.getBundleString("CustomCode-PostListeners"), // NOI18N
                                      FormUtils.getBundleString("MSG_JC_PostListenersCodeDesc")); // NOI18N
        }
        else if (postCode != null && !postCode.equals("")) { // NOI18N
            generateEmptyLineIfNeeded(writer);
            writer.write(postCode);
            if (!postCode.endsWith("\n")) // NOI18N
                writer.write("\n"); // NOI18N
        }
    }

    // generates complete code for handling one listener
    // (one component.addXXXListener() call)
    private void generateListenerAddCode(RADComponent comp,
                                         EventSetDescriptor eventSetDesc,
                                         java.util.List eventList,
                                         int mode,
                                         Writer codeWriter)
        throws IOException
    {
        Method addListenerMethod = eventSetDesc.getAddListenerMethod();
        Class[] exceptions = addListenerMethod.getExceptionTypes();
        if (needTryCode(exceptions))
            codeWriter.write("try {\n"); // NOI18N
        else
            exceptions = null;

        codeWriter.write(getComponentInvokeString(comp, true));
        codeWriter.write(addListenerMethod.getName());
        codeWriter.write("("); // NOI18N

        switch (mode) {
            case ANONYMOUS_INNERCLASSES:
                codeWriter.write("new "); // NOI18N

                // try to find adpater to use instead of full listener impl
                Class listenerType = eventSetDesc.getListenerType();
                Class adapterClass = BeanSupport.getAdapterForListener(
                                                           listenerType);
                if (adapterClass != null) { // use listener adapter class
                    codeWriter.write(getSourceClassName(adapterClass) + "() {\n"); // NOI18N

                    for (int i=0; i < eventList.size(); i++) {
                        Event event = (Event) eventList.get(i);
                        String[] paramNames = generateListenerMethodHeader(
                                   null, event.getListenerMethod(), codeWriter);
                        generateEventHandlerCalls(event, paramNames, codeWriter, true);
                        codeWriter.write("}\n"); // NOI18N
                    }
                }
                else { // generate full listener implementation (all methods)
                    codeWriter.write(getSourceClassName(listenerType) + "() {\n"); // NOI18N

                    Method[] methods = eventSetDesc.getListenerMethods();
                    for (int i=0; i < methods.length; i++) {
                        Method m = methods[i];
                        Event event = null;
                        for (int j=0; j < eventList.size(); j++) {
                            Event e = (Event) eventList.get(j);
                            if (m.equals(e.getListenerMethod())) {
                                event = e;
                                break;
                            }
                        }
                        String[] paramNames =
                            generateListenerMethodHeader(null, m, codeWriter);
                        if (event != null)
                            generateEventHandlerCalls(event, paramNames, codeWriter, true);
                        codeWriter.write("}\n"); // NOI18N
                    }
                }

                codeWriter.write("}"); // NOI18N
                break;

            case CEDL_INNERCLASS:
                codeWriter.write(getListenerVariableName());
                break;

            case CEDL_MAINCLASS:
                codeWriter.write("this"); // NOI18N
                break;
        }

        codeWriter.write(");\n"); // NOI18N

        if (exceptions != null)
            generateCatchCode(exceptions, codeWriter);
    }

    private RADComponent codeVariableToRADComponent(CodeVariable var) {
        RADComponent metacomp = null;
        Iterator iter = var.getAttachedExpressions().iterator();
        if (iter.hasNext()) {
            Object metaobject = ((CodeExpression)iter.next()).getOrigin().getMetaObject();
            if (metaobject instanceof RADComponent) {
                metacomp = (RADComponent)metaobject;
            }
        }
        return metacomp;
    }

    private void addFieldVariables(CodeWriter variablesWriter)
        throws IOException
    {
        Iterator it = getSortedVariables(CodeVariable.FIELD,
                                         CodeVariable.SCOPE_MASK);

        while (it.hasNext()) {
            CodeVariable var = (CodeVariable) it.next();
            RADComponent metacomp = codeVariableToRADComponent(var);
            if (metacomp != null)
                generateComponentFieldVariable(metacomp, variablesWriter, null);
            // there should not be other than component variables as fields
        }

        // is there any binding?
        for (RADComponent metacomp : formModel.getAllComponents()) {
            if (metacomp.hasBindings()) {
                bindingContextVariable = formModel.getCodeStructure().getExternalVariableName(
                        bindingContextClass, "bindingContext", true); // NOI18N
                variablesWriter.write("private " + bindingContextClass.getName() + " " + bindingContextVariable + ";\n"); // NOI18N
                break;
            }
        }
    }

    private void addLocalVariables(Writer writer)
        throws IOException
    {
        Iterator it = getSortedVariables(
            CodeVariable.LOCAL | CodeVariable.EXPLICIT_DECLARATION,
            CodeVariable.SCOPE_MASK | CodeVariable.DECLARATION_MASK);

        if (it.hasNext())
            generateEmptyLineIfNeeded(writer);

        while (it.hasNext()) {
            CodeVariable var = (CodeVariable) it.next();
            if (codeVariableToRADComponent(var) == null) {
                // other than component variable (e.g. GridBagConstraints)
                writer.write(var.getDeclaration().getJavaCodeString(null, null));
                writer.write("\n"); // NOI18N
            }
        }
    }

    private void generateComponentFieldVariable(RADComponent metacomp,
                                           CodeWriter codeWriter,
                                           CustomCodeData codeData)
        throws IOException
    {
        // optimization - only properties need to go through CodeWriter
        Writer writer = codeWriter.getWriter();

        generateDeclarationPre(metacomp, writer, codeData);

        CodeVariable var = metacomp.getCodeExpression().getVariable();
        if (isFinalFieldVariable(var.getType())) { // add also creation assignment
            generateComponentCreate(metacomp, codeWriter, false, codeData);
        }
        else { // simple declaration
            writer.write(var.getDeclaration().getJavaCodeString(null, null));
            writer.write("\n"); // NOI18N

            if (codeData != null) { // build code data for editing
                String code = indentCode(codeWriter.extractString());
                codeData.addGuardedBlock(code);
            }
        }

        generateDeclarationPost(metacomp, writer, codeData);
    }

    private static boolean isFinalFieldVariable(int varType) {
        return (varType & (CodeVariable.FINAL | CodeVariable.SCOPE_MASK))
                == (CodeVariable.FINAL | CodeVariable.FIELD);
    }

    private static void generateDeclarationPre(RADComponent metacomp,
                                               Writer writer,
                                               CustomCodeData codeData)
        throws IOException
    {
        String preCode = (String) metacomp.getAuxValue(AUX_DECLARATION_PRE);
        if (codeData != null) { // build code data for editing
            codeData.addEditableBlock(preCode,
                                      (FormProperty) metacomp.getSyntheticProperty(PROP_DECLARATION_PRE),
                                      0, // preference index
                                      FormUtils.getBundleString("CustomCode-PreDeclaration"), // NOI18N
                                      FormUtils.getBundleString("MSG_JC_PreDeclarationDesc")); // NOI18N
        }
        else if (preCode != null && !preCode.equals("")) { // NOI18N
            writer.write(preCode);
            if (!preCode.endsWith("\n")) // NOI18N
                writer.write("\n"); // NOI18N
        }
    }

    private static void generateDeclarationPost(RADComponent metacomp,
                                                Writer writer,
                                                CustomCodeData codeData)
        throws IOException
    {
        String postCode = (String) metacomp.getAuxValue(AUX_DECLARATION_POST);
        if (codeData != null) { // build code data for editing
            codeData.addEditableBlock(postCode,
                                      (FormProperty) metacomp.getSyntheticProperty(PROP_DECLARATION_POST),
                                      0, // preference index
                                      FormUtils.getBundleString("CustomCode-PostDeclaration"), // NOI18N
                                      FormUtils.getBundleString("MSG_JC_PostDeclarationDesc")); // NOI18N
        }
        else if (postCode != null && !postCode.equals("")) { // NOI18N
            writer.write(postCode);
            if (!postCode.endsWith("\n")) // NOI18N
                writer.write("\n"); // NOI18N
        }
    }

    /** Adds new empty line if currentAreaNumber has raised from last time.
     * Should never be called when building "code data" for editing.
     */
    private void generateEmptyLineIfNeeded(Writer writer) throws IOException {
        if (emptyLineCounter != emptyLineRequest) {
            writer.write("\n"); // NOI18N
        }
        emptyLineCounter = emptyLineRequest;
    }

    private Iterator getSortedVariables(int type, int typeMask) {
        Collection allVariables = formModel.getCodeStructure().getAllVariables();
        java.util.List variables = new ArrayList(allVariables.size());
        Iterator it = allVariables.iterator();
        while (it.hasNext()) {
            CodeVariable var = (CodeVariable) it.next();
            if (var.getDeclaredType() == org.netbeans.modules.form.Separator.class)
                continue; // treat AWT Separator specially - it is not a component
            if ((var.getType() &  typeMask) == (type & typeMask))
                variables.add(var);
        }
        Collections.sort(variables, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((CodeVariable)o1).getName().compareTo(
                        ((CodeVariable)o2).getName());
            }
        });
        return variables.iterator();
    }

    // Mnemonics support - start -
    static boolean canUseMnemonics(RADComponent comp) {
        return javax.swing.JLabel.class.isAssignableFrom(comp.getBeanClass())
               || javax.swing.AbstractButton.class.isAssignableFrom(comp.getBeanClass());
    }

    static boolean isUsingMnemonics(RADComponent comp) {
        Object mnem = comp.getAuxValue(PROP_GENERATE_MNEMONICS);
        if (mnem != null)
            return Boolean.TRUE.equals(mnem);

        return comp.getFormModel().getSettings().getGenerateMnemonicsCode();
    }
    // Mnemonics support - end -

    private String getComponentParameterString(RADComponent component,
                                               boolean inMainClass)
    {
        if (component == formModel.getTopRADComponent())
            return inMainClass ?
                     "this" : // NOI18N
                     formEditorSupport.getFormDataObject().getName() + ".this"; // NOI18N
        else
            return component.getName();
    }

    private String getComponentInvokeString(RADComponent component,
                                            boolean inMainClass)
    {
        if (component == formModel.getTopRADComponent())
            return inMainClass ?
                     "" : // NOI18N
                     formEditorSupport.getFormDataObject().getName() + ".this."; // NOI18N
        else
            return component.getName() + "."; // NOI18N
    }

    static String getSourceClassName(Class cls) {
        return cls.getName().replace('$', '.').replace('+', '.').replace('/', '.'); // NOI18N
    }

    private static String getVariablesHeaderComment() {
        if (variablesHeader == null)
            variablesHeader = FormUtils.getBundleString("MSG_VariablesBegin"); // NOI18N
        return variablesHeader;
    }

    private static String getVariablesFooterComment() {
        if (variablesFooter == null)
            variablesFooter = FormUtils.getBundleString("MSG_VariablesEnd"); // NOI18N
        return variablesFooter;
    }

    private static String getEventDispatchCodeComment() {
        if (eventDispatchCodeComment == null)
            eventDispatchCodeComment = FormUtils.getBundleString("MSG_EventDispatchCodeComment"); // NOI18N
        return eventDispatchCodeComment;
    }

    private boolean needTryCode(Class[] exceptions) {
        if (exceptions != null)
            for (int i=0; i < exceptions.length; i++)
                if (Exception.class.isAssignableFrom(exceptions[i])
                    && !RuntimeException.class.isAssignableFrom(exceptions[i]))
                {
                    return true;
                }

        return false;
    }

    private void generateCatchCode(Class[] exceptions, Writer initCodeWriter)
        throws IOException
    {
        initCodeWriter.write("}"); // NOI18N
        for (int i=0, exCount=0; i < exceptions.length; i++) {
            Class exception = exceptions[i];
            if (!Exception.class.isAssignableFrom(exception)
                    || RuntimeException.class.isAssignableFrom(exception))
                continue; // need not be caught

            if (i > 0) {
                int j;
                for (j=0; j < i; j++)
                    if (exceptions[j].isAssignableFrom(exception))
                        break;
                if (j < i)
                    continue; // a subclass of this exception already caught
            }

            initCodeWriter.write(" catch ("); // NOI18N
            initCodeWriter.write(getSourceClassName(exception));
            initCodeWriter.write(" "); // NOI18N

            String varName = "e" + ++exCount; // NOI18N

            initCodeWriter.write(varName);
            initCodeWriter.write(") {\n"); // NOI18N
            initCodeWriter.write(varName);
            initCodeWriter.write(".printStackTrace();\n"); // NOI18N
            // [shouldn't return be generated here?]
            initCodeWriter.write("}"); // NOI18N
        }
        initCodeWriter.write("\n"); // NOI18N
    }

    private void addDispatchListenerDeclaration(Writer codeWriter)
        throws IOException
    {
        generateEmptyLineIfNeeded(codeWriter);

        listenerVariableName = null;
        codeWriter.write(getListenerClassName());
        codeWriter.write(" "); // NOI18N
        codeWriter.write(getListenerVariableName());
        codeWriter.write(" = new "); // NOI18N
        codeWriter.write(getListenerClassName());
        codeWriter.write("();\n"); // NOI18N
    }

    private void generateDispatchListenerCode(Writer codeWriter)
        throws IOException
    {   // always ends up with } as last character (no new line - because of fold footer)
        FormEvents formEvents = formModel.getFormEvents();
        boolean innerclass = formModel.getSettings().getListenerGenerationStyle() == CEDL_INNERCLASS;
        boolean mainclass = formModel.getSettings().getListenerGenerationStyle() == CEDL_MAINCLASS;

        Class[] listenersToImplement = formEvents.getCEDLTypes();
        Arrays.sort(listenersToImplement, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Class)o1).getName().compareTo(((Class)o2).getName());
            }
        });

        listenersInMainClass = mainclass ? listenersToImplement : null;

        if (innerclass) {
            String listenerClassName = getListenerClassName();
            codeWriter.write("private class "); // NOI18N
            codeWriter.write(listenerClassName);
            codeWriter.write(" implements "); // NOI18N
            for (int i=0; i < listenersToImplement.length; i++) {
                codeWriter.write(getSourceClassName(listenersToImplement[i]));
                if (i + 1 < listenersToImplement.length)
                    codeWriter.write(", "); // NOI18N
            }
            codeWriter.write(" {\n"); // NOI18N
            codeWriter.write(listenerClassName + "() {}\n"); // NOI18N Issue 72346 resp. 15242
        }

        for (int i=0; i < listenersToImplement.length; i++) {
            boolean implementedInSuperclass =
                mainclass && listenersToImplement[i].isAssignableFrom(
                                          formModel.getFormBaseClass());

            Method[] methods = listenersToImplement[i].getMethods();
            Arrays.sort(methods, new Comparator() {
                public int compare(Object o1, Object o2) {
                    return ((Method)o1).getName().compareTo(((Method)o2).getName());
                }
            });

            for (int j=0; j < methods.length; j++) {
                Method method = methods[j];
                Event[] events = formEvents.getEventsForCEDLMethod(method);
                if (implementedInSuperclass && events.length == 0)
                    continue;

                String[] paramNames =
                    generateListenerMethodHeader(null, method, codeWriter);

                for (int k=0; k < events.length; k++) {
                    Event event = events[k];
                    if (k + 1 < events.length
                        || method.getReturnType() == Void.TYPE)
                    {                                               
			String componentParameterString = getComponentParameterString(
							    event.getComponent(), false);
			
			CodeVariable variable = event.getComponent().getCodeExpression().getVariable();                                                
			if( variable!= null && ( (variable.getType() & CodeVariable.LOCAL) == CodeVariable.LOCAL) ) {
			    codeWriter.write(
				FormUtils.getFormattedBundleString(
				    "MSG_WrongLocalVariableSettingComment", // NOI18N
				    new Object[] { componentParameterString }));
			}
			
                        codeWriter.write(k == 0 ? "if (" : "else if ("); // NOI18N
                        codeWriter.write(paramNames[0]);
                        codeWriter.write(".getSource() == "); // NOI18N
                        codeWriter.write(componentParameterString);
                        codeWriter.write(") {\n"); // NOI18N						

                        generateEventHandlerCalls(event, paramNames, codeWriter, false);
                        codeWriter.write("}\n"); // NOI18N
                        
                    }
                    else { // the listener method returns something
                        if (k > 0)
                            codeWriter.write("else {\n"); // NOI18N
                        generateEventHandlerCalls(event, paramNames, codeWriter, false);
                        if (k > 0)
                            codeWriter.write("}\n"); // NOI18N
                    }
                }
                if (implementedInSuperclass)
                    generateSuperListenerCall(method, paramNames, codeWriter);

                if (j+1 < methods.length || i+1 < listenersToImplement.length)
                    codeWriter.write("}\n\n"); // NOI18N
                else if (innerclass)
                    codeWriter.write("}\n"); // NOI18N
                else
                    codeWriter.write("}"); // last char // NOI18N
            }
        }

        if (innerclass)
            codeWriter.write("}"); // last char // NOI18N
    }

    // modifies the form class declaration to implement required listeners
    // (when event dispatching code is generated as CEDL_MAINCLASS)
    private void ensureMainClassImplementsListeners() {
        if (listenersInMainClass == listenersInMainClass_lastSet)
            return; // no change from last time

        if (listenersInMainClass != null
            && listenersInMainClass_lastSet != null
            && listenersInMainClass.length == listenersInMainClass_lastSet.length)
        {
            boolean different = false;
            for (int i=0; i < listenersInMainClass.length; i++)
                if (listenersInMainClass[i] != listenersInMainClass_lastSet[i]) {
                    different = true;
                    break;
                }
            if (!different)
                return; // no change from last time
        }

        final Set<String> toRemove = new HashSet<String>();
        if (listenersInMainClass_lastSet != null) {
            for (int i=0; i < listenersInMainClass_lastSet.length; i++) {
                Class cls = listenersInMainClass_lastSet[i];
                boolean remains = false;
                if (listenersInMainClass != null)
                    for (int j=0; j < listenersInMainClass.length; j++)
                        if (cls == listenersInMainClass[j]) {
                            remains = true;
                            break;
                        }
                if (!remains) {
                    toRemove.add(cls.getName());
                }
            }
        }

        final FileObject fo = formEditorSupport.getFormDataObject().getPrimaryFile();
        JavaSource js = JavaSource.forFileObject(fo);
        try {
            js.runModificationTask(new CancellableTask<WorkingCopy>() {
                public void cancel() {
                }
                public void run(WorkingCopy wcopy) throws Exception {
                    wcopy.toPhase(JavaSource.Phase.RESOLVED);
                    
                    ClassTree mainClassTree = findMainClass(wcopy, fo.getName());
                    ClassTree origMainTree = mainClassTree;
                    
                    TreePath classTreePath = wcopy.getTrees().getPath(wcopy.getCompilationUnit(), mainClassTree);
                    Element mainClassElm = wcopy.getTrees().getElement(classTreePath);
                            
                    if (mainClassElm != null) {
                        java.util.List<TypeElement> actualInterfaces = new ArrayList<TypeElement>();
                        TreeMaker maker = wcopy.getTreeMaker();
                        // first take the current interfaces and exclude the removed ones
                        int infIndex = 0;
                        for (TypeMirror infMirror: ((TypeElement) mainClassElm).getInterfaces()) {
                            TypeElement infElm = (TypeElement) wcopy.getTypes().asElement(infMirror);
                            actualInterfaces.add(infElm);
                            if (toRemove.contains(infElm.getQualifiedName().toString())) {
                                mainClassTree = maker.removeClassImplementsClause(mainClassTree, infIndex);
                            }
                            ++infIndex;
                        }

                        // then ensure all required interfaces are present
                        if (listenersInMainClass != null) {
                            for (int i=0; i < listenersInMainClass.length; i++) {
                                String name = listenersInMainClass[i].getName();
                                boolean alreadyIn = false;
                                for (TypeElement infElm: actualInterfaces)
                                    if (name.equals(infElm.getQualifiedName().toString())) {
                                        alreadyIn = true;
                                        break;
                                    }
                                if (!alreadyIn) {
                                    TypeElement inf2add = wcopy.getElements().getTypeElement(name);
                                    ExpressionTree infTree2add = inf2add != null
                                            ? maker.QualIdent(inf2add)
                                            : maker.Identifier(name);
                                    mainClassTree = maker.addClassImplementsClause(mainClassTree, infTree2add);
                                }
                            }
                        }

                        if (origMainTree != mainClassTree) {
                            wcopy.rewrite(origMainTree, mainClassTree);
                        }

                    }
                }
            }).commit();
            
            listenersInMainClass_lastSet = listenersInMainClass;
        } catch (IOException ex) {
            Logger.getLogger(JavaCodeGenerator.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }

    private static ClassTree findMainClass(CompilationController controller, String name) {
        for (Tree t: controller.getCompilationUnit().getTypeDecls()) {
            if (t.getKind() == Tree.Kind.CLASS &&
                    name.equals(((ClassTree) t).getSimpleName().toString())) {

                return (ClassTree) t;
            }
        }
        return null;
    }

    // ---------
    // generating general code structure (metadata from codestructure package)

    // java code for a statement
    private static String getStatementJavaString(CodeStatement statement,
                                                 String thisStr)
    {
        CodeExpression parent = statement.getParentExpression();
        String parentStr;
        if (parent != null) {
            parentStr = getExpressionJavaString(parent, thisStr);
            if ("this".equals(parentStr)) // NOI18N
                parentStr = thisStr;
        }
        else parentStr = null;

        CodeExpression[] params = statement.getStatementParameters();
        String[] paramsStr = new String[params.length];
        for (int i=0; i < params.length; i++)
            paramsStr[i] = getExpressionJavaString(params[i], thisStr);

        return statement.getJavaCodeString(parentStr, paramsStr);
    }

    // java code for an expression
    private static String getExpressionJavaString(CodeExpression exp,
                                                  String thisStr)
    {
        CodeVariable var = exp.getVariable();
        if (var != null)
            return var.getName();

        CodeExpressionOrigin origin = exp.getOrigin();
        if (origin == null)
            return null;

        CodeExpression parent = origin.getParentExpression();
        String parentStr;
        if (parent != null) {
            parentStr = getExpressionJavaString(parent, thisStr);
            if ("this".equals(parentStr)) // NOI18N
                parentStr = thisStr;
        }
        else parentStr = null;

        CodeExpression[] params = origin.getCreationParameters();
        String[] paramsStr = new String[params.length];
        for (int i=0; i < params.length; i++)
            paramsStr[i] = getExpressionJavaString(params[i], thisStr);

        return origin.getJavaCodeString(parentStr, paramsStr);
    }

    // ---------
    // Events

    private boolean anyEvents() {
        return formModel.getFormEvents().hasEventsInCEDL();
    }

    private String getListenerClassName() {
        if (listenerClassName == null) {
            String initText = formEditorSupport.getInitComponentSection().getText();
            int index = initText.lastIndexOf("private class "); // NOI18N
            if (index >= 0) {
                StringBuffer nameBuffer = new StringBuffer(16);
                index += "private class ".length(); // NOI18N

                int length = initText.length();
                while (index < length && initText.charAt(index) == ' ')
                    index++;

                int i = index;
                while (i < length && initText.charAt(i) != ' ')
                    nameBuffer.append(initText.charAt(i++));

                if (i < length)
                    listenerClassName = nameBuffer.toString();
            }

            if (listenerClassName == null) {
                javax.swing.text.Document document = formEditorSupport.getDocument();
                try {
                    String wholeText = document.getText(0, document.getLength());
                    listenerClassName = DEFAULT_LISTENER_CLASS_NAME;
                    while (wholeText.indexOf(listenerClassName) >= 0)
                        listenerClassName = "_" + listenerClassName; // NOI18N
                }
                catch (javax.swing.text.BadLocationException ex) {} // ignore
            }

            if (listenerClassName == null)
                listenerClassName = DEFAULT_LISTENER_CLASS_NAME;
        }

        return listenerClassName;
    }

    private String getListenerVariableName() {
        if (listenerVariableName == null) {
            listenerVariableName = "formListener"; // NOI18N
            CodeStructure codeStructure = formModel.getCodeStructure();
            for (int i=1; codeStructure.isVariableNameReserved(listenerVariableName); i++)
                listenerVariableName = "formListener" + i; // NOI18N
        }
        return listenerVariableName;
    }

    // -----------------------------------------------------------------------------
    // Event handlers

    /** Generates the specified event handler.
     */
    private void generateEventHandler(String handlerName,
                                      Method originalMethod,
                                      String bodyText)
    {
        if (!initialized || !canGenerate)
            return;

        InteriorSection sec = getEventHandlerSection(handlerName);
        if (sec != null && bodyText == null)
            return; // already exists, no need to generate

        IndentEngine engine = IndentEngine.find(formEditorSupport.getDocument());
        StringWriter buffer = new StringWriter();
        final SimpleSection initComponentsSection = formEditorSupport.getInitComponentSection();
        Writer codeWriter = engine.createWriter(
                        formEditorSupport.getDocument(),
                        initComponentsSection.getEndPosition().getOffset(),
                        buffer);

        try {
            if (sec == null) {
                sec = formEditorSupport.getGuardedSectionManager().createInteriorSection(
                          formEditorSupport.getDocument().createPosition(initComponentsSection.getEndPosition().getOffset() + 1),
                          getEventSectionName(handlerName));
            }
            int i1, i2;

            generateListenerMethodHeader(handlerName, originalMethod, codeWriter);
            codeWriter.flush();
            i1 = buffer.getBuffer().length();
            if (bodyText == null)
                bodyText = getDefaultEventBody();
            codeWriter.write(bodyText);
            codeWriter.flush();
            i2 = buffer.getBuffer().length();
            codeWriter.write("}\n"); // footer with new line // NOI18N
            codeWriter.flush();

            sec.setHeader(buffer.getBuffer().substring(0,i1));
            sec.setBody(buffer.getBuffer().substring(i1,i2));
            sec.setFooter(buffer.getBuffer().substring(i2));

            codeWriter.close();
        } 
        catch (javax.swing.text.BadLocationException e) {
            return;
        }
        catch (java.io.IOException ioe) {
            return;
        }

        clearUndo();
    }

    /** Removes the specified event handler - removes the whole method together with the user code!
     * @param handlerName The name of the event handler
     */
    private boolean deleteEventHandler(String handlerName) {
        InteriorSection section = getEventHandlerSection(handlerName);
        if (section == null || !initialized || !canGenerate)
            return false;

        section.deleteSection();
        clearUndo();

        return true;
    }

    private String getDefaultEventBody() {
        return FormUtils.getBundleString("MSG_EventHandlerBody"); // NOI18N
    }

    /** Renames the specified event handler to the given new name.
     * @param oldHandlerName The old name of the event handler
     * @param newHandlerName The new name of the event handler
     */
    private void renameEventHandler(String oldHandlerName,
                                    String newHandlerName)
    {
        InteriorSection sec = getEventHandlerSection(oldHandlerName);
        if (sec == null || !initialized || !canGenerate)
            return;

        String header = sec.getHeader();

        // find the old handler name in the handler method header
        int index = header.indexOf('(');
        if (index < 0)
            return; // should not happen unless the handler code is corrupted
        index = header.substring(0, index).lastIndexOf(oldHandlerName);
        if (index < 0)
            return; // old name not found; should not happen

        IndentEngine engine = IndentEngine.find(formEditorSupport.getDocument());
        StringWriter buffer = new StringWriter();
        Writer codeWriter = engine.createWriter(formEditorSupport.getDocument(),
                                                sec.getStartPosition().getOffset(),
                                                buffer);
        try {
            codeWriter.write(header.substring(0, index));
            codeWriter.write(newHandlerName);
            codeWriter.write(header.substring(index + oldHandlerName.length()));
            codeWriter.flush();
            int i1 = buffer.getBuffer().length();
            codeWriter.write("}\n"); // NOI18N // footer with new line
            codeWriter.flush();

            sec.setHeader(buffer.getBuffer().substring(0, i1));
            sec.setFooter(buffer.getBuffer().substring(i1));
            sec.setName(getEventSectionName(newHandlerName));

            codeWriter.close();
        } 
        catch (java.beans.PropertyVetoException e) {
            return;
        }
        catch (IOException e) {
            return;
        }

        clearUndo();
    }

    /** Focuses the specified event handler in the editor. */
    private void gotoEventHandler(String handlerName) {
        InteriorSection sec = getEventHandlerSection(handlerName);
        if (sec != null && initialized) {
            formEditorSupport.openAt(sec.getCaretPosition());
        }
    }

    /** Gets the body (text) of event handler of given name. */
    String getEventHandlerText(String handlerName) {
        InteriorSection section = getEventHandlerSection(handlerName);
        if (section != null) {
            // XXX try to use section.getBody instead
            String tx = section.getText();
            tx = tx.substring(tx.indexOf("{")+1, tx.lastIndexOf("}")).trim() + "\n"; // NOI18N
            return tx;
        }
        return null;
    }

    // ------------------------------------------------------------------------------------------
    // Private methods

    /** Clears undo buffer after code generation */
    private void clearUndo() {
        formEditorSupport.discardEditorUndoableEdits();
    }

    // sections acquirement

    private InteriorSection getEventHandlerSection(String eventName) {
        return formEditorSupport.getGuardedSectionManager().findInteriorSection(getEventSectionName(eventName));
    }

    // other

    private String getEventSectionName(String handlerName) {
        return EVT_SECTION_PREFIX + handlerName;
    }

    private String[] generateListenerMethodHeader(String methodName,
                                                  Method originalMethod,
                                                  Writer writer)
        throws IOException
    {
        Class[] paramTypes = originalMethod.getParameterTypes();
        String[] paramNames;

        if (paramTypes.length == 1
            && EventObject.class.isAssignableFrom(paramTypes[0]))
        {
            paramNames = new String[] { formSettings.getEventVariableName() };
        }
        else {
            paramNames = new String[paramTypes.length];
            for (int i=0; i < paramTypes.length; i++)
                paramNames[i] = "param" + i; // NOI18N
        }

        // generate the method
        writer.write(methodName != null ? "private " : "public "); // NOI18N
        writer.write(getSourceClassName(originalMethod.getReturnType()));
        writer.write(" "); // NOI18N
        writer.write(methodName != null ? methodName : originalMethod.getName());
        writer.write("("); // NOI18N

        for (int i=0; i < paramTypes.length; i++) {
            writer.write(getSourceClassName(paramTypes[i]));
            writer.write(" "); // NOI18N
            writer.write(paramNames[i]);
            if (i + 1 < paramTypes.length)
                writer.write(", "); // NOI18N
        }
        writer.write(")"); // NOI18N

        Class[] exceptions = originalMethod.getExceptionTypes();
        if (exceptions.length != 0) {
            writer.write("throws "); // NOI18N
            for (int i=0; i < exceptions.length; i++) {
                writer.write(getSourceClassName(exceptions[i]));
                if (i + 1 < exceptions.length)
                    writer.write(", "); // NOI18N
            }
        }

        writer.write(" {\n"); // NOI18N

        return paramNames;
    }

    private void generateSuperListenerCall(Method method,
                                           String[] paramNames,
                                           Writer codeWriter)
        throws IOException
    {
        if (method.getReturnType() != Void.TYPE)
            codeWriter.write("return "); // NOI18N

        codeWriter.write("super."); // NOI18N
        codeWriter.write(method.getName());
        codeWriter.write("("); // NOI18N

        for (int i=0; i < paramNames.length; i++) {
            codeWriter.write(paramNames[i]);
            if (i + 1 < paramNames.length)
                codeWriter.write(", "); // NOI18N
        }

        codeWriter.write(");\n"); // NOI18N
    }

    private void generateEventHandlerCalls(Event event,
                                           String[] paramNames,
                                           Writer codeWriter,
                                           boolean useShortNameIfPossible)
        throws IOException
    {
        String mainClassRef = null;

        String[] handlers = event.getEventHandlers();
        for (int i=0; i < handlers.length; i++) {
            if (i + 1 == handlers.length
                    && event.getListenerMethod().getReturnType() != Void.TYPE)
                codeWriter.write("return "); // NOI18N

            // with anonymous innerclasses, try to avoid generating full names
            // (for the reason some old forms might be used as innerclasses)
            if (!useShortNameIfPossible
                || event.getListenerMethod().getName().equals(handlers[i]))
            {
                if (mainClassRef == null)
                    mainClassRef = formEditorSupport.getFormDataObject().getName()
                                   + ".this."; // NOI18N
                codeWriter.write(mainClassRef);
            }
            codeWriter.write(handlers[i]);
            codeWriter.write("("); // NOI18N

            for (int j=0; j < paramNames.length; j++) {
                codeWriter.write(paramNames[j]);
                if (j + 1 < paramNames.length)
                    codeWriter.write(", "); // NOI18N
            }

            codeWriter.write(");\n"); // NOI18N
        }
    }
    
    void regenerateCode() {
        if (!codeUpToDate) {	    
            codeUpToDate = true;
            regenerateVariables();
            regenerateInitComponents();
            ensureMainClassImplementsListeners();            
            FormModel.t("code regenerated"); // NOI18N	    
        }
    }

    static CustomCodeData getCodeData(RADComponent metacomp) {
        CodeGenerator gen = FormEditor.getCodeGenerator(metacomp.getFormModel());
        return gen instanceof JavaCodeGenerator ?
            ((JavaCodeGenerator)gen).getCodeData0(metacomp) : null;
    }

    private CustomCodeData getCodeData0(RADComponent metacomp) {
        CustomCodeData codeData = new CustomCodeData();
        codeData.setDefaultCategory(CustomCodeData.CodeCategory.CREATE_AND_INIT);
        CodeWriter codeWriter = new CodeWriter(new StringWriter(1024), true);
        cleanup();

        CodeVariable var = metacomp.getCodeExpression().getVariable();

        try { // creation & init code
            if (var != null && !isFinalFieldVariable(var.getType()))
                generateComponentCreate(metacomp, codeWriter, true, codeData);
            // with final field variable the creation statement is part of declaration

            addInitCode(metacomp, codeWriter, codeData);

            if (var != null) { // add declaration
                boolean fieldVariable = (var.getType() & CodeVariable.SCOPE_MASK) == CodeVariable.FIELD;
                if (fieldVariable) {
                    codeData.setDefaultCategory(CustomCodeData.CodeCategory.DECLARATION);
                    generateComponentFieldVariable(metacomp, codeWriter, codeData);
                }
                codeData.setDeclarationData(!fieldVariable, var.getType() & CodeVariable.ALL_MODIF_MASK);
            }
        }
        catch (IOException ex) { // should not happen
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }

        cleanup();

        return codeData;
    }

    private String indentCode(String code) {
        return indentCode(code, 0, null);
    }

    private String indentCode(String code, int minIndentLevel, IndentEngine refEngine) {
        int spacesPerTab = 4;
        boolean braceOnNewLine = false;
        
        if (refEngine != null) {
            Class engineClass = refEngine.getClass();
            try {
                Method m = engineClass.getMethod("getSpacesPerTab", // NOI18N
                        new Class[0]);
                spacesPerTab = ((Integer)m.invoke(refEngine, new Object[0]))
                .intValue();
            } catch (Exception ex) {} // ignore
            
            try {
                Method m = engineClass.getMethod("getJavaFormatNewlineBeforeBrace", // NOI18N
                        new Class[0]);
                braceOnNewLine = ((Boolean)m.invoke(refEngine, new Object[0]))
                .booleanValue();
            } catch (Exception ex) {} // ignore
        }
        
        StringBuffer tab = new StringBuffer(spacesPerTab);
        for (int i=0; i < spacesPerTab; i++)
            tab.append(" "); // NOI18N
        
        return doIndentation(code, minIndentLevel, tab.toString(), braceOnNewLine);
    }
    
    // simple indentation method
    private static String doIndentation(String code,
            int minIndentLevel,
            String tab,
            boolean braceOnNewLine) {

        int indentLevel = minIndentLevel;
        boolean lastLineEmpty = false;
        int codeLength = code.length();
        StringBuffer buffer = new StringBuffer(codeLength);
        
        int i = 0;
        while (i < codeLength) {
            int lineStart = i;
            int lineEnd;
            boolean startingSpace = true;
            boolean firstClosingBr = false;
            boolean closingBr = false;
            int lastOpeningBr = -1;
            int endingSpace = -1;
            boolean insideString = false;
            int brackets = 0;
            char c;
            
            do { // go through one line
                c = code.charAt(i);
                if (!insideString) {
                    if (c == '}') {
                        lastOpeningBr = -1;
                        endingSpace = -1;
                        if (startingSpace) { // first non-space char on the line
                            firstClosingBr = true;
                            closingBr = true;
                            startingSpace = false;
                            lineStart = i;
                        } else if (!closingBr)
                            brackets--;
                    } else if (c == ')') {
                        
                        int bracketCount = 0;
                        int begin = i;
                        while (begin < code.length() && (code.charAt(begin) == ')')) {
                            bracketCount += 1;
                            begin += 1;
                        }
                        
                        lastOpeningBr = -1;
                        endingSpace = -1;
                        if (startingSpace) { // first non-space char on the line
                            firstClosingBr = true;
                            closingBr = true;
                            startingSpace = false;
                            lineStart = i;
                        } else if (!closingBr)
                            brackets -= bracketCount;
                        if (bracketCount > 1) {
                            i += bracketCount - 1;
                        }
                    } else if (c == '{' || c == '(') {
                        closingBr = false;
                        lastOpeningBr = -1;
                        endingSpace = -1;
                        if (startingSpace) { // first non-space char on the line
                            startingSpace = false;
                            lineStart = i;
                        } else if (c == '{') // possible last brace on the line
                            lastOpeningBr = i;
                        brackets++;
                    } else if (c == '\"') { // start of String, its content is ignored
                        insideString = true;
                        lastOpeningBr = -1;
                        endingSpace = -1;
                        if (startingSpace) { // first non-space char on the line
                            startingSpace = false;
                            lineStart = i;
                        }
                        
                    } else if (c == ' ' || c == '\t') {
                        if (endingSpace < 0)
                            endingSpace = i;
                    } else {
                        if (startingSpace) { // first non-space char on the line
                            startingSpace = false;
                            lineStart = i;
                        }
                        if (c != '\n') { // this char is not a whitespace
                            endingSpace = -1;
                            if (lastOpeningBr > -1)
                                lastOpeningBr = -1;
                        }
                    }
                } else if (c == '\"' && code.charAt(i-1) != '\\') // end of String
                    insideString = false;
                
                i++;
            }
            while (c != '\n' && i < codeLength);
            
            if ((i-1 == lineStart && code.charAt(lineStart) == '\n')
            || (i-2 == lineStart && code.charAt(lineStart) == '\r')) {
                // the line is empty
                if (!lastLineEmpty) {
                    buffer.append("\n"); // NOI18N
                    lastLineEmpty = true;
                }
                continue; // skip second and more empty lines
            } else lastLineEmpty = false;
            
            // adjust indentation level for the line
            if (firstClosingBr) { // the line starts with } or )
                if (indentLevel > minIndentLevel)
                    indentLevel--;
                if (brackets < 0)
                    brackets = 0; // don't change indentation for the next line
            }
            
            // write indentation space
            for (int j=0; j < indentLevel; j++)
                buffer.append(tab);
            
            if (lastOpeningBr > -1 && braceOnNewLine) {
                // write the line without last opening brace
                // (indentation option "Add New Line Before Brace")
                endingSpace = lastOpeningBr;
                c = code.charAt(endingSpace-1);
                while (c == ' ' || c == '\t') {
                    endingSpace--;
                    c = code.charAt(endingSpace-1);
                }
                i = lastOpeningBr;
                brackets = 0;
            }
            
            // calculate line end
            if (endingSpace < 0) {
                if (c == '\n')
                    if (code.charAt(i-2) == '\r')
                        lineEnd = i-2; // \r\n at the end of the line
                    else
                        lineEnd = i-1; // \n at the end of the line
                else
                    lineEnd = i; // end of whole code string
            } else // skip spaces at the end of the line
                lineEnd = endingSpace;
            
            // write the line
            buffer.append(code.substring(lineStart, lineEnd));
            buffer.append("\n"); // NOI18N
            
            // calculate indentation level for the next line
            if (brackets < 0) {
                if (indentLevel > minIndentLevel)
                    indentLevel += brackets;
                
            } else if (brackets > 0)
                indentLevel++;
        }
        return buffer.toString();
    }

    private String replaceCode(String code, Writer initWriter) throws IOException {
        int idx = code.indexOf(RESOURCE_BUNDLE_OPENING_CODE);
        if (idx >= 0) {
            int endIdx = code.indexOf(RESOURCE_BUNDLE_CLOSING_CODE,
                                      idx + RESOURCE_BUNDLE_OPENING_CODE.length());
            if (endIdx >= 0) {
                String bundleLocation = code.substring(idx + RESOURCE_BUNDLE_OPENING_CODE.length(), endIdx);
                if (bundleVariables == null) {
                    bundleVariables = new HashMap();
                }
                String varName = bundleVariables.get(bundleLocation);
                if (varName == null) {
                    varName = formModel.getCodeStructure().getExternalVariableName(ResourceBundle.class, "bundle", true); // NOI18N
                    bundleVariables.put(bundleLocation, varName);
                    initWriter.write("java.util.ResourceBundle " + varName + " = " // NOI18N
                            + code.substring(idx, endIdx + 1) + "; // NOI18N\n"); // NOI18N
                }
                code = code.substring(0, idx) + varName + code.substring(endIdx + 1);
            }
        }
        return code;
    }

    /**
     * Class for filtering generated code - processing special marks in the code
     * (provided by properties/property editors). This way e.g. code for
     * ResourceBundle.getBundle is optimized (caching the bundle in a variable)
     * or line comments for property setters are placed correctly.
     * [In future pre-init and post-init code could be done this way as well
     *  (and it would work also for nested properties or layout constraints).]
     * To work correctly, this class requires to be given complete statements
     * (so it can add a preceding or following statement).
     */
    private class CodeWriter {
        private Writer writer;
        private boolean inMethod;

        CodeWriter(Writer writer, boolean method) {
            this.writer = writer;
            this.inMethod = method;
        }

        void write(String str) throws IOException {
            int idx = str.indexOf(CODE_MARK);
            if (idx >= 0) {
                StringBuilder buf = new StringBuilder(str.length());
                if (idx > 0) {
                    buf.append(str.substring(0, idx));
                }
                String lineComment = null;

                do {
                    String part;
                    if (str.startsWith(MARKED_PROPERTY_CODE, idx)) {
                        int sub = idx + MARKED_PROPERTY_CODE.length();
                        idx = str.indexOf(CODE_MARK, sub);
                        part = idx < 0 ? str.substring(sub) : str.substring(sub, idx);
                        if (inMethod) {
                            part = replaceCode(part, writer);
                        } // can't replace in field variable init
                    }
                    else if (str.startsWith(PROPERTY_LINE_COMMENT, idx)) {
                        int sub = idx + PROPERTY_LINE_COMMENT.length();
                        idx = str.indexOf(CODE_MARK, sub);
                        String lc = idx < 0 ? str.substring(sub) : str.substring(sub, idx);
                        if (lineComment == null)
                            lineComment = lc;
                        else if (!lineComment.equals(lc))
                            lineComment = lineComment + " " + lc; // NOI18N
                        continue;
                    }
                    else {
                        int sub = idx;
                        idx = str.indexOf(CODE_MARK, sub);
                        part = idx < 0 ? str.substring(sub) : str.substring(sub, idx);
                    }
                    if (lineComment != null) {
                        int eol = part.indexOf('\n');
                        if (eol >= 0) {
                            buf.append(part.substring(0, eol));
                            buf.append(" // "); // NOI18N
                            buf.append(lineComment);
                            buf.append("\n"); // NOI18N
                            part = part.substring(eol+1);
                            lineComment = null;
                        }
                    }
                    buf.append(part);
                }
                while (idx >= 0);

                if (lineComment != null) {
                    buf.append(" // "); // NOI18N
                    buf.append(lineComment);
                }

                str = buf.toString();
            }
            writer.write(str);
        }

        Writer getWriter() {
            return writer;
        }

        void clearBuffer() {
            if (writer instanceof StringWriter) {
                StringBuffer buf = ((StringWriter)writer).getBuffer();
                buf.delete(0, buf.length());
            }
        }

        public String extractString() {
            String str = writer.toString();
            clearBuffer();
            return str;
        }
    }

    //
    // {{{ FormListener
    //

    private class FormListener implements FormModelListener {

        public void formChanged(FormModelEvent[] events) {
            if (events == null)
                return;

            boolean modifying = false;
            boolean toBeSaved = false;
            boolean toBeClosed = false;

            for (int i=0; i < events.length; i++) {
                FormModelEvent ev = events[i];

                // form loaded
                if (ev.getChangeType() == FormModelEvent.FORM_LOADED) {
                    if (formModel.getSettings().getListenerGenerationStyle() == CEDL_MAINCLASS)
                        listenersInMainClass_lastSet =
                            formModel.getFormEvents().getCEDLTypes();
                    continue;
                }

                if (ev.isModifying())
                    modifying = true;

                if (ev.getChangeType() == FormModelEvent.EVENT_HANDLER_ADDED) {
                    String handlerName = ev.getEventHandler();
                    String bodyText = ev.getNewEventHandlerContent();
                    if ((ev.getCreatedDeleted() || bodyText != null) && ev.getComponent().isInModel()) {
                        if (!ev.getCreatedDeleted())
                            ev.setOldEventHandlerContent(
                                getEventHandlerText(handlerName));

                        generateEventHandler(handlerName,
                                            (ev.getComponentEvent() == null) ?
                                                formModel.getFormEvents().getOriginalListenerMethod(handlerName) :
                                                ev.getComponentEvent().getListenerMethod(),
                                             bodyText);
                    }
                    if (events.length == 1 && bodyText == null)
                        gotoEventHandler(handlerName);
                }
                else if (ev.getChangeType() == FormModelEvent.EVENT_HANDLER_REMOVED) {
                    if (ev.getCreatedDeleted()) {
                        String handlerName = ev.getEventHandler();
                        ev.setOldEventHandlerContent(
                            getEventHandlerText(handlerName));
                        deleteEventHandler(handlerName);
                    }
                }
                else if (ev.getChangeType() == FormModelEvent.EVENT_HANDLER_RENAMED) {
                    renameEventHandler(ev.getOldEventHandler(),
                                       ev.getNewEventHandler());
                }
                else if (ev.getChangeType() == FormModelEvent.FORM_TO_BE_SAVED)
                    toBeSaved = true;
                else if (ev.getChangeType() == FormModelEvent.FORM_TO_BE_CLOSED)
                    toBeClosed = true;
            }

            if (modifying)
                codeUpToDate = false;

            if ((!codeUpToDate && toBeSaved) || (isJavaEditorDisplayed())) {
		regenerateCode();
            }

            if (toBeSaved) {
                RADComponent[] components =
                    formModel.getModelContainer().getSubBeans();
                for (int i=0; i < components.length; i++)
                    serializeComponentsRecursively(components[i]);
            }
        }
        
        private boolean isJavaEditorDisplayed() {
            boolean showing = false;
            if (EventQueue.isDispatchThread()) { // issue 91715
                JEditorPane[] jeditPane = FormEditor.getFormDataObject(formModel).getFormEditorSupport().getOpenedPanes();
                if (jeditPane != null) {
                    for (int i=0; i<jeditPane.length; i++) {
                        if (showing = jeditPane[i].isShowing()) {
                            break;
                        }
                    }
                }
            }
            return showing;
        }
        
        private void serializeComponentsRecursively(RADComponent comp) {
            Object value = comp.getAuxValue(AUX_CODE_GENERATION);
            if (comp.hasHiddenState()
                    || (value != null && VALUE_SERIALIZE.equals(value))) {
                String serializeTo =(String)comp.getAuxValue(AUX_SERIALIZE_TO);
                if (serializeTo != null) {
                    try {
                        FileObject fo = formEditorSupport.getFormDataObject().getPrimaryFile();
                        FileObject serFile = fo.getParent().getFileObject(serializeTo, "ser"); // NOI18N
                        if (serFile == null) {
                            serFile = fo.getParent().createData(serializeTo, "ser"); // NOI18N
                        }
                        if (serFile != null) {
                            FileLock lock = null;
                            ObjectOutputStream oos = null;
                            try {
                                lock = serFile.lock();
                                oos = new OOS(serFile.getOutputStream(lock));
                                if (comp instanceof RADVisualContainer) {
                                    // [PENDING - remove temporarily the subcomponents]
                                }
                                oos.writeObject(comp.getBeanInstance());
                            } finally {
                                if (oos != null) oos.close();
                                if (lock != null) lock.releaseLock();
                            }
                        } else {
                            // [PENDING - handle problem]
                        }
                    } catch (java.io.NotSerializableException e) {
                        e.printStackTrace();
                        // [PENDING - notify error]
                    } catch (java.io.IOException e) {
                        e.printStackTrace();
                        // [PENDING - notify error]
                    } catch (Exception e) {
                        e.printStackTrace();
                        // [PENDING - notify error]
                    }
                } else {
                    // [PENDING - notify error]
                }
            }
            if (comp instanceof ComponentContainer) {
                RADComponent[] children =((ComponentContainer)comp).getSubBeans();
                for (int i = 0; i < children.length; i++) {
                    serializeComponentsRecursively(children[i]);
                }
            }
        }
    }

    // hacked ObjectOutputStream - to replace special values used by property
    // editors (like SuperColor from ColorEditor or NbImageIcon from IconEditor)
    private static class OOS extends ObjectOutputStream {
        OOS(OutputStream out) throws IOException {
            super(out);
            enableReplaceObject(true);
        }

        protected Object replaceObject(Object obj) throws IOException {
            if (obj.getClass().getName().startsWith("org.netbeans.") // NOI18N
                || obj.getClass().getName().startsWith("org.openide.")) // NOI18N
            {
                if (obj instanceof java.awt.Color)
                    return new java.awt.Color(((java.awt.Color)obj).getRGB());
                if (obj instanceof javax.swing.ImageIcon)
                    return new javax.swing.ImageIcon(
                        ((javax.swing.ImageIcon)obj).getImage());
            }
            return obj;
        }
    }

    //
    // {{{ CodeGenerateEditor
    //

    final public static class CodeGenerateEditor extends PropertyEditorSupport
    {
        private RADComponent component;

        /** Display Names for alignment. */
        private static final String generateName =
            FormUtils.getBundleString("VALUE_codeGen_generate"); // NOI18N
        private static final String serializeName =
            FormUtils.getBundleString("VALUE_codeGen_serialize"); // NOI18N

        public CodeGenerateEditor(RADComponent component) {
            this.component = component;
        }

        /** @return names of the possible directions */
        public String[] getTags() {
            if (component.hasHiddenState()) {
                return new String[] { serializeName } ;
            } else {
                return new String[] { generateName, serializeName } ;
            }
        }

        /** @return text for the current value */
        public String getAsText() {
            Integer value =(Integer)getValue();
            if (value.equals(VALUE_SERIALIZE)) return serializeName;
            else return generateName;
        }

        /** Setter.
         * @param str string equal to one value from directions array
         */
        public void setAsText(String str) {
            if (component.hasHiddenState()) {
                setValue(VALUE_SERIALIZE);
            } else {
                if (serializeName.equals(str)) {
                    setValue(VALUE_SERIALIZE);
                } else if (generateName.equals(str)) {
                    setValue(VALUE_GENERATE_CODE);
                }
            }
        }
    }

    // }}}

    //
    // {{{ CodeProperty
    //

    private class CodeProperty extends FormProperty {
        // using FormProperty to be able to disable change firing for temporary
        // changes in CodeCustomizer
        private String auxKey;
        private RADComponent component;
        
        CodeProperty(RADComponent metacomp,
                     String propertyName, String auxKey ,
                     String displayName, String shortDescription)
        {
            super(propertyName, String.class, displayName, null);
            setShortDescription(shortDescription); // FormProperty adds the type to the tooltip
            this.auxKey = auxKey;
            component = metacomp;
            try {
                reinstateProperty();
            }
            catch (Exception ex) { // should not happen
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }

        public void setTargetValue(Object value) {
            if (value != null && !(value instanceof String))
                throw new IllegalArgumentException();

            Object oldValue = getTargetValue();

            if (value != null && !value.equals("")) // NOI18N
                component.setAuxValue(auxKey, value);
            else if (component.getAuxValue(auxKey) != null) {
                component.getAuxValues().remove(auxKey);
            }
        }

        public Object getTargetValue() {
            Object value = component.getAuxValue(auxKey);
            if (value == null)
                value = ""; // NOI18N
            return value;
        }

        public boolean supportsDefaultValue () {
            return true;
        }

        public Object getDefaultValue() {
            return ""; // NOI18N
        }

        protected void propertyValueChanged(Object old, Object current) {
            super.propertyValueChanged(old, current);
            if (isChangeFiring()) {
                formModel.fireSyntheticPropertyChanged(
                    component, getName(), old, current);
                if (component.getNodeReference() != null) {
                    component.getNodeReference().firePropertyChangeHelper(
                        getName(), null, null);
                }
            }
        }

        public PropertyEditor getExpliciteEditor() {
            return new PropertyEditorSupport() {
                public Component getCustomEditor() {
                    return new CustomCodeEditor(CodeProperty.this,
                                                FormEditor.createCodeEditorPane(formModel));
                }

                public boolean supportsCustomEditor() {
                    return true;
                }
            };
        }

        public boolean canWrite() {
            return JavaCodeGenerator.this.canGenerate && !formModel.isReadOnly();
        }
    }

    // }}}
    
    // Properties
    
    private class VariablesModifierProperty extends PropertySupport.ReadWrite {
        
        private VariablesModifierProperty() {
            super(PROP_VARIABLE_MODIFIER,
                Integer.class,
                FormUtils.getBundleString("PROP_VARIABLES_MODIFIER"), // NOI18N
                FormUtils.getBundleString("HINT_VARIABLES_MODIFIER")); // NOI18N
        }
            
        public void setValue(Object value) {
            if (!(value instanceof Integer))
                throw new IllegalArgumentException();
            
            Integer oldValue = (Integer)getValue();
            Integer newValue = (Integer)value;
            int varType;
            int variablesModifier = newValue.intValue();
            if (formModel.getSettings().getVariablesLocal()) {
                varType = CodeVariable.LOCAL | (variablesModifier & CodeVariable.FINAL); // | CodeVariable.EXPLICIT_DECLARATION;
            } else varType = CodeVariable.FIELD | variablesModifier;

            formModel.getCodeStructure().setDefaultVariableType(varType);
            formModel.getSettings().setVariablesModifier(variablesModifier);
            formModel.fireSyntheticPropertyChanged(null, PROP_VARIABLE_MODIFIER, oldValue, newValue);
            FormEditor formEditor = FormEditor.getFormEditor(formModel);
            formEditor.getFormRootNode().firePropertyChangeHelper(
                PROP_VARIABLE_MODIFIER, oldValue, newValue);
        }
        
        public Object getValue() {
            return new Integer(formModel.getSettings().getVariablesModifier());
        }

        public boolean supportsDefaultValue() {
            return true;
        }
        
        public void restoreDefaultValue() {
            setValue(new Integer(FormLoaderSettings.getInstance().getVariablesModifier()));
        }
        
        public boolean isDefaultValue() {
            return (formModel.getSettings().getVariablesModifier() ==
                FormLoaderSettings.getInstance().getVariablesModifier());
        }
        
        public boolean canWrite() {
            return JavaCodeGenerator.this.canGenerate && !JavaCodeGenerator.this.formModel.isReadOnly();
        }
        
        public PropertyEditor getPropertyEditor() {
            boolean local = formModel.getSettings().getVariablesLocal();
            return local ? new ModifierEditor(Modifier.FINAL) :
                new ModifierEditor(Modifier.PUBLIC
                    | Modifier.PROTECTED
                    | Modifier.PRIVATE
                    | Modifier.STATIC
                    | Modifier.FINAL
                    | Modifier.TRANSIENT
                    | Modifier.VOLATILE);
        }
        
    }
    
    private class LocalVariablesProperty extends PropertySupport.ReadWrite {

        private LocalVariablesProperty() {
            super(PROP_VARIABLE_LOCAL,
                Boolean.TYPE,
                FormUtils.getBundleString("PROP_VARIABLES_LOCAL"), // NOI18N
                FormUtils.getBundleString("HINT_VARIABLES_LOCAL")); // NOI18N
        }
        
        public void setValue(Object value) {
            if (!(value instanceof Boolean))
                throw new IllegalArgumentException();            
            if (value.equals(getValue())) return;
            
            Boolean oldValue = (Boolean)getValue();
            Boolean newValue = (Boolean)value;
            FormSettings formSettings = formModel.getSettings();
            boolean variablesLocal = newValue.booleanValue();
            int variablesModifier = variablesLocal ? (formSettings.getVariablesModifier() & CodeVariable.FINAL)
                : formSettings.getVariablesModifier();
            Integer oldModif = new Integer(formModel.getSettings().getVariablesModifier());
            Integer newModif = new Integer(variablesModifier);
            int varType = variablesLocal ?
                CodeVariable.LOCAL | variablesModifier // | CodeVariable.EXPLICIT_DECLARATION
                : CodeVariable.FIELD | variablesModifier;

            formModel.getCodeStructure().setDefaultVariableType(varType);
            formSettings.setVariablesLocal(variablesLocal);
            formSettings.setVariablesModifier(variablesModifier);
            formModel.fireSyntheticPropertyChanged(null, PROP_VARIABLE_LOCAL, oldValue, newValue);
            formModel.fireSyntheticPropertyChanged(null, PROP_VARIABLE_MODIFIER, oldModif, newModif);
            FormEditor formEditor = FormEditor.getFormEditor(formModel);
            FormNode formRootNode = formEditor.getFormRootNode();
            formRootNode.firePropertyChangeHelper(
                PROP_VARIABLE_LOCAL, oldValue, newValue);
            formRootNode.firePropertyChangeHelper(
                PROP_VARIABLE_MODIFIER, oldModif, newModif);
        }
        
        public Object getValue() {
            return Boolean.valueOf(formModel.getSettings().getVariablesLocal());
        }
        
        public boolean supportsDefaultValue() {
            return true;
        }
        
        public void restoreDefaultValue() {
            setValue(Boolean.valueOf(FormLoaderSettings.getInstance().getVariablesLocal()));
        }
        
        public boolean isDefaultValue() {
            return (formModel.getSettings().getVariablesLocal() == 
                FormLoaderSettings.getInstance().getVariablesLocal());
        }
        
        public boolean canWrite() {
            return JavaCodeGenerator.this.canGenerate && !JavaCodeGenerator.this.formModel.isReadOnly();
        }
        
    }
    
    private class GenerateMnemonicsCodeProperty extends PropertySupport.ReadWrite {
        
        private GenerateMnemonicsCodeProperty() {
            super(PROP_GENERATE_MNEMONICS,
                Boolean.TYPE,
                FormUtils.getBundleString("PROP_GENERATE_MNEMONICS"), // NOI18N
                FormUtils.getBundleString("HINT_GENERATE_MNEMONICS2")); // NOI18N
        }
            
        public void setValue(Object value) {
            if (!(value instanceof Boolean))
                throw new IllegalArgumentException();
            
            Boolean oldValue = (Boolean)getValue();
            Boolean newValue = (Boolean)value;
            formModel.getSettings().setGenerateMnemonicsCode(newValue.booleanValue());
            formModel.fireSyntheticPropertyChanged(null, PROP_GENERATE_MNEMONICS, oldValue, newValue);
            FormEditor formEditor = FormEditor.getFormEditor(formModel);
            formEditor.getFormRootNode().firePropertyChangeHelper(
                PROP_GENERATE_MNEMONICS, oldValue, newValue);
        }
        
        public Object getValue() {
            return Boolean.valueOf(formModel.getSettings().getGenerateMnemonicsCode());
        }
        
        public boolean canWrite() {
            return JavaCodeGenerator.this.canGenerate && !JavaCodeGenerator.this.formModel.isReadOnly();
        }
        
        public boolean supportsDefaultValue() {
            return true;
        }
        
        public void restoreDefaultValue() {
            setValue(Boolean.valueOf(FormLoaderSettings.getInstance().getGenerateMnemonicsCode()));
        }
        
        public boolean isDefaultValue() {
            return (formModel.getSettings().getGenerateMnemonicsCode() == 
                FormLoaderSettings.getInstance().getGenerateMnemonicsCode());
        }
        
    }

    private class ListenerGenerationStyleProperty extends PropertySupport.ReadWrite {
        
        private ListenerGenerationStyleProperty() {
            super(PROP_LISTENER_GENERATION_STYLE,
                Integer.class,
                FormUtils.getBundleString("PROP_LISTENER_GENERATION_STYLE"), // NOI18N
                FormUtils.getBundleString("HINT_LISTENER_GENERATION_STYLE")); // NOI18N
        }
            
        public void setValue(Object value) {
            if (!(value instanceof Integer))
                throw new IllegalArgumentException();
            
            Integer oldValue = (Integer)getValue();
            Integer newValue = (Integer)value;
            formModel.getSettings().setListenerGenerationStyle(newValue.intValue());
            formModel.fireSyntheticPropertyChanged(null, PROP_LISTENER_GENERATION_STYLE, oldValue, newValue);
            FormEditor formEditor = FormEditor.getFormEditor(formModel);
            formEditor.getFormRootNode().firePropertyChangeHelper(
                PROP_LISTENER_GENERATION_STYLE, oldValue, newValue);
        }
        
        public Object getValue() {
            return new Integer(formModel.getSettings().getListenerGenerationStyle());
        }

        public boolean supportsDefaultValue() {
            return true;
        }
        
        public void restoreDefaultValue() {
            setValue(new Integer(FormLoaderSettings.getInstance().getListenerGenerationStyle()));
        }
        
        public boolean isDefaultValue() {
            return (formModel.getSettings().getListenerGenerationStyle() ==
                FormLoaderSettings.getInstance().getListenerGenerationStyle());
        }
        
        public boolean canWrite() {
            return JavaCodeGenerator.this.canGenerate && !JavaCodeGenerator.this.formModel.isReadOnly();
        }
        
        public PropertyEditor getPropertyEditor() {
            return new FormLoaderSettingsBeanInfo.ListenerGenerationStyleEditor();
        }
        
    }

    // analogical to ListenerGenerationStyleProperty ...
    private class LayoutCodeTargetProperty extends PropertySupport.ReadWrite {
        
        private LayoutCodeTargetProperty() {
            super(FormLoaderSettings.PROP_LAYOUT_CODE_TARGET,
                Integer.class,
                FormUtils.getBundleString("PROP_LAYOUT_CODE_TARGET"), // NOI18N
                FormUtils.getBundleString("HINT_LAYOUT_CODE_TARGET")); // NOI18N
        }
            
        public void setValue(Object value) {
            if (!(value instanceof Integer))
                throw new IllegalArgumentException();
            
            Integer oldValue = (Integer)getValue();
            Integer newValue = (Integer)value;
            formModel.getSettings().setLayoutCodeTarget(newValue.intValue());
            FormEditor.updateProjectForNaturalLayout(formModel);
            formModel.fireSyntheticPropertyChanged(null, FormLoaderSettings.PROP_LAYOUT_CODE_TARGET, oldValue, newValue);
            FormEditor.getFormEditor(formModel).getFormRootNode().firePropertyChangeHelper(
                FormLoaderSettings.PROP_LAYOUT_CODE_TARGET, oldValue, newValue);
        }

        public Object getValue() {
            return new Integer(formModel.getSettings().getLayoutCodeTarget());
        }

        public boolean supportsDefaultValue() {
            return true;
        }

        public void restoreDefaultValue() {
            setValue(new Integer(FormLoaderSettings.getInstance().getLayoutCodeTarget()));
        }

        public boolean isDefaultValue() {
            return (formModel.getSettings().getLayoutCodeTarget() ==
                    FormLoaderSettings.getInstance().getLayoutCodeTarget());
        }

        public boolean canWrite() {
            return JavaCodeGenerator.this.canGenerate && !JavaCodeGenerator.this.formModel.isReadOnly();
        }

        public PropertyEditor getPropertyEditor() {
            return new FormLoaderSettingsBeanInfo.LayoutCodeTargetEditor(true);
        }

    }
}
