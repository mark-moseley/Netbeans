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

package org.openide.windows;

import java.awt.Frame;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;

/**
 * Trivial window manager that just keeps track of "workspaces" and "modes"
 * according to contract but does not really use them, and just opens all
 * top components in their own frames.
 * Useful in case core-windows.jar is not installed, e.g. in standalone usage.
 * @author Jesse Glick
 * @see "#29933"
 * 
 */
@SuppressWarnings("deprecation")
final class DummyWindowManager extends WindowManager {

    private static final boolean VISIBLE = Boolean.parseBoolean(System.getProperty("org.openide.windows.DummyWindowManager.VISIBLE", "true"));
    private static final long serialVersionUID = 1L;
    private static Action[] DEFAULT_ACTIONS_CLONEABLE;
    private static Action[] DEFAULT_ACTIONS_NOT_CLONEABLE;
    private final Map<String,Workspace> workspaces;
    private transient Frame mw;
    private transient PropertyChangeSupport pcs;
    private transient R r;

    public DummyWindowManager() {
        workspaces = new TreeMap<String,Workspace>();
        createWorkspace("default", null).createMode(/*CloneableEditorSupport.EDITOR_MODE*/"editor", "editor", null); // NOI18N
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        if (pcs == null) {
            pcs = new PropertyChangeSupport(this);
        }

        pcs.addPropertyChangeListener(l);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        if (pcs != null) {
            pcs.removePropertyChangeListener(l);
        }
    }

    protected TopComponent.Registry componentRegistry() {
        TopComponent.Registry reg = super.componentRegistry();

        if (reg != null) {
            return reg;
        } else {
            return registry();
        }
    }

    synchronized R registry() {
        if (r == null) {
            r = new R();
        }

        return r;
    }

    protected WindowManager.Component createTopComponentManager(TopComponent c) {
        return null; // Not used anymore.
    }

    public synchronized Workspace createWorkspace(String name, String displayName) {
        Workspace w = new W(name);
        workspaces.put(name, w);

        if (pcs != null) {
            pcs.firePropertyChange(PROP_WORKSPACES, null, null);
            pcs.firePropertyChange(PROP_CURRENT_WORKSPACE, null, null);
        }

        return w;
    }

    synchronized void delete(Workspace w) {
        workspaces.remove(w.getName());

        if (workspaces.isEmpty()) {
            createWorkspace("default", null); // NOI18N
        }

        if (pcs != null) {
            pcs.firePropertyChange(PROP_WORKSPACES, null, null);
            pcs.firePropertyChange(PROP_CURRENT_WORKSPACE, null, null);
        }
    }

    public synchronized Workspace findWorkspace(String name) {
        return workspaces.get(name);
    }

    public synchronized Workspace getCurrentWorkspace() {
        return workspaces.values().iterator().next();
    }

    public synchronized Workspace[] getWorkspaces() {
        return workspaces.values().toArray(new Workspace[0]);
    }

    public synchronized void setWorkspaces(Workspace[] ws) {
        if (ws.length == 0) {
            throw new IllegalArgumentException();
        }

        workspaces.clear();

        for (int i = 0; i < ws.length; i++) {
            workspaces.put(ws[i].getName(), ws[i]);
        }

        if (pcs != null) {
            pcs.firePropertyChange(PROP_WORKSPACES, null, null);
            pcs.firePropertyChange(PROP_CURRENT_WORKSPACE, null, null);
        }
    }

    public synchronized Frame getMainWindow() {
        if (mw == null) {
            mw = new JFrame("dummy"); // NOI18N
        }

        return mw;
    }

    public void updateUI() {
    }

    // Modes
    public Set<Mode> getModes() {
        Set<Mode> s = new HashSet<Mode>();

        for (Iterator<Workspace> it = new HashSet<Workspace>(workspaces.values()).iterator(); it.hasNext();) {
            Workspace w = it.next();
            s.addAll(w.getModes());
        }

        return s;
    }

