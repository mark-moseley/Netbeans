/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.ui.models;

import com.sun.jdi.AbsentInformationException;
import java.awt.datatransfer.Transferable;
import java.beans.Customizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;

import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author martin
 */
public class DebuggingNodeModel implements ExtendedNodeModel {

    public static final String CURRENT_THREAD =
        "org/netbeans/modules/debugger/resources/threadsView/CurrentThread"; // NOI18N
    public static final String RUNNING_THREAD =
        "org/netbeans/modules/debugger/resources/threadsView/RunningThread"; // NOI18N
    public static final String SUSPENDED_THREAD =
        "org/netbeans/modules/debugger/resources/threadsView/SuspendedThread"; // NOI18N
    public static final String CALL_STACK =
        "org/netbeans/modules/debugger/resources/callStackView/NonCurrentFrame";
    public static final String CURRENT_CALL_STACK =
        "org/netbeans/modules/debugger/resources/callStackView/CurrentFrame";
    
    public static final String THREAD_AT_BRKT_LINE = 
            "org/netbeans/modules/debugger/resources/threadsView/thread_at_line_bpkt_16.png";
    public static final String THREAD_AT_BRKT_NONLINE = 
            "org/netbeans/modules/debugger/resources/threadsView/thread_at_non_line_bpkt_16.png";
    public static final String THREAD_AT_BRKT_CONDITIONAL = 
            "org/netbeans/modules/debugger/resources/threadsView/thread_at_conditional_bpkt_16.png";
    public static final String THREAD_SUSPENDED = 
            "org/netbeans/modules/debugger/resources/threadsView/thread_suspended_16.png";
    public static final String THREAD_RUNNING = 
            "org/netbeans/modules/debugger/resources/threadsView/thread_running_16.png";
    public static final String CALL_STACK2 =
            "org/netbeans/modules/debugger/resources/threadsView/call_stack_16.png";

    private JPDADebugger debugger;
    
    private List<ModelListener> listeners = new ArrayList<ModelListener>();
    
    private Map<JPDAThread, ThreadStateUpdater> threadStateUpdaters = new WeakHashMap<JPDAThread, ThreadStateUpdater>();
    
    public DebuggingNodeModel(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
    }

    public String getDisplayName(Object node) throws UnknownTypeException {
        if (TreeModel.ROOT.equals(node)) {
            return ""; // NOI18N
        }
        if (node instanceof JPDAThread) {
            JPDAThread t = (JPDAThread) node;
            watch(t);
            return getDisplayName(t);
        }
        if (node instanceof CallStackFrame) {
            CallStackFrame f = (CallStackFrame) node;
            return CallStackNodeModel.getCSFName(null, f, false);
        }
        throw new UnknownTypeException(node.toString());
    }
    
    private String getDisplayName(JPDAThread t) throws UnknownTypeException {
        String frame = null;
        if (t.isSuspended () && (t.getStackDepth () > 0)) {
            try { 
                CallStackFrame sf = t.getCallStack (0, 1) [0];
                frame = CallStackNodeModel.getCSFName (null, sf, false);
            } catch (AbsentInformationException e) {
            }
        }
        String name = t.getName();
        JPDABreakpoint breakpoint = t.getCurrentBreakpoint();
        if (breakpoint != null) {
            return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_At_Breakpoint", name, breakpoint.toString());
        }
        if (t.isSuspended()) {
            if (frame != null) {
                return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Suspended_At", name, frame);
            } else {
                return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Suspended", name);
            }
        } else {
            return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Running", name);
        }
        /*
        int i = t.getState ();
        switch (i) {
            case JPDAThread.STATE_UNKNOWN:
                return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Unknown", name);
            case JPDAThread.STATE_MONITOR:
                if (frame != null) {
                    return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Monitor_At", name, frame);
                } else {
                    return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Monitor", name);
                }
            case JPDAThread.STATE_NOT_STARTED:
                return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_NotStarted", name);
            case JPDAThread.STATE_RUNNING:
                if (frame != null) {
                    return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Running_At", name, frame);
                } else {
                    return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Running", name);
                }
            case JPDAThread.STATE_SLEEPING:
                if (frame != null) {
                    return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Sleeping_At", name, frame);
                } else {
                    return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Sleeping", name);
                }
            case JPDAThread.STATE_WAIT:
                if (frame != null) {
                    return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Waiting_At", name, frame);
                } else {
                    return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Waiting", name);
                }
            case JPDAThread.STATE_ZOMBIE:
                return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Zombie", name);
            default:
                Exceptions.printStackTrace(new IllegalStateException("Unexpected thread state: "+i+" of "+t));
                return NbBundle.getMessage(DebuggingNodeModel.class, "CTL_Thread_State_Unknown", name);
        }
         */
    }

