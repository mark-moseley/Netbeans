/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.awt.Image;
import java.beans.*;
import java.lang.reflect.Modifier;
import java.util.*;

import org.openide.util.Utilities;

/**
 * A BeanInfo for FormLoaderSettings.
 */

public class FormLoaderSettingsBeanInfo extends SimpleBeanInfo {

    /** The icons for Settings */
    private static String iconURL =
        "org/netbeans/modules/form/resources/formSettings.gif"; // NOI18N
    private static String icon32URL =
        "org/netbeans/modules/form/resources/formSettings32.gif"; // NOI18N

    /** Descriptor of valid properties
     * @return array of properties
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor[] desc = new PropertyDescriptor[] {
                new PropertyDescriptor(FormLoaderSettings.PROP_USE_INDENT_ENGINE,
                                       FormLoaderSettings.class,
                                       "getUseIndentEngine", // NOI18N
                                       "setUseIndentEngine"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_GENERATE_ON_SAVE,
                                       FormLoaderSettings.class,
                                       "getGenerateOnSave", // NOI18N
                                       "setGenerateOnSave"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_EVENT_VARIABLE_NAME,
                                       FormLoaderSettings.class,
                                       "getEventVariableName", // NOI18N
                                       "setEventVariableName"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_LISTENER_GENERATION_STYLE,
                                       FormLoaderSettings.class,
                                       "getListenerGenerationStyle", // NOI18N
                                       "setListenerGenerationStyle"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_SELECTION_BORDER_SIZE,
                                       FormLoaderSettings.class,
                                       "getSelectionBorderSize", // NOI18N
                                       "setSelectionBorderSize"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_SELECTION_BORDER_COLOR,
                                       FormLoaderSettings.class,
                                       "getSelectionBorderColor", // NOI18N
                                       "setSelectionBorderColor"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_CONNECTION_BORDER_COLOR,
                                       FormLoaderSettings.class,
                                       "getConnectionBorderColor", // NOI18N
                                       "setConnectionBorderColor"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_DRAG_BORDER_COLOR,
                                       FormLoaderSettings.class,
                                       "getDragBorderColor", // NOI18N
                                       "setDragBorderColor"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_GRID_X,
                                       FormLoaderSettings.class,
                                       "getGridX", "setGridX"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_GRID_Y,
                                       FormLoaderSettings.class,
                                       "getGridY", "setGridY"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_APPLY_GRID_TO_POSITION,
                                       FormLoaderSettings.class,
                                       "getApplyGridToPosition", // NOI18N
                                       "setApplyGridToPosition"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_APPLY_GRID_TO_SIZE,
                                       FormLoaderSettings.class,
                                       "getApplyGridToSize", // NOI18N
                                       "setApplyGridToSize"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_VARIABLES_MODIFIER,
                                       FormLoaderSettings.class,
                                       "getVariablesModifier", // NOI18N
                                       "setVariablesModifier"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_EDITOR_SEARCH_PATH,
                                       FormLoaderSettings.class,
                                       "getEditorSearchPath", // NOI18N
                                       "setEditorSearchPath"), // NOI18N
                new IndexedPropertyDescriptor(FormLoaderSettings.PROP_REGISTERED_EDITORS,
                                              FormLoaderSettings.class,
                                              "getRegisteredEditors", // NOI18N
                                              "setRegisteredEditors", // NOI18N
                                              "getRegisteredEditor", // NOI18N
                                              "setRegisteredEditor"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_PALETTE_IN_TOOLBAR,
                                       FormLoaderSettings.class,
                                       "isPaletteInToolBar", // NOI18N
                                       "setPaletteInToolBar"), // NOI18N
//                new PropertyDescriptor(FormLoaderSettings.PROP_CONTAINER_BEANS,
//                                       FormLoaderSettings.class,
//                                       "getContainerBeans", // NOI18N
//                                       "setContainerBeans"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_FORMDESIGNER_BACKGROUND_COLOR,
                                       FormLoaderSettings.class,
                                       "getFormDesignerBackgroundColor", // NOI18N
                                       "setFormDesignerBackgroundColor"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_FORMDESIGNER_BORDER_COLOR,
                                       FormLoaderSettings.class,
                                       "getFormDesignerBorderColor", // NOI18N
                                       "setFormDesignerBorderColor"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_SHOW_COMPONENTS_NAMES,
                                       FormLoaderSettings.class,
                                       "getShowComponentsNames", // NOI18N
                                       "setShowComponentsNames"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_VARIABLES_LOCAL,
                                       FormLoaderSettings.class,
                                       "getVariablesLocal", // NOI18N
                                       "setVariablesLocal"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_DISPLAY_WRITABLE_ONLY,
                                       FormLoaderSettings.class,
                                       "getDisplayWritableOnly", // NOI18N
                                       "setDisplayWritableOnly"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_GENERATE_MNEMONICS,
                                       FormLoaderSettings.class,
                                       "getGenerateMnemonicsCode", // NOI18N
                                       "setGenerateMnemonicsCode"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_SHOW_MNEMONICS_DIALOG,
                                       FormLoaderSettings.class,
                                       "getShowMnemonicsDialog", // NOI18N
                                       "setShowMnemonicsDialog") // NOI18N
            };

            ResourceBundle bundle = FormUtils.getBundle();

            desc[0].setDisplayName(bundle.getString("PROP_USE_INDENT_ENGINE")); // NOI18N
            desc[0].setShortDescription(bundle.getString("HINT_USE_INDENT_ENGINE")); // NOI18N

            desc[1].setDisplayName(bundle.getString("PROP_GENERATE_ON_SAVE")); // NOI18N
            desc[1].setShortDescription(bundle.getString("HINT_GENERATE_ON_SAVE")); // NOI18N

            desc[2].setDisplayName(bundle.getString("PROP_EVENT_VARIABLE_NAME")); // NOI18N
            desc[2].setShortDescription(bundle.getString("HINT_EVENT_VARIABLE_NAME")); // NOI18N
            desc[2].setExpert(true);

            desc[3].setDisplayName(bundle.getString("PROP_LISTENER_GENERATION_STYLE")); // NOI18N
            desc[3].setShortDescription(bundle.getString("HINT_LISTENER_GENERATION_STYLE")); // NOI18N
            desc[3].setPropertyEditorClass(ListenerGenerationStyleEditor.class);
            desc[3].setExpert(true);

            desc[4].setDisplayName(bundle.getString("PROP_SELECTION_BORDER_SIZE")); // NOI18N
            desc[4].setShortDescription(bundle.getString("HINT_SELECTION_BORDER_SIZE")); // NOI18N

            desc[5].setDisplayName(bundle.getString("PROP_SELECTION_BORDER_COLOR")); // NOI18N
            desc[5].setShortDescription(bundle.getString("HINT_SELECTION_BORDER_COLOR")); // NOI18N

            desc[6].setDisplayName(bundle.getString("PROP_CONNECTION_BORDER_COLOR")); // NOI18N
            desc[6].setShortDescription(bundle.getString("HINT_CONNECTION_BORDER_COLOR")); // NOI18N

            desc[7].setDisplayName(bundle.getString("PROP_DRAG_BORDER_COLOR")); // NOI18N
            desc[7].setShortDescription(bundle.getString("HINT_DRAG_BORDER_COLOR")); // NOI18N

            desc[8].setDisplayName(bundle.getString("PROP_GRID_X")); // NOI18N
            desc[8].setShortDescription(bundle.getString("HINT_GRID_X")); // NOI18N
            desc[8].setExpert(true);

            desc[9].setDisplayName(bundle.getString("PROP_GRID_Y")); // NOI18N
            desc[9].setShortDescription(bundle.getString("HINT_GRID_Y")); // NOI18N
            desc[9].setExpert(true);

            desc[10].setDisplayName(bundle.getString("PROP_APPLY_GRID_TO_POSITION")); // NOI18N
            desc[10].setShortDescription(bundle.getString("HINT_APPLY_GRID_TO_POSITION")); // NOI18N
            desc[10].setExpert(true);

            desc[11].setDisplayName(bundle.getString("PROP_APPLY_GRID_TO_SIZE")); // NOI18N
            desc[11].setShortDescription(bundle.getString("HINT_APPLY_GRID_TO_SIZE")); // NOI18N
            desc[11].setExpert(true);

            desc[12].setDisplayName(bundle.getString("PROP_VARIABLES_MODIFIER")); // NOI18N
            desc[12].setShortDescription(bundle.getString("HINT_VARIABLES_MODIFIER")); // NOI18N
            desc[12].setPropertyEditorClass(FieldModifierPropertyEditor.class);
            desc[12].setExpert(true);

            desc[13].setDisplayName(bundle.getString("PROP_EDITOR_SEARCH_PATH")); // NOI18N
            desc[13].setShortDescription(bundle.getString("HINT_EDITOR_SEARCH_PATH")); // NOI18N
            desc[13].setExpert(true);

            desc[14].setDisplayName(bundle.getString("PROP_REGISTERED_EDITORS")); // NOI18N
            desc[14].setShortDescription(bundle.getString("HINT_REGISTERED_EDITORS")); // NOI18N
            desc[14].setExpert(true);

            desc[15].setDisplayName(bundle.getString("PROP_PALETTE_IN_TOOLBAR")); // NOI18N
            desc[15].setShortDescription(bundle.getString("HINT_PALETTE_IN_TOOLBAR")); // NOI18N

//            desc[16].setHidden(true);

            desc[16].setDisplayName(bundle.getString("PROP_FORMDESIGNER_BACKGROUND_COLOR")); // NOI18N
            desc[16].setShortDescription(bundle.getString("HINT_FORMDESIGNER_BACKGROUND_COLOR")); // NOI18N

            desc[17].setDisplayName(bundle.getString("PROP_FORMDESIGNER_BORDER_COLOR")); // NOI18N
            desc[17].setShortDescription(bundle.getString("HINT_FORMDESIGNER_BORDER_COLOR")); // NOI18N

            desc[18].setDisplayName(bundle.getString("PROP_SHOW_COMPONENT_NAMES")); // NOI18N
            desc[18].setShortDescription(bundle.getString("HINT_SHOW_COMPONENT_NAMES")); // NOI18N

            desc[19].setDisplayName(bundle.getString("PROP_VARIABLES_LOCAL")); // NOI18N
            desc[19].setShortDescription(bundle.getString("HINT_VARIABLES_LOCAL")); // NOI18N
            desc[19].setExpert(true);

            desc[20].setHidden(true);

            desc[21].setDisplayName(bundle.getString("PROP_GENERATE_MNEMONICS")); // NOI18N
            desc[21].setShortDescription(bundle.getString("HINT_GENERATE_MNEMONICS")); // NOI18N
            desc[21].setExpert(true);

            desc[22].setDisplayName(bundle.getString("PROP_SHOW_MNEMONICS_DIALOG")); // NOI18N
            desc[22].setShortDescription(bundle.getString("HINT_SHOW_MNEMONICS_DIALOG")); // NOI18N
            desc[22].setExpert(true);

            return desc;
        }
        catch (IntrospectionException ex) {
            throw new InternalError();
        }
    }

    /** Returns the FormLoaderSettings' icon */
    public Image getIcon(int type) {
        return Utilities.loadImage(
                   type == java.beans.BeanInfo.ICON_COLOR_16x16
                       || type == java.beans.BeanInfo.ICON_MONO_16x16 ?
                   iconURL : icon32URL);
    }

