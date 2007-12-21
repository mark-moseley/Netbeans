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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.soa.mappercore;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.event.MapperSelectionEvent;
import org.netbeans.modules.soa.mappercore.event.MapperSelectionListener;
import org.netbeans.modules.soa.mappercore.model.MapperModel;
import org.netbeans.modules.soa.mappercore.utils.Utils;
import org.netbeans.modules.soa.mappercore.graphics.VerticalGradient;
import org.netbeans.modules.soa.mappercore.graphics.XRange;
import org.netbeans.modules.soa.mappercore.model.Graph;

/**
 *
 * @author anjeleevich
 */
public class Mapper extends JPanel {

    private MapperModel model;
    private MapperNode root;
    private TreeModelListener treeModelListener = new TreeModelListenerImpl();
    private MapperSelectionListener selectionListener;
    private int leftDividerPosition = -1;
    private int rightDividerPosition = -1;
    private JPanel leftDivider;
    private JPanel rightDivider;
    private LeftTree leftTree;
    private RightTree rightTree;
    private Canvas canvas;
    // L&F
    private int leftChildIndent;
    private int rightChildIndent;
    private Icon openIcon;
    private Icon closedIcon;
    private Icon leafIcon;
    private Icon expandedIcon;
    private Icon collapsedIcon;
    private Color treeLineColor;
    private Dimension preferredTreeSize = null;
    private XRange graphXRange = null;
    private boolean validNodes = false;
    private boolean repaintSceduled = false;
    private MapperContext context = new DefaultMapperContext();
    private LinkTool linkTool;
    private MoveTool moveTool;
    private EventListenerList listenersList = new EventListenerList();
    private SelectionModel selectionModel;
    private TreePath pathDndselect = null;

