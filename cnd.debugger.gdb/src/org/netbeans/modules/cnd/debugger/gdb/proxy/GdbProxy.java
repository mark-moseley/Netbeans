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

/*
 * GdbProxy.java
 *
 * Note: For a description of the current state of quoting and related topics, see
 * http://sourceware.org/ml/gdb/2006-02/msg00283.html.
 *
 * @author Nik Molchanov and Gordon Prieur
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Utilities;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.utils.CommandBuffer;
import org.netbeans.modules.cnd.debugger.gdb.utils.GdbUtils;

/**
 * Class GdbProxy is a Controller component of gdb driver
 * The main part of the knowledge about the debugger UI is concentrated in this class.
 * It knows the differences between the debuggers, and will eliminate these differences
 * as much as possible, presenting data to the View layer.
 * Main functions:
 *    tell low level to start the debugger
 *    translate commands from CLI to MI
 *    tell low level to send a command to the debugger
 *    receive the debugger's output from low level and parse the MI messages
 *    call the "callback" methods (upper level)
 *    tell low level to send a signal to the application to interrupt the execution
 *    tell low level to kill the debugger
 */
public class GdbProxy implements GdbMiDefinitions {

    private GdbDebugger debugger;
    private GdbProxyEngine engine;
    private GdbLogger gdbLogger;
    private Logger log = Logger.getLogger("gdb.gdbproxy.logger"); // NOI18N

    /**
     * Creates a new instance of GdbProxy
     *
     * @param debugger The GdbDebugger
     * @param debuggerCommand The gdb command to use
     * @param debuggerEnvironment The overrides to the user's environment
     * @param workingDirectory The directory to start the debugger from
     * @throws IOException Pass this on to the caller
     */
    public GdbProxy(GdbDebugger debugger, String debuggerCommand, String[] debuggerEnvironment, String workingDirectory, String termpath) throws IOException {
        this.debugger = debugger;

        log.setLevel(Level.FINE);

        ArrayList<String> dc = new ArrayList<String>();
        dc.add(debuggerCommand);
        dc.add("--nw"); // NOI18N
        dc.add("--silent"); // NOI18N
        dc.add("--interpreter=mi"); // NOI18N
        gdbLogger = new GdbLogger(debugger, this);
        engine = new GdbProxyEngine(debugger, this, dc, debuggerEnvironment, workingDirectory, termpath);
    }

    protected GdbProxyEngine getProxyEngine() {
        return engine;
    }

    public GdbLogger getLogger() {
        return gdbLogger;
    }

    /**
     * Load the program
     *
     * @param program - a name of an external program to debug
     */
    public int file_exec_and_symbols(String programName) {
        return engine.sendCommand("-file-exec-and-symbols " + programName); // NOI18N
    }
    
    /** Attach to a running program */
    public int target_attach(CommandBuffer cb, String pid) {
//        return engine.sendCommand("-target-attach " + pid); // NOI18N - no implementaion
        return engine.sendCommand(cb, "attach " + pid); // NOI18N
    }
    
    /** Detach from a running program */
    public int target_detach() {
//        return engine.sendCommand("-target-detach"); // NOI18N - no implementaion
        return engine.sendCommand("detach"); // NOI18N
    }

    /**
     * Load the symbol table only. Used to get symbols for an attached program.
     *
     * @param program - a name of an external program to debug
     */
    public int file_symbol_file(String path) {
        return engine.sendCommand("-file-symbol-file " + path); // NOI18N
    }

    /** Ask gdb for its version */
    public int gdb_version() {
        return engine.sendCommand("-gdb-version"); // NOI18N
    }

    /** Ask gdb about a variable (currently used to find the current language) */
    public int gdb_show(String arg) {
        return engine.sendCommand("-gdb-show " + arg); // NOI18N
    }

    /**
     * Set the runtime directory. Note that this method may get called before we have
     * gdb's version. Thats why we check that its greater than 6.3. This way, if we
     * don't have the version we fallback to the non-mi command.
     *
     * @param path The directory we want to run from
     */
    public int environment_cd(String dir) {
        double ver = debugger.getGdbVersion();
        if (ver > 6.3) {
            return engine.sendCommand("-environment-cd  \"" + dir + "\""); // NOI18N
        } else {
            return engine.sendCommand("cd \"" + dir + "\""); // NOI18N
        }
    }

