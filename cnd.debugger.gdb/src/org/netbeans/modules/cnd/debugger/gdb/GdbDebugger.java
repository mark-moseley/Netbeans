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

package org.netbeans.modules.cnd.debugger.gdb;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.api.compilers.PlatformTypes;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.remote.ExecutionEnvironmentFactory;
import org.netbeans.modules.cnd.api.remote.HostInfoProvider;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.debugger.gdb.actions.GdbActionHandler;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.AddressBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.BreakpointImpl;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.GdbBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.LineBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.disassembly.Disassembly;
import org.netbeans.modules.cnd.debugger.gdb.event.GdbBreakpointEvent;
import org.netbeans.modules.cnd.debugger.gdb.profiles.GdbProfile;
import org.netbeans.modules.cnd.debugger.gdb.proxy.GdbProxy;
import org.netbeans.modules.cnd.debugger.gdb.proxy.IOProxy;
import org.netbeans.modules.cnd.debugger.gdb.timer.GdbTimer;
import org.netbeans.modules.cnd.debugger.gdb.utils.CommandBuffer;
import org.netbeans.modules.cnd.debugger.gdb.utils.GdbUtils;
import org.netbeans.modules.cnd.execution.Unbuffer;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompilerSet2Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.settings.CppSettings;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.InputOutput;

/**
 * Represents one GDB debugger session.
 *
 * <br><br>
 * <b>How to obtain it from DebuggerEngine:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    GdbDebugger cndDebugger = (GdbDebugger) debuggerEngine.lookup
 *        (GdbDebugger.class);</pre>
 */
public class GdbDebugger implements PropertyChangeListener {

    public static final String          PROP_STATE = "state"; // NOI18N
    public static final String          PROP_CURRENT_THREAD = "currentThread"; // NOI18N
    public static final String          PROP_CURRENT_CALL_STACK_FRAME = "currentCallStackFrame"; // NOI18N
    public static final String          PROP_KILLTERM = "killTerm"; // NOI18N
    public static final String          PROP_SHARED_LIB_LOADED = "sharedLibLoaded"; // NOI18N
    public static final String          PROP_VALUE_CHANGED = "valueChanged"; // NOI18N
    public static final String          PROP_LOCALS_REFRESH = "localsRefresh"; // NOI18N

    public static enum State {
        NONE, STARTING, LOADING, READY, RUNNING, STOPPED, SILENT_STOP, EXITED;
    }

    public static enum LastGoState {
        CONTINUE, FINISH, STEP, NEXT;
    }

    public static final String DONE_PREFIX = "^done"; // NOI18N

    private LastGoState                 lastGo;
    private String                      lastStop;

    private static final ProjectActionEvent.Type DEBUG_ATTACH = ProjectActionEvent.Type.CHECK_EXECUTABLE;

    /** ID of GDB Debugger Engine for C */
    public static final String          ENGINE_ID = "netbeans-cnd-GdbSession/C"; // NOI18N

    /** ID of GDB Debugger Session */
    public static final String          SESSION_ID = "netbeans-cnd-GdbSession"; // NOI18N

    /** ID of GDB Debugger SessionProvider */
    public static final String          SESSION_PROVIDER_ID = "netbeans-cnd-GdbSessionProvider"; // NOI18N

    /** Dis update */
    public static final String          DIS_UPDATE = "dis_update"; // NOI18N

    private static final String MSG_BREAKPOINT_ERROR = "Cannot insert breakpoint"; // NOI18N

    private GdbProxy gdb;
    private final ContextProvider lookupProvider;
    private State state = State.NONE;
    private final PropertyChangeSupport pcs;
    private String runDirectory;
    private String baseDir;
    private final List<CallStackFrame> callstack = new ArrayList<CallStackFrame>();
    private final GdbEngineProvider gdbEngineProvider;
    private CallStackFrame currentCallStackFrame;
    public final Object LOCK = new Object();
    private long programPID = 0;
    private double gdbVersion = 6.4;
    private boolean continueAfterFirstStop = true;
    private final List<GdbVariable> localVariables = new ArrayList<GdbVariable>();
    private final Map<Integer, BreakpointImpl> pendingBreakpointMap = new HashMap<Integer, BreakpointImpl>();
    private final Map<Integer, BreakpointImpl> breakpointList = Collections.synchronizedMap(new HashMap<Integer, BreakpointImpl>());
    private final List<String> temporaryBreakpoints = new ArrayList<String>();
    private static final Map<String, TypeInfo> ticache = new HashMap<String, TypeInfo>();
    private static final Logger log = Logger.getLogger("gdb.logger"); // NOI18N
    private static final Logger tlog = Logger.getLogger("gdb.testlogger"); // NOI18N
    private int currentToken = 0;
    private String currentThreadID = "1"; // NOI18N
    private static final String[] emptyThreadsList = new String[0];
    private String[] threadsList = emptyThreadsList;
    private Timer startupTimer = null;
    private boolean cygwin = false;
    private boolean mingw = false;
    private boolean cplusplus = false;
    private String firstBPfullname;
    private String firstBPfile;
    private String firstBPline;
    private InputOutput iotab;
    private boolean firstOutput = true;
    private boolean dlopenPending = false;
    private int shareToken;
    private final Disassembly disassembly;
    private GdbBreakpoint currentBreakpoint = null;
    private ExecutionEnvironment execEnv;
    private int platform;
    private PathMap pathMap;
    private Map<String, ShareInfo> shareTab;
    private String sig = null;
    private IOProxy ioProxy = null;
    private GdbVersionPeculiarity versionPeculiarity = null;

    public GdbDebugger(ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        pcs = new PropertyChangeSupport(this);
        addPropertyChangeListener(this);

        GdbEngineProvider dep = null;
        List<? extends DebuggerEngineProvider> l = lookupProvider.lookup(null, DebuggerEngineProvider.class);
        for (DebuggerEngineProvider curDep : l) {
            if (curDep instanceof GdbEngineProvider) {
                dep = (GdbEngineProvider) curDep;
            }
        }
        if (dep == null && !isUnitTest()) {
            throw new IllegalArgumentException(
                    "GdbEngineProvider must be used to start GdbDebugger!"); // NOI18N
        }
        this.gdbEngineProvider = dep;

        threadsViewInit();
        this.disassembly = new Disassembly(this);
        shareTab = null;
        execEnv = ExecutionEnvironmentFactory.getLocalExecutionEnvironment();
    }

    public ContextProvider getLookup() {
        return lookupProvider;
    }

