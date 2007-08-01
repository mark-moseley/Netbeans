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

/*
 * SonyEricssonCustomizerPanel.java
 *
 * Created on 19. kveten 2005, 14:19
 */
package org.netbeans.modules.mobility.deployment.sonyericsson;

import java.util.Collection;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  Adam Sotona
 */
public class SonyEricssonCustomizerPanel extends javax.swing.JPanel {
    
    /** Creates new form SonyEricssonCustomizerPanel */
    public SonyEricssonCustomizerPanel() {
        initComponents();
        if (!Utilities.isWindows()) {
            remove(jButton1);
            lError.setText(NbBundle.getMessage(SonyEricssonCustomizerPanel.class, "ERR_WindowsOnly"));
        } else testSDK();
    }
    
    private void testSDK() {
        final JavaPlatform p[] = JavaPlatformManager.getDefault().getInstalledPlatforms();
        for (JavaPlatform jp : p ) {
            for ( final FileObject fo : (Collection<FileObject>)jp.getInstallFolders() ) {
                if (fo.getFileObject("bin/ejava.exe") != null) { //NOI18N
                    setVisible(false);
                    return;
                }
            }
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

        lError = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        lError.setForeground(new java.awt.Color(89, 79, 191));
        org.openide.awt.Mnemonics.setLocalizedText(lError, NbBundle.getMessage(SonyEricssonCustomizerPanel.class, "ERR_MissingSDK")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(lError, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jButton1, NbBundle.getMessage(SonyEricssonCustomizerPanel.class, "LBL_ManageEmulators")); // NOI18N
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 11, 0, 0);
        add(jButton1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        PlatformsCustomizer.showCustomizer(null);
        testSDK();
    }//GEN-LAST:event_jButton1ActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel lError;
    // End of variables declaration//GEN-END:variables
    
}
