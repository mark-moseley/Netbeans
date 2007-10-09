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

package org.netbeans.modules.profiler.ppoints;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.profiler.ppoints.ui.TakeSnapshotCustomizer;
import org.netbeans.modules.profiler.ppoints.ui.TriggeredTakeSnapshotCustomizer;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import java.text.MessageFormat;
import java.util.Properties;
import javax.swing.Icon;
import javax.swing.ImageIcon;


/**
 *
 * @author Jiri Sedlacek
 */
public class TriggeredTakeSnapshotProfilingPointFactory extends CodeProfilingPointFactory {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // -----
    // I18N String constants
    private static final String PP_TYPE = NbBundle.getMessage(TriggeredTakeSnapshotProfilingPointFactory.class,
                                                              "TriggeredTakeSnapshotProfilingPointFactory_PpType"); // NOI18N
    private static final String PP_DESCR = NbBundle.getMessage(TriggeredTakeSnapshotProfilingPointFactory.class,
                                                               "TriggeredTakeSnapshotProfilingPointFactory_PpDescr"); // NOI18N
    private static final String PP_DEFAULT_NAME = NbBundle.getMessage(TriggeredTakeSnapshotProfilingPointFactory.class,
                                                                      "TriggeredTakeSnapshotProfilingPointFactory_PpDefaultName"); // NOI18N
                                                                                                                                   // -----
    public static final Icon TAKE_SNAPSHOT_PP_ICON = new ImageIcon(Utilities.loadImage("org/netbeans/modules/profiler/ppoints/ui/resources/triggeredTakeSnapshotProfilingPoint.png")); // NOI18N
    public static final String TAKE_SNAPSHOT_PP_TYPE = PP_TYPE;
    public static final String TAKE_SNAPSHOT_PP_DESCR = PP_DESCR;
    private static TriggeredTakeSnapshotProfilingPointFactory defaultInstance;

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public static TriggeredTakeSnapshotProfilingPointFactory getDefault() {
        if (defaultInstance == null) {
            defaultInstance = new TriggeredTakeSnapshotProfilingPointFactory();
        }

        return defaultInstance;
    }

    public String getDescription() {
        return TAKE_SNAPSHOT_PP_DESCR;
    }

    public Icon getIcon() {
        return TAKE_SNAPSHOT_PP_ICON;
    }

    public int getScope() {
        return SCOPE_GLOBAL;
    }

    public String getType() {
        return TAKE_SNAPSHOT_PP_TYPE;
    }

    public TriggeredTakeSnapshotProfilingPoint create(Project project) {
        if (project == null) {
            project = Utils.getCurrentProject(); // project not defined, will be detected from most active Editor or Main Project will be used
        }

        String name = Utils.getUniqueName(getType(),
                                          MessageFormat.format(PP_DEFAULT_NAME,
                                                               new Object[] {
                                                                   "", ProjectUtils.getInformation(project).getDisplayName()
                                                               }), project);

        return new TriggeredTakeSnapshotProfilingPoint(name, project);
    }

    public boolean supportsCPU() {
        return false;
    }

    public boolean supportsMemory() {
        return true;
    }

    public boolean supportsMonitor() {
        return false;
    }

    protected Class getProfilingPointsClass() {
        return TriggeredTakeSnapshotProfilingPoint.class;
    }

    protected String getServerHandlerClassName() {
        throw new UnsupportedOperationException();
    }

    protected TriggeredTakeSnapshotCustomizer createCustomizer() {
        return new TriggeredTakeSnapshotCustomizer(getType(), getIcon());
    }

    protected ProfilingPoint loadProfilingPoint(Project project, Properties properties, int index) {
        String name = properties.getProperty(index + "_" + ProfilingPoint.PROPERTY_NAME, null); // NOI18N
        String enabledStr = properties.getProperty(index + "_" + ProfilingPoint.PROPERTY_ENABLED, null); // NOI18N
        String type = properties.getProperty(index + "_" + TriggeredTakeSnapshotProfilingPoint.PROPERTY_TYPE, null); // NOI18N
        String target = properties.getProperty(index + "_" + TriggeredTakeSnapshotProfilingPoint.PROPERTY_TARGET, null); // NOI18N
        String file = properties.getProperty(index + "_" + TriggeredTakeSnapshotProfilingPoint.PROPERTY_CUSTOM_FILE, null); // NOI18N
        String resetResultsStr = properties.getProperty(index + "_" + TriggeredTakeSnapshotProfilingPoint.PROPERTY_RESET_RESULTS,
                                                        null); // NOI18N
        TriggeredGlobalProfilingPoint.TriggerCondition condition = TriggeredGlobalProfilingPoint.TriggerCondition.load(project,
                                                                                                                       index,
                                                                                                                       properties);

        if ((name == null) || (enabledStr == null) || (condition == null) || (type == null) || (target == null) || (file == null)
                || (resetResultsStr == null)) {
            return null;
        }

        TriggeredTakeSnapshotProfilingPoint profilingPoint = null;

        try {
            profilingPoint = new TriggeredTakeSnapshotProfilingPoint(name, project);
            profilingPoint.setEnabled(Boolean.parseBoolean(enabledStr));
            profilingPoint.setSnapshotType(type);
            profilingPoint.setSnapshotTarget(target);
            profilingPoint.setSnapshotFile(file);
            profilingPoint.setResetResults(Boolean.parseBoolean(resetResultsStr));
            profilingPoint.setCondition(condition);
        } catch (Exception e) {
            ErrorManager.getDefault().log(ErrorManager.ERROR, e.getMessage());
        }

        ;

        return profilingPoint;
    }

    protected void storeProfilingPoint(ProfilingPoint profilingPoint, int index, Properties properties) {
        TriggeredTakeSnapshotProfilingPoint takeSnapshot = (TriggeredTakeSnapshotProfilingPoint) profilingPoint;
        properties.put(index + "_" + ProfilingPoint.PROPERTY_NAME, takeSnapshot.getName()); // NOI18N
        properties.put(index + "_" + ProfilingPoint.PROPERTY_ENABLED, Boolean.toString(takeSnapshot.isEnabled())); // NOI18N
        properties.put(index + "_" + TriggeredTakeSnapshotProfilingPoint.PROPERTY_TYPE, takeSnapshot.getSnapshotType()); // NOI18N
        properties.put(index + "_" + TriggeredTakeSnapshotProfilingPoint.PROPERTY_TARGET, takeSnapshot.getSnapshotTarget()); // NOI18N
        properties.put(index + "_" + TriggeredTakeSnapshotProfilingPoint.PROPERTY_CUSTOM_FILE,
                       (takeSnapshot.getSnapshotFile() == null) ? "" : takeSnapshot.getSnapshotFile()); // NOI18N
        properties.put(index + "_" + TriggeredTakeSnapshotProfilingPoint.PROPERTY_RESET_RESULTS,
                       Boolean.toString(takeSnapshot.getResetResults())); // NOI18N
        takeSnapshot.getCondition().store(takeSnapshot.getProject(), index, properties);
    }
}
