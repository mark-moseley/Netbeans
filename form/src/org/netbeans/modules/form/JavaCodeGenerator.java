/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.form;

import org.openide.explorer.propertysheet.editors.ModifierEditor;
import org.openide.filesystems.*;
import org.openide.nodes.*;
import org.openide.text.IndentEngine;
import org.openide.util.Utilities;
import org.openide.util.SharedClassObject;
import org.openide.loaders.MultiDataObject.Entry;

import org.netbeans.modules.java.JavaEditor;

import org.netbeans.modules.form.editors.CustomCodeEditor;
import org.netbeans.modules.form.codestructure.*;
import org.netbeans.modules.form.layoutsupport.LayoutSupportManager;

import java.awt.Dimension;
import java.awt.Point;
import java.beans.*;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * JavaCodeGenerator is the default code generator which produces a Java source
 * for the form.
 *
 * @author Ian Formanek
 */

class JavaCodeGenerator extends CodeGenerator {
    private static Object GEN_LOCK = new Object();

    protected static final String AUX_VARIABLE_MODIFIER =
        "JavaCodeGenerator_VariableModifier"; // NOI18N
    protected static final String AUX_VARIABLE_LOCAL =
        "JavaCodeGenerator_VariableLocal"; // NOI18N
    protected static final String AUX_SERIALIZE_TO =
        "JavaCodeGenerator_SerializeTo"; // NOI18N
    protected static final String AUX_CODE_GENERATION =
        "JavaCodeGenerator_CodeGeneration"; // NOI18N
    protected static final String AUX_CREATE_CODE_PRE =
        "JavaCodeGenerator_CreateCodePre"; // NOI18N
    protected static final String AUX_CREATE_CODE_POST =
        "JavaCodeGenerator_CreateCodePost"; // NOI18N
    protected static final String AUX_CREATE_CODE_CUSTOM =
        "JavaCodeGenerator_CreateCodeCustom"; // NOI18N
    protected static final String AUX_INIT_CODE_PRE =
        "JavaCodeGenerator_InitCodePre"; // NOI18N
    protected static final String AUX_INIT_CODE_POST =
        "JavaCodeGenerator_InitCodePost"; // NOI18N

    protected static final String SECTION_INIT_COMPONENTS =
        "initComponents"; // NOI18N
    protected static final String SECTION_VARIABLES =
        "variables"; // NOI18N
    protected static final String SECTION_EVENT_PREFIX =
        "event_"; // NOI18N

    public static final Integer VALUE_GENERATE_CODE = new Integer(0);
    public static final Integer VALUE_SERIALIZE = new Integer(1);

    private static final String INIT_COMPONENTS_HEADER =
        "private void initComponents() {\n"; // NOI18N
    private static final String INIT_COMPONENTS_FOOTER = "}\n"; // NOI18N
    private static final String VARIABLES_HEADER =
        FormEditor.getFormBundle().getString("MSG_VariablesBegin"); // NOI18N
    private static final String VARIABLES_FOOTER =
        FormEditor.getFormBundle().getString("MSG_VariablesEnd"); // NOI18N

    /** The prefix for event handler sections */
    private static final String EVT_SECTION_PREFIX = "event_"; // NOI18N

    private static final String oneIndent = "  "; // [PENDING - indentation engine] // NOI18N

    /** The FormLoaderSettings instance */
    private static FormLoaderSettings formSettings = (FormLoaderSettings)
                   SharedClassObject.findObject(FormLoaderSettings.class, true);

    private FormModel formModel;
    private FormEditorSupport formEditorSupport;

    private boolean initialized = false;
    private boolean canGenerate = true;
    private boolean codeUpToDate = true;

    private JavaEditor.SimpleSection initComponentsSection;
    private JavaEditor.SimpleSection variablesSection;

    private Map constructorProperties;
    private Map containerDependentProperties;

    /** Creates new JavaCodeGenerator */

    public JavaCodeGenerator() {
    }

    public void initialize(FormModel formModel) {
        if (!initialized) {
            this.formModel = formModel;
            formEditorSupport = FormEditorSupport.getSupport(formModel);

            if (!formModel.isReadOnly()) {
                canGenerate = true;
                formModel.addFormModelListener(new JCGFormListener());
            }
            else canGenerate = false;

            initComponentsSection =
                formEditorSupport.findSimpleSection(SECTION_INIT_COMPONENTS);
            variablesSection =
                formEditorSupport.findSimpleSection(SECTION_VARIABLES);

            if (initComponentsSection == null || variablesSection == null) {
                System.out.println("ERROR: Cannot initialize guarded sections... code generation is disabled."); // NOI18N
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
        Node.Property[] props = new Node.Property[] {
            new PropertySupport.ReadWrite(
                "variableName", // NOI18N
                String.class,
                FormEditor.getFormBundle().getString("MSG_JC_VariableName"), // NOI18N
                FormEditor.getFormBundle().getString("MSG_JC_VariableDesc")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof String)) {
                        throw new IllegalArgumentException();
                    }
                    component.setName((String)value);
                    component.getNodeReference().firePropertyChangeHelper(
                        "variableName", null, null); // NOI18N
                }

                public Object getValue() {
                    return component.getName();
                }

                public boolean canWrite() {
                    return JavaCodeGenerator.this.canGenerate;
                }
            },

            new PropertySupport.ReadWrite(
                "modifiers", // NOI18N
                Integer.class,
                FormEditor.getFormBundle().getString("MSG_JC_VariableModifiers"), // NOI18N
                FormEditor.getFormBundle().getString("MSG_JC_VariableModifiersDesc")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof Integer))
                        throw new IllegalArgumentException();

                    CodeStructure codeStructure = formModel.getCodeStructure();
                    CodeExpression exp = component.getCodeExpression();
                    int varType = exp.getVariable().getType();
                    String varName = component.getName();

                    varType &= ~CodeVariable.ALL_MODIF_MASK;
                    varType |= ((Integer)value).intValue() & CodeVariable.ALL_MODIF_MASK;
                    if ((varType & (CodeVariable.LOCAL | CodeVariable.FINAL))
                            == CodeVariable.LOCAL)
                        varType |= CodeVariable.EXPLICIT_DECLARATION;

                    if ((varType & CodeVariable.ALL_MODIF_MASK)
                            != (formSettings.getVariablesModifier()
                                & CodeVariable.ALL_MODIF_MASK))
                    {   // non-default value
                        component.setAuxValue(AUX_VARIABLE_MODIFIER, value);
                    }
                    else { // default value
                        varType = 0x30DF; // default
                        component.getAuxValues().remove(AUX_VARIABLE_MODIFIER);
                    }

                    codeStructure.removeExpressionFromVariable(exp);
                    codeStructure.createVariableForExpression(
                                         exp, varType, varName);

