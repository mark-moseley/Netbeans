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

import com.sun.jdi.Field;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.WatchpointEvent;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.request.AccessWatchpointRequest;
import com.sun.jdi.request.ModificationWatchpointRequest;

import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;

import org.netbeans.api.debugger.Session;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public class FieldBreakpointImpl extends ClassBasedBreakpoint {

    
    private FieldBreakpoint breakpoint;
    
    
    public FieldBreakpointImpl (FieldBreakpoint breakpoint, JPDADebuggerImpl debugger, Session session) {
        super (breakpoint, debugger, session);
        this.breakpoint = breakpoint;
        set ();
    }
    
    protected void setRequests () {
        setClassRequests (
            new String[] {breakpoint.getClassName ()},
            new String[0],
            ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED
        );
        checkLoadedClasses (breakpoint.getClassName (), true);
    }
    
    protected void classLoaded (ReferenceType referenceType) {
        Field f = referenceType.fieldByName (breakpoint.getFieldName ());
        try {
            if ( (breakpoint.getBreakpointType () & 
                  FieldBreakpoint.TYPE_ACCESS) != 0
            ) {
                AccessWatchpointRequest awr = getEventRequestManager ().
                    createAccessWatchpointRequest (f);
                addEventRequest (awr);
            }
            if ( (breakpoint.getBreakpointType () & 
                  FieldBreakpoint.TYPE_MODIFICATION) != 0
            ) {
                ModificationWatchpointRequest mwr = getEventRequestManager ().
                    createModificationWatchpointRequest (f);
                addEventRequest (mwr);
            }
        } catch (VMDisconnectedException e) {
        }
    }

    public boolean exec (Event event) {
        if (event instanceof ModificationWatchpointEvent)
            return perform (
                breakpoint.getCondition (),
                ((WatchpointEvent) event).thread (),
                ((LocatableEvent) event).location ().declaringType (),
                ((ModificationWatchpointEvent) event).valueToBe ()
            );
        if (event instanceof AccessWatchpointEvent)
            return perform (
                breakpoint.getCondition (),
                ((WatchpointEvent) event).thread (),
                ((LocatableEvent) event).location ().declaringType (),
                ((AccessWatchpointEvent) event).valueCurrent ()
            );
        return super.exec (event);
    }
}

