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
package org.netbeans.spi.java.classpath.support;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Iterator;
import org.netbeans.spi.java.classpath.PathResourceImplementation;

/**
 * This class provides a base class for PathResource implementations
 * @since org.netbeans.api.java/1 1.4
 */
public abstract class PathResourceBase implements PathResourceImplementation {

    private ArrayList pListeners;


    /**
     * Adds property change listener.
     * The listener is notified when the roots of the PathResource are changed.
     * @param listener
     */
    public synchronized final void addPropertyChangeListener(PropertyChangeListener listener) {
        if (this.pListeners == null)
            this.pListeners = new ArrayList ();
        this.pListeners.add (listener);
    }

    /**
     * Removes PropertyChangeListener
     * @param listener
     */
    public synchronized final void removePropertyChangeListener(PropertyChangeListener listener) {
        if (this.pListeners == null)
            return;
        this.pListeners.remove (listener);
    }

    /**
     * Fires PropertyChangeEvent
     * @param propName name of property
     * @param oldValue old property value or null
     * @param newValue new property value or null
     */
    protected final void firePropertyChange (String propName, Object oldValue, Object newValue) {
        Iterator it = null;
        synchronized (this) {
            if (this.pListeners == null)
                return;
            it = ((ArrayList)this.pListeners.clone()).iterator();
        }
        PropertyChangeEvent event = new PropertyChangeEvent (this, propName, oldValue, newValue);
        while (it.hasNext()) {
            ((PropertyChangeListener)it.next ()).propertyChange (event);
        }
    }
}
