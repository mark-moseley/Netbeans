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
package org.netbeans.modules.nativeexecution;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.HostInfo.OSFamily;
import org.netbeans.modules.nativeexecution.api.util.CommandLineHelper;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory;
import org.netbeans.modules.nativeexecution.api.util.MacroExpanderFactory.MacroExpander;
import org.netbeans.modules.nativeexecution.support.CaseInsensitiveMacroMap;
import org.netbeans.modules.nativeexecution.support.MacroMap;
import org.openide.util.Utilities;

/**
 *
 */
// @NotThreadSafe
// This class is always used in a thread-safe manner ...
public final class NativeProcessInfo {

    public final MacroExpander macroExpander;
    private final ExecutionEnvironment execEnv;
    private final boolean isWindows;
    private final MacroMap envVariables;
    private final List<String> arguments = new ArrayList<String>();
    private String executable;
    private String commandLine;
    private String workingDirectory;
    private boolean unbuffer;
    private boolean redirectError;
    private Collection<ChangeListener> listeners = null;

//    public NativeProcessInfo(NativeProcessInfo info) {
//        execEnv = info.execEnv;
//        command = info.command;
//        macroExpander = MacroExpanderFactory.getExpander(execEnv);
//        workingDirectory = info.workingDirectory;
//
//        if (execEnv.isLocal() && Utilities.isWindows()) {
//            envVariables = new CaseInsensitiveMacroMap(macroExpander);
//        } else {
//            envVariables = new MacroMap(macroExpander);
//        }
//
//        envVariables.putAll(info.envVariables);
//        arguments.addAll(info.arguments);
//
//        if (info.listeners != null) {
//            listeners = new ArrayList<ChangeListener>(info.listeners);
//        }
//
//        unbuffer = info.unbuffer;
//        isWindows = info.isWindows;
//    }
    public NativeProcessInfo(ExecutionEnvironment execEnv) {
        this.execEnv = execEnv;
        this.executable = null;
        this.unbuffer = false;
        this.workingDirectory = null;
        this.macroExpander = MacroExpanderFactory.getExpander(execEnv);

        if (execEnv.isLocal() && Utilities.isWindows()) {
            envVariables = new CaseInsensitiveMacroMap(macroExpander);
        } else {
            envVariables = new MacroMap(macroExpander);
        }

        HostInfo hostInfo = null;

        try {
            hostInfo = HostInfoUtils.getHostInfo(execEnv);
        } catch (IOException ex) {
        } catch (CancellationException ex) {
        }

        isWindows = hostInfo != null && hostInfo.getOSFamily() == OSFamily.WINDOWS;

        redirectError = false;
    }

    public void addNativeProcessListener(ChangeListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<ChangeListener>();
        }

        listeners.add(listener);
    }

    public void redirectError(boolean redirectError) {
        this.redirectError = redirectError;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }

    public void setCommandLine(String commandLine) {
        this.commandLine = commandLine;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public void setUnbuffer(boolean unbuffer) {
        this.unbuffer = unbuffer;
    }

    public boolean isUnbuffer() {
        return unbuffer;
    }

    /**
     *
     * @param name
     * @param value
     */
    public void addEnvironmentVariable(final String name, final String value) {
        envVariables.put(name, value);
    }

    public void addEnvironmentVariables(Map<String, String> envs) {
        for (String key : envs.keySet()) {
            addEnvironmentVariable(key, envs.get(key));
        }
    }

    public void setArguments(String... arguments) {
        if (commandLine != null) {
            throw new IllegalStateException("commandLine is already defined. No additional parameters can be set"); // NOI18N
        }

        this.arguments.clear();
        this.arguments.addAll(Arrays.asList(arguments));
    }

//    public String[] getCommand() {
//        String cmd;
//
//        try {
//            cmd = macroExpander.expandPredefinedMacros(executable);
//        } catch (ParseException ex) {
//            cmd = executable;
//        }
//
//        List<String> result = new ArrayList<String>();
//        result.add(cmd);
//        if (!arguments.isEmpty()) {
//            result.addAll(arguments);
//        }
//        return result.toArray(new String[0]);
//    }
//
    public String getCommandLineForShell() {
        if (commandLine != null) {
            return commandLine;
        }

        String cmd;

        try {
            cmd = macroExpander.expandPredefinedMacros(executable);
        } catch (ParseException ex) {
            cmd = executable;
        }

        cmd = CommandLineHelper.getInstance(execEnv).toShellPath(cmd);

        StringBuilder sb = new StringBuilder(cmd);

        if (!arguments.isEmpty()) {
            for (String arg : arguments) {
                sb.append(" '").append(arg).append('\''); // NOI18N
            }
        }

        if (redirectError) {
            sb.append(" 2>&1"); // NOI18N
        }

        return sb.toString().trim();
    }

    public ExecutionEnvironment getExecutionEnvironment() {
        return execEnv;
    }

    public Collection<ChangeListener> getListeners() {
        return listeners;
    }

    public String getWorkingDirectory(boolean expandMacros) {
        String result;

        if (expandMacros && macroExpander != null) {
            try {
                result = macroExpander.expandPredefinedMacros(workingDirectory);
            } catch (ParseException ex) {
                result = workingDirectory;
            }
        }

        result = workingDirectory;

        return result;
    }

    public MacroMap getEnvVariables() {
        return getEnvVariables(null);
    }

    public MacroMap getEnvVariables(Map<String, String> prependMap) {
        // TODO: is there a need of prepending prependMap ?
        // there was some implementation in one of previous version...
        return envVariables;
    }
}
