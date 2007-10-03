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

package org.netbeans.modules.websvc.core.webservices.ui;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Adamek
 */
public class DeleteWsDialog extends javax.swing.JPanel {

    public static final String DELETE_NOTHING = "deleteNothing"; //NOI18N
    public static final String DELETE_ALL = "deleteALL"; //NOI18N
    public static final String DELETE_WS = "deleteWebService"; //NOI18N
    public static final String DELETE_PACKAGE = "deletePackage"; //NOI18N
    public static final String DELETE_WSDL = "deleteWsdl"; //NOI18N
    
    private String wsName, packageName, wsdlName;
    
    private DeleteWsDialog(String wsName, String packageName, String wsdlName) {
        this.wsName = wsName;
        this.packageName=packageName;
        this.wsdlName=wsdlName;
        initComponents();
        // display the delete_wsdl checkbox only if wsdl exists
        if (wsdlName==null) deleteWsdlCheckBox.setVisible(false);
    }

    public static String open(String wsName, String packageName, String wsdlName) {
        String title = NbBundle.getMessage(DeleteWsDialog.class, "MSG_ConfirmDeleteObjectTitle");
        DeleteWsDialog delDialog = new DeleteWsDialog(wsName, packageName, wsdlName);
        NotifyDescriptor desc = new NotifyDescriptor.Confirmation(delDialog, title, NotifyDescriptor.YES_NO_OPTION);
        Object result = DialogDisplayer.getDefault().notify(desc);
        if (result.equals(NotifyDescriptor.CLOSED_OPTION)) {
            return DELETE_NOTHING;
        } else if (result.equals(NotifyDescriptor.NO_OPTION)) {
            return DELETE_NOTHING;
        } else if (delDialog.deletePackage() && delDialog.deleteWsdl()) {
            return DELETE_ALL;
        } else if (delDialog.deletePackage()) {
            return DELETE_PACKAGE;
        } else if (delDialog.deleteWsdl()) {
            return DELETE_WSDL;
        } else return DELETE_WS;
        
    }
    
    private boolean deletePackage() {
        return deletePackageCheckBox.isSelected();
    }
    
    private boolean deleteWsdl() {
        if (wsdlName==null) return false;
        else return deleteWsdlCheckBox.isSelected();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel1 = new javax.swing.JLabel();
        deletePackageCheckBox = new javax.swing.JCheckBox();
        deleteWsdlCheckBox = new javax.swing.JCheckBox();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(DeleteWsDialog.class, "MSG_ConfirmDeleteObject", new Object[] {wsName})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 8, 0);
        add(jLabel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(deletePackageCheckBox, org.openide.util.NbBundle.getMessage(DeleteWsDialog.class, "MSG_DeletePackage", new Object[] {packageName})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(deletePackageCheckBox, gridBagConstraints);

        deleteWsdlCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(deleteWsdlCheckBox, org.openide.util.NbBundle.getMessage(DeleteWsDialog.class, "MSG_DeleteWsdl", new Object[] {wsdlName})); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        add(deleteWsdlCheckBox, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox deletePackageCheckBox;
    private javax.swing.JCheckBox deleteWsdlCheckBox;
    private javax.swing.JLabel jLabel1;
    // End of variables declaration//GEN-END:variables
    
}
