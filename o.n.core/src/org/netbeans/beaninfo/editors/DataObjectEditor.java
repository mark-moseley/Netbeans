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

package org.netbeans.beaninfo.editors;

import java.beans.*;

import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataFilter;
import org.openide.explorer.propertysheet.*;

/**
 * Property editor for org.openide.loaders.DataObject.
 * Uses class DataObjectPanel as custom property editor.
 * @author David Strupl
 */
public class DataObjectEditor extends PropertyEditorSupport implements ExPropertyEditor {

    /** Name of the custom property that can be passed in PropertyEnv. */
    private static final String PROPERTY_CURRENT_FOLDER = "currentFolder"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. */
    private static final String PROPERTY_ROOT_FOLDER = "rootFolder"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. */
    private static final String PROPERTY_ROOT_NODE = "rootNode"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. */
    private static final String PROPERTY_COOKIES = "cookies"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. */
    private static final String PROPERTY_DATA_FILTER = "dataFilter"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. */
    private static final String PROPERTY_FOLDER_FILTER = "folderFilter"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. */
    private static final String PROPERTY_NODE_ACCEPTOR = "nodeAcceptor"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. */
    private static final String PROPERTY_LABEL = "label"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. */
    private static final String PROPERTY_TITLE = "title"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. */
    private static final String PROPERTY_INSET = "inset"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. */
    private static final String PROPERTY_DESCRIPTION = "description"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. */
    private static final String PROPERTY_GUI_TYPE = "guitype"; // NOI18N
    /** Name of the custom property that can be passed in PropertyEnv. */
    private static final String PROPERTY_SELECTION_MODE = "selectionMode"; // NOI18N
    
    /** This gets lazy initialized in getDataObjectPanel*/
    private DataObjectPanel customEditor;
   
    /** A property stored between calls to atachEnv and getCustomEditor() */
    private DataFolder rootFolder;
    /** A property stored between calls to atachEnv and getCustomEditor() */
    private Node rootNode;
    /** A property stored between calls to atachEnv and getCustomEditor() */
    private DataObject currentFolder;
    /** A property stored between calls to atachEnv and getCustomEditor() */
    private Class[] cookies;
    /** A property stored between calls to atachEnv and getCustomEditor() */
    private DataFilter dataFilter;
    /** A property stored between calls to atachEnv and getCustomEditor() */
    private DataFilter folderFilter;
    /** A property stored between calls to atachEnv and getCustomEditor() */
    private NodeAcceptor nodeAcceptor;
    /** A property stored between calls to atachEnv and getCustomEditor() */
    private String label;
    /** A property stored between calls to atachEnv and getCustomEditor() */
    private String title;
    /** A property stored between calls to atachEnv and getCustomEditor() */
    private Integer insets;
    /** A property stored between calls to atachEnv and getCustomEditor() */
    private String description;
    /** A property stored between calls to atachEnv and getCustomEditor()
     * It can be 'TreeView' or 'ListView'. Default is 'ListView'.
     */
    private String guiType;
    /** A property stored between calls to atachEnv and getCustomEditor()
     * Valid only for 'ListView' GUI type. It controls selection mode of 
     * JFileChooser.
     *
     * Valid values are:
     * JFileChooser.FILES_ONLY
     * JFileChooser.DIRECTORIES_ONLY
     * JFileChooser.FILES_AND_DIRECTORIES
     */
    private Integer selectionMode;
    
    private PropertyChangeSupport supp = new PropertyChangeSupport(this);
    
    private PropertyEnv env;

    /**
     * This method is called by the IDE to pass
     * the environment to the property editor.
     */
    public void attachEnv(PropertyEnv env) {
        this.env = env;
        Object newObj = env.getFeatureDescriptor().getValue(PROPERTY_CURRENT_FOLDER);
        if (newObj instanceof DataObject) {
            currentFolder = (DataObject)newObj;
        }
        newObj = env.getFeatureDescriptor().getValue(PROPERTY_ROOT_FOLDER);
        if (newObj instanceof DataFolder) {
            rootFolder = (DataFolder)newObj;
        }
        newObj = env.getFeatureDescriptor().getValue(PROPERTY_ROOT_NODE);
        if (newObj instanceof Node) {
            rootNode = (Node)newObj;
        }
        newObj = env.getFeatureDescriptor().getValue(PROPERTY_COOKIES);
        if (newObj instanceof Class[]) {
            cookies = (Class[])newObj;
        }
        newObj = env.getFeatureDescriptor().getValue(PROPERTY_DATA_FILTER);
        if (newObj instanceof DataFilter) {
            dataFilter = (DataFilter)newObj;
        }
        newObj = env.getFeatureDescriptor().getValue(PROPERTY_FOLDER_FILTER);
        if (newObj instanceof DataFilter) {
            folderFilter = (DataFilter)newObj;
        }
        newObj = env.getFeatureDescriptor().getValue(PROPERTY_NODE_ACCEPTOR);
        if (newObj instanceof NodeAcceptor) {
            nodeAcceptor = (NodeAcceptor)newObj;
        }
        newObj = env.getFeatureDescriptor().getValue(PROPERTY_LABEL);
        if (newObj instanceof String) {
            label = (String)newObj;
        }
        newObj = env.getFeatureDescriptor().getValue(PROPERTY_TITLE);
        if (newObj instanceof String) {
            title = (String)newObj;
        }
        newObj = env.getFeatureDescriptor().getValue(PROPERTY_INSET);
        if (newObj instanceof Integer) {
            insets = (Integer)newObj;
        }
        newObj = env.getFeatureDescriptor().getValue(PROPERTY_DESCRIPTION);
        if (newObj instanceof String) {
            description = (String)newObj;
        }
        newObj = env.getFeatureDescriptor().getValue(PROPERTY_GUI_TYPE);
        if (newObj instanceof String) {
            guiType = (String)newObj;
        }
        newObj = env.getFeatureDescriptor().getValue(PROPERTY_SELECTION_MODE);
        if (newObj instanceof Integer) {
            selectionMode = (Integer)newObj;
        }
        // fix of 19318
        env.getFeatureDescriptor().setValue( "canEditAsText", Boolean.FALSE );
    }
    
