/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.tax;

import java.beans.PropertyChangeListener;

import org.netbeans.tax.event.TreeEventManager;
import org.netbeans.tax.event.TreeEvent;

/**
 * Tree node adds notion of owner document.
 *
 * @author  Libor Kramolis
 * @version 0.1
 */
public abstract class TreeNode extends TreeObject {
    
    /** */
    public static final String PROP_NODE = "this"; // NOI18N
    
    
    //
    // init
    //
    
    /** Creates new TreeNode. */
    protected TreeNode () {
    }
    
    
    /**
     * Creates new TreeNode - copy constructor.
     */
    protected TreeNode (TreeNode node) {
        super (node);
    }
    
    
    //
    // itself
    //
    
    /**
     * Traverse to the owner document and return it.
     */
    public abstract TreeDocumentRoot getOwnerDocument ();
    
    
    //
    // Event support
    //
    
    /** Get assigned event manager assigned to ownerDocument.
     * If this node does not have its one, it returns null;
     * @return assigned event manager (may be null).
     */
    public final TreeEventManager getEventManager () {
        if ( getOwnerDocument () == null ) {
            return null;
        }
        return getOwnerDocument ().getRootEventManager ();
    }
    
}
