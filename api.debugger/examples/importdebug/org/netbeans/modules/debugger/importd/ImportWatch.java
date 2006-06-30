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

package org.netbeans.modules.debugger.importd;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.openide.TopManager;
import org.openide.debugger.Debugger;
import org.openide.debugger.DebuggerException;
import org.openide.text.Line;

import org.netbeans.modules.debugger.*;


/**
* Standart implementation of Watch interface.
* @see org.openide.debugger.Watch
*
* @author   Jan Jancura
* @version  0.18, Feb 23, 1998
*/
public class ImportWatch extends AbstractWatch implements Validator.Object {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 3431277044447811206L;


    // private variables .....................................................

    private String errorMessage = null;
    private String name = "";
    private String type;
    private String value;
    private boolean isHidden;
    private boolean valid = false;
    private ImportDebugger debugger;
    private transient PropertyChangeSupport pcs;
    
    
    // init .....................................................................

    /**
    * Non public constructor called from the JavaDebugger only.
    * User must create watch from Debugger.getNewWatch () method.
    */
    ImportWatch (ImportDebugger debugger, boolean hidden) {
        this.debugger = debugger;
        this.isHidden = hidden;
        init ();
    }

    protected void init () {
        pcs = new PropertyChangeSupport (this);
        debugger.getValidator ().add (this);        
    }

    private void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject ();
        init ();
    }
    

    // Watch implementation ....................................................
    
    
    /** Remove the watch from the list of all watches in the system.
    */
    public void remove () {
        debugger.removeWatch (this);
    }

    /** Get the name of the variable to watch.
    *
    * @return the variable name
    */
    public String getVariableName () {
        return name;
    }

    /** Set the variable name to watch.
    *
    * @param name string name of the variable to watch
    */
    public void setVariableName (String name) {
        this.name = name;
        valid = false;
    }

    /** Get a textual representation of the value.
    * The watch should convert
    * the real value to a string representation. So if the watch represents
    * a <code>null</code> reference, the returned string will be for example <code>"null"</code>.
    *
    * @return the value of this watch, or <code>null</code> if the watch is not in scope
    */
    public String getAsText () {
        if (!valid) refresh ();
        return value;
    }

    /** Set the value of the watched variable (as text).
    *
    * @param value text representation of the new value
    * @exception DebuggerException if the value cannot be changed, or the
    *    string does not represent valid value, or the value type cannot reasonably be set as text
    */
    public void setAsText (String value) throws DebuggerException {
        valid = false;
    }

    /** Get the string representation of the type of the variable.
    *
    * @return type string (i.e. the class name, or for a primitive e.g. <code>"int"</code>)
    */
    public String getType () {
        if (!valid) refresh ();
        return type;
    }

    /** Test whether the watch is hidden.
    * If so, it
    * is not presented in the list of all watches. Such a watch can be used
    * for the IDE's (or some module's) private use, not displaying anything to the user.
    * @return <code>true</code> if the watch is hidden
    * @see Debugger#createWatch(String, boolean)
    */
    public boolean isHidden () {
        return isHidden;
    }

    /**
    * Add a property change listener.
    * Change events should be fired for the properties {@link #PROP_VARIABLE_NAME}, {@link #PROP_AS_TEXT}, and {@link #PROP_TYPE}.
    *
    * @param l the listener to add
    */
    public void addPropertyChangeListener (PropertyChangeListener l) {
        pcs.addPropertyChangeListener (l);
    }

    /**
    * Remove a property change listener.
    *
    * @param l the listener to remove
    */
    public void removePropertyChangeListener (PropertyChangeListener l) {
        pcs.removePropertyChangeListener (l);
    }
    
    
    // AbstractWatch implementation ............................................

    public String getErrorMessage () {
        if (!valid) refresh ();
        return errorMessage;
    }
    
    /**
    * Checks value of this watch and if is valide, and sets it.
    */
    public void validate () {
        valid = false;
        firePropertyChange ();
    }
    
    private void refresh () {
        type = "";
        value = "";
        if ((debugger.getState () == debugger.DEBUGGER_NOT_RUNNING) ||
            (debugger.getState () == debugger.DEBUGGER_STARTING)
           ) {
            errorMessage = ImportDebugger.getLocString ("EXC_No_session");
            firePropertyChange ();
            return;
        }
        if (debugger.getState () != debugger.DEBUGGER_STOPPED) {
            errorMessage = ImportDebugger.getLocString ("CTL_No_context");
            firePropertyChange ();
            return;
        }
        try {
            Line l = debugger.getCurrentLine ();
            if (l == null) {
                errorMessage = ImportDebugger.getLocString ("CTL_No_context");
                firePropertyChange ();
                return;
            }
            errorMessage = null;
            String s = ImportDebugger.getText (l);
            int i = s.indexOf (getVariableName ());
            if (i >= 0) {
                value = s.substring (i);
                type = s.substring (0, i);
            } else
                errorMessage = ImportDebugger.getLocString ("Unknown_variable");
        } catch (Exception e) {
            value = "";
            errorMessage = e.toString ();
        }
    }

    protected void firePropertyChange () {
        pcs.firePropertyChange (null, null, null);
    }
    
    /**
     * If return <code>null</code>, this object can be removed from
     * validator.
     *
     * @return <code>null</code>, if this object can be removed from
     * validator
     */
    public boolean canRemove () {
        return false;
    }
    
}

