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
package org.openide.explorer.view;

import java.util.logging.Logger;
import org.openide.awt.MouseUtils;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerManager.Provider;
import org.openide.explorer.ExplorerUtils;

import java.awt.*;
import java.awt.event.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;

import javax.accessibility.AccessibleContext;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.plaf.metal.MetalScrollBarUI;
import javax.swing.table.*;
import javax.swing.tree.*;
import org.openide.explorer.view.TreeView.PopupAdapter;
import org.openide.explorer.view.TreeView.PopupSupport;
import org.openide.explorer.view.TreeView.TreePropertyListener;


/** Explorer view. Allows to view tree of nodes on the left
 * and its properties in table on the right.
 * <p>
 * The main mechanism for setting what properties are displayed is
 * <a href="#setProperties"><code>setProperties (Node.Property[])</code></a>.
 * Pass this method an
 * array of properties.  These will act as a template, and properties of
 * the displayed nodes which share the same <i>name</i> will be used in
 * the columns of the table.
 *
 * You can customize behaviour
 * of property columns using <code>Property.setValue (String parameter,
 * Object value)</code>.  For example,
 * assume you have following array of properties:
 * <br><code>org.openide.nodes.Node.Property[] properties</code><br>
 *
 * if you need second column to be initially invisible in TreeTableView, you
 * should set its custom parameter:
 * <br><code>properties[1].setValue ("InvisibleInTreeTableView", Boolean.TRUE);</code>
 *
 * <TABLE border="1" summary="custom parameter list">
 *     <TR>
 *         <TH> Parameter name
 *         </TH>
 *         <TH> Parameter type
 *         </TH>
 *         <TH> Description
 *         </TH>
 *     </TR>
 *     <TR>
 *         <TD> InvisibleInTreeTableView</TD>
 *         <TD> Boolean </TD>
 *         <TD> This property column should be initially invisible (hidden).</TD>
 *     </TR>
 *     <TR>
 *         <TD> ComparableColumnTTV</TD>
 *         <TD> Boolean </TD>
 *         <TD> This property column should be  used for sorting.</TD>
 *     </TR>
 *     <TR>
 *         <TD> SortingColumnTTV</TD>
 *         <TD> Boolean </TD>
 *         <TD> TreeTableView should be initially sorted by this property column.</TD>
 *     </TR>
 *     <TR>
 *         <TD> DescendingOrderTTV</TD>
 *         <TD> Boolean </TD>
 *         <TD> If this parameter and <code>SortingColumnTTV</code> is set, TreeTableView should
 *              be initially sorted by this property columns in descending order.
 *         </TD>
 *     </TR>
 *     <TR>
 *         <TD> OrderNumberTTV</TD>
 *         <TD> Integer </TD>
 *         <TD> If this parameter is set to <code>N</code>, this property column will be
 *             displayed as Nth column of table. If not set, column will be
 *             displayed in natural order.
 *         </TD>
 *     </TR>
 *     <TR>
 *         <TD> TreeColumnTTV</TD>
 *         <TD> Boolean </TD>
 *         <TD> Identifies special property representing first (tree) column. To allow setting
 *             of <code>SortingColumnTTV, DescendingOrderTTV, ComparableColumnTTV</code> parameters
 *             also for first (tree) column, use this special parameter and add
 *             this property to Node.Property[] array before calling
 *             TreeTableView.setProperties (Node.Property[]).
 *         </TD>
 *     </TR>
 *    <TR>
 *        <TD> ColumnMnemonicCharTTV</TD>
 *        <TD> String </TD>
 *        <TD> When set, this parameter contains the mnemonic character for column's
 *            display name (e.g. in <I>Change Visible Columns</I> dialog window).
 *            If not set, no mnemonic will be displayed.
 *        </TD>
 *    </TR>
 * </TABLE>
 *
 * <p>
 * This class is a <q>view</q>
 * to use it properly you need to add it into a component which implements
 * {@link Provider}. Good examples of that can be found 
 * in {@link ExplorerUtils}. Then just use 
 * {@link Provider#getExplorerManager} call to get the {@link ExplorerManager}
 * and control its state.
 * </p>
 * <p>
 * There can be multiple <q>views</q> under one container implementing {@link Provider}. Select from
 * range of predefined ones or write your own:
 * </p>
 * <ul>
 *      <li>{@link org.openide.explorer.view.BeanTreeView} - shows a tree of nodes</li>
 *      <li>{@link org.openide.explorer.view.ContextTreeView} - shows a tree of nodes without leaf nodes</li>
 *      <li>{@link org.openide.explorer.view.ListView} - shows a list of nodes</li>
 *      <li>{@link org.openide.explorer.view.IconView} - shows a rows of nodes with bigger icons</li>
 *      <li>{@link org.openide.explorer.view.ChoiceView} - creates a combo box based on the explored nodes</li>
 *      <li>{@link org.openide.explorer.view.TreeTableView} - shows tree of nodes together with a set of their {@link Property}</li>
 *      <li>{@link org.openide.explorer.view.MenuView} - can create a {@link JMenu} structure based on structure of {@link Node}s</li>
 * </ul>
 * <p>
 * All of these views use {@link ExplorerManager#find} to walk up the AWT hierarchy and locate the
 * {@link ExplorerManager} to use as a controler. They attach as listeners to
 * it and also call its setter methods to update the shared state based on the
 * user action. Not all views make sence together, but for example
 * {@link org.openide.explorer.view.ContextTreeView} and {@link org.openide.explorer.view.ListView} were designed to complement
 * themselves and behaves like windows explorer. The {@link org.openide.explorer.propertysheet.PropertySheetView}
 * for example should be able to work with any other view.
 * </p>
 *
 * @author  jrojcek
 * @since 1.7
 */
public class TreeTableView extends BeanTreeView {
    // icon of column button
    private static final String COLUMNS_ICON = "/org/openide/resources/columns.gif"; // NOI18N

    // icons of ascending/descending order in column header
    private static final String SORT_ASC_ICON = "org/openide/resources/columnsSortedAsc.gif"; // NOI18N
    private static final String SORT_DESC_ICON = "org/openide/resources/columnsSortedDesc.gif"; // NOI18N

    /** The table */
    protected JTable treeTable;
    private NodeTableModel tableModel;

