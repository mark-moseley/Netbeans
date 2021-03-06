/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.ejbjarproject.ui.customizer;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class CustomizerRun extends javax.swing.JPanel implements HelpCtx.Provider {
    private static final long serialVersionUID = 168604540122121876L;
    private Object initialJ2eeSpecVersion;

    /** Creates new form CustomizerRun */
    public CustomizerRun( EjbJarProjectProperties uiProperties ) {
        initComponents();

        jComboBoxJ2eePlatform.setModel (uiProperties.J2EE_SERVER_INSTANCE_MODEL );
        jComboBoxJ2eeSpecVersion.setModel (uiProperties.J2EE_PLATFORM_MODEL );
        
        initialJ2eeSpecVersion = uiProperties.J2EE_PLATFORM_MODEL.getSelectedItem();
    }
    
    private void checkJ2eePlatformSpecMatch() {
        Object j2eePlatform = jComboBoxJ2eePlatform.getSelectedItem();
        Object j2eeSpecVersion = jComboBoxJ2eeSpecVersion.getSelectedItem();
        boolean match = true;
        
        if (j2eePlatform != null && j2eeSpecVersion != null) {
            match = J2eePlatformUiSupport.getJ2eePlatformAndSpecVersionMatch(j2eePlatform, j2eeSpecVersion);
        }
        
        jLabelJ2eePlatformSpecMismatch.setText(match ? "" : NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_PlatformSpecMismatch_JLabel"));
    }
    
    private void checkJ2eeSpecVersionChanged() {
        boolean changed = !jComboBoxJ2eeSpecVersion.getSelectedItem().equals(initialJ2eeSpecVersion);
        jLabelWarnDdChange.setText(changed ? NbBundle.getMessage(CustomizerRun.class, "LBL_CustomizeRun_Run_WardDdChange_JLabel") : "");
    }
    
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizerRun.class);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabelJ2eePlatform = new javax.swing.JLabel();
        jComboBoxJ2eePlatform = new javax.swing.JComboBox();
        jLabelJ2eeVersion = new javax.swing.JLabel();
        jComboBoxJ2eeSpecVersion = new javax.swing.JComboBox();
        jPanel1 = new javax.swing.JPanel();
        jLabelJ2eePlatformSpecMismatch = new javax.swing.JLabel();
        jLabelWarnDdChange = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jLabelJ2eePlatform.setLabelFor(jComboBoxJ2eePlatform);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelJ2eePlatform, org.openide.util.NbBundle.getBundle(CustomizerRun.class).getString("LBL_CustomizeRun_Run_Server_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        add(jLabelJ2eePlatform, gridBagConstraints);

        jComboBoxJ2eePlatform.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxJ2eePlatformItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(jComboBoxJ2eePlatform, gridBagConstraints);
        jComboBoxJ2eePlatform.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getBundle(CustomizerRun.class).getString("AD_jComboBoxServer"));

        jLabelJ2eeVersion.setLabelFor(jComboBoxJ2eeSpecVersion);
        org.openide.awt.Mnemonics.setLocalizedText(jLabelJ2eeVersion, org.openide.util.NbBundle.getBundle(CustomizerRun.class).getString("LBL_CustomizeRun_Run_J2EEVersion_JLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 12);
        add(jLabelJ2eeVersion, gridBagConstraints);

        jComboBoxJ2eeSpecVersion.setEnabled(false);
        jComboBoxJ2eeSpecVersion.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxJ2eeSpecVersionItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jComboBoxJ2eeSpecVersion, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);

        jLabelJ2eePlatformSpecMismatch.setForeground(new java.awt.Color(89, 71, 191));
        org.openide.awt.Mnemonics.setLocalizedText(jLabelJ2eePlatformSpecMismatch, " ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jLabelJ2eePlatformSpecMismatch, gridBagConstraints);

        jLabelWarnDdChange.setForeground(new java.awt.Color(89, 71, 191));
        org.openide.awt.Mnemonics.setLocalizedText(jLabelWarnDdChange, " ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(jLabelWarnDdChange, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxJ2eeSpecVersionItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxJ2eeSpecVersionItemStateChanged
        //checkJ2eePlatformSpecMatch();
        checkJ2eeSpecVersionChanged();
    }//GEN-LAST:event_jComboBoxJ2eeSpecVersionItemStateChanged

    private void jComboBoxJ2eePlatformItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxJ2eePlatformItemStateChanged
        //checkJ2eePlatformSpecMatch();
    }//GEN-LAST:event_jComboBoxJ2eePlatformItemStateChanged
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBoxJ2eePlatform;
    private javax.swing.JComboBox jComboBoxJ2eeSpecVersion;
    private javax.swing.JLabel jLabelJ2eePlatform;
    private javax.swing.JLabel jLabelJ2eePlatformSpecMismatch;
    private javax.swing.JLabel jLabelJ2eeVersion;
    private javax.swing.JLabel jLabelWarnDdChange;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
    
}
