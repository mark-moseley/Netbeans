/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.db.explorer;

/**
 * Listener for changes to the list of drivers maintained by JDBCDriverManager.
 *
 * @see JDBCDriverManager
 *
 * @author Andrei Badea
 */
public interface JDBCDriverListener {
    
    /**
     * Called when the list of drivers has changed.
     */
    public void driversChanged();
}
