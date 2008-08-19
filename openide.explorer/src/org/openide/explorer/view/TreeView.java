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
package org.openide.explorer.view;

import java.awt.AWTEvent;
import javax.swing.plaf.TreeUI;
import org.openide.awt.MouseUtils;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOp;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.accessibility.AccessibleContext;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.plaf.UIResource;
import javax.swing.text.Position;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.RowMapper;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;


/**
 * Base class for tree-style explorer views. 
 * @see BeanTreeView
 * @see ContextTreeView
 */
public abstract class TreeView extends JScrollPane {
    static {
        // Workaround for issue #42794 on JDK1.5
        UIManager.put("Tree.scrollsHorizontallyAndVertically", Boolean.TRUE);
    }

    //
    // static fields
    //

    static final Logger LOG = Logger.getLogger(TreeView.class.getName());

    /** generated Serialized Version UID */
    static final long serialVersionUID = -1639001987693376168L;

    /** How long it takes before collapsed nodes are released from the tree's cache
    */
    private static final int TIME_TO_COLLAPSE = (System.getProperty("netbeans.debug.heap") != null) ? 0 : 15000;

    /** Minimum width of this component. */
    private static final int MIN_TREEVIEW_WIDTH = 400;

    /** Minimum height of this component. */
    private static final int MIN_TREEVIEW_HEIGHT = 400;

    //GTK Look and feel hack
    private static boolean isSynth = UIManager.getLookAndFeel().getClass().getName().indexOf(
            "com.sun.java.swing.plaf.gtk"
        ) != -1;

    //
    // components
    //

    /** Main <code>JTree</code> component. */
    transient protected JTree tree;

    /** model */
    transient NodeTreeModel treeModel;

    /** Explorer manager, valid when this view is showing */
    transient ExplorerManager manager;

    // Attributes

    /** Mouse and action listener. */
    transient PopupSupport defaultActionListener;

    /** Property indicating whether the default action is enabled. */
    transient boolean defaultActionEnabled;

    /** not null if popup menu enabled */
    transient PopupAdapter popupListener;

    /** the most important listener (on four types of events */
    transient TreePropertyListener managerListener = null;

    /** weak variation of the listener for property change on the explorer manager */
    transient PropertyChangeListener wlpc;

    /** weak variation of the listener for vetoable change on the explorer manager */
    transient VetoableChangeListener wlvc;

    /** true if drag support is active */
    private transient boolean dragActive = true;

    /** true if drop support is active */
    private transient boolean dropActive = true;

    /** Drag support */
    transient TreeViewDragSupport dragSupport;

    /** Drop support */
    transient TreeViewDropSupport dropSupport;
    transient boolean dropTargetPopupAllowed = true;
    transient private Container contentPane;
    transient private List storeSelectedPaths;

    // default DnD actions
    transient private int allowedDragActions = DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_REFERENCE;
    transient private int allowedDropActions = DnDConstants.ACTION_COPY_OR_MOVE | DnDConstants.ACTION_REFERENCE;
    
    /**
     * Whether the quick search uses prefix or substring. 
     * Defaults to false meaning prefix is used.
     */
    transient private boolean quickSearchUsingSubstring = false;
    
    /** Holds VisualizerChildren and Visualizers for all visible nodes */
    private final Set<Object> visHolder = new HashSet<Object>();

    /** Constructor.
    */
    public TreeView() {
        this(true, true);
    }

    /** Constructor.
    * @param defaultAction should double click on a node open its default action?
    * @param popupAllowed should right-click open popup?
    */
    public TreeView(boolean defaultAction, boolean popupAllowed) {
        initializeTree();

//        // activation of drop target
//        if (DragDropUtilities.dragAndDropEnabled) {
//            setdroptExplorerDnDManager.getDefault().addFutureDropTarget(this);
//
//            // note: drag target is activated on focus gained
//        }
        setDropTarget( DragDropUtilities.dragAndDropEnabled );

        setPopupAllowed(popupAllowed);
        setDefaultActionAllowed(defaultAction);

        Dimension dim = null;

        try {
            dim = getPreferredSize();

            if (dim == null) {
                dim = new Dimension(MIN_TREEVIEW_WIDTH, MIN_TREEVIEW_HEIGHT);
            }
        } catch (NullPointerException npe) {
            dim = new Dimension(MIN_TREEVIEW_WIDTH, MIN_TREEVIEW_HEIGHT);
        }

        if (dim.width < MIN_TREEVIEW_WIDTH) {
            dim.width = MIN_TREEVIEW_WIDTH;
        }

        if (dim.height < MIN_TREEVIEW_HEIGHT) {
            dim.height = MIN_TREEVIEW_HEIGHT;
        }

        setPreferredSize(dim);
    }

    @Override
    public void updateUI() {
        Set<Object> tmp = visHolder;
        if (tmp != null) {
            tmp.clear();
        }

        super.updateUI();

        //On GTK L&F, the viewport border must be set to empty (not null!) or we still get border buildup
        setViewportBorder(BorderFactory.createEmptyBorder());
        setBorder(BorderFactory.createEmptyBorder());
    }

    /** Initializes the tree & model.
     * [dafe] Horrible technique - overridable method called from constructor
     * may result in subclass code invoked when this object is not fully
     * constructed.
     * However I don't have enough knowledge about this code to change it.
    */
    void initializeTree() {
        // initilizes the JTree
        treeModel = createModel();
        treeModel.addView(this);

        tree = new ExplorerTree(treeModel);

        NodeRenderer rend = new NodeRenderer();
        tree.setCellRenderer(rend);
        tree.putClientProperty("JTree.lineStyle", "Angled"); // NOI18N
        setViewportView(tree);

        // Init of the editor
        tree.setCellEditor(new TreeViewCellEditor(tree));
        tree.setEditable(true);
        tree.setRowHeight(16);
        tree.setLargeModel(true);

        // set selection mode to DISCONTIGUOUS_TREE_SELECTION as default
        setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        ToolTipManager.sharedInstance().registerComponent(tree);

        // init listener & attach it to closing of
        managerListener = new TreePropertyListener();
        tree.addTreeExpansionListener(managerListener);
        tree.addTreeWillExpandListener(managerListener);

        // do not care about focus
        setRequestFocusEnabled(false);

        defaultActionListener = new PopupSupport();
        getInputMap( JTree.WHEN_FOCUSED ).put( 
                KeyStroke.getKeyStroke( KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK ), "org.openide.actions.PopupAction" );
        getActionMap().put("org.openide.actions.PopupAction", defaultActionListener.popup);
        tree.addFocusListener(defaultActionListener);
        tree.addMouseListener(defaultActionListener);
    }

    /** Is it permitted to display a popup menu?
     * @return <code>true</code> if so
     */
    public boolean isPopupAllowed() {
        return popupListener != null;
    }

    /** Enable/disable displaying popup menus on tree view items.
    * Default is enabled.
    * @param value <code>true</code> to enable
    */
    public void setPopupAllowed(boolean value) {
        if ((popupListener == null) && value) {
            // on
            popupListener = new PopupAdapter();
            tree.addMouseListener(popupListener);

            return;
        }

        if ((popupListener != null) && !value) {
            // off
            tree.removeMouseListener(popupListener);
            popupListener = null;

            return;
        }
    }

    void setDropTargetPopupAllowed(boolean value) {
        dropTargetPopupAllowed = value;

        if (dropSupport != null) {
            dropSupport.setDropTargetPopupAllowed(value);
        }
    }

    boolean isDropTargetPopupAllowed() {
        return (dropSupport != null) ? dropSupport.isDropTargetPopupAllowed() : dropTargetPopupAllowed;
    }

    /** Does a double click invoke the default node action?
     * @return <code>true</code> if so
     */
    public boolean isDefaultActionEnabled() {
        return defaultActionEnabled;
    }

    /** Requests focus for the tree component. Overrides superclass method. */
    @Override
    public void requestFocus() {
        tree.requestFocus();
    }

    /** Requests focus for the tree component. Overrides superclass method. */
    @Override
    public boolean requestFocusInWindow() {
        return tree.requestFocusInWindow();
    }

