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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.util.List;

import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
 * The implementation of JPDAThread.
 */
public final class JPDAThreadImpl implements JPDAThread {
    
    /**
     * Suspended property of the thread. Fired when isSuspended() changes.
     */
    public static final String PROP_SUSPENDED = "suspended";
    
    private ThreadReference     threadReference;
    private JPDADebuggerImpl    debugger;
    private boolean             suspended;
    private PropertyChangeSupport pch = new PropertyChangeSupport(this);

    public JPDAThreadImpl (
        ThreadReference     threadReference,
        JPDADebuggerImpl    debugger
    ) {
        this.threadReference = threadReference;
        this.debugger = debugger;
        suspended = threadReference.isSuspended();
    }

    /**
     * Getter for the name of thread property.
     *
     * @return name of thread.
     */
    public String getName () {
        try {
            return threadReference.name ();
        } catch (ObjectCollectedException ex) {
            return "";
        } catch (VMDisconnectedException ex) {
            return "";
        }
    }
    
    /**
    * Returns parent thread group.
    *
    * @return parent thread group.
    */
    public JPDAThreadGroup getParentThreadGroup () {
        try {
            ThreadGroupReference tgr = threadReference.threadGroup ();
            if (tgr == null) return null;
            return debugger.getThreadGroup(tgr);
        } catch (ObjectCollectedException ex) {
            return null;
        } catch (VMDisconnectedException ex) {
            return null;
        }
    }

    /**
     * Returns line number of the location this thread stopped at.
     * The thread should be suspended at the moment this method is called.
     *
     * @return  line number of the current location if the thread is suspended,
     *          contains at least one frame and the topmost frame does not
     *          represent a native method invocation; <CODE>-1</CODE> otherwise
     * @see  CallStackFrame
    */
    public int getLineNumber (String stratum) {
        try {
            if (threadReference.frameCount () < 1) return -1;
            return threadReference.frame (0).location ().lineNumber (stratum);
        } catch (ObjectCollectedException ex) {
        } catch (InvalidStackFrameException ex) {
        } catch (IncompatibleThreadStateException ex) {
        } catch (VMDisconnectedException ex) {
        }
        return -1;
    }

    /**
     * Returns current state of this thread.
     *
     * @return current state of this thread
     */
    public int getState () {
        try {
            return threadReference.status ();
        } catch (ObjectCollectedException ex) {
        } catch (VMDisconnectedException ex) {
        }
        return STATE_UNKNOWN;
    }
    
    /**
     * Returns true if this thread is suspended by debugger.
     *
     * @return true if this thread is suspended by debugger
     */
    public boolean isSuspended () {
        try {
            return threadReference.isSuspended ();
        } catch (ObjectCollectedException ex) {
        } catch (VMDisconnectedException ex) {
        }
        return false;
    }

    /**
    * If this thread is suspended returns class name where this thread is stopped.
    *
    * @return class name where this thread is stopped.
    */
    public String getClassName () {
        try {
            if (threadReference.frameCount () < 1) return "";
            return threadReference.frame (0).location ().declaringType ().name ();
        } catch (ObjectCollectedException ex) {
        } catch (InvalidStackFrameException ex) {
        } catch (IncompatibleThreadStateException ex) {
        } catch (VMDisconnectedException ex) {
        }
        return "";
    }

    /**
    * If this thread is suspended returns method name where this thread is stopped.
    *
    * @return method name where this thread is stopped.
    */
    public String getMethodName () {
        try {
            if (threadReference.frameCount () < 1) return "";
            return threadReference.frame (0).location ().method ().name ();
        } catch (ObjectCollectedException ex) {
        } catch (InvalidStackFrameException ex) {
        } catch (IncompatibleThreadStateException ex) {
        } catch (VMDisconnectedException ex) {
        }
        return "";
    }
    
    /**
    * Returns name of file of this frame or null if thread has no frame.
    *
    * @return Returns name of file of this frame.
    */
    public String getSourceName (String stratum) throws AbsentInformationException {
        try {
            if (threadReference.frameCount () < 1) return "";
            return threadReference.frame (0).location ().sourceName (stratum);
        } catch (ObjectCollectedException ex) {
        } catch (InvalidStackFrameException ex) {
        } catch (IncompatibleThreadStateException ex) {
        } catch (VMDisconnectedException ex) {
        }
        return "";
    }
    
    /**
    * Returns name of file of this frame or null if thread has no frame.
    *
    * @return Returns name of file of this frame.
    */
    public String getSourcePath (String stratum) 
    throws AbsentInformationException {
        try {
            if (threadReference.frameCount () < 1) return "";
            return threadReference.frame (0).location ().sourcePath (stratum);
        } catch (ObjectCollectedException ex) {
        } catch (InvalidStackFrameException ex) {
        } catch (IncompatibleThreadStateException ex) {
        } catch (VMDisconnectedException ex) {
        }
        return "";
    }
    
    /**
     * Returns call stack for this thread.
     *
     * @throws AbsentInformationException if the thread is running or not able
     *         to return callstack. If the thread is in an incompatible state
     *         (e.g. running), the AbsentInformationException has
     *         IncompatibleThreadStateException as a cause.
     *         If the thread is collected, the AbsentInformationException has
     *         ObjectCollectedException as a cause.
     * @return call stack
     */
    public CallStackFrame[] getCallStack () throws AbsentInformationException {
        return getCallStack (0, getStackDepth ());
    }
    
    private Object lastBottomSF;
    
