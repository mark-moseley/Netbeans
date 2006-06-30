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

package org.netbeans.modules.debugger.jpda.ui.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.spi.debugger.ActionsProviderSupport;


/**
* Representation of a debugging session.
*
* @author   Jan Jancura
* @author  Marian Petras
*/
abstract class JPDADebuggerAction extends ActionsProviderSupport implements
PropertyChangeListener {

    private JPDADebugger debugger;

    JPDADebuggerAction (JPDADebugger debugger) {
        this.debugger = debugger;
        debugger.addPropertyChangeListener (debugger.PROP_STATE, this);
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        try {
            checkEnabled (debugger.getState ());
        } catch (com.sun.jdi.VMDisconnectedException e) {
            // Causes kill action when something is being evaluated
        }
    }
    
    protected abstract void checkEnabled (int debuggerState);
    
    JPDADebugger getDebuggerImpl () {
        return debugger;
    }
}
