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

package org.netbeans.modules.debugger.jpda.breakpoints;


import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.request.ThreadDeathRequest;
import com.sun.jdi.request.ThreadStartRequest;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.ThreadBreakpoint;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.api.debugger.jpda.JPDAThread;

import org.netbeans.modules.debugger.jpda.util.Executor;


/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public class ThreadBreakpointImpl extends BreakpointImpl implements Executor {

    // variables ...............................................................

    private ThreadBreakpoint              breakpoint;


    // init ....................................................................
    
    public ThreadBreakpointImpl (ThreadBreakpoint presenter, JPDADebuggerImpl session) {
        super (presenter, session);
        breakpoint = presenter;
        set ();
    }
            
       
    // Event impl ..............................................................

    protected void setRequests () {
        try {
            if ( (breakpoint.getBreakpointType () & 
                  breakpoint.TYPE_THREAD_STARTED ) != 0
            ) {
                ThreadStartRequest tsr = getEventRequestManager ().
                    createThreadStartRequest ();
                addEventRequest (tsr);
            }
            if ( (breakpoint.getBreakpointType () & 
                  breakpoint.TYPE_THREAD_DEATH) != 0
            ) {
                ThreadDeathRequest tdr = getVirtualMachine ().
                    eventRequestManager ().createThreadDeathRequest ();
                addEventRequest (tdr);
            }
        } catch (VMDisconnectedException e) {
        }
    }

    public boolean exec (Event event) {
        ThreadReference thread = null;
        if (event instanceof ThreadStartEvent)
            thread = ((ThreadStartEvent) event).thread ();
        else
        if (event instanceof ThreadDeathEvent)
            thread = ((ThreadDeathEvent) event).thread ();

        return perform (
            null,
            thread,
            null,
            thread
        );
    }
}