    /**
     * Returns call stack for this thread on the given indexes.
     *
     * @param from a from index, inclusive
     * @param to a to index, exclusive
     * @throws AbsentInformationException if the thread is running or not able
     *         to return callstack. If the thread is in an incompatible state
     *         (e.g. running), the AbsentInformationException has
     *         IncompatibleThreadStateException as a cause.
     *         If the thread is collected, the AbsentInformationException has
     *         ObjectCollectedException as a cause.
     * @return call stack
     */
    public CallStackFrame[] getCallStack (int from, int to) 
    throws AbsentInformationException {
        try {
            int max = threadReference.frameCount();
            from = Math.min(from, max);
            to = Math.min(to, max);
            List l = threadReference.frames (from, to - from);
            int n = l.size();
            CallStackFrame[] frames = new CallStackFrame[n];
            for (int i = 0; i < n; i++) {
                frames[i] = new CallStackFrameImpl((StackFrame) l.get(i), from + i, debugger);
            }
            return frames;
        } catch (IncompatibleThreadStateException ex) {
            AbsentInformationException aiex = new AbsentInformationException(ex.getLocalizedMessage());
            aiex.initCause(ex);
            throw aiex;
        } catch (ObjectCollectedException ocex) {
            AbsentInformationException aiex = new AbsentInformationException(ocex.getLocalizedMessage());
            aiex.initCause(ocex);
            throw aiex;
        } catch (VMDisconnectedException ex) {
            return new CallStackFrame [0];
        }
    }
    
    /**
     * Returns length of current call stack.
     *
     * @return length of current call stack
     */
    public int getStackDepth () {
        try {
            return threadReference.frameCount ();
        } catch (ObjectCollectedException ex) {
        } catch (IncompatibleThreadStateException e) {
        }
        return 0;
    }
    
    /**
     * Suspends thread.
     */
    public void suspend () {
        Boolean suspendedToFire = null;
        synchronized (this) {
            try {
                if (!isSuspended ()) {
                    threadReference.suspend ();
                    suspendedToFire = Boolean.TRUE;
                }
                suspended = true;
            } catch (ObjectCollectedException ex) {
            } catch (VMDisconnectedException ex) {
            }
        }
        if (suspendedToFire != null) {
            pch.firePropertyChange(PROP_SUSPENDED,
                    Boolean.valueOf(!suspendedToFire.booleanValue()),
                    suspendedToFire);
        }
    }
    
    /**
     * Unsuspends thread.
     */
    public void resume () {
        Boolean suspendedToFire = null;
        synchronized (this) {
            try {
                if (isSuspended ()) {
                    int count = threadReference.suspendCount ();
                    while (count > 0) {
                        threadReference.resume (); count--;
                    }
                    suspendedToFire = Boolean.FALSE;
                }
                suspended = false;
            } catch (ObjectCollectedException ex) {
            } catch (VMDisconnectedException ex) {
            }
        }        
        if (suspendedToFire != null) {
            pch.firePropertyChange(PROP_SUSPENDED,
                    Boolean.valueOf(!suspendedToFire.booleanValue()),
                    suspendedToFire);
        }
    }
    
    public void notifyToBeRunning() {
        Boolean suspendedToFire = null;
        synchronized (this) {
            if (suspended) {
                suspended = false;
                suspendedToFire = Boolean.FALSE;
            }
        }
        if (suspendedToFire != null) {
            pch.firePropertyChange(PROP_SUSPENDED,
                    Boolean.valueOf(!suspendedToFire.booleanValue()),
                    suspendedToFire);
        }
    }
    
    public void notifySuspended() {
        Boolean suspendedToFire = null;
        synchronized (this) {
            if (!suspended && isSuspended()) {
                suspended = true;
                suspendedToFire = Boolean.TRUE;
            }
        }
        if (suspendedToFire != null) {
            pch.firePropertyChange(PROP_SUSPENDED,
                    Boolean.valueOf(!suspendedToFire.booleanValue()),
                    suspendedToFire);
        }
    }
    
    public void interrupt() {
        try {
            if (isSuspended ()) return;
            threadReference.interrupt();
        } catch (ObjectCollectedException ex) {
        } catch (VMDisconnectedException ex) {
        }
    }
    
    /**
     * Sets this thread current.
     *
     * @see JPDADebugger#getCurrentThread
     */
    public void makeCurrent () {
        debugger.setCurrentThread (this);
    }
    
    /**
     * Returns monitor this thread is waiting on.
     *
     * @return monitor this thread is waiting on
     */
    public ObjectVariable getContendedMonitor () {
        try {
            if (!isSuspended()) return null;
            ObjectReference or = threadReference.currentContendedMonitor ();
            if (or == null) return null;
            return new ThisVariable (debugger, or, "");
        } catch (ObjectCollectedException ex) {
        } catch (IncompatibleThreadStateException e) {
        } catch (UnsupportedOperationException e) {
            // if JVM deos not support this feature - not a problem
        }
        return null;
    }
    
    /**
     * Returns monitors owned by this thread.
     *
     * @return monitors owned by this thread
     */
    public ObjectVariable[] getOwnedMonitors () {
        try {
            if (!isSuspended()) return new ObjectVariable [0];
            List l = threadReference.ownedMonitors ();
            int i, k = l.size ();
            ObjectVariable[] vs = new ObjectVariable [k];
            for (i = 0; i < k; i++) {
                vs [i] = new ThisVariable (debugger, (ObjectReference) l.get (i), "");
            }
            return vs;
        } catch (ObjectCollectedException ex) {
        } catch (IncompatibleThreadStateException e) {
        } catch (UnsupportedOperationException e) {
        }
        return new ObjectVariable [0];
    }
    
    public ThreadReference getThreadReference () {
        return threadReference;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pch.addPropertyChangeListener(l);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pch.removePropertyChangeListener(l);
    }
    
    private void fireSuspended(boolean suspended) {
        pch.firePropertyChange(PROP_SUSPENDED,
                Boolean.valueOf(!suspended), Boolean.valueOf(suspended));
    }
}
