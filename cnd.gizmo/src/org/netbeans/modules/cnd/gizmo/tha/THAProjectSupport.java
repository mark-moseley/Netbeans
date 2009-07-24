/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.gizmo.tha;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.compilers.Tool;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.dlight.perfan.tha.api.THAInstrumentationSupport;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

public final class THAProjectSupport implements PropertyChangeListener {

    private static final String MODIFY_PROJECT_CAPTION = loc("THA_ModifyProjectCaption"); // NOI18N
    private static final String MODIFY_PROJECT_MSG = loc("THA_ModifyProjectMsg"); // NOI18N
    private final static Map<Project, THAProjectSupport> cache = new HashMap<Project, THAProjectSupport>();
    private final Collection<PropertyChangeListener> listeners = new CopyOnWriteArrayList<PropertyChangeListener>();
    private final Project project;

    private THAProjectSupport(Project project) {
        this.project = project;
    }

    public static final synchronized THAProjectSupport getSupportFor(Project project) {
        if (project == null) {
            return null;
        }

        if (cache.containsKey(project)) {
            return cache.get(project);
        }

        if (!isSupported(project)) {
            return null;
        }

        THAProjectSupport support = new THAProjectSupport(project);

        ProjectConfigurationProvider pcp = project.getLookup().lookup(ProjectConfigurationProvider.class);

        if (pcp != null) {
            pcp.addPropertyChangeListener(support);
        }

        cache.put(project, support);

        return support;
    }

    /**
     * Returns true if and only if:
     *   - project is not NULL
     *   - project is NativeProject with SunStudio active toolchain
     *   - SunStudio is up-to-date enough (>= 7.6 (mars))
     *
     * @param project
     * @return
     */
    public boolean canInstrument() {
        MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
        MakeConfiguration mc = mcd.getActiveConfiguration();
        CompilerSet compilerSet = mc.getCompilerSet().getCompilerSet();

        if (!compilerSet.isSunCompiler()) {
            return false;
        }

        THAInstrumentationSupport instrSupport = getInstrumentationSupport();

        if (instrSupport == null || !instrSupport.isSupported()) {
            return false;
        }

        return true;
    }

    public boolean isConfiguredForInstrumentation() {
        THAInstrumentationSupport instrSupport = getInstrumentationSupport();

        if (instrSupport == null || !instrSupport.isSupported()) {
            return false;
        }

        MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
        MakeConfiguration mc = mcd.getActiveConfiguration();

        if (mc.getLinkerConfiguration().getCommandLineConfiguration().getValue().contains(instrSupport.getLinkerOptions())) {
            return true;
        }

        return false;
    }

    public boolean isInstrumented() {
        if (!isSupported(project)) {
            return false;
        }

        // First - check for required options.

        if (!activeCompierIsSunStudio()) {
            return false;
        }

        MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
        MakeConfiguration mc = mcd.getActiveConfiguration();
        CompilerSet compilerSet = mc.getCompilerSet().getCompilerSet();

        Tool ccTool = compilerSet.getTool(Tool.CCCompiler);
        String ccPath = ccTool.getPath();
        String sunstudioBinDir = ccPath.substring(0, ccPath.length() - ccTool.getName().length());

        THAInstrumentationSupport instrSupport = THAInstrumentationSupport.getSupport(mc.getDevelopmentHost().getExecutionEnvironment(), sunstudioBinDir);

        boolean result = false;

        try {
            result = instrSupport.isInstrumented(mc.getAbsoluteOutputValue()).get();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }

        return result;
    }

