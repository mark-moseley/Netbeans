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

package org.netbeans.api.debugger.test.actions;

import java.util.Collections;
import java.util.Set;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.test.TestDICookie;
import org.netbeans.api.debugger.test.TestDebugger;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderListener;


/**
* Provider for the Kill action in the test debugger.
*
* @author Maros Sandor
*/
public class KillActionProvider extends ActionsProvider {

    private ContextProvider lookupProvider;
    private TestDebugger debugger;

    public KillActionProvider (ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        debugger = (TestDebugger) lookupProvider.lookupFirst
            (null, TestDebugger.class);
    }

    public boolean isEnabled(Object action) {
        return true;
    }

    public void addActionsProviderListener(ActionsProviderListener l) {}
    public void removeActionsProviderListener(ActionsProviderListener l) {}

    public Set getActions() {
        return Collections.singleton (ActionsManager.ACTION_KILL);
    }
        
    public void doAction (Object action) {
        debugger.finish();
        DebuggerInfo di = (DebuggerInfo) lookupProvider.lookupFirst
            (null, DebuggerInfo.class);
        TestDICookie tic = (TestDICookie) di.lookupFirst(null, TestDICookie.class);
        tic.addInfo(ActionsManager.ACTION_KILL);
    }
}
