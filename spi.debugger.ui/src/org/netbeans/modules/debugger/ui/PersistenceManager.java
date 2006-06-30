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

package org.netbeans.modules.debugger.ui;

import java.beans.PropertyChangeEvent;
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
 * @author Jan Jancura
 */
public class PersistenceManager implements LazyDebuggerManagerListener {
    
    public Breakpoint[] initBreakpoints () {
        return new Breakpoint [0];
    }
    
    public void initWatches () {
        // As a side-effect, creates the watches. WatchesReader is triggered.
        Properties p = Properties.getDefault ().getProperties ("debugger");
        p.getArray (
            DebuggerManager.PROP_WATCHES, 
            new Watch [0]
        );
    }
    
    public String[] getProperties () {
        return new String [] {
            DebuggerManager.PROP_WATCHES_INIT,
            DebuggerManager.PROP_WATCHES
        };
    }
    
    public void breakpointAdded (Breakpoint breakpoint) {
    }

    public void breakpointRemoved (Breakpoint breakpoint) {
    }
    
    public void watchAdded (Watch watch) {
        Properties p = Properties.getDefault ().getProperties ("debugger");
        p.setArray (
            DebuggerManager.PROP_WATCHES, 
            DebuggerManager.getDebuggerManager ().getWatches ()
        );
        watch.addPropertyChangeListener (this);
    }
    
    public void watchRemoved (Watch watch) {
        Properties p = Properties.getDefault ().getProperties ("debugger");
        p.setArray (
            DebuggerManager.PROP_WATCHES, 
            DebuggerManager.getDebuggerManager ().getWatches ()
        );
        watch.removePropertyChangeListener(this);
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        if (evt.getSource() instanceof Watch) {
            Properties.getDefault ().getProperties ("debugger").setArray (
                DebuggerManager.PROP_WATCHES,
                DebuggerManager.getDebuggerManager ().getWatches ()
            );
        }
    }
    
    public void sessionAdded (Session session) {}
    public void sessionRemoved (Session session) {}
    public void engineAdded (DebuggerEngine engine) {}
    public void engineRemoved (DebuggerEngine engine) {}
}
