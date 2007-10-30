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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.debugger.jpda.ant;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;


/**
 * Ant task, which is called after the debugged application is reloaded.
 * E.g. re-deployed to the server
 *
 * @author Martin Entlicher
 */
public class JPDAAppReloaded extends Task {

    private static final Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.ant"); // NOI18N
    
    public void execute() throws BuildException {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("JPDAAppReloaded.execute()");
        }
        
        // check debugger state
        DebuggerEngine debuggerEngine = DebuggerManager.getDebuggerManager ().
            getCurrentEngine ();
        if (debuggerEngine == null) {
            throw new BuildException ("No debugging sessions was found.");
        }
        JPDADebugger debugger = (JPDADebugger) debuggerEngine.lookupFirst 
            (null, JPDADebugger.class);
        if (debugger == null) {
            throw new BuildException("Current debugger is not JPDA one.");
        }
        try {
            Method fixBreakpointsMethod = debugger.getClass().getMethod("fixBreakpoints", new Class[] {});
            fixBreakpointsMethod.invoke(debugger, new Object[] {});
        } catch (Exception ex) {
            throw new BuildException(ex);
        }
        
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Breakpoints fixed.");
        }
    }
    
}
