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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.debugger.gdb.attach;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.netbeans.modules.cnd.debugger.gdb.actions.AttachTableColumn;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * Get a process list for the GdbAttachPanel. This needs to be platform neutral and
 * provide enough information so we can attach on <b>all</b> platforms.
 * 
 * @author gordonp
 */
final class ProcessList {
    private static enum PTYPE {
        UNINITIALIZED,
        NONE,
        STD,
        WINDOWS;
    }

    private PTYPE ptype = PTYPE.UNINITIALIZED;
    private final List<String> argsSimple = new ArrayList<String>();

    protected ProcessList() {
        if (Utilities.isWindows()) {
            File file = new File(CompilerSetManager.getCygwinBase() + "/bin", "ps.exe"); // NOI18N
            if (file.exists()) {
                argsSimple.add(file.getAbsolutePath());
                ptype = PTYPE.WINDOWS;
            } else {
                file = new File(CompilerSetManager.getMSysBase() + "/bin", "ps.exe"); // NOI18N
                if (file.exists()) {
                    argsSimple.add(file.getAbsolutePath());
                    ptype = PTYPE.WINDOWS;
                } else {
                    ptype = PTYPE.NONE;
                }
            }
        } else {
            if (new File("/bin/ps").exists()) { // NOI18N
                argsSimple.add("/bin/ps"); // NOI18N
                argsSimple.add("-a"); // NOI18N
                argsSimple.add("-o"); // NOI18N
                if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                    argsSimple.add("user,pid,ppid,stime,time,command"); // NOI18N
                } else {
                    argsSimple.add("user,pid,ppid,stime,time,args"); // NOI18N
                }
                ptype = PTYPE.STD;
            } else if (new File("/usr/bin/ps").exists()) { // NOI18N
                argsSimple.add("/usr/bin/ps"); // NOI18N
                argsSimple.add("-a"); // NOI18N
                argsSimple.add("-o"); // NOI18N
                argsSimple.add("user,pid,ppid,stime,time,args"); // NOI18N
                ptype = PTYPE.STD;
            } else {
                ptype = PTYPE.NONE;
            }
        }
    }

    private void request(final ProcessListReader plr, final List<String> args) {
        if (!argsSimple.isEmpty()) {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        ProcessBuilder pb = new ProcessBuilder(args);
                        pb.redirectErrorStream(true);
                        Process process = pb.start();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                        reader.readLine(); // read and ignore header line...
                        List<String> proclist = new ArrayList<String>();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            proclist.add(line);
                        }
                        plr.processListCallback(proclist);
                    } catch (IOException ioe) {
                        //do nothing
                    }
                }
            });
        }
    }

    void requestSimple(ProcessListReader plr) {
        request(plr, argsSimple);
    }

    void requestFull(ProcessListReader plr) {
        List<String> argsFull = new ArrayList<String>(argsSimple);
        if (ptype == PTYPE.WINDOWS) {
            argsFull.add("-W"); // NOI18N
        } else if (ptype == PTYPE.STD) {
            argsFull.add("-A"); // NOI18N
        }
        request(plr, argsFull);
    }
    
    protected PTYPE getPType() {
        return ptype;
    }
    
    protected boolean isStd() {
        return ptype == PTYPE.STD;
    }
    
    protected boolean isWindowsPsFound() {
        return ptype == PTYPE.WINDOWS;
    }

    protected List<AttachTableColumn> getColumnHeaders() {
        List<AttachTableColumn> headers = new ArrayList<AttachTableColumn>();
        
        if (ptype == PTYPE.STD) {
            headers.add(new AttachTableColumn("user",  // NOI18N
                    NbBundle.getMessage(ProcessList.class, "HDR_USER"))); // NOI18N
            headers.add(new AttachTableColumn("pid", // NOI18N
                    NbBundle.getMessage(ProcessList.class, "HDR_PID"))); // NOI18N
            headers.add(new AttachTableColumn("ppid", // NOI18N
                    NbBundle.getMessage(ProcessList.class, "HDR_PPID"))); // NOI18N
            headers.add(new AttachTableColumn("stime", // NOI18N
                    NbBundle.getMessage(ProcessList.class, "HDR_STIME"))); // NOI18N
            headers.add(new AttachTableColumn("time", // NOI18N
                    NbBundle.getMessage(ProcessList.class, "HDR_TIME"))); // NOI18N
            headers.add(new AttachTableColumn("args", // NOI18N
                    NbBundle.getMessage(ProcessList.class, "HDR_ARGS"))); // NOI18N
        } else if (ptype == PTYPE.WINDOWS) {
            headers.add(new AttachTableColumn("uid",  // NOI18N
                    NbBundle.getMessage(ProcessList.class, "HDR_UID"))); // NOI18N
            headers.add(new AttachTableColumn("winpid", // NOI18N
                    NbBundle.getMessage(ProcessList.class, "HDR_WINPID"))); // NOI18N
            headers.add(new AttachTableColumn("pid", // NOI18N
                    NbBundle.getMessage(ProcessList.class, "HDR_PID"))); // NOI18N
            headers.add(new AttachTableColumn("ppid", // NOI18N
                    NbBundle.getMessage(ProcessList.class, "HDR_PPID"))); // NOI18N
            headers.add(new AttachTableColumn("stime", // NOI18N
                    NbBundle.getMessage(ProcessList.class, "HDR_STIME"))); // NOI18N
            headers.add(new AttachTableColumn("args", // NOI18N
                    NbBundle.getMessage(ProcessList.class, "HDR_ARGS"))); // NOI18N
        }
        return headers;
    }
    
    protected String getArgsHeader() {
        return NbBundle.getMessage(ProcessList.class, "HDR_ARGS");
    }
}
