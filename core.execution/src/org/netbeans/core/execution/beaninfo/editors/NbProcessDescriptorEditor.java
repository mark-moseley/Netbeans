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


package org.netbeans.core.execution.beaninfo.editors;


import java.awt.Component;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyEditor;
import java.io.File;
import org.openide.execution.NbProcessDescriptor;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;


/** 
 * A property editor for <code>NbProcessDescriptor</code>.
 *
 * @author  Ian Formanek
 */
public class NbProcessDescriptorEditor extends Object implements ExPropertyEditor {

    /** <code>NbProcessDescriptor</code> to custmize. */
    NbProcessDescriptor pd;
    /** Property support, helper instance. */
    private PropertyChangeSupport support;

    
    /** Creates property editor. */
    public NbProcessDescriptorEditor() {
        support = new PropertyChangeSupport (this);
    }

    
    /** Gets value. Implements <code>PropertyEditor</code> interface. */
    public Object getValue () {
        return pd;
    }

    /** Sets value. Implements <code>PropertyEditor</code> interface. */
    public void setValue (Object value) {
        pd = (NbProcessDescriptor) value;
        support.firePropertyChange("", null, null); // NOI18N
    }

    /** Gets value as text. Implements <code>PropertyEditor</code> interface. */
    public String getAsText () {
        if ( pd == null )
            return "null";        // NOI18N
        return pd.getProcessName () + " " + pd.getArguments (); // NOI18N
    }

    /** Sets value as text. Implemetns <code>ProepertyEditor</code> interface. */
    public void setAsText(String string) {
        string = string.trim ();

        int indx = string.indexOf(' ');
        
        String prg;
        String args;
        
        // Fix #13186. If the string represents path
        // with directories containing white spaces don't separate them to args.
        if(indx == -1 || new File(string).exists()) {
            prg = string;
            args = ""; // NOI18N
        } else {
            prg = string.substring(0, indx);
            args = string.substring(indx + 1);
        }

        NbProcessDescriptor newPD = null;
        if ( pd == null )
            newPD = new NbProcessDescriptor (
                        prg,
                        args
                    );
        else
            newPD = new NbProcessDescriptor (
                        prg,
                        args,
                        pd.getInfo()
                    );
        
        setValue(newPD);
    }

    /** Gets java initialization string. Implements <code>PropertyEditor</code>
     * interface.
     * @return <code>null</code> */
    public String getJavaInitializationString () {
        return null; // no code generation
    }

    /** Gets tags. Implements <code>PropertyEditor</code> interface. 
     * @return <code>null</code> */
    public String[] getTags () {
        return null;
    }

    /** Indicates wheter this editor paints itself the value. Implements
     * <code>PropertyEditor</code> interface. 
     * @return <code>null</code> */
    public boolean isPaintable () {
        return false;
    }

    /** Dummy implementation of <code>PropertyEditor</code> interface method.
     * @see #isPaintable */
    public void paintValue (Graphics g, Rectangle rectangle) {
    }

    /** Inidicates whether this editor supports custom editing. Implements 
     * <code>PropertyEdtitor</code> interface. 
     * @return <code>true</code> */
    public boolean supportsCustomEditor () {
        return true;
    }

    /** Gets custom editor. Implements <code>PropertyEditor</code> interface.
     * @return <code>NbProcessDescriptorCustomEditor</code> 
     * @see NbProcessDescriptorCustomEditor */
    public Component getCustomEditor () {
        return new NbProcessDescriptorCustomEditor (this);
    }

    /** Adds <code>PropertyChangeListener</code>. Implements 
     * <code>PropertyEditor</code> interface. */
    public void addPropertyChangeListener (PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener (propertyChangeListener);
    }

    /** Removes <code>PropertyChangeListner</code>. Implements 
     * <code>PropertyEditor</code> interface. */
    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener(propertyChangeListener);
    }
    
    /**
     * This method is called by the IDE to pass
     * the environment to the property editor.
     * @param env Environment passed by the ide.
     */
    public void attachEnv(PropertyEnv env) {
    }
}