    public String getIconBase(Object node) throws UnknownTypeException {
        if (node instanceof CallStackFrame) {
            CallStackFrame ccsf = debugger.getCurrentCallStackFrame ();
            if ( (ccsf != null) && 
                 (ccsf.equals (node)) 
            ) return CURRENT_CALL_STACK;
            return CALL_STACK;
        }
        if (node instanceof JPDAThread) {
            if (node == debugger.getCurrentThread ())
                return CURRENT_THREAD;
            return ((JPDAThread) node).isSuspended () ? 
                SUSPENDED_THREAD : RUNNING_THREAD;
            
        }
        throw new UnknownTypeException (node);
    }

    public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
        if (node instanceof JPDAThread) {
            JPDAThread t = (JPDAThread) node;
            Breakpoint b = t.getCurrentBreakpoint();
            if (b != null) {
                if (b instanceof LineBreakpoint) {
                    String condition = ((LineBreakpoint) b).getCondition();
                    if (condition != null && condition.length() > 0) {
                        return THREAD_AT_BRKT_CONDITIONAL;
                    } else {
                        return THREAD_AT_BRKT_LINE;
                    }
                } else {
                    return THREAD_AT_BRKT_NONLINE;
                }
            }
            if (t.isSuspended()) {
                return THREAD_SUSPENDED;
            } else {
                return THREAD_RUNNING;
            }
        }
        if (node instanceof CallStackFrame) {
            return CALL_STACK2;
        }
        return getIconBase(node)+".gif";
    }

    public String getShortDescription(Object node) throws UnknownTypeException {
        if (node instanceof JPDAThread) {
            JPDAThread t = (JPDAThread) node;
            int i = t.getState ();
            String s = "";
            switch (i) {
                case JPDAThread.STATE_UNKNOWN:
                    s = NbBundle.getBundle (ThreadsNodeModel.class).getString
                        ("CTL_ThreadsModel_State_Unknown");
                    break;
                case JPDAThread.STATE_MONITOR:
                    ObjectVariable ov = t.getContendedMonitor ();
                    if (ov == null)
                        s = NbBundle.getBundle (ThreadsNodeModel.class).
                            getString ("CTL_ThreadsModel_State_Monitor");
                    else
                        try {
                            s = java.text.MessageFormat.
                                format (
                                    NbBundle.getBundle (ThreadsNodeModel.class).
                                        getString (
                                    "CTL_ThreadsModel_State_ConcreteMonitor"), 
                                    new Object [] { ov.getToStringValue () });
                        } catch (InvalidExpressionException ex) {
                            s = ex.getLocalizedMessage ();
                        }
                    break;
                case JPDAThread.STATE_NOT_STARTED:
                    s = NbBundle.getBundle (ThreadsNodeModel.class).getString
                        ("CTL_ThreadsModel_State_NotStarted");
                    break;
                case JPDAThread.STATE_RUNNING:
                    s = NbBundle.getBundle (ThreadsNodeModel.class).getString
                        ("CTL_ThreadsModel_State_Running");
                    break;
                case JPDAThread.STATE_SLEEPING:
                    s = NbBundle.getBundle (ThreadsNodeModel.class).getString
                        ("CTL_ThreadsModel_State_Sleeping");
                    break;
                case JPDAThread.STATE_WAIT:
                    ov = t.getContendedMonitor ();
                    if (ov == null)
                        s = NbBundle.getBundle (ThreadsNodeModel.class).
                            getString ("CTL_ThreadsModel_State_Waiting");
                    else
                        try {
                            s = java.text.MessageFormat.format
                                (NbBundle.getBundle (ThreadsNodeModel.class).
                                getString ("CTL_ThreadsModel_State_WaitingOn"), 
                                new Object [] { ov.getToStringValue () });
                        } catch (InvalidExpressionException ex) {
                            s = ex.getLocalizedMessage ();
                        }
                    break;
                case JPDAThread.STATE_ZOMBIE:
                    s = NbBundle.getBundle (ThreadsNodeModel.class).getString
                        ("CTL_ThreadsModel_State_Zombie");
                    break;
            }
            if (t.isSuspended () && (t.getStackDepth () > 0)) {
                try { 
                    CallStackFrame sf = t.getCallStack (0, 1) [0];
                    s += " " + CallStackNodeModel.getCSFName (null, sf, true);
                } catch (AbsentInformationException e) {
                }
            }
            return s;
        }
        if (node instanceof CallStackFrame) {
            CallStackFrame sf = (CallStackFrame) node;
            return CallStackNodeModel.getCSFName (null, sf, true);
        }
        throw new UnknownTypeException(node.toString());
    }

    public void addModelListener(ModelListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public void removeModelListener(ModelListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    public boolean canCopy(Object node) throws UnknownTypeException {
        return false;
    }

    public boolean canCut(Object node) throws UnknownTypeException {
        return false;
    }

    public boolean canRename(Object node) throws UnknownTypeException {
        return false;
    }

    public void setName(Object node, String name) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Transferable clipboardCopy(Object node) throws IOException, UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Transferable clipboardCut(Object node) throws IOException, UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
        return null;
    }

    private void fireNodeChanged (Object node) {
        List<ModelListener> ls;
        synchronized (listeners) {
            ls = new ArrayList<ModelListener>(listeners);
        }
        ModelEvent event = new ModelEvent.NodeChanged(this, node,
                ModelEvent.NodeChanged.DISPLAY_NAME_MASK |
                ModelEvent.NodeChanged.ICON_MASK |
                ModelEvent.NodeChanged.SHORT_DESCRIPTION_MASK |
                ModelEvent.NodeChanged.CHILDREN_MASK);
        for (ModelListener ml : ls) {
            ml.modelChanged (event);
        }
    }
    
    private void watch(JPDAThread t) {
        synchronized (threadStateUpdaters) {
            if (!threadStateUpdaters.containsKey(t)) {
                threadStateUpdaters.put(t, new ThreadStateUpdater(t));
            }
        }
    }
    
    private class ThreadStateUpdater implements PropertyChangeListener {
        
        private Reference<JPDAThread> tr;
        // currently waiting / running refresh task
        // there is at most one
        private RequestProcessor.Task task;
        
        public ThreadStateUpdater(JPDAThread t) {
            this.tr = new WeakReference(t);
            ((Customizer) t).addPropertyChangeListener(WeakListeners.propertyChange(this, t));
        }

        public void propertyChange(PropertyChangeEvent evt) {
            JPDAThread t = tr.get();
            if (t != null) {
                synchronized (this) {
                    if (task == null) {
                        task = RequestProcessor.getDefault().create(new Refresher());
                    }
                    task.schedule(100);
                }
            }
        }
        
        private class Refresher extends Object implements Runnable {
            public void run() {
                JPDAThread t = tr.get();
                if (t != null) {
                    fireNodeChanged(t);
                }
            }
        }
    }
    
}
