/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.bpel.debugger.ui.action;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.modules.bpel.debugger.api.Position;
import org.netbeans.modules.bpel.debugger.ui.action.BpelActionsProviderSupport.PositionListener;
import org.netbeans.spi.debugger.ContextProvider;


public class PauseActionProvider extends BpelActionsProviderSupport {

    public PauseActionProvider(ContextProvider lookupProvider) {
        super(lookupProvider, ActionsManager.ACTION_PAUSE);
        getDebugger().addPropertyChangeListener(new PositionListener(this));
    }
    
    public void doAction(Object action) {
        // TODO not yet implemented
        // getDebugger().pause();
    }
    
    protected void positionChanged(Position oldPosition, Position newPosition) {
        // only enabled when no position exists (suspended)
        setEnabled(newPosition == null);
    }
}