    /** Enable/disable double click to invoke default action.
     * If defaultAction is not enabled double click expand/collapse node.
     * @param value <code>true</code> to enable
     */
    public void setDefaultActionAllowed(boolean value) {
        defaultActionEnabled = value;

        if (value) {
            tree.registerKeyboardAction(
                defaultActionListener, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_FOCUSED
            );
        } else {
            // Switch off.
            tree.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false));
        }
    }

    /**
    * Is the root node of the tree displayed?
    *
    * @return <code>true</code> if so
    */
    public boolean isRootVisible() {
        return tree.isRootVisible();
    }

    /** Set whether or not the root node from
    * the <code>TreeModel</code> is visible.
    *
    * @param visible <code>true</code> if it is to be displayed
    */
    public void setRootVisible(boolean visible) {
        tree.setRootVisible(visible);
        tree.setShowsRootHandles(!visible);
    }

    /**
     * Set whether the quick search feature uses substring or prefix
     * matching for the typed characters. Defaults to prefix (false).
     * @since 6.11
     * @param useSubstring <code>true</code> if substring search is used in quick search
     */
    public void setUseSubstringInQuickSearch(boolean useSubstring) {
        quickSearchUsingSubstring = useSubstring;
    }
    
    /********** Support for the Drag & Drop operations *********/
    /** Drag support is enabled by default.
    * @return true if dragging from the view is enabled, false
    * otherwise.
    */
    public boolean isDragSource() {
        return dragActive;
    }

    /** Enables/disables dragging support.
    * @param state true enables dragging support, false disables it.
    */
    public void setDragSource(boolean state) {
        // create drag support if needed
        if (state && (dragSupport == null)) {
            dragSupport = new TreeViewDragSupport(this, tree);
        }

        // activate / deactivate support according to the state
        dragActive = state;

        if (dragSupport != null) {
            dragSupport.activate(dragActive);
        }
    }

    /** Drop support is enabled by default.
    * @return true if dropping to the view is enabled, false
    * otherwise<br>
    */
    public boolean isDropTarget() {
        return dropActive;
    }

    /** Enables/disables dropping support.
    * @param state true means drops into view are allowed,
    * false forbids any drops into this view.
    */
    public void setDropTarget(boolean state) {
        // create drop support if needed
        if (dropActive && (dropSupport == null)) {
            dropSupport = new TreeViewDropSupport(this, tree, dropTargetPopupAllowed);
        }

        // activate / deactivate support according to the state
        dropActive = state;

        if (dropSupport != null) {
            dropSupport.activate(dropActive);
        }
    }

    /** Actions constants comes from {@link java.awt.dnd.DnDConstants}.
    * All actions (copy, move, link) are allowed by default.
    * @return int representing set of actions which are allowed when dragging from
    * asociated component.
     */
    public int getAllowedDragActions() {
        return allowedDragActions;
    }

    /** Sets allowed actions for dragging
    * @param actions new drag actions, using {@link java.awt.dnd.DnDConstants}
    */
    public void setAllowedDragActions(int actions) {
        // PENDING: check parameters
        allowedDragActions = actions;
    }

    /** Actions constants comes from {@link java.awt.dnd.DnDConstants}.
    * All actions are allowed by default.
    * @return int representing set of actions which are allowed when dropping
    * into the asociated component.
    */
    public int getAllowedDropActions() {
        return allowedDropActions;
    }

    /** Sets allowed actions for dropping.
    * @param actions new allowed drop actions, using {@link java.awt.dnd.DnDConstants}
    */
    public void setAllowedDropActions(int actions) {
        // PENDING: check parameters
        allowedDropActions = actions;
    }

    //
    // Control over expanded state
    //

    /** Collapses the tree under given node.
    *
    * @param n node to collapse
    */
    public void collapseNode(final Node n) {
        if (n == null) {
            throw new IllegalArgumentException();
        }

        // run safely to be sure all preceding events are processed (especially VisualizerEvent.Added)
        // otherwise VisualizerNodes may not be in hierarchy yet (see #140629)
        VisualizerNode.runSafe(new Runnable() {

            public void run() {
                tree.collapsePath(getTreePath(n));
            }
        });
    }

    /** Expandes the node in the tree.
    *
    * @param n node
    */
    public void expandNode(final Node n) {
        if (n == null) {
            throw new IllegalArgumentException();
        }

        lookupExplorerManager();

        // run safely to be sure all preceding events are processed (especially VisualizerEvent.Added)
        // otherwise VisualizerNodes may not be in hierarchy yet (see #140629)
        VisualizerNode.runSafe(new Runnable() {

            public void run() {
                tree.expandPath(getTreePath(n));
            }
        });
    }

    /** Test whether a node is expanded in the tree or not
    * @param n the node to test
    * @return true if the node is expanded
    */
    public boolean isExpanded(Node n) {
        return tree.isExpanded(getTreePath(n));
    }

    /** Expands all paths.
    */
    public void expandAll() {

        // run safely to be sure all preceding events are processed (especially VisualizerEvent.Added)
        // otherwise VisualizerNodes may not be in hierarchy yet (see #140629)
        VisualizerNode.runSafe(new Runnable() {

            public void run() {
                int i = 0;
                int j;

                do {
                    do {
                        j = tree.getRowCount();
                        tree.expandRow(i);
                    } while (j != tree.getRowCount());

                    i++;
                } while (i < tree.getRowCount());
            }
        });
    }

    //
    // Processing functions
    //

    @Override
    public void validate() {
        Children.MUTEX.readAccess(new Runnable() {
            public void run() {
                TreeView.super.validate();
            }
        });
    }

    /** Initializes the component and lookup explorer manager.
    */
    @Override
    public void addNotify() {
        super.addNotify();
        lookupExplorerManager();
    }

    /** Registers in the tree of components.
     */
    private void lookupExplorerManager() {
        // Enter key in the tree
        ExplorerManager newManager = ExplorerManager.find(TreeView.this);

        if (newManager != manager) {
            if (manager != null) {
                manager.removeVetoableChangeListener(wlvc);
                manager.removePropertyChangeListener(wlpc);
            }

            manager = newManager;

            manager.addVetoableChangeListener(wlvc = WeakListeners.vetoableChange(managerListener, manager));
            manager.addPropertyChangeListener(wlpc = WeakListeners.propertyChange(managerListener, manager));

            synchronizeRootContext();
            synchronizeExploredContext();
            synchronizeSelectedNodes();
        }

        // Sometimes the listener is registered twice and we get the 
        // selection events twice. Removing the listener before adding it
        // should be a safe fix.
        tree.getSelectionModel().removeTreeSelectionListener(managerListener);
        tree.getSelectionModel().addTreeSelectionListener(managerListener);
    }

    /** Deinitializes listeners.
    */
    @Override
    public void removeNotify() {
        super.removeNotify();

        tree.getSelectionModel().removeTreeSelectionListener(managerListener);
    }

    // *************************************
    // Methods to be overriden by subclasses
    // *************************************

    /** Allows subclasses to provide own model for displaying nodes.
    * @return the model to use for this view
    */
    protected abstract NodeTreeModel createModel();

    /** Called to allow subclasses to define the behaviour when a
    * node(s) are selected in the tree.
    *
    * @param nodes the selected nodes
    * @param em explorer manager to work on (change nodes to it)
    * @throws PropertyVetoException if the change cannot be done by the explorer
    *    (the exception is silently consumed)
    */
    protected abstract void selectionChanged(Node[] nodes, ExplorerManager em)
    throws PropertyVetoException;

    /** Called when explorer manager is about to change the current selection.
    * The view can forbid the change if it is not able to display such
    * selection.
    *
    * @param nodes the nodes to select
    * @return false if the view is not able to change the selection
    */
    protected abstract boolean selectionAccept(Node[] nodes);

    /** Show a given path in the screen. It depends on the kind of <code>TreeView</code>
    * if the path should be expanded or just made visible.
    *
    * @param path the path
    */
    protected abstract void showPath(TreePath path);

    /** Shows selection to reflect the current state of the selection in the explorer.
    *
    * @param paths array of paths that should be selected
    */
    protected abstract void showSelection(TreePath[] paths);

    /** Specify whether a context menu of the explored context should be used.
    * Applicable when no nodes are selected and the user wants to invoke
    * a context menu (clicks right mouse button).
    *
    * @return <code>true</code> if so; <code>false</code> in the default implementation
    */
    protected boolean useExploredContextMenu() {
        return false;
    }

    /** Check if selection of the nodes could break the selection mode set in TreeSelectionModel.
     * @param nodes the nodes for selection
     * @return true if the selection mode is broken */
    private boolean isSelectionModeBroken(Node[] nodes) {
        // if nodes are empty or single the everthing is ok
        // or if discontiguous selection then everthing ok
        if ((nodes.length <= 1) || (getSelectionMode() == TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION)) {
            return false;
        }

        // if many nodes
        // brakes single selection mode
        if (getSelectionMode() == TreeSelectionModel.SINGLE_TREE_SELECTION) {
            return true;
        }

        // check the contiguous selection mode
        TreePath[] paths = new TreePath[nodes.length];
        RowMapper rowMapper = tree.getSelectionModel().getRowMapper();

        // if rowMapper is null then tree bahaves as discontiguous selection mode is set
        if (rowMapper == null) {
            return false;
        }

        ArrayList<Node> toBeExpaned = new ArrayList<Node>(3);

        for (int i = 0; i < nodes.length; i++) {
            toBeExpaned.clear();

            Node n = nodes[i];

            while (n.getParentNode() != null) {
                if (!isExpanded(n)) {
                    toBeExpaned.add(n);
                }

                n = n.getParentNode();
            }

            for (int j = toBeExpaned.size() - 1; j >= 0; j--) {
                expandNode((Node) toBeExpaned.get(j));
            }
            paths[i] = getTreePath(nodes[i]);
        }

        int[] rows = rowMapper.getRowsForPaths(paths);

        // check selection's rows
        Arrays.sort(rows);

        for (int i = 1; i < rows.length; i++) {
            if (rows[i] != (rows[i - 1] + 1)) {
                return true;
            }
        }

        // all is ok
        return false;
    }
    
    private TreePath getTreePath(Node node) {
        return new TreePath(treeModel.getPathToRoot(VisualizerNode.getVisualizer(null, node)));
    }

    //
    // synchronizations
    //

    /** Called when selection in tree is changed.
    */
    final void callSelectionChanged(Node[] nodes) {
        manager.removePropertyChangeListener(wlpc);
        manager.removeVetoableChangeListener(wlvc);

        try {
            selectionChanged(nodes, manager);
        } catch (PropertyVetoException e) {
            synchronizeSelectedNodes();
        } finally {
            manager.addPropertyChangeListener(wlpc);
            manager.addVetoableChangeListener(wlvc);
        }
    }

    /** Synchronize the root context from the manager of this Explorer.
    */
    final void synchronizeRootContext() {
        treeModel.setNode(manager.getRootContext(), visHolder);
    }

    /** Synchronize the explored context from the manager of this Explorer.
    */
    final void synchronizeExploredContext() {
        final Node n = manager.getExploredContext();
        if (n == null) {
            return;
        }

        // run safely to be sure all preceding events are processed (especially VisualizerEvent.Added)
        // otherwise VisualizerNodes may not be in hierarchy yet (see #140629)
        VisualizerNode.runSafe(new Runnable() {

            public void run() {
                showPath(getTreePath(n));
            }
        });
    }

    /** Sets the selection model, which must be one of
     * TreeSelectionModel.SINGLE_TREE_SELECTION,
     * TreeSelectionModel.CONTIGUOUS_TREE_SELECTION or
     * TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION.
     * <p>
     * This may change the selection if the current selection is not valid
     * for the new mode. For example, if three TreePaths are
     * selected when the mode is changed to <code>TreeSelectionModel.SINGLE_TREE_SELECTION</code>,
     * only one TreePath will remain selected. It is up to the particular
     * implementation to decide what TreePath remains selected.
     * Note: TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION is set as default.
     * @since 2.15
     * @param mode selection mode
     */
    public void setSelectionMode(int mode) {
        tree.getSelectionModel().setSelectionMode(mode);
    }

    /** Returns the current selection mode, one of
     * <code>TreeSelectionModel.SINGLE_TREE_SELECTION</code>,
     * <code>TreeSelectionModel.CONTIGUOUS_TREE_SELECTION</code> or
     * <code>TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION</code>.
     * @since 2.15
     * @return selection mode
     */
    public int getSelectionMode() {
        return tree.getSelectionModel().getSelectionMode();
    }

    //
    // showing and removing the wait cursor
    //
    private void showWaitCursor() {
        if (getRootPane() == null) {
            return;
        }

        contentPane = getRootPane().getContentPane();

        if (SwingUtilities.isEventDispatchThread()) {
            contentPane.setCursor(Utilities.createProgressCursor(contentPane));
        } else {
            SwingUtilities.invokeLater(new CursorR(contentPane, Utilities.createProgressCursor(contentPane)));
        }
    }

    private void showNormalCursor() {
        if (contentPane == null) {
            return;
        }

        if (SwingUtilities.isEventDispatchThread()) {
            contentPane.setCursor(null);
        } else {
            SwingUtilities.invokeLater(new CursorR(contentPane, null));
        }
    }

    private void prepareWaitCursor(final Node node) {
        // check type of node
        if (node == null) {
            showNormalCursor();
        }

        showWaitCursor();
        RequestProcessor.getDefault().post(new Runnable() {

            public void run() {
                try {
                    node.getChildren().getNodesCount(true);
                } catch (Exception e) {
                    // log a exception
                    LOG.log(Level.WARNING, null, e);
                } finally {
                    // show normal cursor above all
                    showNormalCursor();
                }
            }
        });
    }

    /** Synchronize the selected nodes from the manager of this Explorer.
    * The default implementation does nothing.
    */
    final void synchronizeSelectedNodes() {
        // run safely to be sure all preceding events are processed (especially VisualizerEvent.Added)
        // otherwise VisualizerNodes may not be in hierarchy yet (see #140629)
        VisualizerNode.runSafe(new Runnable() {

            public void run() {
                Node[] arr = manager.getSelectedNodes();
                TreePath[] paths = new TreePath[arr.length];

                for (int i = 0; i < arr.length; i++) {
                    paths[i] = getTreePath(arr[i]);
                }

                tree.getSelectionModel().removeTreeSelectionListener(managerListener);
                showSelection(paths);
                tree.getSelectionModel().addTreeSelectionListener(managerListener);
            }
        });
    }

    void scrollTreeToVisible(TreePath path, TreeNode child) {
        Rectangle base = tree.getVisibleRect();
        Rectangle b1 = tree.getPathBounds(path);
        Rectangle b2 = tree.getPathBounds(new TreePath(treeModel.getPathToRoot(child)));

        if ((base != null) && (b1 != null) && (b2 != null)) {
            tree.scrollRectToVisible(new Rectangle(base.x, b1.y, 1, b2.y - b1.y + b2.height));
        }
    }

    private void createPopup(int xpos, int ypos, JPopupMenu popup) {
        if (popup.getSubElements().length > 0) {
            popup.show(TreeView.this, xpos, ypos);
        }
    }

    void createPopup(int xpos, int ypos) {
        // bugfix #23932, don't create if it's disabled
        if (isPopupAllowed()) {
            Node[] selNodes = manager.getSelectedNodes();

            if (selNodes.length > 0) {
                Action[] actions = NodeOp.findActions(selNodes);
                if (actions.length > 0) {
                    createPopup(xpos, ypos, Utilities.actionsToPopup(actions, this));
                }                
            } else if (manager.getRootContext() != null) {
                JPopupMenu popup = manager.getRootContext().getContextMenu();
                if (popup != null) {
                    createPopup(xpos, ypos, popup);
                }
            }                
        }
    }

    /* create standard popup menu and add newMenu to it
     */
    void createExtendedPopup(int xpos, int ypos, JMenu newMenu) {
        Node[] ns = manager.getSelectedNodes();
        JPopupMenu popup = null;

        if (ns.length > 0) {
            // if any nodes are selected --> find theirs actions
            Action[] actions = NodeOp.findActions(ns);
            popup = Utilities.actionsToPopup(actions, this);
        } else {
            // if none node is selected --> get context actions from view's root
            if (manager.getRootContext() != null) {
                popup = manager.getRootContext().getContextMenu();
            }
        }

        int cnt = 0;

        if (popup == null) {
            popup = SystemAction.createPopupMenu(new SystemAction[] {  });
        }

        popup.add(newMenu);

        createPopup(xpos, ypos, popup);
    }

    /** Returns the the point at which the popup menu is to be showed. May return null.
     * @return the point or null
     */
    Point getPositionForPopup() {
        int i = tree.getLeadSelectionRow();

        if (i < 0) {
            return null;
        }

        Rectangle rect = tree.getRowBounds(i);

        if (rect == null) {
            return null;
        }

        Point p = new Point(rect.x, rect.y);

        // bugfix #36984, convert point by TreeView.this
        p = SwingUtilities.convertPoint(tree, p, TreeView.this);

        return p;
    }

    static Action takeAction(Action action, Node ... nodes) {
        // bugfix #42843, use ContextAwareAction if possible
        if (action instanceof ContextAwareAction) {
            Lookup contextLookup = getLookupFor(nodes);

            Action contextInstance = ((ContextAwareAction) action).createContextAwareInstance(contextLookup);
            assert contextInstance != action : "Cannot be same. ContextAwareAction:  " + action +
            ", ContextAwareInstance: " + contextInstance;
            action = contextInstance;
        }

        return action;
    }
    
    private static Lookup getLookupFor(Node ... nodes) {
        if (nodes.length == 1) {
            Lookup contextLookup = nodes[0].getLookup ();
            Object o = contextLookup.lookup(nodes[0].getClass());
             // #55826, don't added the node twice
            if (!nodes[0].equals (o)) {
                 contextLookup = new ProxyLookup (new Lookup[] { Lookups.singleton (nodes[0]), contextLookup });
            }
            return contextLookup;
        } else {
            Lookup[] lkps = new Lookup[nodes.length];
            for (int i=0; i<nodes.length; i++) {
                lkps[i] = nodes[i].getLookup();
            }
            Lookup contextLookup = new ProxyLookup(lkps);
            Set<Node> toAdd = new HashSet<Node>(Arrays.asList(nodes));
            toAdd.removeAll(contextLookup.lookupAll(Node.class));

            if (!toAdd.isEmpty()) {
                contextLookup = new ProxyLookup(
                    contextLookup,
                    Lookups.fixed((Object[])toAdd.toArray(new Node[toAdd.size()])));
            }
            return contextLookup;
        }
    }

    /** Returns the tree path nearby to given tree node. Either a sibling if there is or the parent.
     * @param parentPath tree path to parent of changed nodes
     * @param childIndices indexes of changed children
     * @return the tree path or null if there no changed children
     */
    final static TreePath findSiblingTreePath(TreePath parentPath, int[] childIndices) {
        if (childIndices == null) {
            throw new IllegalArgumentException("Indexes of changed children are null."); // NOI18N
        }

        if (parentPath == null) {
            throw new IllegalArgumentException("The tree path to parent is null."); // NOI18N
        }

        // bugfix #29342, if childIndices is the empty then don't change the selection
        if (childIndices.length == 0) {
            return null;
        }

        TreeNode parent = (TreeNode) parentPath.getLastPathComponent();
        Object[] parentPaths = parentPath.getPath();
        TreePath newSelection = null;
        
        int childCount = parent.getChildCount();
        if (childCount > 0) {
            // get parent path, add child to it
            int childPathLength = parentPaths.length + 1;
            Object[] childPath = new Object[childPathLength];
            System.arraycopy(parentPaths, 0, childPath, 0, parentPaths.length);

            int selectedChild = Math.min(childIndices[0], childCount-1);

            childPath[childPathLength - 1] = parent.getChildAt(selectedChild);
            newSelection = new TreePath(childPath);
        } else {
            // all children removed, select parent
            newSelection = new TreePath(parentPaths);
        }

        return newSelection;
    }

    // Workaround for JDK issue 6472844 (NB #84970)
    void removedNodes(List<VisualizerNode> removed) {
        TreeSelectionModel sm = tree.getSelectionModel();
	TreePath[] selPaths = (sm != null) ? sm.getSelectionPaths() : null;
        if (selPaths == null) return;
        
        List<TreePath> remSel = null;
        for (VisualizerNode vn : removed) {
            visHolder.remove(vn.getChildren(false));
            TreePath path = new TreePath(vn.getPathToRoot());
	    for(TreePath tp : selPaths) {
                if (path.isDescendant(tp)) {
                    if (remSel == null) remSel = new ArrayList();
                    remSel.add(tp);
                }
	    }
        }
        
        if (remSel != null) {
            sm.removeSelectionPaths(remSel.toArray(new TreePath[remSel.size()]));
        }

        /*
        try {
            Field f = BasicTreeUI.class.getDeclaredField("treeState");
            f.setAccessible(true);
            AbstractLayoutCache cache = (AbstractLayoutCache)f.get(tree.getUI());
            cache.setModel(treeModel);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
         */
    }

    private static class CursorR implements Runnable {
        private Container contentPane;
        private Cursor c;

        private CursorR(Container cont, Cursor c) {
            contentPane = cont;
            this.c = c;
        }

        public void run() {
            contentPane.setCursor(c);
        }
    }

    /** Listens to the property changes on tree */
    class TreePropertyListener implements VetoableChangeListener, PropertyChangeListener, TreeExpansionListener,
        TreeWillExpandListener, TreeSelectionListener, Runnable {
        private RequestProcessor.Task scheduled;
        private TreePath[] readAccessPaths;
        
        TreePropertyListener() {
        }

        public void vetoableChange(PropertyChangeEvent evt)
        throws PropertyVetoException {
            if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                // issue 11928 check if selecetion mode will be broken
                Node[] nodes = (Node[]) evt.getNewValue();

                if (isSelectionModeBroken(nodes)) {
                    throw new PropertyVetoException(
                        "selection mode " + getSelectionMode() + " broken by " + Arrays.asList(nodes), evt
                    ); // NOI18N
                }

                if (!selectionAccept(nodes)) {
                    throw new PropertyVetoException("selection " + Arrays.asList(nodes) + " rejected", evt); // NOI18N
                }
            }
        }

        public final void propertyChange(final PropertyChangeEvent evt) {
            if (manager == null) {
                return; // the tree view has been removed before the event got delivered
            }
            Children.MUTEX.readAccess(new Runnable() {

                public void run() {
                    if (evt.getPropertyName().equals(ExplorerManager.PROP_ROOT_CONTEXT)) {
                        synchronizeRootContext();
                    }

                    if (evt.getPropertyName().equals(ExplorerManager.PROP_EXPLORED_CONTEXT)) {
                        synchronizeExploredContext();
                    }

                    if (evt.getPropertyName().equals(ExplorerManager.PROP_SELECTED_NODES)) {
                        synchronizeSelectedNodes();
                    }
                }
            });
        }

        public synchronized void treeExpanded(TreeExpansionEvent ev) {
            VisualizerNode vn = (VisualizerNode) ev.getPath().getLastPathComponent();
            visHolder.add(vn.getChildren());
            
            if (!tree.getScrollsOnExpand()) {
                return;
            }
            
            RequestProcessor.Task t = scheduled;

            if (t != null) {
                t.cancel();
            }

            class Request implements Runnable {
                private TreePath path;

                public Request(TreePath path) {
                    this.path = path;
                }

                public void run() {
                    if (!SwingUtilities.isEventDispatchThread()) {
                        SwingUtilities.invokeLater(this);
                        return;
                    }
                    if (!Children.MUTEX.isReadAccess() && !Children.MUTEX.isWriteAccess()) {
                        Children.MUTEX.readAccess(this);
                        return;
                    }
                    try {
                        if (!tree.isVisible(path)) {
                            // if the path is not visible - don't check the children
                            return;
                        }

                        if (treeModel == null) {
                            // no model, no action, no problem
                            return;
                        }

                        TreeNode myNode = (TreeNode) path.getLastPathComponent();

                        if (treeModel.getPathToRoot(myNode)[0] != treeModel.getRoot()) {
                            // the way from the path no longer
                            // goes to the root, probably someone
                            // has removed the node on the way up
                            // System.out.println("different roots.");
                            return;
                        }

                        // show wait cursor
                        //showWaitCursor ();
                        int lastChildIndex = myNode.getChildCount() - 1;

                        if (lastChildIndex >= 0) {
                            TreeNode lastChild = myNode.getChildAt(lastChildIndex);

                            Rectangle base = tree.getVisibleRect();
                            Rectangle b1 = tree.getPathBounds(path);
                            Rectangle b2 = tree.getPathBounds(new TreePath(treeModel.getPathToRoot(lastChild)));

                            if ((base != null) && (b1 != null) && (b2 != null)) {
                                tree.scrollRectToVisible(new Rectangle(base.x, b1.y, 1, b2.y - b1.y + b2.height));
                            }

                            //                        scrollTreeToVisible(path, lastChild);
                        }
                    } finally {
                        path = null;
                    }
                }
            }

            // It is OK to use multithreaded shared RP as the requests
            // will be serialized in event queue later
            scheduled = RequestProcessor.getDefault().post(new Request(ev.getPath()), 250); // hope that all children are there after this time
        }

        public synchronized void treeCollapsed(final TreeExpansionEvent ev) {

            showNormalCursor();
            class Request implements Runnable {
                private TreePath path;

                public Request(TreePath path) {
                    this.path = path;
                }

                public void run() {
                    if (!SwingUtilities.isEventDispatchThread()) {
                        SwingUtilities.invokeLater(this);

                        return;
                    }

                    // we are delayed, another treeExpanded() could arrive meanwhile
                    boolean expanded = true;

                    try {
                        expanded = tree.isExpanded(path);
                        if (expanded) {
                            // the tree shows the path - do not collapse
                            // the tree
                            return;
                        }

                        if (!tree.isVisible(path)) {
                            // if the path is not visible do not collapse
                            // the tree
                            return;
                        }

                        if (treeModel == null) {
                            // no model, no action, no problem
                            return;
                        }

                        TreeNode myNode = (TreeNode) path.getLastPathComponent();

                        if (treeModel.getPathToRoot(myNode)[0] != treeModel.getRoot()) {
                            // the way from the path no longer
                            // goes to the root, probably someone
                            // has removed the node on the way up
                            // System.out.println("different roots.");
                            return;
                        }

                        treeModel.nodeStructureChanged(myNode);
                    } finally {
                        if (!expanded) {
                            VisualizerNode vn = (VisualizerNode) path.getLastPathComponent();
                            visHolder.remove(vn.getChildren(false)); 
                        }
                        this.path = null;
                    }
                }
            }

            // It is OK to use multithreaded shared RP as the requests
            // will be serialized in event queue later
            // bugfix #37420, children of all collapsed folders will be throw out
            RequestProcessor.getDefault().post(new Request(ev.getPath()), TIME_TO_COLLAPSE);
        }

        /* Called whenever the value of the selection changes.
        * @param ev the event that characterizes the change.
        */
        public void valueChanged(TreeSelectionEvent ev) {
            TreePath[] paths = tree.getSelectionPaths();
            storeSelectedPaths = Arrays.asList((paths == null) ? new TreePath[0] : paths);

            if (paths == null) {
                // part of bugfix #37279, if DnD is active then is useless select a nearby node
                if (ExplorerDnDManager.getDefault().isDnDActive()) {
                    return;
                }

                callSelectionChanged(new Node[0]);
            } else {
                // we need to force no changes to nodes hierarchy =>
                // we are requesting read request, but it is not necessary
                // to execute the next action immediatelly, so postReadRequest
                // should be enough
                readAccessPaths = paths;
                Children.MUTEX.postReadRequest(this);
            }
        }

        /** Called under Children.MUTEX to refresh the currently selected nodes.
        */
        public void run() {
            if (readAccessPaths == null) {
                return;
            }

            TreePath[] paths = readAccessPaths;

            // non null value caused leak in
            // ComponentInspector
            // When the last Form was closed then the ComponentInspector was
            // closed as well. Since this variable was not null - 
            // last selected Node (RADComponentNode) was held ---> FormManager2 was held, etc.
            readAccessPaths = null;

            java.util.List<Node> ll = new java.util.ArrayList<Node>(paths.length);

            for (int i = 0; i < paths.length; i++) {
                Node n = Visualizer.findNode(paths[i].getLastPathComponent());

                if( isUnderRoot( manager.getRootContext(), n ) ) {
                    ll.add(n);
                }
            }
            callSelectionChanged(ll.toArray(new Node[ll.size()]));
        }
        
        /** Checks whether given Node is a subnode of rootContext.
        * @return true if specified Node is under current rootContext
        */
        private boolean isUnderRoot(Node rootContext, Node node) {
            while (node != null) {
                if (node.equals(rootContext)) {
                    return true;
                }

                node = node.getParentNode();
            }

            return false;
        }
            
        public void treeWillCollapse(TreeExpansionEvent event)
        throws ExpandVetoException {
        }

        public void treeWillExpand(TreeExpansionEvent event)
        throws ExpandVetoException {
            // prepare wait cursor and optionally show it
            TreePath path = event.getPath();
            prepareWaitCursor(DragDropUtilities.secureFindNode(path.getLastPathComponent()));
        }
    }
     // end of TreePropertyListener

    /** Popup adapter.
    */
    class PopupAdapter extends MouseUtils.PopupMouseAdapter {
        PopupAdapter() {
        }

        protected void showPopup(MouseEvent e) {
            int selRow = tree.getRowForLocation(e.getX(), e.getY());

            if ((selRow == -1) && !isRootVisible()) {
                // clear selection
                try {
                    manager.setSelectedNodes(new Node[]{});
                } catch (PropertyVetoException exc) {
                    assert false : exc; // not permitted to be thrown
                }
            } else if (!tree.isRowSelected(selRow)) {
                // This will set ExplorerManager selection as well.
                // If selRow == -1 the selection will be cleared.
                tree.setSelectionRow(selRow);
            }

            if ((selRow != -1) || !isRootVisible()) {
                Point p = SwingUtilities.convertPoint(e.getComponent(), e.getX(), e.getY(), TreeView.this);

                createPopup((int) p.getX(), (int) p.getY());
            }
        }
    }

    final class PopupSupport extends MouseAdapter implements Runnable, FocusListener, ActionListener {
        public final Action popup = new AbstractAction() {
                public void actionPerformed(ActionEvent evt) {
                    SwingUtilities.invokeLater(PopupSupport.this);
                }

                /**
                 * Returns true if the action is enabled.
                 *
                 * @return true if the action is enabled, false otherwise
                 * @see Action#isEnabled
                 */
            @Override
                public boolean isEnabled() {
                    return TreeView.this.isFocusOwner() || tree.isFocusOwner();
                }
            };

        //CallbackSystemAction csa;
        public void run() {
            Point p = getPositionForPopup();

            if (p == null) {
                //we're going to create a popup menu for the root node
                p = new Point(0, 0);
            }

            createPopup(p.x, p.y);
        }

        public void focusGained(java.awt.event.FocusEvent ev) {
            // unregister
            ev.getComponent().removeFocusListener(this);

            // lazy activation of drag source
            if (DragDropUtilities.dragAndDropEnabled && dragActive) {
                setDragSource(true);

                // note: dropTarget is activated in constructor
            }
        }

        public void focusLost(FocusEvent ev) {
        }

        /* clicking adapter */
        @Override
        public void mouseClicked(MouseEvent e) {
            int selRow = tree.getRowForLocation(e.getX(), e.getY());

            if ((selRow != -1) && SwingUtilities.isLeftMouseButton(e) && MouseUtils.isDoubleClick(e)) {
                // Default action.
                if (defaultActionEnabled) {
                    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                    Node node = Visualizer.findNode(selPath.getLastPathComponent());

                    Action a = takeAction(node.getPreferredAction(), node);

                    if (a != null) {
                        if (a.isEnabled()) {
                            a.actionPerformed(new ActionEvent(node, ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                        } else {
                            Toolkit.getDefaultToolkit().beep();
                        }

                        e.consume();

                        return;
                    }
                }

                if (tree.isExpanded(selRow)) {
                    tree.collapseRow(selRow);
                } else {
                    tree.expandRow(selRow);
                }
            }
        }

        /* VK_ENTER key processor */
        public void actionPerformed(ActionEvent evt) {
            Node[] nodes = manager.getSelectedNodes();
            
            if (nodes.length > 0) {
                Action a = nodes[0].getPreferredAction();
                if (a == null) {
                    return;
                }
                for (int i=1; i<nodes.length; i++) {
                    Action ai = nodes[i].getPreferredAction();
                    if (ai == null || !ai.equals(a)) {
                        return;
                    }
                }
                
                // switch to replacement action if there is some
                a = takeAction(a, nodes);
                if (a != null && a.isEnabled()) {
                    a.actionPerformed(new ActionEvent(
                            nodes.length == 1 ? nodes[0] : nodes,
                            ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                } else {
                    Toolkit.getDefaultToolkit().beep();
                }
            }
        }
    }

    private final class ExplorerTree extends JTree implements Autoscroll {
        AutoscrollSupport support;
        private String maxPrefix;
        int SEARCH_FIELD_SPACE = 3;
        private boolean firstPaint = true;

        // searchTextField manages focus because it handles VK_TAB key
        private JTextField searchTextField = new JTextField() {
            @Override
                public boolean isManagingFocus() {
                    return true;
                }

            @Override
                public void processKeyEvent(KeyEvent ke) {
                    //override the default handling so that
                    //the parent will never receive the escape key and
                    //close a modal dialog
                    if (ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        removeSearchField();
                        ke.consume();

                        // bugfix #32909, reqest focus when search field is removed
                        SwingUtilities.invokeLater(
                            new Runnable() {
                                //additional bugfix - do focus change later or removing
                                //the component while it's focused will cause focus to
                                //get transferred to the next component in the 
                                //parent focusTraversalPolicy *after* our request
                                //focus completes, so focus goes into a black hole - Tim
                                public void run() {
                                    ExplorerTree.this.requestFocus();
                                }
                            }
                        );
                    } else {
                        super.processKeyEvent(ke);
                    }
                }
            };

        private JPanel searchpanel = null;
        final private int heightOfTextField = searchTextField.getPreferredSize().height;
        private int originalScrollMode;

        ExplorerTree(TreeModel model) {
            super(model);
            toggleClickCount = 0;

            // fix for #18292
            // default action map for JTree defines these shortcuts
            // but we use our own mechanism for handling them
            // following lines disable default L&F handling (if it is
            // defined on Ctrl-c, Ctrl-v and Ctrl-x)
            getInputMap().put(KeyStroke.getKeyStroke("control C"), "none"); // NOI18N
            getInputMap().put(KeyStroke.getKeyStroke("control V"), "none"); // NOI18N
            getInputMap().put(KeyStroke.getKeyStroke("control X"), "none"); // NOI18N
            getInputMap().put(KeyStroke.getKeyStroke("COPY"), "none"); // NOI18N
            getInputMap().put(KeyStroke.getKeyStroke("PASTE"), "none"); // NOI18N
            getInputMap().put(KeyStroke.getKeyStroke("CUT"), "none"); // NOI18N

            if (Utilities.isMac()) {
                getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.META_MASK), "none"); // NOI18N
                getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.META_MASK), "none"); // NOI18N
                getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.META_MASK), "none"); // NOI18N
            }

            setupSearch();
            
            setDragEnabled( true );
        }

        @Override
        public void addNotify() {
            super.addNotify();
            ViewTooltips.register(this);
        }
        
        @Override
        public void removeNotify() {
            super.removeNotify();
            ViewTooltips.unregister(this);
        }

        @Override
        public void updateUI() {
            super.updateUI();
            setBorder(BorderFactory.createEmptyBorder());
            if( getTransferHandler() != null && getTransferHandler() instanceof UIResource ) {
                //we handle drag and drop in our own way, so let's just fool the UI with a dummy
                //TransferHandler to ensure that multiple selection is not lost when drag starts
                setTransferHandler( new DummyTransferHandler() );
            }
        }
        
        private void calcRowHeight(Graphics g) {
            int height = Math.max(18, 2 + g.getFontMetrics(getFont()).getHeight());

            //Issue 42743/"Jesse mode"
            String s = System.getProperty("nb.cellrenderer.fixedheight"); //NOI18N

            if (s != null) {
                try {
                    height = Integer.parseInt(s);
                } catch (Exception e) {
                    //do nothing, height not changed
                }
            }

            if (getRowHeight() != height) {
	        setRowHeight(height);
            } else {
                revalidate();
                repaint();
            }
        }

        //
        // Certain operation should be executed in guarded mode - e.g.
        // not allow changes in nodes during the operation being executed
        //
        @Override
        public void paint(final Graphics g) {
            new GuardedActions(0, g);
        }

        @Override
        protected void validateTree() {
            new GuardedActions(1, null);
        }

        @Override
        public Dimension getPreferredSize() {
            return (Dimension)new GuardedActions(5, null).ret;
        }

        @Override
        public void doLayout() {
            new GuardedActions(2, null);
        }

        @Override
        public void setUI(TreeUI ui) {
            super.setUI(ui);
            for (Object key : getActionMap().allKeys()) {
                if( "cancel".equals(key) ) //NOI18N
                    continue;
                Action a = getActionMap().get(key);
                if (a.getClass().getName().contains("TreeUI")) {
                    getActionMap().put(key, new GuardedActions(99, a));
                }
            }
        }
