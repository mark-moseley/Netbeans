/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui.models;

import java.net.URL;
import java.text.MessageFormat;
import java.util.Vector;

import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.ExceptionBreakpoint;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ThreadBreakpoint;
import org.netbeans.modules.debugger.jpda.ui.Context;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelListener;
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

//    private Vector listeners = new Vector ();
    
    
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return "Name";
        } else
        if (o instanceof LineBreakpoint) {
            LineBreakpoint b = (LineBreakpoint) o;
            return new MessageFormat (
                NbBundle.getMessage (
                    BreakpointsNodeModel.class,
                    "CTL_Line_Breakpoint"
                )).format (new Object[] {
                    Context.getFileName ((LineBreakpoint) o),
                    "" + ((LineBreakpoint) o).getLineNumber ()
                });
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
                return new MessageFormat (
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Field_Access_Breakpoint"
                    )).format (new Object[] {
                        getShort (b.getClassName ()),
                        b.getFieldName ()
                    });
            else
                return new MessageFormat (
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Field_Modification_Breakpoint"
                    )).format (new Object[] {
                        getShort (b.getClassName ()),
                        b.getFieldName ()
                    });
        } else
        if (o instanceof MethodBreakpoint) {
            MethodBreakpoint b = (MethodBreakpoint) o;
            String className = "";
            String[] fs = b.getClassFilters ();
            if (fs.length > 0) className = fs [0];
            if (b.getMethodName ().equals (""))
                return new MessageFormat (
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_All_Methods_Breakpoint"
                    )).format (new Object[] {
                        getShort (className)
                    });
            else
                return new MessageFormat (
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Method_Breakpoint"
                    )).format (new Object[] {
                        getShort (className),
                        b.getMethodName ()
                    });
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
                return new MessageFormat (
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Class_Loaded_Breakpoint"
                    )).format (new Object[] {
                        getShort (className)
                    });
            else
            if (b.getBreakpointType () == b.TYPE_CLASS_UNLOADED)
                return new MessageFormat (
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Class_Unloaded_Breakpoint"
                    )).format (new Object[] {
                        getShort (className)
                    });
            else
                return new MessageFormat (
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Class_Breakpoint"
                    )).format (new Object[] {
                        getShort (className)
                    });
        } else
        if (o instanceof ExceptionBreakpoint) {
            ExceptionBreakpoint b = (ExceptionBreakpoint) o;
            if (b.getCatchType () == b.TYPE_EXCEPTION_CATCHED)
                return new MessageFormat (
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Exception_Catched_Breakpoint"
                    )).format (new Object[] {
                        getShort (b.getExceptionClassName ())
                    });
            else
            if (b.getCatchType () == b.TYPE_EXCEPTION_UNCATCHED)
                return new MessageFormat (
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Exception_Uncatched_Breakpoint"
                    )).format (new Object[] {
                        getShort (b.getExceptionClassName ())
                    });
            else
                return new MessageFormat (
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Exception_Breakpoint"
                    )).format (new Object[] {
                        getShort (b.getExceptionClassName ())
                    });
        } else
        throw new UnknownTypeException (o);
    }
    
    public String getShortDescription (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return TreeModel.ROOT;
        } else
        if (o instanceof LineBreakpoint) {
            return new MessageFormat (
                NbBundle.getMessage (
                    BreakpointsNodeModel.class,
                    "CTL_Line_Breakpoint"
                )).format (new Object[] {
                    Context.getFileName ((LineBreakpoint) o),
                    "" + ((LineBreakpoint) o).getLineNumber ()
                });
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
                return new MessageFormat (
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Field_Access_Breakpoint"
                    )).format (new Object[] {
                        b.getClassName (),
                        b.getFieldName ()
                    });
            else
                return new MessageFormat (
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Field_Modification_Breakpoint"
                    )).format (new Object[] {
                        b.getClassName (),
                        b.getFieldName ()
                    });
        } else
        if (o instanceof MethodBreakpoint) {
            MethodBreakpoint b = (MethodBreakpoint) o;
            String className = "";
            String[] fs = b.getClassFilters ();
            if (fs.length > 0) className = fs [0];
            if (b.getMethodName ().equals (""))
                return new MessageFormat (
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_All_Methods_Breakpoint"
                    )).format (new Object[] {
                        className
                    });
            else
                return new MessageFormat (
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Method_Breakpoint"
                    )).format (new Object[] {
                        className,
                        b.getMethodName ()
                    });
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
                return new MessageFormat (
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Class_Loaded_Breakpoint"
                    )).format (new Object[] {
                        className
                    });
            else
            if (b.getBreakpointType () == b.TYPE_CLASS_UNLOADED)
                return new MessageFormat (
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Class_Unloaded_Breakpoint"
                    )).format (new Object[] {
                        className
                    });
            else
                return new MessageFormat (
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Class_Breakpoint"
                    )).format (new Object[] {
                        className
                    });
        } else
        if (o instanceof ExceptionBreakpoint) {
            ExceptionBreakpoint b = (ExceptionBreakpoint) o;
            if (b.getCatchType () == b.TYPE_EXCEPTION_CATCHED)
                return new MessageFormat (
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Exception_Catched_Breakpoint"
                    )).format (new Object[] {
                        b.getExceptionClassName ()
                    });
            else
            if (b.getCatchType () == b.TYPE_EXCEPTION_UNCATCHED)
                return new MessageFormat (
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Exception_Uncatched_Breakpoint"
                    )).format (new Object[] {
                        b.getExceptionClassName ()
                    });
            else
                return new MessageFormat (
                    NbBundle.getMessage (
                        BreakpointsNodeModel.class,
                        "CTL_Exception_Breakpoint"
                    )).format (new Object[] {
                        b.getExceptionClassName ()
                    });
        } else
        throw new UnknownTypeException (o);
    }
    
    public String getIconBase (Object o) throws UnknownTypeException {
        if (o == TreeModel.ROOT) {
            return BREAKPOINT;
        } else
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
    public void addTreeModelListener (TreeModelListener l) {
//        listeners.add (l);
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeTreeModelListener (TreeModelListener l) {
//        listeners.remove (l);
    }
    
//    private void fireTreeChanged () {
//        Vector v = (Vector) listeners.clone ();
//        int i, k = v.size ();
//        for (i = 0; i < k; i++)
//            ((TreeModelListener) v.get (i)).treeChanged ();
//    }
//    
//    private void fireTreeNodeChanged (Object parent) {
//        Vector v = (Vector) listeners.clone ();
//        int i, k = v.size ();
//        for (i = 0; i < k; i++)
//            ((TreeModelListener) v.get (i)).treeNodeChanged (parent);
//    }
    
    static String getShort (String s) {
        if (s.indexOf ('*') >= 0) return s;
        int i = s.lastIndexOf ('.');
        if (i < 0) return s;
        return s.substring (i + 1);
    }
}