    /** Creates a new instance of RightTree */
    public Mapper(MapperModel model) {
        setLayout(new MapperLayout());

        selectionModel = new SelectionModel(this);

        leftTree = new LeftTree(this);
        rightTree = new RightTree(this);
        canvas = new Canvas(this);

        leftDivider = new MapperDivider();
        leftDivider.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));

        rightDivider = new MapperDivider();
        rightDivider.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));

        new MapperDividersController(this, leftDivider, rightDivider);

        add(leftTree.getView(), MapperLayout.LEFT_SCROLL);
        add(leftDivider, MapperLayout.LEFT_DIVIDER);
        add(canvas.getView(), MapperLayout.CENTER_SCROLL);
        add(rightDivider, MapperLayout.RIGHT_DIVIDER);
        add(rightTree.getView(), MapperLayout.RIGHT_SCROLL);

        new ScrollPaneYSyncronizer(canvas.getScrollPane(),
                rightTree.getScrollPane());

        linkTool = new LinkTool(this);
        moveTool = new MoveTool(this);

        setModel(model);

        selectionModel.addSelectionListener(new MapperSelectionListener() {

            public void mapperSelectionChanged(MapperSelectionEvent event) {
                repaint();
            }
        });
        
        InputMap iMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap aMap = getActionMap();
        
        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK),
                "mapper-select-all-action");
        aMap.put("mapper-select-all-action", new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                SelectionModel selectionModel = getSelectionModel();
                TreePath selectedPath = selectionModel.getSelectedPath();
                if (selectedPath != null) {
                    selectionModel.selectAll(selectedPath);
                }
            }
        });
    }

    public void addRightTreeExpansionListener(TreeExpansionListener listener) {
        listenersList.add(TreeExpansionListener.class, listener);
    }

    public void removeRightTreeExpansionListener(TreeExpansionListener listener) {
        listenersList.remove(TreeExpansionListener.class, listener);
    }

    public void addSelectionListener(MapperSelectionListener listener) {
        getSelectionModel().addSelectionListener(listener);
    }

    public void removeSelectionListener(MapperSelectionListener listener) {
        getSelectionModel().removeSelectionListener(listener);
    }
    
    public TreePath getSelectedDndPath() {
        return pathDndselect;
    }

    public SelectionModel getSelectionModel() {
        return selectionModel;
    }

    LinkTool getLinkTool() {
        return linkTool;
    }

    MoveTool getMoveTool() {
        return moveTool;
    }

    public MapperContext getContext() {
        return context;
    }
    
    public void setSelectedDndPath(TreePath path) {
        pathDndselect = path;
    }

    public void setContext(MapperContext context) {
        if (context == null) {
            context = new DefaultMapperContext();
        }

        if (this.context != context) {
            this.context = context;

            TreeCellRenderer oldCellRenderer = leftTree.getCellRenderer();
            leftTree.setCellRenderer(new DefaultTreeCellRenderer());
            leftTree.setCellRenderer(oldCellRenderer);
            leftTree.revalidate();
            leftTree.repaint();

            MapperNode root = getRoot();

            if (root != null) {
                root.invalidateTree();
                root.repaint();
            }
        }
    }

    public TreePath getSelected() {
        return getSelectionModel().getSelectedPath();
    }

    public TreePath getSelectedPath() {
        return getSelectionModel().getSelectedPath();
    }

    public void setSelected(TreePath treePath) {
        getSelectionModel().setSelected(treePath);
    }

    void resetRepaintSceduled() {
        repaintSceduled = false;
    }

    void setSelectedNode(MapperNode selectedNode) {
        setSelected(selectedNode.getTreePath());
    }

    public void setExpandedState(TreePath treePath, boolean state) {
        if (state) {
            MapperNode node = getNode(treePath, true);
            TreePath expandedTreePath = null;
            while (node != null) {
                if (!node.isLeaf() && node.isCollapsed()) {
                    node.setExpanded(true);
                    if (expandedTreePath == null) {
                        expandedTreePath = node.getTreePath();
                    }
                }
                node = node.getParent();
            }

            if (expandedTreePath != null) {
                fireNodeExpanded(expandedTreePath);
            }
        } else {
            MapperNode node = getNode(treePath, false);
            if (node != null && !node.isLeaf() && node.isExpanded()) {
                node.setCollapsed(true);
                fireNodeCollapsed(treePath);
            }
        }
    }

    public void setExpandedGraphState(TreePath treePath, boolean state) {
        if (model == null) {
            return;
        }

        Graph graph = model.getGraph(treePath);

        if (graph == null || graph.isEmpty()) {
            return;
        }

        if (state) {
            MapperNode node = getNode(treePath, true);
            node.setGraphExpanded(true);
        } else {
            MapperNode node = getNode(treePath, false);
            if (node != null) {
                node.setGraphExpanded(false);
            }
        }
    }

    private void fireNodeCollapsed(TreePath treePath) {
        if (treePath == null) {
            return;
        }
        TreeExpansionListener[] listeners = listenersList.getListeners(TreeExpansionListener.class);
        if (listeners != null && listeners.length > 0) {
            TreeExpansionEvent event = new TreeExpansionEvent(this, treePath);
            for (TreeExpansionListener l : listeners) {
                l.treeCollapsed(event);
            }
        }
    }

    private void fireNodeExpanded(TreePath treePath) {
        if (treePath == null) {
            return;
        }
        TreeExpansionListener[] listeners = listenersList.getListeners(TreeExpansionListener.class);
        if (listeners != null && listeners.length > 0) {
            TreeExpansionEvent event = new TreeExpansionEvent(this, treePath);
            for (TreeExpansionListener l : listeners) {
                l.treeExpanded(event);
            }
        }
    }

    void collapseNode(MapperNode node) {
        setExpandedState(node.getTreePath(), false);
    }

    void expandNode(MapperNode node) {
        setExpandedState(node.getTreePath(), true);
    }

    void switchCollapsedExpandedState(MapperNode node) {
        if (node.isLeaf()) {
            return;
        }
        if (node.isExpanded()) {
            collapseNode(node);
        } else {
            expandNode(node);
        }
    }

    public int getLeftDividerPosition() {
        return leftDividerPosition;
    }

    public int getRightDividerPosition() {
        return rightDividerPosition;
    }

    void setDividerPositions(
            int leftDividerPosition,
            int rightDividerPosition) {
        this.leftDividerPosition = leftDividerPosition;
        this.rightDividerPosition = rightDividerPosition;
    }

    public void setModel(MapperModel model) {
        MapperModel oldModel = this.model;

        TreeModel oldLeftTreeModel = (oldModel != null) ? leftTree.getModel() : null;
        TreeModel newLeftTreeModel = (model != null) ? model.getLeftTreeModel() : null;

        if (oldModel != model) {
            this.model = model;

            if (oldLeftTreeModel != newLeftTreeModel) {
                leftTree.setModel(newLeftTreeModel);
            }
            
            if (oldModel != null) {
                oldModel.removeTreeModelListener(treeModelListener);
            }

            if (model != null) {
                model.addTreeModelListener(treeModelListener);
                root = new MapperNode(this, null, model.getRoot());
//                root.getChildCount();
            } else {
                root = null;
            }

            invalidateNodes();
            repaintNodes();

            firePropertyChange(MODEL_PROPERTY, oldModel, model);
        }
    }

    public MapperModel getModel() {
        return model;
    }

    MapperNode getRoot() {
        return root;
    }

    public LeftTree getLeftTree() {
        return leftTree;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public RightTree getRightTree() {
        return rightTree;
    }
    
    
    public void expandNonEmptyGraphs() {
        expandGraphs(Utils.getNonEmptyGraphs(getModel()));
    }
    
    
    public void expandGraphs(List<TreePath> treePathes) {
        if (treePathes == null) {
            return;
        }

        Set<TreePath> parentTreePathes = new HashSet<TreePath>();
        for (TreePath treePath : treePathes) {
            TreePath parentTreePath = treePath.getParentPath();
            if (parentTreePath != null) {
                parentTreePathes.add(parentTreePath);
            }
        }

        for (TreePath parentTreePath : parentTreePathes) {
            setExpandedState(parentTreePath, true);
        }

        for (TreePath treePath : treePathes) {
            setExpandedGraphState(treePath, true);
        }
    }

    
    public void hideOtherPathes(int expandedLevel) {
        if (model == null) return;
        if (root == null) return;
        
        TreePath selectedPath = getSelectedPath();
        if (selectedPath == null) return;
        
        collapseAll(root, 0, expandedLevel, selectedPath);
    }
    

    public void collapseAll(int expandedLevel) {
        if (root == null) return;
        collapseAll(root, 0, expandedLevel, null);
    }


    private void collapseAll(MapperNode node, int level,
            int expandedLevel, TreePath skipPath) {
        TreePath treePath = node.getTreePath();
        
        boolean skipCollapse = false;
        boolean skipCollapseGraph = false;
        
        if (skipPath != null) {
            skipCollapseGraph = treePath.equals(skipPath);
            skipCollapse = treePath.isDescendant(skipPath) && !skipCollapseGraph;
        }
        
        if (!node.isLeaf()) {
            if (level >= expandedLevel && node.isExpanded() && !skipCollapse) {
                setExpandedState(treePath, false);
            }

            if (node.isLoaded()) {
                for (int i = node.getChildCount() - 1; i >= 0; i--) {
                    collapseAll(node.getChild(i), level + 1, 
                            expandedLevel, skipPath);
                }
            }
        }
        
        Graph graph = node.getGraph();
        if (graph != null && !skipCollapseGraph && node.isGraphExpanded()) {
            setExpandedGraphState(treePath, false);
        }
    }

    
    public RightTreeCellRenderer getRightTreeCellRenderer() {
        return rightTree.getTreeCellRenderer();
    }

    int getTextHeight() {
        return getFontMetrics(getFont()).getHeight();
    }

    int getTextWidth(String string) {
        return getFontMetrics(getFont()).stringWidth(string);
    }

    int getStepSize() {
        return (getTextHeight() + 4) / 2;
    }

    MapperNode getNode(TreePath treePath, boolean load) {
        return getNode(treePath.getPath(), load);
    }

    MapperNode getNode(Object[] path, boolean load) {
        if (path == null) {
            return null;
        }
        if (path.length == 0) {
            return null;
        }
        if (root == null) {
            return null;
        }
        if (root.getValue() != path[0]) {
            throw new IllegalStateException();
        }

        MapperNode node = root;
        for (int i = 1; i < path.length; i++) {
            if (!load && !node.isLoaded()) {
                return null;
            }
            node = node.getChild(model.getIndexOfChild(node.getValue(), path[i]));
        }

        return node;
    }

    MapperNode getClosestLoadedNode(Object[] path) {
        if (path == null) {
            return null;
        }
        if (path.length == 0) {
            return null;
        }
        if (root == null) {
            return null;
        }
        if (root.getValue() != path[0]) {
            throw new IllegalStateException();
        }

        MapperNode node = root;

        for (int i = 1; i < path.length; i++) {
            if (!node.isLoaded()) {
                break;
            }
            node = node.getChild(model.getIndexOfChild(node.getValue(), path[i]));
        }

        return node;
    }

    MapperNode getNodeAt(int y) {
        return (root == null) ? null : root.getNode(y);
    }

    void invalidateNodes() {
        if (validNodes) {
            rightTree.revalidate();

            canvas.revalidate();

            JComponent component = (JComponent) rightTree.getScrollPane().getRowHeader().getView();
            component.revalidate();

            preferredTreeSize = null;
            graphXRange = null;

            validNodes = false;
        }
    }

    void repaintNodes() {
        if (!repaintSceduled) {
            rightTree.repaint();
            canvas.repaint();

            JComponent component = (JComponent) rightTree.getScrollPane().getRowHeader().getView();
            component.repaint();

            repaintSceduled = true;
        }
    }

    void validateNodes() {
        if (!validNodes && root != null) {
            preferredTreeSize = root.getPreferredSize();
            graphXRange = root.getGraphXRange();

            root.setBounds(0, preferredTreeSize.height, 0);
            root.validate();

            validNodes = true;
        }
    }

    Dimension getPreferredTreeSize() {
        validateNodes();
        return (preferredTreeSize == null) ? null
                : new Dimension(preferredTreeSize.width, preferredTreeSize.height - 1);
    }

    XRange getGraphXRange() {
        validateNodes();
        return (graphXRange == null) ? null
                : new XRange(graphXRange);
    }

    int getLeftIndent() {
        return leftChildIndent;
    }

    int getRightIndent() {
        return rightChildIndent;
    }

    int getTotalIndent() {
        return leftChildIndent + rightChildIndent;
    }

    Icon getOpenIcon() {
        return openIcon;
    }

    Icon getClosedIcon() {
        return closedIcon;
    }

    Icon getLeafIcon() {
        return leafIcon;
    }

    Icon getExpandedIcon() {
        return expandedIcon;
    }

    Icon getCollapsedIcon() {
        return collapsedIcon;
    }

    Color getTreeLineColor() {
        return treeLineColor;
    }

    void updateChildGraphs(TreePath treePath) {
        MapperNode node = getClosestLoadedNode(treePath.getPath());

        while (node != null) {
            node.updateChildGraphs();
            node.invalidate();
            node.repaint();
            node = node.getParent();
        }
    }

    public void updateUI() {
        super.updateUI();
        treeLineColor = UIManager.getColor("Tree.hash");

        leftChildIndent = UIManager.getInt("Tree.rightChildIndent");
        rightChildIndent = UIManager.getInt("Tree.leftChildIndent");

        openIcon = UIManager.getIcon("Tree.openIcon");
        closedIcon = UIManager.getIcon("Tree.closedIcon");
        leafIcon = UIManager.getIcon("Tree.leafIcon");

        expandedIcon = UIManager.getIcon("Tree.expandedIcon");
        collapsedIcon = UIManager.getIcon("Tree.collapsedIcon");
    }

    private class TreeModelListenerImpl implements TreeModelListener {

        public void treeNodesChanged(TreeModelEvent e) {
            TreePath treePath = e.getTreePath();
            int[] indeces = e.getChildIndices();

            if (indeces == null || treePath == null) {
                root.updateChildGraphs();
                root.updateNode();
                root.invalidate();
                root.repaint();
            } else {
                updateChildGraphs(treePath);

                MapperNode node = getNode(treePath, false);
                if (node != null) {
                    for (int i : indeces) {
                        MapperNode child = node.getChild(i);
                        child.updateNode();
                        child.invalidate();
                        child.repaint();
                    }
                }
            }
        }

        public void treeNodesInserted(TreeModelEvent e) {
            updateChildGraphs(e.getTreePath());
            MapperNode node = getNode(e.getPath(), false);
            if (node != null) {
//                node.insertChildren(e);
                node.updateNode();
                node.updateChildren();
                node.invalidate();
                node.repaint();
            }
        }

        public void treeNodesRemoved(TreeModelEvent e) {
            updateChildGraphs(e.getTreePath());
            MapperNode node = getNode(e.getPath(), false);
            if (node != null) {
                node.updateNode();
                node.updateChildren();
                node.invalidate();
                node.repaint();
//                node.removeChildren(e);
            }
        }

        public void treeStructureChanged(TreeModelEvent e) {
            MapperModel mapperModel = model;
            setModel(null);
            setModel(mapperModel);
        }
    }

    private class ScrollPaneYSyncronizer implements ChangeListener {

        private JViewport viewport1;
        private JViewport viewport2;

        public ScrollPaneYSyncronizer(
                JScrollPane scrollPane1,
                JScrollPane scrollPane2) {
            viewport1 = scrollPane1.getViewport();
            viewport2 = scrollPane2.getViewport();

            viewport1.addChangeListener(this);
            viewport2.addChangeListener(this);
        }

        public void stateChanged(ChangeEvent e) {
            if (e.getSource() == viewport1) {
                Point position = viewport2.getViewPosition();
                position.y = viewport1.getViewPosition().y;
                viewport2.setViewPosition(position);
            } else {
                Point position = viewport1.getViewPosition();
                position.y = viewport2.getViewPosition().y;
                viewport1.setViewPosition(position);
            }
        }
    }
    public static final String MODEL_PROPERTY = "mapper-model-property";
    public static final Stroke DASHED_ROW_SEPARATOR_STROKE = new BasicStroke(1,
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
            1, new float[]{4, 2}, 0);
    public static final Stroke DASHED_STROKE = new BasicStroke(1,
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
            1, new float[]{4, 4}, 0);
    public static final Color CANVAS_BACKGROUND_COLOR = new Color(0xFCFAF5);
    public static final Color CANVAS_GRID_COLOR = new Color(0xC0C0C0);
    public static final Color ROW_SEPARATOR_COLOR = new Color(0xBBD3E9); //new Color(0x99B7D3);
    public static final Color SELECTED_BACKGROUND_COLOR_TOP = new Color(0xF0F9FF);
    public static final Color SELECTED_BACKGROUND_COLOR_BOTTOM = new Color(0xD0E0F0);
    public static final Color RIGHT_TREE_HEADER_COLOR = new Color(0x999999);
    public static final VerticalGradient SELECTED_BACKGROUND_IN_FOCUS = new VerticalGradient(
            Mapper.SELECTED_BACKGROUND_COLOR_TOP,
            Mapper.SELECTED_BACKGROUND_COLOR_BOTTOM);
    public static final VerticalGradient SELECTED_BACKGROUND_NOT_IN_FOCUS = new VerticalGradient(
            Utils.gray(Mapper.SELECTED_BACKGROUND_COLOR_TOP, 75),
            Utils.gray(Mapper.SELECTED_BACKGROUND_COLOR_BOTTOM, 75));
}
