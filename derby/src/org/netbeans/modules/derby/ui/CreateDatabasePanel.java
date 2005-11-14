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

package org.netbeans.modules.derby.ui;

import java.awt.Color;
import java.io.File;
import java.text.MessageFormat;
import javax.swing.UIManager;
import javax.swing.event.DocumentListener;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;
/**
 *
 * @author Andrei Badea
 */
public class CreateDatabasePanel extends javax.swing.JPanel {
    
    private File derbySystemHome;
    private DialogDescriptor descriptor;
    private Color nbErrorForeground;
    
    private DocumentListener docListener = new DocumentListener() {
        
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            validateDatabaseName();
        }

        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            validateDatabaseName();
        }

        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            validateDatabaseName();
        }
    };
    
    public CreateDatabasePanel(String derbySystemHome) {
        // copied from WizardDescriptor
        nbErrorForeground = UIManager.getColor("nb.errorForeground"); //NOI18N
        if (nbErrorForeground == null) {
            //nbErrorForeground = new Color(89, 79, 191); // RGB suggested by Bruce in #28466
            nbErrorForeground = new Color(255, 0, 0); // RGB suggested by jdinga in #65358
        }
        
        this.derbySystemHome = new File(derbySystemHome);
        
        initComponents();
        databaseNameTextField.getDocument().addDocumentListener(docListener);
    }
    
    public void setDialogDescriptor(DialogDescriptor descriptor) {
        this.descriptor = descriptor;
        validateDatabaseName();
    }

    public String getDatabaseName() {
        return databaseNameTextField.getText().trim();
    }
    
    private void validateDatabaseName() {
        if (descriptor == null) {
            return;
        }
        
        String error = null;
        String databaseName = getDatabaseName();
        
        if (databaseName.length() <= 0) { // NOI18N
            error = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DatabaseNameEmpty");
        } else if (databaseName.length() > 0 && new File(derbySystemHome, databaseName).exists()) { // NOI18N
            error = NbBundle.getMessage(CreateDatabasePanel.class, "ERR_DatabaseDirectoryExists");
            error = MessageFormat.format(error, new Object[] { databaseName });
        }
        
        if (error != null) {
            messageLabel.setText(error);
            descriptor.setValid(false);
        } else {
            messageLabel.setText(" "); // NOI18N
            descriptor.setValid(true);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        databaseNameLabel = new javax.swing.JLabel();
        databaseNameTextField = new javax.swing.JTextField();
        messageLabel = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(databaseNameLabel, org.openide.util.NbBundle.getMessage(CreateDatabasePanel.class, "LBL_DatabaseName"));

        messageLabel.setForeground(nbErrorForeground);
        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, " ");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                        .add(databaseNameLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(databaseNameTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE))
                    .add(messageLabel))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(databaseNameLabel)
                    .add(databaseNameTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 38, Short.MAX_VALUE)
                .add(messageLabel)
                .addContainerGap())
        );
    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel databaseNameLabel;
    public javax.swing.JTextField databaseNameTextField;
    public javax.swing.JLabel messageLabel;
    // End of variables declaration//GEN-END:variables
    
}
