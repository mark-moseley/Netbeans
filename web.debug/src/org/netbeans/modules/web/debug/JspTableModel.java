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

package org.netbeans.modules.web.debug;

import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.*;
import org.netbeans.modules.web.debug.breakpoints.*;

/**
 *
 * @author Martin Grebac
 */
public class JspTableModel implements TableModel, Constants {


    public Object getValueAt (Object row, String columnID) throws UnknownTypeException {
        if (row instanceof JspLineBreakpoint) {
            if (columnID.equals (BREAKPOINT_ENABLED_COLUMN_ID))
                return Boolean.valueOf (((JspLineBreakpoint) row).isEnabled ());
        }
        throw new UnknownTypeException (row);
    }
    
    public boolean isReadOnly (Object row, String columnID) throws UnknownTypeException {
        throw new UnknownTypeException (row);
    }
    
    public void setValueAt (Object row, String columnID, Object value) throws UnknownTypeException {
        if (row instanceof JspLineBreakpoint) {
            if (columnID.equals (BREAKPOINT_ENABLED_COLUMN_ID))
                if (((Boolean) value).equals (Boolean.TRUE))
                    ((Breakpoint) row).enable ();
                else
                    ((Breakpoint) row).disable ();
        }
        throw new UnknownTypeException (row);
    }


//    private static String getSessionState (Session s) {
//        DebuggerEngine e = s.getCurrentEngine ();
//        JPDADebugger d = JPDADebugger.getJPDADebugger (e);
//        switch (d.getState ()) {
//            case JPDADebugger.STATE_DISCONNECTED:
//                return "Not Running";
//            case JPDADebugger.STATE_RUNNING:
//                return "Running";
//            case JPDADebugger.STATE_STARTING:
//                return "Starting";
//            case JPDADebugger.STATE_STOPPED:
//                return "Stopped";
//        }
//        return null;
//    }
//    
    /** 
     * Registers given listener.
     * 
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
    }

    /** 
     * Unregisters given listener.
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
    }
    
//    private static String getShort (String c) {
//        int i = c.lastIndexOf ('.');
//        if (i < 0) return c;
//        return c.substring (i + 1);
//    }
}
