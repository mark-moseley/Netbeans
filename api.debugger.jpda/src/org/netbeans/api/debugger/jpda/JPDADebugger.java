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

package org.netbeans.api.debugger.jpda;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector.Argument;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.ListeningConnector;
import com.sun.jdi.request.EventRequest;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.openide.util.NbBundle;


/**
 * Represents one JPDA debugger session (one 
 * {@link com.sun.jdi.VirtualMachine}). 
 *
 * <br><br>
 * <b>How to obtain it from DebuggerEngine:</b>
 * <pre style="background-color: rgb(255, 255, 153);">
 *    JPDADebugger jpdaDebugger = (JPDADebugger) debuggerEngine.lookup 
 *        (JPDADebugger.class);</pre>
 *
 * @author Jan Jancura
 */
public abstract class JPDADebugger {

    /** Name of property for state of debugger. */
    public static final String          PROP_STATE = "state";
    /** Name of property for current thread. */
    public static final String          PROP_CURRENT_THREAD = "currentThread";
    /** Name of property for current stack frame. */
    public static final String          PROP_CURRENT_CALL_STACK_FRAME = "currentCallStackFrame";
    /** Property name constant. */
    public static final String          PROP_SUSPEND = "suspend"; // NOI18N

    /** Property name constant. */
    public static final String          PROP_THREAD_STARTED = "threadStarted";   // NOI18N
    /** Property name constant. */
    public static final String          PROP_THREAD_DIED = "threadDied";         // NOI18N
    /** Property name constant. */
    public static final String          PROP_THREAD_GROUP_ADDED = "threadGroupAdded";  // NOI18N
    
    /** Suspend property value constant. */
    public static final int             SUSPEND_ALL = EventRequest.SUSPEND_ALL;
    /** Suspend property value constant. */
    public static final int             SUSPEND_EVENT_THREAD = EventRequest.SUSPEND_EVENT_THREAD;
    
    /** Debugger state constant. */
    public static final int             STATE_STARTING = 1;
    /** Debugger state constant. */
    public static final int             STATE_RUNNING = 2;
    /** Debugger state constant. */
    public static final int             STATE_STOPPED = 3;
    /** Debugger state constant. */
    public static final int             STATE_DISCONNECTED = 4;

    /** ID of JPDA Debugger Engine. */
    public static final String          ENGINE_ID = "netbeans-JPDASession/Java";
    /** ID of JPDA Debugger Engine. */
    public static final String          SESSION_ID = "netbeans-JPDASession";
    

    
    /**
     * This utility method helps to start a new JPDA debugger session. 
     * Its implementation use {@link LaunchingDICookie} and 
     * {@link org.netbeans.api.debugger.DebuggerManager#getDebuggerManager}.
     *
     * @param mainClassName a name or main class
     * @param args command line arguments
     * @param classPath a classPath
     * @param suspend if true session will be suspended
     */
    public static void launch (
        String          mainClassName,
        String[]        args,
        String          classPath,
        boolean         suspend
    ) {
        DebuggerEngine[] es = DebuggerManager.getDebuggerManager().startDebugging (
            DebuggerInfo.create (
                LaunchingDICookie.ID,
                new Object[] {
                    LaunchingDICookie.create (
                        mainClassName,
                        args,
                        classPath,
                        suspend
                    )
                }
            )
        );
        if (es.length == 0) {
            /* Can not throw DebuggerStartException, but it should...
            throw new DebuggerStartException(
                    NbBundle.getMessage(JPDADebugger.class, "MSG_NO_DEBUGGER")); */
            throw new RuntimeException(
                    NbBundle.getMessage(JPDADebugger.class, "MSG_NO_DEBUGGER"));
        }
    }
    
    /**
     * This utility method helps to start a new JPDA debugger session. 
     * Its implementation use {@link ListeningDICookie} and 
     * {@link org.netbeans.api.debugger.DebuggerManager#getDebuggerManager}.
     *
     * @param connector The listening connector
     * @param args The arguments
     * @param services The additional services
     */
    public static JPDADebugger listen (
        ListeningConnector        connector,
        Map<String, ? extends Argument>  args,
        Object[]                  services
    ) throws DebuggerStartException {
        Object[] s = new Object [services.length + 1];
        System.arraycopy (services, 0, s, 1, services.length);
        s [0] = ListeningDICookie.create (
            connector,
            args
        );
        DebuggerEngine[] es = DebuggerManager.getDebuggerManager ().
            startDebugging (
                DebuggerInfo.create (
                    ListeningDICookie.ID,
                    s
                )
            );
        int i, k = es.length;
        for (i = 0; i < k; i++) {
            JPDADebugger d = es[i].lookupFirst(null, JPDADebugger.class);
            if (d == null) continue;
            d.waitRunning ();
            return d;
        }
        throw new DebuggerStartException(
                NbBundle.getMessage(JPDADebugger.class, "MSG_NO_DEBUGGER"));
    }
    