    public Mode findMode(TopComponent tc) {
        for (Iterator<Mode> it = getModes().iterator(); it.hasNext();) {
            Mode m = it.next();

            if (Arrays.asList(m.getTopComponents()).contains(tc)) {
                return m;
            }
        }

        return null;
    }

    public Mode findMode(String name) {
        if (name == null) {
            return null;
        }

        for (Iterator<Mode> it = getModes().iterator(); it.hasNext();) {
            Mode m = it.next();

            if (name.equals(m.getName())) {
                return m;
            }
        }

        return null;
    }

    // PENDING Groups not supported.
    public TopComponentGroup findTopComponentGroup(String name) {
        return null;
    }

    //Not supported. Need to access PersistenceManager.
    public TopComponent findTopComponent(String tcID) {
        return null;
    }

    protected String topComponentID(TopComponent tc, String preferredID) {
        return preferredID;
    }

    protected Action[] topComponentDefaultActions(TopComponent tc) {
        // XXX It could be better to provide non-SystemAction instances.
        synchronized (DummyWindowManager.class) {
            //Bugfix #33557: Do not provide CloneViewAction when
            //TopComponent does not implement TopComponent.Cloneable
            if (tc instanceof TopComponent.Cloneable) {
                if (DEFAULT_ACTIONS_CLONEABLE == null) {
                    DEFAULT_ACTIONS_CLONEABLE = loadActions(
                            new String[] { "Save", // NOI18N
                            "CloneView", // NOI18N
                            null, "CloseView" // NOI18N
                        }
                        );
                }

                return DEFAULT_ACTIONS_CLONEABLE;
            } else {
                if (DEFAULT_ACTIONS_NOT_CLONEABLE == null) {
                    DEFAULT_ACTIONS_NOT_CLONEABLE = loadActions(
                            new String[] { "Save", // NOI18N
                            null, "CloseView" // NOI18N
                        }
                        );
                }

                return DEFAULT_ACTIONS_NOT_CLONEABLE;
            }
        }
    }

    private static Action[] loadActions(String[] names) {
        ArrayList<Action> arr = new ArrayList<Action>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();

        for (int i = 0; i < names.length; i++) {
            if (names[i] == null) {
                arr.add(null);

                continue;
            }

            try {
                Class<? extends SystemAction> sa = Class.forName("org.openide.actions." + names[i] + "Action", true, loader).asSubclass(SystemAction.class);
                arr.add(SystemAction.get(sa)); // NOI18N
            } catch (ClassNotFoundException e) {
                // ignore it, missing org-openide-actions.jar
            }
        }

        return arr.toArray(new Action[0]);
    }

    protected boolean topComponentIsOpened(TopComponent tc) {
        return tc.isShowing();
    }

    protected void topComponentActivatedNodesChanged(TopComponent tc, Node[] nodes) {
        registry().setActivatedNodes(tc, nodes);
    }

    protected void topComponentIconChanged(TopComponent tc, Image icon) {
        JFrame f = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, tc);