    /**
     * Set the runtime directory. Note that this method may get called before we have
     * gdb's version. Thats why we check that its greater than 6.3. This way, if we
     * don't have the version we fallback to the non-mi command.
     *
     * @param path The directory we want to run from
     */
    public int environment_directory(String dir) {
        double ver = debugger.getGdbVersion();
        if (ver > 6.3) {
            return engine.sendCommand("-environment-directory  \"" + dir + "\""); // NOI18N
        } else {
            return engine.sendCommand("directory \"" + dir + "\""); // NOI18N
        }
    }

    /**
     *  Do a "set environment" gdb command.
     *
     *  @param var Variable of the form "foo=value"
     */
    public int gdb_set_environment(String var) {
        return engine.sendCommand("-gdb-set environment " + var); // NOI18N
    }

    /**
     *  Ask gdb about threads. We don't really care about the threads, but it also returns
     *  the process ID, which we do care about.
     *
     *  Note: In gdb 6.5.50 the -threads-list-all-threads command isn't implemented so we
     *  revert to the gdb command "info threads".
     */
    public int info_threads(CommandBuffer cb) {
        return engine.sendCommand(cb, "info threads"); // NOI18N;
    }
    
    public int info_threads() {
        return engine.sendCommand("info threads"); // NOI18N;
    }
    
    public int info_files(CommandBuffer cb) {
        return engine.sendCommand(cb, "info files"); // NOI18N
    }

    /** Set the current thread */
    public int thread_select(String id) {
        return engine.sendCommand("-thread-select " + id); // NOI18N
    }

    /**
     *  Ask gdb about /proc info. We don't really care about the /proc, but it also returns
     *  the process ID, which we do care about.
     */
    public int info_proc() {
        return engine.sendCommand("info proc"); // NOI18N
    }
    
    public int info_share() {
        return engine.sendCommand("info share"); // NOI18N
    }
    
    public int info_share(CommandBuffer cb) {
        return engine.sendCommand(cb, "info share"); // NOI18N
    }

    /**
     *  Use this to call _CndSigInit() to initialize signals in Cygwin processes.
     */
    public int data_evaluate_expression(CommandBuffer cb, String string) {
        return engine.sendCommand(cb, "-data-evaluate-expression " + string); // NOI18N
    }

    /**
     *  Use this to call _CndSigInit() to initialize signals in Cygwin processes.
     */
    public int data_evaluate_expression(String string) {
        return engine.sendCommand("-data-evaluate-expression " + string); // NOI18N
    }
    
    /**
     */
    public int data_list_register_names(CommandBuffer cb, String regIds) {
        return engine.sendCommand(cb, "-data-list-register-names " + regIds); // NOI18N
    }
    
    /**
     */
    public int data_list_register_values(CommandBuffer cb, String regIds) {
        return engine.sendCommand(cb, "-data-list-register-values r " + regIds); // NOI18N
    }
    
    /*
     * @param filename - source file to disassemble
     */
    public int data_disassemble(String filename, int line) {
        return engine.sendCommand("-data-disassemble -f " + filename + " -l " + line + " -- 0"); // NOI18N
    }
    
    public int print(CommandBuffer cb, String expression) {
        return engine.sendCommand(cb, "print " + expression); // NOI18N
    }

    /**
     * Send "-file-list-exec-source-file" to the debugger
     * This command lists the line number, the current source file,
     * and the absolute path to the current source file for the current executable.
     *
     * @return null if action is accepted, otherwise return error message
     */
    public int file_list_exec_source_file() {
        return engine.sendCommand("-file-list-exec-source-file"); // NOI18N
    }

    /**
     * Send "-exec-run" with parameters to the debugger
     * This command starts execution of the inferior from the beginning.
     * The inferior executes until either a breakpoint is encountered or
     * the program exits.
     *
     * @param programParameters - command line options for the program
     */
    public int exec_run(String programParameters) {
        return engine.sendCommand("-exec-run " + programParameters); // NOI18N
    }

