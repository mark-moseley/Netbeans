/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.beaninfo.editors;

import java.beans.*;
import java.io.File;
import java.text.MessageFormat;
import org.openide.ErrorManager;

import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;

/**
 * 
 * @author  David Strupl
 * @version 
 */
public class DataFolderEditor extends PropertyEditorSupport {

    /** Creates new DataFolderEditor */
    public DataFolderEditor() {
    }

    private DataFolderPanel dfPanel;

    /** This method is intended for use when generating Java code to set
    * the value of the property.  It should return a fragment of Java code
    * that can be used to initialize a variable with the current property
    * value.
    * <p>
    *
    * @return A fragment of Java code representing an initializer for the
    *    current value.
    */
    public String getJavaInitializationString() {
        // TODO: corect this
        return "???"; // NOI18N
    }

    /**
    * @return The property value as a human editable string.
    * <p>   Returns null if the value can't be expressed as an editable string.
    * <p>   If a non-null value is returned, then the PropertyEditor should
    *       be prepared to parse that string back in setAsText().
    */
    public String getAsText() {
        DataFolder df = (DataFolder)getValue ();
        String result;
        if (df == null) {
            result = getString ("LAB_DefaultDataFolder"); //NOI18N
        } else {
            result = df.getName();
        }
        if (result.length() ==0) {
            result = File.pathSeparator;
        }
        return result;
    }

    /** Set the property value by parsing a given String.  May raise
    * java.lang.IllegalArgumentException if either the String is
    * badly formatted or if this kind of property can't be expressed
    * as text.
    * @param text  The string to be parsed.
    */
    public void setAsText(String text) {
        if (text==null || 
            "".equals(text) || 
            text.equals(getString ("LAB_DefaultDataFolder")) || 
            File.pathSeparator.equals(text)) {
            //XXX Mysterious why a real implementation of setAsText is not here
            setValue(null);
        } else {
            //Reasonable to assume any exceptions from core/jdk editors are legit
            IllegalArgumentException iae = new IllegalArgumentException ();
            String msg = MessageFormat.format(
                NbBundle.getMessage(
                    DataFolderEditor.class, "FMT_DF_UNKNOWN"), new Object[] {text}); //NOI18N
            ErrorManager.getDefault().annotate(iae, ErrorManager.USER, iae.getMessage(), 
                msg, null, new java.util.Date());
            throw iae;
        }        
    }

    public boolean supportsCustomEditor () {
        return true;
    }

    public java.awt.Component getCustomEditor () {
        dfPanel = getDFPanel();
        Object val = getValue();
        if (val instanceof DataFolder) {
            dfPanel.setTargetFolder((DataFolder)val);
        }
        return dfPanel;
    }

    /** Calls super.setValue(newValue) and then updates
     * the look of the associated DataFolderPanel by
     * providing appropriate node to display.
     */
    public void setValue(Object newValue) {
        Object oldValue = getValue();
        super.setValue(newValue);
        DataFolderPanel dfp = getDFPanel();
        if ((newValue != oldValue)&&(dfp != null) && (newValue instanceof DataFolder)){
            dfp.setTargetFolder((DataFolder)newValue);
        }

    }

    /** This method is called from DataFolderPanel, so it is similar to
     * setValue but does not call DataFolderPanel.setTargetFolder()
     */
    void setDataFolder(DataFolder newDf) {
        super.setValue(newDf);
    }
    
    public DataFolderPanel getDFPanel() {
        if (dfPanel == null) {
            dfPanel = new DataFolderPanel(this);
        }
        return dfPanel;
    }

    private static String getString (String s) {
        return org.openide.util.NbBundle.getBundle (DataFolderEditor.class).getString (s);
    }
}