    public void startDebugger() {
        ProjectActionEvent pae;
        GdbProfile profile;
        String termpath = null;
        int conType;
        GdbTimer.getTimer("Startup").start("Startup1"); // NOI18N
        GdbTimer.getTimer("Stop").start("Stop1"); // NOI18N

        setStarting();
        try {
            pae = lookupProvider.lookupFirst(null, ProjectActionEvent.class);
            execEnv = ((MakeConfiguration) pae.getConfiguration()).getDevelopmentHost().getExecutionEnvironment();
            pathMap = HostInfoProvider.getDefault().getMapper(execEnv);
            iotab = lookupProvider.lookupFirst(null, InputOutput.class);
            if (iotab != null) {
                iotab.setErrSeparated(false);
            }
            runDirectory = pathMap.getRemotePath(pae.getProfile().getRunDirectory().replace("\\", "/") + "/");  // NOI18N
            baseDir = pae.getConfiguration().getBaseDir().replace("\\", "/");  // NOI18N
            profile = (GdbProfile) pae.getConfiguration().getAuxObject(GdbProfile.GDB_PROFILE_ID);
            conType = execEnv.isLocal() ? pae.getProfile().getConsoleType().getValue() : RunProfile.CONSOLE_TYPE_OUTPUT_WINDOW;
            platform = ((MakeConfiguration) pae.getConfiguration()).getPlatform().getValue();
            if (platform != PlatformTypes.PLATFORM_WINDOWS && conType != RunProfile.CONSOLE_TYPE_OUTPUT_WINDOW && pae.getType() != DEBUG_ATTACH) {
                termpath = pae.getProfile().getTerminalPath();
            }
            if (!Boolean.getBoolean("gdb.suppress.timeout")) {
                startupTimer = new Timer("GDB Startup Timer Thread"); // NOI18N
                startupTimer.schedule(new TimerTask() {

                    public void run() {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                                "ERR_StartupTimeout"))); // NOI18N
                        finish(true);
                    }
                }, 30000);
            }
            String gdbCommand = profile.getGdbPath((MakeConfiguration) pae.getConfiguration(), false);
            if (gdbCommand.toLowerCase().contains("cygwin")) { // NOI18N
                cygwin = true;
            } else if (gdbCommand.toLowerCase().contains("mingw")) { // NOI18N
                mingw = true;
            }
            String cspath = getCompilerSetPath(pae);
            // see IZ 158224, gdb should be run with the default environment
            gdb = new GdbProxy(this, gdbCommand, /*pae.getProfile().getEnvironment().getenv()*/new String[]{},
                    runDirectory, termpath, cspath);
            // we should not continue until gdb version is initialized
            initGdbVersion();

            gdb.environment_directory(runDirectory);
            gdb.gdb_show("language"); // NOI18N
            gdb.gdb_set("print repeat", Integer.toString(CppSettings.getDefault().getArrayRepeatThreshold())); // NOI18N
            if (pae.getType() == DEBUG_ATTACH) {
                String pgm = null;
                boolean isSharedLibrary = false;
                final String path = getFullPath(baseDir, pae.getExecutable());

                programPID = lookupProvider.lookupFirst(null, Long.class);
                if (((MakeConfiguration) pae.getConfiguration()).isDynamicLibraryConfiguration()) {
                    pgm = getExePath(programPID);
                    gdb.file_exec_and_symbols(pgm);
                    isSharedLibrary = true;
                } else {
                    gdb.file_exec_and_symbols(path);
                }
                CommandBuffer cb = gdb.target_attach(Long.toString(programPID));
                String err = cb.getError();
                if (err != null || cb.isTimedOut()) {
                    final String msg;
                    if (err == null) {
                        msg = NbBundle.getMessage(GdbDebugger.class, "ERR_AttachTimeout"); // NOI18N
                    } else if (err.toLowerCase().contains("no such process") || // NOI18N
                            err.toLowerCase().contains("couldn't open /proc file for process ")) { // NOI18N
                        msg = NbBundle.getMessage(GdbDebugger.class, "ERR_NoSuchProcess"); // NOI18N
                    } else {
                        msg = NbBundle.getMessage(GdbDebugger.class, "ERR_CantAttach"); // NOI18N
                    }
                    warnAndFinish(false, msg);
                    return; // since we've failed, a return here keeps us from sending more gdb commands
                } else {
                    if (isSharedLibrary) {
                        if (platform == PlatformTypes.PLATFORM_MACOSX && pgm == null) {
                            pgm = getMacExePath();
                        }
                        if (pgm != null) {
                            gdb.file_symbol_file(pgm);
                        }
                    }

                    // 1) see if path was explicitly loaded by target_attach (this is system dependent)
                    if (!symbolsRead(cb.getResponse(), path)) {
                        // 2) see if we can validate via /proc (or perhaps other platform specific means)
                        if (validAttachViaSlashProc(programPID, path)) { // Linux or Solaris
                            if (isSolaris()) {
                                gdb.file_symbol_file(path);
                            }
                            setLoading();
                        } else if (isSharedLibrary && platform == PlatformTypes.PLATFORM_MACOSX) {
                            String addr = getMacDylibAddress(path, gdb.info_share(true).getResponse());
                            if (addr != null) {
                                gdb.addSymbolFile(path, addr);
                                setLoading();
                            } else {
                                warnAndFinish(false, NbBundle.getMessage(GdbDebugger.class, "ERR_AttachValidationFailure")); // NOI18N
                                return; // since we've failed, a return here keeps us from sending more gdb commands
                            }
                        } else {
                            // 3) send an "info files" command to gdb. Its response should say what symbols
                            // are read.
                            if (symbolsReadFromInfoFiles(gdb.info_files().getResponse(), path)) {
                                setLoading();
                            } else {
                                warnAndFinish(false, NbBundle.getMessage(GdbDebugger.class, "ERR_AttachValidationFailure")); // NOI18N
                                return; // since we've failed, a return here keeps us from sending more gdb commands
                            }
                        }
                    } else {
                        setLoading();
                    }
                }
                gdb.data_list_register_names("");
            } else {
                gdb.file_exec_and_symbols(getProgramName(pae.getExecutable()));

                String[] env = pae.getProfile().getEnvironment().getenv();
                Map<String, String> mapEnv = new HashMap<String, String>(env.length);
                EnvUtils.appendEnv(mapEnv, env);
                
                if (conType == RunProfile.CONSOLE_TYPE_OUTPUT_WINDOW) {
                    for (String envEntry : Unbuffer.getUnbufferEnvironment(execEnv, pae.getExecutable())) {
                        EnvUtils.appendPath(mapEnv, EnvUtils.getKey(envEntry), EnvUtils.getValue(envEntry));
                    }
                    // disabled on windows because of the issue 148204
                    if (platform != PlatformTypes.PLATFORM_WINDOWS) {
                        ioProxy = IOProxy.create(execEnv, iotab);
                    }
                }

                // set project environment
                for (Map.Entry<String, String> entry : mapEnv.entrySet()) {
                    gdb.gdb_set("environment", entry.getKey() + '=' + entry.getValue()); // NOI18N
                }

                if (platform == PlatformTypes.PLATFORM_WINDOWS) {
                    if (conType != RunProfile.CONSOLE_TYPE_OUTPUT_WINDOW) {
                        gdb.set_new_console();
                    }
                }
                if (pae.getType() == ProjectActionEvent.Type.DEBUG_STEPINTO) {
                    continueAfterFirstStop = false; // step into project
                }
                gdb.break_insert_temporary("main"); // NOI18N
                if (platform == PlatformTypes.PLATFORM_WINDOWS) {
                    // WinAPI apps don't have a "main" function. Use "WinMain" if Windows.
                    gdb.break_insert_temporary("WinMain"); // NOI18N
                }
                gdb.data_list_register_names("");
                try {
                    String inRedir = "";
                    if (ioProxy != null) {
                        String inFile = ioProxy.getInFilename();
                        String outFile = ioProxy.getOutFilename();
                        if (platform == PlatformTypes.PLATFORM_WINDOWS) {
                            inFile = win2UnixPath(inFile);
                            outFile = win2UnixPath(outFile);
                        }
                        // fix for the issue 149736 (2>&1 redirection does not work in gdb MI on mac)
                        if (platform == PlatformTypes.PLATFORM_MACOSX) {
                            inRedir = " < " + inFile + " > " + outFile + " 2> " + outFile; // NOI18N
                        } else {
                            // csh (tcsh also) does not support 2>&1 stream redirection, see issue 147872
                            String shell = HostInfoProvider.getDefault().getEnv(execEnv).get("SHELL"); // NOI18N
                            if (shell != null && shell.endsWith("csh")) { // NOI18N
                                inRedir = " < " + inFile + " >& " + outFile; // NOI18N
                            } else {
                                inRedir = " < " + inFile + " > " + outFile + " 2>&1"; // NOI18N
                            }
                        }
                    }
                    gdb.exec_run(pae.getProfile().getArgsFlat() + inRedir);
                } catch (Exception ex) {
                    ErrorManager.getDefault().notify(ex);
                    (lookupProvider.lookupFirst(null, Session.class)).kill();
                }
                if (platform == PlatformTypes.PLATFORM_WINDOWS) {
                    String msg = gdb.info_threads().getResponse(); // we get the PID from this...
                    int pos1 = msg.indexOf("* 1 thread "); // NOI18N
                    if (pos1 >= 0) {
                        int pos2 = msg.indexOf('.', pos1);
                        if (pos2 > 0) {
                            try {
                                programPID = Long.valueOf(msg.substring(pos1 + 11, pos2));
                            } catch (NumberFormatException ex) {
                                log.warning("Failed to get PID from \"info threads\""); // NOI18N
                            }
                        }
                    }
                } else if (platform != PlatformTypes.PLATFORM_MACOSX) {
                    gdb.info_proc(); // we get the PID from this...
                }
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "GdbDebugger.startDebugger: Exception during start: ", ex);
            if (startupTimer != null) {
                startupTimer.cancel();
            }
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                msg = ex.getMessage();
            }
            if (msg == null || msg.length() == 0) {
                msg = NbBundle.getMessage(GdbDebugger.class, "ERR_UnSpecifiedStartError");
            }
            warnAndFinish(false, msg);
        }
    }

    private void warnAndFinish(final boolean killTerm, final String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
                finish(killTerm);
            }
        });
    }

    private final void initGdbVersion() throws GdbErrorException {
        CommandBuffer res = gdb.gdb_version();
        if (res.isOK()) {
            String message = res.getResponse();

            if (startupTimer != null) {
                // Cancel the startup timer - we've got our first response from gdb
                startupTimer.cancel();
                startupTimer = null;
            }

            gdbVersion = parseGdbVersionString(message.substring(8));
            versionPeculiarity = GdbVersionPeculiarity.create(gdbVersion, platform);
            if (message.contains("cygwin")) { // NOI18N
                cygwin = true;
            }
        } else {
            throw new GdbErrorException("gdb version check failed, exiting"); //NOI18N
        }
    }

    public double getGdbVersion() {
        return gdbVersion;
    }

    public void testSuiteInit(GdbProxy gdb) throws GdbErrorException {
        if (!isUnitTest()) {
            throw new IllegalStateException(NbBundle.getMessage(GdbDebugger.class, "Cannot call testSuiteInit outside of a testsuite!"));
        }

        log.setLevel(Level.OFF);
        tlog.setLevel(Level.FINE);
        this.gdb = gdb;
        initGdbVersion();
    }

    public static boolean isUnitTest() {
        return System.getProperty("gdb.testsuite") != null; // NOI18N
    }

    private String win2UnixPath(String path) {
        String res = path;
        if (isCygwin()) {
            res = "/cygdrive/" + path.charAt(0) + path.substring(2); // NOI18N
        } else if (isMinGW()) {
            res = "/" + path.charAt(0) + "/" + path.substring(2); // NOI18N
        }
        return res.replace('\\', '/');
    }

    public ExecutionEnvironment getHostExecutionEnvironment() {
        return execEnv;
    }

    public int getPlatform() {
        return platform;
    }

    public PathMap getPathMap() {
        return pathMap;
    }

    public static GdbDebugger getGdbDebugger() {
        DebuggerEngine currentEngine = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (currentEngine == null) {
            return null;
        }
        return currentEngine.lookupFirst(null, GdbDebugger.class);
    }

    private String getCompilerSetPath(ProjectActionEvent pae) {
        CompilerSet2Configuration cs = ((MakeConfiguration) pae.getConfiguration()).getCompilerSet();
        String csname = cs.getOption();
        String csdirs = cs.getCompilerSetManager().getCompilerSet(csname).getDirectory();

        if (cs.getCompilerSetManager().getCompilerSet(csname).getCompilerFlavor().isMinGWCompiler()) {
            String msysBase = CompilerSetManager.getMSysBase();
            if (msysBase != null && msysBase.length() > 0) {
                csdirs += File.pathSeparator + msysBase + "/bin"; // NOI18N;
            }
        }

        return csdirs;
    }

    public String getSignal() {
        return sig;
    }

    public GdbVersionPeculiarity getVersionPeculiarity() {
        return versionPeculiarity;
    }

    /**
     * Get the full path name of the executable.
     *
     * Note: A comment suggested we should use IpeUtils.toAbsolutePath(). This actualy is an
     * error because gdb <b>requires</b> forward slashes and toAbsolutePath uses File.separator.
     * Since it can be called in non-gdb situations, its probably best not to make it always
     * use forward slashes, either.
     *
     * @param base The base directory to prepend if path is relative
     * @param path Either an absolute path or relative path component
     * @return An absolute path
     */
    private String getFullPath(String base, String path) {
        if (platform == PlatformTypes.PLATFORM_WINDOWS && Character.isLetter(path.charAt(0)) && path.charAt(1) == ':') {
            return path;
        } else if (path.charAt(0) == '/') {
            return path;
        } else {
            return base + '/' + path; // gdb requires forward slashes!
        }
    }

    public void showCurrentSource(boolean dis) {
        final CallStackFrame csf = getCurrentCallStackFrame();
        if (csf == null) {
            return;
        }
        if (!dis) {
            dis = (currentBreakpoint == null) ? Disassembly.isInDisasm() : (currentBreakpoint instanceof AddressBreakpoint);
        }
        final boolean inDis = dis;
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                // show current line
                EditorContextBridge.showSource(csf, inDis);
            }
        });
    }

    public String[] getThreadsList() {
        if (state == State.STOPPED) {
            if (threadsList == emptyThreadsList) {
                while (gdb == null) {
                    try {
                        Thread.sleep(100); // called before session startup had completed...
                    } catch (InterruptedException ex) {
                    }
                }
                String results = gdb.info_threads().getResponse();
                if (results.length() > 0) {
                    List<String> list = new ArrayList<String>();
                    StringBuilder sb = new StringBuilder();
                    for (String line : results.split("\\\\n")) { // NOI18N
                        if (line.startsWith("    ")) { // NOI18N
                            sb.append(" " + line.replace("\\n", "").trim()); // NOI18N
                        } else {
                            if (sb.length() > 0) {
                                list.add(sb.toString());
                                sb.delete(0, sb.length());
                            }
                            line = line.trim();
                            char ch = line.charAt(0);
                            if (ch == '*' || Character.isDigit(ch)) {
                                sb.append(line);
                            }
                        }
                    }
                    if (sb.length() > 0) {
                        list.add(sb.toString());
                    }
                    threadsList = list.toArray(new String[list.size()]);
                    return threadsList;
                }
            }
        } else {
            return new String[]{ NbBundle.getMessage(GdbDebugger.class, "CTL_NoThreadInfoWhileRunning") }; // NOI18N
        }
        return threadsList;
    }

    public int getThreadCount() {
        return 1;
    }

    private void resetThreadInfo() {
        threadsList = emptyThreadsList;
    }

    private String getProgramName(String program) {
        StringBuilder programName = new StringBuilder();

        for (int i = 0; i < program.length(); i++) {
            if (program.charAt(i) == '\\') {
                programName.append('/');
            } else {
                if (program.charAt(i) == ' ') {
                    programName.append("\\ "); // NOI18N
                } else {
                    programName.append(program.charAt(i));
                }
            }
        }
        return pathMap.getRemotePath(programName.toString());
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(PROP_STATE)) {
            if (evt.getNewValue() == State.LOADING) {
                shareToken = gdb.info_share(false).getID();
            } else if (evt.getNewValue() == State.READY) {
                if (platform == PlatformTypes.PLATFORM_WINDOWS) {
                    gdb.break_insert("dlopen"); // NOI18N
                } else {
                    setStopOnSolibEvents(true);
                }
                if (continueAfterFirstStop) {
                    continueAfterFirstStop = false;
                    setRunning();
                } else {
                    gdb.stack_list_frames();
                    setStopped();
                }
            } else if (evt.getNewValue() == State.STOPPED) {
                updateLocalVariables(0);
                GdbContext.getInstance().gdbStep();
            } else if (evt.getNewValue() == State.SILENT_STOP) {
                interrupt();
            } else if (evt.getNewValue() == State.RUNNING &&
                    (evt.getOldValue() == State.SILENT_STOP ||
                    evt.getOldValue() == State.READY)) {
                gdb.exec_continue();
            } else if (evt.getNewValue() == State.EXITED) {
                finish(false);
            }
        } else if (evt.getPropertyName().equals(PROP_CURRENT_THREAD)) {
            updateCurrentCallStack();
            updateLocalVariables(0);
            GdbContext.getInstance().gdbStep();
        }
    }

    private boolean symbolsRead(String results, String exepath) {
        int pos = -1;
        for (String line : results.split("\\\\n")) { // NOI18N
            if (line.contains("Reading symbols from ") || // NOI18N
                    (platform == PlatformTypes.PLATFORM_MACOSX && line.contains("Symbols from "))) { // NOI18N
                if (platform == PlatformTypes.PLATFORM_WINDOWS && (pos = line.indexOf("/cygdrive/")) != -1) { // NOI18N
                    line = line.substring(0, pos) +
                            line.substring(pos + 10, pos + 11).toUpperCase() + ':' + line.substring(pos + 11);
                }
                String ep = line.substring(21, line.length() - 8);
                if (ep.equals(exepath) || (platform == PlatformTypes.PLATFORM_WINDOWS && ep.equals(exepath + ".exe"))) { // NOI18N
                    return true;
                }
            } else if (line.contains("Loaded symbols for ") && equivalentPaths(exepath, line.substring(19))) { // NOI18N
                return true;
            }
        }
        return false;
    }

    private boolean equivalentPaths(String path1, String path2) {
        if (platform == PlatformTypes.PLATFORM_WINDOWS) {
            return winpath(path1).equals(winpath(path2));
        }
        return path1.equals(path2);
    }

    private String winpath(String path) {
        if (platform == PlatformTypes.PLATFORM_WINDOWS && path.startsWith("/cygdrive/")) { // NOI18N
            return path.substring(10, 11).toUpperCase() + ':' + path.substring(11);
        } else {
            return path;
        }
    }

    private boolean symbolsReadFromInfoFiles(String results, String exepath) {
        for (String line : results.split("\\\\n")) { // NOI18N
            if (line.contains("Symbols from ")) { // NOI18N
                return compareExePaths(line.substring(15, line.length() - 3), exepath);
            }
        }
        return false;
    }

    /**
     * Compare the executable path from gdb (via the <i>info files</i> command to the executable path
     * from the project data. This comparison is <b>very</b> system dependent. For MacOS, all compares
     * are done in lower case. For Unix, they're done as-is. The most difficult platform is Windows,
     * where, depending on the gdb provider, I can see a Cygwin name, either forward or backward slashes,
     * either single or double backslashes (MinGW). The ".exe" may also be missing in some cases.
     *
     * @param exe1 The first path
     * @param exe2 The second path
     * @return True if exe1 and exe2 resolve to the same executable
     */
    private boolean compareExePaths(String exe1, String exe2) {
        if (platform == PlatformTypes.PLATFORM_WINDOWS) {
            return normalizeWindowsExe(exe1).equals(normalizeWindowsExe(exe2));
        } else if (platform == PlatformTypes.PLATFORM_MACOSX) {
            return exe1.toLowerCase().equals(exe2.toLowerCase());
        } else {
            return exe1.equals(exe2);
        }
    }

    private String normalizeWindowsExe(String exe) {
        String n_exe = winpath(exe).replace("\\\\", "/").replace("\\", "/"); // NOI18N
        if (n_exe.endsWith(".exe")) { // NOI18N
            return n_exe.substring(0, n_exe.length() - 4);
        } else {
            return n_exe;
        }
    }

    /**
     * Check that the executable matches the pid. This is system dependent and doesn't necessarily cause
     * an attach failure if we can't validate.
     *
     * @return true if the project matches the attached to executable
     */
    private boolean validAttachViaSlashProc(long pid, String exepath) {
        if (platform != PlatformTypes.PLATFORM_WINDOWS) {
            String procdir = "/proc/" + Long.toString(pid); // NOI18N
            File pathfile = new File(procdir, "path/a.out"); // NOI18N - Solaris only?
            if (!pathfile.exists()) {
                pathfile = new File(procdir, "exe"); // NOI18N - Linux?
            }
            if (pathfile.exists()) {
                File exefile = new File(exepath);
                if (exefile.exists()) {
                    String path = getPathFromSymlink(pathfile.getAbsolutePath());
                    if (path.equals(exefile.getAbsolutePath())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String getExePath(long pid) {
        if (platform != PlatformTypes.PLATFORM_WINDOWS && pid > 0) {
            String procdir = "/proc/" + Long.toString(pid); // NOI18N
            File pathfile = new File(procdir, "path/a.out"); // NOI18N - Solaris only?
            if (!pathfile.exists()) {
                pathfile = new File(procdir, "exe"); // NOI18N - Linux?
            }
            if (pathfile.exists()) {
                String path = getPathFromSymlink(pathfile.getAbsolutePath());
                if (path != null && path.length() > 0) {
                    return path;
                }
            }
        }
        return null;
    }

    private String getMacExePath() {
        String info = gdb.info_files().getResponse();
        for (String line : info.split("\\\\n")) { // NOI18N
            if (line.contains("Symbols from ")) { // NOI18N
                return line.substring(15, line.length() - 3);
            }
        }
        return null;
    }

    private String getMacDylibAddress(String path, String info) {
        String line;
        int start = info.startsWith("shlib-info=") ? 11 : 0; // NOI18N
        int next = info.indexOf(",shlib-info="); // NOI18N

        while ((line = info.substring(start, next > 0 ? next : info.length())) != null) {
            if (line.contains(path)) {
                return parseMacDylibAddress(line);
            }
            start = next + 12;
            next = info.indexOf(",shlib-info=", start); // NOI18N
        }
        return null;

    }

    private static String parseMacDylibAddress(String line) {
        int pos1 = GdbUtils.findMatchingCurly(line, 0);
        if (pos1 != -1) {
            Map<String, String> map = GdbUtils.createMapFromString(line.substring(1, pos1));
            if (map.containsKey("loaded_addr")) { // NOI18N
                return map.get("loaded_addr"); // NOI18N
            }
        }
        return null;
    }

    private static String getPathFromSymlink(String path) {
        File ls = new File("/bin/ls"); // NOI18N
        if (ls.isFile()) {
            List<String> list = new ArrayList<String>();
            list.add(ls.getAbsolutePath());
            list.add("-l"); // NOI18N
            list.add(path);
            ProcessBuilder pb = new ProcessBuilder(list);
            pb.redirectErrorStream(true);

            try {
                Process process = pb.start();
                BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line = br.readLine(); // just read 1st line...
                br.close();
                if (line != null) {
                    int pos = line.indexOf("->"); // NOI18N
                    if (pos > 0) {
                        return line.substring(pos + 2).trim();
                    }
                }
            } catch (IOException ioe) {
            }
        }
        return null;
    }

    public GdbProxy getGdbProxy() {
        return gdb;
    }

    /**
     * Finish debugging session. Terminates execution of the inferior program, exits debugger,
     * closes terminal and console.
     *
     * Note: gdb can be null if we get an exception while starting a debug session.
     */
    public synchronized void finish(boolean killTerm) {
        if (isUnitTest()) {
            setExited();
        } else {
            if (state != State.NONE && state != State.EXITED) {
                if (killTerm) {
                    firePropertyChange(PROP_KILLTERM, true, false);
                }
                if (gdb != null) {
                    ProjectActionEvent pae = lookupProvider.lookupFirst(null, ProjectActionEvent.class);
                    if (state == State.RUNNING) {
                        gdb.exec_interrupt();
                        if (pae.getType() != DEBUG_ATTACH) {
                            gdb.exec_abort();
                        }
                    }
                    if (pae.getType() == DEBUG_ATTACH) {
                        gdb.target_detach();
                    }
                    gdb.gdb_exit();
                    gdb.getProxyEngine().finish();
                }

                stackUpdate(new ArrayList<String>());
                setExited();
                programPID = 0;
                removeRTCBreakpoint();
                gdbEngineProvider.getDestructor().killEngine();
                GdbActionHandler gah = lookupProvider.lookupFirst(null, GdbActionHandler.class);
                if (gah != null) { // gah is null if we attached (but we don't need it then)
                    gah.executionFinished(0);
                }
                Disassembly.close();
                if (ioProxy != null) {
                    ioProxy.stop();
                }
                if (iotab != null) {
                    iotab.getOut().close();
                }
                GdbContext.getInstance().gdbExit();
                GdbTimer.getTimer("Step").reset(); // NOI18N
            }
        }
    }

    /**
     * The user has pressed the stop-out button while in the topmost function (main). gdb/mi
     * doesn't allow this and we've received an error. Set a temporary breakpoint in exit and
     * continue to the breakpoint. This will perform the action the user requested.
     */
    private void finish_from_main() {
        gdb.break_insert_temporary("exit"); // NOI18N
        gdb.exec_continue();
    }

    /** Sends request to get arguments and local variables */
    private void updateLocalVariables(int frame) {
        synchronized (LOCK) {
            synchronized (localVariables) {
                localVariables.clear(); // clear old variables so we can store new ones here
            }
            gdb.stack_select_frame(frame);
            gdb.stack_list_arguments(1, frame, frame);
            gdb.stack_list_locals("--all-values"); // NOI18N
        }
    }

    public void updateGdbVariable(String name, String value) {
        synchronized (localVariables) {
            for (GdbVariable var : localVariables) {
                if (name.equals(var.getName())) {
                    var.setValue(value);
                }
            }
        }
    }

    public void fireLocalsRefresh(Object node) {
        firePropertyChange(PROP_LOCALS_REFRESH, 0, node);
    }

    private void updateCurrentCallStack() {
        gdb.stack_list_frames();
    }

    /** Handle geb responses starting with '^' */
    public void resultRecord(int token, String msg) {
        currentToken = token + 1;
        if (msg.startsWith(DONE_PREFIX)) {
            // ^done should finish corresponding commandBuffer
            CommandBuffer cb = gdb.getCommandBuffer(token);
            if (cb != null) {
                // add message text to the buffer if any
                if (msg.length() > DONE_PREFIX.length()) {
                    cb.append(msg.substring(DONE_PREFIX.length() + 1));
                }
                cb.done();
            }

            if (msg.startsWith("^done,bkpt=")) { // NOI18N (-break-insert)
                msg = msg.substring(12, msg.length() - 1);
                Map<String, String> map = GdbUtils.createMapFromString(msg);
                boolean isTmp = breakpointValidation(token, map);
                if (!isTmp && getState() == State.SILENT_STOP && pendingBreakpointMap.isEmpty()) {
                    setRunning();
                }
            } else if (msg.startsWith("^done,stack=")) { // NOI18N (-stack-list-frames)
                if (state == State.STOPPED) { // Ignore data if we've resumed running
                    stackUpdate(GdbUtils.createListFromString((msg.substring(13, msg.length() - 1))));
                }
            } else if (msg.startsWith("^done,locals=")) { // NOI18N (-stack-list-locals)
                if (state == State.STOPPED) { // Ignore data if we've resumed running
                    addLocalsToLocalVariables(msg.substring(13));
                } else {
                    log.finest("GD.resultRecord: Skipping results from -stack-list-locals (not stopped)");
                }
            } else if (msg.startsWith("^done,stack-args=")) { // NOI18N (-stack-list-arguments)
                if (state == State.STOPPED) { // Ignore data if we've resumed running
                    addArgsToLocalVariables(msg.substring(17));
                } else {
                    log.finest("GD.resultRecord: Skipping results from -stack-list-arguments (not stopped)");
                }
            } else if (msg.startsWith("^done,new-thread-id=")) { // NOI18N (-thread-select)
                String tid = msg.substring(21, msg.indexOf('"', 22));
                if (!tid.equals(currentThreadID)) {
                    String otid = currentThreadID;
                    currentThreadID = tid;
                    log.finest("GD.resultRecord: Thread change, firing PROP_CURRENT_THREAD");
                    firePropertyChange(PROP_CURRENT_THREAD, otid, currentThreadID);
                }
            } else if (msg.startsWith("^done,value=") && msg.contains("auto; currently c")) { // NOI18N
                if (msg.contains("auto; currently c++")) { // NOI18N
                    cplusplus = true;
                    DebuggerManager.getDebuggerManager().getCurrentSession().setCurrentLanguage("C++"); // NOI18N
                }
            } else if (msg.startsWith(Disassembly.RESPONSE_HEADER)) {
                disassembly.update(msg);
            } else if (msg.startsWith(Disassembly.REGISTER_NAMES_HEADER)) {
                disassembly.updateRegNames(msg);
            } else if (msg.startsWith(Disassembly.REGISTER_VALUES_HEADER)) {
                disassembly.updateRegValues(msg);
            } else if (msg.startsWith(Disassembly.REGISTER_MODIFIED_HEADER)) {
                disassembly.updateRegModified(msg);
                GdbContext.getInstance().setProperty(GdbContext.PROP_REGISTERS, disassembly.getRegisterValues());
            } else { // NOI18N
                if (cb != null) {
                    if (token == shareToken) {
                        shareTab = createShareTab(cb.getResponse());
                    }
                } else if (pendingBreakpointMap.get(token) != null) {
                    breakpointValidation(token, null);
                }
            }
        // end of ^done block
        } else if (msg.startsWith("^running") && getState() == State.STOPPED) { // NOI18N
            setRunning();
        } else if (msg.startsWith("^error,msg=")) { // NOI18N
            msg = msg.substring(11);
            CommandBuffer cb = gdb.getCommandBuffer(token);

            if (cb != null) {
                cb.error(msg);
            } else if (msg.equals("\"Can't attach to process.\"")) { // NOI18N
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                        "ERR_CantAttach"))); // NOI18N
                (lookupProvider.lookupFirst(null, Session.class)).kill();
            } else if (msg.startsWith("\"No symbol ") && msg.endsWith(" in current context.\"")) { // NOI18N
                String type = msg.substring(13, msg.length() - 23);
                log.warning("Failed type lookup for " + type);
            } else if (msg.equals("\"\\\"finish\\\" not meaningful in the outermost frame.\"")) { // NOI18N
                finish_from_main();
            } else if (msg.contains("(corrupt stack?)") && gdbVersion > 6.3) { // NOI18N
                // corrupted stack should not stop the debugging on old gdbs, see issue 151472
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                        "ERR_CorruptedStack"))); // NOI18N
                (lookupProvider.lookupFirst(null, Session.class)).kill();
            } else if (msg.contains("error reading line numbers")) { // NOI18N
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                        "ERR_CantReadLineNumbers"))); // NOI18N
            } else if (msg.contains("No symbol table is loaded")) { // NOI18N
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                        "ERR_NoSymbolTable"))); // NOI18N
                (lookupProvider.lookupFirst(null, Session.class)).kill();
            } else if (msg.contains("Cannot access memory at address")) { // NOI18N
                // ignore - probably dereferencing an uninitialized pointer
            } else if (msg.contains("mi_cmd_break_insert: Garbage following <location>")) { // NOI18N
                // ignore - probably a breakpoint from another project
            } else if (msg.contains("Undefined mi command: ") && msg.contains("(missing implementation")) { // NOI18N
                // ignore - gdb/mi defines commands which haven't been implemented yet
            } else if (msg.contains(MSG_BREAKPOINT_ERROR)) { // NOI18N
                setStopped();
                int start = msg.indexOf(MSG_BREAKPOINT_ERROR) + MSG_BREAKPOINT_ERROR.length();
                int end = msg.indexOf('.', start); // NOI18N
                if (end != -1) {
                    String breakpoinIdx = msg.substring(start, end).trim();
                    BreakpointImpl breakpoint = findBreakpoint(breakpoinIdx);
                    if (breakpoint != null) {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                                NbBundle.getMessage(GdbDebugger.class, "ERR_InvalidBreakpoint", breakpoint.getBreakpoint())));
                        breakpoint.getBreakpoint().disable();
                    }
                }
            } else if (pendingBreakpointMap.containsKey(token)) {
                BreakpointImpl breakpoint = pendingBreakpointMap.remove(token);
                if (breakpoint != null) {
                    breakpoint.addError(msg);
                    breakpoint.completeValidation(null);
                }
                if (pendingBreakpointMap.isEmpty() && state == State.LOADING) {
                    setReady();
                }
            } else if (state != State.NONE) {
                // ignore errors after we've terminated (they could have been in the input queue)
                log.warning("Unexpected gdb error: " + msg);
            }
        }
    }

    public void fireDisUpdate(boolean open) {
        firePropertyChange(DIS_UPDATE, open, !open);
    }

    /** Handle gdb responses starting with '*' */
    public void execAsyncOutput(int token, String msg) {
        Map<String, String> map;

        if (msg.startsWith("*stopped")) { // NOI18N
            if (msg.length() > 9) {
                map = GdbUtils.createMapFromString(msg.substring(9));
            } else {
                map = new HashMap<String, String>();
            }
            stopped(token, map);
        }
    }

    /** Handle gdb responses starting with '~' */
    public void consoleStreamOutput(int token, String omsg) {
        String msg;

        if (omsg.endsWith("\\n")) { // NOI18N
            msg = omsg.substring(0, omsg.length() - 2);
        } else {
            msg = omsg;
        }

        CommandBuffer cb = gdb.getCommandBuffer(token);
        if (cb != null) {
            cb.append(omsg);
        } else if (msg.toLowerCase().contains("mingw")) { // NOI18N
            mingw = true;
        } else if (msg.startsWith("Breakpoint ") && msg.contains(" at 0x")) { // NOI18N
            // Due to a gdb bug (6.6 and earlier) we use a "break" command for multi-byte filenames
            int pos = msg.indexOf(' ', 12);
            String num = msg.substring(11, pos);
            Map<String, String> map = new HashMap<String, String>();
            map.put("number", num); // NOI18N
            boolean isTmp = breakpointValidation(token, map);
            if (!isTmp && getState() == State.SILENT_STOP && pendingBreakpointMap.isEmpty()) {
                setRunning();
            }
        } else if (msg.contains("(no debugging symbols found)") && state == State.STARTING && !isAttaching()) { // NOI18N
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                    "ERR_NoDebuggingSymbolsFound"))); // NOI18N
            finish(false);
        } else if (msg.startsWith("gdb: unknown target exception")) { // NOI18N
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                    "ERR_UnknownTargetException"))); // NOI18N
            finish(false);
        } else if (msg.startsWith("Copyright ") || // NOI18N
                msg.startsWith("GDB is free software,") || // NOI18N
                msg.startsWith("welcome to change it and") || // NOI18N
                msg.contains("show copying") || // NOI18N
                msg.startsWith("There is absolutely no warranty for GDB") || // NOI18N
                msg.startsWith("Source directories searched: ") || // NOI18N
                msg.contains("LC_SEGMENT.__TEXT_addr = ") || // NOI18N
                msg.startsWith("This GDB was configured as")) { // NOI18N
            // do nothing (but don't print these expected messages)
        } else if (programPID == 0 && msg.startsWith("process ")) { // NOI18N (Unix method)
            int pos = msg.indexOf(' ', 8);
            String text;
            if (pos > 0) {
                text = msg.substring(8, pos);
            } else {
                text = msg.substring(8);
            }
            try {
                programPID = Long.parseLong(text);
            } catch (NumberFormatException ex) {
                log.warning("Failed to get PID from \"info proc\""); // NOI18N
            }
        } else if (programPID == 0) {
            if (msg.startsWith("* 1 thread ")) { // NOI18N
                int pos = msg.indexOf('.');
                if (pos > 0) {
                    try {
                        programPID = Long.valueOf(msg.substring(11, pos));
                    } catch (NumberFormatException ex) {
                        log.warning("Failed to get PID from \"info threads\""); // NOI18N
                    }
                }
            } else if (msg.startsWith("[Switching to process ")) { // NOI18N
                int pos = msg.indexOf(' ', 22);
                if (pos > 0) {
                    try {
                        programPID = Long.valueOf(msg.substring(22, pos));
                    } catch (NumberFormatException ex) {
                    }
                }
            }
        } else if (msg.startsWith("Stopped due to shared library event")) { // NOI18N
            dlopenPending = true;
        }
    }

    /** Handle gdb responses starting with '&' */
    public void logStreamOutput(String msg) {
        if (msg.startsWith("&\"No source file named ")) {  // NOI18N
            breakpointValidation(currentToken, msg.substring(2, msg.length() - 3));
        } else if (msg.startsWith("&\"info proc") || // NOI18N
                msg.startsWith("&\"info threads") || // NOI18N
                msg.startsWith("&\"directory ") || // NOI18N
                msg.startsWith("&\"set new-console") || // NOI18N
                msg.startsWith("&\"whatis ") || // NOI18N
                msg.startsWith("&\"warning: Temporarily disabling breakpoints for unloaded shared library") || // NOI18N
                msg.contains("/usr/lib/ld.so")) { // NOI18N
            // ignore these messages
        } else {
            log.finest("GD.logStreamOutput: " + msg); // NOI18N
        }
    }

    /** Handle gdb responses starting with '+' */
    public void statusAsyncOutput(int token, String msg) {
        log.finest("GD.statusAsyncOutput[" + token + "]: " + msg); // NOI18N
    }

    /** Handle gdb responses starting with '=' */
    public void notifyAsyncOutput(int token, String msg) {
        log.finest("GD.notifyAsyncOutput[" + token + "]: " + msg); // NOI18N
    }

    /** Handle gdb responses starting with '@' */
    public void targetStreamOutput(String msg) {
        log.finest("GD.targetStreamOutput: " + msg);  // NOI18N
    }

    /**
     * Handle gdb output. The only tricking thing here is that most versions of gdb on
     * Solaris output some proc flags to stdout. So for Solaris, I skip the 1st output
     * if it starts with "PR_" (the proc flag header).
     */
    public void output(String msg) {
        if (iotab != null) {
            if (msg.contains("PR_SYSEXIT")) { // NOI18N
                DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                        "ERR_ExitedFromSYSEXIT"))); // NOI18N
                finish(true);
            }
            if (!(firstOutput && isSolaris() && msg.startsWith("PR_"))) { // NOI18N
                firstOutput = false;
                iotab.getOut().println(msg);
            }
        }
    }

    private void addArgsToLocalVariables(String info) {
        int pos;
        if (info.startsWith("[frame={level=") && (pos = info.indexOf(",args=[")) > 0 && info.endsWith("]}]")) { // NOI18N
            info = info.substring(pos + 7, info.length() - 3);
        } else if (platform == PlatformTypes.PLATFORM_MACOSX &&
                info.startsWith("{frame={level=") && (pos = info.indexOf(",args={")) > 0 && info.endsWith("}}}")) { // NOI18N
            info = info.substring(pos + 7, info.length() - 3);
        }
        Collection<GdbVariable> v = GdbUtils.createArgumentList(info);
        if (!v.isEmpty()) {
            log.finest("GD.addArgsToLocalVariables: Starting to add Args to localVariables"); // NOI18N
            synchronized (localVariables) {
                localVariables.addAll(v);
            }
            log.finest("GD.addArgsToLocalVariables: Added " + v.size() + " args");
        }
    }

    private void addLocalsToLocalVariables(String info) {
        Collection<GdbVariable> v = GdbUtils.createLocalsList(info.substring(1, info.length() - 1));
        if (!v.isEmpty()) {
            log.finest("GD.addLocalsToLocalVariables: Starting to add locals to localVariables"); // NOI18N
            synchronized (localVariables) {
                for (GdbVariable var : v) {
                    if (!localVariables.contains(var)) {
                        localVariables.add(var);
                    }
                }
            }
            log.finest("GD.addLocalsToLocalVariables: Added " + v.size() + " locals");
        }
    }

    public String updateVariable(String name, String value) {
        return evaluate(name + '=' + value);
    /*CommandBuffer cb = new CommandBuffer(gdb);
    gdb.data_evaluate_expression(cb, name + '=' + value);
    return cb.waitForCompletion();*/
    }

    public void variableChanged(Object var) {
        firePropertyChange(PROP_VALUE_CHANGED, null, var);
    }

    // currently not called - should do more than set state (see JPDADebuggerImpl)
    public void suspend() {
        setState(State.STOPPED);
    }

    /**
     * Interrupts execution of the inferior program.
     * This method is called when "Pause" button is pressed.
     *
     * @return null if action is accepted, otherwise return error message
     */
    public void interrupt() {
        gdb.exec_interrupt();
    }

    /**
     * Send a kill command to the debuggee.
     *
     * @param signal The signal to send (as defined by "kill -l")
     */
    public void kill(int signal) {
        if (programPID > 0) { // Never send a kill if PID is 0
            kill(signal, programPID);
        }
    }

    /**
     * Send a kill command to the debuggee.
     *
     * @param signal The signal to send (as defined by "kill -l")
     * @param pid The process ID to send the signal to
     */
    public void kill(int signal, long pid) {
        if (pid > 0) {
            ArrayList<String> killcmd = new ArrayList<String>();
            File f;

            if (platform == PlatformTypes.PLATFORM_WINDOWS) {
                f = InstalledFileLocator.getDefault().locate("bin/GdbKillProc.exe", null, false); // NOI18N
                if (f.exists()) {
                    killcmd.add(f.getAbsolutePath());
                }
            } else {
                f = new File("/usr/bin/kill"); // NOI18N
                if (f.exists()) {
                    killcmd.add(f.getAbsolutePath());
                } else {
                    f = new File("/bin/kill"); // NOI18N
                    if (f.exists()) {
                        killcmd.add(f.getAbsolutePath());
                    }
                }
            }
            if (!killcmd.isEmpty()) {
                killcmd.add("-s"); // NOI18N

                String signalName = Integer.toString(signal);
                // for MacOS we should substitute signal number with the real name
                if (platform == PlatformTypes.PLATFORM_MACOSX) {
                    switch (signal) {
                        case 2:
                            signalName = "TRAP"; // NOI18N
                            break;
                        case 15:
                            signalName = "TERM"; // NOI18N
                            break;
                        default:
                            assert false : "No textual value for MacOS signal " + signal + ", please add it to kill command in GdbDebugger.";// NOI18N
                    }
                }
                killcmd.add(signalName);

                killcmd.add(Long.toString(pid));
                ProcessBuilder pb = new ProcessBuilder(killcmd);
                try {
                    pb.start();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                gdb.getLogger().logMessage("External Command: " + killcmd.toString()); // NOI18N
            }
        }
    }

    /**
     * Resumes execution of the inferior program, until a
     * breakpoint is encountered, or until the inferior exits.
     */
    public void resume() {
        setState(State.RUNNING);
        gdb.exec_continue();
    }

    /**
     * Resumes execution of the inferior program, stopping when the beginning of the
     * next source line is reached, if the next source line is not a function call.
     * If it is, stop at the first instruction of the called function.
     */
    public void stepInto() {
        GdbTimer.getTimer("Step").start("Step1", 10); // NOI18N
        setState(State.RUNNING);
        gdb.exec_step();
    }

    /**
     * Resumes execution of the inferior program, stopping
     * when the beginning of the next source line is reached.
     */
    public void stepOver() {
        setState(State.RUNNING);
        gdb.exec_next();
    }

    /**
     * Step one instruction
     */
    public void stepI() {
        setState(State.RUNNING);
        gdb.exec_step_instruction();
    }

    /**
     * Step over function inside dis
     */
    public void stepOverInstr() {
        setState(State.RUNNING);
        gdb.exec_next_instruction();
    }

    /**
     * Resumes execution of the inferior program until
     * the top function is exited.
     * Note: Slight cemantic change in NB 6.1. In NB 6.0 the current
     * frame was stepped out of. In NB 6.1, the top frame is stepped
     * out of. This makes the behavior match the Java debugger in NB.
     */
    public void stepOut() {
        if (!callstack.isEmpty() || callstack.get(1).isValid()) {
            setState(State.RUNNING);
            gdb.stack_select_frame(0);
            gdb.exec_finish();
        } else {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                    "ERR_InvalidCallStackFrame"))); // NOI18N
        }
    }

    private LineBreakpoint rtcBreakpoint = null;
    /**
     * Set the temporary breakpoint at the current line and continue execution
     */
    public void runToCursor() {
        // TODO: better use until debugger command
        String file = EditorContextBridge.getContext().getCurrentURL();
        if (file.length() == 0) {
            return;
        }
        int line = EditorContextBridge.getContext().getCurrentLineNumber();
        if (line < 0) {
            return;
        }
        removeRTCBreakpoint();
        rtcBreakpoint = LineBreakpoint.create(file, line);
        rtcBreakpoint.setTemporary();
        rtcBreakpoint.setHidden(true);
        DebuggerManager.getDebuggerManager().addBreakpoint(rtcBreakpoint);
        resume();
    }

    private void removeRTCBreakpoint() {
        if (rtcBreakpoint != null) {
            DebuggerManager.getDebuggerManager().removeBreakpoint(rtcBreakpoint);
            rtcBreakpoint = null;
        }
    }

    /**
     * Returns current state of gdb debugger.
     *
     * @return current state of gdb debugger
     */
    public State getState() {
        return state;
    }

    private void setState(State state) {
        if (state == this.state) {
            return;
        }
        State oldState = this.state;
        this.state = state;
        firePropertyChange(PROP_STATE, oldState, state);
    }

    private void setStarting() {
        setState(State.STARTING);
    }

    private void setLoading() {
        setState(State.LOADING);
    }

    public void setReady() {
        setState(State.READY);
    }

    public void setRunning() {
        setState(State.RUNNING);
    }

    private void setStopped() {
        setState(State.STOPPED);
    }

    public void setSilentStop() {
        setState(State.SILENT_STOP);
    }

    private void setExited() {
        setState(State.EXITED);
    }

    /*public Boolean evaluateIn(Expression expression, final Object frame) {
    return Boolean.FALSE;
    }*/
    /**
     * Helper method that fires JPDABreakpointEvent on JPDABreakpoints.
     *
     * @param breakpoint a breakpoint to be changed
     * @param event a event to be fired
     */
    public void fireBreakpointEvent(GdbBreakpoint breakpoint, GdbBreakpointEvent event) {
        breakpoint.fireGdbBreakpointChange(event);
    }

    /**
     * Called from GdbProxy when the target debuggee is stopped.
     *
     * Note: The token parameter isn't used but is useful for conditional
     * breakpoints during debugging...
     *
     * @param token The token responsible for this stop
     * @param reason A reason why program is stopped
     */
    public void stopped(int token, Map<String, String> map) {
        String reason = map.get("reason"); // NOI18N

        if (state == State.STARTING) {
            String frame = map.get("frame"); // NOI18N
            if (frame != null) {
                map = GdbUtils.createMapFromString(frame);
                String fullname = map.get("fullname"); // NOI18N
                String line = map.get("line"); // NOI18N
                if (fullname != null && line != null) {
                    lastStop = fullname + ":" + line; // NOI18N
                }
            }
            setLoading();
            return;
        }
        if (state != State.RUNNING) {
            if (isUnitTest()) {
                if (state == State.NONE && reason == null) { // initial stop in main
                    state = State.STOPPED;
                    tlog("GdbDebugger.stopped: Initial stop (main). Setting state to STOPPED"); // NOI18N
                } else {
                    tlog("GdbDebugger.stopped: Stopped with state " + state + " and reason " + reason); // NOI18N
                }
            } else {
                log.warning("GdbDebugger.stopped: Ignoring stop while in state " + state);
            }
            return;
        }

        log.finest("GD.stopped[" + GdbUtils.threadId() + "]:\n"); // NOI18N
        tlog("GD.stopped: Reason is " + reason); // NOI18N
        resetThreadInfo();
        if (reason != null) {
            setCurrentCallStackFrameNoFire(null);   // will be reset when stack updates
            if (reason.equals("exited-normally")) { // NOI18N
                finish(false);
            } else if (reason.equals("breakpoint-hit")) { // NOI18N
                String tid = map.get("thread-id"); // NOI18N
                lastStop = null;
                if (tid != null && !tid.equals(currentThreadID)) {
                    currentThreadID = tid;
                }
                BreakpointImpl impl = findBreakpoint(map.get("bkptno")); // NOI18N
                if (impl == null) {
                    int idx = temporaryBreakpoints.indexOf(map.get("bkptno")); // NOI18N
                    if (idx >= 0) {
                        temporaryBreakpoints.remove(idx);
                        if (platform == PlatformTypes.PLATFORM_MACOSX) {
                            updateCurrentCallStack();
                            setStopped(); // stepping out of dlopen
                        }
                    } else {
                        // So far this only happens in one case on a Mac. Its stopping at a bp
                        // we didn't set and gdb doesn't show in a breakpoint list. Just continue
                        // on when this happens. If it were a bp we were interested about, it would
                        // either have an impl (which means its a NetBeans breakpoint) or it would
                        // be in temporaryBreakpoints (which means we set it but its not a visible
                        // bp wrt NetBeans).
                        log.warning("GD.stopped: Stopped at unknown breakpoint");
                        gdb.exec_continue();
                    }
                    String frame = map.get("frame"); // NOI18N
                    if (frame != null && frame.contains("func=\"dlopen\"")) { // NOI18N
                        dlopenPending = true;
                        LastGoState saveLastGo = lastGo;
                        gdb.exec_finish();
                        setLastGo(saveLastGo); // exec_finish changes lastGo
                        return;
                    }
                } else {
                    GdbBreakpoint breakpoint = impl.getBreakpoint();
                    if (breakpoint.getSuspend() == GdbBreakpoint.SUSPEND_NONE && lastGo == LastGoState.CONTINUE) {
                        fireBreakpointEvent(breakpoint, new GdbBreakpointEvent(
                                breakpoint, this, GdbBreakpointEvent.CONDITION_NONE, null));
                        gdb.exec_continue();
                    } else {
                        updateCurrentCallStack();
                        fireBreakpointEvent(breakpoint, new GdbBreakpointEvent(
                                breakpoint, this, GdbBreakpointEvent.CONDITION_NONE, null));
                        setStopped();
                    }
                }
                if (dlopenPending) { // who stops here? (Linux - see IZ #145868)
                    dlopenPending = false;
                    checkSharedLibs();
                }
                String frame = map.get("frame"); // NOI18N
                if (frame != null) {
                    map = GdbUtils.createMapFromString(frame);
                    String fullname = map.get("fullname"); // NOI18N
                    String line = map.get("line"); // NOI18N
                    if (fullname != null && line != null) {
                        lastStop = fullname + ":" + line; // NOI18N
                    }
                }
                GdbTimer.getTimer("Startup").stop("Startup1"); // NOI18N
                GdbTimer.getTimer("Startup").report("Startup1"); // NOI18N
                GdbTimer.getTimer("Startup").free(); // NOI18N
                GdbTimer.getTimer("Stop").mark("Stop1");// NOI18N
            } else if (reason.equals("exited-signalled")) { // NOI18N
                String signal = map.get("signal-name"); // NOI18N
                if (signal != null) {
                    DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                            "ERR_ExitedFromSignal", signal))); // NOI18N
                    finish(false);
                }
            } else if (reason.equals("end-stepping-range")) { // NOI18N
                lastStop = null;
                updateCurrentCallStack();
                setStopped();
                String frame = map.get("frame"); // NOI18N
                if (frame != null) {
                    map = GdbUtils.createMapFromString(frame);
                    String fullname = map.get("fullname"); // NOI18N
                    if (platform == PlatformTypes.PLATFORM_WINDOWS && isCygwin() && fullname != null && fullname.charAt(0) == '/') {
                        if (fullname.length() >= 5 && fullname.startsWith("/usr/")) { // NOI18N
                            fullname = CompilerSetManager.getCygwinBase() + fullname.substring(4);
                        } else {
                            fullname = CompilerSetManager.getCygwinBase() + fullname;
                        }
                    }
                    String line = map.get("line"); // NOI18N
                    if (fullname != null && line != null) {
                        lastStop = fullname + ":" + line; // NOI18N
                    }
                }
                if (GdbTimer.getTimer("Step").getSkipCount() == 0) { // NOI18N
                    GdbTimer.getTimer("Step").stop("Step1");// NOI18N
                    GdbTimer.getTimer("Step").report("Step1");// NOI18N
                }
            } else if (reason.equals("shlib-event")) { // NOI18N
                checkSharedLibs();
            } else if (reason.equals("signal-received")) { // NOI18N
                if (getState() == State.RUNNING) {
                    String tid = map.get("thread-id"); // NOI18N
                    if (tid != null && !tid.equals(currentThreadID)) {
                        currentThreadID = tid;
                    }
                    sig = map.get("signal-name"); // NOI18N
                    gdb.stack_list_frames();
                    setStopped();
                }
            } else if (reason.equals("function-finished") && dlopenPending) { // NOI18N
                dlopenPending = false;
                checkSharedLibs(); // Windows (after non-user -exec-finish after non-user breakpoint in dlopen)
            } else {
                if (!reason.startsWith("exited")) { // NOI18N
                    gdb.stack_list_frames();
                    setStopped();
                } else {
                    setStopped();
                    // Disable debugging buttons
                    finish(false);
                }
            }
        } else if (dlopenPending) {
            dlopenPending = false;
            checkSharedLibs(); // Solaris (stopping for solib event)
        } else {
            gdb.stack_list_frames();
            setStopped();
        }
    }

    private Map<String, ShareInfo> createShareTab(String info) {
        Map<String, ShareInfo> shtab = new HashMap<String, ShareInfo>();
        String path, addr;

        if (platform == PlatformTypes.PLATFORM_MACOSX) {
            Map<String, String> map;
            int start = 0;
            int next;

            while ((next = info.indexOf("shlib-info=", start + 1)) > 0) { // NOI18N
                map = GdbUtils.createMapFromString(info.substring(start + 12, next - 2));
                path = map.get("path"); // NOI18N
                addr = map.get("dyld-addr"); // NOI18N
                if (path != null && addr != null) {
                    shtab.put(path, new ShareInfo(path, addr));
                }
                start = next;
            }
            map = GdbUtils.createMapFromString(info.substring(start + 12, info.length() - 1));
            path = map.get("path"); // NOI18N
            addr = map.get("dyld-addr"); // NOI18N
            if (path != null && addr != null) {
                shtab.put(path, new ShareInfo(path, addr));
            }
        } else {
            for (String line : info.split("\\\\n")) { // NOI18N
                if (line.length() > 0 && line.charAt(0) == '0') {
                    String[] s = line.split("\\s+", 4); // NOI18N
                    shtab.put(s[3], new ShareInfo(s[3], s[0]));
                }
            }
        }
        return shtab;
    }

    /**
     * Compare the current set of shared libraries with the previous set. Run in a
     * different thread because we're probably being called from the GdbReaderRP
     * thread and CommandBuffer.waitForCompletion() doesn't work on that thread.
     */
    private void checkSharedLibs() {
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                String share = gdb.info_share(true).getResponse();
                Map<String, ShareInfo> nuTab = createShareTab(share);
                if (nuTab.size() > shareTab.size()) {
                    // dlopened a shared library
                    log.fine("GD.checkSharedLibs: Added a shared library");
                    ShareInfo si = getNewShareInfo(nuTab);
                    if (si.isMatchingProject()) {
                        gdb.environment_directory(si.getSourceDirectories());
                    }
                    firePropertyChange(PROP_SHARED_LIB_LOADED, null, si.getPath());
                } else {
                    // dlclosed a shared library
                    log.fine("GD.checkSharedLibs: Closed a shared library");
                }
                shareTab = nuTab;
                if (lastGo == LastGoState.CONTINUE) {
                    gdb.exec_continue();
                } else {
                    stepOutOfDlopen();
                }
            }
        });
    }

    private ShareInfo getNewShareInfo(Map<String, ShareInfo> map) {
        for (Map.Entry<String, ShareInfo> entry : map.entrySet()) {
            if (!shareTab.containsKey(entry.getKey())) {
                return entry.getValue();
            }
        }
        assert false : "No new shared library found";
        return null;
    }

    /**
     * We've stopped from a dlopen event while stepping. We need to go to the next
     * line of code. If we have a valid stack trace, its trivial. But on systems where
     * we don't (Linux) we use the lastStep field and set a temporary breakpoint on
     * the line following it. This is a bit of a hack, but its required if we don't
     * have a valid stack.
     */
    private void stepOutOfDlopen() {
        State oldState = state;
        setSilentStop();

        String msg = gdb.stack_list_framesEx().getResponse();
        // response have the form ",stack=...", so we trim it
        msg = msg.substring(7, msg.length() - 1);

        int i = 0;
        boolean valid = true;
        boolean checkNextFrame = false;

        for (String frame : GdbUtils.createListFromString(msg)) {
            Map<String, String> map = GdbUtils.createMapFromString(frame.substring(6, frame.length()));
            String func = map.get("func"); // NOI18N
            if (func != null && func.equals("dlopen") && !checkNextFrame) { // NOI18N
                if (platform == PlatformTypes.PLATFORM_MACOSX) {
                    checkNextFrame = true;
                } else {
                    gdb.stack_select_frame(i);
                    setStopOnSolibEvents(false);
                    gdb.exec_finish();
                    setStopOnSolibEvents(true);
                    gdb.exec_next();
                    state = oldState;
                    return;
                }
            } else {
                String fullname = map.get("fullname"); // NOI18N
                if (fullname != null) {
                    File file = new File(getOSPath(fullname));
                    valid = file != null && file.exists();
                    if (valid && checkNextFrame) {
                        String line = map.get("line"); // NOI18N
                        if (line != null) {
                            int lnum = Integer.parseInt(line) + 1;
                            gdb.break_insert_temporary(fullname + ":" + lnum); // NOI18N
                            gdb.exec_continue();
                            return;
                        }
                    }
                } else {
                    valid = false;
                }
            }
            i++;
        }
        if (valid) {
            gdb.exec_next();
        } else if (lastStop != null) {
            int pos = lastStop.lastIndexOf(':');
            int lnum = Integer.parseInt(lastStop.substring(pos + 1)) + 1;
            gdb.break_insert_temporary(lastStop.substring(0, pos + 1) + lnum);
            gdb.exec_continue();
        }
        state = oldState;
    }

    private void setStopOnSolibEvents(boolean enable) {
        if (gdbVersion < 6.8) {
            gdb.gdb_set("stop-on-solib-event", enable ? "1" : "0"); // NOI18N
        }
    }

    private String getOSPath(String path) {
        if (platform == PlatformTypes.PLATFORM_WINDOWS) {
            if (isCygwin() && path.startsWith("/cygdrive/")) { // NOI18N
                return path.charAt(10) + ":" + path.substring(11); // NOI18N
            } else if (isMinGW() && path.charAt(0) == '/' && path.charAt(2) == '/') {
                return path.charAt(1) + ":" + path.substring(2); // NOI18N
            } else {
                return path;
            }
        } else {
            return path;
        }
    }

    private void threadsViewInit() {
        Properties props = Properties.getDefault().getProperties("debugger").getProperties("views"); // NOI18N
        props.getProperties("ThreadState").setBoolean("visible", false); // NOI18N
        props.getProperties("ThreadSuspended").setBoolean("visible", false); // NOI18N
    }

    public void addPendingBreakpoint(int token, BreakpointImpl impl) {
        pendingBreakpointMap.put(token, impl);
    }

    /**
     * Callback method for break_insert Gdb/MI command.
     *
     * @param reason a reason why program is stopped
     */
    private boolean breakpointValidation(int token, Object o) {
        BreakpointImpl impl = pendingBreakpointMap.get(token);

        if (impl != null) { // impl is null for the temporary bp set at main during startup
            if (o instanceof String) {
                impl.addError((String) o);
            } else if (o instanceof Map || o == null) {
                pendingBreakpointMap.remove(token);
                @SuppressWarnings("unchecked")
                Map<String, String> map = (Map<String, String>)o;
                impl.completeValidation(map);
                if (map != null && impl.getBreakpoint().getValidity() == Breakpoint.VALIDITY.VALID && impl.getBreakpoint().isEnabled()) {
                    String fullname = map.get("fullname"); // NOI18N
                    String file = map.get("file"); // NOI18N
                    String line = map.get("line"); // NOI18N
                    if (firstBPfullname != null && firstBPfullname.equals(fullname) &&
                            firstBPline != null && firstBPline.equals(line)) {
                        continueAfterFirstStop = false;
                    } else if (platform == PlatformTypes.PLATFORM_MACOSX &&
                            firstBPfile != null && firstBPfile.equals(file) &&
                            firstBPline != null && firstBPline.equals(line)) {
                        continueAfterFirstStop = false;
                    }
                }
            }
            if (pendingBreakpointMap.isEmpty() && state == State.LOADING) {
                setReady();
            } else if (impl.isRunWhenValidated()) {
                impl.setRunWhenValidated(false);
                setRunning();
            }
            return true;
        } else if (o instanceof Map) { // temporary breakpoints aren't NetBeans breakpoints...
            @SuppressWarnings("unchecked")
            Map<String, String> map = (Map<String, String>) o;
            String number = map.get("number"); // NOI18N
            String fullname = map.get("fullname"); // NOI18N
            String file = map.get("file"); // NOI18N
            String line = map.get("line"); // NOI18N
            String func = map.get("func"); // NOI18N
            if (number != null && ((number.equals("1")) || // NOI18N
                    (number.equals("2") && func != null && func.equals("WinMain") && platform == PlatformTypes.PLATFORM_WINDOWS))) { // NOI18N
                firstBPfullname = fullname;
                firstBPfile = file;
                firstBPline = line;
            }
            temporaryBreakpoints.add(number);
        }
        return false;
    }

    /**
     * Start the attach process.
     *
     * @param pid The process ID
     * @param pinfo Miscelaneous project information
     */
    public static void attach(String pid, ProjectInformation pinfo) throws DebuggerStartException {
        Project project = pinfo.getProject();
        ConfigurationDescriptorProvider cdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfigurationDescriptor mcd = (MakeConfigurationDescriptor) cdp.getConfigurationDescriptor();
        
        if (mcd != null) {
            MakeConfiguration conf = (MakeConfiguration) mcd.getConfs().getActive();
            String path = getExecutableOrSharedLibrary(pinfo, conf);

            if (path != null) {
                ProjectActionEvent pae = new ProjectActionEvent(project,
                        ProjectActionEvent.Type.CHECK_EXECUTABLE, pinfo.getDisplayName(), path, conf, null, false);
                DebuggerEngine[] es = DebuggerManager.getDebuggerManager().startDebugging(
                        DebuggerInfo.create(SESSION_PROVIDER_ID, new Object[] { pae, Long.valueOf(pid) }));
                if (es == null) {
                    throw new DebuggerStartException(new InternalError());
                }
            } else {
                String buildResult = conf.getMakefileConfiguration().getOutput().getValue();
                final String msg;

                if (buildResult.length() > 0) {
                    if (isAbsolute(conf, buildResult)) {
                        msg = NbBundle.getMessage(GdbDebugger.class, "ERR_InvalidBuildResult", buildResult); // NOI18N
                    } else {
                       msg = NbBundle.getMessage(GdbDebugger.class, "ERR_RelativePathInBuildResult"); // NOI18N
                    }
                } else {
                    msg = NbBundle.getMessage(GdbDebugger.class, "ERR_NoBuildResult"); // NOI18N
                }
                DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(msg));
            }
        }
    }

    private static String getExecutableOrSharedLibrary(ProjectInformation pinfo, MakeConfiguration conf) {
        String buildResult = getBuildResult(pinfo, conf);
        return isExecutableOrSharedLibrary(conf, buildResult) ? buildResult : null;
    }

    /**
     * Return the project's Build Result. If empty, use project system APIs to do the same
     * popup to set it that the Run or Debug actionw do.
     *
     * @return The Build Result property from the project's configuration
     */
    private static String getBuildResult(ProjectInformation pinfo, MakeConfiguration conf) {
        String path = conf.getAbsoluteOutputValue().replace("\\", "/"); // NOI18N

        if (path.length() == 0) {
            ProjectActionEvent pae = new ProjectActionEvent(pinfo.getProject(),
                    ProjectActionEvent.Type.CHECK_EXECUTABLE, pinfo.getDisplayName(), path, conf, null, false);
            ProjectActionSupport.getInstance().fireActionPerformed(new ProjectActionEvent[] { pae });
            path = conf.getAbsoluteOutputValue().replace("\\", "/"); // NOI18N
        }
        return path;
    }

    private static boolean isAbsolute(MakeConfiguration conf, String path) {
        int platform = conf.getPlatform().getValue();
        if (platform == PlatformTypes.PLATFORM_WINDOWS) {
            return path.length() > 2 && path.charAt(1) == ':' && path.charAt(2) == '/';
        } else {
            return path.length() > 0 && path.charAt(0) == '/';
        }
    }

    /**
     * Use various heuristics to verify that either the project produces an executable
     * or that the path is to an executable.
     *
     * @param conf A Makefile project configuration
     * @param path The absolute pathname to the file
     * @return true iff the input parameters get an executable
     */
    private static boolean isExecutableOrSharedLibrary(MakeConfiguration conf, String path) {
        File file;
        int platform = conf.getPlatform().getValue();

        if (conf.isApplicationConfiguration() || conf.isDynamicLibraryConfiguration()) {
            return true;
        } else if (conf.isMakefileConfiguration()) {
            if (platform == PlatformTypes.PLATFORM_WINDOWS) {
                if (path.endsWith(".dll")) { // NOI18N
                    return false;
                } else if (!path.endsWith(".exe")) { // NOI18N
                    path = path + ".exe"; // NOI18N
                }
                file = new File(path);
                if (file.exists()) {
                    return true;
                }
            }
            file = new File(path);
            if (file.exists()) {
                String mime_type = FileUtil.getMIMEType(FileUtil.toFileObject(file));
                if (mime_type != null && mime_type.startsWith("application/x-exe")) { // NOI18N
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    /**
     *  Called when GdbProxy receives the results of a -stack-list-frames command.
     */
    private void stackUpdate(List<String> stack) {
        synchronized (callstack) {
            callstack.clear();

            for (int i = 0; i < stack.size(); i++) {
                String line = stack.get(i);
                Map<String, String> map = GdbUtils.createMapFromString(line.substring(6));

                String func = map.get("func"); // NOI18N
                String file = map.get("file"); // NOI18N
                String fullname = map.get("fullname"); // NOI18N
                String lnum = map.get("line"); // NOI18N
                String addr = map.get("addr"); // NOI18N
                String from = map.get("from"); // NOI18N
                if (fullname == null && file != null) {
                    if (file.charAt(0) == '/') {
                        fullname = file;
                        log.finest("GD.stackUpdate: Setting fullname from file"); // NOI18N
                    } else {
                        fullname = runDirectory + file;
                        log.finest("GD.stackUpdate: Setting fullname from runDirectory + file"); // NOI18N
                    }
                }
                if (platform == PlatformTypes.PLATFORM_WINDOWS && isCygwin() && fullname != null && fullname.charAt(0) == '/') {
                    if (fullname.length() >= 5 && fullname.startsWith("/usr/")) { // NOI18N
                        fullname = CompilerSetManager.getCygwinBase() + fullname.substring(4);
                    } else {
                        fullname = CompilerSetManager.getCygwinBase() + fullname;
                    }
                }

                callstack.add(i, new CallStackFrame(this, func, file, fullname, lnum, addr, i, from));
            }
        }

        if (!stack.isEmpty()) {
            pcs.firePropertyChange(PROP_CURRENT_CALL_STACK_FRAME, 0, 1);
        }
    }

    public void setCurrentThread(String tline) {
        if (tline.length() > 0) {
            if (Character.isDigit(tline.charAt(0))) {
                int idx = tline.indexOf(' ');
                if (idx > 0) {
                    resetThreadInfo();
                    gdb.thread_select(tline.substring(0, idx));
                }
            }
        }
    }

    /**
     * Returns list of cached local variables for this debugger. This typically gets
     * called from an evaluator thread. If we don't have the type, it should be coming
     * on the GdbReaderRP thread so we wait for it.
     *
     * @return list of local variables
     */
    public List<GdbVariable> getLocalVariables() {
        assert !(Thread.currentThread().getName().equals("GdbReaderRP"));
        synchronized (localVariables) {
            return new ArrayList<GdbVariable>(localVariables);
        }
    }

    private static final String VALUE_PREFIX = "value="; // NOI18N

    public String evaluate(String expression) {
        // IZ:131315 (gdb may not be initialized yet)
        if (gdb == null) {
            return null;
        }

        boolean suspendAll = expression.indexOf('(') != -1;

        if (suspendAll) {
            suspendBreakpointsAndSignals();
        }
        CommandBuffer cb = gdb.data_evaluate_expressionEx('"' + expression + '"'); // NOI18N
        if (suspendAll) {
            restoreBreakpointsAndSignals();
        }

        if (cb.isError()) {
            String err = cb.getError();
            if (err.startsWith("\"The program being debugged was signaled while in a function called from GDB")) { // NOI18N
                err = NbBundle.getMessage(GdbDebugger.class, "ERR_WatchedFunctionAborted");
            }
            return err;
        }

        String response = cb.getResponse();
        // response have the form "value="..."", so we trim it and skip commas also
        if (response.startsWith(VALUE_PREFIX)) {
            response = response.substring(VALUE_PREFIX.length() + 1, response.length() - 1);
        } else {
            log.severe("GDBDebugger.evaluate: unexpected response " + response + " for expression " + expression); // NOI18N
        }
        if (response.startsWith("@0x")) { // NOI18N
            cb = gdb.print(expression);
            response = cb.getResponse();
            if (response.length() > 0 && response.charAt(0) == '$') {
                int pos = response.indexOf('=');
                if (pos != -1 && (pos + 2) < response.length()) {
                    response = response.substring(pos + 2, response.length()).replace("\\n", "").trim(); // NOI18N
                }
            }
        }
        return response.length() > 0 ? response : null;
    }

    public Map<String, TypeInfo> getTypeInfoCache() {
        return ticache;
    }

    public String requestValue(String name) {
        try {
            return requestValueEx(name);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    // TODO: unify with requestWhatis and requestSymbolType
    public String requestValueEx(String name) throws GdbErrorException {
        assert !Thread.currentThread().getName().equals("GdbReaderRP"); // NOI18N

        if (state == State.STOPPED) {
            CommandBuffer cb = gdb.data_evaluate_expressionEx(name);
            String info = cb.getResponse();
            if (info.length() == 0 || !cb.isOK()) {
                if (cb.isError()) {
                    log.fine("GD.requestValue Error[" + cb + "]"); // NOI18N
                    // TODO: do not enclose the message in ><
                    throw new GdbErrorException('>' + cb.getError() + '<');
                } else {
                    log.fine("GD.requestValue Failure[" + cb + "]"); // NOI18N
                    return "";
                }
            } else {
                return info.substring(7, info.length() - 1);
            }
        } else {
            return null;
        }
    }

    // TODO: unify with requestValueEx and requestSymbolType
    public String requestWhatis(String name) {
        assert !Thread.currentThread().getName().equals("GdbReaderRP"); // NOI18N

        if (state == State.STOPPED && name != null && name.length() > 0) {
            CommandBuffer cb = gdb.whatis(name);
            String info = cb.getResponse();
            if (info.length() == 0 || !cb.isOK()) {
                if (cb.isError()) {
                    log.fine("GD.requestWhatis Error[" + cb + "]"); // NOI18N
                    return "";
                } else {
                    log.fine("GD.requestWhatis Failure[" + cb + "]"); // NOI18N
                    return "";
                }
            } else {
                return info.substring(7, info.length() - 2);
            }
        } else {
            return null;
        }
    }

    // TODO: unify with requestWhatis and requestValueEx
    public String requestSymbolType(String type) {
        assert !Thread.currentThread().getName().equals("GdbReaderRP"); // NOI18N

        if (state == State.STOPPED && type != null && type.length() > 0) {
            CommandBuffer cb = gdb.symbol_type(type);
            String info = cb.getResponse();
            if (info.length() == 0 || !cb.isOK()) {
                if (cb.isError()) {
                    log.fine("GD.requestSymbolType Error[" + cb + "]"); // NOI18N
                    return "";
                } else {
                    log.fine("GD.requestSymbolType Failure[" + cb + "]. Returning original type"); // NOI18N
                    return type;
                }
            } else {
                log.fine("GD.requestSymbolType[" + cb + "]: " + type + " --> [" + info + "]");
                return info.substring(7, info.length() - 2);
            }
        } else {
            return null;
        }
    }

    // TODO: unify with requestWhatis and requestValueEx
    public String requestSymbolTypeFromName(String name) {
        assert !Thread.currentThread().getName().equals("GdbReaderRP"); // NOI18N

        if (state == State.STOPPED && name != null && name.length() > 0) {
            CommandBuffer cb = gdb.symbol_type(name);
            String info = cb.getResponse();
            if (info.length() == 0 || !cb.isOK()) {
                if (cb.isError()) {
                    log.fine("GD.requestSymbolTypeFromName Error[" + cb + "]"); // NOI18N
                    return "";
                } else {
                    log.fine("GD.requestSymbolTypeFromName Failure[" + cb + "]. Returning original type"); // NOI18N
                    return "";
                }
            } else {
                log.fine("GD.requestSymbolTypeFromName[" + cb + "]: " + name + " --> [" + info + "]");
                return info.substring(7, info.length() - 2);
            }
        } else {
            return null;
        }
    }

    /**
     * Suspend all breakpoints. This is used to suspend breakpoints during Watch
     * updates so functions called don't stop.
     */
    private void suspendBreakpointsAndSignals() {
        gdb.break_disable();
        gdb.set_unwindonsignal("on"); // NOI18N
    }

    /**
     * Resume all breakpoints. This is used to re-enable breakpoints after a Watch
     * update.
     */
    private void restoreBreakpointsAndSignals() {
        gdb.set_unwindonsignal("off"); // NOI18N
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for (Map.Entry<Integer, BreakpointImpl> entry : getBreakpointList().entrySet()) {
            if (entry.getValue().getBreakpoint().isEnabled()) {
                ids.add(entry.getKey());
            }
        }
        gdb.break_enable(ids.toArray(new Integer[ids.size()]));
    }

    /**
     * Returns call stack for this debugger.
     *
     * @return call stack
     */
    public List<CallStackFrame> getCallStack() {
        return callstack;
    }

    public int getStackDepth() {
        return callstack.size();
    }

    /**
     * Returns current stack frame or null.
     *
     * @return current stack frame or null
     */
    public synchronized CallStackFrame getCurrentCallStackFrame() {
        if (currentCallStackFrame != null) {
            return currentCallStackFrame;
        } else if (!callstack.isEmpty()) {
            return callstack.get(0);
        }
        return null;
    }

    /**
     * Sets a stack frame current.
     *
     * @param Frame to make current (or null)
     */
    public void setCurrentCallStackFrame(CallStackFrame callStackFrame) {
        if (callStackFrame.isValid()) {
            CallStackFrame old = setCurrentCallStackFrameNoFire(callStackFrame);
            updateLocalVariables(callStackFrame.getFrameNumber());
            if (old == callStackFrame) {
                return;
            }
            pcs.firePropertyChange(PROP_CURRENT_CALL_STACK_FRAME, old, callStackFrame);
        } else {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                    "ERR_InvalidCallStackFrame"))); // NOI18N

        }
    }

    private CallStackFrame setCurrentCallStackFrameNoFire(CallStackFrame callStackFrame) {
        CallStackFrame old;

        synchronized (this) {
            old = getCurrentCallStackFrame();
            if (callStackFrame == old) {
                return callStackFrame;
            }
            currentCallStackFrame = callStackFrame;
        }
        return old;
    }

    public void popTopmostCall() {
        if (!callstack.isEmpty() && callstack.get(1).isValid()) {
            gdb.stack_select_frame(0);
            gdb.exec_finish();
        } else {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(NbBundle.getMessage(GdbDebugger.class,
                    "ERR_InvalidCallStackFrame"))); // NOI18N
        }
    }

    public Map<Integer, BreakpointImpl> getBreakpointList() {
        return breakpointList;
    }

    private BreakpointImpl findBreakpoint(String id) {
        try {
            return breakpointList.get(Integer.valueOf(id));
        } catch (NumberFormatException nfe) {
            return null;
        }
    }

    /**
     *  Gdb/mi doesn't handle spaces in paths (see http://sourceware.org/ml/gdb/2006-02/msg00283.html
     *  for more details). So try an alternate if the path has embedded spaces.
     *
     *  @param path The absolute path to convert
     *  @return The possibly modified path
     */
    public String getBestPath(String path) {
        path = pathMap.getRemotePath(path);
        if (path.startsWith(baseDir + '/')) {
            return path.substring(baseDir.length() + 1);
        } else {
            int pos = path.lastIndexOf('/');
            if (pos != -1) {
                return path.substring(pos + 1);
            }
        // Don't delete the following code yet. Neet to understand breakpoints outside the current
        // project better! This might still be relevant.
//            String rdir;
//            if (runDirectory.endsWith("/")) { // NOI18N
//                rdir = runDirectory.substring(0, runDirectory.length() - 1);
//            } else {
//                rdir = runDirectory;
//            }
//            int rdir_pos = rdir.indexOf('/');
//            int path_pos = path.indexOf('/');
//            int match = -1;
//            while (rdir_pos == path_pos && rdir_pos != -1) {
//                if (path.substring(0, rdir_pos).equals(rdir.substring(0, rdir_pos))) {
//                    match = rdir_pos;
//                }
//                rdir_pos = rdir.indexOf('/', rdir_pos + 1);
//                path_pos = path.indexOf('/', path_pos + 1);
//            }
//            if (match != -1) {
//                path_pos = path.substring(0, path_pos).lastIndexOf('/'); // we want the previous path_pos
//                int count = 1;
//                while (rdir_pos != -1) {
//                    count++;
//                    rdir_pos = rdir.indexOf('/', rdir_pos + 1);
//                }
//                StringBuilder rpath = new StringBuilder();
//                while (count-- > 0) {
//                    rpath.append("../");
//                }
//                return rpath.toString()  + path.substring(path_pos + 1);
//            }
        }
        return path;
    }

    /**
     * Adds property change listener.
     *
     * @param propertyName a name of property to listen on
     * @param l new listener.
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
        pcs.addPropertyChangeListener(propertyName, l);
    }

    /**
     *  Adds property change listener.
     *
     * @param l new listener.
     */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    /**
     * Removes property change listener.
     *
     * @param propertyName a name of property to listen on
     * @param l removed listener.
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {
        pcs.removePropertyChangeListener(propertyName, l);
    }

    /**
     *  Removes property change listener.
     *
     * @param l removed listener.
     */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private void firePropertyChange(String name, Object o, Object n) {
        pcs.firePropertyChange(name, o, n);
    }

    private boolean isCygwin() {
        return cygwin;
    }

    private boolean isMinGW() {
        return mingw;
    }

    private boolean isSolaris() {
        return platform == PlatformTypes.PLATFORM_SOLARIS_INTEL || platform == PlatformTypes.PLATFORM_SOLARIS_SPARC;
    }

    /**
     * Warning: The gdb debugger isn't very good at checking C vs C++. I'm not deprecating this call but I've
     * discovered it isn't reliable (because gdb isn't reliable).
     *
     * @return True for C++, false otherwise
     */
    public boolean isCplusPlus() {
        return cplusplus;
    }

    public Disassembly getDisassembly() {
        return disassembly;
    }

    public void setCurrentBreakpoint(GdbBreakpoint currentBreakpoint) {
        this.currentBreakpoint = currentBreakpoint;
    }

    public void setLastGo(LastGoState lastGo) {
        this.lastGo = lastGo;
    }

    private boolean isAttaching() {
        ProjectActionEvent pae = lookupProvider.lookupFirst(null, ProjectActionEvent.class);
        return pae.getType() == DEBUG_ATTACH;
    }

    private static double parseGdbVersionString(String msg) {
        double ver = 0.0;
        int first = msg.indexOf('.');
        int last = first + 1;

        try {
            while (last < msg.length() && Character.isDigit(msg.charAt(last))) {
                last++;
            }
            ver = Double.parseDouble(msg.substring(0, last));
        } catch (Exception ex) {
            log.warning("GdbDebugger: Failed to parse version string [" + ex.getClass().getName() + "]");
            if (msg.contains("6.5")) { // NOI18N
                ver = 6.5;
            } else if (msg.contains("6.6")) { // NOI18N
                ver = 6.6;
            } else if (msg.contains("6.7")) { // NOI18N
                ver = 6.7;
            } else if (msg.contains("6.8")) { // NOI18N
                ver = 6.8;
            } else {
                log.warning("GdbDebugger: Failed to guess version string");
            }
        }
        return ver;
    }

    private static class ShareInfo {
        private final String path;
        private final String addr;
        private List<String> sourceDirs;
        private Project project;

        public ShareInfo(String path, String addr) {
            this.path = path;
            this.addr = addr;
            sourceDirs = null;
            project = null;
        }

        public String getPath() {
            return path;
        }

        public String getAddress() {
            return addr;
        }

        public List<String> getSourceDirectories() {
            if (sourceDirs == null) {
                sourceDirs = new ArrayList<String>();

                if (project != null) {
                    NativeProject nativeProject = project.getLookup().lookup(NativeProject.class);
                    if (nativeProject != null) {
                        sourceDirs.add(nativeProject.getProjectRoot());
                        for (String dir : nativeProject.getSourceRoots()) {
                            sourceDirs.add(dir);
                        }
                    }

                }
            }
            return sourceDirs;
        }

        public boolean isMatchingProject() {
            Object o;
            MakeConfiguration conf;

            for (Project proj : OpenProjects.getDefault().getOpenProjects()) {
                ProjectConfigurationProvider pcp = proj.getLookup().lookup(ProjectConfigurationProvider.class);
                if (pcp != null) {
                    o = pcp.getActiveConfiguration();
                    if (o instanceof MakeConfiguration) {
                        conf = (MakeConfiguration) o;
                        if (conf.isDynamicLibraryConfiguration()) {
                            String proot = FileUtil.getFileDisplayName(proj.getProjectDirectory());
                            // TODO: possible bug, consider using IPEUtils
                            String output = proot + "/" + conf.getOutputValue(); // NOI18N
                            output = conf.expandMacros(output); // expand macros (FIXUP: needs verification)
                            if (output.equals(path)) {
                                this.project = proj;
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }
    }

    private void tlog(String msg) {
        if (isUnitTest() && tlog.isLoggable(Level.WARNING)) {
            System.out.println("    " + msg); // NOI18N
        }
    }
}