    /**
     * Send "-exec-run" to the debugger
     * This command starts execution of the inferior from the beginning.
     * The inferior executes until either a breakpoint is encountered or
     * the program exits.
     */
    public int exec_run() {
        return exec_run("");
    }

    /**
     * Send "-exec-step" to the debugger
     * This command resumes execution of the inferior program, stopping
     * when the beginning of the next source line is reached, if the next
     * source line is not a function call.
     * If it is, stop at the first instruction of the called function.
     */
    public int exec_step() {
        return engine.sendCommand("-exec-step"); // NOI18N
    }

    /**
     * Send "-exec-next" to the debugger (go to the next source line)
     * This command resumes execution of the inferior program, stopping
     * when the beginning of the next source line is reached.
     */
    public int exec_next() {
        return engine.sendCommand("-exec-next"); // NOI18N
    }
    
    /**
     * Execute single instruction
     */
    public int exec_instruction() {
        return engine.sendCommand("-exec-step-instruction"); // NOI18N
    }

    /**
     * Send "-exec-finish" to the debugger (finish this function)
     * This command resumes execution of the inferior program until
     * the current function is exited.
     */
    public int exec_finish() {
        return engine.sendCommand("-exec-finish"); // NOI18N
    }

    /**
     * Send "-exec-continue" to the debugger
     * This command resumes execution of the inferior program, until a
     * breakpoint is encountered, or until the inferior exits.
     */
    public int exec_continue() {
        return engine.sendCommand("-exec-continue"); // NOI18N
    }

    /**
     * Interrupts execution of the inferior program.
     * This method is supposed to send "-exec-interrupt" to the debugger,
     * but this feature is not implemented in gdb yet, so it is replaced
     * with sending a signal "INT" (Unix) or signal TSTP (Windows).
     */
    public int exec_interrupt() {
        if (debugger.getState().equals(GdbDebugger.STATE_RUNNING) || debugger.getState().equals(GdbDebugger.STATE_SILENT_STOP)) {
            if (Utilities.isWindows()) {
                debugger.kill(18);
            } else {
                debugger.kill(2);
            }
        }
        return 0;
    }

    /**
     * Send "-exec-abort" to the debugger
     * This command kills the inferior program.
     */
    public int exec_abort() {
        String cmd;

        // -exec-abort isn't implemented yet (as of gdb 6.6)
        if (debugger.getGdbVersion() > 6.6) {
            cmd = "-exec-abort "; // NOI18N
        } else {
            cmd = "kill "; // NOI18N
        }
        return engine.sendCommand(cmd);
    }

