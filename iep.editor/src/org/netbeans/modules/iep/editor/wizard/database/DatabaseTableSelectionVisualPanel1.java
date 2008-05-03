/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.iep.editor.wizard.database;

import java.awt.BorderLayout;
import java.sql.Connection;
import java.util.List;
import javax.swing.JPanel;

import org.openide.util.NbBundle;

public final class DatabaseTableSelectionVisualPanel1 extends JPanel {

    private DatabaseTableSelectionPanel mDBTableSelectionPanel;
    
    /** Creates new form DatabaseTableSelectionVisualPanel1 */
    public DatabaseTableSelectionVisualPanel1() {
        initComponents();
        init();
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(DatabaseTableSelectionVisualPanel1.class, "DatabaseTableSelectionVisualPanel1_SELECT_TABLES");
    }

    private void init() {
        this.setLayout(new BorderLayout());
        mDBTableSelectionPanel = new DatabaseTableSelectionPanel();
        this.add(mDBTableSelectionPanel, BorderLayout.CENTER);
    }
    
    public List<TableInfo> getSelectedTables() {
        return mDBTableSelectionPanel.getSelectedTables();
    }
    
    public Connection getSelectedConnection() {
    	return mDBTableSelectionPanel.getSelectedConnection();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}

