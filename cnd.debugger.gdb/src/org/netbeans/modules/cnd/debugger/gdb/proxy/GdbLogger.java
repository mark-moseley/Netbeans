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
 * GdbLogger.java
 *
 * @author Nik Molchanov
 *
 * Originally this class was in org.netbeans.modules.cnd.debugger.gdb package.
 * Later a new "proxy" package was created and this class was moved, that's how
 * it lost its history. To view the history look at the previous location.
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;

/**
 * Class GdbLogger is used to log all incoming and outgoing messages
 */
public class GdbLogger {
    
    private GdbConsoleWindow gdbConsoleWindow = null;
    private FileWriter logFile;
    private Logger log = Logger.getLogger("gdb.gdbproxy.logger"); // NOI18N
    
    /** Creates a new instance of GdbLogger */
    public GdbLogger(GdbDebugger debugger, GdbProxy gdbProxy) {
        File tmpfile;
        try {
            tmpfile = File.createTempFile("gdb-cmds", ".log"); // NOI18N
	    if (!Boolean.getBoolean("gdb.console.savelog")) { // NOI18N - This lets me save logs
		tmpfile.deleteOnExit();
	    }
            logFile = new FileWriter(tmpfile);
        } catch (IOException ex) {
            logFile = null;
        }
        
        if (Boolean.getBoolean("gdb.console.window")) { // NOI18N
            gdbConsoleWindow = GdbConsoleWindow.getInstance(debugger, gdbProxy);
            gdbConsoleWindow.openConsole();
        }
    }
    
    /**
     * Sends message to the debugger log. If console property is set also send it
     * to the console.
     *
     * @param message - a message from the debugger
     */
    public void logMessage(String message) {
        if (message != null && message.length() > 0) {
            if (!message.endsWith("\n")) { // NOI18N
                message = message + '\n';
            }
            if (logFile != null) {
                try {
                    logFile.write(message);
                    logFile.flush();
                } catch (IOException ioex) {
                }
            }
            if (gdbConsoleWindow != null) {
                gdbConsoleWindow.add(message);
            }
        }
    }
}
