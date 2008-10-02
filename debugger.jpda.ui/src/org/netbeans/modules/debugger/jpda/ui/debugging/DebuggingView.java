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

package org.netbeans.modules.debugger.jpda.ui.debugging;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.jpda.DeadlockDetector.Deadlock;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.JPDAThreadGroup;
import org.netbeans.modules.debugger.jpda.ui.views.ViewModelListener;

import org.netbeans.spi.viewmodel.Models;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.Visualizer;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author  Dan
 */
public class DebuggingView extends TopComponent implements org.openide.util.HelpCtx.Provider,
       ExplorerManager.Provider, PropertyChangeListener, TreeExpansionListener, TreeModelListener,
       AdjustmentListener, ChangeListener, MouseWheelListener, TreeSelectionListener {

    /** unique ID of <code>TopComponent</code> (singleton) */
    private static final String ID = "debugging"; //NOI18N
    public static final int BAR_WIDTH = 8;
    
    static final Color hitsColor = new Color(255, 255, 178);
    static final Color hitsBarColor = new Color(230, 230, 130);
    static final Color deadlockColor = UIManager.getDefaults().getColor("nb.errorForeground"); // new Color(252, 157, 159); 
    static final Color greenBarColor = new Color(189, 230, 170);
    private transient Color treeBackgroundColor = UIManager.getDefaults().getColor("Tree.background"); // NOI18N
    
    private transient RequestProcessor requestProcessor = new RequestProcessor("DebuggingView Refresh Scheduler", 1);
    private transient boolean refreshScheduled = false;
    private transient ExplorerManager manager = new ExplorerManager();
    private transient ViewModelListener viewModelListener;
    private Preferences preferences = NbPreferences.forModule(getClass()).node("debugging"); // NOI18N
    private PreferenceChangeListener prefListener;
    private SessionsComboBoxListener sessionsComboListener;

    private transient ImageIcon resumeIcon;
    private transient ImageIcon focusedResumeIcon;
    private transient ImageIcon pressedResumeIcon;
    private transient ImageIcon suspendIcon;
    private transient ImageIcon focusedSuspendIcon;
    private transient ImageIcon pressedSuspendIcon;
    
    private DebugTreeView treeView;
    private TapPanel tapPanel;
    private InfoPanel infoPanel;
    private JPDADebugger debugger;
    private Session session;
    private JPDADebugger previousDebugger;

    private ViewRefresher viewRefresher = new ViewRefresher();
    private BarsPanel leftPanel;
    private IconsPanel rightPanel;
    
    private ThreadsListener threadsListener = null;
    private transient Reference<TopComponent> lastSelectedTCRef;
    private transient Reference<TopComponent> componentToActivateAfterClose;
    
    /**
     * instance/singleton of this class
     *
     * @see  #getInstance
     */
    private static Reference<DebuggingView> instance = null;
    
    /** Creates new form DebuggingView */
    public DebuggingView() {
        setIcon(ImageUtilities.loadImage ("org/netbeans/modules/debugger/jpda/resources/debugging.png")); // NOI18N
        // Remember the location of the component when closed.
        putClientProperty("KeepNonPersistentTCInModelWhenClosed", Boolean.TRUE); // NOI18N
        
        initComponents();
    
        resumeIcon = new ImageIcon(ImageUtilities.loadImage("org/netbeans/modules/debugger/jpda/resources/resume_button_16.png"));
        focusedResumeIcon = new ImageIcon(Utilities.loadImage("org/netbeans/modules/debugger/jpda/resources/resume_button_focused_16.png"));
        pressedResumeIcon = new ImageIcon(Utilities.loadImage("org/netbeans/modules/debugger/jpda/resources/resume_button_pressed_16.png"));
        suspendIcon = new ImageIcon(Utilities.loadImage("org/netbeans/modules/debugger/jpda/resources/suspend_button_16.png"));
        focusedSuspendIcon = new ImageIcon(Utilities.loadImage("org/netbeans/modules/debugger/jpda/resources/suspend_button_focused_16.png"));
        pressedSuspendIcon = new ImageIcon(Utilities.loadImage("org/netbeans/modules/debugger/jpda/resources/suspend_button_pressed_16.png"));
        
        setBackground(treeBackgroundColor);
        
        treeView = new DebugTreeView();
        treeView.setRootVisible(false);
        treeView.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        treeView.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        treeView.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DebuggingView.class, "DebuggingView.treeView.AccessibleContext.accessibleName")); // NOI18N
        treeView.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DebuggingView.class, "DebuggingView.treeView.AccessibleContext.accessibleDescription")); // NOI18N
        treeView.getTree().addMouseWheelListener(this);
        
        mainPanel.add(treeView, BorderLayout.CENTER);
        leftPanel = new BarsPanel();
        rightPanel = new IconsPanel();
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(rightPanel, BorderLayout.EAST);

        tapPanel = new TapPanel();
        tapPanel.setOrientation(TapPanel.DOWN);
        tapPanel.setExpanded(true);
        
        infoPanel = new InfoPanel(tapPanel);
        tapPanel.add(infoPanel);
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(tapPanel, gridBagConstraints);
        
        manager.addPropertyChangeListener(this);
        treeView.addTreeExpansionListener(this);
        TreeModel model = treeView.getTree().getModel();
        model.addTreeModelListener(this);
        
        prefListener = new DebuggingPreferenceChangeListener();
        preferences.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, prefListener, preferences));
        sessionsComboListener = new SessionsComboBoxListener();

        scrollBarPanel.setVisible(false);
        treeScrollBar.addAdjustmentListener(this);
        treeView.getViewport().addChangeListener(this);
        treeView.getTree().addTreeSelectionListener(this);

        setSuspendTableVisible(preferences.getBoolean(FiltersDescriptor.SHOW_SUSPEND_TABLE, true));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        sessionComboBox = new javax.swing.JComboBox();
        mainScrollPane = new javax.swing.JScrollPane();
        mainPanel = new javax.swing.JPanel();
        scrollBarPanel = new javax.swing.JPanel();
        treeScrollBar = new javax.swing.JScrollBar();
        leftPanel1 = new javax.swing.JPanel();
        rightPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        sessionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Java Project" }));
        sessionComboBox.setMaximumSize(new java.awt.Dimension(32767, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(sessionComboBox, gridBagConstraints);
        sessionComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(DebuggingView.class, "DebuggingView.sessionComboBox.AccessibleContext.accessibleName")); // NOI18N
        sessionComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(DebuggingView.class, "DebuggingView.sessionComboBox.AccessibleContext.accessibleDescription")); // NOI18N

        mainScrollPane.setBorder(null);
        mainScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainScrollPane.setPreferredSize(new java.awt.Dimension(32, 10));

        mainPanel.setLayout(new java.awt.BorderLayout());
        mainScrollPane.setViewportView(mainPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(mainScrollPane, gridBagConstraints);

        scrollBarPanel.setMaximumSize(new java.awt.Dimension(2147483647, 17));
        scrollBarPanel.setLayout(new java.awt.BorderLayout());

        treeScrollBar.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        scrollBarPanel.add(treeScrollBar, java.awt.BorderLayout.CENTER);

        leftPanel1.setPreferredSize(new java.awt.Dimension(8, 0));
        leftPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        scrollBarPanel.add(leftPanel1, java.awt.BorderLayout.WEST);

        rightPanel1.setPreferredSize(new java.awt.Dimension(24, 0));
        rightPanel1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));
        scrollBarPanel.add(rightPanel1, java.awt.BorderLayout.EAST);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(scrollBarPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel leftPanel1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JScrollPane mainScrollPane;
    private javax.swing.JPanel rightPanel1;
    private javax.swing.JPanel scrollBarPanel;
    private javax.swing.JComboBox sessionComboBox;
    private javax.swing.JScrollBar treeScrollBar;
    // End of variables declaration//GEN-END:variables

    public void setRootContext(Models.CompoundModel model, final DebuggerEngine engine) {
        {   // Destroy the old node
            Node root = manager.getRootContext();
            if (root != null) {
                try {
                    root.destroy();
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        if (threadsListener == null) {
            threadsListener = ThreadsListener.getDefault();
        }
        if (engine != null) {
            final JPDADebugger deb = engine.lookupFirst(null, JPDADebugger.class);
            if (deb != null) {
                if (threadsListener != null) {
                    threadsListener.setDebuggingView(this);
                }
            }
            synchronized (this) {
                if (previousDebugger != null) {
                    previousDebugger.removePropertyChangeListener(this);
                }
                previousDebugger = this.debugger;
                this.debugger = deb;
                if (deb != null) {
                    this.session = engine.lookupFirst(null, Session.class);
                    deb.addPropertyChangeListener(this);
                } else {
                    this.session = null;
                }
            }
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    threadsListener.changeDebugger(deb);
                }
            });
        } else {
            synchronized (this) {
                if (previousDebugger != null) {
                    previousDebugger.removePropertyChangeListener(this);
                }
                previousDebugger = null;
                this.debugger = null;
                this.session = null;
            }
            if (threadsListener != null) {
                threadsListener.changeDebugger(null);
            }
        }
        Node root;
        if (model == null) {
            root = Node.EMPTY;
        } else {
            root = Models.createNodes(model, treeView);
            treeView.setExpansionModel(model);
        }
        manager.setRootContext(root);
        refreshView();
        updateSessionsComboBox();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                adjustTreeScrollBar(-1);
                if (engine == null) {
                    // Clean up the UI from memory leaks:
                    setActivatedNodes (new Node[] {});
                    treeView.resetSelection();
                    treeView.updateUI();
                }
            }
        });
    }
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    public static DebuggingView getInstance() {
        DebuggingView view;
        view = (DebuggingView) WindowManager.getDefault().findTopComponent(ID);
        if (view == null) {
            view = getDefault();
        }
        return view;
    }

    /*public Action[] getFilterActions() {
        return FiltersDescriptor.getInstance().getFilterActions();
    }*/
    
    public void setSuspendTableVisible(boolean visible) {
        rightPanel.setVisible(visible);
    }
    
    /**
     * Singleton accessor reserved for the window systemm only. The window
     * system calls this method to create an instance of this
     * <code>TopComponent</code> from a <code>.settings</code> file.
     * <p>
     * <em>This method should not be called anywhere except from the window
     * system's code. </em>
     *
     * @return  singleton - instance of this class
     */
    public static synchronized DebuggingView getDefault() {
        DebuggingView view;
        if (instance == null) {
            view = new DebuggingView();
            instance = new WeakReference<DebuggingView>(view);
        } else {
            view = instance.get();
            if (view == null) {
                view = new DebuggingView();
                instance = new WeakReference<DebuggingView>(view);
            }
        }
        return view;
    }
    
    @Override
    protected String preferredID() {
        return this.getClass().getName();
    }

    @Override
    protected void componentShowing() {
        super.componentShowing ();
        if (viewModelListener != null) {
            viewModelListener.setUp();
            return;
        }
        if (viewModelListener != null) {
            throw new InternalError ();
        }
        viewModelListener = new ViewModelListener ("DebuggingView", this); // NOI18N
    }
    
    @Override
    protected void componentHidden() {
        super.componentHidden ();
        if (viewModelListener != null) {
            viewModelListener.destroy ();
        }
    }

    @Override
    protected void componentActivated() {
        ExplorerUtils.activateActions(manager, true);
    }

    @Override
    protected void componentDeactivated() {
        ExplorerUtils.activateActions(manager, false);
    }

    @Override
    protected void componentOpened() {
        // Remember which component was active so that we can re-activate it
        // after Debugging is closed.
        super.componentOpened();
        Mode debuggingMode = WindowManager.getDefault().findMode(this);
        lastSelectedTCRef = new WeakReference(debuggingMode.getSelectedTopComponent());
        requestVisible();
    }

    @Override
    public boolean canClose() {
        // Check whether we're active, if so, we'll re-activate the previously
        // active component.
        Mode debuggingMode = WindowManager.getDefault().findMode(this);
        if (debuggingMode.getSelectedTopComponent() == this) {
            componentToActivateAfterClose = lastSelectedTCRef;
        } else {
            componentToActivateAfterClose = null;
        }
        return super.canClose();
    }

    @Override
    protected void componentClosed() {
        // Re-activate the previously active component, if any.
        TopComponent lastSelectedTC = (componentToActivateAfterClose != null) ? componentToActivateAfterClose.get() : null;
        if (lastSelectedTC != null) {
            lastSelectedTC.requestActive();
        }
        super.componentClosed();
    }
    
    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx("DebuggingView"); // NOI18N
    }
    
    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ALWAYS;
    }
        
    @Override
    public boolean requestFocusInWindow() {
        return treeView.requestFocusInWindow ();
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage (DebuggingView.class, "CTL_Debugging_view"); // NOI18N
    }
    
    @Override
    public String getToolTipText() {
        return NbBundle.getMessage (DebuggingView.class, "CTL_Debugging_tooltip"); // NOI18N
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (ExplorerManager.PROP_ROOT_CONTEXT.equals(propertyName) || 
                ExplorerManager.PROP_NODE_CHANGE.equals(propertyName)) {
            refreshView();
        } else if (JPDADebugger.PROP_CURRENT_THREAD.equals(propertyName)) {
            refreshView();
        } else if (propertyName.equals (ExplorerManager.PROP_SELECTED_NODES)) {
            setActivatedNodes ((Node[]) evt.getNewValue ());
        }
    }

    private static boolean isJPDASession(Session s) {
        DebuggerEngine engine = s.getCurrentEngine ();
        if (engine == null) return false;
        return engine.lookupFirst(null, JPDADebugger.class) != null;
    }
    
    void updateSessionsComboBox() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                sessionComboBox.removeActionListener(sessionsComboListener);
                sessionComboBox.removePopupMenuListener(sessionsComboListener);
                ComboBoxModel model = sessionComboBox.getModel();
                sessionComboBox.removeAllItems();
                DebuggerManager dm = DebuggerManager.getDebuggerManager();
                Session[] sessions = dm.getSessions();
                for (int x = 0; x < sessions.length; x++) {
                    if (isJPDASession(sessions[x])) {
                        sessionComboBox.addItem(new SessionItem(sessions[x]));
                    }
                }
                if (model.getSize() == 0) {
                    sessionComboBox.addItem(new SessionItem(null));
                }
                sessionComboBox.setSelectedItem(new SessionItem(dm.getCurrentSession()));
                sessionComboBox.setVisible(model.getSize() > 1);
                sessionComboBox.addActionListener(sessionsComboListener);
                sessionComboBox.addPopupMenuListener(sessionsComboListener);
            }
        });
    }
    
    // **************************************************************************
    // implementation of TreeExpansion and TreeModel listener
    // **************************************************************************
    
    public void treeExpanded(TreeExpansionEvent event) {
        refreshView();
    }

    public void treeCollapsed(TreeExpansionEvent event) {
        refreshView();
    }

    public void treeNodesChanged(TreeModelEvent e) {
        refreshView();
    }

    public void treeNodesInserted(TreeModelEvent e) {
        refreshView();
    }

    public void treeNodesRemoved(TreeModelEvent e) {
        refreshView();
    }

    public void treeStructureChanged(TreeModelEvent e) {
        refreshView();
    }
    
    // **************************************************************************
    
    InfoPanel getInfoPanel() {
        return infoPanel;
    }
    
    void refreshView() {
        if (refreshScheduled) {
            return;
        }
        refreshScheduled = true;
        requestProcessor.post(new Runnable() {
            public void run() {
                SwingUtilities.invokeLater(viewRefresher);
            }
        }, 20);
    }

    private void adjustTreeScrollBar(int treeViewWidth) {
        JViewport viewport = treeView.getViewport();
        Point point = viewport.getViewPosition();
        if (point.y < 0) {
            viewport.setViewPosition(new Point(point.x, 0));
        }
        Dimension viewSize = viewport.getExtentSize();
        Dimension treeSize = viewport.getViewSize();
        if (treeViewWidth < 0) {
            treeViewWidth = treeSize.width;
        }
        int unitHeight = treeView.getUnitHeight();
        if (unitHeight > 0) {
            JScrollBar sbar = mainScrollPane.getVerticalScrollBar();
            if (sbar.getUnitIncrement() != unitHeight) {
                sbar.setUnitIncrement(unitHeight);
            }
        }
        if (treeViewWidth <= viewSize.width) {
            scrollBarPanel.setVisible(false);
        } else {
            treeScrollBar.setMaximum(treeViewWidth);
            treeScrollBar.setVisibleAmount(viewSize.width);
            if (unitHeight > 0) {
                treeScrollBar.setUnitIncrement(unitHeight / 2);
            }
            scrollBarPanel.setVisible(true);
        } // else
    }
    
    // **************************************************************************
    // implementation of AdjustmentListener (listens on horizontal scrollbar
    // connected to treeView)
    // **************************************************************************
    
    public void adjustmentValueChanged(AdjustmentEvent e) {
        JViewport viewport = treeView.getViewport();
        Point position = viewport.getViewPosition();
        Dimension viewSize = viewport.getExtentSize();
        Rectangle newRect = new Rectangle(e.getValue(), position.y, viewSize.width, viewSize.height);
        ((JComponent)viewport.getView()).scrollRectToVisible(newRect);
    }
    
    // **************************************************************************
    // implementation of ChangeListener on treeView
    // **************************************************************************
    
    public void stateChanged(ChangeEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                adjustTreeScrollBar(-1);
            }
        });
    }

    // **************************************************************************
    // implementation of MouseWheelListener on treeView
    // **************************************************************************
    
    public void mouseWheelMoved(MouseWheelEvent e) {
        JScrollBar scrollBar = mainScrollPane.getVerticalScrollBar();
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            int totalScrollAmount = e.getUnitsToScroll() * scrollBar.getUnitIncrement();
            scrollBar.setValue(scrollBar.getValue() + totalScrollAmount);
        }
    }

    // **************************************************************************
    // implementation of TreeSelectionListener
    // **************************************************************************
    
    public void valueChanged(TreeSelectionEvent e) {
        TreePath path = e.getNewLeadSelectionPath();
        if (path != null) {
            JTree tree = treeView.getTree();
            int row = tree.getRowForPath(path);
            Rectangle rect = tree.getRowBounds(row);
            if (rect == null) return ;
            JViewport viewport = mainScrollPane.getViewport();
            ((JComponent)viewport.getView()).scrollRectToVisible(rect);
        }
    }
    
    // **************************************************************************
    // inner classes
    // **************************************************************************
    
    private final class DebuggingPreferenceChangeListener implements PreferenceChangeListener {

        public void preferenceChange(PreferenceChangeEvent evt) {
            String key = evt.getKey();
            if (FiltersDescriptor.SHOW_SUSPEND_TABLE.equals(key)) {
                setSuspendTableVisible(evt.getNewValue().equals("true"));
            }
        }

    }

    private final class ViewRefresher implements Runnable {

        public void run() {
            refreshScheduled = false;
            leftPanel.clearBars();
            rightPanel.startReset();
            int sx = (rightPanel.getWidth() - ClickableIcon.CLICKABLE_ICON_WIDTH) / 2;
            int sy = 0;

            JPDAThread currentThread = debugger != null ? debugger.getCurrentThread() : null;
            // collect all deadlocked threads
            Set<Deadlock> deadlocks = debugger != null ? debugger.getThreadsCollector().getDeadlockDetector().getDeadlocks() : Collections.EMPTY_SET;
            if (deadlocks == null) {
                deadlocks = Collections.EMPTY_SET;
            }
            Set<JPDAThread> deadlockedThreads = new HashSet<JPDAThread>();
            for (Deadlock deadlock : deadlocks) {
                deadlockedThreads.addAll(deadlock.getThreads());
            }

            int mainPanelHeight = 0;
            int treeViewWidth = 0;
            int leftBarHeight = 0;
            boolean isCurrent = false;
            boolean isAtBreakpoint = false;
            boolean isInDeadlock = false;
            Object currentObject = null;
            int currentSY = 0;
            int height = 0;

            for (TreePath path : treeView.getVisiblePaths()) {
                Node node = Visualizer.findNode(path.getLastPathComponent());
                JPDAThread jpdaThread = node.getLookup().lookup(JPDAThread.class);
                JPDAThreadGroup jpdaThreadGroup = node.getLookup().lookup(JPDAThreadGroup.class);
                
                JTree tree = treeView.getTree();
                Rectangle rect = tree.getRowBounds(tree.getRowForPath(path));
                height = rect != null ? (int) Math.round(rect.getHeight()) : 0;
                
                if (jpdaThread != null || jpdaThreadGroup != null) {
                    if (currentObject != null) {
                        addPanels(currentObject, isCurrent, isAtBreakpoint, isInDeadlock,
                                leftBarHeight, sx, currentSY, height);
                    }
                    leftBarHeight = 0;
                    if (jpdaThread != null) {
                        isCurrent = jpdaThread == currentThread && jpdaThread.isSuspended();
                        isAtBreakpoint = threadsListener.isBreakpointHit(jpdaThread);
                        isInDeadlock = deadlockedThreads.contains(jpdaThread);
                    } else {
                        isCurrent = false;
                        isAtBreakpoint = false;
                        isInDeadlock = false;
                    }
                    currentObject = jpdaThread != null ? jpdaThread : jpdaThreadGroup;
                    currentSY = sy;
                }

                mainPanelHeight += height;
                treeViewWidth = rect != null ? Math.max(treeViewWidth, (int) Math.round(rect.getX() + rect.getWidth())) : treeViewWidth;
                leftBarHeight += height;
                sy += height;
            } // for
            if (currentObject != null) {
                addPanels(currentObject, isCurrent, isAtBreakpoint, isInDeadlock,
                        leftBarHeight, sx, currentSY, height);
            }

            rightPanel.endReset();
            leftPanel.repaint();
            rightPanel.revalidate();
            rightPanel.repaint();
            treeView.getTree().setPreferredSize(new Dimension(treeViewWidth, 0));
            mainPanel.setPreferredSize(new Dimension(0, mainPanelHeight));
            mainScrollPane.revalidate();
            mainPanel.revalidate();
            treeView.repaint();

            adjustTreeScrollBar(treeViewWidth);
        }

        private void addPanels(Object jpdaObject, boolean current, boolean atBreakpoint,
                boolean inDeadlock, int height, int sx, int sy, int rowHeight) {
            if (current || atBreakpoint || inDeadlock) {
                leftPanel.addBar(current, atBreakpoint, inDeadlock, height, sy);
            }
            if (jpdaObject instanceof JPDAThread) {
                rightPanel.addIcon(sx, sy, rowHeight, (JPDAThread)jpdaObject);
            }
        }
    }
    
    private class BarsPanel extends JPanel implements MouseMotionListener {
        
        private ArrayList<Bar> bars = new ArrayList<Bar>();

        BarsPanel() {
            setBackground(treeBackgroundColor);
            setPreferredSize(new Dimension(BAR_WIDTH, 0));
            setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
            addMouseMotionListener(this);
        }

        public void clearBars() {
            bars.clear();
        }

        public void addBar(boolean isCurrent, boolean atBreakpoint, boolean inDeadlock, int height, int sy) {
            String toolTipText = null;
            Color color = null;
            Color secondaryColor = null;
            if (inDeadlock) {
                color = deadlockColor;
                toolTipText = NbBundle.getMessage(DebuggingView.class, "LBL_DEADLOCKED_THREAD_TIP");
            } else if (isCurrent) {
                color = greenBarColor;
                toolTipText = NbBundle.getMessage(DebuggingView.class, "LBL_CURRENT_BAR_TIP");
            } else if (atBreakpoint) {
                color = hitsBarColor;
                toolTipText = NbBundle.getMessage(DebuggingView.class, "LBL_BREAKPOINT_HIT_TIP");
            }
            if (isCurrent && inDeadlock) {
                secondaryColor = greenBarColor;
                toolTipText = NbBundle.getMessage(DebuggingView.class, "LBL_CURRENT_DEADLOCKED_TIP");
            }
            Bar bar = new Bar(sy, height, color, secondaryColor, toolTipText);
            bars.add(bar);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Color originalColor = g.getColor();
            Rectangle clipRect = g.getClipBounds();
            double clipYStart = clipRect.getY();
            double clipYEnd = clipYStart + clipRect.getHeight() - 1;
            int width = (int)getBounds().getWidth();
            for (Bar bar : bars) {
                if (bar.sy + bar.height - 1 < clipYStart) {
                    continue;
                }
                if (bar.sy > clipYEnd) {
                    break;
                }
                if (bar.color != null) {
                    Rectangle rect = new Rectangle (0, bar.sy + 1, width, bar.height - 1);
                    rect = rect.intersection(clipRect);
                    g.setColor(bar.color);
                    g.fillRect(rect.x, rect.y, rect.width, rect.height);
                }
                if (bar.secondaryColor != null) {
                    Rectangle rect = new Rectangle (width / 2 - 1, bar.sy + 1, (width + 1) / 2 + 1, bar.height - 1);
                    rect = rect.intersection(clipRect);
                    if (!rect.isEmpty()) {
                        g.setColor(bar.secondaryColor);
                        g.fillRect(rect.x, rect.y, rect.width, rect.height);
                    }
                }
            } // for
            g.setColor(originalColor);
        }

        public void mouseDragged(MouseEvent e) {
            computeToolTipText(e);
        }

        public void mouseMoved(MouseEvent e) {
            computeToolTipText(e);
        }

        private void computeToolTipText(MouseEvent evt) {
            int sy = evt.getY();
            try {
                for (Bar bar : bars) {
                    if (sy >= bar.sy && sy < bar.sy + bar.height) {
                        setToolTipText(bar.toolTipText);
                        return;
                    }
                }
            } catch (ConcurrentModificationException e) {
            }
            setToolTipText(null);
        }
        
        private class Bar {
            private int sy;
            private int height;
            private Color color;
            private Color secondaryColor = null;
            private String toolTipText;

            Bar(int sy, int height, Color color, Color secondary, String toolTipText) {
                this.sy = sy;
                this.height = height;
                this.color = color;
                this.secondaryColor = secondary;
                this.toolTipText = toolTipText;
            }
        }
        
    }
    
    private class IconsPanel extends JPanel {
        
        private int endSY;
        private int counter;
        private ArrayList<IconItem> icons = new ArrayList<IconItem>();
        
        IconsPanel() {
            setBackground(treeBackgroundColor);
            setPreferredSize(new Dimension(ClickableIcon.CLICKABLE_ICON_WIDTH, 0));
            setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
        }

        public void startReset() {
            counter = 0;
            endSY = 0;
        }
        
        public void endReset() {
            int size = icons.size();
            for (int x = size - 1; x >= counter; x--) {
                remove(x);
                icons.remove(x);
            }
        }
        
        public void addIcon(int sx, int sy, int rowHeight, JPDAThread jpdaThread) {
            int height = sy - endSY + rowHeight;
            IconItem item;
            if (counter < icons.size()) {
                item = icons.get(counter);
                item.icon.changeThread(jpdaThread, DebuggingView.this, sx, sy,
                        ClickableIcon.CLICKABLE_ICON_WIDTH, height);
                item.panel.setPreferredSize(new Dimension(ClickableIcon.CLICKABLE_ICON_WIDTH, height));
                item.height = height;
            } else {
                JPanel panel = new JPanel(new GridBagLayout());
                panel.setBackground(treeBackgroundColor);
                panel.setOpaque(false);
                panel.setPreferredSize(new Dimension(ClickableIcon.CLICKABLE_ICON_WIDTH, height));
            
                ClickableIcon icon = new ClickableIcon(resumeIcon, focusedResumeIcon, pressedResumeIcon,
                        suspendIcon, focusedSuspendIcon, pressedSuspendIcon, jpdaThread, treeView);
                icon.setBackground(treeBackgroundColor);
                
                GridBagConstraints gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = 0;
                gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.weighty = 1.0;
                int delta = rowHeight - ClickableIcon.CLICKABLE_ICON_HEIGHT;
                int insetTop = delta / 2;
                int insetBottom = delta - insetTop;
                gridBagConstraints.insets = new Insets(insetTop, 0, insetBottom, 0);
                
                panel.add(icon, gridBagConstraints);
                icon.initializeState(DebuggingView.this, sx, sy, ClickableIcon.CLICKABLE_ICON_WIDTH, height);
                
                item = new IconItem(height, icon, panel);
                icons.add(item);
                add(panel);
            }
            counter++;
            endSY += height;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            treeView.paintStripes(g, this);
        }
        
        private class IconItem {
            private int height;
            private ClickableIcon icon;
            private JPanel panel;
            
            IconItem(int height, ClickableIcon icon, JPanel panel) {
                this.height = height;
                this.icon = icon;
                this.panel = panel;
            }
        }
        
    }
    
    private class SessionsComboBoxListener implements ActionListener, PopupMenuListener {

        SessionItem selectedItem = null;
        boolean popupVisible = false;
        
        public void actionPerformed(ActionEvent e) {
            SessionItem si = (SessionItem)sessionComboBox.getSelectedItem();
            if (popupVisible) {
                selectedItem = si;
            } else {
                changeSession(si);
            }
        }

        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            popupVisible = true;
        }

        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            changeSession(selectedItem);
            selectedItem = null;
            popupVisible = false;
        }

        public void popupMenuCanceled(PopupMenuEvent e) {
            selectedItem = null;
            popupVisible = false;
        }
        
        private void changeSession(SessionItem si) {
            if (si != null) {
                Session ses = si.getSession();
                DebuggerManager dm = DebuggerManager.getDebuggerManager();
                if (ses != null && ses != dm.getCurrentSession()) {
                    dm.setCurrentSession(ses);
                }
            }
        }
        
    }
    
    private class SessionItem {
        
        private Session session;

        SessionItem(Session session) {
            this.session = session;
        }
        
        public Session getSession() {
            return session;
        }

        @Override
        public String toString() {
            if (session != null) {
                return session.getName();
            } else {
                return '<' + NbBundle.getMessage(DebuggingView.class, "LBL_No_Session_Running") + '>';
            }
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SessionItem)) {
                return false;
            }
            Session s = ((SessionItem)obj).getSession();
            if (session == null) {
                return s == null;
            } else {
                return session.equals(s);
            }
        }

        @Override
        public int hashCode() {
            return 29 * 3 + (this.session != null ? this.session.hashCode() : 0);
        }

    }

}