    /**
     * Send "-break-insert function" to the debugger
     * This command inserts a regular breakpoint in all functions
     * whose names match the given name.
     *
     * @param flags One or more flags aout this breakpoint
     * @param name A function name
     * @return token number
     */
    public int break_insert(int flags, String name) {
        StringBuilder cmd = new StringBuilder();

        if (GdbUtils.isMultiByte(name)) {
            if ((flags & GdbDebugger.GDB_TMP_BREAKPOINT) != 0) {
                cmd.append("tbreak "); // NOI18N
            } else {
                cmd.append("break "); // NOI18N
            }
        } else {
            cmd.append("-break-insert "); // NOI18N
            if ((flags & GdbDebugger.GDB_TMP_BREAKPOINT) != 0) {
                cmd.append("-t "); // NOI18N
            }
        }

        // Temporary fix for Windows
        if (Utilities.isWindows() && name.indexOf('/') == 0 && name.indexOf(':') == 2) {
            // Remove first slash
            name = name.substring(1);
        } else if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
            cmd.append("-l 1 "); // NOI18N - Always use 1st choice
        }
        cmd.append(name);
        return engine.sendCommand(cmd.toString());
    }

    /**
     * Send "-break-insert function" to the debugger
     * This command inserts a regular breakpoint in all functions
     * whose names match the given name.
     *
     * @param name The function name or linenumber information
     * @return token number
     */
    public int break_insert(String name) {
        return break_insert(0, name);
    }

    /**
     * Send "-break-delete number" to the debugger
     * This command deletes the breakpoints
     * whose number(s) are specified in the argument list.
     *
     * @param number - breakpoint's number
     */
    public int break_delete(int number) {
        return engine.sendCommand("-break-delete " + Integer.toString(number)); // NOI18N
    }

    /**
     * Send "-break-enable number" to the debugger
     * This command enables the breakpoint
     * whose number is specified by the argument.
     *
     * @param number - breakpoint number
     */
    public int break_enable(int number) {
        return engine.sendCommand("-break-enable " + Integer.toString(number)); // NOI18N
    }

    /**
     * Send "-break-disable number" to the debugger
     * This command disables the breakpoint
     * whose number is specified by the argument.
     *
     * @param number - breakpoint number
     */
    public int break_disable(int number) {
        return engine.sendCommand("-break-disable " + Integer.toString(number)); // NOI18N
    }

    /** Send "-stack-list-locals" to the debugger */
    public int stack_list_locals() {
        return stack_list_locals(""); // NOI18N
    }

    /**
     * Send "-stack-list-locals" to the debugger
     * Display the local variable names for the selected frame.
     * If print-values is 0 or --no-values, print only the names of the variables;
     * if it is 1 or --all-values, print also their values; and if it is 2 or --simple-values,
     * print the name, type and value for simple data types and the name and type for arrays,
     * structures and unions. In this last case, a frontend can immediately display the value
     * of simple data types and create variable objects for other data types when the the user
     * wishes to explore their values in more detail.
     *
     * @param printValues defines output format
     */
    public int stack_list_locals(String printValues) {
        return engine.sendCommand("-stack-list-locals " + printValues); // NOI18N
    }

    public int stack_list_arguments(int showValues, int low, int high) {
        return engine.sendCommand("-stack-list-arguments " + showValues + " " + low + " " + high); // NOI18N
    }

    /**
     * Send "-stack-select-frame frameNumber" to the debugger
     * This command tells gdb to change the current frame.
     * Select a different frame frameNumber on the stack.
     */
    public int stack_select_frame(int frameNumber) {
        return engine.sendCommand("-stack-select-frame " + Integer.valueOf(frameNumber)); // NOI18N
    }

    /**
     * Send "-stack-info-frame " to the debugger
     * This command asks gdb to provide information about current frame.
     */
    public int stack_info_frame() {
        return engine.sendCommand("-stack-info-frame "); // NOI18N
    }

    /** Request a stack dump from gdb */
    public int stack_list_frames() {
        return engine.sendCommand("-stack-list-frames "); // NOI18N
    }
    
    public int gdb_set(String command, String value) {
        StringBuilder sb = new StringBuilder();
        sb.append("-gdb-set "); // NOI18N
        sb.append(command);
        sb.append(' ');
        sb.append(value);
        return engine.sendCommand(sb.toString());
    }

    /**
     * Send "set new-console" to the debugger
     * This command tells gdb to execute inferior program with console.
     */
    public int set_new_console() {
        return engine.sendCommand(CLI_CMD_SET_NEW_CONSOLE);
    }

    /**
     * Send "set new-console" to the debugger
     * This command tells gdb to execute inferior program with console.
     */
    public int set_unwindonsignal(String on_off) {
        return engine.sendCommand("set unwindonsignal " + on_off); // NOI18N
    }

    /**
     * Request the type of a symbol. As of gdb 6.6, this is unimplemented so we send a
     * non-mi command "ptype". We should only be called when symbol is in scope.
     */
    public int symbol_type(CommandBuffer cb, String symbol) {
        return engine.sendCommand(cb, "ptype " + symbol); // NOI18N
    }

    /**
     * Request the type of a symbol. As of gdb 6.6, there is no gdb/mi way of doing this
     * so we send a gdb "whatis" command. This is different from -system-type in the case
     * of abstract data structures (structs and classes). Its the same for other types.
     */
    public int whatis(CommandBuffer cb, String symbol) {
        return engine.sendCommand(cb, "whatis " + symbol); // NOI18N
    }

    /**
     * Send "-gdb-exit" to the debugger
     * This command forces gdb to exit immediately.
     */
    public int gdb_exit() {
        int token = engine.sendCommand("-gdb-exit "); // NOI18N
        engine.stopSending();
        return token;
    }
} /* End of public class GdbProxy */
