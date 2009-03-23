/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.remote.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.openide.util.Exceptions;

/**
 * Run a remote command. This remote command should <b>not</b> expect input. The output
 * from the command is stored in a StringWriter and can be gotten via toString().
 *
 * @author gordonp
 */
public class RemoteCommandSupport extends RemoteConnectionSupport {

    private BufferedReader in;
    private StringWriter out;
    private final String cmd;
    private final Map<String, String> env;

    public static int run(ExecutionEnvironment execEnv, String cmd) {
        RemoteCommandSupport support = new RemoteCommandSupport(execEnv, cmd);
        return support.run();
    }

    public RemoteCommandSupport(ExecutionEnvironment execEnv, String cmd, Map<String, String> env) {
        super(execEnv);
        this.cmd = cmd;
        this.env = env;
    }

    public int run() {
        if (!isFailedOrCancelled()) {
            log.fine("RemoteCommandSupport<Init>: Running [" + cmd + "] on " + executionEnvironment);
            if (SwingUtilities.isEventDispatchThread()) {
                String text = "Running remote command in EDT: " + cmd; //NOI18N
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, text, new Exception(text));
                } else {
                    log.warning(text);
                }
            }
            try {
//                final String substitutedCommand = substituteCommand();
                NativeProcessBuilder pb = new NativeProcessBuilder(executionEnvironment, cmd);
                pb = pb.addEnvironmentVariables(env);
                Process process = pb.call();
                InputStream is = process.getInputStream();
                if (is == null) { // otherwise we can get an NPE in reader
                    throw new IOException("process (" + process.getClass().getName() + ") returned null input stream"); //NOI18N
                }
                in = new BufferedReader(new InputStreamReader(is));
                out = new StringWriter();
                String line;
                while ((line = in.readLine()) != null) {
                    if (line != null) {
                        out.write(line + '\n');
                        out.flush();
                    }
                }
// TODO (execution) should we revive this?
//                try {
//                    Thread.sleep(100); // according to jsch samples
//                } catch (InterruptedException e) {
//                }
                int rc = process.waitFor();
                log.fine("RemoteCommandSupport: " + cmd + " on " + executionEnvironment + " finished; rc=" + rc);
                if (rc != 0 && log.isLoggable(Level.FINEST)) {
                    String errMsg;
                    final BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    while ((errMsg = reader.readLine()) != null) {
                        log.finest("RemoteCommandSupport ERROR: " + errMsg);
                    }
                }
                setExitStatus(rc);
            } catch (InterruptedException ie) {
                log.log(Level.WARNING, ie.getMessage(), ie);
            } catch (IOException ex) {
                log.warning("IO failure during running " + cmd);
//            } finally {
//                disconnect();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        return getExitStatus();
    }

    public RemoteCommandSupport(ExecutionEnvironment execEnv, String cmd) {
        this(execEnv, cmd, null);
    }

    @Override
    public String toString() {
        return getOutput();
    }

    public String getOutput() {
        if (out != null) {
            return out.toString();
        } else {
            return "";
        }
    }

//    public void setPreserveCommand(boolean value) {
//        preserveCommand = value;
//    }
//
//    private boolean preserveCommand = true; // false;

    //TODO (execution): ???
//    private String substituteCommand() {
//        StringBuilder cmdline = new StringBuilder();
//        if (!preserveCommand) {
//            if (env != null) {
//                // we can't use ssh env routine cause it allows only vars described by AllowEnv in /etc/ssh/sshd_config
//                // echannel.setEnv(ev, env.get(ev));
//                // so we do next
//                cmdline.append(ShellUtils.prepareExportString(env));
//            }
//
//            String pathName = "PATH";//PlatformInfo.getDefault(key).getPathName();//NOI18N
//            if (env == null || env.get(pathName) == null) {
//                cmdline.append(ShellUtils.prepareExportString(new String[] {pathName + "=/bin:/usr/bin:$PATH"}));//NOI18N
//            }
//        } else {
//            assert env==null || env.size() == 0; // if one didn't want command to be changed but provided env he should be aware of doing something wrong
//        }
//
//
//        cmdline.append(cmd);
//
//        String theCommand = cmdline.toString();
//
//        if (!preserveCommand) {
//            theCommand = ShellUtils.wrapCommand(executionEnvironment, theCommand);
//        }
//
//        return theCommand;
//    }
}
