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

package org.netbeans.spi.debugger.ui;

import java.beans.PropertyChangeListener;


/**
 * Support for validation of various customizers. This interface can be 
 * optionally implemented by some customizers like Attach Panel (see 
 * {@link AttachType#getCustomizer}) and breakpoint customizer (see
 * {@link BreakpointType#getCustomizer}).
 *
 * @author   Jan Jancura
 */
public interface Controller {
    
    /** Property name constant for valid property. */
    public static final String      PROP_VALID = "valid"; // NOI18N

    
    /**
     * Called when "Ok" button is pressed.
     *
     * @return whether customizer can be closed
     */
    public boolean ok ();
    
    /**
     * Called when "Cancel" button is pressed.
     *
     * @return whether customizer can be closed
     */
    public boolean cancel ();
    
    /**
     * Return <code>true</code> whether value of this customizer 
     * is valid (and OK button can be enabled).
     *
     * @return <code>true</code> whether value of this customizer 
     * is valid
     */
    public boolean isValid ();

    /** 
     * Add a listener to property changes.
     *
     * @param l the listener to add
     */
    public abstract void addPropertyChangeListener (PropertyChangeListener l);

    /** 
     * Remove a listener to property changes.
     *
     * @param l the listener to remove
     */
    public abstract void removePropertyChangeListener (PropertyChangeListener l);
}

