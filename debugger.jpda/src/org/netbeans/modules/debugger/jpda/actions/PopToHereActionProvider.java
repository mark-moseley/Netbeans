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

import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LookupProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.spi.viewmodel.NoInformationException;

/**
* Representation of a debugging session.
*
* @author   Jan Jancura
*/
public class PopToHereActionProvider extends JPDADebuggerActionProvider {
    
    private LookupProvider lookupProvider;

    
    public PopToHereActionProvider (LookupProvider lookupProvider) {
        super (
            (JPDADebuggerImpl) lookupProvider.lookupFirst 
                (JPDADebugger.class) 
        );
        this.lookupProvider = lookupProvider;
    }
    
    public Set getActions () {
        return Collections.singleton (DebuggerManager.ACTION_POP_TOPMOST_CALL);
    }

    public void doAction (Object action) {
        try {
            JPDAThread t = getDebuggerImpl ().getCurrentThread ();
            t.getCallStack () [0].popFrame ();
        } catch (NoInformationException ex) {
        }
    }
    
    protected void checkEnabled (int debuggerState) {
        if (debuggerState == getDebuggerImpl ().STATE_STOPPED) {
            JPDAThread t = getDebuggerImpl ().getCurrentThread ();
            if (t == null) {
                setEnabled (
                    DebuggerManager.ACTION_POP_TOPMOST_CALL,
                    false
                );
                return;
            }
            setEnabled (
                DebuggerManager.ACTION_POP_TOPMOST_CALL,
                t.getStackDepth () > 1
            );
            return;
        }
        setEnabled (
            DebuggerManager.ACTION_POP_TOPMOST_CALL,
            false
        );
    }
}
