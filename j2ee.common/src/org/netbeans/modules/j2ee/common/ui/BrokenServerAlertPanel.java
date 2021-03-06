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

package org.netbeans.modules.j2ee.common.ui;

import javax.swing.JPanel;
import org.openide.util.NbBundle;

/**
 * Broken/missing server alert panel.
 *
 * PLEASE NOTE! This is just a temporary solution. BrokenReferencesSupport from
 * the java project support currently does not allow to plug in a check for missing
 * servers. Once BrokenReferencesSupport will support it, this class should be
 * removed.
 */
public class BrokenServerAlertPanel extends JPanel {
    
    public BrokenServerAlertPanel() {
        initComponents();
        notAgain.setSelected(!J2EEUISettings.getDefault().isShowAgainBrokenServerAlert());
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

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BrokenServerAlertPanel.class, "ACSN_BrokenServersAlertPanel"));
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BrokenServerAlertPanel.class, "ACSD_BrokenServersAlertPanel"));
        org.openide.awt.Mnemonics.setLocalizedText(notAgain, org.openide.util.NbBundle.getMessage(BrokenServerAlertPanel.class, "MSG_Broken_Server_Again"));
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
        notAgain.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(BrokenServerAlertPanel.class, "ACSN_BrokenServersAlertPanel_notAgain"));
        notAgain.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BrokenServerAlertPanel.class, "ACSD_BrokenServersAlertPanel_notAgain"));

        org.openide.awt.Mnemonics.setLocalizedText(message, org.openide.util.NbBundle.getMessage(BrokenServerAlertPanel.class, "MSG_Broken_Server"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(11, 11, 0, 0);
        add(message, gridBagConstraints);
        message.getAccessibleContext().setAccessibleName(NbBundle.getMessage(BrokenServerAlertPanel.class, "ACSN_BrokenServersAlertPanel"));
        message.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BrokenServerAlertPanel.class, "ACSD_BrokenServersAlertPanel"));

    }// </editor-fold>//GEN-END:initComponents
    
    private void notAgainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_notAgainActionPerformed
        J2EEUISettings.getDefault().setShowAgainBrokenServerAlert(!notAgain.isSelected());
    }//GEN-LAST:event_notAgainActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel message;
    private javax.swing.JCheckBox notAgain;
    // End of variables declaration//GEN-END:variables
    
}
