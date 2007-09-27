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
package org.netbeans.modules.cnd.discovery.wizard;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JPanel;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public class ConsolidationStrategyPanel extends JPanel {
    private ConsolidationStrategyWizard wizard;
    public static final String PROJECT_LEVEL = "project"; // NOI18N
    public static final String FOLDER_LEVEL = "folder"; // NOI18N
    public static final String FILE_LEVEL = "file"; // NOI18N
    private String level = FILE_LEVEL;
    
    public ConsolidationStrategyPanel(ConsolidationStrategyWizard wizard) {
        this.wizard = wizard;
        initComponents();
        addListeners();
        fileConsolidation.setSelected(true);
        update(FILE_LEVEL);
    }

    private void addListeners(){
        fileConsolidation.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                update(FILE_LEVEL);
            }
        });
        folderConsolidation.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                update(FOLDER_LEVEL);
            }
        });
        projectConsolidation.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                update(PROJECT_LEVEL);
            }
        });
    }
    
    void read(DiscoveryDescriptor wizardDescriptor) {
    }
    
    void store(DiscoveryDescriptor wizardDescriptor) {
        wizardDescriptor.setLevel(level);
    }
    
    boolean valid(DiscoveryDescriptor settings) {
        // TOD: remove when folder can be configured
        return true;
    }

    private void update(String level) {
        this.level = level;
        wizard.stateChanged(null);
      	String description = NbBundle.getMessage(ConsolidationStrategyPanel.class,
                "ConsolidationDescription_"+level); // NOI18N
        instructionsTextArea.setText(description);
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        consolidationGroup = new javax.swing.ButtonGroup();
        instructionPanel = new javax.swing.JPanel();
        instructionsTextArea = new javax.swing.JTextArea();
        projectConsolidation = new javax.swing.JRadioButton();
        folderConsolidation = new javax.swing.JRadioButton();
        fileConsolidation = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        instructionPanel.setLayout(new java.awt.GridBagLayout());

        instructionPanel.setVerifyInputWhenFocusTarget(false);
        instructionsTextArea.setBackground(instructionPanel.getBackground());
        instructionsTextArea.setEditable(false);
        instructionsTextArea.setLineWrap(true);
        instructionsTextArea.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        instructionPanel.add(instructionsTextArea, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(instructionPanel, gridBagConstraints);

        consolidationGroup.add(projectConsolidation);
        org.openide.awt.Mnemonics.setLocalizedText(projectConsolidation, java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/discovery/wizard/Bundle").getString("ConsolidateToProjectLabel"));
        projectConsolidation.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        projectConsolidation.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 0);
        add(projectConsolidation, gridBagConstraints);

        consolidationGroup.add(folderConsolidation);
        org.openide.awt.Mnemonics.setLocalizedText(folderConsolidation, java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/discovery/wizard/Bundle").getString("ConsolidateToFolderLabel"));
        folderConsolidation.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        folderConsolidation.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 0);
        add(folderConsolidation, gridBagConstraints);

        consolidationGroup.add(fileConsolidation);
        org.openide.awt.Mnemonics.setLocalizedText(fileConsolidation, java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/discovery/wizard/Bundle").getString("FileConsolidateLabel"));
        fileConsolidation.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        fileConsolidation.setMargin(new java.awt.Insets(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 12, 0, 0);
        add(fileConsolidation, gridBagConstraints);

        jLabel1.setLabelFor(projectConsolidation);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/discovery/wizard/Bundle").getString("ConsolidationLevelText"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(jLabel1, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup consolidationGroup;
    private javax.swing.JRadioButton fileConsolidation;
    private javax.swing.JRadioButton folderConsolidation;
    private javax.swing.JPanel instructionPanel;
    private javax.swing.JTextArea instructionsTextArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JRadioButton projectConsolidation;
    // End of variables declaration//GEN-END:variables
    
}