/*
        @Override
        public void expandPath(TreePath path) {
            new GuardedActions(7, path);
        }

        @Override
        public Rectangle getPathBounds(TreePath path) {
            return (Rectangle) new GuardedActions(8, path).ret;
        }

        @Override
        public TreePath getPathForRow(int row) {
            return (TreePath) new GuardedActions(9, row).ret;
        }
        */

        private void doProcessEvent(AWTEvent e) {
            super.processEvent(e);
        }

        private void guardedPaint(Graphics g) {
            if (firstPaint) {
                firstPaint = false;
                calcRowHeight(g);

                //This will generate a repaint, so don't bother continuing with super.paint()
                //but do paint the background color so it doesn't paint gray the first time
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());

                return;
            }

            try {
                ExplorerTree.super.paint(g);
            } catch (NullPointerException ex) {
                // #139696: Making this issue more acceptable by not showing a dialog
                // still it deserves more investigation later
               LOG.log(Level.INFO, "Problems while painting", ex);  // NOI18N
            }
        }

        private void guardedValidateTree() {
            super.validateTree();
        }

        private void guardedDoLayout() {
            super.doLayout();

            Rectangle visibleRect = getVisibleRect();

            if ((searchpanel != null) && searchpanel.isDisplayable()) {
                int width = searchpanel.getPreferredSize().width;

                searchpanel.setBounds(
                    Math.max(SEARCH_FIELD_SPACE, (visibleRect.x + visibleRect.width) - width),
                    visibleRect.y + SEARCH_FIELD_SPACE, Math.min(visibleRect.width, width) - SEARCH_FIELD_SPACE,
                    heightOfTextField
                );
            }
        }

        @Override
        public void setFont(Font f) {
            if (f != getFont()) {
                firstPaint = true;
                super.setFont(f);
            }
        }

        @Override
        protected void processFocusEvent(FocusEvent fe) {
            new GuardedActions(3, fe);
        }

        private void repaintSelection() {
            int first = getSelectionModel().getMinSelectionRow();
            int last = getSelectionModel().getMaxSelectionRow();

            if (first != -1) {
                if (first == last) {
                    Rectangle r = getRowBounds(first);
                    if (r == null) {
                        repaint();
                        return;
                    }
                    repaint(r.x, r.y, r.width, r.height);
                } else {
                    Rectangle top = getRowBounds(first);
                    Rectangle bottom = getRowBounds(last);
                    if (top == null || bottom == null) {
                        repaint();
                        return;
                    }
                    Rectangle r = new Rectangle();
                    r.x = Math.min(top.x, bottom.x);
                    r.y = top.y;
                    r.width = getWidth();
                    r.height = (bottom.y + bottom.height) - top.y;
                    repaint(r.x, r.y, r.width, r.height);
                }
            }
        }

        private void prepareSearchPanel() {
            if (searchpanel == null) {
                searchpanel = new JPanel();

                JLabel lbl = new JLabel(NbBundle.getMessage(TreeView.class, "LBL_QUICKSEARCH")); //NOI18N
                searchpanel.setLayout(new BoxLayout(searchpanel, BoxLayout.X_AXIS));
                searchpanel.add(lbl);
                searchpanel.add(searchTextField);
                lbl.setLabelFor(searchTextField);
                searchTextField.setColumns(10);
                searchpanel.setBorder(BorderFactory.createRaisedBevelBorder());
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            }
        }

        private void setupSearch() {
            // Remove the default key listeners
            KeyListener[] keyListeners = getListeners(KeyListener.class);

            for (int i = 0; i < keyListeners.length; i++) {
                removeKeyListener(keyListeners[i]);
            }

            // Add new key listeners
            addKeyListener(
                new KeyAdapter() {
                @Override
                    public void keyTyped(KeyEvent e) {
                        int modifiers = e.getModifiers();
                        int keyCode = e.getKeyCode();
                        char c = e.getKeyChar();

                        //#43617 - don't eat + and -
                        //#98634 - and all its duplicates dont't react to space
                        if ((c == '+') || (c == '-') || (c==' ')) return; // NOI18N

                        if (((modifiers > 0) && (modifiers != KeyEvent.SHIFT_MASK)) || e.isActionKey()) {
                            return;
                        }

                        if (Character.isISOControl(c) ||
                              (keyCode == KeyEvent.VK_SHIFT) ||
			      (keyCode == KeyEvent.VK_ESCAPE)) return;

                        final KeyStroke stroke = KeyStroke.getKeyStrokeForEvent(e);
                        searchTextField.setText(String.valueOf(stroke.getKeyChar()));

                        displaySearchField();
                        e.consume();
                    }
                }
            );

            // Create a the "multi-event" listener for the text field. Instead of
            // adding separate instances of each needed listener, we're using a
            // class which implements them all. This approach is used in order 
            // to avoid the creation of 4 instances which takes some time
            SearchFieldListener searchFieldListener = new SearchFieldListener();
            searchTextField.addKeyListener(searchFieldListener);
            searchTextField.addFocusListener(searchFieldListener);
            searchTextField.getDocument().addDocumentListener(searchFieldListener);
        }

        private List<TreePath> doSearch(String prefix) {
            List<TreePath> results = new ArrayList<TreePath>();

            // do search forward the selected index
            int[] rows = getSelectionRows();
            int startIndex = ((rows == null) || (rows.length == 0)) ? 0 : rows[0];

            int size = getRowCount();

            if (size == 0) {
                // Empty tree (no root visible); cannot match anything.
                return results;
            }

            while (true) {
                startIndex = startIndex % size;

                TreePath path = null;
                if (quickSearchUsingSubstring) {
                    path = getNextSubstringMatch(prefix, startIndex, Position.Bias.Forward);
                } else {
                    path = getNextMatch(prefix, startIndex, Position.Bias.Forward);
                }

                if ((path != null) && !results.contains(path)) {
                    startIndex = tree.getRowForPath(path);
                    results.add(path);

                    if (!quickSearchUsingSubstring) {
                        String elementName = ((VisualizerNode) path.getLastPathComponent()).getDisplayName();

                        // initialize prefix
                        if (maxPrefix == null) {
                            maxPrefix = elementName;
                        }

                        maxPrefix = findMaxPrefix(maxPrefix, elementName);
                    }
                    // try next element
                    startIndex++;
                } else {
                    break;
                }
            }

            return results;
        }

        private String findMaxPrefix(String str1, String str2) {
            String res = null;

            for (int i = 0; str1.regionMatches(true, 0, str2, 0, i); i++) {
                res = str1.substring(0, i);
            }

            return res;
        }
        
        /**
         * Copied and adapted from JTree.getNextMatch(...).
         */
        private TreePath getNextSubstringMatch(
                String substring, int startingRow, Position.Bias bias) {

            int max = getRowCount();
            if (substring == null) {
                throw new IllegalArgumentException();
            }
            if (startingRow < 0 || startingRow >= max) {
                throw new IllegalArgumentException();
            }
            substring = substring.toUpperCase();

            // start search from the next/previous element froom the 
            // selected element
            int increment = (bias == Position.Bias.Forward) ? 1 : -1;
            int row = startingRow;
            do {
                TreePath path = getPathForRow(row);
                String text = convertValueToText(
                    path.getLastPathComponent(), isRowSelected(row),
                    isExpanded(row), true, row, false);

                if (text.toUpperCase().indexOf(substring) >= 0) {
                    return path;
                }
                row = (row + increment + max) % max;
            } while (row != startingRow);
            return null;
        }

        /**
         * Adds the search field to the tree.
         */
        private void displaySearchField() {
            if (!searchTextField.isDisplayable()) {
                JViewport viewport = TreeView.this.getViewport();
                originalScrollMode = viewport.getScrollMode();
                viewport.setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
                searchTextField.setFont(ExplorerTree.this.getFont());
                prepareSearchPanel();
                add(searchpanel);
                revalidate();
                repaint();
                searchTextField.requestFocus();
            }
        }

        /**
         * Removes the search field from the tree.
         */
        private void removeSearchField() {
            if (searchpanel.isDisplayable()) {
                remove(searchpanel);
                TreeView.this.getViewport().setScrollMode(originalScrollMode);

                Rectangle r = searchpanel.getBounds();
                this.repaint(r);
            }
        }

        /** notify the Component to autoscroll */
        public void autoscroll(Point cursorLoc) {
            getSupport().autoscroll(cursorLoc);
        }

        /** @return the Insets describing the autoscrolling
         * region or border relative to the geometry of the
         * implementing Component.
         */
        public Insets getAutoscrollInsets() {
            return getSupport().getAutoscrollInsets();
        }

        /** Safe getter for autoscroll support. */
        AutoscrollSupport getSupport() {
            if (support == null) {
                support = new AutoscrollSupport(this, new Insets(15, 10, 15, 10));
            }

            return support;
        }

        @Override
        public String getToolTipText(MouseEvent event) {
            return (String) new GuardedActions(6, event).ret;
        }
        final String getToolTipTextImpl(MouseEvent event) {
            if (event != null) {
                Point p = event.getPoint();
                int selRow = getRowForLocation(p.x, p.y);

                if (selRow != -1) {
                    TreePath path = getPathForRow(selRow);
                    VisualizerNode v = (VisualizerNode) path.getLastPathComponent();
                    String tooltip = v.getShortDescription();
                    String displayName = v.getDisplayName();

                    if ((tooltip != null) && !tooltip.equals(displayName)) {
                        return tooltip;
                    }
                }
            }

            return null;
        }

        @Override
        protected TreeModelListener createTreeModelListener() {
            return new ModelHandler();
        }

        @Override
        public AccessibleContext getAccessibleContext() {
            if (accessibleContext == null) {
                accessibleContext = new AccessibleExplorerTree();
            }

            return accessibleContext;
        }

        private class GuardedActions implements Mutex.Action<Object>, Action {
            private int type;
            private Object p1;
            final Object ret;

            public GuardedActions(int type, Object p1) {
                this.type = type;
                this.p1 = p1;
                if (type == 99) {
                    ret = null;
                    return;
                }
                if (Children.MUTEX.isReadAccess() || Children.MUTEX.isWriteAccess()) {
                    ret = run();
                } else {
                    ret = Children.MUTEX.readAccess(this);
                }
            }

            public Object run() {
                switch (type) {
                case 0:
                    guardedPaint((Graphics) p1);
                    break;
                case 1:
                    guardedValidateTree();
                    break;
                case 2:
                    guardedDoLayout();
                    break;
                case 3:
                    ExplorerTree.super.processFocusEvent((FocusEvent)p1);
                    //Since the selected when focused is different, we need to force a
                    //repaint of the entire selection, but let's do it in guarded more
                    //as any other repaint
                    repaintSelection();
                    break;
                case 4:
                    doProcessEvent((AWTEvent)p1);
                    break;
                case 5:
                    return ExplorerTree.super.getPreferredSize();
                case 6:
                    return getToolTipTextImpl((MouseEvent)p1);
                case 7:
                    ExplorerTree.super.expandPath((TreePath) p1);
                    break;
                case 8:
                    return ExplorerTree.super.getPathBounds((TreePath) p1);
                case 9:
                    return ExplorerTree.super.getPathForRow((Integer) p1);
                case 10:
                    Object[] arr = (Object[])p1;
                    return ExplorerTree.super.processKeyBinding(
                        (KeyStroke)arr[0],
                        (KeyEvent)arr[1],
                        (Integer)arr[2],
                        (Boolean)arr[3]
                    );
                default:
                    throw new IllegalStateException("type: " + type);
                }

                return null;
            }

            public Object getValue(String key) {
                return ((Action)p1).getValue(key);
            }

            public void putValue(String key, Object value) {
                ((Action)p1).putValue(key, value);
            }

            public void setEnabled(boolean b) {
                ((Action)p1).setEnabled(b);
            }

            public boolean isEnabled() {
                return ((Action)p1).isEnabled();
            }

            public void addPropertyChangeListener(PropertyChangeListener listener) {
            }

            public void removePropertyChangeListener(PropertyChangeListener listener) {
            }

            public void actionPerformed(final ActionEvent e) {
                Children.MUTEX.readAccess(new Runnable() {
                    public void run() {
                            ((Action)p1).actionPerformed(e);
                    }
                });
            }
            
        }

        private class SearchFieldListener extends KeyAdapter implements DocumentListener, FocusListener {
            /** The last search results */
            private List<TreePath> results = new ArrayList<TreePath>();

            /** The last selected index from the search results. */
            private int currentSelectionIndex;

            SearchFieldListener() {
            }

            public void changedUpdate(DocumentEvent e) {
                searchForNode();
            }

            public void insertUpdate(DocumentEvent e) {
                searchForNode();
            }

            public void removeUpdate(DocumentEvent e) {
                searchForNode();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                int keyCode = e.getKeyCode();

                if (keyCode == KeyEvent.VK_ESCAPE) {
                    removeSearchField();
                    ExplorerTree.this.requestFocus();
                } else if (keyCode == KeyEvent.VK_UP) {
                    currentSelectionIndex--;
                    displaySearchResult();

                    // Stop processing the event here. Otherwise it's dispatched
                    // to the tree too (which scrolls)
                    e.consume();
                } else if (keyCode == KeyEvent.VK_DOWN) {
                    currentSelectionIndex++;
                    displaySearchResult();

                    // Stop processing the event here. Otherwise it's dispatched
                    // to the tree too (which scrolls)
                    e.consume();
                } else if (keyCode == KeyEvent.VK_TAB) {
                    if (maxPrefix != null) {
                        searchTextField.setText(maxPrefix);
                    }

                    e.consume();
                } else if (keyCode == KeyEvent.VK_ENTER) {
                    removeSearchField();

                    // bugfix #39607, don't expand selected node when default action invoked
                    TreePath selectedTPath = getSelectionPath();

                    if (selectedTPath != null) {
                        TreeNode selectedTNode = (TreeNode) selectedTPath.getLastPathComponent();
                        Node selectedNode = Visualizer.findNode(selectedTNode);

                        if (
                            (selectedNode.getPreferredAction() == null) ||
                                !selectedNode.getPreferredAction().isEnabled()
                        ) {
                            expandPath(getSelectionPath());
                        }
                    }

                    ExplorerTree.this.requestFocus();
                    ExplorerTree.this.dispatchEvent(e);
                }
            }

            /** Searches for a node in the tree. */
            private void searchForNode() {
                currentSelectionIndex = 0;
                results.clear();
                maxPrefix = null;

                String text = searchTextField.getText().toUpperCase();

                if (text.length() > 0) {
                    results = doSearch(text);
                    displaySearchResult();
                }
            }

            private void displaySearchResult() {
                int sz = results.size();

                if (sz > 0) {
                    if (currentSelectionIndex < 0) {
                        currentSelectionIndex = sz - 1;
                    } else if (currentSelectionIndex >= sz) {
                        currentSelectionIndex = 0;
                    }

                    TreePath path = results.get(currentSelectionIndex);
                    setSelectionPath(path);
                    scrollPathToVisible(path);
                } else {
                    clearSelection();
                }
            }

            public void focusGained(FocusEvent e) {
                // Do nothing
            }

            public void focusLost(FocusEvent e) {
                removeSearchField();
            }
        }

        private class AccessibleExplorerTree extends JTree.AccessibleJTree {
            AccessibleExplorerTree() {
            }

            @Override
            public String getAccessibleName() {
                return TreeView.this.getAccessibleContext().getAccessibleName();
            }

            @Override
            public String getAccessibleDescription() {
                return TreeView.this.getAccessibleContext().getAccessibleDescription();
            }
        }

        private class ModelHandler extends JTree.TreeModelHandler {
            ModelHandler() {
            }

            @Override
            public void treeStructureChanged(TreeModelEvent e) {
                // Remember selections and expansions
                TreePath[] selectionPaths = getSelectionPaths();
                java.util.Enumeration expanded = getExpandedDescendants(e.getTreePath());

                // Restructure the node
                super.treeStructureChanged(e);

                // Expand previously expanded paths
                if (expanded != null) {
                    while (expanded.hasMoreElements()) {
                        expandPath((TreePath) expanded.nextElement());
                    }
                }

                // Select previously selected paths
                if ((selectionPaths != null) && (selectionPaths.length > 0)) {
                    boolean wasSelected = isPathSelected(selectionPaths[0]);

                    setSelectionPaths(selectionPaths);

                    if (!wasSelected) {
                        // do not scroll if the first selection path survived structure change
                        scrollPathToVisible(selectionPaths[0]);
                    }
                }
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
                // called to removed from JTree.expandedState
                super.treeNodesRemoved(e);

                // part of bugfix #37279, if DnD is active then is useless select a nearby node
                if (ExplorerDnDManager.getDefault().isDnDActive()) {
                    return;
                }

                if (tree.getSelectionCount() == 0) {
                    TreePath path = findSiblingTreePath(e.getTreePath(), e.getChildIndices());

                    // bugfix #39564, don't select again the same object
                    if ((path == null) || path.equals(e.getTreePath())) {
                        return;
                    } else if (path.getPathCount() > 0) {
                        tree.setSelectionPath(path);
                    }
                }
            }
        }
    }
    
    private static class DummyTransferHandler extends TransferHandler /*implements UIResource*/ {
        @Override
        public void exportAsDrag(JComponent comp, InputEvent e, int action) {
            //do nothing - ExplorerDnDManager will kick in when necessary
        }
        @Override
        public void exportToClipboard(JComponent comp, Clipboard clip, int action)
                                                      throws IllegalStateException {
            //do nothing - Node actions will hande this
        }
        @Override
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            return false; //TreeViewDropSupport will decided
        }
        @Override
        public boolean importData(JComponent comp, Transferable t) {
            return false;
        }
        @Override
        public int getSourceActions(JComponent c) {
            return COPY_OR_MOVE;
        }
    }
}
