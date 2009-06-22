/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.debugger.jpda.breakpoints;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.WatchpointEvent;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.request.AccessWatchpointRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.ModificationWatchpointRequest;
import com.sun.jdi.request.WatchpointRequest;

import java.util.List;
import org.netbeans.api.debugger.Breakpoint.VALIDITY;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocatableWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LocationWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.LocatableEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.ModificationWatchpointEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.event.WatchpointEventWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.EventRequestManagerWrapper;
import org.netbeans.modules.debugger.jpda.jdi.request.WatchpointRequestWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;

import org.openide.util.NbBundle;

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
        boolean access = (breakpoint.getBreakpointType () & 
                          FieldBreakpoint.TYPE_ACCESS) != 0;
        try {
            if (access && !VirtualMachineWrapper.canWatchFieldAccess(getVirtualMachine())) {
                setValidity(VALIDITY.INVALID,
                        NbBundle.getMessage(FieldBreakpointImpl.class, "MSG_NoFieldAccess"));
                return ;
            }
            boolean modification = (breakpoint.getBreakpointType () &
                                    FieldBreakpoint.TYPE_MODIFICATION) != 0;
            if (modification && !VirtualMachineWrapper.canWatchFieldModification(getVirtualMachine())) {
                setValidity(VALIDITY.INVALID,
                        NbBundle.getMessage(FieldBreakpointImpl.class, "MSG_NoFieldModification"));
                return ;
            }
            setClassRequests (
                new String[] {breakpoint.getClassName ()},
                new String[0],
                ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED
            );
            checkLoadedClasses (breakpoint.getClassName (), null);
        } catch (InternalExceptionWrapper e) {
        } catch (VMDisconnectedExceptionWrapper e) {
        }
    }
    
    @Override
    protected void classLoaded (List<ReferenceType> referenceTypes) {
        boolean submitted = false;
        for (ReferenceType referenceType : referenceTypes) {
            try {
                Field f = ReferenceTypeWrapper.fieldByName (referenceType, breakpoint.getFieldName ());
                if (f == null) {
                    continue;
                }
                if ( (breakpoint.getBreakpointType () &
                      FieldBreakpoint.TYPE_ACCESS) != 0
                ) {
                    AccessWatchpointRequest awr = EventRequestManagerWrapper.
                        createAccessWatchpointRequest (getEventRequestManager (), f);
                    setFilters(awr);
                    addEventRequest (awr);
                }
                if ( (breakpoint.getBreakpointType () &
                      FieldBreakpoint.TYPE_MODIFICATION) != 0
                ) {
                    ModificationWatchpointRequest mwr = EventRequestManagerWrapper.
                        createModificationWatchpointRequest (getEventRequestManager (), f);
                    setFilters(mwr);
                    addEventRequest (mwr);
                }
                submitted = true;
            } catch (InternalExceptionWrapper e) {
            } catch (ClassNotPreparedExceptionWrapper e) {
            } catch (VMDisconnectedExceptionWrapper e) {
                return ;
            }
        }
        if (submitted) {
            setValidity(VALIDITY.VALID, null);
        } else {
            String name;
            try {
                name = ReferenceTypeWrapper.name(referenceTypes.get(0));
            } catch (InternalExceptionWrapper e) {
                name = e.getLocalizedMessage();
            } catch (VMDisconnectedExceptionWrapper e) {
                return ;
            }
            setValidity(VALIDITY.INVALID,
                    NbBundle.getMessage(FieldBreakpointImpl.class, "MSG_NoField", name, breakpoint.getFieldName ()));
        }
    }
    
    protected EventRequest createEventRequest(EventRequest oldRequest) throws VMDisconnectedExceptionWrapper, InternalExceptionWrapper {
        if (oldRequest instanceof AccessWatchpointRequest) {
            Field field = WatchpointRequestWrapper.field((AccessWatchpointRequest) oldRequest);
            WatchpointRequest awr = EventRequestManagerWrapper.
                    createAccessWatchpointRequest (getEventRequestManager (), field);
            setFilters(awr);
            return awr;
        }
        if (oldRequest instanceof ModificationWatchpointRequest) {
            Field field = WatchpointRequestWrapper.field((ModificationWatchpointRequest) oldRequest);
            WatchpointRequest mwr = EventRequestManagerWrapper.
                    createModificationWatchpointRequest (getEventRequestManager (), field);
            setFilters(mwr);
            return mwr;
        }
        return null;
    }

    private void setFilters(WatchpointRequest wr) throws InternalExceptionWrapper, VMDisconnectedExceptionWrapper {
        JPDAThread[] threadFilters = breakpoint.getThreadFilters(getDebugger());
        if (threadFilters != null && threadFilters.length > 0) {
            for (JPDAThread t : threadFilters) {
                WatchpointRequestWrapper.addThreadFilter(wr, ((JPDAThreadImpl) t).getThreadReference());
            }
        }
        ObjectVariable[] varFilters = breakpoint.getInstanceFilters(getDebugger());
        if (varFilters != null && varFilters.length > 0) {
            for (ObjectVariable v : varFilters) {
                WatchpointRequestWrapper.addInstanceFilter(wr, (ObjectReference) ((JDIVariable) v).getJDIValue());
            }
        }
    }
    
    public boolean processCondition(Event event) {
        ThreadReference thread;
        try {
            if (event instanceof ModificationWatchpointEvent) {
                thread = LocatableEventWrapper.thread((ModificationWatchpointEvent) event);
            } else if (event instanceof AccessWatchpointEvent) {
                thread = LocatableEventWrapper.thread((AccessWatchpointEvent) event);
            } else {
                return true; // Empty condition, always satisfied.
            }
        } catch (InternalExceptionWrapper ex) {
            return true;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return false;
        }
        return processCondition(event, breakpoint.getCondition (), thread, null);
    }

    @Override
    public boolean exec (Event event) {
        try {
            if (event instanceof ModificationWatchpointEvent) {
                ModificationWatchpointEvent me = (ModificationWatchpointEvent) event;
                return perform (
                    event,
                    LocatableEventWrapper.thread(me),
                    LocationWrapper.declaringType(LocatableWrapper.location(me)),
                    ModificationWatchpointEventWrapper.valueToBe(me)
                );
            }
            if (event instanceof AccessWatchpointEvent) {
                AccessWatchpointEvent ae = (AccessWatchpointEvent) event;
                return perform (
                    event,
                    LocatableEventWrapper.thread((WatchpointEvent) event),
                    LocationWrapper.declaringType(LocatableWrapper.location((LocatableEvent) event)),
                    WatchpointEventWrapper.valueCurrent(ae)
                );
            }
        } catch (InternalExceptionWrapper ex) {
            return false;
        } catch (VMDisconnectedExceptionWrapper ex) {
            return false;
        }
        return super.exec (event);
    }
}

