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

package org.netbeans.modules.versioning.system.cvss.ui.syncview;

import org.openide.explorer.view.NodeTableModel;
import org.openide.nodes.*;
import org.openide.nodes.PropertySupport.ReadOnly;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.ErrorManager;
import org.openide.awt.MouseUtils;
import org.openide.awt.Mnemonics;
import org.netbeans.modules.versioning.system.cvss.Annotator;
import org.netbeans.modules.versioning.system.cvss.FileStatusCache;
import org.netbeans.modules.versioning.system.cvss.CvsVersioningSystem;
import org.netbeans.modules.versioning.system.cvss.FileInformation;
import org.netbeans.modules.versioning.system.cvss.ui.actions.DeleteLocalAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.ignore.IgnoreAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.AnnotationsAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.log.SearchHistoryAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.tag.TagAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.tag.BranchAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.commit.CommitAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.commit.ExcludeFromCommitAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.UpdateAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.update.GetCleanAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.diff.DiffAction;
import org.netbeans.modules.versioning.system.cvss.ui.actions.diff.ResolveConflictsAction;
import org.netbeans.modules.versioning.system.cvss.CvsModuleConfig;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.util.FilePathCellRenderer;
import org.netbeans.modules.versioning.util.TableSorter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.AncestorEvent;
import java.lang.reflect.InvocationTargetException;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Component;
import java.awt.Color;
import java.awt.Point;
import java.util.*;
import java.io.File;
import org.netbeans.modules.versioning.util.SystemActionBridge;

/**
 * Table that displays nodes in the Versioning view. 
 * 
 * @author Maros Sandor
 */
class SyncTable implements MouseListener, ListSelectionListener, AncestorListener {

    private NodeTableModel  tableModel;
    private JTable          table;
    private JScrollPane     component;
    private SyncFileNode [] nodes = new SyncFileNode[0];
    
    private String []   tableColumns; 
    private TableSorter sorter;

    /**
     * Defines labels for Versioning view table columns.
     */ 
    private static final Map columnLabels = new HashMap(4);
    {
        ResourceBundle loc = NbBundle.getBundle(SyncTable.class);
        columnLabels.put(SyncFileNode.COLUMN_NAME_STICKY, new String [] {
                                          loc.getString("CTL_VersioningView_Column_Sticky_Title"), 
                                          loc.getString("CTL_VersioningView_Column_Sticky_Desc")});
        columnLabels.put(SyncFileNode.COLUMN_NAME_NAME, new String [] { 
                                          loc.getString("CTL_VersioningView_Column_File_Title"), 
                                          loc.getString("CTL_VersioningView_Column_File_Desc")});
        columnLabels.put(SyncFileNode.COLUMN_NAME_STATUS, new String [] { 
                                          loc.getString("CTL_VersioningView_Column_Status_Title"), 
                                          loc.getString("CTL_VersioningView_Column_Status_Desc")});
        columnLabels.put(SyncFileNode.COLUMN_NAME_PATH, new String [] { 
                                          loc.getString("CTL_VersioningView_Column_Path_Title"), 
                                          loc.getString("CTL_VersioningView_Column_Path_Desc")});
    }

    private static final Comparator NodeComparator = new Comparator() {
        public int compare(Object o1, Object o2) {
            Node.Property p1 = (Node.Property) o1;
            Node.Property p2 = (Node.Property) o2;
            String sk1 = (String) p1.getValue("sortkey"); // NOI18N
            if (sk1 != null) {
                String sk2 = (String) p2.getValue("sortkey"); // NOI18N
                return sk1.compareToIgnoreCase(sk2);
            } else {
                try {
                    String s1 = (String) p1.getValue();
                    String s2 = (String) p2.getValue();
                    return s1.compareToIgnoreCase(s2);
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(e);
                    return 0;
                }
            }
        }
    };
    
