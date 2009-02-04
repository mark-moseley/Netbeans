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
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.debugger.gdb.proxy;


// Imported classes for ShellCommand() class
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.PlatformTypes;
import org.netbeans.modules.cnd.api.remote.InteractiveCommandProvider;
import org.netbeans.modules.cnd.api.remote.InteractiveCommandProviderFactory;
import org.netbeans.modules.cnd.api.utils.Path;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.utils.CommandBuffer;
import org.netbeans.modules.cnd.debugger.gdb.utils.GdbUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Class GdbProxyEngine implements the communication with gdb (low level)
 * Main functions:
 *    start the debugger
 *    send a message (ascii text) to the debugger (via stdin)
 *    read the debugger's stdout and report this event to the upper level
 *    send a signal to the application to interrupt the execution
 *    kill the debugger
 */
public class GdbProxyEngine {
    
    private static final int MIN_TOKEN = 100;
    
    private PrintStream toGdb;
    private final GdbDebugger debugger;
    private final GdbProxy gdbProxy;
    private final List<CommandInfo> tokenList = Collections.synchronizedList(new LinkedList<CommandInfo>());
    
    //TODO: int may not be enough here, consider using long
    private int nextToken = MIN_TOKEN;
    private int currentToken = MIN_TOKEN;
    private boolean active;
    private InteractiveCommandProvider provider = null;
    private RequestProcessor.Task gdbReader = null;

    // This queue was created due to the issue 156138
    private final RequestProcessor sendQueue = new RequestProcessor("sendQueue"); // NOI18N
    private final boolean timerOn = Boolean.getBoolean("gdb.proxy.timer"); // NOI18N
    
    private static final Logger log = Logger.getLogger("gdb.gdbproxy.logger"); // NOI18N

    /**
     * Create a gdb process
     *
     * @param debuggerCommand - a name of an external debugger with parameters
     * @param debuggerEnvironment - environment variables and values to be set
     * @param workingDirectory - a directory where the debugger should run
     * @param stepIntoProject - a flag to stop at first source line
     */
    public GdbProxyEngine(GdbDebugger debugger, GdbProxy gdbProxy, List<String> debuggerCommand,
                    String[] debuggerEnvironment, String workingDirectory, String termpath,
                    String cspath) throws IOException {
        
        if (debugger.getPlatform() != PlatformTypes.PLATFORM_WINDOWS && termpath != null) {
            String tty = ExternalTerminal.create(debugger, termpath, debuggerEnvironment);
            if (tty != null) {
                debuggerCommand.add("-tty"); // NOI18N
                debuggerCommand.add(tty);
            }
        }
        this.debugger = debugger;
        this.gdbProxy = gdbProxy;
        active = true;
        
        getLogger().logMessage("Debugger Command: " + debuggerCommand); // NOI18N
        getLogger().logMessage("Env[" + debuggerEnvironment.length + "]: " + // NOI18N
                Arrays.asList(debuggerEnvironment));
        getLogger().logMessage("workingDirectory: " + workingDirectory); // NOI18N
        getLogger().logMessage("================================================"); // NOI18N
        
        if (debugger.getHostKey().equals(CompilerSetManager.LOCALHOST)) {
            localDebugger(debuggerCommand, debuggerEnvironment, workingDirectory, cspath);
        } else {
            remoteDebugger(debugger, debuggerCommand, debuggerEnvironment, workingDirectory, cspath);
        }
    }
    
    private void localDebugger(List<String> debuggerCommand, String[] debuggerEnvironment, String workingDirectory, String cspath) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(debuggerCommand);
        Map<String, String> env = pb.environment();
        
        String pathname = Path.getPathName();
        for (String var : debuggerEnvironment) {
            String key, value;
            int idx = var.indexOf('=');
            if (idx != -1) {
                key = var.substring(0, idx);
                value = var.substring(idx + 1);
                if (key.equals(pathname)) {
                    env.put(key, value + File.pathSeparator + cspath);
                } else {
                    env.put(key, value);
                }
            }
        }
        env.put(pathname, Path.getPathAsString() + File.pathSeparator + cspath);
        pb.directory(new File(workingDirectory));
        pb.redirectErrorStream(true);
        
