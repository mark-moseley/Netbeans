/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui.models;

import java.net.URL;
import java.util.Vector;
import org.netbeans.api.debugger.jpda.CallStackFrame;

import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.ExceptionBreakpoint;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ThreadBreakpoint;
import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
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
            else
                return bold (b,
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Field_Modification_Breakpoint",
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
            if (b.getMethodName ().equals (""))
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
        if (o instanceof LineBreakpoint) {
            return 
                NbBundle.getMessage (
                    BreakpointsNodeModel.class,
                    "CTL_Line_Breakpoint",
                    EditorContextBridge.getFileName ((LineBreakpoint) o),
                    "" + ((LineBreakpoint) o).getLineNumber ()
                );
        } else
        if (o instanceof ThreadBreakpoint) {
            ThreadBreakpoint b = (ThreadBreakpoint) o;
            if (b.getBreakpointType () == b.TYPE_THREAD_STARTED)
                return NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Thread_Started_Breakpoint"
                    );
            else
            if (b.getBreakpointType () == b.TYPE_THREAD_DEATH)
                return NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Thread_Death_Breakpoint"
                    );
            else
                return NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Thread_Breakpoint"
                    );
        } else
        if (o instanceof FieldBreakpoint) {
            FieldBreakpoint b = (FieldBreakpoint) o;
            if (b.getBreakpointType () == b.TYPE_ACCESS)
                return 
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Field_Access_Breakpoint",
                        b.getClassName (),
                        b.getFieldName ()
                    );
            else
                return 
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Field_Modification_Breakpoint",
                        b.getClassName (),
                        b.getFieldName ()
                    );
        } else
        if (o instanceof MethodBreakpoint) {
            MethodBreakpoint b = (MethodBreakpoint) o;
            String className = "";
            String[] fs = b.getClassFilters ();
            if (fs.length > 0) className = fs [0];
            if (b.getMethodName ().equals (""))
                return
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_All_Methods_Breakpoint",
                        className
                    );
            else
                return 
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
                return 
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Class_Loaded_Breakpoint",
                        className
                    );
            else
            if (b.getBreakpointType () == b.TYPE_CLASS_UNLOADED)
                return 
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Class_Unloaded_Breakpoint",
                        className
                    );
            else
                return 
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Class_Breakpoint",
                        className
                    );
        } else
        if (o instanceof ExceptionBreakpoint) {
            ExceptionBreakpoint b = (ExceptionBreakpoint) o;
            if (b.getCatchType () == b.TYPE_EXCEPTION_CATCHED)
                return 
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Exception_Catched_Breakpoint",
                        b.getExceptionClassName ()
                    );
            else
            if (b.getCatchType () == b.TYPE_EXCEPTION_UNCATCHED)
                return 
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Exception_Uncatched_Breakpoint",
                        b.getExceptionClassName ()
                    );
            else
                return 
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Exception_Breakpoint",
                        b.getExceptionClassName ()
                    );
        } else
        throw new UnknownTypeException (o);
    }
    
    public String getIconBase (Object o) throws UnknownTypeException {
        if (o instanceof LineBreakpoint) {
            return LINE_BREAKPOINT;
        } else
        if (o instanceof ThreadBreakpoint) {
            return BREAKPOINT;
        } else
        if (o instanceof FieldBreakpoint) {
            return BREAKPOINT;
        } else
        if (o instanceof MethodBreakpoint) {
            return BREAKPOINT;
        } else
        if (o instanceof ClassLoadUnloadBreakpoint) {
            return BREAKPOINT;
        } else
        if (o instanceof ExceptionBreakpoint) {
            return BREAKPOINT;
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
        if (this.currentBreakpoint != null)
            fireNodeChanged (this.currentBreakpoint);
        this.currentBreakpoint = currentBreakpoint;
        if (currentBreakpoint != null)
            fireNodeChanged (currentBreakpoint);
    }
}
