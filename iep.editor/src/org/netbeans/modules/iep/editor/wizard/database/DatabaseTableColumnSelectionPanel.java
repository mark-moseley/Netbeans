/*
 * DatabaseTableColumnSelectionPanel.java
 *
 * Created on April 15, 2008, 6:38 PM
 */

package org.netbeans.modules.iep.editor.wizard.database;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;

import javax.swing.tree.TreePath;
import org.netbeans.modules.iep.editor.xsd.SchemaArtifactTreeCellEditor;
import org.netbeans.modules.iep.editor.xsd.SchemaArtifactTreeCellRenderer;

/**
 *
 * @author  radval
 */
public class DatabaseTableColumnSelectionPanel extends javax.swing.JPanel {

    private List<ColumnInfo> mExistingColumnNames = new ArrayList<ColumnInfo>();

    
    /** Creates new form DatabaseTableColumnSelectionPanel */
    public DatabaseTableColumnSelectionPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(DatabaseTableColumnSelectionPanel.class, "DatabaseTableColumnSelectionPanel.jLabel1.text")); // NOI18N

        jScrollPane2.setViewportView(jTree1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(jLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                .addContainerGap())
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
    
    
    private void updateExistColumnList(List<TableInfo> tables) {
    	
    	List<ColumnInfo> invalidExistingColumns = new ArrayList<ColumnInfo>();
    	
    	Iterator<ColumnInfo> it = this.mExistingColumnNames.iterator();
    	while(it.hasNext()) {
    		ColumnInfo column = it.next();
    		TableInfo table = column.getTable();
    		if(!tables.contains(table)) {
    			invalidExistingColumns.add(column);
    		}
    	}
    	
    	//remove old invalid columns
    	this.mExistingColumnNames.removeAll(invalidExistingColumns);
    	
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables

}
