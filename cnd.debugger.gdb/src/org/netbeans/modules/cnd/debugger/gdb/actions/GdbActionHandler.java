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

import java.util.ArrayList;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;

import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.CustomProjectActionHandler;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.profiles.GdbProfile;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

public class GdbActionHandler implements CustomProjectActionHandler {
    
    private ArrayList<ExecutionListener> listeners = new ArrayList<ExecutionListener>();
    
    public void execute(final ProjectActionEvent ev, final InputOutput io) {
        GdbProfile profile = (GdbProfile) ev.getConfiguration().getAuxObject(GdbProfile.GDB_PROFILE_ID);
        if (profile != null) { // profile can be null if dbxgui is enabled
            String gdb = profile.getGdbPath((MakeConfiguration)ev.getConfiguration());
            if (gdb != null) {
                final GdbActionHandler gah = this;
                executionStarted();
                Runnable loadProgram = new Runnable() {
                    public void run() {
                        if (ev.getID() == ProjectActionEvent.DEBUG) {
                            DebuggerManager.getDebuggerManager().startDebugging(
                                    DebuggerInfo.create(GdbDebugger.SESSION_PROVIDER_ID,
                                    new Object[] {ev, io, gah}));
                        } else if (ev.getID() == ProjectActionEvent.DEBUG_STEPINTO) {
                            DebuggerManager.getDebuggerManager().startDebugging(
                                    DebuggerInfo.create(GdbDebugger.SESSION_PROVIDER_ID,
                                    new Object[] {ev, io, gah}));
                        }
                    }
                };
                SwingUtilities.invokeLater(loadProgram);
            } else {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    NbBundle.getMessage(GdbActionHandler.class, "Err_NoGdbFound"))); // NOI18N

            }
        }
    }

    public void addExecutionListener(ExecutionListener l) {
        listeners.add(l);
    }

    public void removeExecutionListener(ExecutionListener l) {
        listeners.remove(listeners.indexOf(l));
    }
    
    public void executionStarted() {
        for (int i = 0; i < listeners.size(); i++) {
            ExecutionListener listener = listeners.get(i);
            listener.executionStarted();
        }
    }
    
    public void executionFinished(int rc) {
        for (int i = 0; i < listeners.size(); i++) {
            ExecutionListener listener = (ExecutionListener) listeners.get(i);
            listener.executionFinished(rc);
        }
    }
}
