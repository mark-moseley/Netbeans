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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.util.Vector;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Breakpoint.VALIDITY;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.ExceptionBreakpoint;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ThreadBreakpoint;
import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.openide.util.NbBundle;


/**
 * @author   Jan Jancura
 */
public class BreakpointsNodeModel implements NodeModel {

    public static final String BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/NonLineBreakpoint";
    public static final String LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint";
    public static final String CURRENT_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/NonLineBreakpointHit";
    public static final String CURRENT_LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/BreakpointHit";
    public static final String DISABLED_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledNonLineBreakpoint";
    public static final String DISABLED_LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledBreakpoint";
    public static final String DISABLED_CURRENT_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledNonLineBreakpointHit";
    public static final String DISABLED_CURRENT_LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledBreakpointHit";
    public static final String LINE_CONDITIONAL_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/ConditionalBreakpoint";
    public static final String CURRENT_LINE_CONDITIONAL_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/ConditionalBreakpointHit";
    public static final String DISABLED_LINE_CONDITIONAL_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/DisabledConditionalBreakpoint";

    private Vector listeners = new Vector ();

    static int log10(int n) {
        int l = 1;
        while ((n = n / 10) > 0) l++;
        return l;
    }
    
    private static final String ZEROS = "            "; // NOI18N
    
