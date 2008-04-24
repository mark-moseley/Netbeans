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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.debugger.jpda.ui.models;

import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.VMDisconnectedException;
import java.awt.Color;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import java.util.Set;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.Action;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;

import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.MonitorInfo;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModelFilter;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.NodeActionsProvider;
import org.netbeans.spi.viewmodel.NodeActionsProviderFilter;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.TreeModelFilter;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;
import org.openide.util.datatransfer.PasteType;


/**
 * @author   Jan Jancura
 */
public class DebuggingMonitorModel implements TreeModelFilter, ExtendedNodeModelFilter, 
NodeActionsProviderFilter, TableModel, Constants {
    
    public static final String SHOW_MONITORS = "show.monitors"; // NOI18N

    public static final String CONTENDED_MONITOR =
        "org/netbeans/modules/debugger/resources/allInOneView/waiting_on_monitor_16.png"; // NOI18N
    public static final String OWNED_MONITORS =
        "org/netbeans/modules/debugger/resources/allInOneView/monitor_acquired_16.png"; // NOI18N
    public static final String MONITOR =
        "org/netbeans/modules/debugger/resources/allInOneView/monitor_acquired_16.png"; // NOI18N

    private RequestProcessor evaluationRP = new RequestProcessor();
    private final Collection modelListeners = new HashSet();
    private Preferences preferences = NbPreferences.forModule(getClass()).node("debugging"); // NOI18N
    private PreferenceChangeListener prefListener;
    private Set<JPDAThread> threadsAskedForMonitors = new WeakSet<JPDAThread>();
    private Set<CallStackFrame> framesAskedForMonitors = new WeakSet<CallStackFrame>();
    private JPDADebugger debugger;
    
    public DebuggingMonitorModel(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
        prefListener = new MonitorPreferenceChangeListener();
        preferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, prefListener, preferences));
    }
    
    // TreeView impl............................................................
    
    public Object getRoot (TreeModel model) {
        return model.getRoot ();
    }
    
    public Object[] getChildren (
        TreeModel   model, 
        Object      o, 
        int         from, 
        int         to
    ) throws UnknownTypeException {
        if (o instanceof JPDAThread) {
            JPDAThread t = (JPDAThread) o;
            synchronized (threadsAskedForMonitors) {
                threadsAskedForMonitors.add(t);
            }
            if (preferences.getBoolean(SHOW_MONITORS, false)) {
                try {
                    ObjectVariable contended = t.getContendedMonitor ();
                    ObjectVariable[] owned;
                    boolean noFrameMonitors = false;
                    List<MonitorInfo> mf = t.getOwnedMonitorsAndFrames();
                    if (mf.size() > 0) {
                        List<ObjectVariable> mlf = new ArrayList<ObjectVariable>();
                        for (MonitorInfo m: mf) {
                            if (m.getFrame() == null) {
                                mlf.add(m.getMonitor());
                            }
                        }
                        owned = mlf.toArray(new ObjectVariable[0]);
                        noFrameMonitors = true;
                    } else {
                        owned = t.getOwnedMonitors ();
                    }
                    ContendedMonitor cm = null;
                    OwnedMonitors om = null;
                    if ( (contended != null) &&
                         (from  == 0) && (to > 0)
                    ) cm = new ContendedMonitor (contended);
                    if ( (owned.length > 0) &&
                         ( ((contended != null) && (from < 2) && (to > 1)) ||
                           ((contended == null) && (from == 0) && (to > 0))
                         )
                    ) om = new OwnedMonitors (owned);
                    if (om != null) om.noFrame = noFrameMonitors;
                    int i = 0;
                    if (cm != null) i++;
                    if (om != null) i++;
                    Object[] os = new Object [i];
                    i = 0;
                    if (cm != null) os[i++] = cm;
                    if (om != null) os[i++] = om;
                    Object[] ch = model.getChildren(o, from, to);
                    if (i > 0) {
                        Object[] newCh = new Object[i + ch.length];
                        System.arraycopy(os, 0, newCh, 0, os.length);
                        System.arraycopy(ch, 0, newCh, i, ch.length);
                        ch = newCh;
                    }
                    return ch;
                } catch (ObjectCollectedException e) {
                } catch (VMDisconnectedException e) {
                }
            }
            return model.getChildren(o, from, to);
        }
        /*if (o instanceof JPDAThreadGroup) {
            JPDAThreadGroup tg = (JPDAThreadGroup) o;
            Object[] ch = model.getChildren (o, from, to);
            int i, k = ch.length;
            for (i = 0; i < k; i++) {
                if (!(ch [i] instanceof JPDAThread)) continue;
                try {
                    JPDAThread t = (JPDAThread) ch [i];
                    if (t.getContendedMonitor () == null &&
                        t.getOwnedMonitors ().length == 0
                    ) continue;
                    ThreadWithBordel twb = new ThreadWithBordel ();
                    twb.originalThread = t;
                    ch [i] = twb;
                } catch (ObjectCollectedException e) {
                } catch (VMDisconnectedException e) {
                }
            }
            return ch;
        }*/
        if (o instanceof OwnedMonitors) {
            OwnedMonitors om = (OwnedMonitors) o;
            Object[] fo = new Object [to - from];
            System.arraycopy (om.variables, from, fo, 0, to - from);
            return fo;
        }
        if (o instanceof CallStackFrame) {
            if (preferences.getBoolean(SHOW_MONITORS, false)) {
                CallStackFrame frame = (CallStackFrame) o;
                List<MonitorInfo> monitors = frame.getOwnedMonitors();
                int n = monitors.size();
                if (n > 0) {
                    synchronized (framesAskedForMonitors) {
                        framesAskedForMonitors.add(frame);
                    }
                    ObjectVariable[] ch = new ObjectVariable[n];
                    for (int i = 0; i < n; i++) {
                        ch[i] = monitors.get(i).getMonitor();
                    }
                    return ch;
                }
            } else {
                synchronized (framesAskedForMonitors) {
                    framesAskedForMonitors.add((CallStackFrame) o);
                }
            }
        }
        return model.getChildren (o, from, to);
    }
    
    public int getChildrenCount (
        TreeModel   model, 
        Object      o
    ) throws UnknownTypeException {
        /*if (o instanceof ThreadWithBordel) {
            // Performance, see issue #59058.
            return Integer.MAX_VALUE;
            /*
            try {
                JPDAThread t = ((ThreadWithBordel) o).originalThread;
                ObjectVariable contended = t.getContendedMonitor ();
                ObjectVariable[] owned = t.getOwnedMonitors ();
                int i = 0;
                if (contended != null) i++;
                if (owned.length > 0) i++;
                return i;
            } catch (ObjectCollectedException e) {
            } catch (VMDisconnectedException e) {
            }
            return 0;
             *//*
        }
        if (o instanceof ThreadWithBordel) {
            return model.getChildrenCount (
                ((ThreadWithBordel) o).originalThread
            );
        }*/
        if (o instanceof OwnedMonitors) {
            return ((OwnedMonitors) o).variables.length;
        }
        return model.getChildrenCount (o);
    }
    
    public boolean isLeaf (TreeModel model, Object o) 
    throws UnknownTypeException {
        /*if (o instanceof ThreadWithBordel) {
            return false;
        }*/
        if (o instanceof OwnedMonitors)
            return false;
        if (o instanceof ContendedMonitor)
            return true;
        if (o instanceof ObjectVariable)
            return true;
        if (o instanceof CallStackFrame) {
            if (preferences.getBoolean(SHOW_MONITORS, false)) {
                return false;
            } else {
                synchronized (framesAskedForMonitors) {
                    framesAskedForMonitors.add((CallStackFrame) o);
                }
            }
        }
        return model.isLeaf (o);
    }
    
    
    // NodeModel impl...........................................................
    
    public String getDisplayName (NodeModel model, Object o) throws 
    UnknownTypeException {
        if (o instanceof ContendedMonitor) {
            ObjectVariable v = ((ContendedMonitor) o).variable;
            String monitorText = java.text.MessageFormat.format(NbBundle.getBundle(DebuggingMonitorModel.class).getString(
                    "CTL_MonitorModel_Column_ContendedMonitor"), new Object [] { v.getType(), v.getValue() });
            Set nodesInDeadlock = DebuggingNodeModel.getNodesInDeadlock(debugger);
            if (nodesInDeadlock != null) {
                synchronized (nodesInDeadlock) {
                    if (nodesInDeadlock.contains(v)) {
                        monitorText = BoldVariablesTableModelFilterFirst.toHTML(
                                monitorText,
                                false, false, Color.RED);
                    }
                }
            }
            return monitorText;
        } else
        if (o instanceof OwnedMonitors) {
            return NbBundle.getBundle(DebuggingMonitorModel.class).getString("CTL_MonitorModel_Column_OwnedMonitors");
        } else
        if (o instanceof ObjectVariable) {
            ObjectVariable v = (ObjectVariable) o;
            String monitorText = java.text.MessageFormat.format(NbBundle.getBundle(DebuggingMonitorModel.class).getString(
                    "CTL_MonitorModel_Column_Monitor"), new Object [] { v.getType(), v.getValue() });
            Set nodesInDeadlock = DebuggingNodeModel.getNodesInDeadlock(debugger);
            if (nodesInDeadlock != null) {
                synchronized (nodesInDeadlock) {
                    if (nodesInDeadlock.contains(o)) {
                        monitorText = BoldVariablesTableModelFilterFirst.toHTML(
                                monitorText,
                                false, false, Color.RED);
                    }
                }
            }
            return monitorText;
        } else
        return model.getDisplayName (o);
    }
    
    private Map shortDescriptionMap = new HashMap();
    
    public String getShortDescription (final NodeModel model, final Object o) throws 
    UnknownTypeException {
        
        synchronized (shortDescriptionMap) {
            Object shortDescription = shortDescriptionMap.remove(o);
            if (shortDescription instanceof String) {
                return (String) shortDescription;
            } else if (shortDescription instanceof UnknownTypeException) {
                throw (UnknownTypeException) shortDescription;
            }
        }
        
        // Called from AWT - we need to postpone the work...
        evaluationRP.post(new Runnable() {
            public void run() {
                Object shortDescription;
                if (o instanceof ContendedMonitor) {
                    ObjectVariable v = ((ContendedMonitor) o).variable;
                    try {
                        shortDescription = "(" + v.getType () + ") " + v.getToStringValue ();
                    } catch (InvalidExpressionException ex) {
                        shortDescription = ex.getLocalizedMessage ();
                    }
                } else
                if (o instanceof OwnedMonitors) {
                    shortDescription = "";
                } else
                if (o instanceof ObjectVariable) {
                    ObjectVariable v = (ObjectVariable) o;
                    try {
                        shortDescription = "(" + v.getType () + ") " + v.getToStringValue ();
                    } catch (InvalidExpressionException ex) {
                        shortDescription = ex.getLocalizedMessage ();
                    }
                } else {
                    try {
                        shortDescription = model.getShortDescription (o);
                    } catch (UnknownTypeException utex) {
                        shortDescription = utex;
                    }
                }
                
                if (shortDescription != null && !"".equals(shortDescription)) {
                    synchronized (shortDescriptionMap) {
                        shortDescriptionMap.put(o, shortDescription);
                    }
                    fireModelChange(new ModelEvent.NodeChanged(DebuggingMonitorModel.this,
                        o, ModelEvent.NodeChanged.SHORT_DESCRIPTION_MASK));
                }
            }
        });
        
        return ""; // NOI18N
    }
    
    public String getIconBase (NodeModel model, Object o) throws 
    UnknownTypeException {
        return model.getIconBase (o);
    }

    public void addModelListener (ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.add(l);
        }
    }

    public void removeModelListener (ModelListener l) {
        synchronized (modelListeners) {
            modelListeners.remove(l);
        }
    }
    
    private void fireModelChange(ModelEvent me) {
        Object[] listeners;
        synchronized (modelListeners) {
            listeners = modelListeners.toArray();
        }
        for (int i = 0; i < listeners.length; i++) {
            ((ModelListener) listeners[i]).modelChanged(me);
        }
    }
    
    
    // NodeActionsProvider impl.................................................
    
    public Action[] getActions (NodeActionsProvider model, Object o) throws 
    UnknownTypeException {
        if (o instanceof ContendedMonitor) {
            return new Action [0];
        } else
        if (o instanceof OwnedMonitors) {
            return new Action [0];
        } else
        if (o instanceof ObjectVariable) {
            return new Action [0];
        } else
        return model.getActions (o);
    }
    
    public void performDefaultAction (NodeActionsProvider model, Object o) 
    throws UnknownTypeException {
        if (o instanceof ContendedMonitor) {
            return;
        } else
        if (o instanceof OwnedMonitors) {
            return;
        } else
        if (o instanceof ObjectVariable) {
            return;
        } else
        model.performDefaultAction (o);
    }
    
    
    // TableModel ..............................................................
    
    public Object getValueAt (Object node, String columnID) throws 
    UnknownTypeException {
        if (node instanceof OwnedMonitors ||
            node instanceof ContendedMonitor ||
            node instanceof ObjectVariable) {
            
            if (columnID == THREAD_STATE_COLUMN_ID)
                return "";
            if (columnID == THREAD_SUSPENDED_COLUMN_ID)
                return null;
        }
        throw new UnknownTypeException (node);
    }
    
    public boolean isReadOnly (Object node, String columnID) throws 
    UnknownTypeException {
        if (node instanceof OwnedMonitors ||
            node instanceof ContendedMonitor ||
            node instanceof ObjectVariable) {
            
            if (columnID == THREAD_STATE_COLUMN_ID || 
                columnID == THREAD_SUSPENDED_COLUMN_ID) {
                
                return true;
            }
        }
        throw new UnknownTypeException (node);
    }
    
    public void setValueAt (Object node, String columnID, Object value) 
    throws UnknownTypeException {
    }
    
    
    // innerclasses ............................................................
    
    static class OwnedMonitors {
        ObjectVariable[] variables;
        boolean noFrame; // If true, these are monitors which are not part of a concrete frame.
        
        OwnedMonitors (ObjectVariable[] vs) {
            variables = vs;
        }
    }
    
    private static class ContendedMonitor {
        ObjectVariable variable;
        
        ContendedMonitor (ObjectVariable v) {
            variable = v;
        }
    }

    public boolean canRename(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return false;
    }

    public boolean canCopy(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return false;
    }

    public boolean canCut(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return false;
    }

    public Transferable clipboardCopy(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        return null;
    }

    public Transferable clipboardCut(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        return null;
    }

    public PasteType[] getPasteTypes(ExtendedNodeModel original, Object node, Transferable t) throws UnknownTypeException {
        return null;
    }

    public void setName(ExtendedNodeModel original, Object node, String name) throws UnknownTypeException {
    }

    public String getIconBaseWithExtension(ExtendedNodeModel model, Object node) throws UnknownTypeException {
        if (node instanceof ContendedMonitor) {
            return CONTENDED_MONITOR;
        } else
        if (node instanceof OwnedMonitors) {
            return OWNED_MONITORS;
        } else
        if (node instanceof ObjectVariable) {
            return MONITOR;
        } else
        return model.getIconBaseWithExtension(node);
    }
    
    
    private class MonitorPreferenceChangeListener implements PreferenceChangeListener {

        public void preferenceChange(PreferenceChangeEvent evt) {
            String key = evt.getKey();
            if (SHOW_MONITORS.equals(key)) {
                List<JPDAThread> threads;
                synchronized (threadsAskedForMonitors) {
                    threads = new ArrayList(threadsAskedForMonitors);
                }
                for (JPDAThread t : threads) {
                    fireModelChange(new ModelEvent.NodeChanged(DebuggingMonitorModel.this,
                                    t, ModelEvent.NodeChanged.CHILDREN_MASK));
                }
                List<CallStackFrame> frames;
                synchronized (framesAskedForMonitors) {
                    frames = new ArrayList(framesAskedForMonitors);
                }
                for (CallStackFrame frame : frames) {
                    fireModelChange(new ModelEvent.NodeChanged(DebuggingMonitorModel.this,
                                    frame, ModelEvent.NodeChanged.CHILDREN_MASK));
                }
            }
        }
        
    }
    
}
