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

package org.netbeans.modules.debugger.jpda.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.spi.debugger.ActionsProviderSupport;


/**
* Representation of a debugging session.
*
* @author   Jan Jancura
* @author  Marian Petras
*/
abstract class JPDADebuggerActionProvider extends ActionsProviderSupport 
implements PropertyChangeListener {

    private JPDADebuggerImpl debugger;
    
    JPDADebuggerActionProvider (JPDADebuggerImpl debugger) {
        this.debugger = debugger;
        debugger.addPropertyChangeListener (debugger.PROP_STATE, this);
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        checkEnabled (debugger.getState ());
    }
    
    protected abstract void checkEnabled (int debuggerState);
    
    JPDADebuggerImpl getDebuggerImpl () {
        return debugger;
    }
}
