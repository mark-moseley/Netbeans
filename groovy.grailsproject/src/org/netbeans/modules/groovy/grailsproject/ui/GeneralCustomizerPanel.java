/*
 * GeneralCustomizerPanel.java
 *
 * Created on September 28, 2007, 3:19 PM
 */

package org.netbeans.modules.groovy.grailsproject.ui;

import org.netbeans.api.project.Project;

/**
 *
 * @author  schmidtm
 */
public class GeneralCustomizerPanel extends javax.swing.JPanel {
    Project project;
    
    /** Creates new form GeneralCustomizerPanel */
    public GeneralCustomizerPanel(Project project) {
        this.project = project;
        initComponents();
        grailsEnvChooser.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Development", "Production", "Test" }));
        projectFolderTextField.setText("/" + project.getProjectDirectory().getPath());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        grailsEnvChooser = new javax.swing.JComboBox();
        projectFolderLabel = new javax.swing.JLabel();
        projectFolderTextField = new javax.swing.JTextField();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(GeneralCustomizerPanel.class, "GeneralCustomizerPanel.jLabel1.text")); // NOI18N

        grailsEnvChooser.setMaximumRowCount(3);
        grailsEnvChooser.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        grailsEnvChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                grailsEnvChooserActionPerformed(evt);
            }
        });

        projectFolderLabel.setText(org.openide.util.NbBundle.getMessage(GeneralCustomizerPanel.class, "GeneralCustomizerPanel.projectFolderLabel.text")); // NOI18N

        projectFolderTextField.setEditable(false);
        projectFolderTextField.setText(org.openide.util.NbBundle.getMessage(GeneralCustomizerPanel.class, "GeneralCustomizerPanel.projectFolderTextField.text")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(25, 25, 25)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 141, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(projectFolderLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(grailsEnvChooser, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 117, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(projectFolderTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 210, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(28, 28, 28)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(projectFolderLabel)
                    .add(projectFolderTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(31, 31, 31)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(grailsEnvChooser, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(jLabel1))
                .addContainerGap(200, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void grailsEnvChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_grailsEnvChooserActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_grailsEnvChooserActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox grailsEnvChooser;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel projectFolderLabel;
    private javax.swing.JTextField projectFolderTextField;
    // End of variables declaration//GEN-END:variables
    
}
