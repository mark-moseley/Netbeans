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

package org.netbeans.modules.debugger.importd2;

import org.netbeans.modules.debugger.*;
import org.openide.modules.ModuleInstall;

/**
* Module installation class for ImportDebugger Module
*
* @author Jan Jancura
*/
public class ImportDebuggerModule extends ModuleInstall {

    static final long serialVersionUID = -2272025566936120988L;

    private static ImportDebuggerImpl idi;
    
    /** Module installed for the first time. */
    public void installed () {
        restored ();
    }

    /** Module installed again. */
    public void restored () {
        try {
            Register.registerDebuggerImpl (
                idi = new ImportDebuggerImpl ()
            );
        } catch (Exception e) {
        }
    }

    /** Module was uninstalled. */
    public void uninstalled () {
        try {
            Register.unregisterDebuggerImpl (
                idi
            );
            idi = null;
        } catch (RuntimeException e) {
        }
    }
    
    /**
     * Return type of debugger which should be used to debug this DebuggerInfo.
     *
     * @return type of debugger which should be used to debug this DebuggerInfo
     */
    public static DebuggerImpl getDebuggerImpl () {
        return idi;
    }
}
