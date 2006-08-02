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

import com.sun.jdi.ReferenceType;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ClassUnloadEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.ClassUnloadRequest;
import com.sun.jdi.VirtualMachine;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.Iterator;
import java.util.logging.Logger;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;

import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;
import org.openide.util.WeakListeners;

/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
*/
public abstract class ClassBasedBreakpoint extends BreakpointImpl {
    
    private String sourceRoot;
    private final Object SOURCE_ROOT_LOCK = new Object();
    private SourceRootsChangedListener srChListener;
    
    private static Logger logger = Logger.getLogger("org.netbeans.modules.debugger.jpda.breakpoints"); // NOI18N

    public ClassBasedBreakpoint (
        JPDABreakpoint breakpoint, 
        JPDADebuggerImpl debugger,
        Session session
    ) {
        super (breakpoint, null, debugger, session);
    }
    
    public ClassBasedBreakpoint (
        JPDABreakpoint breakpoint, 
        BreakpointsReader reader,
        JPDADebuggerImpl debugger,
        Session session
    ) {
        super (breakpoint, reader, debugger, session);
    }
    
    protected final void setSourceRoot(String sourceRoot) {
        synchronized (SOURCE_ROOT_LOCK) {
            this.sourceRoot = sourceRoot;
            if (sourceRoot != null && srChListener == null) {
                srChListener = new SourceRootsChangedListener();
                getDebugger().getEngineContext().addPropertyChangeListener(
                        WeakListeners.propertyChange(srChListener,
                                                     getDebugger().getEngineContext()));
            } else if (sourceRoot == null) {
                srChListener = null; // release the listener
            }
        }
    }
    
    protected final String getSourceRoot() {
        synchronized (SOURCE_ROOT_LOCK) {
            return sourceRoot;
        }
    }
    
    protected boolean isEnabled() {
        synchronized (SOURCE_ROOT_LOCK) {
            String sourceRoot = getSourceRoot();
            if (sourceRoot == null) {
                return true;
            }
            String[] sourceRoots = getDebugger().getEngineContext().getSourceRoots();
            for (int i = 0; i < sourceRoots.length; i++) {
                if (sourceRoot.equals(sourceRoots[i])) {
                    return true;
                }
            }
            return false;
        }
    }
    
    protected void setClassRequests (
        String[] classFilters,
        String[] classExclusionFilters,
        int breakpointType
    ) {
        try {
            if ((breakpointType & ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED) != 0
            ) {
                int i, k = classFilters.length;
                for (i = 0; i < k; i++) {
                    ClassPrepareRequest cpr = getEventRequestManager ().
                        createClassPrepareRequest ();
                    cpr.addClassFilter (classFilters [i]);
                    logger.fine("Set class load request: " + classFilters [i]);
                    addEventRequest (cpr);
                }
                k = classExclusionFilters.length;
                for (i = 0; i < k; i++) {
                    ClassPrepareRequest cpr = getEventRequestManager ().
                        createClassPrepareRequest ();
                    cpr.addClassExclusionFilter (classExclusionFilters [i]);
                    logger.fine("Set class load exclusion request: " + classExclusionFilters [i]);
                    addEventRequest (cpr);
                }
            }
            if ((breakpointType & ClassLoadUnloadBreakpoint.TYPE_CLASS_UNLOADED) != 0
            ) {
                int i, k = classFilters.length;
                for (i = 0; i < k; i++) {
                    ClassUnloadRequest cur = getEventRequestManager ().
                        createClassUnloadRequest ();
                    cur.addClassFilter (classFilters [i]);
                    logger.fine("Set class unload request: " + classFilters [i]);
                    addEventRequest (cur);
                }
                k = classExclusionFilters.length;
                for (i = 0; i < k; i++) {
                    ClassUnloadRequest cur = getEventRequestManager ().
                        createClassUnloadRequest ();
                    cur.addClassExclusionFilter (classExclusionFilters [i]);
                    logger.fine("Set class unload exclusion request: " + classExclusionFilters [i]);
                    addEventRequest (cur);
                }
            }
        } catch (VMDisconnectedException e) {
        }
    }
    
    protected boolean checkLoadedClasses (
        String className, 
        boolean all
    ) {
        logger.fine("Check loaded classes: " + className + " : " + all);
        VirtualMachine vm = getVirtualMachine ();
        if (vm == null) return false;
        boolean matched = false;
        try {
            Iterator i = null;
            if (all) {
                i = vm.allClasses ().iterator ();
            } else {
                i = vm.classesByName (className).iterator ();
            }
            while (i.hasNext ()) {
                ReferenceType referenceType = (ReferenceType) i.next ();
//                if (verbose)
//                    System.out.println("B     cls: " + referenceType);
                if (i != null) {
                    String name = referenceType.name ();
                    if (match (name, className)) {
                        logger.fine(" Class loaded: " + referenceType);
                        classLoaded (referenceType);
                        matched = true;
                    }
                }
            }
        } catch (VMDisconnectedException e) {
        }
        return matched;
    }

    public boolean exec (Event event) {
        if (event instanceof ClassPrepareEvent) {
            logger.fine(" Class loaded: " + ((ClassPrepareEvent) event).referenceType ());
            classLoaded (((ClassPrepareEvent) event).referenceType ());
        } else if (event instanceof ClassUnloadEvent) {
            logger.fine(" Class unloaded: " + ((ClassPrepareEvent) event).referenceType ());
            classUnloaded (((ClassUnloadEvent) event).className ());
        }
        return true;
    }
    
    protected void classLoaded (ReferenceType referenceType) {}
    protected void classUnloaded (String className) {}
    
    
    private class SourceRootsChangedListener implements PropertyChangeListener {
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (SourcePathProvider.PROP_SOURCE_ROOTS.equals(evt.getPropertyName())) {
                update();
            }
        }
        
    }
}

