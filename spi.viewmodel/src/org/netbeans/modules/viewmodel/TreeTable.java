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

package org.netbeans.modules.viewmodel;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;


import java.util.*;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreeNode;

import javax.swing.tree.TreePath;
import javax.swing.tree.TreeModel;


import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.ComputingException;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.explorer.ExplorerActions;

import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;

import org.openide.explorer.view.NodeTableModel;
import org.openide.explorer.view.TreeTableView;
import org.openide.explorer.view.Visualizer;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeNotFoundException;

import org.openide.nodes.NodeOp;

import org.openide.nodes.PropertySupport;
import org.openide.windows.TopComponent;


/**
 * Implements root node of hierarchy created for given TreeModel.
 *
 * @author   Jan Jancura
 */
public class TreeTable extends JPanel implements 
ExplorerManager.Provider, PropertyChangeListener {
    
    private ExplorerManager     explorerManager;
    private MyTreeTable         treeTable;
    private Node.Property[]     columns;
    private List                expandedPaths = new ArrayList ();
    private TreeModelRoot       currentTreeModelRoot;
    
    private ExplorerActions     explorerActions = new ExplorerActions ();
    {explorerActions.setConfirmDelete (false);}
    
    
    public TreeTable () {
        setLayout (new BorderLayout ());
            treeTable = new MyTreeTable ();
            treeTable.setTreePreferredWidth (200);
            treeTable.setRootVisible (false);
            treeTable.setVerticalScrollBarPolicy 
                (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
            treeTable.setHorizontalScrollBarPolicy 
                (JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add (treeTable, "Center");  //NOI18N
    }
    
    public void setModel (CompoundModel model) {
        
        // 1) destroy old model
        if (currentTreeModelRoot != null) 
            currentTreeModelRoot.destroy ();
        
        // 2) save current settings (like columns, expanded paths)
        List ep = treeTable.getExpandedPaths ();
        saveWidths ();
        
        // 3) no model => set empty root node & return
        if (model == null) {
            getExplorerManager ().setRootContext (
                new AbstractNode (Children.LEAF)
            );
            return;
        }
        
        // 4) set columns for given model
        columns = createColumns (model);
        treeTable.setProperties (columns);
        
        
//        try {
//            treeTable.setToolTipText (model.getShortDescription (
//                model.getRoot ()
//            ));
//        } catch (ComputingException ex) {
//        } catch (UnknownTypeException ex) {
//            ex.printStackTrace ();
//        }
        
        // 5) set root node for given model
        currentTreeModelRoot = new TreeModelRoot (model);
        getExplorerManager ().setRootContext (
            currentTreeModelRoot.getRootNode ()
        );
        
        // 6) update column widths & expanded nodes
        updateColumnWidths ();
        treeTable.expandNodes (expandedPaths);
        // TODO: this is a workaround, we should find a better way later
        final List backupPath = new ArrayList(expandedPaths);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                treeTable.expandNodes (backupPath);
            }
        });
        if (ep.size () > 0) expandedPaths = ep;
    }
    
    public ExplorerManager getExplorerManager () {
        if (explorerManager == null) {
            explorerManager = new ExplorerManager ();
        }
        return explorerManager;
    }
    
    public void propertyChange (PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName ();
        TopComponent tc = (TopComponent) SwingUtilities.
            getAncestorOfClass (TopComponent.class, this);
        if (tc == null) return;
        if (propertyName.equals (TopComponent.Registry.PROP_CURRENT_NODES)) {
            if (equalNodes ()) {
                explorerActions.attach (getExplorerManager ());
            } else
                explorerActions.detach ();
        } else
        if (propertyName.equals (ExplorerManager.PROP_SELECTED_NODES)) {
            tc.setActivatedNodes ((Node[]) evt.getNewValue ());
        }
    }
    
    private boolean equalNodes () {
        Node[] ns1 = TopComponent.getRegistry ().getCurrentNodes ();
        Node[] ns2 = getExplorerManager ().getSelectedNodes ();
        if (ns1 == ns2) return true;
        if ( (ns1 == null) || (ns2 == null) ) return false;
        if (ns1.length != ns2.length) return false;
        int i, k = ns1.length;
        for (i = 0; i < k; i++)
            if (!ns1 [i].equals (ns2 [i])) return false;
        return true;
    }
    
    private Node.Property[] createColumns (CompoundModel model) {
        ColumnModel[] cs = model.getColumns ();
        int i, k = cs.length;
        Node.Property[] columns = new Column [k];
        boolean addDefaultColumn = true;
        for (i = 0; i < k; i++) {
            columns [i] = new Column (
                cs [i], this
            );
            if (cs [i].getType () == null)
                addDefaultColumn = false;
        }
        if (!addDefaultColumn) return columns;
        PropertySupport.ReadWrite[] columns2 = 
            new PropertySupport.ReadWrite [columns.length + 1];
        System.arraycopy (columns, 0, columns2, 1, columns.length);
        columns2 [0] = new DefaultColumn ();
        return columns2;
    }

    void updateColumnWidths () {
        int i, k = columns.length;
        for (i = 0; i < k; i++) {
            if (Boolean.TRUE.equals (columns [i].getValue 
                ("InvisibleInTreeTableView"))
            ) continue;
            if (columns [i] instanceof Column) {
                Column column = (Column) columns [i];
                if (column.isDefault ()) {
                    int width = column.getColumnWidth ();
                    treeTable.setTreePreferredWidth (width);
                } else {
                    int order = column.getOrderNumber ();
                    if (order == -1) continue;
                    int width = column.getColumnWidth ();
                    treeTable.setTableColumnPreferredWidth (order, width);
                }
            }
        }
    }
    
    private void saveWidths () {
        if (columns == null) return;
        int i, k = columns.length;
        for (i = 0; i < k; i++) {
            if (Boolean.TRUE.equals (columns [i].getValue 
                ("InvisibleInTreeTableView"))
            ) continue;
            if (!(columns [i] instanceof Column)) continue;
            Column column = (Column) columns [i];
            if (column.isDefault ()) {
                TableColumn tc = treeTable.getTable ().getColumnModel ().
                    getColumn (0);
                if (tc == null) continue;
                int width = tc.getWidth ();
                column.setColumnWidth (width);
            } else {
                int order = column.getOrderNumber ();
                if (order == -1) continue;

                TableColumn tc = treeTable.getTable ().getColumnModel ().
                    getColumn (order + 1);
                if (tc == null) continue;
                int width = tc.getWidth ();
                column.setColumnWidth (width);
            }
        }
    }
    
    /** Requests focus for the tree component. Overrides superclass method. */
    public boolean requestFocusInWindow () {
        super.requestFocusInWindow ();
        return treeTable.requestFocusInWindow ();
    }
    
    public void addNotify () {
        super.addNotify ();
        TopComponent.getRegistry ().addPropertyChangeListener (this);
        getExplorerManager ().addPropertyChangeListener (this);
    }
    
    public void removeNotify () {
        super.removeNotify ();
        TopComponent.getRegistry ().removePropertyChangeListener (this);
        getExplorerManager ().removePropertyChangeListener (this);
    }

    
    private static class MyTreeTable extends TreeTableView {
        MyTreeTable () {
            super ();
            treeTable.setShowHorizontalLines (true);
            treeTable.setShowVerticalLines (false);
        }
        
        JTable getTable () {
            return treeTable;
        }

        public List getExpandedPaths () {
            List result = new ArrayList ();
            ExplorerManager em = ExplorerManager.find (this);
            TreeNode rtn = Visualizer.findVisualizer (
                em.getRootContext ()
            );
            TreePath tp = new TreePath (rtn); // Get the root
            
            Enumeration exPaths = tree.getExpandedDescendants (tp); 
            if (exPaths == null) return result;
            for (;exPaths.hasMoreElements ();) {
                TreePath ep = (TreePath) exPaths.nextElement ();
                Node en = Visualizer.findNode (ep.getLastPathComponent ());
                String[] path = NodeOp.createPath (en, em.getRootContext ());
                result.add (path);
            }
            return result;
        }
        
        /** Expands all the paths, when exists
         */
        public void expandNodes (List exPaths) {
            for (Iterator it = exPaths.iterator (); it.hasNext ();) {
                String[] sp = (String[]) it.next ();
                TreePath tp = stringPath2TreePath (sp);
                if (tp != null) showPath (tp);
            }
        }

        /** Converts path of strings to TreePath if exists null otherwise
         */
        private TreePath stringPath2TreePath (String[] sp) {
            ExplorerManager em = ExplorerManager.find (this);
            try {
                Node n = NodeOp.findPath (em.getRootContext (), sp); 
                
                // Create the tree path
                TreeNode tns[] = new TreeNode [sp.length + 1];
                
                for (int i = sp.length; i >= 0; i--) {
                    tns[i] = Visualizer.findVisualizer (n);
                    n = n.getParentNode ();
                }                
                return new TreePath (tns);
            } catch (NodeNotFoundException e) {
                return null;
            }
        }
    }
}

