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

package org.netbeans.modules.profiler.actions;

import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.common.event.ProfilingStateEvent;
import org.netbeans.lib.profiler.common.event.ProfilingStateListener;
import org.netbeans.modules.profiler.NetBeansProfiler;
import org.netbeans.modules.profiler.ui.ProfilerDialogs;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import javax.swing.Action;


/**
 * Rerun the profiling using the same settings as last executed one
 *
 * @author Ian Formanek
 */
public final class RerunAction extends CallableSystemAction implements ProfilingStateListener {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private boolean lastState = false;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public RerunAction() {
        putProperty(Action.SHORT_DESCRIPTION, NbBundle.getMessage(RerunAction.class, "HINT_RerunAction")); //NOI18N
        Profiler.getDefault().addProfilingStateListener(this);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public boolean isEnabled() {
        if (!NetBeansProfiler.isInitialized()) {
            return false;
        }

        lastState = Profiler.getDefault().rerunAvailable();

        return lastState;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;

        // If you will provide context help then use:
        // return new HelpCtx(MyAction.class);
    }

    public String getName() {
        return NbBundle.getMessage(RerunAction.class, "LBL_RerunAction"); //NOI18N
    }

    public void instrumentationChanged(final int oldInstrType, final int currentInstrType) {
        // ignore
    }

    public void performAction() {
        final int state = Profiler.getDefault().getProfilingState();
        final int mode = Profiler.getDefault().getProfilingMode();

        if ((state == Profiler.PROFILING_PAUSED) || (state == Profiler.PROFILING_RUNNING)) {
            if (mode == Profiler.MODE_PROFILE) {
                final NotifyDescriptor d = new NotifyDescriptor.Confirmation(NbBundle.getMessage(RerunAction.class,
                                                                                                 "MSG_ReRunOnProfile"), //NOI18N
                                                                             NbBundle.getMessage(RerunAction.class,
                                                                                                 "CAPTION_Question"),
                                                                             NotifyDescriptor.YES_NO_OPTION // NOI18N
                );

                if (ProfilerDialogs.notify(d) != NotifyDescriptor.YES_OPTION) {
                    return;
                }

                Profiler.getDefault().stopApp();
            } else {
                final NotifyDescriptor d = new NotifyDescriptor.Confirmation(NbBundle.getMessage(RerunAction.class,
                                                                                                 "MSG_ReRunOnAttach"), //NOI18N
                                                                             NbBundle.getMessage(RerunAction.class,
                                                                                                 "CAPTION_Question"),
                                                                             NotifyDescriptor.YES_NO_OPTION // NOI18N
                );

                if (ProfilerDialogs.notify(d) != NotifyDescriptor.YES_OPTION) {
                    return;
                }

                Profiler.getDefault().detachFromApp();
            }
        }

        Profiler.getDefault().rerunLastProfiling();
    }

    public void profilingStateChanged(final ProfilingStateEvent e) {
        updateAction();
    }

    public void threadsMonitoringChanged() {
        // ignore
    }

    public void updateAction() {
        if (lastState != Profiler.getDefault().rerunAvailable()) {
            boolean shouldBeEnabled = isEnabled();
            firePropertyChange(PROP_ENABLED, !shouldBeEnabled, shouldBeEnabled);
        }
    }

    protected boolean asynchronous() {
        return false; // run in event queue
    }

    protected String iconResource() {
        return "org/netbeans/modules/profiler/actions/resources/rerun.png"; //NOI18N
    }
}
