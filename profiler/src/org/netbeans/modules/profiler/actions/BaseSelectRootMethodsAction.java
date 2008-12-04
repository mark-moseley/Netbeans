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

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.lib.profiler.client.ClientUtils;
import org.netbeans.lib.profiler.common.ProfilingSettings;
import org.netbeans.modules.profiler.NetBeansProfiler;
import org.netbeans.modules.profiler.ui.NBSwingWorker;
import org.netbeans.modules.profiler.ui.stp.ProfilingSettingsManager;
import org.netbeans.modules.profiler.utils.IDEUtils;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.profiler.ui.panels.ClassSelectRootMethodsPanel;
import org.openide.filesystems.FileObject;


/**
 * Base class for actions providing functionality to select a profiling root
 * method from the currently opened source file
 *
 * @author Ian Formanek
 * @author Jiri Sedlacek
 */
abstract public class BaseSelectRootMethodsAction extends NodeAction {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String NO_CLASS_FOUND_MSG = NbBundle.getMessage(BaseSelectRootMethodsAction.class,
                                                                         "SelectRootMethodsAction_NoClassFoundMsg"); // NOI18N
                                                                                                                     // -----

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public BaseSelectRootMethodsAction() {
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public String getName() {
        return NbBundle.getMessage(BaseSelectRootMethodsAction.class, "LBL_SelectRootMethodsAction"); //NOI18N
    }

    protected boolean asynchronous() {
        return false;
    } // runs in event thread

    protected boolean enable(final Node[] nodes) {
        return true;
    }

    protected void performAction(final Node[] nodes) {
        // No nodes, shouldn't happen when invoked from Editor
        if (nodes.length == 0) {
            return;
        }

        // Get data object
        final DataObject dobj = (DataObject) nodes[0].getLookup().lookup(DataObject.class);

        if (dobj == null) {
            return;
        }

        new NBSwingWorker() {
                String className = null;

                protected void doInBackground() {
                    className = getFileClassName(dobj.getPrimaryFile());
                }

                protected void done() {
                    if (className != null) {
                        Project project = FileOwnerQuery.getOwner(dobj.getPrimaryFile());
                        ProfilingSettings[] projectSettings = ProfilingSettingsManager.getDefault().getProfilingSettings(project)
                                                                                      .getProfilingSettings();
                        List<ProfilingSettings> cpuSettings = new ArrayList();

                        for (ProfilingSettings settings : projectSettings) {
                            if (org.netbeans.modules.profiler.ui.stp.Utils.isCPUSettings(settings.getProfilingType())) {
                                cpuSettings.add(settings);
                            }
                        }

                        ProfilingSettings settings = IDEUtils.selectSettings(project, ProfilingSettings.PROFILE_CPU_PART,
                                                                             cpuSettings.toArray(new ProfilingSettings[cpuSettings
                                                                                                                       .size()]),
                                                                             null);

                        if (settings == null) {
                            return; // cancelled by the user
                        }

                        ClientUtils.SourceCodeSelection[] rootMethodsSelection = ClassSelectRootMethodsPanel.getDefault()
                                                                                                               .getRootMethods(project,
                                                                                                                               dobj.getPrimaryFile(),
                                                                                                                               settings
                                                                                                                               .getInstrumentationRootMethods());

                        if (rootMethodsSelection == null) {
                            return;
                        }

                        settings.addRootMethods(rootMethodsSelection);

                        if (cpuSettings.contains(settings)) {
                            ProfilingSettingsManager.getDefault().storeProfilingSettings(projectSettings, settings, project);
                        } else {
                            ProfilingSettings[] newProjectSettings = new ProfilingSettings[projectSettings.length + 1];
                            System.arraycopy(projectSettings, 0, newProjectSettings, 0, projectSettings.length);
                            newProjectSettings[projectSettings.length] = settings;
                            ProfilingSettingsManager.getDefault().storeProfilingSettings(newProjectSettings, settings, project);
                        }
                    } else {
                        NetBeansProfiler.getDefaultNB().displayError(NO_CLASS_FOUND_MSG);
                    }
                }
            }.execute();
    }

    abstract protected String getFileClassName(FileObject file);
}
