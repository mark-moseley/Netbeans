/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.tax.event;

import java.beans.PropertyChangeListener;

/**
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public interface TreeNodeContentEventModel extends TreeEventModel {
    
    /**
     * @param listener The listener to add.
     */
    public void addContentChangeListener (PropertyChangeListener listener);

    /**
     * @param listener The listener to remove.
     */
    public void removeContentChangeListener (PropertyChangeListener listener);

    /**
     */
    public boolean hasContentChangeListeners ();

}
