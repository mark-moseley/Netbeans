/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.iep.editor.wizard.database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;

import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.editor.xsd.SchemaArtifactTreeCellEditor;
import org.netbeans.modules.iep.editor.xsd.SchemaArtifactTreeCellRenderer;
import org.openide.util.NbBundle;

public final class DatabaseTableSelectionVisualPanel3 extends JPanel {

    private List<ColumnInfo> mExistingColumnNames = new ArrayList<ColumnInfo>();
    
    /** Creates new form DatabaseTableSelectionVisualPanel3 */
    public DatabaseTableSelectionVisualPanel3() {
        initComponents();
        init();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(DatabaseTableSelectionVisualPanel3.class, "DatabaseTableSelectionVisualPanel3_SPECIFY_POLLING_CONFIGURATION");
    }

    private void init() {
        DefaultComboBoxModel model = new DefaultComboBoxModel(new Vector(DatabaseTableWizardConstants.getTimeUnitInfos()));
        pollingTimeUnitComboBox.setModel(model);
        pollingTimeUnitComboBox.setSelectedItem(DatabaseTableWizardConstants.TIMEUNIT_SECOND);
        pollingIntervalTextField.setText("1");
        recordsToPollTextField.setText("1");
        jndiNameTextField.setText(SharedConstants.DEFAULT_JNDINAME);
    }
    
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        deleteRecordsCheckBox = new javax.swing.JCheckBox();
        recordsToPollTextField = new javax.swing.JTextField();
        pollingIntervalTextField = new javax.swing.JTextField();
        pollingTimeUnitComboBox = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jndiNameTextField = new javax.swing.JTextField();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Polling Configuration"));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, "Select column(s) which uniquely identify a record and increasing in value (ex: system timestamp)");

        jScrollPane1.setViewportView(jTree1);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, "Interval:"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, "Record Size:"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, "Delete Records:"); // NOI18N

        deleteRecordsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteRecordsCheckBoxActionPerformed(evt);
            }
        });

        pollingTimeUnitComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        pollingTimeUnitComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pollingTimeUnitComboBoxActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel4)
                    .add(jLabel2)
                    .add(jLabel1))
                .add(6, 6, 6)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(deleteRecordsCheckBox)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pollingIntervalTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 195, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pollingTimeUnitComboBox, 0, 177, Short.MAX_VALUE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, recordsToPollTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(pollingTimeUnitComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1)
                    .add(pollingIntervalTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(recordsToPollTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel2))
                .add(4, 4, 4)
                .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel4)
                    .add(deleteRecordsCheckBox))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel5)
                    .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 461, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jLabel5)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 119, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Database Configuration"));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, "JNDI Name:"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jLabel3)
                .add(37, 37, 37)
                .add(jndiNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 377, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel3)
                    .add(jndiNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    public void setSelectedTables(List<TableInfo> tables) {
    	if(tables == null) {
    		return;
    	}
    	
    	updateExistColumnList(tables);
        DBArtifactTreeModel model = new DBArtifactTreeModel(new DefaultMutableTreeNode("root"), tables, this.mExistingColumnNames);
        this.jTree1.setModel(model);
        this.jTree1.setRootVisible(false);
        TreeCellRenderer renderer = new SchemaArtifactTreeCellRenderer();
        TreeCellEditor editor = new SchemaArtifactTreeCellEditor(jTree1, renderer, this.mExistingColumnNames);
        this.jTree1.setCellRenderer(renderer);
        this.jTree1.setCellEditor(editor);
        this.jTree1.setEditable(true);
        
        final List<TableNode> tableNodes = model.getTableNodes();
        
        Runnable r = new Runnable() {
        	public void run() {
                    Iterator<TableNode> it = tableNodes.iterator();
                
                    while(it.hasNext()) {
                            TableNode node = it.next();
                            TreePath path = new TreePath(node.getPath());
                            jTree1.expandPath(path);
                    }
        		
        	}
        };
        
        
        SwingUtilities.invokeLater(r);
    }
    
    public List<ColumnInfo> getSelectedColumns() {
    	return this.mExistingColumnNames;
    }
    
    public String getPollingInterval() {
        return pollingIntervalTextField.getText();
    }
    
    public String getPollingRecordSize() {
        return recordsToPollTextField.getText();
    }
    
    public String getPollingTimeUnit() {
        DatabaseTableWizardConstants.TimeUnitInfo selectedUnit = (DatabaseTableWizardConstants.TimeUnitInfo) pollingTimeUnitComboBox.getSelectedItem();
        String unit = selectedUnit.getCodeName();
        
        return unit;
    }
    
    public String getJNDIName() {
        return jndiNameTextField.getText();
    }
    
    public boolean isDeleteRecords() {
        return deleteRecordsCheckBox.isSelected();
    }
    
 private void updateExistColumnList(List<TableInfo> tables) {
    	List<ColumnInfo> validExistingColumns = new ArrayList<ColumnInfo>();
    	
    	Iterator<TableInfo> tIt = tables.iterator();
    	while(tIt.hasNext()) {
    		TableInfo table = tIt.next();
    		List<ColumnInfo> columns = table.getColumns();
    		if(columns != null) {
    			Iterator<ColumnInfo> it = columns.iterator();
    	    	while(it.hasNext()) {
    	    		ColumnInfo column = it.next();
    	    		if(this.mExistingColumnNames.contains(column)) {
    	    			validExistingColumns.add(column);
    	    		}
    	    	}
    		}
    		
    	}
    	
    	
    	
    	//keep old valid columns
    	this.mExistingColumnNames = validExistingColumns;
    	
    }
    
 
private void pollingTimeUnitComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pollingTimeUnitComboBoxActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_pollingTimeUnitComboBoxActionPerformed

private void deleteRecordsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteRecordsCheckBoxActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_deleteRecordsCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox deleteRecordsCheckBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTree jTree1;
    private javax.swing.JTextField jndiNameTextField;
    private javax.swing.JTextField pollingIntervalTextField;
    private javax.swing.JComboBox pollingTimeUnitComboBox;
    private javax.swing.JTextField recordsToPollTextField;
    // End of variables declaration//GEN-END:variables
}

