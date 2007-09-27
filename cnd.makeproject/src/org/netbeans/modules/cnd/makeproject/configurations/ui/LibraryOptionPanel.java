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

package org.netbeans.modules.cnd.makeproject.configurations.ui;

import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.api.compilers.CompilerSetManager;
import org.openide.util.NbBundle;

public class LibraryOptionPanel extends javax.swing.JPanel {

    /** Creates new form LibraryOptionPanel */
    public LibraryOptionPanel() {
        initComponents();
        buttonGroup.add(staticRadioButton);
        buttonGroup.add(dynamicRadioButton);
        buttonGroup.add(otherRadioButton);
        staticRadioButton.setSelected(true);
        otherTextField.setEnabled(false);
        
        // Accessibility
        getAccessibleContext().setAccessibleDescription(getString("LINKER_OPTIONS_PANEL_SD"));
        dynamicRadioButton.getAccessibleContext().setAccessibleDescription(getString("LINKER_OPTIONS_DYNAMIC_RB_SD"));
        otherRadioButton.getAccessibleContext().setAccessibleDescription(getString("LINKER_OPTIONS_OTHER_RB_SD"));
        otherTextField.getAccessibleContext().setAccessibleDescription(getString("LINKER_OPTIONS_OTHER_TF_SD"));
        staticRadioButton.getAccessibleContext().setAccessibleDescription(getString("LINKER_OPTIONS_STATIC_RB_SD"));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup = new javax.swing.ButtonGroup();
        staticRadioButton = new javax.swing.JRadioButton();
        dynamicRadioButton = new javax.swing.JRadioButton();
        otherRadioButton = new javax.swing.JRadioButton();
        otherTextField = new javax.swing.JTextField();

        setLayout(new java.awt.GridBagLayout());

        staticRadioButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/configurations/ui/Bundle").getString("STATIC_BINDINGS_MN").charAt(0));
        staticRadioButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/configurations/ui/Bundle").getString("STATIC_BINDINGS_TXT"));
        staticRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                staticRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 12);
        add(staticRadioButton, gridBagConstraints);

        dynamicRadioButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/configurations/ui/Bundle").getString("DYNAMIC_BINDINGS_MN").charAt(0));
        dynamicRadioButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/configurations/ui/Bundle").getString("DYNAMIC_BINDINGS_TXT"));
        dynamicRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dynamicRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 12);
        add(dynamicRadioButton, gridBagConstraints);

        otherRadioButton.setMnemonic(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/configurations/ui/Bundle").getString("OTHER_OPTION_MN").charAt(0));
        otherRadioButton.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/configurations/ui/Bundle").getString("OTHER_OPTION_TXT"));
        otherRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                otherRadioButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 12);
        add(otherRadioButton, gridBagConstraints);

        otherTextField.setColumns(32);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 34, 12, 12);
        add(otherTextField, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents

    private void otherRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_otherRadioButtonActionPerformed
        otherTextField.setEnabled(true);
    }//GEN-LAST:event_otherRadioButtonActionPerformed

    private void dynamicRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dynamicRadioButtonActionPerformed
        otherTextField.setEnabled(false);
    }//GEN-LAST:event_dynamicRadioButtonActionPerformed

    private void staticRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_staticRadioButtonActionPerformed
        otherTextField.setEnabled(false);
    }//GEN-LAST:event_staticRadioButtonActionPerformed
    
    public String getOption(MakeConfiguration conf) {
        if (dynamicRadioButton.isSelected()) {
            if (CompilerSetManager.getDefault().getCompilerSet(conf.getCompilerSet().getValue()).isSunCompiler())
                return "-Bdynamic"; // NOI18N
            else if (CompilerSetManager.getDefault().getCompilerSet(conf.getCompilerSet().getValue()).isGnuCompiler())
                return "-dynamic"; // NOI18N
            else
                assert false;
            return ""; // NOI18N
        }
        else if (staticRadioButton.isSelected()) {
            if (CompilerSetManager.getDefault().getCompilerSet(conf.getCompilerSet().getValue()).isSunCompiler())
                return "-Bstatic"; // NOI18N
            else if (CompilerSetManager.getDefault().getCompilerSet(conf.getCompilerSet().getValue()).isGnuCompiler())
                return "-static"; // NOI18N
            else
                assert false;
            return ""; // NOI18N
        }
        else
            return otherTextField.getText();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JRadioButton dynamicRadioButton;
    private javax.swing.JRadioButton otherRadioButton;
    private javax.swing.JTextField otherTextField;
    private javax.swing.JRadioButton staticRadioButton;
    // End of variables declaration//GEN-END:variables
    
    private static String getString(String s) {
        return NbBundle.getBundle(LibraryOptionPanel.class).getString(s);
    }
}
