/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.cnd.debugger.gdb.actions;

import java.util.Collections;
import java.util.Set;

import org.openide.util.RequestProcessor;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderListener;
import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;


/**
 *
 * @author gordonp
 */
public class StartActionProvider extends ActionsProvider {
    
    private GdbDebugger debuggerImpl;
    private ContextProvider lookupProvider;
    private static final boolean startVerbose =
                System.getProperty("netbeans.debugger.start") != null;

    /**
     * Creates a new instance of StartActionProvider
     */
    public StartActionProvider(ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        debuggerImpl = (GdbDebugger) lookupProvider.lookupFirst(null, GdbDebugger.class);
        
    }
    
    /**
     * Returns set of actions supported by this ActionsProvider.
     *
     * @return set of actions supported by this ActionsProvider
     */
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_START);
    }
    
    /**
     * Called when the action is called (action button is pressed).
     *
     * @param action an action which has been called
     */
    public void doAction(Object action) {
        
        if (action instanceof String) {
            debuggerImpl.startDebugger();
        }
    }
    
    /**
     * Should return a state of given action.
     *
     * @param action action
     */
    public boolean isEnabled(Object action) {
        return true;
    }
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    public void addActionsProviderListener(ActionsProviderListener l) {
    }
    

    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    public void removeActionsProviderListener(ActionsProviderListener l) {
    }
    
    /**
     * Post the action and let it process asynchronously.
     * The default implementation just delegates to {@link #doAction}
     * in a separate thread and returns immediately.
     *
     * @param action The action to post
     * @param actionPerformedNotifier run this notifier after the action is
     *        done.
     * @since 1.5
     */
    public void postAction(final Object action, final Runnable actionPerformedNotifier) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    doAction(action);
                } finally {
                    actionPerformedNotifier.run();
                }
            }
        });
    }    
}
