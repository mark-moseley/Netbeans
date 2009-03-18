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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminal;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 */
public final class TerminalLocalNativeProcess extends AbstractNativeProcess {

    private final static String dorunScript;
    private final static boolean isWindows;
    private final InputStream processOutput;
    private final InputStream processError;
    private final OutputStream processInput;
    private final String pidFileName;
    private final Process termProcess;


    static {
        isWindows = Utilities.isWindows();
        String runScript = null;
        InstalledFileLocator fl = InstalledFileLocator.getDefault();
        File file = fl.locate("bin/nativeexecution/dorun.sh", null, false); // NOI18N
        if (file != null) {
            runScript = file.toString();

            if (!isWindows) {
                try {
                    new ProcessBuilder("/bin/chmod", "+x", runScript).start().waitFor(); // NOI18N
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        dorunScript = runScript;
    }

    public TerminalLocalNativeProcess(final ExternalTerminal t,
            final NativeProcessInfo info) throws IOException {
        super(info);

        if (dorunScript == null) {
            //throw new IOException("dorun not found"); // NOI18N
            processError = new ByteArrayInputStream(
                    "unable to start process in an external terminal - dorun script not found".getBytes()); // NOI18N
            processOutput = new ByteArrayInputStream(new byte[0]);
            processInput = null;
            pidFileName = null;
            termProcess = null;
        } else {
            ExternalTerminal terminal = t;

            final String commandLine = info.getCommandLine();
            final String workingDirectory = info.getWorkingDirectory(true);
            final File wdir =
                    workingDirectory == null ? null : new File(workingDirectory);

            File pidFile = File.createTempFile("dlight", "termexec"); // NOI18N
            pidFile.deleteOnExit();

            String pidFName = pidFile.toString();

            final ExternalTerminalAccessor terminalInfo =
                    ExternalTerminalAccessor.getDefault();

            if (terminalInfo.getTitle(terminal) == null) {
                terminal = terminal.setTitle(commandLine);
            }

            String cmd = commandLine;

            if (isWindows) {
                pidFName = pidFName.replaceAll("\\\\", "/"); // NOI18N
                cmd = cmd.replaceAll("\\\\", "/"); // NOI18N
            }

            pidFileName = pidFName;

            List<String> command = terminalInfo.wrapCommand(
                    info.getExecutionEnvironment(),
                    terminal,
                    dorunScript,
                    "-p", pidFileName, // NOI18N
                    "-x", terminalInfo.getPrompt(terminal), // NOI18N
                    cmd);

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.environment().putAll(info.getEnvVariables());
            pb.directory(wdir);

            termProcess = pb.start();

            processOutput = termProcess.getInputStream();
            processError = termProcess.getErrorStream();
            processInput = null;

            waitPID();
        }
    }

    @Override
    public void cancel() {
        try {
            String cmd = isWindows ? "kill" : "/bin/kill"; // NOI18N
            ProcessBuilder pb =
                    new ProcessBuilder(cmd, "-9", "" + getPID()); // NOI18N
            pb.start().waitFor();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public int waitResult() throws InterruptedException {
        int pid = -1;

        try {
            pid = getPID();
        } catch (IllegalStateException ex) {
        }

        if (pid < 0) {
            return -1;
        }

        if (isWindows) {
            ProcessBuilder pb = new ProcessBuilder("kill", "-0", "" + getPID()); // NOI18N
            while (true) {
                try {
                    int status = pb.start().waitFor();
                    if (status != 0) {
                        break;
                    } else {
                        Thread.sleep(500);
                    }
                } catch (IOException ex) {
                }
            }
        } else {
            File f = new File("/proc/" + getPID()); // NOI18N

            while (f.exists()) {
                Thread.sleep(300);
            }
        }

        int exitCode = -1;

        try {
            File resFile = new File(pidFileName + ".res"); // NOI18N
            resFile.deleteOnExit();
            exitCode = Integer.parseInt(new BufferedReader(new FileReader(resFile)).readLine().trim());
        } catch (IOException ex) {
        } catch (NumberFormatException ex) {
        }

        return exitCode;
    }

    @Override
    public OutputStream getOutputStream() {
        return processInput;
    }

    @Override
    public InputStream getInputStream() {
        return processOutput;
    }

    @Override
    public InputStream getErrorStream() {
        return processError;
    }

    private boolean isFinished() {
        try {
            termProcess.exitValue();
            return true;
        } catch (IllegalThreadStateException ex) {
            return false;
        }
    }

    private void waitPID() {
        File realPidFile = new File(pidFileName); // NOI18N

        while (true) {
            if (realPidFile.exists() && realPidFile.length() > 0) {
                try {
                    InputStream pidIS = new FileInputStream(realPidFile);
                    readPID(pidIS);
                    pidIS.close();
                    break;
                } catch (IOException ex) {
                    return;
                }
            }

            if (isFinished() || Thread.currentThread().isInterrupted()) {
                // use readPID(null) to initiate ERROR state...
                readPID(null);
                return;
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
