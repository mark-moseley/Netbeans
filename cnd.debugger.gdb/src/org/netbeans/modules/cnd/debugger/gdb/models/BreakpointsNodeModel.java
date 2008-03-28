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

package org.netbeans.modules.cnd.debugger.gdb.models;

import java.util.Vector;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.FunctionBreakpoint;

import org.openide.util.NbBundle;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.GdbBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.LineBreakpoint;
import org.netbeans.modules.cnd.debugger.gdb.EditorContextBridge;
import org.netbeans.modules.cnd.debugger.gdb.breakpoints.AddressBreakpoint;

/**
 * @author   Jan Jancura and Gordon Prieur
 */
public class BreakpointsNodeModel implements NodeModel {

    public static final String BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/NonLineBreakpoint"; // NOI18N
    public static final String DISABLED_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledNonLineBreakpoint";
    public static final String LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint"; // NOI18N
    public static final String DISABLED_LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledBreakpoint";
    public static final String LINE_CONDITIONAL_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/ConditionalBreakpoint";
    public static final String DISABLED_LINE_CONDITIONAL_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledConditionalBreakpoint";
    

    private Vector listeners = new Vector();

    static int log10(int n) {
        int l = 1;
        while ((n = n / 10) > 0) {
            l++;
        }
        return l;
    }
    
    private static final String ZEROS = "            "; // NOI18N
    
    static String zeros(int n) {
        if (n < ZEROS.length()) {
            return ZEROS.substring(0, n);
        } else {
            String z = ZEROS;
            while (z.length() < n) {
                z += " "; // NOI18N
            }
            return z;
        }
    }

    
    public String getDisplayName(Object o) throws UnknownTypeException {
        if (o instanceof LineBreakpoint) {
            LineBreakpoint b = (LineBreakpoint) o;
            int lineNum = b.getLineNumber();
            String line = Integer.toString(lineNum);
            Integer maxInt = (Integer) BreakpointsTreeModelFilter.MAX_LINES.get(b);
            if (maxInt != null) {
                int max = maxInt.intValue();
                int num0 = log10(max) - log10(lineNum);
                if (num0 > 0) {
                    line = zeros(num0) + line;
                }
            }
            return bold(b, NbBundle.getMessage(BreakpointsNodeModel.class, "CTL_Line_Breakpoint", // NOI18N
                    EditorContextBridge.getFileName(b), line));
        } else if (o instanceof FunctionBreakpoint) {
            FunctionBreakpoint b = (FunctionBreakpoint) o;
            String className = ""; // NOI18N
            //NM Commented out code below, because it leads to NPE
            // String[] fs = null; // XXX - Unimplemented
            // if (fs.length > 0) {
            //     className = fs[0];
            // }
            if (b.getFunctionName().equals("")) {
                return bold(b, NbBundle.getMessage(BreakpointsNodeModel.class,
                        "CTL_All_Functions_Breakpoint", getShort(className))); // NOI18N
            } else {
		String clazz = getShort(className);
		if (clazz != null && clazz.length() > 0) {
		    return bold(b, NbBundle.getMessage(BreakpointsNodeModel.class,
			"CTL_Function_Breakpoint_With_Class", getShort(className), b.getFunctionName())); // NOI18N
		} else {
		    return bold(b, NbBundle.getMessage(BreakpointsNodeModel.class,
			"CTL_Function_Breakpoint", b.getFunctionName())); // NOI18N
		}
            }
        } else if (o instanceof AddressBreakpoint) {
            AddressBreakpoint b = (AddressBreakpoint)o;
            return bold(b, NbBundle.getMessage(BreakpointsNodeModel.class, "CTL_Address_Breakpoint", // NOI18N
                    b.getAddress()));
        } else {
            throw new UnknownTypeException(o);
        }
    }
    
    public String getShortDescription (Object o) throws UnknownTypeException {
        if (o instanceof LineBreakpoint) {
            return NbBundle.getMessage(BreakpointsNodeModel.class, "CTL_Line_Breakpoint", // NOI18N
                    EditorContextBridge.getFileName((LineBreakpoint) o),
                    "" + ((LineBreakpoint) o).getLineNumber()); // NOI18N
        } else if (o instanceof FunctionBreakpoint) {
            FunctionBreakpoint b = (FunctionBreakpoint) o;
            String className = "";
            //NM Commented out code below, because it leads to NPE
            // String[] fs = null; // XXX - Unimplemented
            // if (fs.length > 0) {
            //     className = fs [0];
            // }
            if (b.getFunctionName().equals("")) { // NOI18N
                return NbBundle.getMessage(BreakpointsNodeModel.class,
                        "CTL_All_Functions_Breakpoint", className); // NOI18N
            } else {
                return NbBundle.getMessage(BreakpointsNodeModel.class, "CTL_Function_Breakpoint", // NOI18N
                        className, b.getFunctionName());
            }
        } else if (o instanceof AddressBreakpoint) {
            return NbBundle.getMessage(BreakpointsNodeModel.class, "CTL_Address_Breakpoint", // NOI18N
                    EditorContextBridge.getFileName((AddressBreakpoint) o),
                    ((AddressBreakpoint) o).getAddress()); // NOI18N
        } else {
            throw new UnknownTypeException(o);
        }
    }
    
    public String getIconBase(Object o) throws UnknownTypeException {
        boolean disabled = !((Breakpoint) o).isEnabled();
        if (o instanceof LineBreakpoint || o instanceof AddressBreakpoint) {
            String condition = ((GdbBreakpoint) o).getCondition();
            boolean conditional = condition != null && condition.trim().length() > 0;
            if (disabled) {
                if (conditional) {
                    return DISABLED_LINE_CONDITIONAL_BREAKPOINT;
                } else {
                    return DISABLED_LINE_BREAKPOINT;
                }
            } else {
                if (conditional) {
                    return LINE_CONDITIONAL_BREAKPOINT;
                } else {
                    return LINE_BREAKPOINT;
                }
            }
        } else if (o instanceof FunctionBreakpoint) {
            if (disabled) {
                return DISABLED_BREAKPOINT;
            } else {
                return BREAKPOINT;
            }
        } else {
            throw new UnknownTypeException (o);
        }
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }
    
//    private void fireTreeChanged () {
//        Vector v = (Vector) listeners.clone ();
//        int i, k = v.size ();
//        for (i = 0; i < k; i++)
//            ((TreeModelListener) v.get (i)).treeChanged ();
//    }
//    
    
    void fireNodeChanged(GdbBreakpoint b) {
        Vector v = (Vector) listeners.clone();
        int i, k = v.size();
        for (i = 0; i < k; i++) {
            ((ModelListener) v.get(i)).modelChanged(new ModelEvent.NodeChanged(this, b));
        }
    }
    
    static String getShort(String s) {
        if (s.indexOf('*') >= 0) {
            return s;
        }
        int i = s.lastIndexOf('.');
        if (i < 0) {
            return s;
        }
        return s.substring(i + 1);
    }
    
    private GdbBreakpoint currentBreakpoint;
    private String bold(GdbBreakpoint b, String name) {
        return b == currentBreakpoint ?
                BoldVariablesTableModelFilterFirst.toHTML(name, true, false, null) : name;
    }
    
    public void setCurrentBreakpoint(GdbBreakpoint currentBreakpoint) {
        if (this.currentBreakpoint != null) {
            fireNodeChanged(this.currentBreakpoint);
        }
        this.currentBreakpoint = currentBreakpoint;
        if (currentBreakpoint != null) {
            fireNodeChanged(currentBreakpoint);
        }
    }
}