        if (f != null) {
            f.setIconImage(icon);
        }
    }

    protected void topComponentToolTipChanged(TopComponent tc, String tooltip) {
        // No op.
    }

    protected void topComponentDisplayNameChanged(TopComponent tc, String displayName) {
        JFrame f = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, tc);

        if (f != null) {
            f.setTitle(displayName);
        }
    }
    
    protected void topComponentHtmlDisplayNameChanged(TopComponent tc, String htmlDisplayName) {
        // no operarion, html looks ugly in frame titles
    }

    protected void topComponentOpen(TopComponent tc) {
        JFrame f = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, tc);

        if (f == null) {
            f = new JFrame(tc.getName());

            Image icon = tc.getIcon();

            if (icon != null) {
                f.setIconImage(icon);
            }

            f.getContentPane().add(tc);
            f.pack();

            final java.lang.ref.WeakReference<TopComponent> ref = new java.lang.ref.WeakReference<TopComponent>(tc);
            f.addWindowListener(
                new WindowAdapter() {
                    public void windowClosing(WindowEvent ev) {
                        TopComponent tc = ref.get();

                        if (tc == null) {
                            return;
                        }

                        tc.close();
                    }

                    public void windowActivated(WindowEvent e) {
                        TopComponent tc = ref.get();

                        if (tc == null) {
                            return;
                        }

                        tc.requestActive();
                    }
                }
            );
        }

        if (!tc.isShowing()) {
            componentOpenNotify(tc);
            componentShowing(tc);
            if (VISIBLE) {
                f.setVisible(true);
            }
            registry().open(tc);
        }
    }

    protected void topComponentClose(TopComponent tc) {
        componentHidden(tc);
        componentCloseNotify(tc);

        JFrame f = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, tc);

        if (f != null) {
            if (VISIBLE) {
                f.setVisible(false);
            }
            tc.getParent().remove(tc);
        }

        registry().close(tc);

        java.util.Iterator it = workspaces.values().iterator();

        while (it.hasNext()) {
            W w = (W) it.next();
            w.close(tc);
        }
    }

    protected void topComponentRequestVisible(TopComponent tc) {
        JFrame f = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, tc);

        if (f != null) {
            if (VISIBLE) {
                f.setVisible(true);
            }
        }
    }

    protected void topComponentRequestActive(TopComponent tc) {
        JFrame f = (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, tc);

        if (f != null) {
            f.toFront();
        }

        registry().setActive(tc);
        activateComponent(tc);
    }

    protected void topComponentRequestAttention(TopComponent tc) {
        //TODO what to do here?
    }

    protected void topComponentCancelRequestAttention(TopComponent tc) {
        //TODO what to do here?
    }

    private final class W implements Workspace {
        private static final long serialVersionUID = 1L;
        private final String name;
        private final Map<String,Mode> modes = new HashMap<String,Mode>();
        private final Map<TopComponent,Mode> modesByComponent = new WeakHashMap<TopComponent,Mode>();
        private transient PropertyChangeSupport pcs;

        public W(String name) {
            this.name = name;
        }

        public void activate() {
        }

        public synchronized void addPropertyChangeListener(PropertyChangeListener list) {
            if (pcs == null) {
                pcs = new PropertyChangeSupport(this);
            }

            pcs.addPropertyChangeListener(list);
        }

        public synchronized void removePropertyChangeListener(PropertyChangeListener list) {
            if (pcs != null) {
                pcs.removePropertyChangeListener(list);
            }
        }

        public void remove() {
            DummyWindowManager.this.delete(this);
        }

        public synchronized Mode createMode(String name, String displayName, URL icon) {
            Mode m = new M(name);
            modes.put(name, m);

            if (pcs != null) {
                pcs.firePropertyChange(PROP_MODES, null, null);
            }

            return m;
        }

        public synchronized Set<Mode> getModes() {
            return new HashSet<Mode>(modes.values());
        }

        public synchronized Mode findMode(String name) {
            return modes.get(name);
        }

        public synchronized Mode findMode(TopComponent c) {
            return modesByComponent.get(c);
        }

        synchronized void dock(Mode m, TopComponent c) {
            modesByComponent.put(c, m);
        }

        public Rectangle getBounds() {
            return Utilities.getUsableScreenBounds();
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return getName();
        }

        public void close(TopComponent tc) {
            java.util.Iterator it = modes.values().iterator();

            while (it.hasNext()) {
                M m = (M) it.next();
                m.close(tc);
            }
        }

        private final class M implements Mode {
            private static final long serialVersionUID = 1L;
            private final String name;
            private final Set<TopComponent> components = new HashSet<TopComponent>();

            public M(String name) {
                this.name = name;
            }

            public void close(TopComponent tc) {
                components.remove(tc);
            }

            /* Not needed:
            private transient PropertyChangeSupport pcs;
            public synchronized void addPropertyChangeListener(PropertyChangeListener list) {
                if (pcs == null) {
                    pcs = new PropertyChangeSupport(this);
                }
                pcs.addPropertyChangeListener(list);
            }
            public synchronized void removePropertyChangeListener(PropertyChangeListener list) {
                if (pcs != null) {
                    pcs.removePropertyChangeListener(list);
                }
            }
             */
            public void addPropertyChangeListener(PropertyChangeListener l) {
            }

            public void removePropertyChangeListener(PropertyChangeListener l) {
            }

            public boolean canDock(TopComponent tc) {
                return true;
            }

            public synchronized boolean dockInto(TopComponent c) {
                if (components.add(c)) {
                    Mode old = findMode(c);

                    if ((old != null) && (old != this) && old instanceof M) {
                        synchronized (old) {
                            ((M) old).components.remove(c);
                        }
                    }

                    dock(this, c);
                }

                return true;
            }

            public String getName() {
                return name;
            }

            public String getDisplayName() {
                return getName();
            }

            public Image getIcon() {
                return null;
            }

            public synchronized TopComponent[] getTopComponents() {
                return components.toArray(new TopComponent[0]);
            }

            public Workspace getWorkspace() {
                return W.this;
            }

            public synchronized Rectangle getBounds() {
                return W.this.getBounds();
            }

            public void setBounds(Rectangle s) {
            }

            public TopComponent getSelectedTopComponent() {
                TopComponent[] tcs = components.toArray(new TopComponent[0]);

                return (tcs.length > 0) ? tcs[0] : null;
            }
        }
    }

    private static final class R implements TopComponent.Registry {
        private TopComponent active;
        private final Set<TopComponent> opened;
        private Node[] nodes;
        private PropertyChangeSupport pcs;

        public R() {
            opened = new HashSet<TopComponent>();
            nodes = new Node[0];
        }

        public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
            if (pcs == null) {
                pcs = new PropertyChangeSupport(this);
            }

            pcs.addPropertyChangeListener(l);
        }

        public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
            if (pcs != null) {
                pcs.removePropertyChangeListener(l);
            }
        }

        synchronized void open(TopComponent tc) {
            opened.add(tc);

            if (pcs != null) {
                pcs.firePropertyChange(PROP_TC_OPENED, null, tc);
                pcs.firePropertyChange(PROP_OPENED, null, null);
            }
        }

        synchronized void close(TopComponent tc) {
            opened.remove(tc);

            if (pcs != null) {
                pcs.firePropertyChange(PROP_TC_CLOSED, null, tc);
                pcs.firePropertyChange(PROP_OPENED, null, null);
            }

            if (active == tc) {
                setActive(null);
            }
        }

        public synchronized Set<TopComponent> getOpened() {
            return new HashSet<TopComponent>(opened);
        }

        synchronized void setActive(TopComponent tc) {
            active = tc;

            Node[] _nodes = (tc == null) ? new Node[0] : tc.getActivatedNodes();

            if (_nodes != null) {
                nodes = _nodes;

                if (pcs != null) {
                    pcs.firePropertyChange(PROP_ACTIVATED_NODES, null, null);
                }
            }

            if (pcs != null) {
                pcs.firePropertyChange(PROP_ACTIVATED, null, null);
                pcs.firePropertyChange(PROP_CURRENT_NODES, null, null);
            }
        }

        synchronized void setActivatedNodes(TopComponent tc, Node[] _nodes) {
            if (tc == active) {
                if (_nodes != null) {
                    nodes = _nodes;

                    if (pcs != null) {
                        pcs.firePropertyChange(PROP_ACTIVATED_NODES, null, null);
                    }
                }

                if (pcs != null) {
                    pcs.firePropertyChange(PROP_CURRENT_NODES, null, null);
                }
            }
        }

        public TopComponent getActivated() {
            return active;
        }

        public Node[] getActivatedNodes() {
            return nodes;
        }

        public synchronized Node[] getCurrentNodes() {
            if (active != null) {
                return active.getActivatedNodes();
            } else {
                return null;
            }
        }
    }
}
