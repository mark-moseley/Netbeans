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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.versioning.system.cvss.ui.actions.tag;

import org.netbeans.modules.versioning.system.cvss.CvsModuleConfig;
import org.netbeans.modules.versioning.system.cvss.ui.selectors.BranchSelector;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.lib.cvsclient.CVSRoot;

import java.io.File;
import java.io.IOException;
import org.openide.util.*;

/**
 * Settings panel for the Switch To Branch action.
 * 
 * @author Maros Sandor
 */
class SwitchBranchPanel extends javax.swing.JPanel {
    
    private final File[] roots;

    /** Creates new form SwitchBranchPanel */
    public SwitchBranchPanel(File [] roots) {
        this.roots = roots;
        initComponents();
        rbSwitchToTrunk.setSelected(CvsModuleConfig.getDefault().getPreferences().getBoolean("SwitchBranchSettings.switchToTrunk", true)); // NOI18N
        rbSwitchToBranch.setSelected(CvsModuleConfig.getDefault().getPreferences().getBoolean("SwitchBranchSettings.switchToBranch", false)); // NOI18N
        tfBranchName.setText(CvsModuleConfig.getDefault().getPreferences().get("SwitchBranchSettings.branchName", NbBundle.getMessage(SwitchBranchPanel.class, "BK2001"))); // NOI18N
        refreshComponents();
    }

    public void saveSettings() {
        CvsModuleConfig.getDefault().getPreferences().putBoolean("SwitchBranchSettings.switchToTrunk", rbSwitchToTrunk.isSelected()); // NOI18N
        CvsModuleConfig.getDefault().getPreferences().putBoolean("SwitchBranchSettings.switchToBranch", rbSwitchToBranch.isSelected()); // NOI18N
        CvsModuleConfig.getDefault().getPreferences().put("SwitchBranchSettings.branchName", tfBranchName.getText()); // NOI18N
    }

    private void refreshComponents() {
        if (rbSwitchToTrunk.isSelected()) {
            tfBranchName.setEnabled(false);
            bBrowse.setEnabled(false);
        } else {
            tfBranchName.setEnabled(true);
            bBrowse.setEnabled(true);
        }
    }
    
    public String getBranchName() {
        return tfBranchName.getText();
    }

    public boolean isSwitchToTrunk() {
        return rbSwitchToTrunk.isSelected();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        bgSwitchRadio = new javax.swing.ButtonGroup();
        rbSwitchToTrunk = new javax.swing.JRadioButton();
        rbSwitchToBranch = new javax.swing.JRadioButton();
        tfBranchName = new javax.swing.JTextField();
        bBrowse = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 0, 11));
        bgSwitchRadio.add(rbSwitchToTrunk);
        org.openide.awt.Mnemonics.setLocalizedText(rbSwitchToTrunk, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/tag/Bundle").getString("CTL_SwitchBranchForm_ToTrunk"));
        rbSwitchToTrunk.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/tag/Bundle").getString("TT_SwitchBranchForm_ToTrunk"));
        rbSwitchToTrunk.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 2, 0));
        rbSwitchToTrunk.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rbSwitchToTrunk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioSwitch(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(rbSwitchToTrunk, gridBagConstraints);

        bgSwitchRadio.add(rbSwitchToBranch);
        org.openide.awt.Mnemonics.setLocalizedText(rbSwitchToBranch, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/tag/Bundle").getString("CTL_SwitchBranchForm_ToBranch"));
        rbSwitchToBranch.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/tag/Bundle").getString("TT_SwitchBranchForm_ToBranch"));
        rbSwitchToBranch.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        rbSwitchToBranch.setMargin(new java.awt.Insets(0, 0, 0, 0));
        rbSwitchToBranch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioSwitch(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(rbSwitchToBranch, gridBagConstraints);

        tfBranchName.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        add(tfBranchName, gridBagConstraints);
        tfBranchName.getAccessibleContext().setAccessibleName(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/tag/Bundle").getString("ACSN_SwitchBranchForm_BranchName"));
        tfBranchName.getAccessibleContext().setAccessibleDescription(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/tag/Bundle").getString("ACSD_SwitchBranchForm_BranchName"));

        org.openide.awt.Mnemonics.setLocalizedText(bBrowse, java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/tag/Bundle").getString("CTL_SwitchBranchForm_BrowseBranch"));
        bBrowse.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/versioning/system/cvss/ui/actions/tag/Bundle").getString("TT_SwitchBranchForm_BrowseBranch"));
        bBrowse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseBranches(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        add(bBrowse, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void browseBranches(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseBranches
        for (int i = 0; i < roots.length; i++) {
            try {
                CVSRoot.parse(Utils.getCVSRootFor(roots[i]));  // raises exception
                BranchSelector selector = new BranchSelector();
                String tag = selector.selectTag(roots[i]);
                if (tag != null) {
                    tfBranchName.setText(tag);
                }
                return;
            } catch (IOException e) {
                // no root for this file, try next
            }
        }
    }//GEN-LAST:event_browseBranches

    private void radioSwitch(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioSwitch
        refreshComponents();
    }//GEN-LAST:event_radioSwitch
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bBrowse;
    private javax.swing.ButtonGroup bgSwitchRadio;
    private javax.swing.JRadioButton rbSwitchToBranch;
    private javax.swing.JRadioButton rbSwitchToTrunk;
    private javax.swing.JTextField tfBranchName;
    // End of variables declaration//GEN-END:variables
    
}
