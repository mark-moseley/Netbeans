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

package org.netbeans.modules.debugger.jpda.ui.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;


import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.ui.breakpoints.BreakpointAnnotationListener;
import org.netbeans.spi.debugger.ActionsProviderSupport;

import org.openide.util.NbBundle;


/** 
 *
 * @author   Jan Jancura
 */
public class ToggleBreakpointActionProvider extends ActionsProviderSupport 
implements PropertyChangeListener {
    
    private JPDADebugger debugger;

    
    public ToggleBreakpointActionProvider () {
        EditorContextBridge.addPropertyChangeListener (this);
    }
    
    public ToggleBreakpointActionProvider (ContextProvider lookupProvider) {
        debugger = (JPDADebugger) lookupProvider.lookupFirst 
                (null, JPDADebugger.class);
        debugger.addPropertyChangeListener (debugger.PROP_STATE, this);
        EditorContextBridge.addPropertyChangeListener (this);
    }
    
    private void destroy () {
        debugger.removePropertyChangeListener (debugger.PROP_STATE, this);
        EditorContextBridge.removePropertyChangeListener (this);
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        setEnabled (
            ActionsManager.ACTION_TOGGLE_BREAKPOINT,
            (EditorContextBridge.getCurrentLineNumber () >= 0) && 
            (EditorContextBridge.getCurrentURL ().endsWith (".java"))
        );
        if ( debugger != null && 
             debugger.getState () == debugger.STATE_DISCONNECTED
        ) 
            destroy ();
    }
    
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }
    
    public void doAction (Object action) {
        DebuggerManager d = DebuggerManager.getDebuggerManager ();
        
        // 1) get source name & line number
        int ln = EditorContextBridge.getCurrentLineNumber ();
        String url = EditorContextBridge.getCurrentURL ();
        if (url.trim ().equals ("")) return;
        
        // 2) find and remove existing line breakpoint
        LineBreakpoint lb = getBreakpointAnnotationListener ().findBreakpoint (
            url, ln
        );
        if (lb != null) {
            d.removeBreakpoint (lb);
            return;
        }
//        Breakpoint[] bs = d.getBreakpoints ();
//        int i, k = bs.length;
//        for (i = 0; i < k; i++) {
//            if (!(bs [i] instanceof LineBreakpoint)) continue;
//            LineBreakpoint lb = (LineBreakpoint) bs [i];
//            if (ln != lb.getLineNumber ()) continue;
//            if (!url.equals (lb.getURL ())) continue;
//            d.removeBreakpoint (lb);
//            return;
//        }
        
        // 3) create a new line breakpoint
        lb = LineBreakpoint.create (
            url,
            ln
        );
        lb.setPrintText (
            NbBundle.getBundle (ToggleBreakpointActionProvider.class).getString 
                ("CTL_Line_Breakpoint_Print_Text")
        );
        d.addBreakpoint (lb);
    }
    
    private BreakpointAnnotationListener breakpointAnnotationListener;
    private BreakpointAnnotationListener getBreakpointAnnotationListener () {
        if (breakpointAnnotationListener == null)
            breakpointAnnotationListener = (BreakpointAnnotationListener) 
                DebuggerManager.getDebuggerManager ().lookupFirst 
                (null, BreakpointAnnotationListener.class);
        return breakpointAnnotationListener;
    }
}
