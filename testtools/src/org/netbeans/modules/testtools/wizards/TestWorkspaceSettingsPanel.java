/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.testtools.wizards;

/*
 * TestWorkspaceSettingsPanel.java
 *
 * Created on April 10, 2002, 1:43 PM
 */

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.loaders.TemplateWizard;
import java.awt.CardLayout;
import org.openide.loaders.DataFolder;
import java.io.File;
import javax.swing.JFileChooser;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author  <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class TestWorkspaceSettingsPanel extends javax.swing.JPanel implements WizardDescriptor.FinishPanel {
    
    private boolean stop=true;
    private static final String netbeansPath="../../../nb_all/nbbuild/netbeans";
    private static final String xtestPath="../../../nb_all/xtest";
    private static final String jemmyPath="../../../nbextra/jemmy";
    private static final String jellyPath="../../../nbextra/jelly";
    private String jemmyHome=jemmyPath;
    private String jellyHome=jellyPath;
    private TemplateWizard wizard;

    /** Creates new form TestWorkspacePanel */
    public TestWorkspaceSettingsPanel() {
        initComponents();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        panel = new javax.swing.JPanel();
        levelLabel = new javax.swing.JLabel();
        levelCombo = new javax.swing.JComboBox();
        typeLabel = new javax.swing.JLabel();
        typeField = new javax.swing.JTextField();
        attrLabel = new javax.swing.JLabel();
        attrField = new javax.swing.JTextField();
        separator1 = new javax.swing.JSeparator();
        advancedCheck = new javax.swing.JCheckBox();
        netbeansLabel = new javax.swing.JLabel();
        netbeansField = new javax.swing.JTextField();
        xtestLabel = new javax.swing.JLabel();
        xtestField = new javax.swing.JTextField();
        netbeansButton = new javax.swing.JButton();
        xtestButton = new javax.swing.JButton();
        separator2 = new javax.swing.JSeparator();
        stopLabel = new javax.swing.JLabel();

        setLayout(new java.awt.CardLayout());

        panel.setLayout(new java.awt.GridBagLayout());

        levelLabel.setText("Test Workspace possition in CVS: ");
        levelLabel.setDisplayedMnemonic(87);
        levelLabel.setLabelFor(levelCombo);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        panel.add(levelLabel, gridBagConstraints);

        levelCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "On top of the module (repository / module)", "One level lower (repository / module / package)", "Two levels lower (repository / module / package / package)", "Out of CVS structute (for local use only)" }));
        levelCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                levelComboActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        panel.add(levelCombo, gridBagConstraints);

        typeLabel.setText("Default Test Type: ");
        typeLabel.setDisplayedMnemonic(84);
        typeLabel.setLabelFor(typeField);
        typeLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        panel.add(typeLabel, gridBagConstraints);

        typeField.setEnabled(false);
        typeField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                typeFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        panel.add(typeField, gridBagConstraints);

        attrLabel.setText("Default Attributes: ");
        attrLabel.setDisplayedMnemonic(65);
        attrLabel.setLabelFor(attrField);
        attrLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        panel.add(attrLabel, gridBagConstraints);

        attrField.setEnabled(false);
        attrField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                attrFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        panel.add(attrField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.01;
        gridBagConstraints.weighty = 0.01;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 4);
        panel.add(separator1, gridBagConstraints);

        advancedCheck.setText("Advanced Settings");
        advancedCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                advancedCheckActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weightx = 0.01;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 0);
        panel.add(advancedCheck, gridBagConstraints);

        netbeansLabel.setText("Netbeans Home: ");
        netbeansLabel.setDisplayedMnemonic(78);
        netbeansLabel.setLabelFor(netbeansField);
        netbeansLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        panel.add(netbeansLabel, gridBagConstraints);

        netbeansField.setEnabled(false);
        netbeansField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                netbeansFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        panel.add(netbeansField, gridBagConstraints);

        xtestLabel.setText("XTest Home: ");
        xtestLabel.setDisplayedMnemonic(88);
        xtestLabel.setLabelFor(xtestField);
        xtestLabel.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 0, 4);
        panel.add(xtestLabel, gridBagConstraints);

        xtestField.setEnabled(false);
        xtestField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                xtestFieldFocusGained(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        panel.add(xtestField, gridBagConstraints);

        netbeansButton.setText("...");
        netbeansButton.setPreferredSize(new java.awt.Dimension(30, 20));
        netbeansButton.setMinimumSize(new java.awt.Dimension(30, 20));
        netbeansButton.setEnabled(false);
        netbeansButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                netbeansButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.01;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 4);
        panel.add(netbeansButton, gridBagConstraints);

        xtestButton.setText("...");
        xtestButton.setPreferredSize(new java.awt.Dimension(30, 20));
        xtestButton.setMinimumSize(new java.awt.Dimension(30, 20));
        xtestButton.setEnabled(false);
        xtestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xtestButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.01;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 4);
        panel.add(xtestButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.01;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        panel.add(separator2, gridBagConstraints);

        add(panel, "ok");

        stopLabel.setText("Test Workspace already exists in selected package.");
        stopLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        add(stopLabel, "stop");

    }//GEN-END:initComponents

    private void xtestButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xtestButtonActionPerformed
        File home=WizardIterator.showFileChooser(this, "Select XTest Home Directory", true, false);
        if (home!=null) 
            xtestField.setText(home.getAbsolutePath());
    }//GEN-LAST:event_xtestButtonActionPerformed

    private void netbeansButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_netbeansButtonActionPerformed
        File home=WizardIterator.showFileChooser(this, "Select Netbeans Home Directory", true, false);
        if (home!=null) 
            netbeansField.setText(home.getAbsolutePath());
    }//GEN-LAST:event_netbeansButtonActionPerformed

    private void xtestFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_xtestFieldFocusGained
        xtestField.selectAll();
    }//GEN-LAST:event_xtestFieldFocusGained

    private void netbeansFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_netbeansFieldFocusGained
        netbeansField.selectAll();
    }//GEN-LAST:event_netbeansFieldFocusGained

    private void attrFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_attrFieldFocusGained
        attrField.selectAll();
    }//GEN-LAST:event_attrFieldFocusGained

    private void typeFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_typeFieldFocusGained
        typeField.selectAll();
    }//GEN-LAST:event_typeFieldFocusGained

    private void levelComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_levelComboActionPerformed
        updatePanel();
    }//GEN-LAST:event_levelComboActionPerformed

    private void advancedCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_advancedCheckActionPerformed
        updatePanel();
    }//GEN-LAST:event_advancedCheckActionPerformed

    private void updatePanel() {
        boolean advanced=advancedCheck.isSelected();
        levelLabel.setEnabled(!advanced);
        levelCombo.setEnabled(!advanced);
        typeLabel.setEnabled(advanced);
        typeField.setEnabled(advanced);
        attrLabel.setEnabled(advanced);
        attrField.setEnabled(advanced);
        netbeansLabel.setEnabled(advanced);
        netbeansField.setEnabled(advanced);
        netbeansButton.setEnabled(advanced);
        xtestLabel.setEnabled(advanced);
        xtestField.setEnabled(advanced);
        xtestButton.setEnabled(advanced);
        if (!advanced) {
            switch (levelCombo.getSelectedIndex()) {
                 case 0:netbeansField.setText(netbeansPath);
                        xtestField.setText(xtestPath);
                        jemmyHome=jemmyPath;
                        jellyHome=jellyPath;
                        break;
                 case 1:netbeansField.setText("../"+netbeansPath);
                        xtestField.setText("../"+xtestPath);
                        jemmyHome="../"+jemmyPath;
                        jellyHome="../"+jellyPath;
                        break;
                 case 2:netbeansField.setText("../../"+netbeansPath);
                        xtestField.setText("../../"+xtestPath);
                        jemmyHome="../../"+jemmyPath;
                        jellyHome="../../"+jellyPath;
                        break;
                 case 3:String home=System.getProperty("netbeans.home");
                        netbeansField.setText(home);
                        if (!new File(home+File.separator+"xtest-distribution").exists()) 
                            home=System.getProperty("netbeans.user");
                        xtestField.setText(home+File.separator+"xtest-distribution");
                        jemmyHome=home+File.separator+"lib"+File.separator+"ext";
                        jellyHome=jemmyHome;
                        break;
             }
        }
    }
    
    public void addChangeListener(javax.swing.event.ChangeListener changeListener) {
    }    
    
    public java.awt.Component getComponent() {
        return this;
    }    
    
    public org.openide.util.HelpCtx getHelp() {
        return new HelpCtx(TestWorkspaceSettingsPanel.class);
    }
    
    public void readSettings(Object obj) {
        wizard=(TemplateWizard)obj;
        DataFolder df=null;
        stop=true;
        try {
            df=wizard.getTargetFolder();
            stop=WizardIterator.detectBuildScript(df);
        } catch (Exception e) {}
        if (stop)
            ((CardLayout)getLayout()).show(this, "stop");
        else {
            ((CardLayout)getLayout()).show(this, "ok");
            WizardSettings set=WizardSettings.get(obj);
            if (set.workspaceLevel<0)
                levelCombo.setSelectedIndex(WizardIterator.detectWorkspaceLevel(df));
            if (set.defaultType!=null) 
                typeField.setText(set.defaultType);
            if (set.defaultAttributes!=null) 
                attrField.setText(set.defaultAttributes);
            updatePanel();
        }
    }
    
    public void removeChangeListener(javax.swing.event.ChangeListener changeListener) {
    }
    
    public void storeSettings(Object obj) {
        WizardSettings set=WizardSettings.get(obj);
        set.workspaceLevel=levelCombo.getSelectedIndex();
        set.netbeansHome=netbeansField.getText();
        set.xtestHome=xtestField.getText();
        set.defaultType=typeField.getText();
        set.defaultAttributes=attrField.getText();
        set.typeJemmyHome=jemmyHome;
        set.typeJellyHome=jellyHome;
    }

    public boolean isValid() {
        return !stop;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel stopLabel;
    private javax.swing.JButton xtestButton;
    private javax.swing.JLabel levelLabel;
    private javax.swing.JLabel xtestLabel;
    private javax.swing.JTextField xtestField;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JButton netbeansButton;
    private javax.swing.JCheckBox advancedCheck;
    private javax.swing.JTextField typeField;
    private javax.swing.JPanel panel;
    private javax.swing.JSeparator separator2;
    private javax.swing.JSeparator separator1;
    private javax.swing.JLabel attrLabel;
    private javax.swing.JTextField attrField;
    private javax.swing.JLabel netbeansLabel;
    private javax.swing.JComboBox levelCombo;
    private javax.swing.JTextField netbeansField;
    // End of variables declaration//GEN-END:variables
    
}
