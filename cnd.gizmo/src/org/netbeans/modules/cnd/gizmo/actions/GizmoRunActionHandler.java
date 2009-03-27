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
package org.netbeans.modules.cnd.gizmo.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.netbeans.modules.cnd.api.compilers.CompilerSet;
import org.netbeans.modules.cnd.api.execution.ExecutionListener;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.netbeans.modules.cnd.gizmo.GizmoConfigurationOptions;
import org.netbeans.modules.cnd.gizmo.GizmoServiceInfo;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionHandler;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.dlight.api.execution.DLightTarget;
import org.netbeans.modules.dlight.api.execution.DLightTarget.State;
import org.netbeans.modules.dlight.api.execution.DLightTargetListener;
import org.netbeans.modules.dlight.api.execution.DLightToolkitManagement;
import org.netbeans.modules.dlight.api.execution.DLightToolkitManagement.DLightSessionHandler;
import org.netbeans.modules.dlight.api.support.NativeExecutableTarget;
import org.netbeans.modules.dlight.api.support.NativeExecutableTargetConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightConfiguration;
import org.netbeans.modules.dlight.api.tool.DLightConfigurationManager;
import org.netbeans.modules.dlight.api.tool.DLightConfigurationOptions;
import org.netbeans.modules.dlight.util.DLightExecutorService;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminalProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.windows.InputOutput;

/**
 * @author Alexey Vladykin
 */
public class GizmoRunActionHandler implements ProjectActionHandler, DLightTargetListener {

    private ProjectActionEvent pae;
    private List<ExecutionListener> listeners;
    private DLightSessionHandler session;
    private static final String GNU_FAMILIY = "gc++filt"; //NOI18N
    private static final String SS_FAMILIY = "dem"; //NOI18N

    public GizmoRunActionHandler() {
        this.listeners = new ArrayList<ExecutionListener>();
    }

    public void init(ProjectActionEvent pae) {
        this.pae = pae;
    }

    public void execute(InputOutput io) {
        // TODO: use given InputOutput
        NativeExecutableTargetConfiguration targetConf = new NativeExecutableTargetConfiguration(
                pae.getExecutable(),
                pae.getProfile().getArgsArray(),
                createMap(pae.getProfile().getEnvironment().getenvAsPairs()));

        MakeConfiguration conf = pae.getConfiguration();
        ExecutionEnvironment execEnv = conf.getDevelopmentHost().getExecutionEnvironment();
        String runDirectory = pae.getProfile().getRunDirectory();
        if (execEnv.isRemote()) {
            PathMap mapper = HostInfoProvider.getDefault().getMapper(execEnv);
            runDirectory = mapper.getRemotePath(runDirectory);
        }

        targetConf.putInfo(GizmoServiceInfo.GIZMO_PROJECT_FOLDER, FileUtil.toFile(pae.getProject().getProjectDirectory()).getAbsolutePath());//NOI18N
        targetConf.putInfo(GizmoServiceInfo.GIZMO_PROJECT_EXECUTABLE, runDirectory + File.separator + pae.getExecutable());
        
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        String binDir = compilerSet.getDirectory();
        String demangle_utility = SS_FAMILIY;
        if (compilerSet.isGnuCompiler()){
            demangle_utility = GNU_FAMILIY;
        }
        String dem_util_path = binDir + "/" + demangle_utility; //NOI18N BTW: isn't it better to use File.Separator?
        targetConf.putInfo(GizmoServiceInfo.GIZMO_DEMANGLE_UTILITY, dem_util_path);
        
        if (execEnv.isRemote()) {
            targetConf.setHost(execEnv.getHost());
            targetConf.setUser(execEnv.getUser());
        }
        targetConf.setWorkingDirectory(runDirectory);
        int consoleType = pae.getProfile().getConsoleType().getValue();
        if (consoleType == RunProfile.CONSOLE_TYPE_DEFAULT) {
            consoleType = RunProfile.getDefaultConsoleType();
        }
        if (consoleType == RunProfile.CONSOLE_TYPE_EXTERNAL) {
            String termPath = pae.getProfile().getTerminalPath();
            if (termPath != null) {
                String termBaseName = IpeUtils.getBaseName(termPath);
                if (ExternalTerminalProvider.getSupportedTerminalIDs().contains(termBaseName)) {
                    targetConf.useExternalTerminal(ExternalTerminalProvider.getTerminal(termBaseName));
                }
            }
        }
        targetConf.setIO(io);
        NativeExecutableTarget target = new NativeExecutableTarget(targetConf);
        target.addTargetListener(this);
        DLightConfiguration configuration = DLightConfigurationManager.getInstance().getConfigurationByName("Gizmo");//NOI18N
        DLightConfigurationOptions options = configuration.getConfigurationOptions(false);
        if (options instanceof GizmoConfigurationOptions){
            ((GizmoConfigurationOptions)options).configure(pae.getProject());
        }
        final Future<DLightSessionHandler> handle = DLightToolkitManagement.getInstance().createSession(target, configuration); // NOI18N

        DLightExecutorService.submit(new Runnable() {
            public void run() {
                try {
                    session = handle.get();
                    DLightToolkitManagement.getInstance().startSession(session);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }, "DLight Session for " + target.toString()); // NOI18N

    }

    private Map<String, String> createMap(String[][] array) {
        Map<String, String> result = new HashMap<String,String>();
        for (int i = 0; i < array.length; ++i) {
            result.put(array[i][0], array[i][1]);
        }
        return result;
    }

    public boolean canCancel() {
        return true;
    }

    public void cancel() {
        if (session != null) {
            DLightToolkitManagement.getInstance().stopSession(session);
            session = null;
        }
    }

    public void addExecutionListener(ExecutionListener l) {
        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    public void removeExecutionListener(ExecutionListener l) {
        listeners.remove(l);
    }

    public void targetStateChanged(DLightTarget source, State oldState, State newState) {
        switch (newState) {
            case INIT:
            case STARTING:
                break;
            case RUNNING:
                notifyStarted();
                break;
            case FAILED:
            case STOPPED:
            case TERMINATED:
                notifyFinished(1);
                break;
            case DONE:
                notifyFinished(0);
                break;
        }
    }

    private void notifyStarted() {
        for (ExecutionListener l : listeners) {
            l.executionStarted();
        }
    }

    private void notifyFinished(int rc) {
        for (ExecutionListener l : listeners) {
            l.executionFinished(rc);
        }
    }
}