        final Process proc = pb.start(); // Let IOException be handled in GdbdebuggerImpl.startDebugging()...
        toGdb = gdbReader(proc.getInputStream(), proc.getOutputStream());
        new RequestProcessor("GdbReaperThread").post(new Runnable() { // NOI18N
            public void run() {
                try {
                    int rc = proc.waitFor();
                    if (rc == 0) {
                        debugger.finish(false);
                    } else {
                        unexpectedGdbExit(rc);
                    }
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private void unexpectedGdbExit(int rc) {
        String msg;

        if (rc < 0) {
            msg = NbBundle.getMessage(GdbDebugger.class, "ERR_UnexpectedGdbExit");  // NOI18N
        } else {
            msg = NbBundle.getMessage(GdbDebugger.class, "ERR_UnexpectedGdbExitRC", rc);  // NOI18N
        }

        NotifyDescriptor nd = new NotifyDescriptor(msg,
                NbBundle.getMessage(GdbDebugger.class, "TITLE_UnexpectedGdbFailure"), // NOI18N
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.ERROR_MESSAGE,
                new Object[] { NotifyDescriptor.OK_OPTION },
                NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notify(nd);
        debugger.finish(false);
    }
    
    private void remoteDebugger(GdbDebugger debugger, List<String> debuggerCommand, String[] debuggerEnvironment, String workingDirectory, String cspath) {
        StringBuilder sb = new StringBuilder();
        
        for (String arg : debuggerCommand) {
            sb.append(arg);
            sb.append(' ');
        }
        
        provider = InteractiveCommandProviderFactory.create(debugger.getHostKey());
        if (provider != null && provider.run(debugger.getHostKey(), sb.toString(), null)) {
            try {
                toGdb = gdbReader(provider.getInputStream(), provider.getOutputStream());
            } catch (IOException ioe) {
           }
        }
    }
    
    private PrintStream gdbReader(InputStream is, OutputStream os) {
        final BufferedReader fromGdb = new BufferedReader(new InputStreamReader(is));
        PrintStream togdb = new PrintStream(os, true);

        gdbReader = new RequestProcessor("GdbReaderRP").post(new Runnable() { // NOI18N
            public void run() {
                String line;

                try {
                    while ((line = fromGdb.readLine()) != null) {
                        line = line.trim();
                        if (line.length() > 0) {
                            processMessage(line);
                        }
                    }
                } catch (IOException ioe) {
                } finally {
                    if (provider != null) {
                        provider.disconnect();
                        provider = null;
                    }
                }
            }
        });
        return togdb;
    }
    
    public void finish() {
        if (provider != null) {
            provider.disconnect();
        }
        if (gdbReader != null) {
            gdbReader.cancel();
        }
    }
    
    private synchronized int nextToken() {
        return nextToken++;
    }
    
    /**
     * Send a command to the debugger
     *
     * @param cmd - a command to be sent to the debugger
     */
    int sendCommand(String cmd) {
        int token = nextToken();
        sendCommand(token, cmd);
        return token;
    }

    private synchronized void sendCommand(final int token, final String cmd) {
        if (active) {
            sendQueue.post(new Runnable() {
                public void run() {
                    String time = CommandBuffer.getTimePrefix(timerOn);
                    if (cmd.charAt(0) != '-') {
                        tokenList.add(new CommandInfo(token, cmd));
                    }
                    StringBuilder fullcmd = new StringBuilder(String.valueOf(token));
                    fullcmd.append(cmd);
                    fullcmd.append('\n');
                    gdbProxy.getLogger().logMessage(time + fullcmd.toString());
                    toGdb.print(fullcmd.toString());
                }
            });
        }
    }

    CommandBuffer sendCommandEx(String cmd) {
        return sendCommandEx(cmd, true);
    }

    CommandBuffer sendCommandEx(String cmd, boolean waitForCompletion) {
        int token = nextToken();
        CommandBuffer cb = new CommandBuffer(gdbProxy, token);
        gdbProxy.putCB(token, cb);
        sendCommand(token, cmd);
        if (waitForCompletion) {
            cb.waitForCompletion();
        }
        return cb;
    }
    
    void sendConsoleCommand(String cmd) {
        int token = nextToken() + 10000;
        sendCommand(token, cmd);
    }
    
    void stopSending() {
        active = false;
    }
    
    /**
     * Process the first complete reply from the queue
     *
     * @return null if the reply is not recognized, otherwise return reply
     */
    private void processMessage(String msg) {
        String time = CommandBuffer.getTimePrefix(timerOn);
        if (msg.equals("(gdb)")) { // NOI18N
            return; // skip prompts
        }
        int token = getToken(msg);
        if (token < 0) {
            token = getCurrentToken(msg);
            if (token != -1) {
                gdbProxy.getLogger().logMessage(time + token + msg);
            } else {
                gdbProxy.getLogger().logMessage(time + msg);
            }
        } else {
            gdbProxy.getLogger().logMessage(time + msg);
        }
        msg = stripToken(msg);

        // bugfix for IZ:142454
        // ('-enable-timings no' does not turn it off sometimes)
        msg = stripTiming(msg);

        if (msg.length() == 0) {
            log.warning("Empty message received from gdb");
            return;
        }

        switch (msg.charAt(0)) {
            case '^': // result-record
                if (token == currentToken && msg.equals(GdbDebugger.DONE_PREFIX)) { // NOI18N
                    currentToken = -1;
                }
                debugger.resultRecord(token, msg);
                break;
                
            case '*': // exec-async-output
                debugger.execAsyncOutput(token, msg);
                break;
                
            case '+': // status-async-output
                debugger.statusAsyncOutput(token, msg);
                break;
                
            case '=': // notify-async-output
                debugger.notifyAsyncOutput(token, msg);
                break;
                
            case '~': // console-stream-output
                debugger.consoleStreamOutput(token, msg.substring(2, msg.length() - 1));
                break;
                
            case '@': // target-stream-output
                debugger.targetStreamOutput(msg);
                break;
                
            case '&': // log-stream-output
                debugger.logStreamOutput(msg);
                break;
                
            default:
                debugger.output(msg);
        }
    }
    
    private int getCurrentToken(String msg) {
        char ch1 = msg.charAt(0);
        if (ch1 == '&') {
            CommandInfo ci = getCommandInfo(msg);
            if (ci != null) {
                tokenList.remove(ci);
                currentToken = ci.getToken();
            }
        }
        return currentToken;
    }
    
    private CommandInfo getCommandInfo(String msg) {
        msg = msg.substring(2, msg.length() - 1).replace("\\n", ""); // NOI18N

        synchronized (tokenList) {
            for (CommandInfo ci : tokenList) {
                if (ci.getCommand().equals(msg)) {
                    return ci;
                }
            }
        }
        return null;
    }

    /**
     * Returns the position of the first non-digit symbol
     */
    private static int getFirstNonDigit(String msg) {
        for (int i = 0; i < msg.length(); i++) {
            if (!Character.isDigit(msg.charAt(i))) {
                return i;
            }
        }
        return 0;
    }
    
    /**
     * Strip the token from the start of a command line and return the token
     *
     * @param msg The line which may or may not start with a token
     * @return token The token or -1
     */
    private static int getToken(String msg) {
        int i = getFirstNonDigit(msg);
        if (i > 0) {
            return Integer.parseInt(msg.substring(0, i));
        } else {
            return -1;
        }
    }
    
    /**
     * Strip the token from the start of a command line
     *
     * @param msg The line which may or may not start with a token
     * @return msg The message without a leading integer token
     */
    private static String stripToken(String msg) {
        int i = getFirstNonDigit(msg);
        char ch = (i < msg.length()) ? msg.charAt(i) : 0;
        if ((ch == '^' || ch == '*' || ch == '+' || ch == '=') && ch != 0) {
            return msg.substring(i);
        } else {
            return msg;
        }
    }

    private static final String TIME_PREFIX = ",time="; // NOI18N
    /**
     * Cut timing information if any
     * @param msg
     */
    private String stripTiming(String msg) {
        int pos = msg.indexOf(TIME_PREFIX);
        if (pos != -1) {
            // time= prefix may appear not only in the end of the message, see issue 147938
            int endPos = GdbUtils.findMatchingCurly(msg, pos + TIME_PREFIX.length());
            if (endPos != -1) {
                return msg.substring(0, pos) + msg.substring(endPos+1);
            } else {
                log.warning("Matching curly not found in timing info: " + msg); // NOI18N
            }
        }
        return msg;
    }
    
    private GdbLogger getLogger() {
        return gdbProxy.getLogger();
    }
    
    private static class CommandInfo {
        
        private final int token;
        private final String cmd;
        
        public CommandInfo(int token, String cmd) {
            this.token = token;
            this.cmd = cmd;
        }
        
        private String getCommand() {
            return cmd;
        }
        
        public int getToken() {
            return token;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o instanceof CommandInfo) {
                CommandInfo ci = (CommandInfo) o;
                return cmd.equals(ci.getCommand());
            } else if (o instanceof String) {
                return cmd.equals(o.toString());
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return token;
        }
    }
    
} /* End of GdbProxyEngine */
