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

package org.netbeans.modules.j2ee.archive.customizer;

import java.util.ArrayList;
import javax.swing.JPanel;
import org.netbeans.modules.j2ee.archive.project.ArchiveProjectProperties;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public final class CustomizerRun extends JPanel implements ArchiveCustomizerPanel, HelpCtx.Provider {
    
    // Helper for storing properties
    private transient final VisualPropertySupport vps;
    
    private String[] serverInstanceIDs;
    private String[] serverNames;
    boolean initialized = false;
    
        
    public CustomizerRun(final ArchiveProjectProperties apProperties) {
        initComponents();
        this.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(CustomizerRun.class, "ACS_CustomizeRun_A11YDesc")); // NOI18N
        vps = new VisualPropertySupport(apProperties);
    }
    
    
    public void initValues() {
        initialized = false;
        initServerInstances();
        
        vps.register(jComboBoxServer, serverNames, serverInstanceIDs, ArchiveProjectProperties.J2EE_SERVER_INSTANCE);
        
        initialized = true;
    }
    
    
        
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelServer = new javax.swing.JLabel();
        jComboBoxServer = new javax.swing.JComboBox();
        filler = new javax.swing.JLabel();

        jLabelServer.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/archive/customizer/Bundle").getString("MNM_Server_Label").charAt(0));
        jLabelServer.setLabelFor(jComboBoxServer);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelServer, NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Server_JLabel"));

        jComboBoxServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxServerActionPerformed(evt);
            }
        });

        jComboBoxServer.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/j2ee/archive/customizer/Bundle").getString("ACS_CustomizeRun_Server_A11YDesc"));

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(filler)
                    .add(layout.createSequentialGroup()
                        .add(jLabelServer)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jComboBoxServer, 0, 203, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(filler)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(jLabelServer)
                        .add(jComboBoxServer, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(21, 21, 21))
        );
    }// </editor-fold>//GEN-END:initComponents
    
    private void jComboBoxServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxServerActionPerformed

    }//GEN-LAST:event_jComboBoxServerActionPerformed
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel filler;
    private javax.swing.JComboBox jComboBoxServer;
    private javax.swing.JLabel jLabelServer;
    // End of variables declaration//GEN-END:variables
    
    /** Help context where to find more about the paste type action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerRun.class);
    }
    
    private void initServerInstances() {
        String[] servInstIDs = Deployment.getDefault().getServerInstanceIDs();
        java.util.List servInstIDsList = new ArrayList();
        java.util.List servNamesList = new ArrayList();
        Deployment deployment = Deployment.getDefault();
        for (int i = 0; i < servInstIDs.length; i++) {
            String instanceID = servInstIDs[i];
            J2eePlatform j2eePlat = deployment.getJ2eePlatform(instanceID);
            String servInstDisplayName = Deployment.getDefault().getServerInstanceDisplayName(servInstIDs[i]);
            if (servInstDisplayName != null
                    && j2eePlat != null 
                    && j2eePlat.getSupportedModuleTypes().contains(J2eeModule.WAR))
                   /* && j2eePlat.getSupportedSpecVersions().contains(J2eeModule.JAVA_EE_50)) */{ //see #74597
                servInstIDsList.add(instanceID);
                servNamesList.add(servInstDisplayName);
            }
        }
        serverInstanceIDs = (String[]) servInstIDsList.toArray(new String[servInstIDsList.size()]);
        serverNames = (String[]) servNamesList.toArray(new String[servNamesList.size()]);
    }
    
}
