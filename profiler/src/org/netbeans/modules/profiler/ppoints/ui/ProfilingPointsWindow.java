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

package org.netbeans.modules.profiler.ppoints.ui;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import java.awt.BorderLayout;


/**
 * Top class of the Profiling points view.
 *
 * @author Maros Sandor
 */
public class ProfilingPointsWindow extends TopComponent {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String COMPONENT_NAME = NbBundle.getMessage(ProfilingPointsWindow.class,
                                                                     "ProfilingPointsWindow_ComponentName"); // NOI18N
    private static final String COMPONENT_ACCESS_DESCR = NbBundle.getMessage(ProfilingPointsWindow.class,
                                                                             "ProfilingPointsWindow_ComponentAccessDescr"); // NOI18N
                                                                                                                            // -----
    private static final String HELP_CTX_KEY = "ProfilingPointsWindow.HelpCtx"; // NOI18N
    private static final HelpCtx HELP_CTX = new HelpCtx(HELP_CTX_KEY);
    private static final long serialVersionUID = 1L;
    private static final String PREFERRED_ID = "PROFILINGPOINTSWINDOW_TC"; // NOI18N // for winsys persistence
    private static ProfilingPointsWindow defaultInstance;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private ProfilingPointsWindowUI ui;
    private boolean needsNewDock = false; // TODO: fix according to ProfilerControlPanel2

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ProfilingPointsWindow() {
        setName(COMPONENT_NAME);
        setIcon(org.openide.util.Utilities.loadImage("org/netbeans/modules/profiler/ppoints/ui/resources/ppoint.png", true)); // NOI18N
        setLayout(new BorderLayout());
        getAccessibleContext().setAccessibleDescription(COMPONENT_ACCESS_DESCR);
        ui = new ProfilingPointsWindowUI();
        add(ui, BorderLayout.CENTER);
        setFocusable(true);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public HelpCtx getHelpCtx() {
        return HELP_CTX;
    }

    public static synchronized ProfilingPointsWindow getInstance() {
        // TODO: fix according to ProfilerControlPanel2
        if (defaultInstance == null) {
            final TopComponent tc = WindowManager.getDefault().findTopComponent(PREFERRED_ID);

            if ((tc != null) && tc instanceof ProfilingPointsWindow) {
                defaultInstance = (ProfilingPointsWindow) tc;
                defaultInstance.needsNewDock = false;
            } else {
                defaultInstance = new ProfilingPointsWindow();
                defaultInstance.needsNewDock = true;
            }
        }

        return defaultInstance;
    }

    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    public void notifyProfilingStateChanged() {
        ui.notifyProfilingStateChanged();
    }

    public void open() {
        // TODO: fix according to ProfilerControlPanel2
        if (needsNewDock) {
            final Mode myMode = WindowManager.getDefault().findMode("output"); //NOI18N

            if (myMode != null) {
                myMode.dockInto(this);
            }

            needsNewDock = false;
        }

        super.open();
    }

    protected String preferredID() {
        return PREFERRED_ID;
    }
}
