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
package org.netbeans.modules.j2ee.sun.ide.j2ee.ui;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.NbBundle;

public final class AddInstanceVisualNamePasswordPanel extends JPanel {

    /**
     * Creates new form AddInstanceVisualNamePasswordPanel
     */
    public AddInstanceVisualNamePasswordPanel() {
        initComponents();
        DocumentListener l = new MyDocListener();
        adminName.getDocument().addDocumentListener(l);
        adminPassword.getDocument().addDocumentListener(l);
    }

    public String getName() {
        return NbBundle.getMessage(AddInstanceVisualNamePasswordPanel.class, 
                "StepName_EnterAdminLoginInfo");                                // NOI18N
    }
    
    void setUName(String uname) {
        adminName.setText(uname);
    }
    
    String getUName() {
        return adminName.getText();
    }
    
    void setPWord(String pw) {
        adminPassword.setText(pw);
    }

    String getPWord() {
        return new String(adminPassword.getPassword());
    }
    
    // Event Handler
    //
    private Set/*<ChangeListener.*/ listenrs = new HashSet/*<Changelisteners.*/();

    void addChangeListener(ChangeListener l) {
        synchronized (listenrs) {
            listenrs.add(l);
        }
    }
    
    void removeChangeListener(ChangeListener l ) {
        synchronized (listenrs) {
            listenrs.remove(l);
        }
    }

    private void fireChangeEvent() {
        Iterator it;
        synchronized (listenrs) {
            it = new HashSet(listenrs).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged (ev);
        }
    }
    
    class MyDocListener implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            fireChangeEvent();
        }

        public void removeUpdate(DocumentEvent e) {
            fireChangeEvent();
        }

        public void changedUpdate(DocumentEvent e) {
            fireChangeEvent();
        }
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        description = new javax.swing.JLabel();
        adminNameLabel = new javax.swing.JLabel();
        adminName = new javax.swing.JTextField();
        adminPasswordLabel = new javax.swing.JLabel();
        adminPassword = new javax.swing.JPasswordField();
        warning = new javax.swing.JLabel();
        spacingHack = new javax.swing.JLabel();

        jLabel1.setText("jLabel1");

        setLayout(new java.awt.GridBagLayout());

        description.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("TXT_namePasswordDescription"));
        description.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 5, 0);
        add(description, gridBagConstraints);

        adminNameLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(AddInstanceVisualNamePasswordPanel.class, "MNM_adminNameLabel").charAt(0));
        adminNameLabel.setLabelFor(adminName);
        adminNameLabel.setText(org.openide.util.NbBundle.getMessage(AddInstanceVisualNamePasswordPanel.class, "LBL_adminNameLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 5, 6);
        add(adminNameLabel, gridBagConstraints);

        adminName.setText(org.openide.util.NbBundle.getMessage(AddInstanceVisualNamePasswordPanel.class, "VAL_adminName_NOI18N"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(adminName, gridBagConstraints);
        adminName.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("DSC_adminName"));

        adminPasswordLabel.setDisplayedMnemonic(org.openide.util.NbBundle.getMessage(AddInstanceVisualNamePasswordPanel.class, "MNM_adminPasswordLabel").charAt(0));
        adminPasswordLabel.setLabelFor(adminPassword);
        adminPasswordLabel.setText(org.openide.util.NbBundle.getMessage(AddInstanceVisualNamePasswordPanel.class, "LBL_adminPasswordLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 5, 6);
        add(adminPasswordLabel, gridBagConstraints);

        adminPassword.setColumns(10);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(adminPassword, gridBagConstraints);
        adminPassword.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("DSC_adminPassword"));

        warning.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/sun/ide/j2ee/ui/Bundle").getString("TXT_namePasswordWarning"));
        warning.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(warning, gridBagConstraints);

        spacingHack.setEnabled(false);
        spacingHack.setFocusable(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.weighty = 1.0;
        add(spacingHack, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField adminName;
    private javax.swing.JLabel adminNameLabel;
    private javax.swing.JPasswordField adminPassword;
    private javax.swing.JLabel adminPasswordLabel;
    private javax.swing.JLabel description;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel spacingHack;
    private javax.swing.JLabel warning;
    // End of variables declaration//GEN-END:variables

}

