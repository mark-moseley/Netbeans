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

import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.common.Profiler;
import org.netbeans.lib.profiler.ui.components.HTMLTextArea;
import org.netbeans.modules.profiler.NetBeansProfiler;
import org.netbeans.modules.profiler.ui.ProfilerDialogs;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import javax.swing.*;
import org.netbeans.lib.profiler.common.event.ProfilingStateEvent;
import org.netbeans.lib.profiler.common.event.ProfilingStateListener;


/**
 * Provisionary action to display internal profiler stats.
 *
 * @author Ian Formanek
 */
public final class InternalStatsAction extends AbstractAction implements ProfilingStateListener {
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public InternalStatsAction() {
        putValue(Action.NAME, NbBundle.getMessage(InternalStatsAction.class, "LBL_InternalStatsAction" // NOI18N
        ));
        putValue(Action.SHORT_DESCRIPTION, NbBundle.getMessage(InternalStatsAction.class, "HINT_InternalStatsAction" // NOI18N
        ));
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
        
        updateEnabledState();
        Profiler.getDefault().addProfilingStateListener(this);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    /**
     * Invoked when an action occurs.
     */
    public void actionPerformed(final ActionEvent evt) {
        String stats;

        try {
            stats = Profiler.getDefault().getTargetAppRunner().getInternalStats();

            final HTMLTextArea textArea = new HTMLTextArea(stats);
            textArea.getAccessibleContext()
                    .setAccessibleName(NbBundle.getMessage(InternalStatsAction.class, "CAPTION_InternalStatisticsInstrHotswap")); // NOI18N

            final JPanel p = new JPanel();
            p.setLayout(new BorderLayout());
            p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            p.add(new JScrollPane(textArea), BorderLayout.CENTER);

            ProfilerDialogs.createDialog(new DialogDescriptor(p,
                                                              NbBundle.getMessage(InternalStatsAction.class,
                                                                                  "CAPTION_InternalStatisticsInstrHotswap"), // NOI18N
                                                              true, new Object[] { DialogDescriptor.CLOSED_OPTION },
                                                              DialogDescriptor.CLOSED_OPTION, DialogDescriptor.BOTTOM_ALIGN,
                                                              null, null)).setVisible(true);
        } catch (ClientUtils.TargetAppOrVMTerminated e) {
            Profiler.getDefault()
                    .displayWarning(MessageFormat.format(NbBundle.getMessage(InternalStatsAction.class, "MSG_NotAvailableNow"),
                                                         new Object[] { e.getMessage() })); // NOI18N
        }
    }
    
    public void profilingStateChanged(final ProfilingStateEvent e) {
        updateEnabledState();
    }
    
    public void threadsMonitoringChanged() {} // ignore
    public void instrumentationChanged(final int oldInstrType, final int currentInstrType) {} // ignore
    
    private void updateEnabledState() {
        setEnabled(Profiler.getDefault().getProfilingState() == Profiler.PROFILING_RUNNING);
    }
}
