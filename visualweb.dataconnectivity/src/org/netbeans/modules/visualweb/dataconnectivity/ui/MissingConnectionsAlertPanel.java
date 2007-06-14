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

package org.netbeans.modules.visualweb.dataconnectivity.ui;

import javax.swing.JPanel;
import org.openide.util.NbBundle;

/**
 * Broken/missing datasource alert panel.
 *
 * PLEASE NOTE! This is just a temporary solution. BrokenReferencesSupport from
 * the java project support currently does not allow to plug in a check for missing
 * datasources. Once BrokenReferencesSupport will support it, this class should be
 * removed.
 */
public class MissingConnectionsAlertPanel extends JPanel {
    
    public MissingConnectionsAlertPanel() {
        initComponents();
        notAgain.setSelected(!DatasourceUISettings.getDefault().isShowAgainBrokenDatasourceAlert());
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        notAgain = new javax.swing.JCheckBox();
        message = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(notAgain, org.openide.util.NbBundle.getMessage(MissingConnectionsAlertPanel.class, "MSG_Broken_Datasources_Again")); // NOI18N
        notAgain.setMargin(new java.awt.Insets(0, 0, 0, 0));
        notAgain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                notAgainActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 11, 0, 0);
        add(notAgain, gridBagConstraints);
        notAgain.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MissingConnectionsAlertPanel.class, "ACSN_UpdateDatasourcesAlertPanel_notAgain")); // NOI18N
        notAgain.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MissingConnectionsAlertPanel.class, "ACSD_UpdateDatasourcesAlertPanel_notAgain")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(message, org.openide.util.NbBundle.getMessage(MissingConnectionsAlertPanel.class, "MSG_Update_DatasourcesAlertPanel")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 0);
        add(message, gridBagConstraints);
        message.getAccessibleContext().setAccessibleName(NbBundle.getMessage(MissingConnectionsAlertPanel.class, "ACSN_UpdateDatasourcesAlertPanel")); // NOI18N
        message.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MissingConnectionsAlertPanel.class, "ACSD_UpdateDatasourcesAlertPanel")); // NOI18N

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(MissingConnectionsAlertPanel.class, "ACSN_UpdateDatasourcesAlertPanel")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(MissingConnectionsAlertPanel.class, "ACSD_UpdateDatasourcesAlertPanel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
    private void notAgainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_notAgainActionPerformed
        DatasourceUISettings.getDefault().setShowAgainBrokenDatasourceAlert(!notAgain.isSelected());
    }//GEN-LAST:event_notAgainActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel message;
    private javax.swing.JCheckBox notAgain;
    // End of variables declaration//GEN-END:variables
    
}
