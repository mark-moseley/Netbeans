/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.wizards;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class ConfigureServerPanelVisual extends JPanel implements ActionListener {
    private static final long serialVersionUID = -105799339751969L;

    private final ConfigureServerPanel wizardPanel;

    public ConfigureServerPanelVisual(ConfigureServerPanel wizardPanel) {
        this.wizardPanel = wizardPanel;

        // Provide a name in the title bar.
        setName(NbBundle.getMessage(ConfigureProjectPanelVisual.class, "LBL_ProjectServer"));
        putClientProperty("WizardPanel_contentSelectedIndex", 1); // NOI18N
        // Step name (actually the whole list for reference).
        putClientProperty("WizardPanel_contentData", wizardPanel.getSteps()); // NOI18N

        initComponents();

        init();
    }

    private void init() {
        copyFilesCheckBox.addActionListener(this);
    }

    private void changeLocalServerFieldsState(boolean state) {
        localServerComboBox.setEnabled(state);
        locateButton.setEnabled(state);
        browseButton.setEnabled(state);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        copyFilesCheckBox = new javax.swing.JCheckBox();
        localServerInfoLabel = new javax.swing.JLabel();
        localServerComboBox = new javax.swing.JComboBox();
        localServerLabel = new javax.swing.JLabel();
        locateButton = new javax.swing.JButton();
        browseButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(copyFilesCheckBox, org.openide.util.NbBundle.getMessage(ConfigureServerPanelVisual.class, "LBL_CopyFiles")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(localServerInfoLabel, org.openide.util.NbBundle.getMessage(ConfigureServerPanelVisual.class, "TXT_LocalServerFolder")); // NOI18N

        localServerComboBox.setEditable(true);

        localServerLabel.setLabelFor(localServerComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(localServerLabel, org.openide.util.NbBundle.getMessage(ConfigureServerPanelVisual.class, "LBL_LocalServerFolder")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(locateButton, org.openide.util.NbBundle.getMessage(ConfigureServerPanelVisual.class, "LBL_LocateLocalServer")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, org.openide.util.NbBundle.getMessage(ConfigureServerPanelVisual.class, "LBL_BrowseLocalServer")); // NOI18N

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(localServerLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(localServerComboBox, 0, 205, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(locateButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton))
                    .add(copyFilesCheckBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 339, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(localServerInfoLabel)))
                .addContainerGap())
        );

        layout.linkSize(new java.awt.Component[] {browseButton, locateButton}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(copyFilesCheckBox)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(localServerInfoLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(browseButton)
                    .add(localServerLabel)
                    .add(locateButton)
                    .add(localServerComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.JCheckBox copyFilesCheckBox;
    private javax.swing.JComboBox localServerComboBox;
    private javax.swing.JLabel localServerInfoLabel;
    private javax.swing.JLabel localServerLabel;
    private javax.swing.JButton locateButton;
    // End of variables declaration//GEN-END:variables

    // listeners
    public void actionPerformed(ActionEvent e) {
        changeLocalServerFieldsState(copyFilesCheckBox.isSelected());
    }
}