                    regenerateVariables();
                    regenerateInitializer();
                    component.getNodeReference().firePropertyChangeHelper(
                        "modifiers", null, null); // NOI18N
                }

                public Object getValue() {
                    Object val = component.getAuxValue(AUX_VARIABLE_MODIFIER);
                    if (val != null)
                        return val;

                    return new Integer(formSettings.getVariablesModifier());
                }

                public boolean supportsDefaultValue() {
                    return true;
                }

                public void restoreDefaultValue() {
                    if (component.getAuxValue(AUX_VARIABLE_LOCAL) == null)
                        setValue(new Integer(formSettings.getVariablesModifier()));
                }

                public boolean canWrite() {
                    return JavaCodeGenerator.this.canGenerate;
                }

                public PropertyEditor getPropertyEditor() {
                    Boolean local = (Boolean) component.getAuxValue(AUX_VARIABLE_LOCAL);
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
            },

            new PropertySupport.ReadWrite(
                "useLocalVariable", // NOI18N
                Boolean.TYPE,
                FormEditor.getFormBundle().getString("MSG_JC_UseLocalVar"), // NOI18N
                FormEditor.getFormBundle().getString("MSG_JC_UseLocalVarDesc")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof Boolean))
                        throw new IllegalArgumentException();

                    boolean useLocalVariable = ((Boolean)value).booleanValue();

                    CodeStructure codeStructure = formModel.getCodeStructure();
                    CodeExpression exp = component.getCodeExpression();
                    int varType = exp.getVariable().getType();
                    String varName = component.getName();

                    varType &= /*CodeVariable.FINAL
                               |*/ ~(CodeVariable.ALL_MODIF_MASK
                                     | CodeVariable.SCOPE_MASK);
                    if (Boolean.TRUE.equals(value))
                        varType |= CodeVariable.LOCAL
                                   | CodeVariable.EXPLICIT_DECLARATION;
                    else
                        varType |= CodeVariable.FIELD
                                   | formSettings.getVariablesModifier();

                    if (((varType & CodeVariable.LOCAL) != 0)
                            != (formSettings.getVariablesLocal()))
                    {   // non-default value
                        component.setAuxValue(AUX_VARIABLE_LOCAL, value);
                        component.setAuxValue(
                            AUX_VARIABLE_MODIFIER,
                            new Integer(varType & CodeVariable.ALL_MODIF_MASK));
                    }
                    else { // default value
                        varType = 0x30DF; // default
                        component.getAuxValues().remove(AUX_VARIABLE_LOCAL);
                        component.getAuxValues().remove(AUX_VARIABLE_MODIFIER);
                    }

                    codeStructure.removeExpressionFromVariable(exp);
                    codeStructure.createVariableForExpression(
                                         exp, varType, varName);

                    regenerateVariables();
                    regenerateInitializer();
                    component.getNodeReference().fireComponentPropertySetsChange();
                }

                public Object getValue() {
                    Object val = component.getAuxValue(AUX_VARIABLE_LOCAL);
                    if (val != null)
                        return val;

                    return new Boolean(formSettings.getVariablesLocal());
                }

                public boolean supportsDefaultValue() {
                    return true;
                }

                public void restoreDefaultValue() {
                    setValue(new Boolean(formSettings.getVariablesLocal()));
                }
                    
                public boolean canWrite() {
                    return JavaCodeGenerator.this.canGenerate;
                }
            },

            new PropertySupport.ReadWrite(
                "codeGeneration", // NOI18N
                Integer.TYPE,
                FormEditor.getFormBundle().getString("MSG_JC_CodeGeneration"), // NOI18N
                FormEditor.getFormBundle().getString("MSG_JC_CodeGenerationDesc")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof Integer)) {
                        throw new IllegalArgumentException();
                    }
                    component.setAuxValue(AUX_CODE_GENERATION, value);
                    if (value.equals(VALUE_SERIALIZE)) {
                        if (component.getAuxValue(AUX_SERIALIZE_TO) == null) {
                            component.setAuxValue(AUX_SERIALIZE_TO,
                                                  getDefaultSerializedName(component));
                        }
                    }
                    regenerateInitializer();
                    component.getNodeReference().firePropertyChangeHelper(
                        "codeGeneration", null, null); // NOI18N
                }

                public Object getValue() {
                    Object value = component.getAuxValue(AUX_CODE_GENERATION);
                    if (value == null) {
                        if (component.hasHiddenState()) {
                            value = VALUE_SERIALIZE;
                        } else {
                            value = VALUE_GENERATE_CODE;
                        }
                    }
                    return value;
                }

                public boolean canWrite() {
                    return JavaCodeGenerator.this.canGenerate;
                }

                public PropertyEditor getPropertyEditor() {
                    return new CodeGenerateEditor(component);
                }
            },
            new CodePropertySupportRW(
                "creationCodePre", // NOI18N
                String.class,
                FormEditor.getFormBundle().getString("MSG_JC_PreCreationCode"), // NOI18N
                FormEditor.getFormBundle().getString("MSG_JC_PreCreationCodeDesc")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof String)) {
                        throw new IllegalArgumentException();
                    }
                    component.setAuxValue(AUX_CREATE_CODE_PRE, value);
                    regenerateInitializer();
                    component.getNodeReference().firePropertyChangeHelper(
                        "creationCodePre", null, null); // NOI18N
                }

                public Object getValue() {
                    Object value = component.getAuxValue(AUX_CREATE_CODE_PRE);
                    if (value == null) {
                        value = ""; // NOI18N
                    }
                    return value;
                }
            },
            new CodePropertySupportRW(
                "creationCodePost", // NOI18N
                String.class,
                FormEditor.getFormBundle().getString("MSG_JC_PostCreationCode"), // NOI18N
                FormEditor.getFormBundle().getString("MSG_JC_PostCreationCodeDesc")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof String)) {
                        throw new IllegalArgumentException();
                    }
                    component.setAuxValue(AUX_CREATE_CODE_POST, value);
                    regenerateInitializer();
                    component.getNodeReference().firePropertyChangeHelper(
                        "creationCodePost", null, null); // NOI18N
                }

                public Object getValue() {
                    Object value = component.getAuxValue(AUX_CREATE_CODE_POST);
                    if (value == null) {
                        value = ""; // NOI18N
                    }
                    return value;
                }
            },
            new CodePropertySupportRW(
                "initCodePre", // NOI18N
                String.class,
                FormEditor.getFormBundle().getString("MSG_JC_PreInitCode"), // NOI18N
                FormEditor.getFormBundle().getString("MSG_JC_PreInitCodeDesc")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof String)) {
                        throw new IllegalArgumentException();
                    }
                    component.setAuxValue(AUX_INIT_CODE_PRE, value);
                    regenerateInitializer();
                    component.getNodeReference().firePropertyChangeHelper(
                        "initCodePre", null, null); // NOI18N
                }

                public Object getValue() {
                    Object value = component.getAuxValue(AUX_INIT_CODE_PRE);
                    if (value == null) {
                        value = ""; // NOI18N
                    }
                    return value;
                }
            },
            new CodePropertySupportRW(
                "initCodePost", // NOI18N
                String.class,
                FormEditor.getFormBundle().getString("MSG_JC_PostInitCode"), // NOI18N
                FormEditor.getFormBundle().getString("MSG_JC_PostInitCodeDesc")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof String)) {
                        throw new IllegalArgumentException();
                    }
                    component.setAuxValue(AUX_INIT_CODE_POST, value);
                    regenerateInitializer();
                    component.getNodeReference().firePropertyChangeHelper(
                        "initCodePost", null, null); // NOI18N
                }

                public Object getValue() {
                    Object value = component.getAuxValue(AUX_INIT_CODE_POST);
                    if (value == null) {
                        value = ""; // NOI18N
                    }
                    return value;
                }
            },
            new PropertySupport.ReadWrite(
                "serializeTo", // NOI18N
                String.class,
                FormEditor.getFormBundle().getString("MSG_JC_SerializeTo"), // NOI18N
                FormEditor.getFormBundle().getString("MSG_JC_SerializeToDesc")) // NOI18N
            {
                public void setValue(Object value) {
                    if (!(value instanceof String)) {
                        throw new IllegalArgumentException();
                    }
                    component.setAuxValue(AUX_SERIALIZE_TO, value);
                    regenerateInitializer();
                    component.getNodeReference().firePropertyChangeHelper(
                        "serializeTo", null, null); // NOI18N
                }

                public Object getValue() {
                    Object value = component.getAuxValue(AUX_SERIALIZE_TO);
                    if (value == null) {
                        value = getDefaultSerializedName(component);
                    }
                    return value;
                }

                public boolean canWrite() {
                    return JavaCodeGenerator.this.canGenerate;
                }
            }
        };

        Integer generationType =(Integer) component.getAuxValue(AUX_CODE_GENERATION);
        if ((generationType == null) ||(generationType.equals(VALUE_GENERATE_CODE))) {
            Node.Property[] moreProps = new Node.Property[props.length + 1];
            for (int i=0, n=props.length; i<n; i++) {
                moreProps [i] = props [i];
            }
            moreProps [moreProps.length -1] =
                new CodePropertySupportRW(
                    "creationCodeCustom", // NOI18N
                    String.class,
                    FormEditor.getFormBundle().getString("MSG_JC_CustomCreationCode"), // NOI18N
                    FormEditor.getFormBundle().getString("MSG_JC_CustomCreationCodeDesc")) // NOI18N
                {
                    public void setValue(Object value) {
                        if (!(value instanceof String)) {
                            throw new IllegalArgumentException();
                        }
                        component.setAuxValue(AUX_CREATE_CODE_CUSTOM, value);
                        regenerateInitializer();
                        component.getNodeReference().firePropertyChangeHelper(
                            "creationCodeCustom", null, null); // NOI18N
                    }

                    public Object getValue() {
                        Object value = component.getAuxValue(AUX_CREATE_CODE_CUSTOM);
                        if (value == null) {
                            value = ""; // NOI18N
                        }
                        return value;
                    }

                    public boolean canWrite() {
                        if (!JavaCodeGenerator.this.canGenerate)
                            return false;
                        Integer genType =(Integer)component.getAuxValue(AUX_CODE_GENERATION);
                        return((genType == null) ||(genType.equals(VALUE_GENERATE_CODE)));
                    }
                };
            return moreProps;
        } else {
            return props;
        }
    }

    //
    // Private Methods
    //

    private String getDefaultSerializedName(RADComponent component) {
        return component.getFormModel().getName()
            + "_" + component.getName(); // NOI18N
    }

    private void regenerateInitializer() {
        if (!initialized || !canGenerate)
            return;

        IndentEngine indentEngine = IndentEngine.find(
                                        formEditorSupport.getDocument());

        StringWriter initCodeBuffer = new StringWriter(1024);
        Writer initCodeWriter;
        if (formSettings.getUseIndentEngine())
            initCodeWriter = indentEngine.createWriter(
                               formEditorSupport.getDocument(),
                               initComponentsSection.getBegin().getOffset(),
                               initCodeBuffer);
        else
            initCodeWriter = initCodeBuffer;

        if (constructorProperties != null)
            constructorProperties.clear();
        if (containerDependentProperties != null)
            containerDependentProperties.clear();

        try {
            initCodeWriter.write(INIT_COMPONENTS_HEADER);

            if (addLocalVariables(initCodeWriter))
                initCodeWriter.write("\n"); // NOI18N

            RADComponent[] nonVisualComponents = formModel.getNonVisualComponents();
            for (int i = 0; i < nonVisualComponents.length; i++) {
                addCreateCode(nonVisualComponents[i], initCodeWriter);
            }
            RADComponent top = formModel.getTopRADComponent();
            addCreateCode(top, initCodeWriter);
            initCodeWriter.write("\n"); // NOI18N

            for (int i = 0; i < nonVisualComponents.length; i++) {
                addInitCode(nonVisualComponents[i], initCodeWriter, 0);
            }
            if (nonVisualComponents.length > 0)
                initCodeWriter.write("\n"); // NOI18N
            addInitCode(top, initCodeWriter, 0);

            // for visual forms append sizing text
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

                switch (formPolicy) {
                    case RADVisualFormContainer.GEN_PACK:
                        sizeText = "pack();\n"; // NOI18N
                        break;
                    case RADVisualFormContainer.GEN_BOUNDS:
                        if (genCenter) {
                            StringBuffer sizeBuffer = new StringBuffer();
                            if (genSize) {
                                sizeBuffer.append("pack();\n"); // NOI18N
                                sizeBuffer.append("java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();\n"); // NOI18N
                                sizeBuffer.append("setSize(new java.awt.Dimension("+formSize.width + ", " + formSize.height + "));\n"); // NOI18N
                                sizeBuffer.append("setLocation((screenSize.width-"+formSize.width+")/2,(screenSize.height-"+formSize.height+")/2);\n"); // NOI18N
                            } else {
                                sizeBuffer.append("pack();\n"); // NOI18N
                                sizeBuffer.append("java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();\n"); // NOI18N
                                sizeBuffer.append("java.awt.Dimension dialogSize = getSize();\n"); // NOI18N
                                sizeBuffer.append("setLocation((screenSize.width-dialogSize.width)/2,(screenSize.height-dialogSize.height)/2);\n"); // NOI18N
                            }

                            sizeText = sizeBuffer.toString();

                        } else if (genPosition && genSize) { // both size and position
                            sizeText = "setBounds("+formPosition.x + ", " + formPosition.y +", " + formSize.width + ", " + formSize.height + ");\n"; // NOI18N
                        } else if (genPosition) { // position only
                            sizeText = "setLocation(new java.awt.Point("+formPosition.x + ", " + formPosition.y + "));\n"; // NOI18N
                        } else if (genSize) { // size only
                            sizeText = "setSize(new java.awt.Dimension("+formSize.width + ", " + formSize.height + "));\n"; // NOI18N
                        }
                        break;
                }

                initCodeWriter.write(sizeText);
            }

            if (constructorProperties != null)
                constructorProperties.clear();
            if (containerDependentProperties != null)
                containerDependentProperties.clear();

            initCodeWriter.write(INIT_COMPONENTS_FOOTER);
            initCodeWriter.close();

            // set the text into the guarded block
            synchronized(GEN_LOCK) {
                String newText = initCodeBuffer.toString();
                if (!formSettings.getUseIndentEngine())
                    newText = indentCode(newText, indentEngine);

                initComponentsSection.setText(newText);
                clearUndo();
            }
        }
        catch (IOException e) { // should not happen
            e.printStackTrace();
        }
    }

    private void regenerateVariables() {
        if (!initialized || !canGenerate)
            return;
        
        IndentEngine indentEngine = IndentEngine.find(
                                        formEditorSupport.getDocument());

        StringWriter variablesBuffer = new StringWriter(1024);
        Writer variablesWriter;
        if (formSettings.getUseIndentEngine())
            variablesWriter = indentEngine.createWriter(
                               formEditorSupport.getDocument(),
                               variablesSection.getBegin().getOffset(),
                               variablesBuffer);
        else
            variablesWriter = variablesBuffer;

        try {
            variablesWriter.write(VARIABLES_HEADER);
            variablesWriter.write("\n"); // NOI18N

            addVariables(variablesWriter);

            variablesWriter.write(VARIABLES_FOOTER);
            variablesWriter.write("\n"); // NOI18N
            variablesWriter.close();

            synchronized(GEN_LOCK) {
                String newText = variablesBuffer.toString();
                if (!formSettings.getUseIndentEngine())
                    newText = indentCode(newText, indentEngine);

                variablesSection.setText(newText);
                clearUndo();
            }
        }
        catch (IOException e) { // should not happen
            e.printStackTrace();
        }
    }

    private void regenerateEventHandlers() {
        // only missing handler methods are generated, existing are left intact
        Object[] handlers = formModel.getFormEventHandlers().getAllHandlers();
        for (int i=0; i < handlers.length; i++) {
            EventHandler eh = (EventHandler)handlers[i];
            eh.generateHandler(null);
        }
    }

    private void addCreateCode(RADComponent comp, Writer initCodeWriter)
        throws IOException
    {
        if (comp == null)
            return;

        if (comp != formModel.getTopRADComponent()) {
            generateComponentCreate(comp, initCodeWriter, true);
        }
        if (comp instanceof ComponentContainer) {
            RADComponent[] children =((ComponentContainer)comp).getSubBeans();
            for (int i = 0; i < children.length; i++) {
                addCreateCode(children[i], initCodeWriter);
            }
        }
    }

    private void addInitCode(RADComponent comp,
                             Writer initCodeWriter,
                             int level)
        throws IOException
    {
        if (comp == null)
            return;

        generateComponentInit(comp, initCodeWriter);
        generateComponentEvents(comp, initCodeWriter);

        if (comp instanceof ComponentContainer) {
            RADComponent[] children =((ComponentContainer)comp).getSubBeans();
            for (int i = 0; i < children.length; i++) {
                RADComponent subcomp = children[i];
                addInitCode(subcomp, initCodeWriter, level);

                if (comp instanceof RADVisualContainer) {
                    // visual container
                    generateComponentAddCode(subcomp,
                                             (RADVisualContainer)comp,
                                             initCodeWriter);
                }
                else if (comp instanceof RADMenuComponent) {
                    // menu
                    generateMenuAddCode(subcomp,
                                        (RADMenuComponent) comp,
                                        initCodeWriter);
                } // [PENDING - adding to non-visual containers]

                initCodeWriter.write("\n"); // NOI18N
            }

            // hack for properties that can't be set until all children 
            // are added to the container
            List postProps;
            if (containerDependentProperties != null
                && (postProps = (List)containerDependentProperties.get(comp))
                    != null)
            {
                for (Iterator it = postProps.iterator(); it.hasNext(); ) {
                    RADProperty prop = (RADProperty) it.next();
                    generatePropertySetter(comp, prop, initCodeWriter);
                }
                initCodeWriter.write("\n"); // NOI18N
            }
//            if (comp instanceof RADVisualContainer)
//                generateVisualCode((RADVisualContainer)comp, initCodeWriter);
        }
    }

    private void generateComponentCreate(RADComponent comp,
                                         Writer initCodeWriter,
                                         boolean insideMethod)
        throws IOException
    {
        if (comp instanceof RADMenuItemComponent
            && ((RADMenuItemComponent)comp).getMenuItemType()
                   == RADMenuItemComponent.T_SEPARATOR)
        { // do not generate anything for AWT separator as it is not a real component
            return;
        }

        CodeVariable var = comp.getCodeExpression().getVariable();
        int varType = var.getType();

        if (insideMethod) {
            int finalField = CodeVariable.FIELD | CodeVariable.FINAL;
            if ((varType & finalField) == finalField)
                return;

            String preCode = (String) comp.getAuxValue(AUX_CREATE_CODE_PRE);
            if (preCode != null && !preCode.equals("")) { // NOI18N
                initCodeWriter.write(preCode);
                initCodeWriter.write("\n"); // NOI18N
            }
        }

        Integer generationType = (Integer)comp.getAuxValue(AUX_CODE_GENERATION);
        if (comp.hasHiddenState()
            || (generationType != null
                && generationType.equals(VALUE_SERIALIZE)))
        {   // generate code for restoring serialized component
            if (!insideMethod)
                return;

            String serializeTo = (String)comp.getAuxValue(AUX_SERIALIZE_TO);
            if (serializeTo == null) {
                serializeTo = getDefaultSerializedName(comp);
                comp.setAuxValue(AUX_SERIALIZE_TO, serializeTo);
            }
            initCodeWriter.write("try {\n"); // NOI18N
            initCodeWriter.write(comp.getName());
            initCodeWriter.write(" =("); // NOI18N
            initCodeWriter.write(comp.getBeanClass().getName());
            initCodeWriter.write(")java.beans.Beans.instantiate(getClass().getClassLoader(), \""); // NOI18N
            // write package name
            // !! [this won't work when filesystem root != classpath root]
            String packageName = formEditorSupport.getFormDataObject()
                            .getPrimaryFile().getParent().getPackageName('.');
            if (!"".equals(packageName)) { // NOI18N
                initCodeWriter.write(packageName + "."); // NOI18N
            }
            initCodeWriter.write(serializeTo);
            initCodeWriter.write("\");\n"); // NOI18N
            initCodeWriter.write("} catch (ClassNotFoundException e) {\n"); // NOI18N
            initCodeWriter.write("e.printStackTrace();\n"); // NOI18N
            initCodeWriter.write("} catch (java.io.IOException e) {\n"); // NOI18N
            initCodeWriter.write("e.printStackTrace();\n"); // NOI18N
            initCodeWriter.write("}\n"); // NOI18N
        }
        else { // generate component creation code
            StringBuffer varBuf = new StringBuffer();

            int declareMask = CodeVariable.SCOPE_MASK
                              | CodeVariable.DECLARATION_MASK;

            if ((varType & CodeVariable.FINAL) == CodeVariable.FINAL
                || (varType & declareMask) == CodeVariable.LOCAL)
            {   // generate a variable declaration together with the assignment
                varBuf.append(Modifier.toString(
                                varType & CodeVariable.ALL_MODIF_MASK));
                varBuf.append(" "); // NOI18N
                varBuf.append(comp.getBeanClass().getName());
                varBuf.append(" "); // NOI18N
            }

            varBuf.append(var.getName());

            String customCreateCode = (String) comp.getAuxValue(AUX_CREATE_CODE_CUSTOM);
            if (customCreateCode != null && !"".equals(customCreateCode)) { // NOI18N
                initCodeWriter.write(varBuf.toString());
                initCodeWriter.write(" = "); // NOI18N
                initCodeWriter.write(customCreateCode);
                initCodeWriter.write("\n"); // NOI18N
            }
            else {
                CreationDescriptor desc = CreationFactory.getDescriptor(
                                                              comp.getBeanClass());
                if (desc == null)
                    desc = new ConstructorsDescriptor(comp.getBeanClass());

                CreationDescriptor.Creator creator =
                    desc.findBestCreator(comp.getAllBeanProperties(),
                                         CreationDescriptor.CHANGED_ONLY);
                
                Class[] exceptions = creator.getExceptionTypes();
                if (insideMethod && needTryCode(exceptions)) {
                    if ((varType & declareMask) == CodeVariable.LOCAL) {
                        initCodeWriter.write(varBuf.toString());
                        initCodeWriter.write(";\n"); // NOI18N
                    }
                    initCodeWriter.write("try {\n"); // NOI18N
                    initCodeWriter.write(var.getName());
                }
                else {
                    initCodeWriter.write(varBuf.toString());
                    exceptions = null;
                }

                initCodeWriter.write(" = "); // NOI18N

                String[] propNames = creator.getPropertyNames();
                FormProperty[] props;
                if (propNames.length > 0) {
                    if (constructorProperties == null)
                        constructorProperties = new HashMap();

                    props = new FormProperty[propNames.length];

                    for (int i=0; i < propNames.length; i++) {
                        FormProperty prop = comp.getPropertyByName(propNames[i]);
                        props[i] = prop;
                        constructorProperties.put(prop, prop);
                    }
                }
                else props = RADComponent.NO_PROPERTIES;

                initCodeWriter.write(creator.getJavaCreationCode(props));
                initCodeWriter.write(";\n"); // NOI18N

                if (exceptions != null)
                    generateCatchCode(exceptions, initCodeWriter);
            }
        }

        if (insideMethod) {
            String postCode = (String) comp.getAuxValue(AUX_CREATE_CODE_POST);
            if (postCode != null && !postCode.equals("")) { // NOI18N
                initCodeWriter.write(postCode);
                initCodeWriter.write("\n"); // NOI18N
            }
        }
    }

    private void generateComponentInit(RADComponent comp,
                                       Writer initCodeWriter)
        throws IOException
    {
        if (comp instanceof RADVisualContainer) {
            LayoutSupportManager layoutSupport =
                ((RADVisualContainer)comp).getLayoutSupport();

            if (layoutSupport.isLayoutChanged()) {
                Iterator it = layoutSupport.getLayoutCode()
                                                .getStatementsIterator();
                while (it.hasNext()) {
                    CodeStatement statement = (CodeStatement) it.next();
                    initCodeWriter.write(getStatementJavaString(statement, "")); // NOI18N
                    initCodeWriter.write("\n"); // NOI18N
                }
                initCodeWriter.write("\n"); // NOI18N
            }
        }

        Object genType = comp.getAuxValue(AUX_CODE_GENERATION);
        String preCode = (String) comp.getAuxValue(AUX_INIT_CODE_PRE);
        String postCode = (String) comp.getAuxValue(AUX_INIT_CODE_POST);
        if (preCode != null && !preCode.equals("")) { // NOI18N
            initCodeWriter.write(preCode);
            initCodeWriter.write("\n"); // NOI18N
        }

        if (!comp.hasHiddenState() 
                && (genType == null || VALUE_GENERATE_CODE.equals(genType)))
        {   // not serialized
            RADProperty[] props = comp.getAllBeanProperties();
            for (int i = 0; i < props.length; i++) {
                RADProperty prop = props[i];
                if ((prop.isChanged()
                     && (constructorProperties == null
                         || constructorProperties.get(prop) == null))
                    || prop.getPreCode() != null
                    || prop.getPostCode() != null)
                {
                    if (!FormUtils.isContainerContentDependentProperty(
                                    comp.getBeanClass(), prop.getName()))
                        generatePropertySetter(comp, prop, initCodeWriter);
                    else {
                        // hack for properties that can't be set until all
                        // children are added to the container
                        List propList;
                        if (containerDependentProperties != null) {
                            propList = (List) containerDependentProperties.get(comp);
                        }
                        else {
                            containerDependentProperties = new HashMap();
                            propList = null;
                        }
                        if (propList == null) {
                            propList = new LinkedList();
                            containerDependentProperties.put(comp, propList);
                        }

                        propList.add(prop);
                    }
                }
            }
        }
        if ((postCode != null) &&(!postCode.equals(""))) { // NOI18N
            initCodeWriter.write(postCode);
            initCodeWriter.write("\n"); // NOI18N
        }
    }

    // This method generates all layout code in one block. Currently not used.
    private void generateVisualCode(RADVisualContainer container,
                                    Writer initCodeWriter)
        throws IOException
    {
        LayoutSupportManager layoutSupport = container.getLayoutSupport();

        if (layoutSupport.isLayoutChanged()) {
            Iterator it = layoutSupport.getLayoutCode()
                                            .getStatementsIterator();
            while (it.hasNext()) {
                CodeStatement statement = (CodeStatement) it.next();
                initCodeWriter.write(getStatementJavaString(statement, "")); // NOI18N
                initCodeWriter.write("\n"); // NOI18N
            }
        }

        for (int i=0, n=layoutSupport.getComponentCount(); i < n; i++) {
            Iterator it = layoutSupport.getComponentCode(i)
                                            .getStatementsIterator();
            while (it.hasNext()) {
                CodeStatement statement = (CodeStatement) it.next();
                initCodeWriter.write(getStatementJavaString(statement, "")); // NOI18N
                initCodeWriter.write("\n"); // NOI18N
            }
        }

        initCodeWriter.write("\n"); // NOI18N

        // hack for properties that can't be set until all children 
        // are added to the container
        List postProps;
        if (containerDependentProperties != null
            && (postProps = (List)containerDependentProperties.get(container))
                != null)
        {
            for (Iterator it = postProps.iterator(); it.hasNext(); ) {
                RADProperty prop = (RADProperty) it.next();
                generatePropertySetter(container, prop, initCodeWriter);
            }
            initCodeWriter.write("\n"); // NOI18N
        }
    }

    private void generateComponentAddCode(RADComponent comp,
                                          RADVisualContainer container,
                                          Writer initCodeWriter)
        throws IOException
    {
        if (comp instanceof RADVisualComponent) {
            CodeGroup componentCode = container.getLayoutSupport()
                                 .getComponentCode((RADVisualComponent)comp);
            if (componentCode != null) {
                Iterator it = componentCode.getStatementsIterator();
                while (it.hasNext()) {
                    CodeStatement statement = (CodeStatement) it.next();
                    initCodeWriter.write(getStatementJavaString(statement, "")); // NOI18N
                    initCodeWriter.write("\n"); // NOI18N
                }
            }
        }
        else if (comp instanceof RADMenuComponent) {
            String menuText;
            RADMenuComponent menuComp = (RADMenuComponent) comp;
            Class contClass = container.getBeanClass();

            if (menuComp.getMenuItemType() == RADMenuItemComponent.T_JMENUBAR
                    && javax.swing.RootPaneContainer.class.isAssignableFrom(contClass))
                menuText = "setJMenuBar"; // NOI18N
            else if (menuComp.getMenuItemType() == RADMenuItemComponent.T_MENUBAR
                     && java.awt.Frame.class.isAssignableFrom(contClass))
                menuText = "setMenuBar"; // NOI18N
            else
                menuText = null;

            if (menuText != null) {
                initCodeWriter.write(getVariableGenString(container));
                initCodeWriter.write(menuText);
                initCodeWriter.write("("); // NOI18N
                initCodeWriter.write(menuComp.getName());
                initCodeWriter.write(");\n"); // NOI18N
            }
        }
    }

    private void generateMenuAddCode(RADComponent comp, RADMenuComponent container, Writer initCodeWriter) throws IOException {
        if ((comp instanceof RADMenuItemComponent) &&(((RADMenuItemComponent)comp).getMenuItemType() == RADMenuItemComponent.T_SEPARATOR)) {
            // treat AWT Separator specially - it is not a component
            initCodeWriter.write(container.getName());
            initCodeWriter.write(".addSeparator();"); // NOI18N
        } else {
            initCodeWriter.write(container.getName());
            initCodeWriter.write(".add("); // NOI18N
            initCodeWriter.write(comp.getName());
            initCodeWriter.write(");"); // NOI18N
        }
    }

    /*  private void generateIndexedPropertySetter(RADComponent comp, PropertyDescriptor desc, StringBuffer text, String indent) {
        System.out.println("generateIndexedPropertySetter: NotImplemented...(Property: "+desc.getName()+", Value: "+value+")"); // [PENDING]
        }
    */

    // why is this method synchronized??
    private synchronized void generatePropertySetter(RADComponent comp,
                                                     RADProperty prop,
                                                     Writer initCodeWriter)
    throws IOException {
        // 1. pre-initialization code
        String preCode = prop.getPreCode();
        if (preCode != null) {
            initCodeWriter.write(preCode);
            if (!preCode.endsWith("\n")) initCodeWriter.write("\n"); // NOI18N
        }

        // 2. property setter code
        if (prop.isChanged()) {
            String javaStr;
            Method writeMethod;

            if ((javaStr = prop.getWholeSetterCode()) != null) {
                initCodeWriter.write(javaStr);
                if (!javaStr.endsWith("\n")) // NOI18N
                    initCodeWriter.write("\n"); // NOI18N
            }
            else if ((javaStr = prop.getJavaInitializationString()) != null
                      && (writeMethod = prop.getPropertyDescriptor().getWriteMethod()) != null) {
               // if the setter throws checked exceptions,
               // we must generate try/catch block around it.
                Class[] exceptions = writeMethod.getExceptionTypes();
                if (needTryCode(exceptions)) {
                    initCodeWriter.write("try {\n"); // NOI18N
                } else {
                    exceptions = null;
                }

                initCodeWriter.write(getVariableGenString(comp));
                initCodeWriter.write(writeMethod.getName());
                initCodeWriter.write("("); // NOI18N
                initCodeWriter.write(javaStr);
                initCodeWriter.write(");\n"); // NOI18N

                // add the catch code if needed
                if (exceptions != null)
                    generateCatchCode(exceptions, initCodeWriter);
            }
        }

        // 3. post-initialization code
        String postCode = prop.getPostCode();
        if (postCode != null) {
            initCodeWriter.write(postCode);
            if (!postCode.endsWith("\n")) // NOI18N
                initCodeWriter.write("\n"); // NOI18N
        }
    }

    private void generateComponentEvents(RADComponent comp, Writer initCodeWriter) throws IOException {
        String variablePrefix = getVariableGenString(comp);

        EventSet[] eventSets = comp.getEventHandlers().getEventSets();

        // go through the event sets - we generate the innerclass for whole
        // EventSet at once
        for (int i = 0; i < eventSets.length; i++) {
            Event events[] = eventSets[i].getEvents();
            EventSetDescriptor eventSetDesc = eventSets[i].getEventSetDescriptor();

            // try to find adpater to use instead of the listener
            Class classToGenerate = BeanSupport.getAdapterForListener(
                eventSetDesc.getListenerType());
            boolean adapterUsed = true;
            if (classToGenerate == null) { // if not found, we must use the listener
                classToGenerate = eventSetDesc.getListenerType();
                adapterUsed = false;
            }

            // test if we should generate the addListener for this eventSet
            boolean shouldGenerate = false;
            boolean[] shouldGenerateEvent = new boolean[events.length];
            for (int j = 0; j < events.length; j++) {
                if (events[j].getHandlers().size() > 0) {
                    shouldGenerate = true;
                    shouldGenerateEvent[j] = true;
                    continue;
                }
                else shouldGenerateEvent[j] = false;
            }
            // if we should generate inner class for this listener and we do not
            // use adapter, we must generate all methods!!!
            if (shouldGenerate && !adapterUsed)
                for (int j = 0; j < events.length; j++)
                    shouldGenerateEvent[j] = true;

            if (shouldGenerate) {
                Method eventAddMethod = eventSetDesc.getAddListenerMethod();
                Class[] exceptions = eventAddMethod.getExceptionTypes();
                if (needTryCode(exceptions)) {
                    initCodeWriter.write("try {\n"); // NOI18N
                } else {
                    exceptions = null;
                }

                // beginning of the addXXXListener
                initCodeWriter.write(variablePrefix);
                initCodeWriter.write(eventSetDesc.getAddListenerMethod().getName());
                initCodeWriter.write("(new "); // NOI18N
                initCodeWriter.write(classToGenerate.getName() + "() {\n"); // NOI18N

                // listener innerclass' methods - indented one more indent to the right
                for (int j = 0; j < events.length; j++) {
                    if (!shouldGenerateEvent[j])
                        continue;

                    Method evtMethod = events[j].getListenerMethod();
                    Class[] evtParams = evtMethod.getParameterTypes();
                    String[] varNames;

                    if ((evtParams.length == 1) &&
                        (java.util.EventObject.class.isAssignableFrom(evtParams[0])))
                        varNames = new String[] {
                            FormEditor.getFormSettings().getEventVariableName()
                        };
                    else {
                        varNames = new String[evtParams.length];
                        for (int k = 0; k < evtParams.length; k ++)
                            varNames[k] = "param" + k; // NOI18N
                    }

                    // generate the listener's method
                    initCodeWriter.write(getMethodHeaderText(evtMethod, varNames));
                    initCodeWriter.write(" {\n"); // NOI18N

                    if (events[j].getHandlers().size() > 0) {
                        // generate the call to the handlers
                        for (Iterator it = events[j].getHandlers().iterator(); it.hasNext();) {
                            EventHandler handler = (EventHandler) it.next();
                            if (evtMethod.getName().equals(handler.getName()))
                                initCodeWriter.write(formEditorSupport.getFormDataObject().getName() + ".this."); // NOI18N
                            initCodeWriter.write(handler.getName());
                            initCodeWriter.write("("); // NOI18N
                            for (int k = 0; k < varNames.length; k++) {
                                initCodeWriter.write(varNames[k]);
                                if (k != varNames.length - 1)
                                    initCodeWriter.write(", "); // NOI18N
                            }
                            initCodeWriter.write(");"); // NOI18N
                            initCodeWriter.write("\n"); // NOI18N
                        }
                    }
                    initCodeWriter.write("}\n"); // NOI18N
                }

                // end of the innerclass
                initCodeWriter.write("});\n"); // NOI18N

                // generate the catch code for the addXXXListener method
                if (exceptions != null)
                    generateCatchCode(exceptions, initCodeWriter);

                initCodeWriter.write("\n"); // NOI18N
            }
        }
    }

    private void addVariables(Writer variablesWriter)
        throws IOException
    {
        Iterator it = formModel.getCodeStructure().getVariablesIterator(
                                                   CodeVariable.FIELD,
                                                   CodeVariable.SCOPE_MASK,
                                                   null);

        while (it.hasNext()) {
            CodeVariable var = (CodeVariable) it.next();

            if (var.getDeclaredType() == org.netbeans.modules.form.Separator.class)
                continue; // treat AWT Separator specially - it is not a component

            if ((var.getType() & CodeVariable.FINAL) == CodeVariable.FINAL) {
                // final field variable - add also creation assignment
                Iterator it2 = var.getAttachedExpressions().iterator();
                if (it2.hasNext()) {
                    Object metaobject =
                        ((CodeExpression)it2.next()).getOrigin().getMetaObject();
                    if (metaobject instanceof RADComponent)
                        generateComponentCreate((RADComponent) metaobject,
                                                variablesWriter,
                                                false);
                }
            }
            else { // simple field variable declaration
                variablesWriter.write(
                    var.getDeclaration().getJavaCodeString(null, null));
                variablesWriter.write("\n"); // NOI18N
            }
        }
    }

    private boolean addLocalVariables(Writer initCodeWriter)
        throws IOException
    {
        Iterator it = formModel.getCodeStructure().getVariablesIterator(
            CodeVariable.LOCAL | CodeVariable.EXPLICIT_DECLARATION,
            CodeVariable.SCOPE_MASK | CodeVariable.DECLARATION_MASK,
            null);

        boolean anyVariable = false;
        while (it.hasNext()) {
            CodeVariable var = (CodeVariable) it.next();
            initCodeWriter.write(
                var.getDeclaration().getJavaCodeString(null, null));
            initCodeWriter.write("\n"); // NOI18N
            anyVariable = true;
        }
        return anyVariable;
    }

    private String getVariableGenString(RADComponent comp) {
        if (comp == formModel.getTopRADComponent()) {
                return ""; // NOI18N
        } else {
            return comp.getName() + "."; // NOI18N
        }
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
                continue; // needs not be caught

            if (i > 0) {
                int j;
                for (j=0; j < i; j++)
                    if (exceptions[j].isAssignableFrom(exception))
                        break;
                if (j < i)
                    continue; // a subclass of this exception already caught
            }

            initCodeWriter.write(" catch ("); // NOI18N
            initCodeWriter.write(exception.getName());
            initCodeWriter.write(" "); // NOI18N

            String varName = "e" + ++exCount; // NOI18N
//            int varCount = 0;
//            while (formModel.getVariablePool().isReserved(varName))
//                varName = "e" + (++varCount); // NOI18N

            initCodeWriter.write(varName);
            initCodeWriter.write(") {\n"); // NOI18N
            initCodeWriter.write(varName);
            initCodeWriter.write(".printStackTrace();\n"); // NOI18N
            initCodeWriter.write("}"); // NOI18N
                        
        }
        initCodeWriter.write("\n"); // NOI18N
    }

    // ---------
    // generating general code structure

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

    // -----------------------------------------------------------------------------
    // Event handlers

    /** Generates the specified event handler, if it does not exist yet.
     * @param handlerName The name of the event handler
     * @param paramTypes the array of event handler parameter types
     * @param exceptTypes the array of exception types that event handler throws
     * @param bodyText the body text of the event handler or null for default(empty) one
     * @return true if the event handler have not existed yet and was creaated, false otherwise
     */
    public boolean generateEventHandler(String handlerName, String[] paramTypes,
                                        String[] exceptTypes, String bodyText) {
        if (!initialized || !canGenerate
              || getEventHandlerSection(handlerName) != null)
            return false;

        IndentEngine engine = IndentEngine.find(formEditorSupport.getDocument());
        StringWriter buffer = new StringWriter();
        Writer codeWriter = engine.createWriter(
                        formEditorSupport.getDocument(),
                        initComponentsSection.getPositionAfter().getOffset(),
                        buffer);

        synchronized(GEN_LOCK) {
            try {
                JavaEditor.InteriorSection sec =
                    formEditorSupport.createInteriorSectionAfter(
                        initComponentsSection, getEventSectionName(handlerName));
                int i1, i2;

                codeWriter.write(getEventHandlerHeader(handlerName, paramTypes, exceptTypes));
                codeWriter.flush();
                i1 = buffer.getBuffer().length();
                codeWriter.write(getEventHandlerBody(bodyText));
                codeWriter.flush();
                i2 = buffer.getBuffer().length();
                codeWriter.write(getEventHandlerFooter());
                codeWriter.flush();

                sec.setHeader(buffer.getBuffer().substring(0,i1));
                sec.setBody(buffer.getBuffer().substring(i1,i2));
                sec.setBottom(buffer.getBuffer().substring(i2));

                codeWriter.close();
            } 
            catch (javax.swing.text.BadLocationException e) {
                return false;
            }
            catch (java.io.IOException ioe) {
                return false;
            }
            clearUndo();
        }
        return true;
    }

    /** Changes the text of the specified event handler, if it already exists.
     * @param handlerName The name of the event handler
     * @param paramTypes the array of event handler parameter types
     * @param exceptTypes the array of exception types that event handler throws
     * @param bodyText the new body text of the event handler or null for default(empty) one
     * @return true if the event handler existed and was modified, false otherwise
     */
    public boolean changeEventHandler(final String handlerName, final String[] paramTypes,
                                      final String[] exceptTypes, final String bodyText) {
        JavaEditor.InteriorSection sec = getEventHandlerSection(handlerName);
        if (sec == null || !initialized || !canGenerate)
            return false;

        IndentEngine engine = IndentEngine.find(formEditorSupport.getDocument());
        StringWriter buffer = new StringWriter();
        Writer codeWriter = engine.createWriter(formEditorSupport.getDocument(),
                                                sec.getPositionBefore().getOffset(),
                                                buffer);
        synchronized(GEN_LOCK) {
            try {
                int i1, i2;

                codeWriter.write(getEventHandlerHeader(handlerName, paramTypes, exceptTypes));
                codeWriter.flush();
                i1 = buffer.getBuffer().length();
                codeWriter.write(getEventHandlerBody(bodyText));
                codeWriter.flush();
                i2 = buffer.getBuffer().length();
                codeWriter.write(getEventHandlerFooter());
                codeWriter.flush();

                sec.setHeader(buffer.getBuffer().substring(0,i1));
                sec.setBody(buffer.getBuffer().substring(i1,i2));
                sec.setBottom(buffer.getBuffer().substring(i2));

                codeWriter.close();
            }
            catch (IOException e) {
                return false;
            }
            clearUndo();
        }
        return true;
    }

    /** Removes the specified event handler - removes the whole method together with the user code!
     * @param handlerName The name of the event handler
     */
    public boolean deleteEventHandler(String handlerName) {
        JavaEditor.InteriorSection section = getEventHandlerSection(handlerName);
        if (section == null || !initialized || !canGenerate)
            return false;

        synchronized(GEN_LOCK) {
            section.deleteSection();
            clearUndo();
        }

        return true;
    }

    private String getEventHandlerHeader(String handlerName, String[] paramTypes, String[] exceptTypes) {
        StringBuffer buf = new StringBuffer();

        buf.append("private void "); // NOI18N
        buf.append(handlerName);
        buf.append("("); // NOI18N

        // create variable names
        String[] varNames = new String [paramTypes.length];

        if (paramTypes.length == 1)
            varNames [0] = paramTypes [0] + " " + formSettings.getEventVariableName(); // NOI18N
        else
            for (int i = 0; i < paramTypes.length; i ++)
                varNames [i] = paramTypes [0] + " param" + i; // NOI18N

        for (int i = 0; i < paramTypes.length; i++) {
            buf.append(varNames[i]);
            if (i != paramTypes.length - 1)
                buf.append(", "); // NOI18N
            else
                buf.append(")"); // NOI18N
        }

        if (exceptTypes != null && exceptTypes.length > 0) {
            buf.append(" throws "); // NOI18N

            for (int i=0; i < exceptTypes.length; i++) {
                buf.append(exceptTypes[i]);
                if (i != exceptTypes.length - 1)
                    buf.append(", "); // NOI18N
            }
        }
        buf.append(" {\n"); // NOI18N

        return buf.toString();
    }

    private String getEventHandlerBody(String bodyText) {
        if (bodyText == null) {
            bodyText = getDefaultEventBody();
        }
        return bodyText;
    }

    private String getEventHandlerFooter() {
        return "}\n"; // NOI18N
    }

    private String getDefaultEventBody() {
        return FormEditor.getFormBundle().getString("MSG_EventHandlerBody"); // NOI18N
    }

    /** Renames the specified event handler to the given new name.
     * @param oldHandlerName The old name of the event handler
     * @param newHandlerName The new name of the event handler
     */
    public boolean renameEventHandler(String oldHandlerName, String newHandlerName,
                                      String[] paramTypes, String[] exceptTypes) {
        JavaEditor.InteriorSection sec = getEventHandlerSection(oldHandlerName);
        if (sec == null || !initialized || !canGenerate)
            return false;

        IndentEngine engine = IndentEngine.find(formEditorSupport.getDocument());
        StringWriter buffer = new StringWriter();
        Writer codeWriter = engine.createWriter(formEditorSupport.getDocument(),
                                                sec.getPositionBefore().getOffset(),
                                                buffer);
        synchronized(GEN_LOCK) {
            try {
                codeWriter.write(getEventHandlerHeader(newHandlerName, paramTypes, exceptTypes));
                codeWriter.flush();
                int i1 = buffer.getBuffer().length();
                codeWriter.write(getEventHandlerFooter());
                codeWriter.flush();

                sec.setHeader(buffer.getBuffer().substring(0,i1));
                sec.setBottom(buffer.getBuffer().substring(i1));
                sec.setName(getEventSectionName(newHandlerName));

                codeWriter.close();
            } 
            catch (java.beans.PropertyVetoException e) {
                return false;
            }
            catch (IOException e) {
                return false;
            }
            clearUndo();
        }
        return true;
    }

    /** Focuses the specified event handler in the editor. */
    public void gotoEventHandler(String handlerName) {
        JavaEditor.InteriorSection sec = getEventHandlerSection(handlerName);
        if (sec != null && initialized) {
            sec.openAt();
            formEditorSupport.gotoEditor();
        }
    }

    /** 
     * Returns whether the specified event handler is empty (with no user
     * code). Empty handlers can be deleted without user confirmation.
     * @return true if the event handler exists and is empty
     */
    public boolean isEventHandlerEmpty(String handlerName) {
        JavaEditor.InteriorSection section = getEventHandlerSection(handlerName);
        if (section != null) {
            String tx = section.getText();
            tx = tx.substring(tx.indexOf("{")+1, tx.lastIndexOf("}")).trim(); // NOI18N
            return tx.equals("") || tx.equals(getDefaultEventBody().trim()); // NOI18N
        }
        return false;
    }

    // ------------------------------------------------------------------------------------------
    // Private methods

    /** Clears undo buffer after code generation */
    private void clearUndo() {
        formEditorSupport.getUndoManager().discardAllEdits();
    }

    // sections acquirement

    private JavaEditor.InteriorSection getEventHandlerSection(String eventName) {
        return formEditorSupport.findInteriorSection(getEventSectionName(eventName));
    }

    // other

    private String getEventSectionName(String handlerName) {
        return EVT_SECTION_PREFIX + handlerName;
    }

    /** A utility method for formatting method header text for specified
     * method, its name and parameter names.
     * @param m The method - its modifiers, return type, parameter types and
     *                       exceptions are used
     * @param paramNames An array of names of parameters - the length of this
     *            array MUST be the same as the actual number of method's parameters
     */
    private String getMethodHeaderText(Method m, String[] paramNames) {
        StringBuffer buf = new StringBuffer() ;
        buf.append("public "); // NOI18N
        buf.append(m.getReturnType().getName());
        buf.append(" "); // NOI18N
        buf.append(m.getName());
        buf.append("("); // NOI18N

        Class[] params = m.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            buf.append(params[i].getName());
            buf.append(" "); // NOI18N
            buf.append(paramNames[i]);
            if (i != params.length - 1)
                buf.append(", "); // NOI18N
        }
        buf.append(")"); // NOI18N

        Class[] exceptions = m.getExceptionTypes();
        if (exceptions.length != 0) {
            buf.append("\n"); // NOI18N
            buf.append("throws "); // NOI18N
        }
        for (int i = 0; i < exceptions.length; i++) {
            buf.append(exceptions[i].getName());
            if (i != exceptions.length - 1)
                buf.append(", "); // NOI18N
        }

        return buf.toString();
    }

    /** Checks whether loaded form needs to be regenerated (code for
     * initComponents() and variables). If the diff in last modif time
     * of .java and .form files is less then (say) 1.5 sec then it is
     * not necessary to regenerate the code.
     */
    private boolean needsRegeneration() {
        FormDataObject fdo = formEditorSupport.getFormDataObject();
        Entry primary = fdo.getPrimaryEntry();
        Entry form = fdo.formEntry;

        FileObject primaryFO = primary.getFile();
        FileObject formFO = form.getFile();

        long diff = formFO.lastModified().getTime() - primaryFO.lastModified().getTime();
        diff = Math.abs(diff);
        return diff > 1500 ? true : false;
    }

    private String indentCode(String code, IndentEngine refEngine) {
        int spacesPerTab = 4;
        boolean braceOnNewLine = false;

        if (refEngine != null) {
            Class engineClass = refEngine.getClass();

            try {
                Method m = engineClass.getMethod("getSpacesPerTab", // NOI18N
                                                 new Class[0]);
                spacesPerTab = ((Integer)m.invoke(refEngine, new Object[0]))
                                         .intValue();
            }
            catch (Exception ex) {} // ignore

            try {
                Method m = engineClass.getMethod("getJavaFormatNewlineBeforeBrace", // NOI18N
                                                 new Class[0]);
                braceOnNewLine = ((Boolean)m.invoke(refEngine, new Object[0]))
                                           .booleanValue();
            }
            catch (Exception ex) {} // ignore
        }

        StringBuffer tab = new StringBuffer(spacesPerTab);
        for (int i=0; i < spacesPerTab; i++)
            tab.append(" "); // NOI18N

        return doIndentation(code, 1, tab.toString(), braceOnNewLine);
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
                    if (c == '}' || c == ')') {
                        lastOpeningBr = -1;
                        endingSpace = -1;
                        if (startingSpace) { // first non-space char on the line
                            firstClosingBr = true;
                            closingBr = true;
                            startingSpace = false;
                            lineStart = i;
                        }
                        else if (!closingBr)
                            brackets--;
                    }
                    else if (c == '{' || c == '(') {
                        closingBr = false;
                        lastOpeningBr = -1;
                        endingSpace = -1;
                        if (startingSpace) { // first non-space char on the line
                            startingSpace = false;
                            lineStart = i;
                        }
                        else if (c == '{') // possible last brace on the line
                            lastOpeningBr = i;
                        brackets++;
                    }
                    else if (c == '\"') { // start of String, its content is ignored
                        insideString = true;
                        lastOpeningBr = -1;
                        endingSpace = -1;
                        if (startingSpace) { // first non-space char on the line
                            startingSpace = false;
                            lineStart = i;
                        }
                    }
                    else if (c == ' ' || c == '\t') {
                        if (endingSpace < 0)
                            endingSpace = i;
                    }
                    else {
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
                }
                else if (c == '\"' && code.charAt(i-1) != '\\') // end of String
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
            }
            else lastLineEmpty = false;

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
            }
            else // skip spaces at the end of the line
                lineEnd = endingSpace;

            // write the line
            buffer.append(code.substring(lineStart, lineEnd));
            buffer.append("\n"); // NOI18N

            // calculate indentation level for the next line
            if (brackets < 0) {
                if (indentLevel > minIndentLevel)
                    indentLevel--;
            }
            else if (brackets > 0)
                indentLevel++;
        }

        return buffer.toString();
    }

    //
    // {{{ JCGFormListener
    //

    private class JCGFormListener extends FormModelAdapter
    {
        /** Called when the form is succesfully loaded and fully initialized */

        public void formLoaded(FormModelEvent e) {
            if (needsRegeneration()) {
                regenerateVariables();
                regenerateInitializer();
                regenerateEventHandlers();
            }
        }

        public void formChanged(FormModelEvent e) {
            if (!formSettings.getGenerateOnSave()) {
                regenerateVariables();
                regenerateInitializer();
                codeUpToDate = true;
            }
            else codeUpToDate = false;
        }

        /** Called when the form is about to be saved */

        public void formToBeSaved(FormModelEvent e) {
            if (!codeUpToDate && formSettings.getGenerateOnSave()) {
                regenerateVariables();
                regenerateInitializer();
                codeUpToDate = true;
            }

//            serializeComponentsRecursively(formModel.getTopRADComponent());
            RADComponent[] components = formModel.getModelContainer().getSubBeans();
            for (int i = 0; i < components.length; i++)
                serializeComponentsRecursively(components[i]);
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
                            java.io.ObjectOutputStream oos = null;
                            try {
                                lock = serFile.lock();
                                oos = new java.io.ObjectOutputStream(serFile.getOutputStream(lock));
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
    // }}}

    //
    // {{{ CodeGenerateEditor
    //

    final public static class CodeGenerateEditor extends PropertyEditorSupport
    {
        private RADComponent component;

        /** Display Names for alignment. */
        private static final String generateName = FormEditor.getFormBundle().getString("VALUE_codeGen_generate"); // NOI18N
        private static final String serializeName = FormEditor.getFormBundle().getString("VALUE_codeGen_serialize"); // NOI18N

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
    // {{{ CodePropertySupportRW
    //

    abstract class CodePropertySupportRW extends PropertySupport.ReadWrite
    {
        CodePropertySupportRW(String name, Class type,
                              String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        public PropertyEditor getPropertyEditor() {
            return new PropertyEditorSupport() {
                public java.awt.Component getCustomEditor() {
                    return new CustomCodeEditor(CodePropertySupportRW.this);
                }

                public boolean supportsCustomEditor() {
                    return true;
                }
            };
        }

        public boolean canWrite() {
            return JavaCodeGenerator.this.canGenerate;
        }
    }

    // }}}
}