    static String zeros(int n) {
        if (n < ZEROS.length()) {
            return ZEROS.substring(0, n);
        } else {
            String z = ZEROS;
            while (z.length() < n) z += " "; // NOI18N
            return z;
        }
    }

    
    public String getDisplayName (Object o) throws UnknownTypeException {
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
            return bold (
                b,
                NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Line_Breakpoint",
                        EditorContextBridge.getFileName (b),
                        line
                    )
            );
        } else
        if (o instanceof ThreadBreakpoint) {
            ThreadBreakpoint b = (ThreadBreakpoint) o;
            if (b.getBreakpointType () == b.TYPE_THREAD_STARTED)
                return bold (b, NbBundle.getMessage (
                    BreakpointsNodeModel.class,
                    "CTL_Thread_Started_Breakpoint"
                ));
            else
            if (b.getBreakpointType () == b.TYPE_THREAD_DEATH)
                return bold (b, NbBundle.getMessage (
                    BreakpointsNodeModel.class,
                    "CTL_Thread_Death_Breakpoint"
                ));
            else
                return bold (b, NbBundle.getMessage (
                    BreakpointsNodeModel.class,
                    "CTL_Thread_Breakpoint"
                ));
        } else
        if (o instanceof FieldBreakpoint) {
            FieldBreakpoint b = (FieldBreakpoint) o;
            if (b.getBreakpointType () == b.TYPE_ACCESS)
                return bold (b,
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Field_Access_Breakpoint",
                        getShort (b.getClassName ()),
                        b.getFieldName ()
                    )
                );
            else if (b.getBreakpointType() == b.TYPE_MODIFICATION)
                return bold (b,
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Field_Modification_Breakpoint",
                        getShort (b.getClassName ()),
                        b.getFieldName ()
                    )
                );
            else
                return bold (b,
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Field_Access_or_Modification_Breakpoint",
                        getShort (b.getClassName ()),
                        b.getFieldName ()
                    )
                );
        } else
        if (o instanceof MethodBreakpoint) {
            MethodBreakpoint b = (MethodBreakpoint) o;
            String className = "";
            String[] fs = b.getClassFilters ();
            if (fs.length > 0) className = fs [0];
            if ("".equals (b.getMethodName ()))
                return bold (b,
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_All_Methods_Breakpoint",
                        getShort (className)
                    )
                );
            else
                return bold (b,
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Method_Breakpoint",
                        getShort (className),
                        b.getMethodName ()
                    )
                );
        } else
        if (o instanceof ClassLoadUnloadBreakpoint) {
            ClassLoadUnloadBreakpoint b = (ClassLoadUnloadBreakpoint) o;
            String className = "";
            String[] fs = b.getClassFilters ();
            if (fs.length > 0)
                className = fs [0];
            else {
                fs = b.getClassExclusionFilters ();
                if (fs.length > 0) className = fs [0];
            }
            if (b.getBreakpointType () == b.TYPE_CLASS_LOADED)
                return bold (b,
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Class_Loaded_Breakpoint",
                        getShort (className)
                    )
                );
            else
            if (b.getBreakpointType () == b.TYPE_CLASS_UNLOADED)
                return bold (b,
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Class_Unloaded_Breakpoint",
                        getShort (className)
                    )
                );
            else
                return bold (b,
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Class_Breakpoint",
                        getShort (className)
                    )
                );
        } else
        if (o instanceof ExceptionBreakpoint) {
            ExceptionBreakpoint b = (ExceptionBreakpoint) o;
            if (b.getCatchType () == b.TYPE_EXCEPTION_CATCHED)
                return bold (b,
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Exception_Catched_Breakpoint",
                        getShort (b.getExceptionClassName ())
                    )
                );
            else
            if (b.getCatchType () == b.TYPE_EXCEPTION_UNCATCHED)
                return bold (b,
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Exception_Uncatched_Breakpoint",
                        getShort (b.getExceptionClassName ())
                    )
               );
            else
                return bold (b,
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Exception_Breakpoint",
                        getShort (b.getExceptionClassName ())
                    )
                );
        } else
        throw new UnknownTypeException (o);
    }
    
    public String getShortDescription (Object o) throws UnknownTypeException {
        String appendMsg = null;
        if (o instanceof Breakpoint) {
            boolean valid = false;
            boolean invalid = false;
            String message = null;
            Breakpoint brkp = (Breakpoint) o;
            Breakpoint.VALIDITY validity = brkp.getValidity();
            valid = validity == Breakpoint.VALIDITY.VALID;
            invalid = validity == Breakpoint.VALIDITY.INVALID;
            message = brkp.getValidityMessage();
            if (valid) {
                appendMsg = NbBundle.getMessage(BreakpointsNodeModel.class,
                                                "CTL_APPEND_BP_Valid");
            }
            if (invalid) {
                if (message != null) {
                    appendMsg = NbBundle.getMessage(BreakpointsNodeModel.class,
                                                    "CTL_APPEND_BP_Invalid_with_reason", message);
                } else {
                    appendMsg = NbBundle.getMessage(BreakpointsNodeModel.class,
                                                    "CTL_APPEND_BP_Invalid");
                }
            }
        }
        String description;
        if (o instanceof LineBreakpoint) {
            description = 
                NbBundle.getMessage (
                    BreakpointsNodeModel.class,
                    "CTL_Line_Breakpoint",
                    EditorContextBridge.getFileName ((LineBreakpoint) o),
                    String.valueOf(((LineBreakpoint) o).getLineNumber ())
                );
        } else
        if (o instanceof ThreadBreakpoint) {
            ThreadBreakpoint b = (ThreadBreakpoint) o;
            if (b.getBreakpointType () == b.TYPE_THREAD_STARTED)
                description = NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Thread_Started_Breakpoint"
                    );
            else
            if (b.getBreakpointType () == b.TYPE_THREAD_DEATH)
                description = NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Thread_Death_Breakpoint"
                    );
            else
                description = NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Thread_Breakpoint"
                    );
        } else
        if (o instanceof FieldBreakpoint) {
            FieldBreakpoint b = (FieldBreakpoint) o;
            if (b.getBreakpointType () == b.TYPE_ACCESS)
                description = 
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Field_Access_Breakpoint",
                        b.getClassName (),
                        b.getFieldName ()
                    );
            else if (b.getBreakpointType() == b.TYPE_MODIFICATION)
                description = 
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Field_Modification_Breakpoint",
                        b.getClassName (),
                        b.getFieldName ()
                    );
            else
                description = 
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Field_Access_or_Modification_Breakpoint",
                        b.getClassName (),
                        b.getFieldName ()
                    );
        } else
        if (o instanceof MethodBreakpoint) {
            MethodBreakpoint b = (MethodBreakpoint) o;
            String className = "";
            String[] fs = b.getClassFilters ();
            if (fs.length > 0) className = fs [0];
            if ("".equals (b.getMethodName ()))
                description =
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_All_Methods_Breakpoint",
                        className
                    );
            else
                description = 
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Method_Breakpoint",
                        className,
                        b.getMethodName ()
                    );
        } else
        if (o instanceof ClassLoadUnloadBreakpoint) {
            ClassLoadUnloadBreakpoint b = (ClassLoadUnloadBreakpoint) o;
            String className = "";
            String[] fs = b.getClassFilters ();
            if (fs.length > 0)
                className = fs [0];
            else {
                fs = b.getClassExclusionFilters ();
                if (fs.length > 0) className = fs [0];
            }
            if (b.getBreakpointType () == b.TYPE_CLASS_LOADED)
                description = 
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Class_Loaded_Breakpoint",
                        className
                    );
            else
            if (b.getBreakpointType () == b.TYPE_CLASS_UNLOADED)
                description = 
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Class_Unloaded_Breakpoint",
                        className
                    );
            else
                description = 
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Class_Breakpoint",
                        className
                    );
        } else
        if (o instanceof ExceptionBreakpoint) {
            ExceptionBreakpoint b = (ExceptionBreakpoint) o;
            if (b.getCatchType () == b.TYPE_EXCEPTION_CATCHED)
                description = 
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Exception_Catched_Breakpoint",
                        b.getExceptionClassName ()
                    );
            else
            if (b.getCatchType () == b.TYPE_EXCEPTION_UNCATCHED)
                description = 
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Exception_Uncatched_Breakpoint",
                        b.getExceptionClassName ()
                    );
            else
                description = 
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Exception_Breakpoint",
                        b.getExceptionClassName ()
                    );
        } else
        throw new UnknownTypeException (o);
        if (appendMsg != null) {
            description = description + " " + appendMsg;
        }
        return description;
    }
    
    public String getIconBase (Object o) throws UnknownTypeException {
        boolean current = currentBreakpoint == o;
        boolean disabled = !((Breakpoint) o).isEnabled();
        boolean invalid = ((Breakpoint) o).getValidity() == VALIDITY.INVALID;
        if (o instanceof LineBreakpoint) {
            String condition = ((LineBreakpoint) o).getCondition();
            boolean conditional = condition != null && condition.trim().length() > 0;
            String iconBase;
            if (current) {
                if (disabled) {
                    if (conditional) {
                        iconBase = DISABLED_LINE_CONDITIONAL_BREAKPOINT;
                    } else {
                        iconBase = DISABLED_CURRENT_LINE_BREAKPOINT;
                    }
                } else {
                    if (conditional) {
                        iconBase = CURRENT_LINE_CONDITIONAL_BREAKPOINT;
                    } else {
                        iconBase = CURRENT_LINE_BREAKPOINT;
                    }
                }
            } else if (disabled) {
                if (conditional) {
                    iconBase = DISABLED_LINE_CONDITIONAL_BREAKPOINT;
                } else {
                    iconBase = DISABLED_LINE_BREAKPOINT;
                }
            } else {
                if (conditional) {
                    iconBase = LINE_CONDITIONAL_BREAKPOINT;
                } else {
                    iconBase = LINE_BREAKPOINT;
                }
            }
            if (invalid && !disabled) {
                iconBase += "_broken";
            }
            return iconBase;
        } else
        if (o instanceof ThreadBreakpoint ||
            o instanceof FieldBreakpoint ||
            o instanceof MethodBreakpoint ||
            o instanceof ClassLoadUnloadBreakpoint ||
            o instanceof ExceptionBreakpoint) {
            
            String iconBase;
            if (current) {
                if (disabled) {
                    iconBase = DISABLED_CURRENT_BREAKPOINT;
                } else {
                    iconBase = CURRENT_BREAKPOINT;
                }
            } else if (disabled) {
                iconBase = DISABLED_BREAKPOINT;
            } else {
                iconBase = BREAKPOINT;
                if (invalid) {
                    iconBase += "_broken";
                }
            }
            return iconBase;
        } else
        throw new UnknownTypeException (o);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
        listeners.add (l);
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
        listeners.remove (l);
    }
    
