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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.etl.ui.view.wizards;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.jdbc.builder.DBMetaData;
import org.netbeans.modules.jdbc.builder.ForeignKeyColumn;
import org.netbeans.modules.jdbc.builder.KeyColumn;
import org.netbeans.modules.jdbc.builder.Table;
import org.netbeans.modules.jdbc.builder.TableColumn;
import org.netbeans.modules.model.database.DBConnectionDefinition;
import org.netbeans.modules.model.database.DatabaseModel;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.impl.ForeignKeyImpl;
import org.netbeans.modules.sql.framework.model.impl.PrimaryKeyImpl;
import org.netbeans.modules.sql.framework.model.impl.SourceColumnImpl;
import org.netbeans.modules.sql.framework.model.impl.SourceTableImpl;
import org.netbeans.modules.sql.framework.model.impl.TargetColumnImpl;
import org.netbeans.modules.sql.framework.model.impl.TargetTableImpl;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

import com.sun.sql.framework.utils.Logger;

/**
 * Implements a two-list transfer panel with bulk add/remove capability.
 *
 * @author Sanjeeth Duvuru
 * @author Jonathan Giron
 * @version $Revision$
 */
public class ETLCollaborationWizardTransferPanel extends JPanel implements ActionListener, ListSelectionListener, WizardDescriptor.Panel,
        TableModelListener, PropertyChangeListener {
    
    /* Log4J category string */
    private static final String LOG_CATEGORY = ETLCollaborationWizardTransferPanel.class.getName();
    
    /**
     * Extends ChangeEvent to convey information on an item being transferred to or from
     * the source of the event.
     *
     * @author Jonathan Giron
     * @version $Revision$
     */
    public static class TransferEvent extends ChangeEvent {
        /** Indicates addition of an item to the source of the event */
        public static final int ADDED = 0;
        
        /** Indicates removal of an item from the source of the event */
        public static final int REMOVED = 1;
        
        private Object item;
        private int type;
        
        /**
         * Create a new TransferEvent instance with the given source, item and type.
         *
         * @param source source of this transfer event
         * @param item transferred item
         * @param type transfer type, either ADDED or REMOVED
         * @see #ADDED
         * @see #REMOVED
         */
        public TransferEvent(Object source, Object item, int type) {
            super(source);
            this.item = item;
            this.type = type;
        }
        
        /**
         * Gets item that was transferred.
         *
         * @return transferred item
         */
        public Object getItem() {
            return item;
        }
        
        /**
         * Gets type of transfer event.
         *
         * @return ADDED or REMOVED
         */
        public int getType() {
            return type;
        }
    }
    
    /**
     * Container for ListModels associated with source and destination lists of a list
     * transfer panel. Holds ButtonModels for controls that indicate selected addition and
     * bulk addition to destination list and selected removal and bulk removal of items
     * from the destination list.
     *
     * @author Jonathan Giron
     * @author Sanjeeth Duvuru
     * @version $Revision$
     */
    class ListTransferModel {
        
        private HashSet changeListeners;
        // Dropdown of schemas for a selected connection (source)
        private DefaultComboBoxModel dest;
        
        private String listPrototype;
        
        // Connections from the DB explorer.
        private DefaultListModel source;
        
        // Tables for a selected schema.
        private DefaultListModel schemaTables;
        
        private DefaultTableModel selectedTableModel;
        
        private HashMap<String, SQLDBTable> nameToModelMap;
        
        private final String[] tableHeaders = new String [] { "Name", "Schema", "Catalog", "Connection"};
        
        /**
         * Creates a new instance of ListTransferModel, using the data in the given
         * collections to initially populate the source and destination lists.
         *
         * @param srcColl Collection used to populate source list
         * @param dstColl Collection used to populate destination list
         */
        public ListTransferModel(Collection srcColl, Collection dstColl) {
            
            if (srcColl == null || dstColl == null) {
                throw new IllegalArgumentException("Must supply non-null collections for srcColl and dstColl");
            }
            
            listPrototype = "";
            
            source = new DefaultListModel();
            dest = new DefaultComboBoxModel();
            schemaTables = new DefaultListModel();
            nameToModelMap = new HashMap<String, SQLDBTable>();
            
            selectedTableModel = new DefaultTableModel(new Object[][]{}, tableHeaders ) { public boolean isCellEditable(int row, int col) { return false; } };
            
            setSourceList(srcColl);
            setDestinationList(dstColl);
            
            changeListeners = new HashSet();
        }
        
        
        /**
         * Moves indicated items from source to destination list.
         *
         * @param selections array of selected items
         * @param indices array of indices, each element corresponding to the item in
         *        selections array
         */
        public void add(Object[] selections) {
            synchronized (dest) {
                synchronized (source) {
                    
                    for (int i = 0; i < selections.length; i++) {
                        Object element = selections[i];
                        dest.addElement(element);
                    }
                }
            }
        }
        
        public void addToSelectedTables(Object[] rowData, SQLDBTable table) {
            if (rowData != null && rowData.length > 0) {
                selectedTableModel.addRow(rowData);
                String key = (String) rowData[1] + "." + (String) rowData[0];
                nameToModelMap.put(key, table);
            }
        }
        
        public String getConnectionNameForTable(String tableDisplayString, String schemaName) {
            String connName = null;
            String key = schemaName + "." + tableDisplayString;
            for (int i = 0; i < selectedTableModel.getRowCount(); i++) {
                String rowKey = (String)selectedTableModel.getValueAt(i, 1) + "."
                        + (String)selectedTableModel.getValueAt(i, 0);
                if (rowKey.equals(key)) {
                    connName = (String)selectedTableModel.getValueAt(i, 3);
                    break;
                }
            }
            return connName;
        }
        
        public void removeFromSelectedTables(int rowIndex) {
            try {
                selectedTableModel.removeRow(rowIndex);
            } catch (ArrayIndexOutOfBoundsException aix) {
                Logger.printThrowable(Logger.INFO, LOG_CATEGORY, null, "Failed to remove row from selected tables" , aix);
            }
        }
        
        public DefaultTableModel getSelectedTablesModel() {
            return this.selectedTableModel;
        }
        
        public HashMap getNameToModelMap() {
            return this.nameToModelMap;
        }
        
        /**
         * Moves indicated items from source to destination list.
         *
         * @param selections
         *            array of selected items
         * @param indices
         *            array of indices, each element corresponding to the item
         *            in selections array
         */
        public void addSchemas(Vector newSchemas) {
            synchronized (schemaTables) {
                for (int i = 0; i < newSchemas.size(); i++) {
                    Object element = newSchemas.elementAt(i);
                    schemaTables.addElement(element);
                }
            }
        }
        
        /**
         * Add a ChangeListener to this model.
         *
         * @param l ChangeListener to add
         */
        public void addChangeListener(ChangeListener l) {
            if (l != null) {
                synchronized (changeListeners) {
                    changeListeners.add(l);
                }
            }
        }
        
        /**
         * Gets copy of current contents of destination list
         *
         * @return List of current destination list contents
         */
        public List getSelectedTablesList() {
            ArrayList dstList = new ArrayList();
            synchronized (this.selectedTableModel) {
                for (int i = 0; i < selectedTableModel.getRowCount(); i++) {
                    String tableName = (String) selectedTableModel.getValueAt(i, 0);
                    String schemaName = (String) selectedTableModel.getValueAt(i, 1);
                    String key = schemaName + "." + tableName;
                    SQLDBTable tableModel = (SQLDBTable) this.nameToModelMap.get(key);
                    if (tableModel != null) {
                        dstList.add(tableModel);
                    }
                }
            }
            return dstList;
        }
        
        
        /**
         * Gets ListModel associated with destination list.
         *
         * @return source ListModel
         */
        public ListModel getDestinationModel() {
            return dest;
        }
        
        /**
         * Gets ListModel associated with schemaTables JList (below combobox).
         *
         * @return source ListModel
         */
        public ListModel getSchemaTablesModel() {
            return schemaTables;
        }
        
        /**
         * Gets maximum number of items expected in either the source or destination list.
         *
         * @return maximum count of items in any one list
         */
        public int getMaximumListSize() {
            return source.size() + dest.getSize();
        }
        
        /**
         * Gets prototype String that has the largest width of an item in either list.
         *
         * @return String whose length is the largest among the items in either list
         */
        public String getPrototypeCell() {
            return listPrototype;
        }
        
        /**
         * Returns index of source item matching the given string.
         *
         * @param searchStr string to search for in source list
         * @param startFrom index from which to start search
         * @return index of matching item, or -1 if no match exists
         */
        public int getSourceIndexFor(String searchStr, int startFrom) {
            if (startFrom < 0 || startFrom > source.size()) {
                startFrom = 0;
            }
            
            if (searchStr != null && searchStr.trim().length() != 0) {
                return source.indexOf(searchStr, startFrom);
            }
            
            return -1;
        }
        
        /**
         * Gets copy of current contents of source list
         *
         * @return List of current source list contents
         */
        public List getSourceList() {
            ArrayList srcList = new ArrayList();
            
            synchronized (source) {
                source.trimToSize();
                for (int i = 0; i < source.size(); i++) {
                    srcList.add(source.get(i));
                }
            }
            return srcList;
        }
        
        /**
         * Gets ListModel associated with source list.
         *
         * @return source ListModel
         */
        public ListModel getSourceModel() {
            return source;
        }
        
        /**
         * Moves indicated items from destination to source list.
         *
         * @param selections array of selected items
         * @param indices array of indices, each element corresponding to the item in
         *        selections array
         */
        public void remove(Object[] selections, int[] indices) {
            synchronized (dest) {
                synchronized (source) {
                    for (int i = 0; i < indices.length; i++) {
                        Object element = selections[i];
                        source.addElement(element);
                        dest.removeElement(element);
                        fireTransferEvent(dest, element, TransferEvent.REMOVED);
                    }
                }
            }
            List tableNameList = getTableNames(dest);
            ETLCollaborationWizardTransferPanel.this.tablePanel.resetTable(tableNameList);
            // fire change event so that next button can be enabled as we remove new rows
            // in table
            fireChangeEvent();
        }
        
        /**
         * Remove a ChangeListener from this model.
         *
         * @param l ChangeListener to remove
         */
        public void removeChangeListener(ChangeListener l) {
            if (l != null) {
                synchronized (changeListeners) {
                    changeListeners.remove(l);
                }
            }
        }
        
        /**
         * Sets destination list to include contents of given list. Clears current
         * contents before adding items from newList.
         *
         * @param newList List whose contents will supplant the current contents of the
         *        destination list
         */
        public void setDestinationList(Collection newList) {
            if (newList == null) {
                throw new IllegalArgumentException("Must supply non-null Collection for newList");
            }
            
            if (dest == null) {
                dest = new DefaultComboBoxModel();
            }
            
            synchronized (dest) {
                dest.removeAllElements();
                
                Iterator it = newList.iterator();
                while (it.hasNext()) {
                    Object o = it.next();
                    dest.addElement(o);
                    if (o.toString().trim().length() > listPrototype.length()) {
                        listPrototype = o.toString().trim();
                    }
                }
            }
        }
        
        /**
         * Sets source list to include contents of given list. Clears current contents
         * before adding items from newList.
         *
         * @param newList List whose contents will supplant the current contents of the
         *        source list
         */
        public void setSourceList(Collection newList) {
            if (newList == null) {
                throw new IllegalArgumentException("Must supply non-null Collection for newList");
            }
            
            if (source == null) {
                source = new DefaultListModel();
            }
            
            synchronized (source) {
                source.clear();
                
                Iterator it = newList.iterator();
                while (it.hasNext()) {
                    Object o = it.next();
                    source.addElement(o);
                    if (o.toString().trim().length() > listPrototype.length()) {
                        listPrototype = o.toString().trim();
                    }
                }
            }
        }
        
        private void fireTransferEvent(Object src, Object item, int type) {
            if (src != null && item != null) {
                TransferEvent e = new TransferEvent(src, item, type);
                synchronized (changeListeners) {
                    Iterator iter = changeListeners.iterator();
                    while (iter.hasNext()) {
                        ChangeListener l = (ChangeListener) iter.next();
                        l.stateChanged(e);
                    }
                }
            }
        }
    }
    
    class DBModelNameCellRenderer extends DefaultListCellRenderer{
        public DBModelNameCellRenderer(String protoString){
            super();
            setText(protoString.toString());
        }
        
        public Component getListCellRendererComponent(JList list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus){
            
            if (isSelected) {
                this.setBackground(list.getSelectionBackground());
                this.setForeground(list.getSelectionForeground());
            } else {
                this.setBackground(list.getBackground());
                this.setForeground(list.getForeground());
            }
            
            this.setEnabled(list.isEnabled());
            this.setFont(list.getFont());
            this.setOpaque(true);
            
            if (value instanceof SQLDBModel){
                SQLDBModel model = (SQLDBModel) value;
                if (model.getDisplayName() != null){
                    this.setText(model.getDisplayName());
                }else{
                    this.setText(model.getModelName());
                }
            } else if (value instanceof DatabaseConnection) {
                if( value.toString()!=null)
                    this.setText(((DatabaseConnection)value).getName());
                else
                    this.setText("<None>");
            } else if (value instanceof String) {
                this.setText((String)value);
            }
            
            return this;
        }
    }
    
    /** Command requesting search of source list for a given string */
    public static final String CMD_SEARCH = "Search";
    
    /** Indicates addition of item(s). */
    public static final String LBL_ADD = ">";
    
    /** Label indicating that all elements should be moved. */
    public static final String LBL_ALL = "ALL";
    
    /** Indicates addition of all source items. */
    public static final String LBL_ADD_ALL = LBL_ALL + " " + LBL_ADD;
    
    /** Describes destination list */
    public static final String LBL_DEST_MSG = "Schemas:";
    
    /** Indicates removal of item(s). */
    public static final String LBL_REMOVE = "<";
    
    /** Indicates removal of all destination items. */
    public static final String LBL_REMOVE_ALL = LBL_REMOVE + " " + LBL_ALL;
    
    /** Describes source list and user task. */
    public static final String LBL_SOURCE_MSG = "Available Connections:";
    
    /** Maximum number of visible items in lists */
    public static final int MAXIMUM_VISIBLE = 10;
    
    /** Minimum number of visible items in lists */
    public static final int MINIMUM_VISIBLE = 5;
    
    /** Tooltip to describe addition of selected item(s). */
    public static final String TIP_ADD = "Add to selected items";
    
    /** Tooltip to describe addition of all source items. */
    public static final String TIP_ADD_ALL = "Add all items";
    
    /** Tooltip to describe addition of selected item(s). */
    public static final String TIP_REMOVE = "Remove from selected items";
    
    /** Tooltip to describe removal of all destination items. */
    public static final String TIP_REMOVE_ALL = "Remove all items";
    
    /**
     * Indicates whether OTDs in the given List have enough selected tables to allow for
     * creation of a join from among the set of tables.
     *
     * @return true if number of selected tables is sufficient to create a join; false
     *         otherwise
     */
    static boolean hasEnoughTablesForJoin(List otds) {
        return (getSelectedTableCount(otds) >= 2);
    }
    
    /**
     * Counts number of selected tables in the given List of OTDs.
     *
     * @param srcOtds List of OTDs to iterate through
     * @return count of selected tables in <code>srcOtds</code>
     */
    private static int getSelectedTableCount(List otds) {
        int selected = 0;
        
        Iterator otdIter = otds.iterator();
        while (otdIter.hasNext()) {
            SQLDBModel dbModel = (SQLDBModel) otdIter.next();
            Iterator tblIter = dbModel.getTables().iterator();
            while (tblIter.hasNext()) {
                if (((SQLDBTable) tblIter.next()).isSelected()) {
                    selected++;
                }
            }
        }
        
        return selected;
    }
    
    private static List getTableNames(ListModel dest) {
        ArrayList tabNameList = new ArrayList();
        for (int i = 0; i < dest.getSize(); i++) {
            DatabaseModel otd = (DatabaseModel) dest.getElementAt(i);
            if (otd != null) {
                
                tabNameList.addAll(otd.getTables());
            }
        }
        return tabNameList;
    }
    
    
    private javax.swing.JButton selectButton;
    private javax.swing.JButton removeButton;
    private javax.swing.JComboBox schemaComboBox;
    private javax.swing.JLabel srcLabel;
    private javax.swing.JLabel destLabel;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JList sourceList;
    private javax.swing.JList schemaTablesList;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    
    private void initComponents() {
        srcLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        destLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        schemaTablesList = new javax.swing.JList();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jTable1.setAutoscrolls(true);
        selectButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        //jButton3 = new javax.swing.JButton();
        srcLabel.setText(LBL_SOURCE_MSG);
        srcLabel.setName("srcLabel");
        
        jScrollPane1.setViewportView(sourceList);
        
        destLabel.setText(LBL_DEST_MSG);
        destLabel.setName("destLabel");
        
        schemaTablesList.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        schemaTablesList.setName("schemaTables");
        schemaTablesList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if(evt.getClickCount() == 2 && evt.getSource() instanceof JList ) {
                    moveSelectedTables();
                } // end if
            }
        });
        
        jScrollPane2.setViewportView(schemaTablesList);
        
        jLabel3.setText("Selected Tables:");
        jLabel3.setName("selectedTablesLabel");
        
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
                new Object [][] {
        },
                new String [] {
            "Name", "Schema", "User", "Connection"
        }
        ));
        jTable1.setName("selectedTables");
        jScrollPane3.setViewportView(jTable1);
        jScrollPane3.setAutoscrolls(true);
        
        selectButton.setMnemonic('S');
        selectButton.setText("Select");
        selectButton.addActionListener(this);
        
        removeButton.setMnemonic('R');
        removeButton.setText("Remove");
        removeButton.addActionListener(this);
        removeButton.setEnabled(false);
        
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                .add(jLabel3)
                .addContainerGap(519, Short.MAX_VALUE))
                .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE)
                .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(srcLabel)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 195, Short.MAX_VALUE))
                .add(26, 26, 26)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(schemaComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 198, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(destLabel))
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 200, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .add(23, 23, 23)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                //.add(jButton3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(removeButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(selectButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .add(35, 35, 35))))
                );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                .add(srcLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 130, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                .add(destLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(schemaComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                .add(layout.createSequentialGroup()
                //.add(jButton3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 56, Short.MAX_VALUE)
                .add(selectButton))
                .add(jScrollPane2, 0, 0, Short.MAX_VALUE))))
                .add(20, 20, 20)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                .add(layout.createSequentialGroup()
                .add(jLabel3)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 214, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(removeButton))
                .addContainerGap(66, Short.MAX_VALUE))
                );
    }// </editor-fold>
    
    
    private JButton addButton;
    
    
    private boolean isSource;
    
    /* Set <ChangeListeners> */
    private final Set listeners = new HashSet(1);
    
    private ListTransferModel listModel;
    
    /* private JLabel srcLabel;*/
    private ETLCollaborationWizardTablePanel tablePanel;
    
    /**
     * place holder to determine the selected database connection
     */
    private DatabaseConnection selectedConnection = null;
    
    private HashMap<String,DatabaseConnection> nameToConnMap = null;
    
    /** Creates a default instance of ETLCollaborationWizardTransferPanel. */
    public ETLCollaborationWizardTransferPanel() {
    }
    
    /**
     * Creates a new instance of ETLCollaborationWizardTransferPanel using the given
     * ListModels to initially populate the source and destination panels.
     *
     * @param title String to be displayed as title of this panel
     * @param dsList List of DatabaseModels used to populate datasource panel
     * @param destColl Collection of selected DatabaseModels
     * @param sourceOTD true if this panel displays available selections for source OTDs;
     *        false if it displays available destination OTDs
     */
    public ETLCollaborationWizardTransferPanel(String title, List dsList, Collection destColl, boolean sourceOTD) {
        this();
        nameToConnMap = new HashMap<String, DatabaseConnection>();
        if (title != null && title.trim().length() != 0) {
            setName(title);
        }
        
        this.isSource = sourceOTD;
        listModel = new ListTransferModel(dsList, destColl);
        String largestString = listModel.getPrototypeCell();
        
        if (largestString.length() < LBL_SOURCE_MSG.length()) {
            largestString = LBL_SOURCE_MSG;
        } else if (largestString.length() < LBL_DEST_MSG.length()) {
            largestString = LBL_DEST_MSG;
        }
        
        int visibleCt = Math.min(Math.max(MINIMUM_VISIBLE, listModel.getMaximumListSize()), MAXIMUM_VISIBLE);
        sourceList = new JList(listModel.getSourceModel());
        sourceList.addMouseListener(new MouseAdapter() {
            
            public void mouseClicked(MouseEvent e) {
                ((DefaultComboBoxModel) listModel.getDestinationModel()).removeAllElements();
                schemaTablesList.setListData(new Vector());
                DatabaseConnection conn = (DatabaseConnection) listModel
                        .getSourceModel().getElementAt(sourceList.getSelectedIndex());
                // assigning the conn to the placeholder for current selected
                // connection
                selectedConnection = conn;
                DBMetaData meta = new DBMetaData();
                try {
                    ConnectionManager.getDefault().showConnectionDialog(selectedConnection);
                    meta.connectDB(selectedConnection.getJDBCConnection());
                    if(selectedConnection.getDatabaseURL().startsWith("jdbc:axiondb:")) {
                        schemaComboBox.setEnabled(false);
                        populateTableList("");
                    } else if(selectedConnection.getDatabaseURL().startsWith("jdbc:mysql:")) {
                        schemaComboBox.setEnabled(false);
                        populateTableList("");
                    } else  {
                        // Get all Schemas from meta
                        schemaComboBox.setEnabled(true);
                        String[] schemas = meta.getSchemas();
                        listModel.add(schemas);
                        schemaComboBox.setSelectedItem(selectedConnection.getUser().toUpperCase());
                        populateTableList(selectedConnection.getUser().toUpperCase());
                        
                    }
                } catch (Exception ex) {
                    Logger.printThrowable(Logger.INFO, LOG_CATEGORY, null, "Error retrieving schemas" , ex);
                }
            }
        });
        sourceList.addListSelectionListener(this);
        DBModelNameCellRenderer srcRenderer = new DBModelNameCellRenderer(largestString);
        sourceList.setPrototypeCellValue(srcRenderer);
        sourceList.setCellRenderer(srcRenderer);
        sourceList.setVisibleRowCount(visibleCt);
        
        ArrayList testList = new ArrayList();
        tablePanel = new ETLCollaborationWizardTablePanel(testList);
        schemaComboBox = new JComboBox((DefaultComboBoxModel)listModel.getDestinationModel());
        
        schemaComboBox.addActionListener(this);
        initComponents();
    }
    
    private List getSelectedDBModels(boolean isSource) throws Exception {
        Object ffTable = null;
        String tableDisplayString = null;
        String connName = null;
        String ttype = "TABLE";
        HashMap<String, SQLDBModel> nameToDBModelMap = new HashMap<String, SQLDBModel>();
        List dbModels = new ArrayList();
        List tableList = this.listModel.getSelectedTablesList();
        if (tableList != null) {
            for (int i = 0; i < tableList.size(); i++) {
                if (isSource) {
                    ffTable = (SourceTableImpl) tableList.get(i);
                    tableDisplayString = ((SourceTableImpl)ffTable).getQualifiedName();
                    connName = this.listModel.getConnectionNameForTable(tableDisplayString, ((SourceTableImpl)ffTable).getSchema());
                } else {
                    ffTable = (TargetTableImpl) tableList.get(i);
                    tableDisplayString = ((TargetTableImpl)ffTable).getQualifiedName();
                    connName = this.listModel.getConnectionNameForTable(tableDisplayString, ((TargetTableImpl)ffTable).getSchema());
                }
                
                DatabaseConnection conn = this.nameToConnMap.get(connName);
                if( conn != null ) {
                    
                    // Add all tables to database
                    DBMetaData meta = new DBMetaData();
                    try {
                        meta.connectDB(conn.getJDBCConnection());
                        
                        // we are now using SQLDBModel and SQLTable etc
                        SQLDBModel model = nameToDBModelMap.get(connName);
                        if( model == null ) {
                            int type = SQLConstants.TARGET_DBMODEL;
                            if (isSource) {
                                type = SQLConstants.SOURCE_DBMODEL;
                            }
                            model = SQLModelObjectFactory.getInstance().createDBModel(type);
                            populateModel(model, conn, meta);
                            nameToDBModelMap.put(connName, model);
                        }
                        Table t = null;
                        if(ffTable instanceof SourceTableImpl) {
                            t = meta.getTableMetaData(((SourceTableImpl)ffTable).getCatalog(),
                                    ((SourceTableImpl)ffTable).getSchema(), ((SourceTableImpl)ffTable).getName(), ttype);
                        } else {
                            t = meta.getTableMetaData(((TargetTableImpl)ffTable).getCatalog(),
                                    ((TargetTableImpl)ffTable).getSchema(), ((TargetTableImpl)ffTable).getName(), ttype);
                        }
                        meta.checkForeignKeys(t);
                        meta.checkPrimaryKeys(t);
                        
                        TableColumn[] cols = t.getColumns();
                        TableColumn tc = null; 
                        List pks = t.getPrimaryKeyColumnList();
                        List pkCols = new ArrayList();
                        Iterator it = pks.iterator();
                        while(it.hasNext()) {
                            KeyColumn kc = (KeyColumn)it.next();
                            pkCols.add(kc.getColumnName());
                        }
                        if(pks.size()!=0) {
                            PrimaryKeyImpl pkImpl = new PrimaryKeyImpl(((KeyColumn)t.getPrimaryKeyColumnList().get(0)).getName(), pkCols, true);                                                        
                            if(ffTable instanceof SourceTableImpl) {
                                ((SourceTableImpl)ffTable).setPrimaryKey(pkImpl);
                            } else {    
                                ((TargetTableImpl)ffTable).setPrimaryKey(pkImpl);
                            }
                        }                   
                        List fkList = t.getForeignKeyColumnList();
                        it = fkList.iterator();
                        while(it.hasNext()) {
                            ForeignKeyColumn fkCol = (ForeignKeyColumn)it.next();
                            ForeignKeyImpl fkImpl = new ForeignKeyImpl((SQLDBTable)ffTable, fkCol.getName(), fkCol.getImportKeyName(),
                                    fkCol.getImportTableName(), fkCol.getImportSchemaName(), fkCol.getImportCatalogName(), fkCol.getUpdateRule(),
                                    fkCol.getDeleteRule(), fkCol.getDeferrability());                                          
                            List fkColumns = new ArrayList();
                            fkColumns.add(fkCol.getColumnName());
                            String catalog = fkCol.getImportCatalogName();
                            if (catalog == null) {
                                catalog = "";
                            }
                            String schema = fkCol.getImportSchemaName();                            
                            if(schema == null) {
                                schema = "";
                            }
                            pks = meta.getPrimaryKeys(catalog, schema, fkCol.getImportTableName());
                            List pkColumns = new ArrayList();
                            Iterator pksIt = pks.iterator();
                            while(pksIt.hasNext()) {
                                KeyColumn kc = (KeyColumn)pksIt.next();
                                pkColumns.add(kc.getColumnName());
                            }
                            fkImpl.setColumnNames(fkColumns, pkColumns);
                            if(ffTable instanceof SourceTableImpl) {
                                ((SourceTableImpl)ffTable).addForeignKey(fkImpl);
                            } else {
                                ((TargetTableImpl)ffTable).addForeignKey(fkImpl);
                            }
                        }                        
                        for (int j = 0; j < cols.length; j++) {
                            tc = cols[j];
                            if (isSource) {
                                SourceColumnImpl ffColumn = new SourceColumnImpl(tc.getName(), tc
                                        .getSqlTypeCode(), tc.getNumericScale(), tc
                                        .getNumericPrecision(), tc
                                        .getIsPrimaryKey(), tc.getIsForeignKey(),
                                        false /* isIndexed */, tc.getIsNullable());                                
                                ((SourceTableImpl)ffTable).addColumn(ffColumn);
                            } else {
                                TargetColumnImpl ffColumn = new TargetColumnImpl(tc.getName(), tc
                                        .getSqlTypeCode(), tc.getNumericScale(), tc
                                        .getNumericPrecision(), tc
                                        .getIsPrimaryKey(), tc.getIsForeignKey(),
                                        false /* isIndexed */, tc.getIsNullable());                                
                                ((TargetTableImpl)ffTable).addColumn(ffColumn);
                            }
                        }
                        if(ffTable instanceof SourceTableImpl) {
                            ((SourceTableImpl)ffTable).setEditable(true);
                            ((SourceTableImpl)ffTable).setSelected(true);
                            model.addTable((SourceTableImpl)ffTable);
                        } else {
                            ((TargetTableImpl)ffTable).setEditable(true);
                            ((TargetTableImpl)ffTable).setSelected(true);
                            model.addTable((TargetTableImpl)ffTable);
                        }
                    } catch (Exception e) {
                        throw e;
                    }
                }
            }
        }
        Iterator iter = nameToDBModelMap.values().iterator();
        while(iter.hasNext()) {
            SQLDBModel model = (SQLDBModel)iter.next();
            dbModels.add(model);
        }
        return dbModels;
    }
    
    private SQLDBModel populateModel(SQLDBModel model, DatabaseConnection conn, DBMetaData meta) {
        DBConnectionDefinition def = null;
        try {
            def = SQLModelObjectFactory.getInstance().createDBConnectionDefinition(conn.getDisplayName(),
                    meta.getDBType(), conn.getDriverClass(), conn.getDatabaseURL(), conn.getUser(),
                    conn.getPassword(), "Descriptive info here");
        } catch (Exception ex) {
            // ignore
        }
        model.setModelName(conn.getDisplayName());
        model.setConnectionDefinition(def);
        return model;
    }
    
    /**
     * Invoked whenever one of the transfer buttons is clicked.
     *
     * @param e
     *            ActionEvent to handle
     */
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JComboBox) {
            populateTableList((String) schemaComboBox.getSelectedItem());
        }
        
        String cmd = e.getActionCommand();
        if ("Select".equals(cmd)) {
            moveSelectedTables();
        } else if ("Remove".equals(cmd)) {
            int[] indexes = jTable1.getSelectedRows();
            for (int ii = 0; ii < indexes.length; ii++) {
                if ((indexes[ii] >= 0) && (jTable1.getRowCount() >= 1))
                    listModel.removeFromSelectedTables(indexes[ii]);
                for (int j = 0; j < indexes.length; j++) {
                    indexes[j]--;
                }
            }
            
            jTable1.setModel(listModel.getSelectedTablesModel());
            if( listModel.getSelectedTablesModel().getRowCount() < 1 ) {
                this.removeButton.setEnabled(false);
            }
        }
    } //end actionPerformed
    
    private void populateTableList(String schemaName) {
        DBMetaData dbMeta = new DBMetaData();
        try {
            if (ETLCollaborationWizardTransferPanel.this.selectedConnection == null) {
                Logger.print(Logger.INFO, LOG_CATEGORY, null, "selectedConn is null");
            }
            
            if ((selectedConnection != null)) {
                dbMeta.connectDB(selectedConnection.getJDBCConnection());                
                String[][] tableList = dbMeta.getTablesAndViews("", schemaName, "", false);
                SQLDBTable aTable = null;
                String[] currTable = null;
                Vector tableNameList = new Vector();
                if (tableList != null) {
                    for (int i = 0; i < tableList.length; i++) {
                        currTable = tableList[i];
                        if (isSource) {
                            aTable = new SourceTableImpl(currTable[DBMetaData.NAME],
                                    currTable[DBMetaData.SCHEMA], currTable[DBMetaData.CATALOG]);                            
                        } else {
                            aTable = new TargetTableImpl(currTable[DBMetaData.NAME],
                                    currTable[DBMetaData.SCHEMA], currTable[DBMetaData.CATALOG]);
                        }
                        tableNameList.add(aTable);
                    }
                }
                schemaTablesList.setListData(tableNameList);
            }
        } catch (Exception ex) {
            Logger.printThrowable(Logger.INFO, LOG_CATEGORY, null, "Error trying to retrieve tables and views" , ex);
        }
    }
    
    
    private String generateTableAliasName(boolean isSource) {
        int cnt = 1;
        String aliasPrefix = isSource ? "S" : "T";
        String aName = aliasPrefix + cnt;
        while (isTableAliasNameExist(aName)) {
            cnt++;
            aName = aliasPrefix + cnt;
        }
        
        return aName;
    }
    
    private boolean isTableAliasNameExist(String aName) {
        List sTables = this.listModel.getSelectedTablesList();
        Iterator it = sTables.iterator();
        
        while (it.hasNext()) {
            SQLDBTable tTable = (SQLDBTable) it.next();
            String tAlias = tTable.getAliasName();
            if (tAlias != null && tAlias.equals(aName)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     *
     */
    private void moveSelectedTables() {
        // add selected tables from the list to table
        try {
            if (ETLCollaborationWizardTransferPanel.this.selectedConnection == null) {
                Logger.print(Logger.INFO, LOG_CATEGORY, null, "selectedConn is null");
            }
            
            Object[] selectedTables = schemaTablesList.getSelectedValues();
            Object[] clonedTables = new Object[selectedTables.length];
            for(int i=0; i < selectedTables.length ; i++ ) {
                if(isSource) {
                    clonedTables[i] = ((SourceTableImpl)selectedTables[i]).clone();
                } else {
                    clonedTables[i] = ((TargetTableImpl)selectedTables[i]).clone();
                }
            }
            
            String[][] tablesList = new String[selectedTables.length][4];
            if (selectedTables.length > 0) {
                for (int i = 0; i < selectedTables.length; i++) {
                    String tableAliasName = generateTableAliasName(isSource);
                    
                    if (isSource) {
                        ((SourceTableImpl) clonedTables[i]).setAliasName(tableAliasName);
                        tablesList[i][0] = ((SourceTableImpl) clonedTables[i]).getQualifiedName();
                        tablesList[i][1] = ((SourceTableImpl) clonedTables[i]).getSchema();
                        tablesList[i][2] = ((SourceTableImpl) clonedTables[i]).getCatalog();
                    } else {
                        ((TargetTableImpl) clonedTables[i]).setAliasName(tableAliasName);
                        tablesList[i][0] = ((TargetTableImpl) clonedTables[i]).getQualifiedName();
                        tablesList[i][1] = ((TargetTableImpl) clonedTables[i]).getSchema();
                        tablesList[i][2] = ((TargetTableImpl) clonedTables[i]).getCatalog();
                    }
                    tablesList[i][3] = selectedConnection.getName();
                    this.nameToConnMap.put(selectedConnection.getName(), selectedConnection);
                    listModel.addToSelectedTables(tablesList[i], (SQLDBTable) clonedTables[i]);
                }
            }
            
            jTable1.setModel(listModel.getSelectedTablesModel());
            if( listModel.getSelectedTablesModel().getRowCount() > 0 ) {
                this.removeButton.setEnabled(true);
            }
        } catch (Exception ex) {
            Logger.printThrowable(Logger.INFO, LOG_CATEGORY, null,
                    "Error trying to get selected table metadata", ex);
        }
    }
    
    /**
     * @see org.openide.WizardDescriptor.Panel#addChangeListener
     */
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    /**
     * Fires a ChangeEvent to all interested listeners to indicate a state change in one
     * or more UI components.
     */
    public void fireChangeEvent() {
        Iterator it;
        
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(ev);
        }
    }
    
    /**
     * @see org.openide.WizardDescriptor.Panel#getComponent
     */
    public Component getComponent() {
        return this;
    }
    
    /**
     * @see org.openide.WizardDescriptor.Panel#getHelp
     */
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(ETLCollaborationWizardTransferPanel.class);
    }
    
    /**
     * Gets ListTransferModel associated with this object.
     *
     * @return ListTransferModel
     */
    public synchronized ListTransferModel getModel() {
        return listModel;
    }
    
    // TODO add constructor flag to set whether panel is always valid, or
    // if at least one item must be transferred to the destination list.
    /**
     * Indicates whether current state of panel is sufficient for enclosing window/dialog
     * to close itself and read the contents of the source and destination panels.
     *
     * @return true if panel contents are in a valid state, false otherwise.
     */
    public boolean hasValidContents() {
        // Return true if at least one item has been selected.
        return (getModel().getDestinationModel().getSize() > 0);
    }
    
    /**
     * @see org.openide.WizardDescriptor.Panel#isValid
     */
    public boolean isValid() {
        return true;
    }
    
    /**
     * @see java.beans.PropertyChangeListener#propertyChange
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("model")) {
            ((TableModel) evt.getNewValue()).addTableModelListener(this);
        }
    }
    
    /**
     * @see org.openide.WizardDescriptor.Panel#readSettings
     */
    public void readSettings(final Object settings) {
        WizardDescriptor wizard = null;
        if(settings instanceof ETLWizardContext) {
            ETLWizardContext wizardContext = (ETLWizardContext) settings;
            wizard = (WizardDescriptor) wizardContext.getProperty(ETLWizardContext.WIZARD_DESCRIPTOR);
            
        } else if(settings instanceof WizardDescriptor) {
            wizard = (WizardDescriptor) settings;
        }
        
        if (wizard != null &&
                WizardDescriptor.NEXT_OPTION.equals(wizard.getValue())) {
            /*
             * If coming from a previous window (i.e via its Next button)
             * then (re)set the "available" database(s) list and clear the
             * "selected" database list. Otherwise, for a "Back" button call,
             * just keep the current user selections.
             **/
            //TODO - more complicated logic to retain user selections across Back/Next buttons
            List models = null;
            if (isSource) { //In the source database mode
                Object[] sources = (Object[]) wizard.getProperty(ETLCollaborationWizard.DATABASE_SOURCES);
                models = Arrays.asList(sources);
            } else {
                Object[] targets = (Object[]) wizard.getProperty(ETLCollaborationWizard.DATABASE_TARGETS);
                models = Arrays.asList(targets);
            }
            listModel.setSourceList(models);
            ((DefaultComboBoxModel)listModel.getDestinationModel()).removeAllElements();
            tablePanel.resetTable(Collections.EMPTY_LIST);
        }
        
        if (sourceList != null) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (sourceList != null && sourceList.getModel().getSize() != 0) {
                        sourceList.requestFocusInWindow();
                    } else if (addButton != null) {
                        addButton.requestFocusInWindow();
                    }
                }
            });
        }
    }
    
    /**
     * @see org.openide.WizardDescriptor.Panel#removeChangeListener
     */
    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    
    /**
     * @see org.openide.WizardDescriptor.Panel#storeSettings
     */
    public void storeSettings(Object settings) {
        WizardDescriptor wizard = null;
        if(settings instanceof ETLWizardContext) {
            ETLWizardContext wizardContext = (ETLWizardContext) settings;
            wizard = (WizardDescriptor) wizardContext.getProperty(ETLWizardContext.WIZARD_DESCRIPTOR);
            
        } else if(settings instanceof WizardDescriptor) {
            wizard = (WizardDescriptor) settings;
        }
        
        if(wizard != null) {
            final Object selectedOption = wizard.getValue();
            if (NotifyDescriptor.CANCEL_OPTION == selectedOption || NotifyDescriptor.CLOSED_OPTION == selectedOption) {
                return;
            }
            
            boolean isAdvancingPanel = (selectedOption == WizardDescriptor.NEXT_OPTION) || (selectedOption == WizardDescriptor.FINISH_OPTION);
            if (isAdvancingPanel) {
                try {
                    List list = this.getSelectedDBModels(isSource);
                    if (isSource) {//In the source database mode
                        wizard.putProperty(ETLCollaborationWizard.SOURCE_DB, list);
                        // We should null out JOIN_VIEW and JOIN_VIEW_VISIBLE_COLUMNS so that
                        // if user goes to join panel then come back to source table panel and
                        // clicks finish, then we should not have a join.
                        wizard.putProperty(ETLCollaborationWizard.JOIN_VIEW, null);
                        wizard.putProperty(ETLCollaborationWizard.JOIN_VIEW_VISIBLE_COLUMNS, null);
                    } else {
                        wizard.putProperty(ETLCollaborationWizard.TARGET_DB, list);
                    }
                } catch( Exception e) {
                    Logger.printThrowable(Logger.INFO, LOG_CATEGORY, null, "Error trying to get selected table metadata" , e);
                }
            }
        }
    }
    
    /**
     * @see javax.swing.event.TableModelListener#tableChanged
     */
    public void tableChanged(TableModelEvent e) {
        fireChangeEvent();
    }
    
    public void updatePanelState() {
        fireChangeEvent();
    }
    
    /**
     * Called whenever the value of the selection changes.
     *
     * @param e the event that characterizes the change.
     */
    public void valueChanged(ListSelectionEvent e) {
    }
    
    /**
     * Indicates whether this panel has enough selected tables to allow for creation of a
     * join.
     *
     * @return true if number of selected tables is sufficient to create a join; false
     *         otherwise
     */
    boolean hasEnoughTablesForJoin() {
        return listModel.getSelectedTablesModel().getRowCount() > 1 ? true: false;
    }
}