    public SyncTable() {
        tableModel = new NodeTableModel();
        sorter = new TableSorter(tableModel);
        sorter.setColumnComparator(Node.Property.class, NodeComparator);
        table = new JTable(sorter);
        sorter.setTableHeader(table.getTableHeader());
        int height = new JLabel("FONTSIZE").getPreferredSize().height * 6 / 5;  // NOI18N
        table.setRowHeight(height);
        component = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        component.getViewport().setBackground(table.getBackground());
        Color borderColor = UIManager.getColor("scrollpane_border"); // NOI18N
        if (borderColor == null) borderColor = UIManager.getColor("controlShadow"); // NOI18N
        component.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor));
        table.addMouseListener(this);
        table.setDefaultRenderer(Node.Property.class, new SyncTableCellRenderer());
        table.getSelectionModel().addListSelectionListener(this);
        table.addAncestorListener(this);
        table.getAccessibleContext().setAccessibleName(NbBundle.getMessage(SyncTable.class, "ACSN_VersioningTable"));
        table.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(SyncTable.class, "ACSD_VersioningTable"));
        setColumns(new String [] { SyncFileNode.COLUMN_NAME_NAME, SyncFileNode.COLUMN_NAME_STATUS, SyncFileNode.COLUMN_NAME_PATH });
        table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_DOWN_MASK ), "org.openide.actions.PopupAction");
        table.getActionMap().put("org.openide.actions.PopupAction", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                showPopup(org.netbeans.modules.versioning.util.Utils.getPositionForPopup(table));
            }
        });
    }

    void setDefaultColumnSizes() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int width = table.getWidth();
                if (tableColumns.length == 3) {
                    for (int i = 0; i < tableColumns.length; i++) {
                        if (SyncFileNode.COLUMN_NAME_PATH.equals(tableColumns[i])) {
                            table.getColumnModel().getColumn(i).setPreferredWidth(width * 60 / 100);
                        } else {
                            table.getColumnModel().getColumn(i).setPreferredWidth(width * 20 / 100);
                        }
                    }
                } else if (tableColumns.length == 4) {
                    for (int i = 0; i < tableColumns.length; i++) {
                        if (SyncFileNode.COLUMN_NAME_PATH.equals(tableColumns[i])) {
                            table.getColumnModel().getColumn(i).setPreferredWidth(width * 55 / 100);
                        } else {
                            table.getColumnModel().getColumn(i).setPreferredWidth(width * 15 / 100);
                        }
                    }
                }
            }
        });
    }

    public void ancestorAdded(AncestorEvent event) {
        setDefaultColumnSizes();
    }

    public void ancestorMoved(AncestorEvent event) {
    }

    public void ancestorRemoved(AncestorEvent event) {
    }

    public SyncFileNode [] getDisplayedNodes() {
        int n = sorter.getRowCount();
        SyncFileNode [] ret = new SyncFileNode[n];
        for (int i = 0; i < n; i++) {
            ret[i] = nodes[sorter.modelIndex(i)]; 
        }
        return ret;
    }

    public JComponent getComponent() {
        return component;
    }
    
    /**
     * Sets visible columns in the Versioning table.
     * 
     * @param columns array of column names, they must be one of SyncFileNode.COLUMN_NAME_XXXXX constants.  
     */ 
    final void setColumns(String [] columns) {
        if (Arrays.equals(columns, tableColumns)) return;
        setDefaultColumnSizes();
        setModelProperties(columns);
        tableColumns = columns;
        for (int i = 0; i < tableColumns.length; i++) {
            sorter.setColumnComparator(i, null);
            sorter.setSortingStatus(i, TableSorter.NOT_SORTED);
            if (SyncFileNode.COLUMN_NAME_STATUS.equals(tableColumns[i])) {
                sorter.setSortingStatus(i, TableSorter.ASCENDING);
                break;
            }
        }
    }
        
    private void setModelProperties(String [] columns) {
        Node.Property [] properties = new Node.Property[columns.length];
        for (int i = 0; i < columns.length; i++) {
            String column = columns[i];
            String [] labels = (String[]) columnLabels.get(column);
            properties[i] = new ColumnDescriptor(column, String.class, labels[0], labels[1]);  
        }
        tableModel.setProperties(properties);
    }

    void setTableModel(SyncFileNode [] nodes) {
        this.nodes = nodes;
        tableModel.setNodes(nodes);
    }

    void focus() {
        table.requestFocus();
    }

    private static class ColumnDescriptor extends ReadOnly {
        
        public ColumnDescriptor(String name, Class type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        public Object getValue() throws IllegalAccessException, InvocationTargetException {
            return null;
        }
    }

    private void showPopup(MouseEvent e) {
        int row = table.rowAtPoint(e.getPoint());
        if (row != -1) {
            boolean makeRowSelected = true;
            int [] selectedrows = table.getSelectedRows();
            for (int i = 0; i < selectedrows.length; i++) {
                if (row == selectedrows[i]) {
                    makeRowSelected = false;
                    break;
                }
            }
            if (makeRowSelected) {
                table.getSelectionModel().setSelectionInterval(row, row);
            }
        }
        showPopup(e.getPoint());
    }
    
    private void showPopup(Point p) {
        JPopupMenu menu = getPopup();
        menu.show(table, p.x, p.y);
    }

    /**
     * Constructs contextual Menu: File Node
        <pre>
        Open
        -------------------
        Diff                 (default action)
        Update
        Commit...
        --------------------
        Tag...
        Branch...
        --------------------
        Show Annotations...
        Show History...
        --------------------
        Exclude from Commit   (Include in Commit)
        Revert Modifications  (Revert Delete)(Delete)
        Ignore                (Unignore)
        </pre>
     */
    private JPopupMenu getPopup() {

        File [] files = Utils.getCurrentContext(null).getFiles();
        JPopupMenu menu = new JPopupMenu();
        JMenuItem item;
        
        item = menu.add(new OpenInEditorAction());
        Mnemonics.setLocalizedText(item, item.getText());
        menu.add(new JSeparator());
        item = menu.add(new SystemActionBridge(SystemAction.get(DiffAction.class), actionString("CTL_PopupMenuItem_Diff"))); // NOI18N
        Mnemonics.setLocalizedText(item, item.getText());
        item = menu.add(new SystemActionBridge(SystemAction.get(UpdateAction.class), actionString("CTL_PopupMenuItem_Update"))); // NOI18N
        Mnemonics.setLocalizedText(item, item.getText());
        item = menu.add(new SystemActionBridge(SystemAction.get(CommitAction.class), actionString("CTL_PopupMenuItem_Commit"))); // NOI18N
        Mnemonics.setLocalizedText(item, item.getText());
        menu.add(new JSeparator());
        item = menu.add(new SystemActionBridge(SystemAction.get(TagAction.class), actionString("CTL_PopupMenuItem_Tag"))); // NOI18N
        Mnemonics.setLocalizedText(item, item.getText());
        item = menu.add(new SystemActionBridge(SystemAction.get(BranchAction.class), actionString("CTL_PopupMenuItem_Branch"))); // NOI18N
        Mnemonics.setLocalizedText(item, item.getText());
        menu.add(new JSeparator());
        item = menu.add(new SystemActionBridge(SystemAction.get(AnnotationsAction.class), 
                                               ((AnnotationsAction)SystemAction.get(AnnotationsAction.class)).visible(null) ?
                                               actionString("CTL_PopupMenuItem_HideAnnotations") : // NOI18N
                                               actionString("CTL_PopupMenuItem_ShowAnnotations"))); // NOI18N
        Mnemonics.setLocalizedText(item, item.getText());
        item = menu.add(new SystemActionBridge(SystemAction.get(SearchHistoryAction.class), actionString("CTL_PopupMenuItem_SearchHistory"))); // NOI18N
        Mnemonics.setLocalizedText(item, item.getText());
        menu.add(new JSeparator());
        item = org.netbeans.modules.versioning.util.Utils.toMenuItem(new ExcludeFromCommitAction(files));
        menu.add(item);

        Action revertAction;
        boolean allLocallyNew = true;
        boolean allLocallyDeleted = true;
        FileStatusCache cache = CvsVersioningSystem.getInstance().getStatusCache();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            FileInformation info = cache.getStatus(file);
            if ((info.getStatus() & DeleteLocalAction.LOCALLY_DELETABLE_MASK) == 0 ) {
                allLocallyNew = false;
            }
            if (info.getStatus() != FileInformation.STATUS_VERSIONED_DELETEDLOCALLY && info.getStatus() != FileInformation.STATUS_VERSIONED_REMOVEDLOCALLY) {
                allLocallyDeleted = false;
            }
        }
        if (allLocallyNew) {
            SystemAction systemAction = SystemAction.get(DeleteLocalAction.class);
            revertAction = new SystemActionBridge(systemAction, actionString("CTL_PopupMenuItem_Delete")); // NOI18N
        } else if (allLocallyDeleted) {
            revertAction = new SystemActionBridge(SystemAction.get(GetCleanAction.class), actionString("CTL_PopupMenuItem_RevertDelete")); // NOI18N
        } else {
            revertAction = new SystemActionBridge(SystemAction.get(GetCleanAction.class), actionString("CTL_PopupMenuItem_GetClean")); // NOI18N
        }
        item = menu.add(revertAction);
        Mnemonics.setLocalizedText(item, item.getText());

        item = menu.add(new SystemActionBridge(SystemAction.get(ResolveConflictsAction.class), actionString("CTL_PopupMenuItem_ResolveConflicts"))); // NOI18N
        Mnemonics.setLocalizedText(item, item.getText());
        
        Action ignoreAction = new SystemActionBridge(SystemAction.get(IgnoreAction.class),
           ((IgnoreAction)SystemAction.get(IgnoreAction.class)).getActionStatus(files) == IgnoreAction.UNIGNORING ?
           actionString("CTL_PopupMenuItem_Unignore") : // NOI18N
           actionString("CTL_PopupMenuItem_Ignore")); // NOI18N
        item = menu.add(ignoreAction);
        Mnemonics.setLocalizedText(item, item.getText());

        return menu;
    }

    /** 
     * Workaround.
     * I18N Test Wizard searches for keys in syncview package Bundle.properties 
     */
    private String actionString(String key) {
        ResourceBundle actionsLoc = NbBundle.getBundle(Annotator.class);
        return actionsLoc.getString(key);
    }
    
    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (e.isPopupTrigger()) {
            showPopup(e);
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && MouseUtils.isDoubleClick(e)) {
            int row = table.rowAtPoint(e.getPoint());
            if (row == -1) return;
            row = sorter.modelIndex(row);
            Action action = nodes[row].getPreferredAction();
            if (action == null || !action.isEnabled()) action = new OpenInEditorAction();
            if (action.isEnabled()) {
                action.actionPerformed(new ActionEvent(this, 0, "")); // NOI18N
            }
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        List selectedNodes = new ArrayList();
        ListSelectionModel selectionModel = table.getSelectionModel();
        TopComponent tc = (TopComponent) SwingUtilities.getAncestorOfClass(TopComponent.class,  table);
        if (tc == null) return; // table is no longer in component hierarchy
        
        int min = selectionModel.getMinSelectionIndex();
        if (min == -1) {
            tc.setActivatedNodes(new Node[0]);            
        }
        int max = selectionModel.getMaxSelectionIndex();
        for (int i = min; i <= max; i++) {
            if (selectionModel.isSelectedIndex(i)) {
                int idx = sorter.modelIndex(i);
                selectedNodes.add(nodes[idx]);
            }
        }
        tc.setActivatedNodes((Node[]) selectedNodes.toArray(new Node[selectedNodes.size()]));
    }
    
    private class SyncTableCellRenderer extends DefaultTableCellRenderer {
        
        private FilePathCellRenderer pathRenderer = new FilePathCellRenderer();
        
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component renderer;
            int modelColumnIndex = table.convertColumnIndexToModel(column);
            if (modelColumnIndex == 0) {
                SyncFileNode node = nodes[sorter.modelIndex(row)];
                if (!isSelected) {
                    value = "<html>" + node.getHtmlDisplayName(); // NOI18N
                }
                if (CvsModuleConfig.getDefault().isExcludedFromCommit(node.getFile())) {
                    String nodeName = node.getDisplayName();
                    if (isSelected) {
                        value = "<html><s>" + nodeName + "</s></html>"; // NOI18N
                    } else {
                        value = "<html><s>" + CvsVersioningSystem.getInstance().getAnnotator().annotateNameHtml(nodeName, node.getFileInformation(), null) + "</s>"; // NOI18N
                    }
                }
            }
            if (modelColumnIndex == 2) {
                renderer = pathRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else {
                renderer = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
            if (renderer instanceof JComponent) {
                String path = nodes[sorter.modelIndex(row)].getFile().getAbsolutePath(); 
                ((JComponent) renderer).setToolTipText(path);
            }
            return renderer;
        }
    }
}
