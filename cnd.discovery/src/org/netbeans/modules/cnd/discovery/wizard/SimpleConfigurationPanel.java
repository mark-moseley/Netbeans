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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.discovery.wizard;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexander Simon
 */
public class SimpleConfigurationPanel extends javax.swing.JPanel {
    private SimpleConfigurationWizard wizard;
    
    
    /** Creates new form SimpleConfigurationPanel */
    public SimpleConfigurationPanel(SimpleConfigurationWizard wizard) {
        this.wizard = wizard;
        initComponents();
        configurationComboBox.addItem(new ConfigutationItem("project",getString("CONFIGURATION_LEVEL_project"))); // NOI18N
        configurationComboBox.addItem(new ConfigutationItem("folder",getString("CONFIGURATION_LEVEL_folder"))); // NOI18N
        configurationComboBox.addItem(new ConfigutationItem("file",getString("CONFIGURATION_LEVEL_file"))); // NOI18N
        configurationComboBox.setSelectedIndex(2);
        addListeners();
    }
    
    private void addListeners(){
        DocumentListener documentListener = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                update(e);
            }
            
            public void removeUpdate(DocumentEvent e) {
                update(e);
            }
            
            public void changedUpdate(DocumentEvent e) {
                update(e);
            }
        };
        librariesTextField.getDocument().addDocumentListener(documentListener);
    }
    
    private void update(DocumentEvent e) {
        wizard.stateChanged(null);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        instructionPanel = new javax.swing.JPanel();
        instructionsTextArea = new javax.swing.JTextArea();
        discoveryPanel = new javax.swing.JPanel();
        configurationComboBox = new javax.swing.JComboBox();
        configurationLabel = new javax.swing.JLabel();
        librariesLabel = new javax.swing.JLabel();
        librariesTextField = new javax.swing.JTextField();
        additionalLibrariesButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        instructionPanel.setLayout(new java.awt.GridBagLayout());

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

        discoveryPanel.setLayout(new java.awt.GridBagLayout());

        configurationComboBox.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                configurationComboBoxItemStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        discoveryPanel.add(configurationComboBox, gridBagConstraints);

        configurationLabel.setLabelFor(configurationComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(configurationLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/discovery/wizard/Bundle").getString("ConfigurationLevelLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        discoveryPanel.add(configurationLabel, gridBagConstraints);

        librariesLabel.setLabelFor(librariesTextField);
        org.openide.awt.Mnemonics.setLocalizedText(librariesLabel, java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/discovery/wizard/Bundle").getString("AdditionalLibrariesLabel"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        discoveryPanel.add(librariesLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        discoveryPanel.add(librariesTextField, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(additionalLibrariesButton, java.util.ResourceBundle.getBundle("org/netbeans/modules/cnd/discovery/wizard/Bundle").getString("LIBRARY_BROWSE_BUTTON_TXT"));
        additionalLibrariesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                additionalLibrariesButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        discoveryPanel.add(additionalLibrariesButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(discoveryPanel, gridBagConstraints);

    }// </editor-fold>//GEN-END:initComponents
    
    private void configurationComboBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_configurationComboBoxItemStateChanged
        Object item = evt.getItem();
        if (item instanceof ConfigutationItem) {
            ConfigutationItem conf = (ConfigutationItem)item;
            instructionsTextArea.setText(getString("SimpleInstructionText_"+conf.getID())); // NOI18N
        }
    }//GEN-LAST:event_configurationComboBoxItemStateChanged
    
    private void additionalLibrariesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_additionalLibrariesButtonActionPerformed
        StringTokenizer tokenizer = new StringTokenizer(librariesTextField.getText(), ";"); // NOI18N
        List<String> list = new ArrayList<String>();
        while (tokenizer.hasMoreTokens()) {
            list.add(tokenizer.nextToken());
        }
        AdditionalLibrariesListPanel panel = new AdditionalLibrariesListPanel(list.toArray());
        DialogDescriptor dialogDescriptor = new DialogDescriptor(AdditionalLibrariesListPanel.wrapPanel(panel),
                getString("ADDITIONAL_LIBRARIES_TXT"));
        DialogDisplayer.getDefault().notify(dialogDescriptor);
        if (dialogDescriptor.getValue() == DialogDescriptor.OK_OPTION) {
            Vector newList = panel.getListData();
            String includes = ""; // NOI18N
            for (int i = 0; i < newList.size(); i++) {
                if (i > 0)
                    includes += ";"; // NOI18N
                includes += newList.elementAt(i);
            }
            librariesTextField.setText(includes);
        }
    }//GEN-LAST:event_additionalLibrariesButtonActionPerformed
    
    private String getString(String key) {
        return NbBundle.getBundle(SimpleConfigurationPanel.class).getString(key);
    }
    
    void read(final DiscoveryDescriptor wizardDescriptor) {
        String providerID = wizardDescriptor.getProviderID();
        if ("dwarf-executable".equals(providerID)){ // NOI18N
            additionalLibrariesButton.setVisible(true);
            librariesLabel.setVisible(true);
            librariesTextField.setVisible(true);
        } else if ("dwarf-folder".equals(providerID)){ // NOI18N
            additionalLibrariesButton.setVisible(false);
            librariesLabel.setVisible(false);
            librariesTextField.setVisible(false);
        }
    }
    
    void store(DiscoveryDescriptor wizardDescriptor) {
        ConfigutationItem level = (ConfigutationItem)configurationComboBox.getSelectedItem();
        wizardDescriptor.setLevel(level.getID());
        wizardDescriptor.setAditionalLibraries(librariesTextField.getText());
    }
    
    boolean valid() {
        StringTokenizer st = new StringTokenizer(librariesTextField.getText());
        while(st.hasMoreTokens()){
            String path = st.nextToken();
            File file = new File(path);
            if (!(file.exists() && file.isFile())){
                return false;
            }
        }
        return true;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton additionalLibrariesButton;
    private javax.swing.JComboBox configurationComboBox;
    private javax.swing.JLabel configurationLabel;
    private javax.swing.JPanel discoveryPanel;
    private javax.swing.JPanel instructionPanel;
    private javax.swing.JTextArea instructionsTextArea;
    private javax.swing.JLabel librariesLabel;
    private javax.swing.JTextField librariesTextField;
    // End of variables declaration//GEN-END:variables
    
    private static class ConfigutationItem {
        private String ID;
        private String name;
        private ConfigutationItem(String ID, String name){
            this.ID = ID;
            this.name = name;
        }
        public String toString(){
            return name;
        }
        public String getID(){
            return ID;
        }
    }
    
}
