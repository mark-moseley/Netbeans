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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.debugger.jpda.projects;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.project.Project;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.project.ActionProvider;
import org.openide.ErrorManager;
import org.openide.util.WeakListeners;


/**
*
* @author   Jan Jancura
*/
public class StepIntoActionProvider extends ActionsProviderSupport {

//    private MethodBreakpoint breakpoint;
    Listener listener;
    
    {
        listener = new Listener ();
        MainProjectManager.getDefault ().addPropertyChangeListener(
                WeakListeners.propertyChange(listener, MainProjectManager.getDefault()));
        DebuggerManager.getDebuggerManager ().addDebuggerListener(
                WeakListeners.create(DebuggerManagerListener.class, listener, DebuggerManager.getDebuggerManager()));
        
        setEnabled (
            ActionsManager.ACTION_STEP_INTO,
            shouldBeEnabled ()
        );
    }
    
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_STEP_INTO);
    }
    
    public void doAction (final Object action) {
        // start debugging of project
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        invokeAction();
                    }
                });
            } catch (InterruptedException iex) {
                // Procceed
            } catch (java.lang.reflect.InvocationTargetException itex) {
                ErrorManager.getDefault().notify(itex);
            }
        } else {
            invokeAction();
        }
    }
    
    public void postAction(Object action, Runnable actionPerformedNotifier) {
        // start debugging of project
        invokeAction();
        actionPerformedNotifier.run();
    }
    
    private void invokeAction() {
        ((ActionProvider) MainProjectManager.getDefault ().
            getMainProject ().getLookup ().lookup (
                ActionProvider.class
            )).invokeAction (
                ActionProvider.COMMAND_DEBUG_STEP_INTO, 
                MainProjectManager.getDefault ().getMainProject ().getLookup ()
            );
    }
    
    private boolean shouldBeEnabled () {
        
        // check if current project supports this action
        Project p = MainProjectManager.getDefault ().getMainProject ();
        if (p == null) return false;
        ActionProvider actionProvider = (ActionProvider) p.getLookup ().
            lookup (ActionProvider.class);
        if (actionProvider == null) return false;
        String[] sa = actionProvider.getSupportedActions ();
        int i, k = sa.length;
        for (i = 0; i < k; i++)
            if (ActionProvider.COMMAND_DEBUG_STEP_INTO.equals (sa [i]))
                break;
        if (i == k) return false;
        
        if (DebuggerManager.getDebuggerManager().getDebuggerEngines().length > 0) {
            // Do not enable this non-contextual action when some debugging session is already running.
            return false;
        }
        // check if this action should be enabled
        return actionProvider.isActionEnabled (
            ActionProvider.COMMAND_DEBUG_STEP_INTO,
            MainProjectManager.getDefault ().getMainProject ().getLookup ()
        );
    }
    
    
    private class Listener implements PropertyChangeListener, DebuggerManagerListener {
        
        public void propertyChange (PropertyChangeEvent e) {}
        public void sessionRemoved (Session session) {}
        public void breakpointAdded (Breakpoint breakpoint) {}
        public void breakpointRemoved (Breakpoint breakpoint) {}
        public Breakpoint[] initBreakpoints () {
            return new Breakpoint [0];
        }
        public void initWatches () {}
        public void sessionAdded (Session session) {}
        public void watchAdded (Watch watch) {}
        public void watchRemoved (Watch watch) {}
        
        public void engineAdded(DebuggerEngine engine) {
            setEnabled (
                ActionsManager.ACTION_STEP_INTO,
                shouldBeEnabled ()
            );
        }
        public void engineRemoved(DebuggerEngine engine) {
            setEnabled (
                ActionsManager.ACTION_STEP_INTO,
                shouldBeEnabled ()
            );
        }
    }
}
