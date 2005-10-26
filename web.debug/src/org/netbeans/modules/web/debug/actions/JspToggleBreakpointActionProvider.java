/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.debug.actions;

import java.util.*;
import java.beans.*;

import org.netbeans.api.debugger.*;
import org.netbeans.api.debugger.jpda.*;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.debugger.*;

import org.netbeans.modules.web.debug.Context;
import org.netbeans.modules.web.debug.JspBreakpointAnnotationListener;
import org.netbeans.modules.web.debug.util.Utils;
import org.netbeans.modules.web.debug.breakpoints.JspLineBreakpoint;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/** 
 * Toggle JSP Breakpoint action provider.
 *
 * @author Martin Grebac
 */
public class JspToggleBreakpointActionProvider extends ActionsProviderSupport implements PropertyChangeListener {
    
    
    private JPDADebugger debugger;

    
    public JspToggleBreakpointActionProvider () {
        Context.addPropertyChangeListener (this);
    }
    
    public JspToggleBreakpointActionProvider (ContextProvider contextProvider) {
        debugger = (JPDADebugger) contextProvider.lookupFirst 
                (null, JPDADebugger.class);
        debugger.addPropertyChangeListener (debugger.PROP_STATE, this);
        Context.addPropertyChangeListener (this);
    }
    
    private void destroy () {
        debugger.removePropertyChangeListener (debugger.PROP_STATE, this);
        Context.removePropertyChangeListener (this);
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        String url = Context.getCurrentURL();
        
        boolean isJsp = Utils.isJsp(url) || Utils.isTag(url);

        //#issue 65969 fix:
        //we allow bp setting only if the file is JSP or TAG file and target server of it's module is NOT WebLogic 9;
        //TODO it should be solved by adding new API into j2eeserver which should announce whether the target server
        //supports JSP debugging or not
        String serverID = Utils.getTargetServerID(url);

        setEnabled(ActionsManager.ACTION_TOGGLE_BREAKPOINT, isJsp && !"WebLogic9".equals(serverID)); //NOI18N
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
        int ln = Context.getCurrentLineNumber ();
        String url = Context.getCurrentURL ();
        if (url == null) return;
                
        // 2) find and remove existing line breakpoint
        JspLineBreakpoint lb = getJspBreakpointAnnotationListener().findBreakpoint(url, ln);        
        if (lb != null) {
            d.removeBreakpoint(lb);
            return;
        }
        lb = JspLineBreakpoint.create(url, ln);
        d.addBreakpoint(lb);
    }

    private JspBreakpointAnnotationListener jspBreakpointAnnotationListener;
    private JspBreakpointAnnotationListener getJspBreakpointAnnotationListener () {
        if (jspBreakpointAnnotationListener == null)
            jspBreakpointAnnotationListener = (JspBreakpointAnnotationListener) 
                DebuggerManager.getDebuggerManager ().lookupFirst 
                (null, JspBreakpointAnnotationListener.class);
        return jspBreakpointAnnotationListener;
    }
}
