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
import org.openide.windows.Workspace;
import org.openide.windows.WindowManager;

import org.netbeans.modules.form.palette.*;

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
                new PropertyDescriptor(FormLoaderSettings.PROP_WORKSPACE,
                                       FormLoaderSettings.class,
                                       "getWorkspace", "setWorkspace"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_PALETTE_IN_TOOLBAR,
                                       FormLoaderSettings.class,
                                       "isPaletteInToolBar", // NOI18N
                                       "setPaletteInToolBar"), // NOI18N
                new PropertyDescriptor(FormLoaderSettings.PROP_CONTAINER_BEANS,
                                       FormLoaderSettings.class,
                                       "getContainerBeans", // NOI18N
                                       "setContainerBeans"), // NOI18N
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
                                       "setDisplayWritableOnly") // NOI18N
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

            desc[15].setDisplayName(bundle.getString("PROP_WORKSPACE")); // NOI18N
            desc[15].setShortDescription(bundle.getString("HINT_WORKSPACE")); // NOI18N
            desc[15].setPropertyEditorClass(WorkspaceEditor.class);
            desc[15].setExpert(true);

            desc[16].setDisplayName(bundle.getString("PROP_PALETTE_IN_TOOLBAR")); // NOI18N
            desc[16].setShortDescription(bundle.getString("HINT_PALETTE_IN_TOOLBAR")); // NOI18N

            desc[17].setHidden(true);

            desc[18].setDisplayName(bundle.getString("PROP_FORMDESIGNER_BACKGROUND_COLOR")); // NOI18N
            desc[18].setShortDescription(bundle.getString("HINT_FORMDESIGNER_BACKGROUND_COLOR")); // NOI18N

            desc[19].setDisplayName(bundle.getString("PROP_FORMDESIGNER_BORDER_COLOR")); // NOI18N
            desc[19].setShortDescription(bundle.getString("HINT_FORMDESIGNER_BORDER_COLOR")); // NOI18N

            desc[20].setDisplayName(bundle.getString("PROP_SHOW_COMPONENT_NAMES")); // NOI18N
            desc[20].setShortDescription(bundle.getString("HINT_SHOW_COMPONENT_NAMES")); // NOI18N

            desc[21].setDisplayName(bundle.getString("PROP_VARIABLES_LOCAL")); // NOI18N
            desc[21].setShortDescription(bundle.getString("HINT_VARIABLES_LOCAL")); // NOI18N
            desc[21].setExpert(true);

            desc[22].setHidden(true);

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


    final public static class WorkspaceEditor extends PropertyEditorSupport {
        /** Mapping between programmatic and display names of workspaces */
        private Map namesMap;
        /** Validity flag - true if namesMap has been initialized already */
        private boolean namesInitialized = false;

        /*
         * @return The property value as a human editable string.
         * <p> Returns null if the value can't be expressed as an editable string.
         * <p> If a non-null value is returned, then the PropertyEditor should
         *     be prepared to parse that string back in setAsText().
         */
        public String getAsText() {
            if (!namesInitialized) {
                namesInitialized = true;
                initializeNamesMap(WindowManager.getDefault().getWorkspaces());
            }
            String value =(String)getValue();
            String displayName =(String)namesMap.get(value);
            return displayName == null ? value : displayName;
        }

        /* Set the property value by parsing a given String. May raise
         * java.lang.IllegalArgumentException if either the String is
         * badly formatted or if this kind of property can't be expressed
         * as text.
         * @param text The string to be parsed.
         */
        public void setAsText(String text) throws IllegalArgumentException {
            String programmaticName = findProgrammaticName(text);
            setValue(programmaticName == null ? text : programmaticName);
        }

        /*
         * If the property value must be one of a set of known tagged values,
         * then this method should return an array of the tag values. This can
         * be used to represent(for example) enum values. If a PropertyEditor
         * supports tags, then it should support the use of setAsText with
         * a tag value as a way of setting the value.
         *
         * @return The tag values for this property. May be null if this
         *   property cannot be represented as a tagged value.
         *
         */
        public String[] getTags() {
            Workspace[] wss = WindowManager.getDefault().getWorkspaces();
            initializeNamesMap(wss);

            // exclude browsing, running and debugging workspaces
            List tagList = new ArrayList();
            for (int i = wss.length; --i >= 0;) {
                String name = wss[i].getName();
                if (!"Debugging".equals(name)) // NOI18N
                    tagList.add(name);
            }
            tagList.add(FormUtils.getBundleString("VALUE_WORKSPACE_NONE")); // NOI18N

            String[] names = new String [tagList.size()]; // + 1];
            for (int i=0, n = tagList.size(); i < n; i++)
                names[i] = (String)namesMap.get(tagList.get(i));

            return names;
        }

        /** Initializes name mapping with given workspace set.
         * Result is stored in nameMap private variable. */
        private void initializeNamesMap(Workspace[] wss) {
            // fill name mapping with proper values
            namesMap = new HashMap(wss.length * 2);
            for (int i = 0; i < wss.length; i++) {
                // create new string for each display name to be able to search
                // using '==' operator in findProgrammaticName(String displayName) method
                String displayName = wss[i].getDisplayName();
                int index = displayName.indexOf('&');
                String part1 = ""; // NOI18N
                String part2 = ""; // NOI18N
                if (index>0)
                    part1 = displayName.substring(0, index);
                if (index<(displayName.length()-1))
                    part2 = displayName.substring(index+1, displayName.length());

                namesMap.put(wss[i].getName(), new String(part1 + part2));;
            }
            namesMap.put(FormEditorSupport.NO_WORKSPACE,
                         FormUtils.getBundleString("VALUE_WORKSPACE_NONE")); // NOI18N
        }

        /** @return Returns programmatic name of the workspace for given
         * display name of the workspace. Uses special features of namesMap mapping
         * to perform succesfull search. */
        private String findProgrammaticName(String displayName) {
            for (Iterator iter = namesMap.entrySet().iterator(); iter.hasNext();) {
                Map.Entry curEntry =(Map.Entry)iter.next();
                if (displayName == curEntry.getValue())
                    return(String)curEntry.getKey();
            }
            return null;
        }
    }


    final public static class PalettesEditor extends PropertyEditorSupport {
        private boolean initialized = false;
        private CPElements.Palette[] registeredPalettes;

        /*
         * @return The property value as a human editable string.
         * <p> Returns null if the value can't be expressed as an editable string.
         * <p> If a non-null value is returned, then the PropertyEditor should
         *     be prepared to parse that string back in setAsText().
         */
        public String getAsText() {
            if (!initialized) {
                initializePalettes();
                initialized = true;
            }

            if (registeredPalettes.length < 1)
                return FormUtils.getBundleString("VALUE_SELECTED_PALETTE_NONE"); // NOI18N

            Object value = getValue();
            int index = 0;
            if (value instanceof Integer)
                index = ((Integer)value).intValue();

            if (index < registeredPalettes.length)
                return registeredPalettes[index].getPaletteName();
            else return null;
        }

        /* Set the property value by parsing a given String. May raise
         * java.lang.IllegalArgumentException if either the String is
         * badly formatted or if this kind of property can't be expressed
         * as text.
         * @param text The string to be parsed.
         */
        public void setAsText(String text) throws java.lang.IllegalArgumentException {
            if (!initialized) {
                initializePalettes();
                initialized = true;
            }

            int index = getIndexOfPalette(text);
            setValue(new Integer(index));
        }

        /*
         * If the property value must be one of a set of known tagged values,
         * then this method should return an array of the tag values. This can
         * be used to represent(for example) enum values. If a PropertyEditor
         * supports tags, then it should support the use of setAsText with
         * a tag value as a way of setting the value.
         * @return The tag values for this property. May be null if this
         *   property cannot be represented as a tagged value.
         *
         */
        public String[] getTags() {
            if (!initialized) {
                initializePalettes();
                initialized = true;
            }

            String[] names;

            if (registeredPalettes.length > 0) {
                names = new String [registeredPalettes.length];

                for (int i=0; i<registeredPalettes.length; i++) {
                    names[i] = registeredPalettes[i].getPaletteName();
                }
            }
            else names = new String[] {
                FormUtils.getBundleString("VALUE_SELECTED_PALETTE_NONE") }; // NOI18N

            return names;
        }

        /** gets array of available palettes from CPManager */
        private void initializePalettes() {
            registeredPalettes = CPManager.getDefault().getRegisteredPalettes();
        }

        /** gets index of palette given by name */
        private int getIndexOfPalette(String name) {
            for (int i=0; i<registeredPalettes.length; i++) {
                if (registeredPalettes[i].getPaletteName().equals(name))
                    return i;
            }
            return -1;
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
