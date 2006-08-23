/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.makeproject.ui.wizards;

import java.io.File;
import org.openide.util.HelpCtx;
import org.openide.WizardDescriptor;

public class SourceFoldersPanel extends javax.swing.JPanel implements HelpCtx.Provider{
    SourceFoldersDescriptorPanel sourceFoldersDescriptorPanel;
    SourceFilesPanel sourceFilesPanel;
    private String baseDir;

    public SourceFoldersPanel(SourceFoldersDescriptorPanel sourceFoldersDescriptorPanel) {
        initComponents();
	this.sourceFoldersDescriptorPanel = sourceFoldersDescriptorPanel;
	sourceFilesPanel = new SourceFilesPanel();
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        headerFoldersOuterPanel.add(sourceFilesPanel, gridBagConstraints);
        instructionsTextArea.setBackground(instructionPanel.getBackground());
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(SourceFoldersPanel.class);
    }

    void read(WizardDescriptor settings) {
	File projectDir = (File)settings.getProperty("projdir"); // NOI18N
	baseDir = projectDir.getPath();
        String wd = (String)settings.getProperty("buildCommandWorkingDirTextField"); // NOI18N
        sourceFilesPanel.setSeed(baseDir, wd);
    }

    void store(WizardDescriptor wizardDescriptor) {
	wizardDescriptor.putProperty("sourceFolders", sourceFilesPanel.getListData()); // NOI18N
    }
    
    boolean valid(WizardDescriptor settings) {
	return true;
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        headerFoldersOuterPanel = new javax.swing.JPanel();
        instructionPanel = new javax.swing.JPanel();
        instructionsTextArea = new javax.swing.JTextArea();

        setLayout(new java.awt.GridBagLayout());

        setPreferredSize(new java.awt.Dimension(323, 223));
        headerFoldersOuterPanel.setLayout(new java.awt.GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(headerFoldersOuterPanel, gridBagConstraints);

        instructionPanel.setLayout(new java.awt.GridBagLayout());

        instructionsTextArea.setEditable(false);
        instructionsTextArea.setLineWrap(true);
        instructionsTextArea.setText(java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/makeproject/ui/wizards/Bundle").getString("SourceFoldersInstructions"));
        instructionsTextArea.setWrapStyleWord(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        instructionPanel.add(instructionsTextArea, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 12, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        add(instructionPanel, gridBagConstraints);

    }//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel headerFoldersOuterPanel;
    private javax.swing.JPanel instructionPanel;
    private javax.swing.JTextArea instructionsTextArea;
    // End of variables declaration//GEN-END:variables
    
}
