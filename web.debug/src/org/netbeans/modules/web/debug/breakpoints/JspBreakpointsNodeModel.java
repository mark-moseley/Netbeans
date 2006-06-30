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

package org.netbeans.modules.web.debug.breakpoints;


import org.netbeans.api.debugger.jpda.*;
import org.netbeans.modules.web.debug.Context;

import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.openide.util.NbBundle;


/**
 * @author Martin Grebac
 */
public class JspBreakpointsNodeModel implements NodeModel {

    public static final String LINE_BREAKPOINT =
        "org/netbeans/modules/debugger/resources/breakpointsView/Breakpoint";
    
    public String getDisplayName (Object o) throws UnknownTypeException {
        if (o instanceof JspLineBreakpoint) {
            JspLineBreakpoint b = (JspLineBreakpoint) o;
            return NbBundle.getMessage (JspBreakpointsNodeModel.class,
                    "CTL_Jsp_Line_Breakpoint",
                    Context.getFileName (b),
                    "" + b.getLineNumber()
                );
        } 
        throw new UnknownTypeException(o);
    }
    
    public String getShortDescription (Object o) throws UnknownTypeException {
        if (o instanceof JspLineBreakpoint) {
            return NbBundle.getMessage (
                    JspBreakpointsNodeModel.class,
                    "CTL_Jsp_Line_Breakpoint",
                    Context.getFileName ((JspLineBreakpoint) o),
                    "" + ((JspLineBreakpoint) o).getLineNumber ()
                );
        }
        throw new UnknownTypeException (o);
    }
    
    public String getIconBase (Object o) throws UnknownTypeException {
        if (o instanceof JspLineBreakpoint) {
            return LINE_BREAKPOINT;
        }
        throw new UnknownTypeException (o);
    }

    /** 
     *
     * @param l the listener to add
     */
    public void addModelListener (ModelListener l) {
//        listeners.add (l);
    }

    /** 
     *
     * @param l the listener to remove
     */
    public void removeModelListener (ModelListener l) {
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
    
//    static String getShort (String s) {
//        if (s.indexOf ('*') >= 0) return s;
//        int i = s.lastIndexOf ('.');
//        if (i < 0) return s;
//        return s.substring (i + 1);
//    }
}
