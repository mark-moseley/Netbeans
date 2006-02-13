/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.explorer.view;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import org.openide.ErrorManager;
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
import java.util.Iterator;
import java.util.List;

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
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
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

        // activation of drop target
        if (DragDropUtilities.dragAndDropEnabled) {
            ExplorerDnDManager.getDefault().addFutureDropTarget(this);

            // note: drag target is activated on focus gained
        }

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

    public void updateUI() {
        super.updateUI();

        //On GTK L&F, the viewport border must be set to empty (not null!) or we still get border buildup
        setViewportBorder(BorderFactory.createEmptyBorder());
        setBorder(BorderFactory.createEmptyBorder());
    }

    public Border getBorder() {
        if (isSynth) {
            return BorderFactory.createEmptyBorder();
        } else {
            return super.getBorder();
        }
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

        tree = new ExplorerTree(treeModel);

        NodeRenderer rend = new NodeRenderer();
        tree.setCellRenderer(rend);
        tree.putClientProperty("JTree.lineStyle", "Angled"); // NOI18N
        setViewportView(tree);

        // Init of the editor
        tree.setCellEditor(new TreeViewCellEditor(tree));
        tree.setEditable(true);

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
    public void requestFocus() {
        tree.requestFocus();
    }

    /** Requests focus for the tree component. Overrides superclass method. */
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
    public void collapseNode(Node n) {
        if (n == null) {
            throw new IllegalArgumentException();
        }

        TreePath treePath = new TreePath(treeModel.getPathToRoot(VisualizerNode.getVisualizer(null, n)));
        tree.collapsePath(treePath);
    }

    /** Expandes the node in the tree.
    *
    * @param n node
    */
    public void expandNode(Node n) {
        if (n == null) {
            throw new IllegalArgumentException();
        }

        lookupExplorerManager();

        TreePath treePath = new TreePath(treeModel.getPathToRoot(VisualizerNode.getVisualizer(null, n)));

        tree.expandPath(treePath);
    }

    /** Test whether a node is expanded in the tree or not
    * @param n the node to test
    * @return true if the node is expanded
    */
    public boolean isExpanded(Node n) {
        TreePath treePath = new TreePath(treeModel.getPathToRoot(VisualizerNode.getVisualizer(null, n)));

        return tree.isExpanded(treePath);
    }

    /** Expands all paths.
    */
    public void expandAll() {
        int i = 0;
        int j /*, k = tree.getRowCount()*/;

        do {
            do {
                j = tree.getRowCount();
                tree.expandRow(i);
            } while (j != tree.getRowCount());

            i++;
        } while (i < tree.getRowCount());
    }

    //
    // Processing functions
    //

    /** Initializes the component and lookup explorer manager.
    */
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

        ArrayList toBeExpaned = new ArrayList(3);

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

            TreePath treePath = new TreePath(treeModel.getPathToRoot(VisualizerNode.getVisualizer(null, nodes[i])));
            paths[i] = treePath;
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
        treeModel.setNode(manager.getRootContext());
    }

    /** Synchronize the explored context from the manager of this Explorer.
    */
    final void synchronizeExploredContext() {
        Node n = manager.getExploredContext();

        if (n != null) {
            TreePath treePath = new TreePath(treeModel.getPathToRoot(VisualizerNode.getVisualizer(null, n)));
            showPath(treePath);
        }
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
        RequestProcessor.getDefault().post(
            new Runnable() {
                public void run() {
                    try {
                        node.getChildren().getNodes(true);
                    } catch (Exception e) {
                        // log a exception
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    } finally {
                        // show normal cursor above all
                        showNormalCursor();
                    }
                }
            }
        );
    }

    /** Synchronize the selected nodes from the manager of this Explorer.
    * The default implementation does nothing.
    */
    final void synchronizeSelectedNodes() {
        // #40152: if there is any scheduled change to view, perform it now
        VisualizerNode.runQueue();

        Node[] arr = manager.getSelectedNodes();
        TreePath[] paths = new TreePath[arr.length];

        for (int i = 0; i < arr.length; i++) {
            TreePath treePath = new TreePath(treeModel.getPathToRoot(VisualizerNode.getVisualizer(null, arr[i])));
            paths[i] = treePath;
        }

        tree.getSelectionModel().removeTreeSelectionListener(managerListener);
        showSelection(paths);
        tree.getSelectionModel().addTreeSelectionListener(managerListener);
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
            Node[] arr = manager.getSelectedNodes();

            if (arr.length == 0) {
                // Should probably not happen when shown from right-click, but may well when from S-F10.
                // Create popup menu for the root node, and make sure it is selected so that action context is correct.
                arr = new Node[] { manager.getRootContext() };

                try {
                    manager.setSelectedNodes(arr);
                } catch (PropertyVetoException e) {
                    assert false : e; // not permitted to be thrown
                }
            }

            Action[] actions = NodeOp.findActions(arr);

            if (actions.length > 0) {
                createPopup(xpos, ypos, Utilities.actionsToPopup(actions, this));
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

    static Action takeAction(Action action, Node node) {
        // bugfix #42843, use ContextAwareAction if possible
        if (action instanceof ContextAwareAction) {
            Lookup contextLookup = node.getLookup();
            Lookup.Result res = contextLookup.lookup(new Lookup.Template(Node.class));

            // #55826, don't added the node twice
            Iterator it = res.allInstances().iterator();

            // temporary workaround #55938
            boolean add = true;

            while (it.hasNext() && add) {
                add = !node.equals(it.next());
            }

            if (add) {
                contextLookup = new ProxyLookup(new Lookup[] { Lookups.singleton(node), node.getLookup() });
            }

            Action contextInstance = ((ContextAwareAction) action).createContextAwareInstance(contextLookup);
            assert contextInstance != action : "Cannot be same. ContextAwareAction:  " + action +
            ", ContextAwareInstance: " + contextInstance;
            action = contextInstance;
        }

        return action;
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

        if (parent.getChildCount() > 0) {
            // get parent path, add child to it
            int childPathLength = parentPaths.length + 1;
            Object[] childPath = new Object[childPathLength];
            System.arraycopy(parentPaths, 0, childPath, 0, parentPaths.length);

            int selectedChild = childIndices[0] - 1;

            if (selectedChild < 0) {
                selectedChild = 0;
            }

            childPath[childPathLength - 1] = parent.getChildAt(selectedChild);
            newSelection = new TreePath(childPath);
        } else {
            // all children removed, select parent
            newSelection = new TreePath(parentPaths);
        }

        return newSelection;
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

        public synchronized void treeExpanded(TreeExpansionEvent ev) {
            
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

                    try {
                        if (tree.isExpanded(path)) {
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

            java.util.List ll = new java.util.ArrayList(paths.length);

            for (int i = 0; i < paths.length; i++) {
                Node n = Visualizer.findNode(paths[i].getLastPathComponent());

                if( isUnderRoot( manager.getRootContext(), n ) ) {
                    ll.add(n);
                }
            }
            callSelectionChanged((Node[]) ll.toArray(new Node[ll.size()]));
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
                // Use the invisible root node as a fake selection, and show its popup.
                try {
                    manager.setSelectedNodes(new Node[] { manager.getRootContext() });
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

            if (nodes.length == 1) {
                Action a = takeAction(nodes[0].getPreferredAction(), nodes[0]);

                if (a != null) {
                    if (a.isEnabled()) {
                        a.actionPerformed(new ActionEvent(nodes[0], ActionEvent.ACTION_PERFORMED, "")); // NOI18N
                    } else {
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }
        }
    }

    private final class ExplorerTree extends JTree implements Autoscroll {
        AutoscrollSupport support;
        private String maxPrefix;
        int SEARCH_FIELD_PREFERRED_SIZE = 160;
        int SEARCH_FIELD_SPACE = 3;
        private boolean firstPaint = true;

        // searchTextField manages focus because it handles VK_TAB key
        private JTextField searchTextField = new JTextField() {
                public boolean isManagingFocus() {
                    return true;
                }

                public void processKeyEvent(KeyEvent ke) {
                    //override the default handling so that
                    //the parent will never receive the escape key and
                    //close a modal dialog
                    if (ke.getKeyCode() == ke.VK_ESCAPE) {
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

            if (Utilities.getOperatingSystem() == Utilities.OS_MAC) {
                getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.META_MASK), "none"); // NOI18N
                getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.META_MASK), "none"); // NOI18N
                getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.META_MASK), "none"); // NOI18N
            }

            setupSearch();
        }

        public void addNotify() {
            super.addNotify();
            ViewTooltips.register(this);
        }
        
        public void removeNotify() {
            super.removeNotify();
            ViewTooltips.unregister(this);
        }

        public void updateUI() {
            super.updateUI();
            setBorder(BorderFactory.createEmptyBorder());
        }

        private void calcRowHeight(Graphics g) {
            int height = g.getFontMetrics(getFont()).getHeight();

            //Issue 42743/"Jesse mode"
            String s = System.getProperty("nb.cellrenderer.fixedheight"); //NOI18N

            if (s != null) {
                try {
                    height = Integer.parseInt(s);
                    setRowHeight(height);
                    firstPaint = false;

                    return;
                } catch (Exception e) {
                    //do nothing
                }
            }

            setRowHeight(Math.max(18, height + 2));
            firstPaint = false;
        }

        //
        // Certain operation should be executed in guarded mode - e.g.
        // not allow changes in nodes during the operation being executed
        //
        public void paint(final Graphics g) {
            new GuardedActions(0, g);
        }

        protected void validateTree() {
            new GuardedActions(1, null);
        }

        public void doLayout() {
            new GuardedActions(2, null);
        }

        private void guardedPaint(Graphics g) {
            if (firstPaint) {
                calcRowHeight(g);

                //This will generate a repaint, so don't bother continuing with super.paint()
                //but do paint the background color so it doesn't paint gray the first time
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());

                return;
            }

            ExplorerTree.super.paint(g);
        }

        private void guardedValidateTree() {
            super.validateTree();
        }

        private void guardedDoLayout() {
            super.doLayout();

            Rectangle visibleRect = getVisibleRect();

            if ((searchpanel != null) && searchpanel.isDisplayable()) {
                int width = Math.min(
                        getPreferredSize().width - (SEARCH_FIELD_SPACE * 2),
                        SEARCH_FIELD_PREFERRED_SIZE - SEARCH_FIELD_SPACE
                    );

                searchpanel.setBounds(
                    Math.max(SEARCH_FIELD_SPACE, (visibleRect.x + visibleRect.width) - width),
                    visibleRect.y + SEARCH_FIELD_SPACE, Math.min(visibleRect.width, width) - SEARCH_FIELD_SPACE,
                    heightOfTextField
                );
            }
        }

        public void setFont(Font f) {
            if (f != getFont()) {
                firstPaint = true;
                super.setFont(f);
            }
        }

        protected void processFocusEvent(FocusEvent fe) {
            super.processFocusEvent(fe);

            //Since the selected when focused is different, we need to force a
            //repaint of the entire selection, but let's do it in guarded more
            //as any other repaint
            new GuardedActions(3, null);
        }

        private void repaintSelection() {
            int first = getSelectionModel().getMinSelectionRow();
            int last = getSelectionModel().getMaxSelectionRow();

            if (first != -1) {
                if (first == last) {
                    Rectangle r = getRowBounds(first);
                    repaint(r.x, r.y, r.width, r.height);
                } else {
                    Rectangle top = getRowBounds(first);
                    Rectangle bottom = getRowBounds(last);
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
                searchpanel.setBorder(BorderFactory.createRaisedBevelBorder());
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
            }
        }

        private void setupSearch() {
            // Remove the default key listeners
            KeyListener[] keyListeners = (KeyListener[]) (getListeners(KeyListener.class));

            for (int i = 0; i < keyListeners.length; i++) {
                removeKeyListener(keyListeners[i]);
            }

            // Add new key listeners
            addKeyListener(
                new KeyAdapter() {
                    public void keyTyped(KeyEvent e) {
                        int modifiers = e.getModifiers();
                        int keyCode = e.getKeyCode();
                        char c = e.getKeyChar();

                        //#43617 - don't eat + and -
                        if ((c == '+') || (c == '-')) return;

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

        private List doSearch(String prefix) {
            List results = new ArrayList();

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

                TreePath path = getNextMatch(prefix, startIndex, Position.Bias.Forward);

                if ((path != null) && !results.contains(path)) {
                    startIndex = tree.getRowForPath(path);
                    results.add(path);

                    String elementName = ((VisualizerNode) path.getLastPathComponent()).getDisplayName();

                    // initialize prefix
                    if (maxPrefix == null) {
                        maxPrefix = elementName;
                    }

                    maxPrefix = findMaxPrefix(maxPrefix, elementName);

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

                // bugfix #28501, avoid the chars duplicated on jdk1.3
                SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run() {
                            searchTextField.requestFocus();
                        }
                    }
                );
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

        public String getToolTipText(MouseEvent event) {
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

        protected TreeModelListener createTreeModelListener() {
            return new ModelHandler();
        }

        public AccessibleContext getAccessibleContext() {
            if (accessibleContext == null) {
                accessibleContext = new AccessibleExplorerTree();
            }

            return accessibleContext;
        }

        private class GuardedActions implements Mutex.Action {
            private int type;
            private Object p1;

            public GuardedActions(int type, Object p1) {
                this.type = type;
                this.p1 = p1;
                Children.MUTEX.readAccess(this);
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
                    repaintSelection();

                    break;

                default:
                    throw new IllegalStateException("type: " + type);
                }

                return null;
            }
        }

        private class SearchFieldListener extends KeyAdapter implements DocumentListener, FocusListener {
            /** The last search results */
            private List results = new ArrayList();

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
                            (selectedNode.getPreferredAction() != null) &&
                                selectedNode.getPreferredAction().isEnabled()
                        ) {
                            selectedNode.getPreferredAction().actionPerformed(
                                new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "")
                            );
                        } else {
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

                    TreePath path = (TreePath) results.get(currentSelectionIndex);
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

            public String getAccessibleName() {
                return TreeView.this.getAccessibleContext().getAccessibleName();
            }

            public String getAccessibleDescription() {
                return TreeView.this.getAccessibleContext().getAccessibleDescription();
            }
        }

        private class ModelHandler extends JTree.TreeModelHandler {
            ModelHandler() {
            }

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
}
