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

package org.netbeans.modules.debugger.jpda.models;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidStackFrameException;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;

import java.beans.PropertyChangeListener;
import java.util.List;

import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.spi.viewmodel.NoInformationException;
import org.netbeans.spi.viewmodel.UnknownTypeException;


/**
*/
public class JPDAThreadImpl implements JPDAThread {
    
    private ThreadReference     threadReference;
    private ThreadsTreeModel    ttm;

    public JPDAThreadImpl (
        ThreadReference     threadReference,
        ThreadsTreeModel    ttm
    ) {
        this.threadReference = threadReference;
        this.ttm = ttm;
    }

    /**
     * Getter for the name of thread property.
     *
     * @return name of thread.
     */
    public String getName () {
        return threadReference.name ();
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
            return (JPDAThreadGroup) ttm.translate (tgr);
        } catch (UnknownTypeException e) {
            e.printStackTrace ();
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
        } catch (InvalidStackFrameException ex) {
            ex.printStackTrace ();
        } catch (IncompatibleThreadStateException ex) {
            ex.printStackTrace ();
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
        } catch (InvalidStackFrameException ex) {
            ex.printStackTrace ();
        } catch (IncompatibleThreadStateException ex) {
            ex.printStackTrace ();
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
        } catch (InvalidStackFrameException ex) {
            ex.printStackTrace ();
        } catch (IncompatibleThreadStateException ex) {
            ex.printStackTrace ();
        } catch (VMDisconnectedException ex) {
        }
        return "";
    }
    
    /**
    * Returns name of file of this frame or null if thread has no frame.
    *
    * @return Returns name of file of this frame.
    */
    public String getSourceName (String stratum) throws NoInformationException {
        try {
            if (threadReference.frameCount () < 1) return "";
            return threadReference.frame (0).location ().sourceName (stratum);
        } catch (InvalidStackFrameException ex) {
            ex.printStackTrace ();
        } catch (IncompatibleThreadStateException ex) {
            ex.printStackTrace ();
        } catch (AbsentInformationException ex) {
            throw new NoInformationException (ex.getMessage ());
        } catch (VMDisconnectedException ex) {
        }
        return "";
    }
    
    /**
    * Returns name of file of this frame or null if thread has no frame.
    *
    * @return Returns name of file of this frame.
    */
    public String getSourcePath (String stratum) throws NoInformationException {
        try {
            if (threadReference.frameCount () < 1) return "";
            return threadReference.frame (0).location ().sourcePath (stratum);
        } catch (InvalidStackFrameException ex) {
            ex.printStackTrace ();
        } catch (IncompatibleThreadStateException ex) {
            ex.printStackTrace ();
        } catch (AbsentInformationException ex) {
            throw new NoInformationException (ex.getMessage ());
        } catch (VMDisconnectedException ex) {
        }
        return "";
    }
    
    /**
     * Returns call stack for this thread.
     *
     * @throws NoInformationException if the thread is running or not able
     *         to return callstack
     * @return call stack
     */
    public CallStackFrame[] getCallStack () throws NoInformationException {
        return getCallStack (0, getStackDepth ());
    }
    
    /**
     * Returns call stack for this thread on the given indexes.
     *
     * @param from a from index
     * @param to a to index
     * @throws NoInformationException if the thread is running or not able
     *         to return callstack
     * @return call stack
     */
    public CallStackFrame[] getCallStack (int from, int to) throws NoInformationException {
        try {
            return (CallStackFrame[]) ttm.getCallStackTreeModel ().getChildren (
                threadReference, from, to
            );
        } catch (UnknownTypeException e) {
            e.printStackTrace ();
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
        } catch (IncompatibleThreadStateException e) {
        }
        return 0;
    }
    
    /**
     * Suspends thread.
     */
    public void suspend () {
        try {
            if (isSuspended ()) return;
            threadReference.suspend ();
        } catch (VMDisconnectedException ex) {
        }
    }
    
    /**
     * Unsuspends thread.
     */
    public void resume () {
        try {
            if (!isSuspended ()) return;
            threadReference.resume ();
        } catch (VMDisconnectedException ex) {
        }
        
    }
    
    /**
     * Sets this thread current.
     *
     * @see JPDADebugger#getCurrentThread
     */
    public void makeCurrent () {
        ttm.getDebugger ().setCurrentThread (this);
    }
    
    /**
     * Returns monitor this thread is waiting on.
     *
     * @return monitor this thread is waiting on
     */
    public ObjectVariable getContendedMonitor () {
        try {
            ObjectReference or = threadReference.currentContendedMonitor ();
            if (or == null) return null;
            LocalsTreeModel ltm = ttm.getLocalsTreeModel ();
            return ltm.getThis (or, "");
        } catch (IncompatibleThreadStateException e) {
        } catch (UnsupportedOperationException e) {
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
            List l = threadReference.ownedMonitors ();
            LocalsTreeModel ltm = ttm.getLocalsTreeModel ();
            int i, k = l.size ();
            ObjectVariable[] vs = new ObjectVariable [k];
            for (i = 0; i < k; i++) {
                vs [i] = ltm.getThis ((ObjectReference) l.get (i), "");
            }
            return vs;
        } catch (IncompatibleThreadStateException e) {
        } catch (UnsupportedOperationException e) {
        }
        return new ObjectVariable [0];
    }
    
    public ThreadReference getThreadReference () {
        return threadReference;
    }
}
