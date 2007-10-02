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

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import java.util.List;
import javax.swing.JComponent;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.debugger.gdb.EditorContextBridge;
import org.netbeans.spi.debugger.ui.BreakpointType;

import org.openide.util.NbBundle;


/**
 * Implementation of breakpoint on function.
 *
 * @author   Nik Molchanov (copied from Jan Jancura's JPDA implementation). 
 */
public class FunctionBreakpointType extends BreakpointType {

    public String getCategoryDisplayName() {
        return NbBundle.getMessage(FunctionBreakpointType.class,
                    "CTL_Gdb_breakpoint_events_category_name"); // NOI18N
    }
    
    public JComponent getCustomizer() {
        return new FunctionBreakpointPanel ();
    }
    
    public String getTypeDisplayName() {
        return NbBundle.getMessage(FunctionBreakpointType.class, "CTL_Gdb_Function_Breakpoint"); // NOI18N
    }
    
    public boolean isDefault() {
        // First, check for an open file. Is it one of ours?
	String mime = EditorContextBridge.getContext().getMostRecentMIMEType();
        if (mime.length() > 0) {
            if (mime.equals("text/x-c++") || mime.equals("text/x-c") || // NOI18N
                        mime.equals("text/x-fortran")) { // NOI18N
                return true;
            } else {
                return false;
            }
        }
        
        // Next, check the main project. Is it one of ours?
        Project project = OpenProjects.getDefault().getMainProject();
        if (project != null) {
            NativeProject np = (NativeProject) project.getLookup().lookup(NativeProject.class);
            if (np != null) {
                return true;
            } else {
                return false;
            }
        }
        
        // Last, count breakpoint types. We define 2. If thats all that are returned, then
        // we're the only active debugger and should be the default.
        List breakpointTypes = DebuggerManager.getDebuggerManager().lookup(null, BreakpointType.class);
        if (breakpointTypes.size() == 2) {
            return true;
        }
        	
	return false;
    }
}