    /**
     * This utility method helps to start a new JPDA debugger session. 
     * Its implementation use {@link ListeningDICookie} and 
     * {@link org.netbeans.api.debugger.DebuggerManager#getDebuggerManager}.
     *
     * @param connector The listening connector
     * @param args The arguments
     * @param services The additional services
     */
    public static void startListening (
        ListeningConnector        connector,
        Map<String, ? extends Argument>  args,
        Object[]                  services
    ) throws DebuggerStartException {
        Object[] s = new Object [services.length + 1];
        System.arraycopy (services, 0, s, 1, services.length);
        s [0] = ListeningDICookie.create (
            connector,
            args
        );
        DebuggerEngine[] es = DebuggerManager.getDebuggerManager ().
            startDebugging (
                DebuggerInfo.create (
                    ListeningDICookie.ID,
                    s
                )
            );
        if (es.length == 0) {
            throw new DebuggerStartException(
                    NbBundle.getMessage(JPDADebugger.class, "MSG_NO_DEBUGGER"));
        }
    }
    
    /**
     * This utility method helps to start a new JPDA debugger session. 
     * Its implementation use {@link AttachingDICookie} and 
     * {@link org.netbeans.api.debugger.DebuggerManager#getDebuggerManager}.
     *
     * @param hostName a name of computer to attach to
     * @param portNumber a port number
     */
    public static JPDADebugger attach (
        String          hostName,
        int             portNumber,
        Object[]        services
    ) throws DebuggerStartException {
        Object[] s = new Object [services.length + 1];
        System.arraycopy (services, 0, s, 1, services.length);
        s [0] = AttachingDICookie.create (
            hostName,
            portNumber
        );
        DebuggerEngine[] es = DebuggerManager.getDebuggerManager ().
            startDebugging (
                DebuggerInfo.create (
                    AttachingDICookie.ID,
                    s
                )
            );
        int i, k = es.length;
        for (i = 0; i < k; i++) {
            JPDADebugger d = es[i].lookupFirst(null, JPDADebugger.class);
            if (d == null) continue;
            d.waitRunning ();
            return d;
        }
        throw new DebuggerStartException(
                NbBundle.getMessage(JPDADebugger.class, "MSG_NO_DEBUGGER"));
    }
    
    /**
     * This utility method helps to start a new JPDA debugger session. 
     * Its implementation use {@link AttachingDICookie} and 
     * {@link org.netbeans.api.debugger.DebuggerManager#getDebuggerManager}.
     *
     * @param name a name of shared memory block
     */
    public static JPDADebugger attach (
        String          name,
        Object[]        services
    ) throws DebuggerStartException {
        Object[] s = new Object [services.length + 1];
        System.arraycopy (services, 0, s, 1, services.length);
        s [0] = AttachingDICookie.create (
            name
        );
        DebuggerEngine[] es = DebuggerManager.getDebuggerManager ().
            startDebugging (
                DebuggerInfo.create (
                    AttachingDICookie.ID,
                    s
                )
            );
        int i, k = es.length;
        for (i = 0; i < k; i++) {
            JPDADebugger d = es[i].lookupFirst(null, JPDADebugger.class);
            d.waitRunning ();
            if (d == null) continue;
            return d;
        }
        throw new DebuggerStartException(
                NbBundle.getMessage(JPDADebugger.class, "MSG_NO_DEBUGGER"));
    }

    /**
     * Returns current state of JPDA debugger.
     *
     * @return current state of JPDA debugger
     * @see #STATE_STARTING
     * @see #STATE_RUNNING
     * @see #STATE_STOPPED
     * @see #STATE_DISCONNECTED
     */
    public abstract int getState ();
    
    /**
     * Gets value of suspend property.
     *
     * @return value of suspend property
     */
    public abstract int getSuspend ();

    /**
     * Sets value of suspend property.
     *
     * @param s a new value of suspend property
     */
    public abstract void setSuspend (int s);
    
    /**
     * Returns all threads that exist in the debuggee.
     *
     * @return all threads
     */
    public List<JPDAThread> getAllThreads() {
        return Collections.emptyList();
    }
    
    /**
     * Returns current thread or null.
     *
     * @return current thread or null
     */
    public abstract JPDAThread getCurrentThread ();
    