    // --------

    /** Property editor for variables modifiers.
     */
    final public static class FieldModifierPropertyEditor
        extends org.openide.explorer.propertysheet.editors.ModifierEditor
    {
        static final long serialVersionUID =7628317154007139777L;
        /** Construct new editor with mask for fields. */
        public FieldModifierPropertyEditor() {
            super(Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE
                  | Modifier.STATIC | Modifier.FINAL | Modifier.TRANSIENT
                  | Modifier.VOLATILE);
        }
    }


    public final static class ListenerGenerationStyleEditor
                      extends org.netbeans.modules.form.editors.EnumEditor
    {
        public ListenerGenerationStyleEditor() {
            super(new Object[] {
                FormUtils.getBundleString("CTL_LISTENER_ANONYMOUS_CLASSES"), // NOI18N
                new Integer(JavaCodeGenerator.ANONYMOUS_INNERCLASSES),
                "", // NOI18N
                FormUtils.getBundleString("CTL_LISTENER_CEDL_INNERCLASS"), // NOI18N
                new Integer(JavaCodeGenerator.CEDL_INNERCLASS),
                "", // NOI18N
                FormUtils.getBundleString("CTL_LISTENER_CEDL_MAINCLASS"), // NOI18N
                new Integer(JavaCodeGenerator.CEDL_MAINCLASS),
                "" // NOI18N
            });
        }
    }
}
