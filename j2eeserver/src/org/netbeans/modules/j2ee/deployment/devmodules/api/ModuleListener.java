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

package org.netbeans.modules.j2ee.deployment.devmodules.api;

/** Notifies about changes in the set of modules contained in the application. 
 */
public interface ModuleListener {
    /** Module was added to the module container */
    public abstract void addModule(J2eeModule module);
    
    /** Module was removed from the module container */
    public abstract void removeModule(J2eeModule module);
}