    /**
     * Returns current stack frame or null.
     *
     * @return current stack frame or null
     */
    public abstract CallStackFrame getCurrentCallStackFrame ();
    
    /**
     * Evaluates given expression in the current context.
     *
     * @param expression a expression to be evaluated
     *  
     * @return current value of given expression
     */
    public abstract Variable evaluate (String expression) 
    throws InvalidExpressionException;

    /**
     * Waits till the Virtual Machine is started and returns 
     * {@link DebuggerStartException} if some problem occurres.
     *
     * @throws DebuggerStartException is some problems occurres during debugger 
     *         start
     *
     * @see AbstractDICookie#getVirtualMachine()
     */
    public abstract void waitRunning () throws DebuggerStartException;

    /**
     * Returns <code>true</code> if this debugger supports fix & continue 
     * (HotSwap).
     *
     * @return <code>true</code> if this debugger supports fix & continue
     */
    public abstract boolean canFixClasses ();

    /**
     * Returns <code>true</code> if this debugger supports Pop action.
     *
     * @return <code>true</code> if this debugger supports Pop action
     */
    public abstract boolean canPopFrames ();
    
    /**
     * Determines if the target debuggee can be modified.
     *
     * @return <code>true</code> if the target debuggee can be modified or when
     *         this information is not available (on JDK 1.4).
     * @since 2.3
     */
    public boolean canBeModified() {
        return true;
    }

    /**
     * Implements fix & continue (HotSwap). Map should contain class names
     * as a keys, and byte[] arrays as a values.
     *
     * @param classes a map from class names to be fixed to byte[] 
     */
    public abstract void fixClasses (Map<String, byte[]> classes);
    
    /** 
     * Returns instance of SmartSteppingFilter.
     *
     * @return instance of SmartSteppingFilter
     */
    public abstract SmartSteppingFilter getSmartSteppingFilter ();

    /**
     * Helper method that fires JPDABreakpointEvent on JPDABreakpoints.
     *
     * @param breakpoint a breakpoint to be changed
     * @param event a event to be fired
     */
    protected void fireBreakpointEvent (
        JPDABreakpoint breakpoint, 
        JPDABreakpointEvent event
    ) {
        breakpoint.fireJPDABreakpointChange (event);
    }
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    public abstract void addPropertyChangeListener (PropertyChangeListener l);

    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    public abstract void removePropertyChangeListener (
        PropertyChangeListener l
    );

    /**
     * Adds property change listener.
     *
     * @param propertyName a name of property to listen on
     * @param l new listener.
     */
    public abstract void addPropertyChangeListener (
        String propertyName, 
        PropertyChangeListener l
    );
    
    /**
     * Removes property change listener.
     *
     * @param propertyName a name of property to listen on
     * @param l removed listener.
     */
    public abstract void removePropertyChangeListener (
        String propertyName, 
        PropertyChangeListener l
    );
    
    /**
     * Creates a new {@link JPDAStep}. 
     * Parameters correspond to {@link JPDAStep} constructor.
     * 
     * @return {@link JPDAStep} 
     * @throws {@link java.lang.UnsupportedOperationException} If not overridden
     */
    public JPDAStep createJPDAStep(int size, int depth) {
        throw new UnsupportedOperationException("This method must be overridden."); 
    } 
    
    /**
     * Test whether the debuggee supports accessing of class instances, instance counts, and referring objects.
     * 
     * @see #getInstanceCounts
     * @see JPDAClassType#getInstanceCount
     * @see JPDAClassType#getInstances
     * @see ObjectVariable#getReferringObjects
     * 
     * @return <code>true</code> when the feature is supported, <code>false</code> otherwise.
     */
    public boolean canGetInstanceInfo() {
        return false;
    }
    
    /**
     * Get the list of all classes in the debuggee.
     * @return The list of all classes.
     */
    public List<JPDAClassType> getAllClasses() {
        return Collections.emptyList();
    }
    
    /**
     * Get the list of all classes mathing the given name in the debuggee.
     * @return The list of classes.
     */
    public List<JPDAClassType> getClassesByName(String name) {
        return Collections.emptyList();
    }
    
    /**
     * Retrieves the number of instances of each class in the list.
     * Use {@link #canGetInstanceInfo} to determine if this operation is supported.
     * @return an array of <code>long</code> containing one instance counts for
     *         each respective element in the <code>classTypes</code> list.
     */
    public long[] getInstanceCounts(List<JPDAClassType> classTypes) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }
    
    /**
     * Creates a deadlock detector.
     * @return deadlock detector with automatic detection of deadlock among suspended threads
     */
    public DeadlockDetector getDeadlockDetector() {
        return new DeadlockDetector() {};
    }

}
