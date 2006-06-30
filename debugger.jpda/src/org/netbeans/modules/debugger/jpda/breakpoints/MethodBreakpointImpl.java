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

package org.netbeans.modules.debugger.jpda.breakpoints;

import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ReferenceType;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.request.BreakpointRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

/**
* Implementation of method breakpoint.
*
* @author   Jan Jancura
*/
public class MethodBreakpointImpl extends ClassBasedBreakpoint {
    
    private static final boolean verbose = 
        System.getProperty ("netbeans.debugger.breakpoints") != null;

    
    private MethodBreakpoint breakpoint;
    
    
    public MethodBreakpointImpl (MethodBreakpoint breakpoint, JPDADebuggerImpl debugger, Session session) {
        super (breakpoint, debugger, session);
        this.breakpoint = breakpoint;
        set ();
    }
    
    protected void setRequests () {
        setClassRequests (
            breakpoint.getClassFilters (), 
            breakpoint.getClassExclusionFilters (), 
            ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED
        );
        checkLoadedClasses (breakpoint.getClassFilters () [0], false);
    }

    public boolean exec (Event event) {
        if (event instanceof BreakpointEvent)
            return perform (
                breakpoint.getCondition (),
                ((BreakpointEvent) event).thread (),
                ((LocatableEvent) event).location ().declaringType (),
                null
            );
        return super.exec (event);
    }
    
    protected void classLoaded (ReferenceType referenceType) {
        if (verbose)
            System.out.println ("B class loaded: " + referenceType);

        List locations = new ArrayList ();
        Iterator methods = referenceType.methods ().iterator ();
        while (methods.hasNext ()) {
            Method method = (Method) methods.next ();
            if ( (match (method.name (), breakpoint.getMethodName ()) ||
                  breakpoint.getMethodName().equals("")) &&
                  method.location () != null &&
                  !method.isNative()
            )
                locations.add (method.location ());
        }
        Iterator it = locations.iterator ();
        while (it.hasNext ()) {
            Location location = (Location) it.next ();
            try {
                BreakpointRequest br = getEventRequestManager ().
                    createBreakpointRequest (location);
                addEventRequest (br);
            } catch (VMDisconnectedException e) {
            }
        }
    }
}