    public boolean doInstrumentation() {
        NativeProject nativeProject = project.getLookup().lookup(NativeProject.class);

        assert nativeProject != null;

        String projectName = nativeProject.getProjectDisplayName();

        if (!activeCompierIsSunStudio()) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                    "Please re-configure Project " + projectName + " to be compiled with SunStudio!", NotifyDescriptor.INFORMATION_MESSAGE));
            return false;
        }

        String caption = MessageFormat.format(MODIFY_PROJECT_CAPTION, new Object[]{projectName});
        String message = MessageFormat.format(MODIFY_PROJECT_MSG, new Object[]{projectName, "build-before-profiler.xml"}); // NOI18N

        if (DialogDisplayer.getDefault().notify(new NotifyDescriptor(message, caption, NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.INFORMATION_MESSAGE, new Object[]{NotifyDescriptor.OK_OPTION,
                    NotifyDescriptor.CANCEL_OPTION}, NotifyDescriptor.OK_OPTION)) != NotifyDescriptor.OK_OPTION) {
            return false;
        }

        MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
        MakeConfiguration mc = mcd.getActiveConfiguration();

        THAInstrumentationSupport instrSupport = getInstrumentationSupport();

        String linkerOptions = mc.getLinkerConfiguration().getCommandLineConfiguration().getValue();

        if (!linkerOptions.contains(instrSupport.getLinkerOptions())) {
            mc.getLinkerConfiguration().getCommandLineConfiguration().setValue(linkerOptions + " " + instrSupport.getLinkerOptions()); // NOI18N
        }

        if (mc.getCRequired().getValue()) {
            String cOptions = mc.getCCompilerConfiguration().getCommandLineConfiguration().getValue();
            if (!cOptions.contains(instrSupport.getCompilerOptions())) {
                mc.getCCompilerConfiguration().getCommandLineConfiguration().setValue(cOptions + " " + instrSupport.getCompilerOptions()); // NOI18N
            }
        }

        if (mc.getCppRequired().getValue()) {
            String ccOptions = mc.getCCCompilerConfiguration().getCommandLineConfiguration().getValue();
            if (!ccOptions.contains(instrSupport.getCompilerOptions())) {
                mc.getCCCompilerConfiguration().getCommandLineConfiguration().setValue(ccOptions + " " + instrSupport.getCompilerOptions()); // NOI18N
            }
        }

        setModified();

        return true;
    }

    public boolean undoInstrumentation() {
        THAInstrumentationSupport instrSupport = getInstrumentationSupport();

        if (instrSupport == null || !instrSupport.isSupported()) {
            return false;
        }

        boolean changed = false;

        MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
        MakeConfiguration mc = mcd.getActiveConfiguration();

        String linkerOptions = mc.getLinkerConfiguration().getCommandLineConfiguration().getValue();
        String linkerInstrOption = instrSupport.getLinkerOptions();
        int idx = linkerOptions.indexOf(linkerInstrOption);

        if (idx >= 0) {
            mc.getLinkerConfiguration().getCommandLineConfiguration().setValue(linkerOptions.replaceAll(linkerInstrOption, "")); // NOI18N
            changed = true;
        }

        if (mc.getCRequired().getValue()) {
            String cOptions = mc.getCCompilerConfiguration().getCommandLineConfiguration().getValue();
            String cInstrOption = instrSupport.getCompilerOptions();
            idx = cOptions.indexOf(cInstrOption);
            if (idx >= 0) {
                mc.getCCompilerConfiguration().getCommandLineConfiguration().setValue(cOptions.replaceAll(cInstrOption, "")); // NOI18N
                changed = true;
            }
        }

        if (mc.getCppRequired().getValue()) {
            String ccOptions = mc.getCCompilerConfiguration().getCommandLineConfiguration().getValue();
            String ccInstrOption = instrSupport.getCompilerOptions();
            idx = ccOptions.indexOf(ccInstrOption);
            if (idx >= 0) {
                mc.getCCCompilerConfiguration().getCommandLineConfiguration().setValue(ccOptions.replaceAll(ccInstrOption, "")); // NOI18N
                changed = true;
            }
        }

        if (changed) {
            setModified();
        }

        return changed;
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(THAProjectSupport.class, key, params);
    }

    private boolean activeCompierIsSunStudio() {
        boolean result = false;
        try {
            MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
            MakeConfiguration mc = mcd.getActiveConfiguration();
            CompilerSet compilerSet = mc.getCompilerSet().getCompilerSet();
            result = compilerSet.isSunCompiler();
        } catch (Throwable th) {
        }
        return result;
    }

    public static boolean isSupported(Project project) {
        if (project == null) {
            return false;
        }

        NativeProject nativeProject = project.getLookup().lookup(NativeProject.class);

        if (nativeProject == null) {
            return false;
        }

        return true;
    }

    public void addProjectConfigurationChangedListener(final PropertyChangeListener listener) {
        listeners.add(listener);
    }

    public void removeProjectConfigurationChangedListener(final PropertyChangeListener listener) {
        listeners.remove(listener);
    }

    private THAInstrumentationSupport getInstrumentationSupport() {
        if (!activeCompierIsSunStudio()) {
            return null;
        }

        MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
        MakeConfiguration mc = mcd.getActiveConfiguration();
        CompilerSet compilerSet = mc.getCompilerSet().getCompilerSet();
        Tool ccTool = compilerSet.getTool(Tool.CCCompiler);
        String ccPath = ccTool.getPath();
        String sunstudioBinDir = ccPath.substring(0, ccPath.length() - ccTool.getName().length());

        return THAInstrumentationSupport.getSupport(mc.getDevelopmentHost().getExecutionEnvironment(), sunstudioBinDir);
    }

    private void setModified() {
        MakeConfigurationDescriptor mcd = MakeConfigurationDescriptor.getMakeConfigurationDescriptor(project);
        mcd.setModified(true);
        propertyChange(null);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        for (PropertyChangeListener l : listeners) {
            l.propertyChange(evt);
        }
    }
}