//    private void fireTreeChanged () {
//        Vector v = (Vector) listeners.clone ();
//        int i, k = v.size ();
//        for (i = 0; i < k; i++)
//            ((TreeModelListener) v.get (i)).treeChanged ();
//    }
//    
    
    void fireNodeChanged (JPDABreakpoint b) {
        Vector v = (Vector) listeners.clone ();
        int i, k = v.size ();
        for (i = 0; i < k; i++)
            ((ModelListener) v.get (i)).modelChanged (
                new ModelEvent.NodeChanged (this, b)
            );
    }
    
    static String getShort (String s) {
        if (s.indexOf ('*') >= 0) return s;
        int i = s.lastIndexOf ('.');
        if (i < 0) return s;
        return s.substring (i + 1);
    }
    
    private JPDABreakpoint currentBreakpoint;
    private String bold (JPDABreakpoint b, String name) {
        return b == currentBreakpoint ?
            BoldVariablesTableModelFilterFirst.toHTML (
                name,
                true,
                false,
                null
            ) :
            name;
    }
    
    public void setCurrentBreakpoint (JPDABreakpoint currentBreakpoint) {
        JPDABreakpoint oldCurrentBreakpoint = this.currentBreakpoint;
        this.currentBreakpoint = currentBreakpoint;
        if (oldCurrentBreakpoint != null)
            fireNodeChanged (oldCurrentBreakpoint);
        if (currentBreakpoint != null)
            fireNodeChanged (currentBreakpoint);
    }
}