    /**
     * Calls lazy initialization in getDataObjectpanel().
     * @return an instanceof DataObjectPanel
     */
    public java.awt.Component getCustomEditor() {
        return getDataObjectPanel();
    }
    
    void setOkButtonEnabled(boolean state) {
        if (env != null) {
            env.setState(state ? PropertyEnv.STATE_VALID:PropertyEnv.STATE_INVALID);
        }
    }
    
    /**
     * Lazy initializes customEditor (DataObjectPanel).
     * Passes all parameters gathered in method attachEnv.
     */
    private DataObjectPanel getDataObjectPanel() {
        if (customEditor == null) {
            // lazy init ...
            if (guiType != null) {
                if ("TreeView".equals(guiType)) {
                    customEditor = new DataObjectTreeView(this);
                } else if ("ListView".equals(guiType)) {
                    customEditor = new DataObjectListView(this);
                } else {
                    customEditor = new DataObjectListView(this);
                }
            } else {
                customEditor = new DataObjectListView(this);
            }
        }
        if (cookies != null) {
            customEditor.setDataFilter(new CookieFilter(cookies, dataFilter));
        } else {
            customEditor.setDataFilter(dataFilter);
        }
        Object value = getValue();
        if ( value != null && value instanceof DataObject) {
            customEditor.setDataObject( (DataObject)value );
        }
        else if (currentFolder != null) {
            customEditor.setDataObject(currentFolder);
        }
        if (label != null) {
            customEditor.setText(label);
        }
        if (title != null) {
            customEditor.putClientProperty("title", title); // NOI18N
        }
        if (nodeAcceptor != null) {
            customEditor.setNodeFilter(nodeAcceptor);
        }
        if (folderFilter != null) {
            customEditor.setFolderFilter(folderFilter);
        }
        if (rootFolder != null) {
            customEditor.setRootObject(rootFolder);
        }
        if (rootNode != null) {
            customEditor.setRootNode(rootNode);
        }
        if (insets != null) {
            customEditor.setInsetValue(insets.intValue());
        }
        if (description != null) {
            customEditor.setDescription(description);
        }
        if (selectionMode != null) {
            customEditor.setSelectionMode(selectionMode.intValue());
        }
        customEditor.setMultiSelection(false);
        return customEditor;
    }
    
    /**
     * Determines whether the propertyEditor can provide a custom editor.
     * @return  true.
     */
    public boolean supportsCustomEditor() {
        return true;
    }

    /** Adds the listener also to private support supp.*/
     public void addPropertyChangeListener(PropertyChangeListener l) {
         super.addPropertyChangeListener(l);
         supp.addPropertyChangeListener(l);
     }

    /** Removes the listener also from private support supp.*/
     public void removePropertyChangeListener(PropertyChangeListener l) {
         super.removePropertyChangeListener(l);
         supp.removePropertyChangeListener(l);
     }
    
    public String getAsText() {
        Object value = getValue();
        if (value instanceof DataObject) {
            return ((DataObject)value).getNodeDelegate().getDisplayName();
        }
        return "";
    }

    public void setAsText(String text) throws java.lang.IllegalArgumentException {
        if ((text==null)||(text.equals(""))) setValue(null);
    }

    /** CookieFilter allows you to filter DataObjects
     * based on presence of specified cookies.
     */
    private static class CookieFilter implements DataFilter {
        private Class[] cookieArray;
        private DataFilter originalFilter;

        /** Just remember the cookie array and original filter.*/
        public CookieFilter(Class[] cookieArray, DataFilter originalFilter) {
            this.cookieArray = cookieArray;
            this.originalFilter = originalFilter;
        }
        /** Should the data object be displayed or not? This implementation
         * combines the originalFilter with set of cookies supplied
         * in cookieArray.
         * @param obj the data object
         * @return <CODE>true</CODE> if the object should be displayed,
         *    <CODE>false</CODE> otherwise
         */
        public boolean acceptDataObject (DataObject obj) {
            if (cookieArray == null) {
                if (originalFilter != null) {
                    return originalFilter.acceptDataObject(obj);
                } else {
                    return true;
                }
            }
            for (int i = 0; i < cookieArray.length; i++) {
                if (obj.getCookie(cookieArray[i]) == null) {
                    return false;
                }
            }
            if (originalFilter != null) {
                return originalFilter.acceptDataObject(obj);
            } else {
                return true;
            }
        }
    }
}
