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

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerEngine;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;

/**
 * Listens on DebuggerManager and:
 * - loads all breakpoints & watches on startup
 * - listens on all changes of breakpoints and watches (like breakoint / watch
 *     added / removed, or some property change) and saves a new values
 *
 * @author Libor Kotouc
 */
public class PersistenceManager implements LazyDebuggerManagerListener {
    
    private static final String JSP_PROPERTY = "jsp";
    
    public Breakpoint[] initBreakpoints () {
        Properties p = Properties.getDefault ().getProperties ("debugger").
            getProperties (DebuggerManager.PROP_BREAKPOINTS);
        Breakpoint[] breakpoints = (Breakpoint[]) p.getArray (
            JSP_PROPERTY, 
            new Breakpoint [0]
        );
        for (int i = 0; i < breakpoints.length; i++) {
            breakpoints[i].addPropertyChangeListener(this);
        }
        return breakpoints;
    }
    
    public void initWatches () {
    }
    
    public String[] getProperties () {
        return new String [] {
            DebuggerManager.PROP_BREAKPOINTS_INIT,
            DebuggerManager.PROP_BREAKPOINTS,
        };
    }
    
    public void breakpointAdded (Breakpoint breakpoint) {
        if (breakpoint instanceof JspLineBreakpoint) {
            Properties p = Properties.getDefault ().getProperties ("debugger").
                getProperties (DebuggerManager.PROP_BREAKPOINTS);
            p.setArray (
                JSP_PROPERTY, 
                getBreakpoints ()
            );
            breakpoint.addPropertyChangeListener(this);
        }
    }

    public void breakpointRemoved (Breakpoint breakpoint) {
        if (breakpoint instanceof JspLineBreakpoint) {
            Properties p = Properties.getDefault ().getProperties ("debugger").
                getProperties (DebuggerManager.PROP_BREAKPOINTS);
            p.setArray (
                JSP_PROPERTY, 
                getBreakpoints ()
            );
            breakpoint.removePropertyChangeListener(this);
        }
    }
    public void watchAdded (Watch watch) {
    }
    
    public void watchRemoved (Watch watch) {
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getSource() instanceof Breakpoint) {
            Properties.getDefault ().getProperties ("debugger").
                getProperties (DebuggerManager.PROP_BREAKPOINTS).setArray (
                    JSP_PROPERTY,
                    getBreakpoints ()
                );
        }
    }
    
    public void sessionAdded (Session session) {}
    public void sessionRemoved (Session session) {}
    public void engineAdded (DebuggerEngine engine) {}
    public void engineRemoved (DebuggerEngine engine) {}
    
    
    private static Breakpoint[] getBreakpoints () {
        Breakpoint[] bs = DebuggerManager.getDebuggerManager ().
            getBreakpoints ();
        int i, k = bs.length;
        ArrayList bb = new ArrayList ();
        for (i = 0; i < k; i++)
            // We store only the JSP breakpoints
            if (bs[i] instanceof JspLineBreakpoint)
                bb.add (bs [i]);
        bs = new Breakpoint [bb.size ()];
        return (Breakpoint[]) bb.toArray (bs);
    }
}

