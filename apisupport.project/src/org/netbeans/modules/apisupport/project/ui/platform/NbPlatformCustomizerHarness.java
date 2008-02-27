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

package org.netbeans.modules.apisupport.project.ui.platform;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import org.netbeans.modules.apisupport.project.ui.UIUtil;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 * Permits selection of build harness associated with a NetBeans platform.
 * @author Jesse Glick
 * @see "#71628"
 */
public class NbPlatformCustomizerHarness extends JPanel {
    
    private NbPlatform plaf;
    
    public NbPlatformCustomizerHarness() {
        initComponents();
    }
    
    void setPlatform(NbPlatform plaf) {
        this.plaf = plaf;
        if (plaf.isDefault()) {
            ideButton.setSelected(true);
        } else {
            File harnessLocation = plaf.getHarnessLocation();
            NbPlatform dflt = NbPlatform.getDefaultPlatform();
            if (dflt != null && harnessLocation.equals(dflt.getHarnessLocation())) {
                ideButton.setSelected(true);
            } else if (harnessLocation.equals(plaf.getBundledHarnessLocation())) {
                platformButton.setSelected(true);
            } else {
                otherButton.setSelected(true);
            }
        }
        update();
        UIUtil.setText(otherText, plaf.getHarnessLocation().getAbsolutePath());
    }
    
    private void update() {
        versionText.setText(NbPlatform.getHarnessVersionDisplayName(plaf.getHarnessVersion()));
        if (plaf.isDefault()) {
            platformButton.setEnabled(false);
            otherButton.setEnabled(false);
        } else {
            platformButton.setEnabled(true);
            otherButton.setEnabled(true);
        }
        browseButton.setEnabled(otherButton.isSelected());
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup = new javax.swing.ButtonGroup();
        ideButton = new javax.swing.JRadioButton();
        platformButton = new javax.swing.JRadioButton();
        otherButton = new javax.swing.JRadioButton();
        otherText = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        versionLabel = new javax.swing.JLabel();
        versionText = new javax.swing.JTextField();

        buttonGroup.add(ideButton);
        org.openide.awt.Mnemonics.setLocalizedText(ideButton, NbBundle.getMessage(NbPlatformCustomizerHarness.class, "NbPCH.ideButton")); // NOI18N
        ideButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        ideButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        ideButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ideButtonActionPerformed(evt);
            }
        });

        buttonGroup.add(platformButton);
        org.openide.awt.Mnemonics.setLocalizedText(platformButton, NbBundle.getMessage(NbPlatformCustomizerHarness.class, "NbPCH.platformButton")); // NOI18N
        platformButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        platformButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        platformButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                platformButtonActionPerformed(evt);
            }
        });

        buttonGroup.add(otherButton);
        org.openide.awt.Mnemonics.setLocalizedText(otherButton, NbBundle.getMessage(NbPlatformCustomizerHarness.class, "NbPCH.otherButton")); // NOI18N
        otherButton.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        otherButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        otherButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                otherButtonActionPerformed(evt);
            }
        });

        otherText.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(browseButton, NbBundle.getMessage(NbPlatformCustomizerHarness.class, "NbPCH.browseButton")); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        versionLabel.setLabelFor(versionText);
        org.openide.awt.Mnemonics.setLocalizedText(versionLabel, NbBundle.getMessage(NbPlatformCustomizerHarness.class, "NbPCH.versionLabel")); // NOI18N

        versionText.setEditable(false);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(ideButton)
                            .add(platformButton)
                            .add(layout.createSequentialGroup()
                                .add(otherButton)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(otherText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 220, Short.MAX_VALUE)))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(browseButton))
                    .add(layout.createSequentialGroup()
                        .add(versionLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(versionText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(ideButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(platformButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(otherButton)
                    .add(browseButton)
                    .add(otherText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(versionLabel)
                    .add(versionText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(196, Short.MAX_VALUE))
        );

        ideButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbPlatformCustomizerHarness.class, "ACS_HarnessSuppliedIDE")); // NOI18N
        platformButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbPlatformCustomizerHarness.class, "ACS_HarnessSuppliedPlatform")); // NOI18N
        otherButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbPlatformCustomizerHarness.class, "ACS_HarnessOther")); // NOI18N
        versionLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NbPlatformCustomizerHarness.class, "ACS_HarnessVersion")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        JFileChooser jfc = new JFileChooser() {
            // Trick stolen from ProjectChooserAccessory.ProjectFileChooser:
            public void approveSelection() {
                File dir = FileUtil.normalizeFile(getSelectedFile());
                if (NbPlatform.isHarness(dir)) {
                    super.approveSelection();
                } else {
                    setCurrentDirectory(dir);
                }
            }
        };
        FileUtil.preventFileChooserSymlinkTraversal(jfc, null);
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jfc.setSelectedFile(plaf.getHarnessLocation());
        if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                plaf.setHarnessLocation(FileUtil.normalizeFile(jfc.getSelectedFile()));
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        update();
        UIUtil.setText(otherText, plaf.getHarnessLocation().getAbsolutePath());
    }//GEN-LAST:event_browseButtonActionPerformed

    private void otherButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_otherButtonActionPerformed
        try {
            plaf.setHarnessLocation(FileUtil.normalizeFile(new File(otherText.getText())));
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        update();
    }//GEN-LAST:event_otherButtonActionPerformed

    private void platformButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_platformButtonActionPerformed
        try {
            plaf.setHarnessLocation(plaf.getBundledHarnessLocation());
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        update();
    }//GEN-LAST:event_platformButtonActionPerformed

    private void ideButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ideButtonActionPerformed
        try {
            assert plaf != null;
            NbPlatform dflt = NbPlatform.getDefaultPlatform();
            if (dflt != null) {
                plaf.setHarnessLocation(dflt.getHarnessLocation());
            } else {
                Logger.getLogger(NbPlatformCustomizerHarness.class.getName()).warning("No default platform found"); // #113233
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        update();
    }//GEN-LAST:event_ideButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browseButton;
    private javax.swing.ButtonGroup buttonGroup;
    private javax.swing.JRadioButton ideButton;
    private javax.swing.JRadioButton otherButton;
    private javax.swing.JTextField otherText;
    private javax.swing.JRadioButton platformButton;
    private javax.swing.JLabel versionLabel;
    private javax.swing.JTextField versionText;
    // End of variables declaration//GEN-END:variables
    
}
