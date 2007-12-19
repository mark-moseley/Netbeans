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
package org.netbeans.modules.php.dbgp.actions;

import java.util.Collections;
import java.util.Set;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.packets.StepIntoCommand;
import org.netbeans.spi.debugger.ContextProvider;


/**
 * @author ads
 *
 */
public class StepIntoActionProvider extends AbstractActionProvider {

    public StepIntoActionProvider( ContextProvider contextProvider ) {
        super(contextProvider);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.debugger.ActionsProviderSupport#doAction(java.lang.Object)
     */
    @Override
    public void doAction( Object action )
    {
        DebugSession session = getSession();
        if ( session == null ){
            return;
        }
        hideSuspendAnnotations();
        StepIntoCommand command = new StepIntoCommand( session.getTransactionId());
        session.sendCommandLater(command);
    }

    /* (non-Javadoc)
     * @see org.netbeans.spi.debugger.ActionsProvider#getActions()
     */
    @Override
    public Set getActions()
    {
        return Collections.singleton( ActionsManager.ACTION_STEP_INTO );
    }

}