    // Tree scroll support
    private JScrollBar hScrollBar;
    private JScrollPane scrollPane;
    private ScrollListener listener;

    // hiding columns allowed
    private boolean allowHideColumns = false;

    // sorting by column allowed
    private boolean allowSortingByColumn = false;

    // hide horizontal scrollbar
    private boolean hideHScrollBar = false;

    // button in corner of scroll pane
    private JButton colsButton = null;

    // tree model with sorting support
    private SortedNodeTreeModel sortedNodeTreeModel;

    /** Listener on keystroke to invoke default action */
    private ActionListener defaultTreeActionListener;

    // default treetable header renderer
    private TableCellRenderer defaultHeaderRenderer = null;
    private MouseUtils.PopupMouseAdapter tableMouseListener;

    /** Accessible context of this class (implemented by inner class AccessibleTreeTableView). */
    private AccessibleContext accessContext;
    private TreeColumnProperty treeColumnProperty = new TreeColumnProperty();
    private int treeColumnWidth;
    private Component treeTableParent = null;

    /** Create TreeTableView with default NodeTableModel
     */
    public TreeTableView() {
        this(new NodeTableModel());
    }

    /** Creates TreeTableView with provided NodeTableModel.
     * @param ntm node table model
     */
    public TreeTableView(NodeTableModel ntm) {
        tableModel = ntm;

        initializeTreeTable();
        setPopupAllowed(true);
        setDefaultActionAllowed(true);

        initializeTreeScrollSupport();

        // add scrollbar and scrollpane into a panel
        JPanel p = new CompoundScrollPane();
        p.setLayout(new BorderLayout());
        scrollPane.setViewportView(treeTable);
        p.add(BorderLayout.CENTER, scrollPane);

        ImageIcon ic = new ImageIcon(TreeTable.class.getResource(COLUMNS_ICON)); // NOI18N
        colsButton = new javax.swing.JButton(ic);
        colsButton.addActionListener(
            new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    selectVisibleColumns();
                }
            }
        );

        JPanel sbp = new JPanel();
        sbp.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        sbp.add(hScrollBar);
        p.add(BorderLayout.SOUTH, sbp);

        super.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        super.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        setViewportView(p);
        setBorder(BorderFactory.createEmptyBorder()); //NOI18N
        setViewportBorder(BorderFactory.createEmptyBorder()); //NOI18N
    }

    public void setRowHeader(JViewport rowHeader) {
        rowHeader.setBorder(BorderFactory.createEmptyBorder());
        super.setRowHeader(rowHeader);
    }

    /* Overriden to allow hide special horizontal scrollbar
     */
    public void setHorizontalScrollBarPolicy(int policy) {
        hideHScrollBar = (policy == JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        if (hideHScrollBar) {
            hScrollBar.setVisible(false);
            ((TreeTable) treeTable).setTreeHScrollingEnabled(false);
        }
    }

    /* Overriden to delegate policy of vertical scrollbar to inner scrollPane
     */
    public void setVerticalScrollBarPolicy(int policy) {
        if (scrollPane == null) {
            return;
        }

        allowHideColumns = (policy == JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        if (allowHideColumns) {
            scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, colsButton);
        }

        treeTable.getTableHeader().setReorderingAllowed(allowHideColumns);

        scrollPane.setVerticalScrollBarPolicy(policy);
    }

    protected NodeTreeModel createModel() {
        return getSortedNodeTreeModel();
    }

    /** Requests focus for the tree component. Overrides superclass method. */
    public void requestFocus() {
        if (treeTable != null) {
            treeTable.requestFocus();
        }
    }

    public boolean requestFocusInWindow() {
        boolean res = super.requestFocusInWindow();

        //#44856: pass the focus request to the treetable as well 
        if (null != treeTable) {
            treeTable.requestFocus();
        }

        return res;
    }

    /* Sets sorting ability
     */
    private void setAllowSortingByColumn(boolean allow) {
        if (allow && (allow != allowSortingByColumn)) {
            addMouseListener(
                new MouseAdapter() {
                    public void mouseClicked(MouseEvent evt) {
                        // Check whether it was really a click
                        if (evt.getClickCount() == 0) return ;
                        Component c = evt.getComponent();

                        if (c instanceof JTableHeader) {
                            JTableHeader h = (JTableHeader) c;
                            int index = h.columnAtPoint(evt.getPoint());

                            //issue 38442, column can be -1 if this is the
                            //upper right corner - there's no column there,
                            //so make sure it's an index >=0.
                            if (index >= 0) {
                                clickOnColumnAction(index - 1);
                            }
                        }
                    }
                }
            );
        }

        allowSortingByColumn = allow;
    }

    /* Change sorting after clicking on comparable column header.
     * Cycle through ascending -> descending -> no sort -> (start over)
     */
    private void clickOnColumnAction(int index) {
        if (index == -1) {
            if (treeColumnProperty.isComparable()) {
                if (treeColumnProperty.isSortingColumn()) {
                    if (!treeColumnProperty.isSortOrderDescending()) {
                        setSortingOrder(false);
                    } else {
                        noSorting();
                    }
                } else {
                    int realIndex = tableModel.translateVisibleColumnIndex(index);
                    setSortingColumn(index);
                    setSortingOrder(true);
                }
            }
        } else if (tableModel.isComparableColumn(index)) {
            if (tableModel.isSortingColumnEx(tableModel.translateVisibleColumnIndex(index))) {
                if (!tableModel.isSortOrderDescending()) {
                    setSortingOrder(false);
                } else {
                    noSorting();
                }
            } else {
                int realIndex = tableModel.translateVisibleColumnIndex(index);
                setSortingColumn(realIndex);
                setSortingOrder(true);
            }
        }
    }

    private void selectVisibleColumns() {
        setCurrentWidths();

        String viewName = null;

        if (getParent() != null) {
            viewName = getParent().getName();
        }

        if (
            tableModel.selectVisibleColumns(
                    viewName, treeTable.getColumnName(0), getSortedNodeTreeModel().getRootDescription()
                )
        ) {
            if (tableModel.getSortingColumn() == -1) {
                getSortedNodeTreeModel().setSortedByProperty(null);
            }

            setTreePreferredWidth(treeColumnWidth);

            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                setTableColumnPreferredWidth(tableModel.getArrayIndex(i), tableModel.getVisibleColumnWidth(i));
            }
        }
    }

    private void setCurrentWidths() {
        treeColumnWidth = treeTable.getColumnModel().getColumn(0).getWidth();

        for (int i = 0; i < tableModel.getColumnCount(); i++) {
            int w = treeTable.getColumnModel().getColumn(i + 1).getWidth();
            tableModel.setVisibleColumnWidth(i, w);
        }
    }

    /** Do not initialize tree now. We will do it from our constructor.
     * [dafe] Used probably because this method is called *before* superclass
     * is fully created (constructor finished) which is horrible but I don't
     * have enough knowledge about this code to change it.
     */
    void initializeTree() {
    }

    /** Initialize tree and treeTable.
     */
    private void initializeTreeTable() {
        treeModel = createModel();
        treeTable = new TreeTable(treeModel, tableModel);
        tree = ((TreeTable) treeTable).getTree();

        defaultHeaderRenderer = treeTable.getTableHeader().getDefaultRenderer();
        treeTable.getTableHeader().setDefaultRenderer(new SortingHeaderRenderer());

        // init listener & attach it to closing of
        managerListener = new TreePropertyListener();
        tree.addTreeExpansionListener(managerListener);

        // add listener to sort a new expanded folders
        tree.addTreeExpansionListener(
            new TreeExpansionListener() {
                public void treeExpanded(TreeExpansionEvent event) {
                    TreePath path = event.getPath();

                    if (path != null) {
                        // bugfix $32480, store and recover currently expanded subnodes
                        // store expanded paths
                        Enumeration en = TreeTableView.this.tree.getExpandedDescendants(path);

                        // sort children
                        getSortedNodeTreeModel().sortChildren((VisualizerNode) path.getLastPathComponent(), true);

                        // expand again folders
                        while (en.hasMoreElements()) {
                            TreeTableView.this.tree.expandPath((TreePath) en.nextElement());
                        }
                    }
                }

                public void treeCollapsed(TreeExpansionEvent event) {
                    // ignore it
                }
            }
        );

        defaultActionListener = new PopupSupport();
        Action popupWrapper = new AbstractAction() {
                public void actionPerformed(ActionEvent evt) {
                    SwingUtilities.invokeLater( defaultActionListener );
                }

                public boolean isEnabled() {
                    return treeTable.isFocusOwner() || tree.isFocusOwner();
                }
            };
            
        treeTable.getInputMap( JTree.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( 
                KeyStroke.getKeyStroke( KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK ), "org.openide.actions.PopupAction" );
        treeTable.getActionMap().put("org.openide.actions.PopupAction", popupWrapper);
        tree.addMouseListener(defaultActionListener);

        tableMouseListener = new MouseUtils.PopupMouseAdapter() {
                    public void showPopup(MouseEvent mevt) {
                        if (isPopupAllowed()) {
                            if (mevt.getY() > treeTable.getHeight()) {
                                // clear selection, if click under the table
                                treeTable.clearSelection();
                            }

                            createPopup(mevt);
                        }
                    }
                };
        treeTable.addMouseListener(tableMouseListener);

        if (UIManager.getColor("control") != null) { // NOI18N
            treeTable.setGridColor(UIManager.getColor("control")); // NOI18N
        }
    }

    public void setSelectionMode(int mode) {
        super.setSelectionMode(mode);

        if (mode == TreeSelectionModel.SINGLE_TREE_SELECTION) {
            treeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        } else if (mode == TreeSelectionModel.CONTIGUOUS_TREE_SELECTION) {
            treeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        } else if (mode == TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION) {
            treeTable.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        }
    }

    /** Overrides JScrollPane's getAccessibleContext() method to use internal accessible context.
     */
    public AccessibleContext getAccessibleContext() {
        if (accessContext == null) {
            accessContext = new AccessibleTreeTableView();
        }

        return accessContext;
    }

    /** Initialize full support for horizontal scrolling.
     */
    private void initializeTreeScrollSupport() {
        scrollPane = new JScrollPane();
        scrollPane.setName("TreeTableView.scrollpane"); //NOI18N
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());

        if (UIManager.getColor("Table.background") != null) { // NOI18N
            scrollPane.getViewport().setBackground(UIManager.getColor("Table.background")); // NOI18N
        }

        hScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
        hScrollBar.putClientProperty(MetalScrollBarUI.FREE_STANDING_PROP, Boolean.FALSE);
        hScrollBar.setVisible(false);

        listener = new ScrollListener();

        treeTable.addPropertyChangeListener(listener);
        scrollPane.getViewport().addComponentListener(listener);
        tree.addPropertyChangeListener(listener);
        hScrollBar.getModel().addChangeListener(listener);
    }

    /* Overriden to work well with treeTable.
     */
    public void setPopupAllowed(boolean value) {
        if (tree == null) {
            return;
        }

        if ((popupListener == null) && value) {
            // on
            popupListener = new PopupAdapter() {
                        protected void showPopup(MouseEvent e) {
                            int selRow = tree.getClosestRowForLocation(e.getX(), e.getY());

                            if (!tree.isRowSelected(selRow)) {
                                tree.setSelectionRow(selRow);
                            }
                        }
                    };

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

    /* Overriden to work well with treeTable.
     */
    public void setDefaultActionAllowed(boolean value) {
        if (tree == null) {
            return;
        }

        defaultActionEnabled = value;

        if (value) {
            defaultTreeActionListener = new DefaultTreeAction();
            treeTable.registerKeyboardAction(
                defaultTreeActionListener, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), JComponent.WHEN_FOCUSED
            );
        } else {
            // Switch off.
            defaultTreeActionListener = null;
            treeTable.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false));
        }
    }

    /** Set columns.
     * @param props each column is constructed from Node.Property
     */
    public void setProperties(Property[] props) {
        tableModel.setProperties(props);
        treeColumnProperty.setProperty(tableModel.propertyForColumn(-1));

        if (treeColumnProperty.isComparable() || tableModel.existsComparableColumn()) {
            setAllowSortingByColumn(true);

            if (treeColumnProperty.isSortingColumn()) {
                getSortedNodeTreeModel().setSortedByName(true, !treeColumnProperty.isSortOrderDescending());
            } else {
                int index = tableModel.getSortingColumn();

                if (index != -1) {
                    getSortedNodeTreeModel().setSortedByProperty(
                        tableModel.propertyForColumnEx(index), !tableModel.isSortOrderDescending()
                    );
                }
            }
        }
    }

    /** Sets resize mode of table.
     *
     * @param mode - One of 5 legal values: <pre>JTable.AUTO_RESIZE_OFF,
     *                                           JTable.AUTO_RESIZE_NEXT_COLUMN,
     *                                           JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS,
     *                                           JTable.AUTO_RESIZE_LAST_COLUMN,
     *                                           JTable.AUTO_RESIZE_ALL_COLUMNS</pre>
     */
    public final void setTableAutoResizeMode(int mode) {
        treeTable.setAutoResizeMode(mode);
    }

    /** Gets resize mode of table.
     *
     * @return mode - One of 5 legal values: <pre>JTable.AUTO_RESIZE_OFF,
     *                                           JTable.AUTO_RESIZE_NEXT_COLUMN,
     *                                           JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS,
     *                                           JTable.AUTO_RESIZE_LAST_COLUMN,
     *                                           JTable.AUTO_RESIZE_ALL_COLUMNS</pre>
     */
    public final int getTableAutoResizeMode() {
        return treeTable.getAutoResizeMode();
    }

    /** Sets preferred width of table column
     * @param index column index
     * @param width preferred column width
     */
    public final void setTableColumnPreferredWidth(int index, int width) {
        if (index == -1) {
            //Issue 47969 - sometimes this is called with a -1 arg
            return;
        }

        tableModel.setArrayColumnWidth(index, width);

        int j = tableModel.getVisibleIndex(index);

        if (j != -1) {
            treeTable.getColumnModel().getColumn(j + 1).setPreferredWidth(width);
        }
    }

    /** Gets preferred width of table column
     * @param index column index
     * @return preferred column width
     */
    public final int getTableColumnPreferredWidth(int index) {
        int j = tableModel.getVisibleIndex(index);

        if (j != -1) {
            return treeTable.getColumnModel().getColumn(j + 1).getPreferredWidth();
        } else {
            return tableModel.getArrayColumnWidth(index);
        }
    }

    /** Set preferred size of tree view
     * @param width preferred width of tree view
     */
    public final void setTreePreferredWidth(int width) {
        treeTable.getColumnModel().getColumn(((TreeTable) treeTable).getTreeColumnIndex()).setPreferredWidth(width);
    }

    /** Get preferred size of tree view
     * @return preferred width of tree view
     */
    public final int getTreePreferredWidth() {
        return treeTable.getColumnModel().getColumn(((TreeTable) treeTable).getTreeColumnIndex()).getPreferredWidth();
    }

    public void addNotify() {
        // to allow displaying popup also in blank area
        if (treeTable.getParent() != null) {
            treeTableParent = treeTable.getParent();
            treeTableParent.addMouseListener(tableMouseListener);
        }

        super.addNotify();
        listener.revalidateScrollBar();
    }

    public void removeNotify() {
        super.removeNotify();

        if (treeTableParent != null) { //IndexedEditorPanel
            treeTableParent.removeMouseListener(tableMouseListener);
        }

        treeTableParent = null;

        // clear node listeners
        tableModel.setNodes(new Node[] {  });
    }

    public void addMouseListener(MouseListener l) {
        super.addMouseListener(l);
        treeTable.getTableHeader().addMouseListener(l);
    }

    public void removeMouseListener(MouseListener l) {
        super.removeMouseListener(l);
        treeTable.getTableHeader().removeMouseListener(l);
    }

    /* DnD is not implemented for treeTable.
     */
    public void setDragSource(boolean state) {
    }

    /* DnD is not implemented for treeTable.
     */
    public void setDropTarget(boolean state) {
    }

    /* Overriden to get position for popup invoked by keyboard
     */
    Point getPositionForPopup() {
        int row = treeTable.getSelectedRow();

        if (row < 0) {
            return null;
        }

        int col = treeTable.getSelectedColumn();

        if (col < 0) {
            col = 0;
        }

        Rectangle r = null;

        if (col == 0) {
            r = tree.getRowBounds(row);
        } else {
            r = treeTable.getCellRect(row, col, true);
        }

        Point p = SwingUtilities.convertPoint(treeTable, r.x, r.y, this);

        return p;
    }

    private void createPopup(MouseEvent e) {
        Point p = SwingUtilities.convertPoint(e.getComponent(), e.getX(), e.getY(), TreeTableView.this);

        createPopup(p.x, p.y);

        e.consume();
    }

    void createPopup(int xpos, int ypos) {
        int treeXpos = xpos - ((TreeTable) treeTable).getPositionX();

        if (allowHideColumns || allowSortingByColumn) {
            int col = treeTable.getColumnModel().getColumnIndexAtX(treeXpos);
            super.createExtendedPopup(xpos, ypos, getListMenu(col));
        } else {
            super.createPopup(xpos, ypos);
        }
    }

    /* creates List Options menu
     */
    private JMenu getListMenu(final int col) {
        JMenu listItem = new JMenu(NbBundle.getBundle(NodeTableModel.class).getString("LBL_ListOptions"));

        if (allowHideColumns && (col > 0)) {
            JMenu colsItem = new JMenu(NbBundle.getBundle(NodeTableModel.class).getString("LBL_ColsMenu"));

            boolean addColsItem = false;

            if (col > 1) {
                JMenuItem moveLItem = new JMenuItem(NbBundle.getBundle(NodeTableModel.class).getString("LBL_MoveLeft"));
                moveLItem.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                            treeTable.getColumnModel().moveColumn(col, col - 1);
                        }
                    }
                );
                colsItem.add(moveLItem);
                addColsItem = true;
            }

            if (col < tableModel.getColumnCount()) {
                JMenuItem moveRItem = new JMenuItem(
                        NbBundle.getBundle(NodeTableModel.class).getString("LBL_MoveRight")
                    );
                moveRItem.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                            treeTable.getColumnModel().moveColumn(col, col + 1);
                        }
                    }
                );
                colsItem.add(moveRItem);
                addColsItem = true;
            }

            if (addColsItem) {
                listItem.add(colsItem);
            }
        }

        if (allowSortingByColumn) {
            JMenu sortItem = new JMenu(NbBundle.getBundle(NodeTableModel.class).getString("LBL_SortMenu"));
            JRadioButtonMenuItem noSortItem = new JRadioButtonMenuItem(
                    NbBundle.getBundle(NodeTableModel.class).getString("LBL_NoSort"),
                    !getSortedNodeTreeModel().isSortingActive()
                );
            noSortItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                        noSorting();
                    }
                }
            );
            sortItem.add(noSortItem);

            int visibleComparable = 0;
            JRadioButtonMenuItem colItem;

            if (treeColumnProperty.isComparable()) {
                visibleComparable++;
                colItem = new JRadioButtonMenuItem(treeTable.getColumnName(0), treeColumnProperty.isSortingColumn());
                colItem.setHorizontalTextPosition(SwingConstants.LEFT);
                colItem.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                            setSortingColumn(-1);
                        }
                    }
                );
                sortItem.add(colItem);
            }

            for (int i = 0; i < tableModel.getColumnCount(); i++) {
                if (tableModel.isComparableColumn(i)) {
                    visibleComparable++;
                    colItem = new JRadioButtonMenuItem(
                            tableModel.getColumnName(i),
                            tableModel.isSortingColumnEx(tableModel.translateVisibleColumnIndex(i))
                        );
                    colItem.setHorizontalTextPosition(SwingConstants.LEFT);

                    final int index = tableModel.translateVisibleColumnIndex(i);
                    colItem.addActionListener(
                        new ActionListener() {
                            public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                                setSortingColumn(index);
                            }
                        }
                    );
                    sortItem.add(colItem);
                }
            }

            //add invisible columns
            for (int i = 0; i < tableModel.getColumnCountEx(); i++) {
                if (tableModel.isComparableColumnEx(i) && !tableModel.isVisibleColumnEx(i)) {
                    visibleComparable++;
                    colItem = new JRadioButtonMenuItem(tableModel.getColumnNameEx(i), tableModel.isSortingColumnEx(i));
                    colItem.setHorizontalTextPosition(SwingConstants.LEFT);

                    final int index = i;
                    colItem.addActionListener(
                        new ActionListener() {
                            public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                                setSortingColumn(index);
                            }
                        }
                    );
                    sortItem.add(colItem);
                }
            }

            if (visibleComparable > 0) {
                sortItem.addSeparator();

                boolean current_sort;

                if (treeColumnProperty.isSortingColumn()) {
                    current_sort = treeColumnProperty.isSortOrderDescending();
                } else {
                    current_sort = tableModel.isSortOrderDescending();
                }

                JRadioButtonMenuItem ascItem = new JRadioButtonMenuItem(
                        NbBundle.getBundle(NodeTableModel.class).getString("LBL_Ascending"), !current_sort
                    );
                ascItem.setHorizontalTextPosition(SwingConstants.LEFT);
                ascItem.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                            setSortingOrder(true);
                        }
                    }
                );
                sortItem.add(ascItem);

                JRadioButtonMenuItem descItem = new JRadioButtonMenuItem(
                        NbBundle.getBundle(NodeTableModel.class).getString("LBL_Descending"), current_sort
                    );
                descItem.setHorizontalTextPosition(SwingConstants.LEFT);
                descItem.addActionListener(
                    new ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                            setSortingOrder(false);
                        }
                    }
                );
                sortItem.add(descItem);

                if (!getSortedNodeTreeModel().isSortingActive()) {
                    ascItem.setEnabled(false);
                    descItem.setEnabled(false);
                }

                listItem.add(sortItem);
            }
        }

        if (allowHideColumns) {
            JMenuItem visItem = new JMenuItem(NbBundle.getBundle(NodeTableModel.class).getString("LBL_ChangeColumns"));
            visItem.addActionListener(
                new ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent actionEvent) {
                        selectVisibleColumns();
                    }
                }
            );

            listItem.add(visItem);
        }

        return listItem;
    }

    /* Sets column to be currently used for sorting
     */
    private void setSortingColumn(int index) {
        tableModel.setSortingColumnEx(index);

        if (index != -1) {
            getSortedNodeTreeModel().setSortedByProperty(
                tableModel.propertyForColumnEx(index), !tableModel.isSortOrderDescending()
            );
            treeColumnProperty.setSortingColumn(false);
        } else {
            getSortedNodeTreeModel().setSortedByName(true, !treeColumnProperty.isSortOrderDescending());
            treeColumnProperty.setSortingColumn(true);
        }

        // to change sort icon
        treeTable.getTableHeader().repaint();
    }

    private void noSorting() {
        tableModel.setSortingColumnEx(-1);
        getSortedNodeTreeModel().setNoSorting();
        treeColumnProperty.setSortingColumn(false);

        // to change sort icon
        treeTable.getTableHeader().repaint();
    }

    /* Sets sorting order for current sorting.
     */
    private void setSortingOrder(boolean ascending) {
        if (treeColumnProperty.isSortingColumn()) {
            treeColumnProperty.setSortOrderDescending(!ascending);
        } else {
            tableModel.setSortOrderDescending(!ascending);
        }

        getSortedNodeTreeModel().setSortOrder(ascending);

        // to change sort icon
        treeTable.getTableHeader().repaint();
    }

    private synchronized SortedNodeTreeModel getSortedNodeTreeModel() {
        if (sortedNodeTreeModel == null) {
            sortedNodeTreeModel = new SortedNodeTreeModel();
        }

        return sortedNodeTreeModel;
    }

    /** This is internal accessible context for TreeTableView.
     * It delegates setAccessibleName and setAccessibleDescription methods to set these properties
     * in underlying TreeTable as well.
     */
    private class AccessibleTreeTableView extends AccessibleJScrollPane {
        AccessibleTreeTableView() {
        }

        public void setAccessibleName(String accessibleName) {
            super.setAccessibleName(accessibleName);

            if (treeTable != null) {
                treeTable.getAccessibleContext().setAccessibleName(accessibleName);
            }
        }

        public void setAccessibleDescription(String accessibleDescription) {
            super.setAccessibleDescription(accessibleDescription);

            if (treeTable != null) {
                treeTable.getAccessibleContext().setAccessibleDescription(accessibleDescription);
            }
        }
    }

    /* Horizontal scrolling support.
     */
    private final class ScrollListener extends ComponentAdapter implements PropertyChangeListener, ChangeListener {
        boolean movecorrection = false;

        ScrollListener() {
        }

        //Column width
        public void propertyChange(PropertyChangeEvent evt) {
            if (((TreeTable) treeTable).getTreeColumnIndex() == -1) {
                return;
            }

            if ("width".equals(evt.getPropertyName())) { // NOI18N

                if (!treeTable.equals(evt.getSource())) {
                    Dimension dim = hScrollBar.getPreferredSize();
                    dim.width = treeTable.getColumnModel().getColumn(((TreeTable) treeTable).getTreeColumnIndex())
                                         .getWidth();
                    hScrollBar.setPreferredSize(dim);
                    hScrollBar.revalidate();
                    hScrollBar.repaint();
                }

                revalidateScrollBar();
            } else if ("positionX".equals(evt.getPropertyName())) { // NOI18N
                revalidateScrollBar();
            } else if ("treeColumnIndex".equals(evt.getPropertyName())) { // NOI18N
                treeTable.getColumnModel().getColumn(((TreeTable) treeTable).getTreeColumnIndex())
                         .addPropertyChangeListener(listener);
            } else if ("column_moved".equals(evt.getPropertyName())) { // NOI18N

                int from = ((Integer) evt.getOldValue()).intValue();
                int to = ((Integer) evt.getNewValue()).intValue();

                if ((from == 0) || (to == 0)) {
                    if (movecorrection) {
                        movecorrection = false;
                    } else {
                        movecorrection = true;

                        // not allowed to move first, tree column
                        treeTable.getColumnModel().moveColumn(to, from);
                    }

                    return;
                }

                // module will be revalidated in NodeTableModel
                treeTable.getTableHeader().getColumnModel().getColumn(from).setModelIndex(from);
                treeTable.getTableHeader().getColumnModel().getColumn(to).setModelIndex(to);
                tableModel.moveColumn(from - 1, to - 1);
            }
        }

        //Viewport height
        public void componentResized(ComponentEvent e) {
            revalidateScrollBar();
        }

        //ScrollBar change
        public void stateChanged(ChangeEvent evt) {
            int value = hScrollBar.getModel().getValue();
            ((TreeTable) treeTable).setPositionX(value);
        }

        private void revalidateScrollBar() {
            if (!isDisplayable()) {
                return;
            }

            if (
                (treeTable.getColumnModel().getColumnCount() > 0) &&
                    (((TreeTable) treeTable).getTreeColumnIndex() >= 0)
            ) {
                int extentWidth = treeTable.getColumnModel().getColumn(((TreeTable) treeTable).getTreeColumnIndex())
                                           .getWidth();
                int maxWidth = tree.getPreferredSize().width;
                int extentHeight = scrollPane.getViewport().getSize().height;
                int maxHeight = tree.getPreferredSize().height;
                int positionX = ((TreeTable) treeTable).getPositionX();

                int value = Math.max(0, Math.min(positionX, maxWidth - extentWidth));

                boolean hsbvisible = hScrollBar.isVisible();
                boolean vsbvisible = scrollPane.getVerticalScrollBar().isVisible();
                int hsbheight = hsbvisible ? hScrollBar.getHeight() : 0;
                int vsbwidth = scrollPane.getVerticalScrollBar().getWidth();

                hScrollBar.setValues(value, extentWidth, 0, maxWidth);

                if (
                    hideHScrollBar || (maxWidth <= extentWidth) ||
                        (vsbvisible &&
                        ((maxHeight <= (extentHeight + hsbheight)) && (maxWidth <= (extentWidth + vsbwidth))))
                ) {
                    hScrollBar.setVisible(false);
                } else {
                    hScrollBar.setVisible(true);
                }
            }
        }
    }

    /** Scrollable (better say not scrollable) pane. Used as container for
     * left (controlling) and rigth (controlled) scroll panes.
     */
    private static final class CompoundScrollPane extends JPanel implements Scrollable {
        CompoundScrollPane() {
        }

        public void setBorder(Border b) {
            //do nothing
        }

        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 10;
        }

        public boolean getScrollableTracksViewportHeight() {
            return true;
        }

        public Dimension getPreferredScrollableViewportSize() {
            return this.getPreferredSize();
        }

        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 10;
        }
    }

    /** Invokes default action.
     */
    private class DefaultTreeAction implements ActionListener {
        DefaultTreeAction() {
        }

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            if (treeTable.getSelectedColumn() != ((TreeTable) treeTable).getTreeColumnIndex()) {
                return;
            }

            Node[] nodes = manager.getSelectedNodes();

            if (nodes.length == 1) {
                Action a = nodes[0].getPreferredAction();

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

    /* node tree model with added sorting support
     */
    private class SortedNodeTreeModel extends NodeTreeModel {
        private Node.Property sortedByProperty;
        private boolean sortAscending = true;
        private Comparator<VisualizerNode> rowComparator;
        private boolean sortedByName = false;
        private SortingTask sortingTask = null;

        SortedNodeTreeModel() {
        }

        void setNoSorting() {
            setSortedByProperty(null);
            setSortedByName(false);
            sortingChanged();
        }

        boolean isSortingActive() {
            return ((sortedByProperty != null) || sortedByName);
        }

        void setSortedByProperty(Node.Property prop) {
            if (sortedByProperty == prop) {
                return;
            }

            sortedByProperty = prop;

            if (prop == null) {
                rowComparator = null;
            } else {
                sortedByName = false;
            }

            sortingChanged();
        }

        void setSortedByProperty(Node.Property prop, boolean ascending) {
            if ((sortedByProperty == prop) && (ascending == sortAscending)) {
                return;
            }

            sortedByProperty = prop;
            sortAscending = ascending;

            if (prop == null) {
                rowComparator = null;
            } else {
                sortedByName = false;
            }

            sortingChanged();
        }

        void setSortedByName(boolean sorted, boolean ascending) {
            if ((sortedByName == sorted) && (ascending == sortAscending)) {
                return;
            }

            sortedByName = sorted;
            sortAscending = ascending;

            if (sortedByName) {
                sortedByProperty = null;
            }

            sortingChanged();
        }

        void setSortedByName(boolean sorted) {
            sortedByName = sorted;

            if (sortedByName) {
                sortedByProperty = null;
            }

            sortingChanged();
        }

        void setSortOrder(boolean ascending) {
            if (ascending == sortAscending) {
                return;
            }

            sortAscending = ascending;
            sortingChanged();
        }

        private Node.Property getNodeProperty(Node node, Node.Property prop) {
            Node.PropertySet[] propsets = node.getPropertySets();

            for (int i = 0, n = propsets.length; i < n; i++) {
                Node.Property[] props = propsets[i].getProperties();

                for (int j = 0, m = props.length; j < m; j++) {
                    if (props[j].equals(prop)) {
                        return props[j];
                    }
                }
            }

            return null;
        }

        synchronized Comparator<VisualizerNode> getRowComparator() {
            if (rowComparator == null) {
                rowComparator = new Comparator<VisualizerNode>() {

                    public int compare(VisualizerNode o1, VisualizerNode o2) {
                        if (o1 == o2) {
                            return 0;
                        }
                        Node n1 = o1.node;
                        Node n2 = o2.node;

                        if ((n1 == null) && (n2 == null)) {
                            return 0;
                        }
                        if (n1 == null) {
                            return 1;
                        }
                        if (n2 == null) {
                            return -1;
                        }
                        if ((n1.getParentNode() == null) ||
                            (n2.getParentNode() == null)) {
                            // PENDING: throw Exception
                            Logger.getAnonymousLogger().warning("TTV.compare: Node " +
                                                                n1 + " or " + n2 +
                                                                " has no parent!");
                            return 0;
                        }
                        if (!(n1.getParentNode().equals(n2.getParentNode()))) {
                            // PENDING: throw Exception
                            Logger.getAnonymousLogger().warning("TTV.compare: Nodes " +
                                                                n1 + " and " +
                                                                n2 +
                                                                " has different parent!");
                            return 0;
                        }
                        int res = 0;

                        if (sortedByName) {
                            res = n1.getDisplayName().compareTo(n2.getDisplayName());
                            return sortAscending ? res
                                                 : (-res);
                        }
                        Property p1 = getNodeProperty(n1, sortedByProperty);
                        Property p2 = getNodeProperty(n2, sortedByProperty);

                        if ((p1 == null) && (p2 == null)) {
                            return 0;
                        }
                        try {
                            if (p1 == null) {
                                res = -1;
                            } else if (p2 == null) {
                                res = 1;
                            } else {
                                Object v1 = p1.getValue();
                                Object v2 = p2.getValue();

                                if ((v1 == null) && (v2 == null)) {
                                    return 0;
                                } else if (v1 == null) {
                                    res = -1;
                                } else if (v2 == null) {
                                    res = 1;
                                } else {
                                    if ((v1.getClass() != v2.getClass()) ||
                                        !(v1 instanceof Comparable)) {
                                        v1 = v1.toString();
                                        v2 = v2.toString();
                                    }
                                    res = ((Comparable) v1).compareTo(v2);
                                }
                            }
                            return sortAscending ? res
                                                 : (-res);
                        }
                        catch (Exception ex) {
                            Logger.global.log(Level.WARNING, null, ex);
                            return 0;
                        }
                    }
                };
            }

            return rowComparator;
        }

        void sortChildren(VisualizerNode parent, boolean synchronous) {
            //#37802 - resorts are processed too aggressively, causing 
            //NPEs.  Except for user-invoked actions (clicking the column
            //header, etc.), we will defer them to run later on the EQ, so
            //the change in the node has a chance to be fully processed
            if (synchronous) {
                synchronized (this) {
                    if (sortingTask != null) {
                        sortingTask.remove(parent);

                        if (sortingTask.isEmpty()) {
                            sortingTask = null;
                        }
                    }
                }

                doSortChildren(parent);
            } else {
                synchronized (this) {
                    if (sortingTask == null) {
                        sortingTask = new SortingTask();
                        SwingUtilities.invokeLater(sortingTask);
                    }
                }

                sortingTask.add(parent);
            }
        }

        void doSortChildren(VisualizerNode parent) {
            if (isSortingActive()) {
                final Comparator<VisualizerNode> comparator = getRowComparator();

                if ((comparator != null) || (parent != null)) {
                    parent.reorderChildren(comparator);
                }
            } else {
                parent.naturalOrder();
            }
        }

        void sortingChanged() {
            // PENDING: remember the last sorting to avoid multiple sorting
            // remenber expanded folders
            TreeNode tn = (TreeNode) (this.getRoot());
            java.util.List<TreePath> list = new ArrayList<TreePath>();
            Enumeration<TreePath> en = TreeTableView.this.tree.getExpandedDescendants(new TreePath(tn));

            while ((en != null) && en.hasMoreElements()) {
                TreePath path = en.nextElement();

                // bugfix #32328, don't sort whole subtree but only expanded folders
                sortChildren((VisualizerNode) path.getLastPathComponent(), true);
                list.add(path);
            }

            // expand again folders
            for (int i = 0; i < list.size(); i++) {
                TreeTableView.this.tree.expandPath(list.get(i));
            }
        }

        String getRootDescription() {
            if (getRoot() instanceof VisualizerNode) {
                //#37802 commenting this out - unfathomable why you would need
                //to sort the root's children in order to get its short 
                //description - Tim
                //                sortChildren ((VisualizerNode)getRoot ());
                return ((VisualizerNode) getRoot()).getShortDescription();
            }

            return ""; // NOI18N
        }

        // overrided mothod from DefaultTreeModel
        public void nodesWereInserted(TreeNode node, int[] childIndices) {
            super.nodesWereInserted(node, childIndices);

            if (node instanceof VisualizerNode && isSortingActive()) {
                sortChildren((VisualizerNode) node, false);
            }
        }

        // overrided mothod from DefaultTreeModel
        public void nodesChanged(TreeNode node, int[] childIndices) {
            super.nodesChanged(node, childIndices);

            if ((node != null) && (childIndices != null) && isSortingActive()) {
                sortChildren((VisualizerNode) node, false);
            }
        }

        // overrided mothod from DefaultTreeModel
        public void setRoot(TreeNode root) {
            super.setRoot(root);

            if (root instanceof VisualizerNode && isSortingActive()) {
                sortChildren((VisualizerNode) root, false);
            }
        }

        private class SortingTask implements Runnable {
            private HashSet<VisualizerNode> toSort = new HashSet<VisualizerNode>();

            public synchronized void add(VisualizerNode parent) {
                toSort.add(parent);
            }

            public synchronized void remove(VisualizerNode parent) {
                toSort.remove(parent);
            }

            public synchronized boolean isEmpty() {
                return toSort.isEmpty();
            }

            public void run() {
                synchronized (SortedNodeTreeModel.this) {
                    SortedNodeTreeModel.this.sortingTask = null;
                }

                for (Iterator<VisualizerNode> i = toSort.iterator(); i.hasNext();) {
                    VisualizerNode curr = i.next();
                    SortedNodeTreeModel.this.doSortChildren(curr);
                }
            }
        }
    }

    /* Cell renderer for sorting column header.
     */
    private class SortingHeaderRenderer extends DefaultTableCellRenderer {
        SortingHeaderRenderer() {
        }

        /** Overrides superclass method. */
        public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
        ) {
            Component comp = defaultHeaderRenderer.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
                );

            if (comp instanceof JLabel) {
                if ((column == 0) && treeColumnProperty.isSortingColumn()) {
                    ((JLabel) comp).setIcon(getProperIcon(treeColumnProperty.isSortOrderDescending()));
                    ((JLabel) comp).setHorizontalTextPosition(SwingConstants.LEFT);

                    // don't use deriveFont() - see #49973 for details
                    comp.setFont(new Font(comp.getFont().getName(), Font.BOLD, comp.getFont().getSize()));
                } else if ((column != 0) && ((tableModel.getVisibleSortingColumn() + 1) == column)) {
                    ((JLabel) comp).setIcon(getProperIcon(tableModel.isSortOrderDescending()));
                    ((JLabel) comp).setHorizontalTextPosition(SwingConstants.LEFT);

                    // don't use deriveFont() - see #49973 for details
                    comp.setFont(new Font(comp.getFont().getName(), Font.BOLD, comp.getFont().getSize()));
                } else {
                    ((JLabel) comp).setIcon(null);
                }
            }

            return comp;
        }

        private ImageIcon getProperIcon(boolean descending) {
            if (descending) {
                return new ImageIcon(org.openide.util.Utilities.loadImage(SORT_DESC_ICON));
            } else {
                return new ImageIcon(org.openide.util.Utilities.loadImage(SORT_ASC_ICON));
            }
        }
    }
     // End of inner class SortingHeaderRenderer.

    private static class TreeColumnProperty {
        private Property p = null;

        TreeColumnProperty() {
        }

        void setProperty(Property p) {
            this.p = p;
        }

        boolean isComparable() {
            if (p == null) {
                return false;
            }

            Object o = p.getValue(NodeTableModel.ATTR_COMPARABLE_COLUMN);

            if ((o != null) && o instanceof Boolean) {
                return ((Boolean) o).booleanValue();
            }

            return false;
        }

        boolean isSortingColumn() {
            if (p == null) {
                return false;
            }

            Object o = p.getValue(NodeTableModel.ATTR_SORTING_COLUMN);

            if ((o != null) && o instanceof Boolean) {
                return ((Boolean) o).booleanValue();
            }

            return false;
        }

        void setSortingColumn(boolean sorting) {
            if (p == null) {
                return;
            }

            p.setValue(NodeTableModel.ATTR_SORTING_COLUMN, sorting ? Boolean.TRUE : Boolean.FALSE);
        }

        boolean isSortOrderDescending() {
            if (p == null) {
                return false;
            }

            Object o = p.getValue(NodeTableModel.ATTR_DESCENDING_ORDER);

            if ((o != null) && o instanceof Boolean) {
                return ((Boolean) o).booleanValue();
            }

            return false;
        }

        void setSortOrderDescending(boolean descending) {
            if (p == null) {
                return;
            }

            p.setValue(NodeTableModel.ATTR_DESCENDING_ORDER, descending ? Boolean.TRUE : Boolean.FALSE);
        }
    }

    /* For testing - use internal execution
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Node n = //new org.netbeans.core.ModuleNode();
                    RepositoryNodeFactory.getDefault().repository(DataFilter.ALL);

                org.openide.explorer.ExplorerManager em = new org.openide.explorer.ExplorerManager();
                em.setRootContext(n);

                org.openide.explorer.ExplorerPanel ep = new org.openide.explorer.ExplorerPanel(em);
                ep.setLayout (new BorderLayout ());
                ep.setBorder(new EmptyBorder(20, 20, 20, 20));

                TreeTableView ttv = new TreeTableView();
                ttv.setRootVisible(false);
                ttv.setPopupAllowed(true);
                ttv.setDefaultActionAllowed(true);
                ttv.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS );
                ttv.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );

                org.openide.nodes.PropertySupport.ReadOnly prop2
                    = new org.openide.nodes.PropertySupport.ReadOnly (
                            "name", // NOI18N
                            String.class,
                            "name",
                            "Name Tooltip"
                        ) {
                            public Object getValue () {
                                return null;
                            }

                        };
                //prop2.setValue( "InvisibleInTreeTableView", Boolean.TRUE );
                prop2.setValue( "SortingColumnTTV", Boolean.TRUE );
                prop2.setValue( "DescendingOrderTTV", Boolean.TRUE );
                prop2.setValue( "ComparableColumnTTV", Boolean.TRUE );

                ttv.setProperties(
    //                    n.getChildren().getNodes()[0].getPropertySets()[0].getProperties());
                    new Property[]{
                        new org.openide.nodes.PropertySupport.ReadWrite (
                            "hidden", // NOI18N
                            Boolean.TYPE,
                            "hidden",
                            "Hidden tooltip"
                        ) {
                            public Object getValue () {
                                return null;
                            }

                            public void setValue (Object o) {
                            }
                        },
                        prop2,
                        new org.openide.nodes.PropertySupport.ReadOnly (
                            "template", // NOI18N
                            Boolean.TYPE,
                            "template",
                            "Template Tooltip"
                        ) {
                            public Object getValue () {
                                return null;
                            }

                        }

                    }
                );
                ttv.setTreePreferredWidth(200);

                ttv.setTableColumnPreferredWidth(0, 60);
                ttv.setTableColumnPreferredWidth(1, 150);
                ttv.setTableColumnPreferredWidth(2, 100);

                ep.add("Center", ttv);
                ep.open();
            }
        });
    }
    */
}
