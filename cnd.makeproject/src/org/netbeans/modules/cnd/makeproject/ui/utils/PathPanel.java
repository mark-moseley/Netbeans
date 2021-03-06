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

package org.netbeans.modules.cnd.makeproject.ui.utils;

import org.netbeans.modules.cnd.makeproject.MakeOptions;

public class PathPanel extends javax.swing.JPanel {
    public static final int REL_OR_ABS = 0;
    public static final int REL = 1;
    public static final int ABS = 2;

    /** Creates new form PathPanel */
    public PathPanel() {
        initComponents();
        pathButtonGroup.add(relOrAbsRadioButton);
        pathButtonGroup.add(relRadioButton);
        pathButtonGroup.add(absRadioButton);

	setMode(MakeOptions.getInstance().getPathMode());
    }
    
    public void setMode(int mode) {
	MakeOptions.getInstance().setPathMode(mode);
        if (mode == REL_OR_ABS)
            relOrAbsRadioButton.setSelected(true);
        else if (mode == REL)
            relRadioButton.setSelected(true);
        else
            absRadioButton.setSelected(true);
    }
    
    public static int getMode() {
        return MakeOptions.getInstance().getPathMode();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        pathButtonGroup = new javax.swing.ButtonGroup();
        pathPanel = new javax.swing.JPanel();
        pathLabel = new javax.swing.JLabel();
        relOrAbsRadioButton = new javax.swing.JRadioButton();
        relRadioButton = new javax.swing.JRadioButton();
        absRadioButton = new javax.swing.JRadioButton();

        setLayout(new java.awt.GridBagLayout());

        pathPanel.setLayout(new java.awt.GridBagLayout());

        pathLabel.setDisplayedMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/utils/Bundle").getString("PATHPANEL_PATHLABEL_MNE").charAt(0));
        pathLabel.setLabelFor(pathPanel);
        pathLabel.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/utils/Bundle").getString("PATHPANEL_PATHLABEL_TXT"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pathPanel.add(pathLabel, gridBagConstraints);

        relOrAbsRadioButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/utils/Bundle").getString("PATHPANEL_AUTO_MNE").charAt(0));
        relOrAbsRadioButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/utils/Bundle").getString("PATHPANEL_AUTO_TXT"));
        relOrAbsRadioButton.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/utils/Bundle").getString("PATHPANEL_AUTO_TT"));
        relOrAbsRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                relOrAbsRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pathPanel.add(relOrAbsRadioButton, gridBagConstraints);

        relRadioButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/utils/Bundle").getString("PATHPANEL_REL_MNE").charAt(0));
        relRadioButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/utils/Bundle").getString("PATHPANEL_REL_TXT"));
        relRadioButton.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/utils/Bundle").getString("PATHPANEL_REL_TT"));
        relRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                relRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pathPanel.add(relRadioButton, gridBagConstraints);

        absRadioButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/utils/Bundle").getString("PATHPANEL_ABS_MNE").charAt(0));
        absRadioButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/utils/Bundle").getString("PATHPANEL_ABS_TXT"));
        absRadioButton.setToolTipText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/utils/Bundle").getString("PATHPANEL_ABS_TT"));
        absRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                absRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        pathPanel.add(absRadioButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 4);
        add(pathPanel, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents

    private void absRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_absRadioButtonActionPerformed
	MakeOptions.getInstance().setPathMode(ABS);
    }//GEN-LAST:event_absRadioButtonActionPerformed

    private void relRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_relRadioButtonActionPerformed
	MakeOptions.getInstance().setPathMode(REL);
    }//GEN-LAST:event_relRadioButtonActionPerformed

    private void relOrAbsRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_relOrAbsRadioButtonActionPerformed
	MakeOptions.getInstance().setPathMode(REL_OR_ABS);
    }//GEN-LAST:event_relOrAbsRadioButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton absRadioButton;
    private javax.swing.ButtonGroup pathButtonGroup;
    private javax.swing.JLabel pathLabel;
    private javax.swing.JPanel pathPanel;
    private javax.swing.JRadioButton relOrAbsRadioButton;
    private javax.swing.JRadioButton relRadioButton;
    // End of variables declaration//GEN-END:variables
    
}
